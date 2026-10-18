package com.github.alexthe666.iceandfire.fabric.dragon;

/** Pitch damping and limits adapted from EntityDragonBase.updatePitch (LGPL-3.0). */
public final class DragonFlightMotion {
    private DragonFlightMotion() {}

    public static float nextPitch(float current, double verticalMovement, double horizontalMovement, boolean flying) {
        if (!flying) return Math.abs(current) < 1 ? 0 : current / 1.5F;
        float pitch = (float) Math.clamp(current - verticalMovement * 10, -60, 40);
        float damping = (float) (horizontalMovement * 6 * Math.abs(pitch) / 90);
        if (pitch > 2) pitch = Math.max(0, pitch - damping - 1);
        else if (pitch < -2) pitch = Math.min(0, pitch + damping + 1);
        return pitch;
    }

    public static double takeoffVelocity(double currentY, boolean grounded, boolean beginningFlight) {
        return grounded && beginningFlight ? Math.max(currentY, 0.8) : currentY;
    }
}
