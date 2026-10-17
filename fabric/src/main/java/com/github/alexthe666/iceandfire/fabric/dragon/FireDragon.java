package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Initial migration of lifecycle and ownership; combat and rendering are migrated separately. */
public final class FireDragon extends TamableAnimal {
    private static final EntityDataAccessor<Integer> AGE_TICKS = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MALE = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AGING_DISABLED = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private int appliedAge = -1;
    private int riderControls;
    private long controlsReceivedAt;
    private int takeoffTicks;
    private int flightTicks;
    private float glidingSpeedBonus;
    public int walkCycle;
    public int flightCycle;
    public float flyProgress;
    public float prevFlyProgress;
    public float sitProgress;
    public float prevSitProgress;

    public FireDragon(EntityType<? extends FireDragon> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.FOLLOW_RANGE, 128)
                .add(Attributes.ARMOR, 1);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AGE_TICKS, 0);
        builder.define(VARIANT, 0);
        builder.define(HUNGER, 50);
        builder.define(MALE, false);
        builder.define(AGING_DISABLED, false);
        builder.define(FLYING, false);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public int ageDays() { return DragonGrowth.days(entityData.get(AGE_TICKS)); }
    public int stage() { return DragonGrowth.stage(ageDays()); }
    public int variant() { return entityData.get(VARIANT); }
    public void setVariant(int value) { entityData.set(VARIANT, Math.clamp(value, 0, 3)); }
    public boolean male() { return entityData.get(MALE); }
    public void setMale(boolean value) { entityData.set(MALE, value); }
    public int hunger() { return entityData.get(HUNGER); }
    public void setHunger(int value) { entityData.set(HUNGER, Math.clamp(value, 0, 100)); }
    public boolean flying() { return entityData.get(FLYING); }
    public void setRiderControls(int flags) {
        riderControls = flags & DragonControlPayload.ALL;
        controlsReceivedAt = level().getGameTime();
    }
    private boolean control(int flag) { return (riderControls & flag) != 0; }

    public void growOneDay() {
        if (entityData.get(AGING_DISABLED)) return;
        long next = (long) (ageDays() + 1) * DragonGrowth.TICKS_PER_DAY;
        entityData.set(AGE_TICKS, (int) Math.min(Integer.MAX_VALUE, next));
        updateGrowth();
    }

    private void updateGrowth() {
        if (appliedAge == ageDays()) return;
        appliedAge = ageDays();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(DragonGrowth.maxHealth(appliedAge, 500));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(DragonGrowth.attackDamage(appliedAge, 17));
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(DragonGrowth.movementSpeed(appliedAge));
        getAttribute(Attributes.ARMOR).setBaseValue(1 + 19 / 125F * appliedAge);
        getAttribute(Attributes.SCALE).setBaseValue(DragonGrowth.scale(appliedAge));
    }

    @Override public void tick() {
        super.tick();
        prevFlyProgress = flyProgress;
        prevSitProgress = sitProgress;
        flyProgress = Math.clamp(flyProgress + (flying() ? 0.5F : -2F), 0, 20);
        sitProgress = Math.clamp(sitProgress + (isOrderedToSit() ? 0.5F : -0.5F), 0, 20);
        if (level().isClientSide()) walkCycle = walkCycle < 39 ? walkCycle + 1 : 0;
        flightCycle = flightCycle < 58 ? flightCycle + 2 : 0;
        if (!level().isClientSide()) {
            int age = entityData.get(AGE_TICKS);
            if (!entityData.get(AGING_DISABLED) && age < Integer.MAX_VALUE) entityData.set(AGE_TICKS, age + 1);
            updateGrowth();
            if (tickCount % 3000 == 0) setHunger(hunger() - 1);
            if (getControllingPassenger() == null || level().getGameTime() - controlsReceivedAt > 10) riderControls = 0;
            takeoffTicks = control(DragonControlPayload.UP) ? takeoffTicks + 1 : 0;
            if (!flying() && takeoffTicks > 20 && DragonGrowth.canCarryRider(ageDays())) {
                entityData.set(FLYING, true);
                setDeltaMovement(getDeltaMovement().add(0, 0.8, 0));
                flightTicks = 0;
            }
            if (flying()) {
                flightTicks++;
                if (onGround() && flightTicks > 20 && !control(DragonControlPayload.UP)) {
                    entityData.set(FLYING, false);
                    flightTicks = 0;
                }
            }
        }
        setNoGravity(flying());
    }

    @Override public void travel(Vec3 input) {
        if (!flying()) { super.travel(input); return; }
        LivingEntity rider = getControllingPassenger();
        if (isLocalInstanceAuthoritative()) {
            double speed = getAttributeValue(Attributes.MOVEMENT_SPEED);
            // Original EntityDragonBase air-speed curve, local acceleration and drag.
            float flightSpeed = (float) (speed * (5.2F + (speed - 0.15F) / (0.4F - 0.15F) * 1.5F));
            double forward = input.z;
            double strafe = input.x * 0.4;
            double vertical = 0;
            if (rider != null) {
                forward = rider.zza * (rider.zza > 0 ? 1 : 0.5F);
                strafe = rider.xxa * 0.4;
                if (rider.isSprinting()) {
                    flightSpeed *= 1.5F;
                    strafe = rider.xxa * 0.1;
                    glidingSpeedBonus = (float) Math.clamp(glidingSpeedBonus - getDeltaMovement().y * 0.05, -0.8, 1.5);
                    forward = Math.abs(Math.cos(Math.toRadians(getXRot())));
                    vertical = -Math.sin(Math.toRadians(getXRot()));
                }
                boolean up = control(DragonControlPayload.UP);
                boolean down = control(DragonControlPayload.DOWN);
                if (up != down) vertical = up ? 1 : -1;
                else if (up) vertical = 0;
            }
            flightSpeed = (flightSpeed + glidingSpeedBonus) * 0.1F;
            glidingSpeedBonus *= 0.99F;
            setSpeed(flightSpeed);
            moveRelative(flightSpeed, new Vec3(strafe, vertical, forward));
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
        } else {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override protected void checkFallDamage(double distance, boolean grounded, BlockState state, BlockPos pos) {
        // Dragons in the original mod do not take fall damage.
    }

    @Override public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player && isOwnedBy(player) ? player : null;
    }

    @Override protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        setYRot(player.getYRot());
        setXRot(player.getXRot() * 0.5F);
        yRotO = getYRot();
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    @Override protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override protected Vec3 getRiddenInput(Player player, Vec3 input) {
        return new Vec3(player.xxa * 0.5F, 0, player.zza < 0 ? player.zza * 0.25F : player.zza);
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isOwnedBy(player)) {
            if (stack.is(IafContent.DRAGON_MEAL)) {
                if (!level().isClientSide()) {
                    growOneDay();
                    setHunger(hunger() + 20);
                    heal(Math.min(getHealth(), (int) (getMaxHealth() / 2)));
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.isEmpty() && !player.isShiftKeyDown() && DragonGrowth.canCarryRider(ageDays())) {
                if (!level().isClientSide()) {
                    setOrderedToSit(false);
                    player.startRiding(this, true, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("AgeTicks", entityData.get(AGE_TICKS));
        output.putInt("Variant", variant());
        output.putInt("Hunger", hunger());
        output.putBoolean("Gender", male());
        output.putBoolean("AgingDisabled", entityData.get(AGING_DISABLED));
        output.putBoolean("Flying", flying());
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(AGE_TICKS, Math.max(0, input.getIntOr("AgeTicks", 0)));
        setVariant(input.getIntOr("Variant", 0));
        setHunger(input.getIntOr("Hunger", 50));
        setMale(input.getBooleanOr("Gender", false));
        entityData.set(AGING_DISABLED, input.getBooleanOr("AgingDisabled", false));
        entityData.set(FLYING, input.getBooleanOr("Flying", false) && DragonGrowth.canCarryRider(ageDays()));
        appliedAge = -1;
        updateGrowth();
    }
}
