package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
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
    private static final EntityDataAccessor<Boolean> HOVERING = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> FLIGHT_PITCH = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BREATHING = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARMOR_PARTS = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER_CARRIER = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER_SLOT = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private BlockPos dragonHome;
    private String homeDimension = "";
    private Player shoulderPlayer;
    public final net.minecraft.world.SimpleContainer dragonInventory = new net.minecraft.world.SimpleContainer(5) {
        @Override public void setChanged() {
            super.setChanged();
            if (FireDragon.this.dragonInventory != null) updateArmor();
        }
    };
    private final DragonCombatState combat = new DragonCombatState();
    final DragonFlightManager flightManager = new DragonFlightManager(this);
    public float hoverProgress, prevHoverProgress;
    public float breathProgress, prevBreathProgress;
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
    public int walkCycle;
    public int flightCycle;
    public float flyProgress;
    public float prevFlyProgress;
    public float sitProgress;
    public float prevSitProgress;
    public float shoulderProgress, prevShoulderProgress;

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
        builder.define(HOVERING, false);
        builder.define(FLIGHT_PITCH, 0F);
        builder.define(BREATHING, false);
        builder.define(ATTACK, DragonCombatState.NONE);
        builder.define(ATTACK_TICK, 0);
        builder.define(ARMOR_PARTS, 0);
        builder.define(SHOULDER_CARRIER, -1);
        builder.define(SHOULDER_SLOT, 0);
        builder.define(COMMAND, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new DragonReturnToRoostGoal(this));
        goalSelector.addGoal(4, new DragonEscortGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1) {
            { setInterval(20); }
            @Override public boolean canUse() { return canMoveOnGround() && command() != 2 && (level().isBrightOutside() || !hasHome()) && !flightManager.wantsRoost() && super.canUse(); }
            @Override public boolean canContinueToUse() { return canMoveOnGround() && command() != 2 && (level().isBrightOutside() || !hasHome()) && !flightManager.wantsRoost() && super.canContinueToUse(); }
        });
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
    public boolean agingDisabled() { return entityData.get(AGING_DISABLED); }
    public int command() { return entityData.get(COMMAND); }
    public void setCommand(int value) {
        entityData.set(COMMAND, Math.clamp(value, 0, 2));
        setOrderedToSit(command() == 1);
        flightManager.commandChanged();
        getNavigation().stop();
    }
    public boolean canMoveOnGround() {
        return canMoveAutonomously() && !flying();
    }
    public boolean canMoveAutonomously() {
        return isAlive() && !isNoAi() && command() != 1 && !isVehicle() && !isPassenger() && shoulderCarrier() == null;
    }
    public boolean hasDragonHome() { return dragonHome != null; }
    public String homeDimension() { return homeDimension; }
    @Override public boolean hasHome() { return dragonHome != null && homeDimension.equals(level().dimension().identifier().toString()); }
    @Override public BlockPos getHomePosition() { return hasHome() ? dragonHome : super.getHomePosition(); }
    @Override public int getHomeRadius() { return hasHome() ? 40 : super.getHomeRadius(); }
    @Override public boolean isWithinHome(BlockPos pos) {
        return !hasHome() || dragonHome.distSqr(pos) < (double) getHomeRadius() * getHomeRadius();
    }
    @Override public boolean isWithinHome(Vec3 pos) {
        return !hasHome() || dragonHome.distToCenterSqr(pos) < (double) getHomeRadius() * getHomeRadius();
    }
    public void setHunger(int value) { entityData.set(HUNGER, Math.clamp(value, 0, 100)); }
    public boolean flying() { return entityData.get(FLYING); }
    public boolean hovering() { return entityData.get(HOVERING); }
    void setAutonomousFlight(boolean airborne, boolean hovering) {
        entityData.set(FLYING, airborne);
        entityData.set(HOVERING, airborne && hovering);
    }
    public int attackAnimation() { return entityData.get(ATTACK); }
    public int attackAnimationTick() { return entityData.get(ATTACK_TICK); }
    public int armorParts() { return entityData.get(ARMOR_PARTS); }
    public int shoulderSlot() { return entityData.get(SHOULDER_SLOT); }
    public Player shoulderCarrier() {
        if (!level().isClientSide()) return shoulderPlayer;
        return level().getEntity(entityData.get(SHOULDER_CARRIER)) instanceof Player player ? player : null;
    }
    public void releaseShoulder() {
        shoulderPlayer = null;
        entityData.set(SHOULDER_CARRIER, -1);
        noPhysics = false;
        setNoGravity(flying());
        setDeltaMovement(Vec3.ZERO);
    }
    public float flightPitch(float partialTick) {
        return previousFlightPitch + (flightPitch - previousFlightPitch) * partialTick;
    }

    @Override public boolean isFlyingVehicle() { return flying(); }

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
        updateArmor();
        getAttribute(Attributes.SCALE).setBaseValue(DragonGrowth.scale(appliedAge));
        getAttribute(Attributes.CAMERA_DISTANCE).setBaseValue(DragonRiderLayout.cameraDistance(appliedAge));
    }

    @Override public void tick() {
        Player carrier = shoulderCarrier();
        if (!level().isClientSide() && carrier != null && (!carrier.isAlive() || carrier.isRemoved()
                || carrier.level() != level() || !isAlive() || !isOwnedBy(carrier)
                || !DragonShoulderLayout.canBeCarried(ageDays()) || isPassenger()
                || (!carrier.isPassenger() && (carrier.isShiftKeyDown() || carrier.isFallFlying())))) {
            releaseShoulder();
            carrier = null;
        }
        noPhysics = carrier != null;
        previousFlightPitch = flightPitch;
        if (!level().isClientSide()) {
            boolean hasRider = getControllingPassenger() != null;
            if (!hasRider || level().getGameTime() - controlsReceivedAt > 10) {
                riderControls = 0;
                takeoff.reset();
            }
            if (hasRider && takeoff.tick(DragonGrowth.canCarryRider(ageDays()), flying(),
                    control(DragonControlPayload.UP), control(DragonControlPayload.DOWN))) {
                setAutonomousFlight(true, false);
                flightTicks = 0;
                launchPending = true;
            }
        }
        if (level() instanceof ServerLevel) flightManager.tick();
        // Flight must be active before vanilla processes this tick's ridden movement.
        setNoGravity(flying() || carrier != null);
        super.tick();
        if (level() instanceof ServerLevel server) tickCombat(server);
        prevBreathProgress = breathProgress;
        breathProgress = Math.clamp(breathProgress + (entityData.get(BREATHING) ? 0.5F : -0.5F), 0, 10);
        prevFlyProgress = flyProgress;
        prevHoverProgress = hoverProgress;
        hoverProgress = Math.clamp(hoverProgress + (hovering() ? 0.5F : -2F), 0, 20);
        prevSitProgress = sitProgress;
        prevShoulderProgress = shoulderProgress;
        shoulderProgress = Math.clamp(shoulderProgress + (carrier != null ? 0.5F : -0.5F), 0, 20);
        flyProgress = Math.clamp(flyProgress + (flying() && !hovering() ? 0.5F : -2F), 0, 20);
        // orderedToSit is a server-only vanilla field; COMMAND is synchronized to observers.
        sitProgress = Math.clamp(sitProgress + (command() == 1 && !flying() ? 0.5F : -0.5F), 0, 20);
        if (level().isClientSide()) walkCycle = walkCycle < 39 ? walkCycle + 1 : 0;
        flightCycle = flightCycle < 58 ? flightCycle + 2 : 0;
        if (!level().isClientSide()) {
            int age = entityData.get(AGE_TICKS);
            if (!entityData.get(AGING_DISABLED) && age < Integer.MAX_VALUE) entityData.set(AGE_TICKS, age + 1);
            updateGrowth();
            if (tickCount % 3000 == 0) setHunger(hunger() - 1);
            if (flying()) {
                flightTicks++;
                if (getControllingPassenger() != null && onGround() && flightTicks > 20 && !control(DragonControlPayload.UP)) {
                    entityData.set(FLYING, false);
                    flightTicks = 0;
                }
            }
        }
        setNoGravity(flying() || carrier != null);
        if (carrier != null) {
            setDeltaMovement(Vec3.ZERO);
            setPos(carrier.position().add(DragonShoulderLayout.offset(shoulderSlot(), carrier.yBodyRot,
                    carrier.isShiftKeyDown(), carrier.getScale())));
            setYRot(carrier.yHeadRot);
            yBodyRot = yHeadRot = carrier.yHeadRot;
            setXRot(0);
        }
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

    public Vec3 headPosition() {
        return position().add(DragonCombatGeometry.headOffset(DragonGrowth.renderSize(ageDays()),
                getYRot(), flightPitch, flyProgress, sitProgress));
    }

    private void updateArmor() {
        if (level().isClientSide()) return;
        int packed = 0;
        double bonus = 1;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = dragonInventory.getItem(slot + 1);
            int ordinal = DragonArmorItem.fits(stack, slot + 1) ? DragonArmorItem.ordinal(stack) : 0;
            packed |= ordinal << (slot * 4);
            bonus += DragonArmorItem.bonus(ordinal);
        }
        entityData.set(ARMOR_PARTS, packed);
        getAttribute(Attributes.ARMOR).setBaseValue(1 + 19 / 125F * ageDays() + bonus);
    }

    @Override protected void dropCustomDeathLoot(ServerLevel server, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(server, source, recentlyHit);
        for (ItemStack stack : dragonInventory.removeAllItems()) if (!stack.isEmpty()) spawnAtLocation(server, stack);
    }

    private void tickCombat(ServerLevel server) {
        LivingEntity rider = getControllingPassenger();
        boolean active = isAlive() && rider != null && DragonGrowth.canCarryRider(ageDays()) && !isInWater();
        boolean fire = active && control(DragonControlPayload.FIRE);
        combat.tick(active, fire, stage());
        if (active && control(DragonControlPayload.STRIKE)) {
            combat.start(DragonCombatState.BITE);
            LivingEntity target = DragonFireCombat.biteTarget(this, rider);
            if (target != null) {
                float damage = (int) getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (target.hurtServer(server, damageSources().mobAttack(this), damage)
                        && DragonCombatConfig.current.canDragonsHealFromBiting) heal(damage * 0.1F);
            }
        } else if (fire && combat.breathing()) {
            // The original rider attack chooses the charge branch with probability 1/5 each tick.
            if (random.nextInt(5) == 0) {
                if (combat.animation() != DragonCombatState.CHARGE) combat.start(DragonCombatState.CHARGE);
                else if (combat.animationTick() == 20) {
                    Vec3 direction = rider.getLookAngle().add(random.nextGaussian() * 0.0075,
                            random.nextGaussian() * 0.0075, random.nextGaussian() * 0.0075).normalize();
                    DragonFireCharge charge = IafContent.FIRE_DRAGON_CHARGE.create(server, EntitySpawnReason.TRIGGERED);
                    if (charge != null) {
                        charge.setOwner(this);
                        charge.setPos(DragonFireCombat.clip(this, getEyePosition(), headPosition()));
                        charge.setDeltaMovement(direction.scale(0.07));
                        server.addFreshEntity(charge);
                    }
                    playSound(IafContent.FIRE_BREATH, 4, 1);
                }
            }
            if (combat.emitting()) {
                Vec3 head = DragonFireCombat.clip(this, getEyePosition(), headPosition());
                DragonFireCombat.breath(server, this, head, DragonFireCombat.aim(this, rider, 10 * stage()), combat.streamProgress());
                if (tickCount % 5 == 0) playSound(IafContent.FIRE_BREATH, 4, 1);
            }
        }
        entityData.set(BREATHING, combat.breathing());
        entityData.set(ATTACK, combat.animation());
        entityData.set(ATTACK_TICK, combat.animationTick());
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FLYING.equals(key)) launchPending = flying() && getControllingPassenger() != null;
    }

    @Override public void travel(Vec3 input) {
        if (isPassenger() || shoulderCarrier() != null) { setDeltaMovement(Vec3.ZERO); return; }
        if (!flying()) { launchPending = false; super.travel(input); return; }
        if (getControllingPassenger() == null) {
            launchPending = false;
            if (isLocalInstanceAuthoritative()) {
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(0.9));
            } else setDeltaMovement(Vec3.ZERO);
            return;
        }
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

    @Override protected boolean couldAcceptPassenger() {
        // Unlike canAddPassenger, vanilla checks this even for forced mounts and NBT loading.
        // Dragons have only one passenger seat: the pilot's saddle.
        return !isVehicle();
    }

    @Override protected boolean canAddPassenger(Entity passenger) {
        return couldAcceptPassenger() && passenger instanceof Player player
                && isOwnedBy(player) && DragonGrowth.canCarryRider(ageDays());
    }

    @Override protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
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
        if (!isAlive()) return InteractionResult.PASS;
        if (stack.is(IafContent.CREATIVE_DRAGON_MEAL)) {
            if (!level().isClientSide()) {
                tame(player);
                setHunger(hunger() + 20);
                heal(Math.min(getHealth(), (int) (getMaxHealth() / 2)));
                feedingEffects(stack, false, true);
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (isOwnedBy(player)) {
            if (stack.is(IafContent.DRAGON_STAFF)) {
                if (!level().isClientSide()) {
                    if (player.isShiftKeyDown()) {
                        if (hasDragonHome()) {
                            dragonHome = null;
                            homeDimension = "";
                            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("dragon.command.remove_home"));
                        } else {
                            dragonHome = blockPosition().immutable();
                            homeDimension = level().dimension().identifier().toString();
                            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("dragon.command.new_home",
                                    dragonHome.getX(), dragonHome.getY(), dragonHome.getZ(), homeDimension));
                        }
                    } else {
                        setCommand((command() + 1) % 3);
                        playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_INFECT, 1, 1);
                        player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("dragon.command."
                                + (command() == 1 ? "sit" : command() == 2 ? "escort" : "stand")));
                    }
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.isEmpty() && player.isShiftKeyDown()) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) DragonMenu.open(serverPlayer, this);
                return InteractionResult.SUCCESS;
            }
            if (stack.is(IafContent.DRAGON_MEAL)) {
                if (!level().isClientSide()) {
                    growOneDay();
                    setHunger(hunger() + 20);
                    heal(Math.min(getHealth(), (int) (getMaxHealth() / 2)));
                    feedingEffects(stack, false, true);
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(IafContent.SICKLY_DRAGON_MEAL) && !agingDisabled()) {
                if (!level().isClientSide()) {
                    setHunger(hunger() + 20);
                    heal(getMaxHealth());
                    entityData.set(AGING_DISABLED, true);
                    feedingEffects(stack, true, true);
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }
            int foodPoints = DragonFeeding.foodPoints(stack);
            if (foodPoints > 0 && (hunger() < 100 || getHealth() < getMaxHealth())) {
                if (!level().isClientSide()) {
                    setHunger(hunger() + foodPoints);
                    setHealth(Math.min(getMaxHealth(), (int) (getHealth() + foodPoints / 10)));
                    feedingEffects(stack, false, false);
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.isEmpty() && !player.isShiftKeyDown() && DragonGrowth.canCarryRider(ageDays())) {
                if (!level().isClientSide() && canAddPassenger(player) && player.startRiding(this)) {
                    setCommand(0);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.isEmpty() && !player.isShiftKeyDown() && DragonShoulderLayout.canBeCarried(ageDays())) {
                if (isPassenger() || shoulderCarrier() != null || !isAlive()) return InteractionResult.FAIL;
                if (level() instanceof ServerLevel server) {
                    boolean[] occupied = new boolean[DragonShoulderLayout.CAPACITY];
                    for (Entity entity : server.getAllEntities()) {
                        if (entity instanceof FireDragon baby && baby.shoulderCarrier() == player)
                            occupied[baby.shoulderSlot()] = true;
                    }
                    int slot = 0;
                    while (slot < occupied.length && occupied[slot]) slot++;
                    if (slot == occupied.length) return InteractionResult.FAIL;
                    shoulderPlayer = player;
                    entityData.set(SHOULDER_CARRIER, player.getId());
                    entityData.set(SHOULDER_SLOT, slot);
                    setCommand(0);
                    getNavigation().stop();
                    setPos(player.position().add(DragonShoulderLayout.offset(slot, player.yBodyRot, false, player.getScale())));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override public boolean isFood(ItemStack stack) { return false; }

    private void feedingEffects(ItemStack stack, boolean sickly, boolean meal) {
        playSound(sickly ? net.minecraft.sounds.SoundEvents.ZOMBIE_VILLAGER_CURE : net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), 1, 1);
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 head = headPosition();
        for (var item : meal ? new net.minecraft.world.item.Item[]{stack.getItem(), net.minecraft.world.item.Items.BONE, net.minecraft.world.item.Items.BONE_MEAL}
                : new net.minecraft.world.item.Item[]{stack.getItem()}) {
            server.sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM, item),
                    head.x, head.y, head.z, 6, 0.15, 0.15, 0.15, 0.05);
        }
        if (sickly) server.sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,
                net.minecraft.world.item.Items.POISONOUS_POTATO), head.x, head.y, head.z, 12, 0.15, 0.15, 0.15, 0.05);
    }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("AgeTicks", entityData.get(AGE_TICKS));
        output.putInt("Variant", variant());
        output.putInt("Hunger", hunger());
        output.putBoolean("Gender", male());
        output.putBoolean("AgingDisabled", entityData.get(AGING_DISABLED));
        output.putBoolean("Flying", flying());
        output.putBoolean("Hovering", hovering());
        output.putInt("Command", command());
        output.putBoolean("HasHomePosition", hasDragonHome());
        if (dragonHome != null) {
            output.putInt("HomeAreaX", dragonHome.getX());
            output.putInt("HomeAreaY", dragonHome.getY());
            output.putInt("HomeAreaZ", dragonHome.getZ());
            output.putString("HomeDimension", homeDimension);
        }
        net.minecraft.world.ContainerHelper.saveAllItems(output.child("DragonInventory"), dragonInventory.getItems());
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Carried babies remain independently saved world entities; loading releases them.
        releaseShoulder();
        entityData.set(AGE_TICKS, Math.max(0, input.getIntOr("AgeTicks", 0)));
        setVariant(input.getIntOr("Variant", 0));
        setHunger(input.getIntOr("Hunger", 50));
        setMale(input.getBooleanOr("Gender", false));
        entityData.set(AGING_DISABLED, input.getBooleanOr("AgingDisabled", false));
        flightManager.reset();
        setAutonomousFlight(input.getBooleanOr("Flying", false) && stage() >= 2, input.getBooleanOr("Hovering", false));
        setCommand(input.getIntOr("Command", isOrderedToSit() ? 1 : 0));
        dragonHome = input.getBooleanOr("HasHomePosition", false)
                ? new BlockPos(input.getIntOr("HomeAreaX", 0), input.getIntOr("HomeAreaY", 0), input.getIntOr("HomeAreaZ", 0)) : null;
        homeDimension = input.getStringOr("HomeDimension", level().dimension().identifier().toString());
        dragonInventory.clearContent();
        net.minecraft.world.ContainerHelper.loadAllItems(input.childOrEmpty("DragonInventory"), dragonInventory.getItems());
        for (int slot = 0; slot < 5; slot++) {
            ItemStack stack = dragonInventory.getItem(slot);
            if (!stack.isEmpty() && !DragonArmorItem.fits(stack, slot)) dragonInventory.setItem(slot, ItemStack.EMPTY);
        }
        appliedAge = -1;
        updateGrowth();
        updateArmor();
    }
}
