package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.Vec3;

/** Seat regression curves from the original EntityDragonBase (LGPL-3.0). */
public final class DragonRiderLayout {
    private DragonRiderLayout() {}

    public static float height(int ageDays) {
        float size = DragonGrowth.renderSize(ageDays);
        return 0.002237889819F * size * size + 0.233137174F * size - 1.717904311F;
    }

    public static float forward(int ageDays) {
        float size = DragonGrowth.renderSize(ageDays);
        return 0.00336283F * size * size + 0.1934242516F * size - 0.02622133882F;
    }

    public static float walkingLiftLimit(int ageDays) {
        return 1.1F * maturity(ageDays) + height(ageDays) * 0.1F;
    }

    private static float maturity(int ageDays) {
        return Math.clamp((ageDays - 50) / 75F, 0, 1);
    }

    public static Vec3 offset(int ageDays, float yaw, boolean flying, float walkingLift,
                              float riderPitch, float riderHeight) {
        float extraY = flying ? 1.1F * maturity(ageDays) + height(ageDays) * 0.6F : walkingLift;
        if (riderPitch < 0) extraY += -0.1F + (riderPitch - 60F) / -100F * 0.2F;
        double angle = Math.toRadians(yaw + 90);
        return new Vec3(forward(ageDays) * Math.cos(angle), height(ageDays) + extraY + riderHeight,
                forward(ageDays) * Math.sin(angle));
    }

    /** Vanilla multiplies this attribute by physical scale and clips the camera against blocks. */
    public static double cameraDistance(int ageDays) {
        return (4 + DragonGrowth.renderSize(ageDays)) / DragonGrowth.scale(ageDays);
    }
}
