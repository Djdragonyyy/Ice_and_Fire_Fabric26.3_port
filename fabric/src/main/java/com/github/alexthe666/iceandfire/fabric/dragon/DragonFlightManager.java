package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Server flight/escort/roost paths adapted from IafDragonLogic,
 * IafDragonFlightManager and DragonUtils (LGPL-3.0). Combat remains separate. */
final class DragonFlightManager {
    enum AirAttack { TACKLE, HOVER_BLAST, SCORCH_STREAM }
    private AirAttack airAttack = AirAttack.TACKLE;
    private LivingEntity combatTarget;
    private Vec3 scorchOffset = Vec3.ZERO;
    private boolean scorchReverse;
    private int combatTicks;
    AirAttack airAttack() { return airAttack; }
    int combatTicks() { return combatTicks; }
    boolean attacking() { return combatTarget != null && !landing; }
    private final FireDragon dragon;
    private final DragonFlightPath path;
    private Vec3 target;
    private int hoverTicks, flyTicks, escortRecalculate, teleportDelay;
    private boolean landing, returning;
    private double speed = 0.2;

    DragonFlightManager(FireDragon dragon) { this.dragon = dragon; this.path = new DragonFlightPath(dragon); }
    Vec3 target() { return target; }
    boolean returning() { return returning; }

