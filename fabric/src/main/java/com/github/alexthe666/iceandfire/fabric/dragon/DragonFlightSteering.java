package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Non-combat FlightMoveHelper from IafDragonFlightManager (LGPL-3.0).
 * Guards the original vertical-target and zero-speed divisions. */
final class DragonFlightSteering {
    record Step(Vec3 acceleration, float yaw, double speed) {}

    static Step towardsWaypoint(Vec3 offset, float yaw, double speed, boolean intermediate) {
        // A local point on a distant journey must not apply the final-arrival slowdown.
        if (intermediate && offset.lengthSqr() > 1.0E-6)
            offset = offset.normalize().scale(Math.max(50, offset.length()));
        return towards(offset, yaw, speed);
    }

    static Step towards(Vec3 offset, float yaw, double speed) {
        double plane = offset.horizontalDistance();
        double horizontalScale = plane < 1.0e-6 ? 0 : Math.clamp(1 - Math.abs(offset.y * 0.7) / plane, 0.2, 1);
        Vec3 direction = new Vec3(offset.x * horizontalScale, offset.y, offset.z * horizontalScale);
        double distance = direction.length();
        if (distance <= 1) return new Step(Vec3.ZERO, yaw, 0.2);
        float targetYaw = direction.horizontalDistanceSqr() < 1.0e-12 ? yaw
                : (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        float nextYaw = yaw + Mth.clamp(Mth.wrapDegrees(targetYaw - yaw), -4, 4);
        double nextSpeed = Math.abs(Mth.wrapDegrees(nextYaw - yaw)) < 3
                ? Math.min(1.8, speed + 0.005 * 1.8 / Math.max(0.2, speed)) : Math.max(0.2, speed - 0.025);
        double effectiveSpeed = nextSpeed * Math.min(1, distance / 50 + 0.3);
        double pitch = Math.atan2(direction.y, direction.horizontalDistance());
        double heading = Math.toRadians(nextYaw + 90);
        Vec3 acceleration = new Vec3(Math.cos(heading) * Math.abs(direction.x / distance),
                Math.sin(pitch) * Math.abs(direction.y / distance),
                Math.sin(heading) * Math.abs(direction.z / distance)).scale(effectiveSpeed * 0.2);
        return new Step(new Vec3(Math.clamp(acceleration.x, -0.2, 0.2), Math.clamp(acceleration.y, -0.2, 0.2),
                Math.clamp(acceleration.z, -0.2, 0.2)), nextYaw, nextSpeed);
    }
    private DragonFlightSteering() {}
}
