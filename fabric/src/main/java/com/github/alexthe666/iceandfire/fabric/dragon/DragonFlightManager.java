package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Server flight/escort/roost paths adapted from IafDragonLogic,
 * IafDragonFlightManager and DragonUtils (LGPL-3.0). Combat remains separate. */
final class DragonFlightManager {
    static final int MAX_HEIGHT = 256;
    private final FireDragon dragon;
    private Vec3 target;
    private int hoverTicks, flyTicks;
    private boolean landing, returning;
    private double speed = 0.2;

    DragonFlightManager(FireDragon dragon) { this.dragon = dragon; }
    Vec3 target() { return target; }
    boolean returning() { return returning; }

    void reset() {
        target = null;
        hoverTicks = flyTicks = 0;
        landing = returning = false;
        speed = 0.2;
    }
    void commandChanged() {
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
        boolean wantsRoost = wantsRoost();
        if (!dragon.flying()) {
            returning = wantsRoost;
            if (dragon.command() == 1) return;
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
                || owner != null && dragon.distanceTo(owner) > 10) {
            target = owner != null ? escortTarget(owner) : wanderTarget();
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
        var step = DragonFlightSteering.towards(target.subtract(dragon.position()), dragon.getYRot(), speed);
        speed = step.speed();
        dragon.setYRot(step.yaw());
        dragon.yBodyRot = dragon.yHeadRot = step.yaw();
        if (dragon.position().distanceToSqr(target) <= 1) dragon.setDeltaMovement(dragon.getDeltaMovement().scale(0.5));
        dragon.setDeltaMovement(dragon.getDeltaMovement().add(step.acceleration()));
        if (target.y > dragon.getY() + 1) dragon.setDeltaMovement(dragon.getDeltaMovement().add(0, 0.1, 0));
    }
    private void land() {
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
        BlockPos pos = owner.blockPosition();
        if (!dragon.level().hasChunkAt(pos)) return null;
        int ground = dragon.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        for (int i = 0; i < 10; i++) {
            Vec3 candidate = clampHeight(new Vec3(pos.getX() + dragon.getRandom().nextInt(40) - 20 + 0.5,
                    pos.getY() - ground > 16 ? pos.getY() + 0.5 : pos.getY() + 8 + dragon.getRandom().nextInt(16) + 0.5,
                    pos.getZ() + dragon.getRandom().nextInt(40) - 20 + 0.5));
            if (dragon.position().distanceToSqr(candidate) > 6 && canReach(candidate)) return candidate;
        }
        return null;
    }
    private Vec3 wanderTarget() {
        for (int i = 0; i < 10; i++) {
            double x, z;
            if (dragon.hasHome()) {
                x = dragon.getHomePosition().getX() + dragon.getRandom().nextInt(80) - 40 + 0.5;
                z = dragon.getHomePosition().getZ() + dragon.getRandom().nextInt(80) - 40 + 0.5;
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
    private int maxHeight() { return Math.min(MAX_HEIGHT, dragon.level().getMaxY() - (int) Math.ceil(dragon.getBbHeight()) - 1); }
    private Vec3 clampHeight(Vec3 pos) { return new Vec3(pos.x, Math.min(pos.y, maxHeight()), pos.z); }
    private boolean canReach(Vec3 destination) {
        Vec3 offset = destination.subtract(dragon.position());
        // Check every traversed chunk before raycasting; never request new chunks for AI.
        int samples = Math.max(1, (int) Math.ceil(offset.length() / 8));
        for (int i = 0; i <= samples; i++) {
            if (!dragon.level().hasChunkAt(BlockPos.containing(dragon.position().add(offset.scale((double) i / samples))))) return false;
        }
        if (!dragon.level().noCollision(dragon, dragon.getBoundingBox().move(offset))) return false;
        Vec3 from = dragon.position().add(0, dragon.getBbHeight() / 2, 0);
        return dragon.level().clip(new ClipContext(from, from.add(offset), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, dragon)).getType() == HitResult.Type.MISS;
    }
}
