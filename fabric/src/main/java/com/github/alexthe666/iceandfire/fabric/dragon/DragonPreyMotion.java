package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.Vec3;

/** EntityDragonBase.updatePreyInMouth movement (LGPL-3.0), in world units. */
public final class DragonPreyMotion {
    private DragonPreyMotion() {}
    public static Vec3 offset(float renderSize, float bodyYaw, float tick) {
        float shake = tick > 25 && tick < 55
                ? 8 * Math.clamp((float) Math.sin(Math.PI + (tick - 25) * 0.25), -0.8F, 0.8F) : 0;
        float lift = tick > 30 ? 10 : Math.max(0, tick - 20);
        float radius = 0.75F * (0.6F * renderSize / 3) * -3;
        float angle = (float) Math.toRadians(bodyYaw) + 3.15F + shake * 2 * 0.015F;
        double y = lift == 0 ? 0 : 0.035F * (renderSize / 3 + lift * 0.5 * (renderSize / 3));
        return new Vec3(radius * Math.sin(Math.PI + angle), y, radius * Math.cos(angle));
    }
}
