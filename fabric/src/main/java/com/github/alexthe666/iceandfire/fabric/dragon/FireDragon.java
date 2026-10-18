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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;

/** Initial migration of lifecycle and ownership; combat and rendering are migrated separately. */
public final class FireDragon extends TamableAnimal {
    private static final EntityDataAccessor<Integer> AGE_TICKS = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MALE = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AGING_DISABLED = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> FLIGHT_PITCH = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BACK_OCCUPIED = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private int appliedAge = -1;
    private int riderControls;
    private long controlsReceivedAt;
    private final DragonTakeoff takeoff = new DragonTakeoff();
    private int flightTicks;
    private float glidingSpeedBonus;
    private float riderWalkingLift;
    private boolean launchPending;
    private Vec3 lastFlightPosition;
    private float flightPitch;
    private float previousFlightPitch;
    private int backStillTicks;
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
                .add(Attributes.CAMERA_DISTANCE, 4)
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
        builder.define(FLIGHT_PITCH, 0F);
        builder.define(BACK_OCCUPIED, false);
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
    public float flightPitch(float partialTick) {
        return previousFlightPitch + (flightPitch - previousFlightPitch) * partialTick;
    }

    @Override public boolean isFlyingVehicle() { return flying(); }

    @Override public boolean canBeCollidedWith(Entity other) {
        // EntitySelector also queries collisions with no requesting entity.
        return isAlive() && (other == null || !isPassengerOfSameVehicle(other));
    }

    private void updateBackPlatform() {
        if (level().isClientSide()) return;
        boolean occupied = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.1, 2, 0.1),
                player -> !player.isSpectator() && !player.noPhysics && !player.isPassenger()
                        && DragonBackPlatform.isOnBack(getBoundingBox(), player.getBoundingBox())).size() > 0;
        backStillTicks = occupied ? 10 : Math.max(0, backStillTicks - 1);
        entityData.set(BACK_OCCUPIED, backStillTicks > 0);
        if (backStillTicks > 0) getNavigation().stop();
    }
    public void setRiderControls(int flags) {
        takeoff.acceptControls(flags);
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
        getAttribute(Attributes.CAMERA_DISTANCE).setBaseValue(DragonRiderLayout.cameraDistance(appliedAge));
    }

    @Override public void tick() {
        previousFlightPitch = flightPitch;
        updateBackPlatform();
        if (!level().isClientSide()) {
            boolean hasRider = getControllingPassenger() != null;
            if (!hasRider || level().getGameTime() - controlsReceivedAt > 10) {
                riderControls = 0;
                takeoff.reset();
            }
            if (hasRider && takeoff.tick(DragonGrowth.canCarryRider(ageDays()), flying(),
                    entityData.get(BACK_OCCUPIED), control(DragonControlPayload.UP), control(DragonControlPayload.DOWN))) {
                entityData.set(FLYING, true);
                flightTicks = 0;
                launchPending = true;
            }
        }
        // Flight must be active before vanilla processes this tick's ridden movement.
        setNoGravity(flying());
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
            if (flying()) {
                flightTicks++;
                if (onGround() && flightTicks > 20 && !control(DragonControlPayload.UP)) {
                    entityData.set(FLYING, false);
                    flightTicks = 0;
                }
            }
        }
        setNoGravity(flying());
        if (!level().isClientSide() || isLocalInstanceAuthoritative()) {
            Vec3 movement = lastFlightPosition == null ? Vec3.ZERO : position().subtract(lastFlightPosition);
            flightPitch = DragonFlightMotion.nextPitch(flightPitch, movement.y,
                    Math.abs(movement.x) + Math.abs(movement.z), flying());
            if (flightPitch > 0) flightPitch = DragonFlightMotion.limitDescentPitch(
                    flightPitch, descentClearance(), DragonGrowth.renderSize(ageDays()));
            if (!level().isClientSide()) entityData.set(FLIGHT_PITCH, flightPitch);
        } else flightPitch = entityData.get(FLIGHT_PITCH);
        lastFlightPosition = position();
        LivingEntity rider = getControllingPassenger();
        riderWalkingLift = !flying() && rider != null && rider.zza > 0
                ? Math.min(DragonRiderLayout.walkingLiftLimit(ageDays()), riderWalkingLift + 0.1F)
                : Math.max(0, riderWalkingLift - 0.15F);
    }

    private double descentClearance() {
        float size = DragonGrowth.renderSize(ageDays());
        double range = DragonFlightMotion.descentFadeDistance(size);
        double yaw = Math.toRadians(getYRot());
        double halfWidth = getBbWidth() * 0.5;
        double clearance = range;
        // Sample the body footprint and the forward neck/head, including ledges and slabs.
        // Nine block-shape rays per tick only while pitching down, never per render frame.
        for (double forward : new double[]{0, size * 0.275, size * 0.55}) {
            for (double side : new double[]{-halfWidth, 0, halfWidth}) {
                double x = getX() - Math.sin(yaw) * forward + Math.cos(yaw) * side;
                double z = getZ() + Math.cos(yaw) * forward + Math.sin(yaw) * side;
                Vec3 start = new Vec3(x, getBoundingBox().maxY, z);
                Vec3 end = new Vec3(x, getY() - range, z);
                var hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (hit.getType() != HitResult.Type.MISS) clearance = Math.min(clearance,
                        Math.max(0, getY() - hit.getLocation().y));
            }
        }
        return clearance;
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FLYING.equals(key)) launchPending = flying() && getControllingPassenger() != null;
    }

    @Override public void travel(Vec3 input) {
        if (entityData.get(BACK_OCCUPIED)) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (!flying()) { launchPending = false; super.travel(input); return; }
        LivingEntity rider = getControllingPassenger();
        if (isLocalInstanceAuthoritative()) {
            // Keep the launch request until the simulator actually leaves the ground.
            var motion = getDeltaMovement();
            boolean rising = control(DragonControlPayload.UP) && !control(DragonControlPayload.DOWN);
            setDeltaMovement(motion.x, DragonFlightMotion.takeoffVelocity(motion.y, launchPending, onGround(), rising), motion.z);
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
            if (!onGround()) launchPending = false;
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
        if (entityData.get(BACK_OCCUPIED)) return;
        setYRot(player.getYRot());
        setXRot(player.getXRot() * 0.5F);
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    @Override protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override protected Vec3 getRiddenInput(Player player, Vec3 input) {
        return new Vec3(player.xxa * 0.5F, 0, player.zza < 0 ? player.zza * 0.25F : player.zza);
    }

    @Override protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        if (passenger != getControllingPassenger()) return super.getPassengerAttachmentPoint(passenger, dimensions, scale);
        // The default positionRider subtracts the passenger's vehicle attachment afterwards.
        // Return the seat in world units; physical SCALE is capped and must not scale it again.
        return DragonRiderLayout.offset(ageDays(), getYRot(), flying(), riderWalkingLift,
                passenger.getXRot(), passenger.getBbHeight()).add(passenger.getVehicleAttachmentPoint(this));
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
