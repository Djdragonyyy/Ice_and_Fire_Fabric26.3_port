package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import java.util.OptionalDouble;

/** Geometry extracted from EntityDragonBase.getHeadPosition and DragonUtils (LGPL-3.0). */
public final class DragonCombatGeometry {
    private DragonCombatGeometry() {}

    public static Vec3 headOffset(float size, float yaw, float flightPitch, float flyProgress, float sitProgress) {
        float pitch = -flightPitch;
        float fly = flyProgress * 0.01F;
        float pitchMulti = 0, adjustment = 0, minus = 0;
        if (flyProgress > 0) {
            pitchMulti = (float) Math.sin(Math.toRadians(pitch)) * 2.1F * Math.abs(pitch) / 90;
            if (pitchMulti > 0) pitchMulti *= 1.5F - pitchMulti * 0.5F;
            if (pitchMulti < 0) pitchMulti *= 1.3F - pitchMulti * 0.1F;
            adjustment = 1.2F;
            minus = 0.3F * Math.abs(pitch / 90);
            if (pitch >= 0) { adjustment = 0.6F * Math.abs(pitch / 90); minus = 0.95F * Math.abs(pitch / 90); }
        }
        double forward = 1.7F * size * 0.3F * (1 + fly)
                + size * (0.3F * Math.sin(Math.toRadians(pitch + 90)) * adjustment - minus);
        double angle = Math.toRadians(yaw + 90);
        return new Vec3(forward * Math.cos(angle),
                (0.7F + sitProgress * 0.015F + fly + pitchMulti) * size * 0.3F,
                forward * Math.sin(angle));
    }

    /** Only real intersections count; a miss must never turn into Vec3.ZERO as a target. */
    public static OptionalDouble intersectionDistance(Vec3 start, Vec3 end, AABB bounds) {
        if (bounds.contains(start)) return OptionalDouble.of(0);
        return bounds.clip(start, end).map(hit -> OptionalDouble.of(start.distanceToSqr(hit)))
                .orElseGet(OptionalDouble::empty);
    }
}
