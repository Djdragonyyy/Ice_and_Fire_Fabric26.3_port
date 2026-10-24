package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Conservative, full-body flight clearance. Queries only chunks already present in the world. */
final class DragonFlightClearance {
    private DragonFlightClearance() {}

    static boolean clear(FireDragon dragon, Vec3 from, Vec3 to) {
        if (!finite(from) || !finite(to) || from.distanceToSqr(to) > 256 * 256) return false;
        AABB sweep = dragon.getBoundingBox().move(from.subtract(dragon.position()))
                .expandTowards(to.subtract(from)).deflate(1.0E-5);
        var level = dragon.level();
        if (sweep.minY < level.getMinY() || sweep.maxY > level.getMaxY()
                || !level.getWorldBorder().isWithinBounds(sweep)) return false;
        int minX = Mth.floor(sweep.minX) >> 4, maxX = Mth.floor(sweep.maxX) >> 4;
        int minZ = Mth.floor(sweep.minZ) >> 4, maxZ = Mth.floor(sweep.maxZ) >> 4;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!level.hasChunkAt(new BlockPos(x << 4, Mth.floor(sweep.minY), z << 4))) return false;
        }
        // Swept AABB is intentionally conservative at diagonal corners: never shrink a large dragon.
        return level.noCollision(dragon, sweep);
    }

    private static boolean finite(Vec3 point) {
        return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }
}
