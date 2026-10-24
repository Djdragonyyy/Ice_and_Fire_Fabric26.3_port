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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
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
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> BANNER = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> GRIPPED_PREY = SynchedEntityData.defineId(FireDragon.class, EntityDataSerializers.INT);
    private java.util.UUID loadedPrey;
    private int loadedGripTick;
    private boolean gripImpactDone;
    private long sleepNotBefore;
    public float sleepProgress, prevSleepProgress;
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
    private LivingEntity autonomousBiteTarget;
    private boolean autonomousBiteHit, wasAutonomousCombat;
    private int nextMeleeAttack = DragonCombatState.BITE;
    private int lastWingImpact = -1;
    private int eatingTicks;
    boolean seekingFood;
    private DragonFoodGoal foodGoal;
    private LivingEntity previousAutonomousTarget;
    private boolean rangedChargePending, autonomousCharging;
    private DragonFlightManager.AirAttack previousAirAttack;
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
        builder.define(SLEEPING, false);
        builder.define(BANNER, ItemStack.EMPTY);
        builder.define(GRIPPED_PREY, -1);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new DragonMateGoal(this));
        goalSelector.addGoal(3, new DragonReturnToRoostGoal(this));
        goalSelector.addGoal(4, new DragonEscortGoal(this));
        goalSelector.addGoal(5, new DragonMeleeGoal(this));
        foodGoal = new DragonFoodGoal(this);
        goalSelector.addGoal(6, foodGoal);
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1) {
            { setInterval(20); }
            @Override public boolean canUse() { return canMoveOnGround() && command() != 2 && (level().isBrightOutside() || !hasHome()) && !flightManager.wantsRoost() && super.canUse(); }
            @Override public boolean canContinueToUse() { return canMoveOnGround() && command() != 2 && (level().isBrightOutside() || !hasHome()) && !flightManager.wantsRoost() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8) {
            @Override public boolean canUse() { return !dragonSleeping() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !dragonSleeping() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(9, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() { return !dragonSleeping() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !dragonSleeping() && super.canContinueToUse(); }
        });
        // Original defensive priorities stay above food and non-defensive hunting.
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(5, new DragonHuntGoal(this, true));
        targetSelector.addGoal(6, new DragonHuntGoal(this, false));
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
    public boolean dragonSleeping() { return entityData.get(SLEEPING); }
    void setDragonSleeping(boolean value) {
        entityData.set(SLEEPING, value);
        if (value) { getNavigation().stop(); seekingFood = false; }
    }
    public void setCommand(int value) {
        if (!level().isClientSide()) releaseGrip(true);
        setDragonSleeping(false);
        entityData.set(COMMAND, Math.clamp(value, 0, 2));
        setOrderedToSit(command() == 1);
        flightManager.commandChanged();
        getNavigation().stop();
        if (command() == 1) setTarget(null);
    }
    @Override public boolean canAttack(LivingEntity target) {
        return autonomousControlsAllowed() && target != null && target.isAlive() && target.level() == level()
                && !DragonFireCombat.friendly(this, target) && super.canAttack(target);
    }
    boolean withinBiteReach(LivingEntity target) {
        // IafDragonLogic.updateDragonAttack: render-size reach, not the capped collision scale.
        double reach = DragonGrowth.renderSize(ageDays()) * 0.2574 * 2 + 2;
        return distanceToSqr(target) < reach * reach;
    }
    public boolean canMoveOnGround() {
        return canMoveAutonomously() && !flying();
    }
    boolean hasPriorityFood() { return foodGoal != null && foodGoal.canUse(); }
    public boolean canMoveAutonomously() {
        return autonomousControlsAllowed() && !dragonSleeping() && attackAnimation() != DragonCombatState.SHAKE;
    }
    private boolean autonomousControlsAllowed() {
        return isAlive() && !isNoAi() && command() != 1 && (!isVehicle() || grippedPrey() != null)
                && !isPassenger() && shoulderCarrier() == null;
    }
    @Override public void setTarget(LivingEntity target) {
        LivingEntity old = getTarget();
        super.setTarget(target);
        if (target != null || old != null) {
            sleepNotBefore = level().getGameTime() + 20;
            setDragonSleeping(false);
        }
    }
    @Override public boolean hurtServer(ServerLevel server, net.minecraft.world.damagesource.DamageSource source, float damage) {
        boolean hurt = super.hurtServer(server, source, damage);
        if (hurt) { setDragonSleeping(false); sleepNotBefore = level().getGameTime() + 20; }
        return hurt;
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
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(DragonGrowth.maxHealth(appliedAge, DragonCombatConfig.current.dragonHealth));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(DragonGrowth.attackDamage(appliedAge, DragonCombatConfig.current.dragonAttackDamage));
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(DragonGrowth.movementSpeed(appliedAge));
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(Math.min(2048, DragonCombatConfig.current.dragonTargetSearchLength));
        getAttribute(Attributes.ARMOR).setBaseValue(1 + 19 / 125F * appliedAge);
        updateArmor();
        getAttribute(Attributes.SCALE).setBaseValue(DragonGrowth.scale(appliedAge));
        getAttribute(Attributes.CAMERA_DISTANCE).setBaseValue(DragonRiderLayout.cameraDistance(appliedAge));
    }

    @Override public void tick() {
        if (!level().isClientSide() && loadedPrey != null) restoreGrip();
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
        if (level() instanceof ServerLevel server) {
            tickSleep(server);
            LivingEntity target = getTarget();
            if (target != null && (!canAttack(target)
                    || distanceTo(target) > getAttributeValue(Attributes.FOLLOW_RANGE))) setTarget(null);
            flightManager.tick();
        }
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
        prevSleepProgress = sleepProgress;
        sleepProgress = Math.clamp(sleepProgress + (dragonSleeping() ? 0.5F : -0.5F), 0, 20);
        prevShoulderProgress = shoulderProgress;
        shoulderProgress = Math.clamp(shoulderProgress + (carrier != null ? 0.5F : -0.5F), 0, 20);
        flyProgress = Math.clamp(flyProgress + (flying() && !hovering() ? 0.5F : -2F), 0, 20);
        // orderedToSit is a server-only vanilla field; COMMAND is synchronized to observers.
        sitProgress = Math.clamp(sitProgress + (command() == 1 && !flying() && !dragonSleeping() ? 0.5F : -0.5F), 0, 20);
        if (level().isClientSide()) walkCycle = walkCycle < 39 ? walkCycle + 1 : 0;
        flightCycle = flightCycle < 58 ? flightCycle + 2 : 0;
        if (!level().isClientSide()) {
            int age = entityData.get(AGE_TICKS);
            if (!entityData.get(AGING_DISABLED) && age < Integer.MAX_VALUE) entityData.set(AGE_TICKS, age + 1);
            updateGrowth();
            if (tickCount % DragonCombatConfig.current.dragonHungerTickRate == 0) setHunger(hunger() - 1);
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

    private void tickSleep(ServerLevel server) {
        if (isNoAi()) return;
        boolean supported = onGround() && !server.noCollision(this, getBoundingBox().move(0, -0.1, 0));
        if (!DragonCombatConfig.current.doDragonsSleep || !isAlive() || flying() || isInWater() || !supported
                || isVehicle() || isPassenger() || shoulderCarrier() != null || command() != 0 || getTarget() != null || isInLove()
                || attackAnimation() == DragonCombatState.SHAKE) {
            setDragonSleeping(false);
            return;
        }
        if (server.isBrightOutside()) {
            if (isTame() || server.canSeeSkyFromBelowWater(blockPosition())) setDragonSleeping(false);
            return;
        }
        if (dragonSleeping()) {
            var nearby = server.getNearestPlayer(this, getBoundingBox().getSize() * 3);
            if (nearby != null && !DragonFireCombat.friendly(this, nearby)) {
                setDragonSleeping(false);
                setTarget(nearby);
            }
            return;
        }
        if (server.getGameTime() < sleepNotBefore || getLastHurtByMob() != null) return;
        if (hasHome() && position().distanceToSqr(Vec3.atCenterOf(getHomePosition())) > getBbWidth() * 10) return;
        setDragonSleeping(true);
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
        ItemStack banner = dragonInventory.getItem(0);
        entityData.set(BANNER, DragonArmorItem.fits(banner, 0) ? banner.copyWithCount(1) : ItemStack.EMPTY);
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

    /** Render snapshot only; inventory remains the sole persisted/drop source. */
    public ItemStack dragonBanner() { return entityData.get(BANNER).copy(); }

    @Override protected void dropCustomDeathLoot(ServerLevel server, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(server, source, recentlyHit);
        for (ItemStack stack : dragonInventory.removeAllItems()) if (!stack.isEmpty()) spawnAtLocation(server, stack);
    }

    private void tickCombat(ServerLevel server) {
        if (combat.animation() == DragonCombatState.SHAKE || grippedPrey() != null) {
            tickGrip(server);
            syncCombat();
            return;
        }
        LivingEntity rider = getControllingPassenger();
        if (rider == null) {
            tickAutonomousCombat(server);
            syncCombat();
            return;
        }
        if (wasAutonomousCombat) combat.reset();
        wasAutonomousCombat = false;
        autonomousBiteTarget = null;
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
        syncCombat();
    }

    private void tickAutonomousCombat(ServerLevel server) {
        LivingEntity target = getTarget();
        if (target != null && flying() && canAttack(target) && !isInWater()) {
            tickAirCombat(server, target);
            return;
        }
        if (previousAirAttack != null) {
            combat.reset();
            previousAutonomousTarget = null;
            rangedChargePending = autonomousCharging = false;
        }
        previousAirAttack = null;
        boolean active = target != null && canAttack(target) && !flying() && !isInWater();
        if (!wasAutonomousCombat) combat.reset();
        wasAutonomousCombat = true;
        if (!active || previousAutonomousTarget != target) {
            combat.reset();
            autonomousBiteTarget = null;
            nextMeleeAttack = DragonCombatState.BITE;
            rangedChargePending = autonomousCharging = false;
        }
        previousAutonomousTarget = target;
        boolean ranged = active && stage() > 1 && !withinBiteReach(target) && hasLineOfSight(target)
                && distanceTo(target) <= 10 * stage();
        if (autonomousCharging) {
            if (!ranged) { combat.reset(); autonomousCharging = false; return; }
            combat.tick(true, false, stage());
            if (combat.animationTick() == 20 && combat.animation() == DragonCombatState.CHARGE) shootAutonomousCharge(server, target);
            if (combat.animation() == DragonCombatState.NONE) autonomousCharging = false;
            return;
        }
        if (rangedChargePending && ranged && combat.animation() == DragonCombatState.NONE) {
            rangedChargePending = false;
            autonomousCharging = true;
            combat.reset();
            combat.start(DragonCombatState.CHARGE);
            return;
        }
        boolean fire = active && stage() > 1 && !withinBiteReach(target) && hasLineOfSight(target)
                && distanceTo(target) <= 10 * stage() && combat.animation() == DragonCombatState.NONE;
        if (combat.breathing() && active && !withinBiteReach(target) && hasLineOfSight(target)
                && distanceTo(target) <= 10 * stage()) fire = true;
        if (!fire && combat.breathing()) combat.reset();
        boolean wasBreathing = combat.breathing();
        combat.tick(active, fire, stage());
        // Keep the 1/5 original charge branch stable for its full animation.
        // Select it between streams, so a new warmup cannot cancel frame 20.
        if (wasBreathing && !combat.breathing() && fire) rangedChargePending = random.nextInt(5) == 0;
        if (!active) return;
        if (fire) {
            if (combat.emitting()) {
                Vec3 head = DragonFireCombat.clip(this, getEyePosition(), headPosition());
                DragonFireCombat.breath(server, this, head, target.getEyePosition(), combat.streamProgress());
                if (tickCount % 5 == 0) playSound(IafContent.FIRE_BREATH, 4, 1);
            }
            return;
        }
        if (combat.animation() == DragonCombatState.NONE && withinBiteReach(target) && hasLineOfSight(target)) {
            int chosen = nextMeleeAttack;
            nextMeleeAttack = stage() == 1 ? DragonCombatState.BITE : new int[]{DragonCombatState.BITE, DragonCombatState.TAIL,
                    DragonCombatState.WING, DragonCombatState.SHAKE}[random.nextInt(4)];
            if (chosen == DragonCombatState.SHAKE && random.nextInt(2) == 0 && tryGrabPrey(target)) return;
            combat.start(chosen == DragonCombatState.SHAKE ? DragonCombatState.BITE : chosen);
            autonomousBiteTarget = target;
            autonomousBiteHit = false;
            lastWingImpact = -1;
        }
        // Original impact window is ticks 16..24. One successful hit per bite,
        // with reach, visibility and ownership checked again at the impact.
        boolean wing = combat.animation() == DragonCombatState.WING;
        if (DragonCombatState.meleeImpact(combat.animation(), combat.animationTick())
                && (wing ? lastWingImpact != combat.animationTick() : !autonomousBiteHit)
                && autonomousBiteTarget == target && withinBiteReach(target) && hasLineOfSight(target)) {
            float damage = (int) getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (target.hurtServer(server, damageSources().mobAttack(this), damage)) {
                autonomousBiteHit = true;
                lastWingImpact = combat.animationTick();
                if (combat.animation() == DragonCombatState.BITE && DragonCombatConfig.current.canDragonsHealFromBiting) heal(damage * 0.1F);
                if (combat.animation() == DragonCombatState.TAIL || wing) {
                    float strength = stage() * 0.6F;
                    double angle = Math.toRadians(getYRot());
                    target.knockback(strength, Math.sin(angle), -Math.cos(angle), damageSources().mobAttack(this), damage);
                }
            }
        }
    }

    private void shootAutonomousCharge(ServerLevel server, LivingEntity target) {
        Vec3 head = DragonFireCombat.clip(this, getEyePosition(), headPosition());
        Vec3 direction = target.getEyePosition().subtract(head).normalize().add(
                random.nextGaussian() * 0.0075, random.nextGaussian() * 0.0075, random.nextGaussian() * 0.0075).normalize();
        DragonFireCharge charge = IafContent.FIRE_DRAGON_CHARGE.create(server, EntitySpawnReason.TRIGGERED);
        if (charge == null) return;
        charge.setOwner(this);
        charge.setPos(head);
        charge.setDeltaMovement(direction.scale(0.07));
        server.addFreshEntity(charge);
        playSound(IafContent.FIRE_BREATH, 4, 1);
    }

    private void tickAirCombat(ServerLevel server, LivingEntity target) {
        if (!flightManager.attacking()) { combat.reset(); return; }
        var mode = flightManager.airAttack();
        if (!wasAutonomousCombat || previousAutonomousTarget != target || previousAirAttack != mode) combat.reset();
        wasAutonomousCombat = true;
        previousAutonomousTarget = target;
        previousAirAttack = mode;
        autonomousBiteTarget = null;
        rangedChargePending = autonomousCharging = false;
        boolean visible = hasLineOfSight(target);
        if (mode == DragonFlightManager.AirAttack.TACKLE) {
            combat.reset();
            if (visible && getBoundingBox().inflate(2).intersects(target.getBoundingBox())) {
                target.hurtServer(server, damageSources().mobAttack(this), stage() * 3);
                setAutonomousFlight(false, false);
                flightManager.reset();
            }
            return;
        }
        boolean ranged = visible && distanceTo(target) <= 10 * stage();
        if (!ranged) { combat.reset(); return; }
        if (mode == DragonFlightManager.AirAttack.HOVER_BLAST) {
            combat.tick(true, false, stage());
            if (combat.animation() == DragonCombatState.NONE) combat.start(DragonCombatState.CHARGE);
            else if (combat.animationTick() == 20) shootAutonomousCharge(server, target);
        } else {
            combat.tick(true, true, stage());
            if (combat.emitting()) {
                Vec3 head = DragonFireCombat.clip(this, getEyePosition(), headPosition());
                DragonFireCombat.breath(server, this, head, target.getEyePosition(), combat.streamProgress());
                if (tickCount % 5 == 0) playSound(IafContent.FIRE_BREATH, 4, 1);
            }
        }
    }

    private void syncCombat() {
        entityData.set(BREATHING, combat.breathing());
        boolean eating = eatingTicks > 0 && combat.animation() == DragonCombatState.NONE && !combat.breathing();
        boolean tackle = flying() && canMoveAutonomously() && getTarget() != null
                && flightManager.attacking() && flightManager.airAttack() == DragonFlightManager.AirAttack.TACKLE;
        entityData.set(ATTACK, tackle ? DragonCombatState.TACKLE : eating ? DragonCombatState.EAT : combat.animation());
        entityData.set(ATTACK_TICK, tackle ? Math.min(40, flightManager.combatTicks()) : eating ? 20 - eatingTicks : combat.animationTick());
        if (eatingTicks > 0) eatingTicks--;
    }

    void eatDroppedFood(net.minecraft.world.entity.item.ItemEntity item) {
        if (!(level() instanceof ServerLevel) || !canMoveOnGround() || command() != 0 || getTarget() != null
                || hunger() >= 100 || !item.isAlive() || item.hasPickUpDelay()) return;
        int points = DragonFeeding.foodPoints(item.getItem());
        if (points <= 0 || !hasLineOfSight(item)) return;
        if (distanceToSqr(item) >= getBbWidth() * 2 + getBbHeight() / 2
                && headPosition().distanceToSqr(item.position()) >= getBbHeight()) return;
        setHunger(hunger() + points);
        setHealth(Math.min(getMaxHealth(), (int) (getHealth() + points)));
        feedingEffects(item.getItem(), false, false);
        eatingTicks = 20;
        item.getItem().shrink(1);
        if (item.getItem().isEmpty()) item.discard();
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FLYING.equals(key)) launchPending = flying() && getControllingPassenger() != null;
    }

    @Override public void travel(Vec3 input) {
        if (isPassenger() || shoulderCarrier() != null) { setDeltaMovement(Vec3.ZERO); return; }
        if (dragonSleeping()) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            super.travel(Vec3.ZERO);
            setDeltaMovement(0, getDeltaMovement().y, 0);
            return;
        }
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
        // One occupant: the owner's pilot seat or an explicitly registered combat prey.
        return !isVehicle();
    }

    @Override protected boolean canAddPassenger(Entity passenger) {
        return couldAcceptPassenger() && (passenger instanceof Mob && passenger.getId() == entityData.get(GRIPPED_PREY)
                || passenger instanceof Player player && isOwnedBy(player) && DragonGrowth.canCarryRider(ageDays()));
    }

    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (isAlive() && onGround() && !flying()) {
            double radius = (getBbWidth() + passenger.getBbWidth()) / 2 + 1;
            for (int ring = 0; ring < 3; ring++) for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(getYRot() + i * 45);
                for (int dy : new int[]{0, 1, -1, -2}) {
                    BlockPos pos = BlockPos.containing(getX() + Math.cos(angle) * (radius + ring),
                            getY() + dy, getZ() + Math.sin(angle) * (radius + ring));
                    if (!level().hasChunkAt(pos) || !level().hasChunkAt(pos.above(2))) continue;
                    Vec3 safe = net.minecraft.world.entity.vehicle.DismountHelper.findSafeDismountLocation(
                            passenger.getType(), level(), pos, true);
                    if (safe == null) continue;
                    var box = passenger.getBoundingBox().move(safe.subtract(passenger.position()));
                    if (!box.intersects(getBoundingBox()) && level().getWorldBorder().isWithinBounds(box)
                            && level().noCollision(passenger, box) && !level().containsAnyLiquid(box)) return safe;
                }
            }
            // No supported side is available: keep vanilla's exit above the body,
            // protecting only this grounded dismount's fall. Never grant this in flight.
            passenger.setIgnoreFallDamageFromCurrentImpulse(true, passenger.position());
        }
        return super.getDismountLocationForPassenger(passenger);
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
        if (passenger == grippedPrey()) return DragonPreyMotion.offset(DragonGrowth.renderSize(ageDays()), getYRot(), attackAnimationTick())
                .add(passenger.getVehicleAttachmentPoint(this));
        if (passenger != getControllingPassenger()) return super.getPassengerAttachmentPoint(passenger, dimensions, scale);
        // The default positionRider subtracts the passenger's vehicle attachment afterwards.
        // Return the seat in world units; physical SCALE is capped and must not scale it again.
        return DragonRiderLayout.offset(ageDays(), getYRot(), flying(), riderWalkingLift,
                passenger.getXRot(), passenger.getBbHeight()).add(passenger.getVehicleAttachmentPoint(this));
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isAlive()) return InteractionResult.PASS;
        if (!level().isClientSide() && (isOwnedBy(player) || stack.is(IafContent.CREATIVE_DRAGON_MEAL))) {
            setDragonSleeping(false);
            sleepNotBefore = level().getGameTime() + 20;
        }
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
        if (stack.is(IafContent.FIRE_STEW)) {
            if (stage() < 4 || getAge() != 0 || !canFallInLove() || isInLove()) return InteractionResult.FAIL;
            if (!level().isClientSide()) {
                setDragonSleeping(false);
                sleepNotBefore = level().getGameTime() + 20;
                setInLove(player);
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

    @Override public boolean isFood(ItemStack stack) { return stack.is(IafContent.FIRE_STEW); }
    boolean readyToMate() {
        return stage() >= 4 && getAge() == 0 && isInLove() && canMoveOnGround() && onGround()
                && command() == 0 && getTarget() == null && !isInWater();
    }
    @Override public boolean canMate(Animal other) {
        return other instanceof FireDragon partner && partner != this && partner.level() == level()
                && partner.getType() == getType() && male() != partner.male()
                && readyToMate() && partner.readyToMate();
    }
    void finishBreeding() {
        setAge(6000);
        resetLove();
        sleepNotBefore = level().getGameTime() + 20;
        getNavigation().stop();
    }

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
        output.putBoolean("Sleeping", dragonSleeping());
        var prey = grippedPrey();
        output.storeNullable("GrippedPrey", net.minecraft.core.UUIDUtil.CODEC, prey != null ? prey.getUUID() : loadedPrey);
        output.putInt("GripTick", prey != null ? combat.animationTick() : loadedGripTick);
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
        setDragonSleeping(input.getBooleanOr("Sleeping", false) && !flying() && command() == 0);
        loadedPrey = input.read("GrippedPrey", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        loadedGripTick = Math.clamp(input.getIntOr("GripTick", 0), 0, 55);
    }

    public Mob grippedPrey() {
        return getFirstPassenger() instanceof Mob mob && mob.getId() == entityData.get(GRIPPED_PREY) ? mob : null;
    }

    private boolean validPrey(LivingEntity target) {
        // Player passengers remain exclusively the owner's pilot seat; combat capture is for mobs.
        return target instanceof Mob && !(target instanceof FireDragon) && target.isAlive() && target.level() == level()
                && stage() > 1 && target.getBbWidth() < getBbWidth() * 0.5F && !DragonFireCombat.protectedTarget(this, target);
    }

    boolean tryGrabPrey(LivingEntity target) {
        if (level().isClientSide() || !canMoveOnGround() || command() != 0 || !onGround() || isVehicle() || isInWater()
                || !validPrey(target) || target.isPassenger() || target.isVehicle() || !canAttack(target)
                || !withinBiteReach(target) || !hasLineOfSight(target)) return false;
        entityData.set(GRIPPED_PREY, target.getId());
        if (!target.startRiding(this)) { entityData.set(GRIPPED_PREY, -1); return false; }
        startGrip(0);
        return true;
    }

    private void startGrip(int tick) {
        combat.reset(); combat.resume(DragonCombatState.SHAKE, tick);
        gripImpactDone = false;
        getNavigation().stop(); setDeltaMovement(Vec3.ZERO);
        syncCombat();
    }

    private void restoreGrip() {
        var pending = loadedPrey;
        loadedPrey = null;
        // Growth attributes are loaded before vanilla's first dimension refresh.
        refreshDimensions();
        if (getFirstPassenger() instanceof Mob prey && pending.equals(prey.getUUID())) {
            entityData.set(GRIPPED_PREY, prey.getId());
            if (validPrey(prey) && !isNoAi() && command() == 0 && !flying() && !isInWater()) startGrip(loadedGripTick);
            else releaseGrip(true);
        }
    }

    private void tickGrip(ServerLevel server) {
        Mob prey = grippedPrey();
        if (!isAlive() || isNoAi() || command() != 0 || flying() || isInWater() || isPassenger() || getControllingPassenger() != null
                || prey != null && (!validPrey(prey) || getTarget() != null && getTarget() != prey)
                || prey == null && !gripImpactDone) { releaseGrip(true); return; }
        getNavigation().stop(); setDeltaMovement(Vec3.ZERO); yBodyRot = getYRot();
        combat.tick(true, false, stage());
        if (prey != null && combat.animationTick() > 55 && !gripImpactDone) {
            float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 2;
            if (prey.hurtServer(server, damageSources().mobAttack(this), damage)
                    && DragonCombatConfig.current.canDragonsHealFromBiting) heal(damage * 0.5F);
            setHunger(hunger() + 1);
            gripImpactDone = true;
            releaseGrip(false);
        }
        if (combat.animation() == DragonCombatState.NONE) gripImpactDone = false;
    }

    private void releaseGrip(boolean reset) {
        Mob prey = grippedPrey();
        entityData.set(GRIPPED_PREY, -1);
        if (prey != null) prey.stopRiding();
        if (reset && combat.animation() == DragonCombatState.SHAKE) { combat.reset(); syncCombat(); }
        if (reset) { gripImpactDone = false; loadedPrey = null; }
    }

    @Override public void remove(RemovalReason reason) {
        if (reason.shouldDestroy()) releaseGrip(true);
        super.remove(reason);
    }
}