    void reset() {
        path.reset();
        target = null;
        hoverTicks = flyTicks = escortRecalculate = teleportDelay = 0;
        landing = returning = false;
        speed = 0.2;
        combatTarget = null;
        combatTicks = 0;
    }
    void commandChanged() {
        path.reset();
        target = null;
        returning = false;
        landing = dragon.command() == 1 && dragon.flying();
    }
    boolean canTakeOff() {
        if (!dragon.canMoveAutonomously() || dragon.stage() < 2 || dragon.flying()) return false;
        if (dragon.isInWater()) return true;
        if (!dragon.onGround()) return false;
        BlockPos top = BlockPos.containing(dragon.getX(), dragon.getBoundingBox().maxY, dragon.getZ());
        for (int y = 1; y < 4; y++) {
            if (!dragon.level().hasChunkAt(top.above(y)) || !dragon.level().isEmptyBlock(top.above(y))) return false;
        }
        return dragon.level().noCollision(dragon, dragon.getBoundingBox().move(0, 3, 0));
    }
    boolean takeOff() {
        if (!canTakeOff()) return false;
        reset();
        dragon.setAutonomousFlight(true, true);
        dragon.getNavigation().stop();
        dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.5, 0, 0.5));
        return true;
    }
    void tick() {
        if (!dragon.isAlive() || dragon.isNoAi() || dragon.isVehicle() || dragon.isPassenger() || dragon.shoulderCarrier() != null) {
            reset();
            dragon.setAutonomousFlight(dragon.isAlive() && !dragon.isPassenger()
                    && dragon.shoulderCarrier() == null && dragon.flying(), false);
            return;
        }
        if (--teleportDelay <= 0) {
            teleportDelay = 20;
            if (DragonEscortTeleport.tryTeleport(dragon)) return;
        }
        boolean wantsRoost = wantsRoost();
        if (dragon.getTarget() != null) {
            if (!dragon.flying()) {
                if (Math.abs(dragon.getTarget().getY() - dragon.getY()) > 5 && takeOff())
                    beginAirAttack(dragon.getTarget(), AirAttack.values()[dragon.getRandom().nextInt(3)]);
            } else tickCombatFlight(dragon.getTarget());
            return;
        }
        combatTarget = null;
        if (!dragon.flying()) {
            returning = wantsRoost;
            if (dragon.command() == 1) return;
            if (dragon.seekingFood || dragon.isInLove()) return;
            if (wantsRoost) {
                if (horizontalHomeDistance() > 40 && takeOff()) returning = true;
                return;
            }
            LivingEntity owner = escortOwner();
            boolean escortTakeoff = owner != null && (dragon.distanceTo(owner) > 30 || owner.getY() - dragon.getY() > 8);
            boolean wanderTakeoff = dragon.command() == 0 && dragon.level().isBrightOutside()
                    && dragon.getRandom().nextInt(1500) == 0;
            if (escortTakeoff || wanderTakeoff || dragon.isInWater()) takeOff();
            return;
        }
        dragon.getNavigation().stop();
        if (dragon.command() == 1 || !dragon.canMoveAutonomously() || flyTicks > 6000) landing = true;
        if (landing) { land(); return; }
        if (dragon.hovering()) {
            if (++hoverTicks > 40 || dragon.getY() >= maxHeight() || dragon.verticalCollision && !dragon.onGround()) {
                dragon.setAutonomousFlight(true, false);
                hoverTicks = 0;
            } else {
                dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.8, 1, 0.8)
                        .add(0, dragon.getY() < maxHeight() ? (dragon.isInWater() ? 0.12 : 0.08) : 0, 0));
                return;
            }
        }
        flyTicks++;
        returning = wantsRoost;
        LivingEntity owner = escortOwner();
        if (dragon.command() == 2 && owner == null) { landing = true; land(); return; }
        if (returning) {
            Vec3 home = Vec3.atBottomCenterOf(dragon.getHomePosition());
            if (horizontalHomeDistance() < Math.max(2, dragon.getBbWidth())) { landing = true; land(); return; }
            target = clampHeight(home.add(0, horizontalHomeDistance() < 40 ? 0 : 15 + dragon.getRandom().nextInt(3), 0));
            if (!canReach(target)) target = null;
        } else if (target == null || dragon.position().distanceToSqr(target) < 4 || !canReach(target)
                || owner != null && --escortRecalculate <= 0) {
            target = owner != null ? escortTarget(owner) : wanderTarget();
            escortRecalculate = 10;
        }
        if (target == null) {
            // A blocked route must not turn into flight towards (0,0,0).
            landing = true;
            land();
            return;
        }
        if (dragon.horizontalCollision) {
            dragon.setYRot(dragon.getYRot() + 180);
            dragon.setDeltaMovement(new Vec3(0, Math.max(0, dragon.getDeltaMovement().y), 0));
            target = null;
            speed = 0.2;
            return;
        }
        if (owner != null && owner.onGround() && owner.position().subtract(dragon.position()).horizontalDistance()
                < Math.max(4, dragon.getBbWidth() + 2)) { landing = true; land(); return; }
        steer(owner != null && dragon.distanceTo(owner) > 20 ? 1.2 : 0.6);
    }

    void beginAirAttack(LivingEntity enemy, AirAttack mode) {
        path.reset();
        combatTarget = enemy;
        airAttack = mode;
        combatTicks = 0;
        landing = returning = false;
        target = null;
        scorchReverse = false;
        scorchOffset = enemy.position().subtract(dragon.position()).multiply(1, 0, 1);
        if (scorchOffset.horizontalDistance() < 6) scorchOffset = new Vec3(12, 0, 0);
        if (scorchOffset.length() > 30) scorchOffset = scorchOffset.normalize().scale(30);
    }

    private void tickCombatFlight(LivingEntity enemy) {
        dragon.getNavigation().stop();
        if (!dragon.canAttack(enemy) || dragon.command() == 1) { landing = true; land(); return; }
        if (landing) { land(); return; }
        if (combatTarget != enemy || combatTicks >= 180)
            beginAirAttack(enemy, AirAttack.values()[dragon.getRandom().nextInt(3)]);
        combatTicks++;
        // Preserve the original takeoff hover before starting the attack route.
        if (!dragon.hovering()) hoverTicks = 40;
        if (dragon.hovering() && hoverTicks < 40) {
            hoverTicks++;
            dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.8, 1, 0.8).add(0, 0.08, 0));
            return;
        }
        double height = 5 + dragon.stage() * 2;
        if (airAttack == AirAttack.TACKLE) {
            target = clampHeight(enemy.position().add(0, enemy.getBbHeight(), 0));
        } else if (airAttack == AirAttack.HOVER_BLAST) {
            double plane = enemy.position().subtract(dragon.position()).horizontalDistance();
            if (target == null || plane < 4 || plane > 30 || !canReach(target)) {
                target = null;
                for (int i = 0; i < 10 && target == null; i++) {
                    Vec3 candidate = clampHeight(enemy.position().add(dragon.getRandom().nextInt(20) - 10,
                            height, dragon.getRandom().nextInt(20) - 10));
                    if (canReach(candidate)) target = candidate;
                }
            }
        } else {
            Vec3 offset = scorchReverse ? scorchOffset.scale(-1) : scorchOffset;
            target = clampHeight(enemy.position().add(offset).add(0, height, 0));
            if (dragon.position().distanceToSqr(target) < 100) {
                scorchReverse = !scorchReverse;
                target = clampHeight(enemy.position().add(scorchReverse ? scorchOffset.scale(-1) : scorchOffset).add(0, height, 0));
            }
        }
        if (target == null || !canReach(target)) { landing = true; land(); return; }
        if (dragon.horizontalCollision) {
            dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0, 1, 0));
            beginAirAttack(enemy, AirAttack.HOVER_BLAST);
            return;
        }
        boolean hold = airAttack == AirAttack.HOVER_BLAST && dragon.position().distanceToSqr(target) < 9;
        dragon.setAutonomousFlight(true, hold);
        if (hold) dragon.setDeltaMovement(dragon.getDeltaMovement().scale(0.6));
        else steer(airAttack == AirAttack.SCORCH_STREAM ? 1.2 : 0.8);
    }

    private void steer(double minimumSpeed) {
        Vec3 waypoint = path.next(target);
        if (waypoint == null) { landing = true; land(); return; }
        boolean corner = path.detouring() && dragon.position().distanceToSqr(waypoint) < 16;
        speed = corner ? Math.min(speed, 0.4) : Math.max(speed, minimumSpeed);
        var step = DragonFlightSteering.towardsWaypoint(waypoint.subtract(dragon.position()), dragon.getYRot(), speed, path.intermediate(), dragon.getFlightSpeedModifier());
        speed = step.speed();
        dragon.setYRot(step.yaw());
        dragon.yBodyRot = dragon.yHeadRot = step.yaw();
        Vec3 motion = dragon.getDeltaMovement();
        if (corner || dragon.position().distanceToSqr(waypoint) <= 1) motion = motion.scale(corner ? 0.6 : 0.5);
        motion = motion.add(step.acceleration());
        if (waypoint.y > dragon.getY() + 1) motion = motion.add(0, 0.1 * dragon.getFlightSpeedModifier(), 0);
        // Native swept movement clips blocked axes while preserving free vertical motion.
        // Stopping all axes here would freeze a descending dragon at the edge of an obstacle.
        dragon.setDeltaMovement(motion);
    }
    private void land() {
        path.reset();
        target = null;
        dragon.setAutonomousFlight(true, true);
        dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.5, 1, 0.5).add(0, -0.25, 0));
        if (dragon.onGround() && !dragon.isInWater()) {
            dragon.setAutonomousFlight(false, false);
            dragon.setDeltaMovement(Vec3.ZERO);
            reset();
        }
    }
    private LivingEntity escortOwner() {
        var owner = dragon.getOwner();
        return dragon.command() == 2 && owner != null && owner.isAlive() && owner.level() == dragon.level()
                && dragon.distanceTo(owner) <= 2000 ? owner : null;
    }
    boolean wantsRoost() {
        return dragon.command() == 0 && dragon.canMoveAutonomously() && !dragon.level().isBrightOutside()
                && dragon.hasHome() && dragon.getTarget() == null && dragon.getLastHurtByMob() == null
                && (dragon.position().distanceToSqr(Vec3.atCenterOf(dragon.getHomePosition())) > dragon.getBbWidth() * 10 || returning && dragon.flying());
    }
    private double horizontalHomeDistance() {
        return Vec3.atCenterOf(dragon.getHomePosition()).subtract(dragon.position()).horizontalDistance();
    }
    private Vec3 escortTarget(LivingEntity owner) {
        Vec3 predicted = owner.position().add(owner.getDeltaMovement().scale(10));
        double height = owner.onGround() ? 8 : 1;
        Vec3 destination = clampHeight(predicted.add(0, height, 0));
        if (canReach(destination)) return destination;
        for (int offset : new int[]{4, 8, 12}) {
            destination = clampHeight(predicted.add(0, height + offset, 0));
            if (canReach(destination)) return destination;
        }
        return null;
    }
    private Vec3 wanderTarget() {
        for (int i = 0; i < 10; i++) {
            double x, z;
            if (dragon.hasHome()) {
                int radius = dragon.getHomeRadius();
                x = dragon.getHomePosition().getX() + dragon.getRandom().nextInt(radius * 2) - radius + 0.5;
                z = dragon.getHomePosition().getZ() + dragon.getRandom().nextInt(radius * 2) - radius + 0.5;
            } else {
                double radius = 12 * (0.7 * DragonGrowth.renderSize(dragon.ageDays()) / 3);
                double angle = Math.toRadians(dragon.yBodyRot) + 3.15
                        + dragon.getRandom().nextFloat() * (dragon.getRandom().nextBoolean() ? 1 : -1);
                x = dragon.getX() + radius * Math.sin(Math.PI + angle);
                z = dragon.getZ() + radius * Math.cos(angle);
            }
            BlockPos column = BlockPos.containing(x, dragon.getY(), z);
            if (!dragon.level().hasChunkAt(column)) continue;
            int ground = dragon.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
            double y = dragon.getY() - ground > 16 ? dragon.getY() + dragon.getRandom().nextInt(16) - 8
                    : dragon.getY() + dragon.getRandom().nextInt(16) + 1;
            Vec3 candidate = clampHeight(new Vec3(x, y, z));
            if (dragon.hasHome() && !dragon.isWithinHome(candidate)) continue;
            if (dragon.position().distanceToSqr(candidate) > 6 && canReach(candidate)) return candidate;
        }
        return null;
    }
    int maxHeight() { return Math.min(DragonCombatConfig.current.maxDragonFlight, dragon.level().getMaxY() - (int) Math.ceil(dragon.getBbHeight()) - 1); }
    private Vec3 clampHeight(Vec3 pos) { return new Vec3(pos.x, Math.min(pos.y, maxHeight()), pos.z); }
    private boolean canReach(Vec3 destination) {
        return path.reaches(destination);
    }
}
