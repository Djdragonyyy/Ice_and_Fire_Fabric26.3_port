package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Owner-requested catch-up rule. Does not change the dimension or load destination chunks. */
final class DragonEscortTeleport {
    static final double DISTANCE = 150;
    static boolean tryTeleport(FireDragon dragon) {
        var owner = dragon.getOwner();
        if (!(dragon.level() instanceof ServerLevel server) || !dragon.canMoveAutonomously() || dragon.command() != 2
                || owner == null || !owner.isAlive() || owner.level() != server
                || dragon.distanceToSqr(owner) < DISTANCE * DISTANCE) return false;
        int radius = (int) Math.ceil((dragon.getBbWidth() + owner.getBbWidth()) / 2) + 2;
        for (int dy = 0; dy <= 8; dy++) for (int sign : new int[]{1, -1}) {
            if (dy == 0 && sign == -1) continue;
            for (int dx : new int[]{-radius, 0, radius}) for (int dz : new int[]{-radius, 0, radius}) {
                if (dx == 0 && dz == 0) continue;
                Vec3 destination = new Vec3(owner.getBlockX() + dx + 0.5, owner.getBlockY() + dy * sign,
                        owner.getBlockZ() + dz + 0.5);
                if (safe(dragon, server, destination, true)) return move(dragon, destination, false);
            }
        }
        // Only dragons capable of autonomous flight can catch up with an airborne owner without ground.
        if (dragon.stage() >= 2 && !owner.onGround()) {
            for (int dx : new int[]{-radius, radius}) for (int dz : new int[]{-radius, radius}) {
                Vec3 destination = owner.position().add(dx, 3, dz);
                if (safe(dragon, server, destination, false)) return move(dragon, destination, true);
            }
        }
        return false;
    }
    private static boolean move(FireDragon dragon, Vec3 destination, boolean flying) {
        dragon.getNavigation().stop();
        dragon.flightManager.reset();
        dragon.setAutonomousFlight(flying, false);
        dragon.setNoGravity(flying);
        dragon.teleportTo(destination.x, destination.y, destination.z);
        dragon.setDeltaMovement(Vec3.ZERO);
        dragon.fallDistance = 0;
        dragon.setOnGround(!flying);
        return true;
    }
    static boolean safe(FireDragon dragon, ServerLevel server, Vec3 destination, boolean ground) {
        AABB box = dragon.getBoundingBox().move(destination.subtract(dragon.position()));
        if (box.minY < server.getMinY() || box.maxY > server.getMaxY() || !server.getWorldBorder().isWithinBounds(box)) return false;
        for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++)
            for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++)
                if (!server.hasChunkAt(new BlockPos(x << 4, (int) destination.y, z << 4))) return false;
        if (!server.noCollision(dragon, box) || server.containsAnyLiquid(box)) return false;
        if (ground) {
            for (double x : new double[]{box.minX + 0.01, box.maxX - 0.01})
                for (double z : new double[]{box.minZ + 0.01, box.maxZ - 0.01}) {
                    BlockPos below = BlockPos.containing(x, destination.y - 0.01, z);
                    if (!server.getFluidState(below).isEmpty() || !server.getBlockState(below).isFaceSturdy(server, below, Direction.UP)) return false;
                }
        }
        return true;
    }
    private DragonEscortTeleport() {}
}
