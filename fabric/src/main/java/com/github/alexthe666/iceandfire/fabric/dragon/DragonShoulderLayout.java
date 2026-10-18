package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.Vec3;

/** Three carried hatchlings and offsets adapted from EntityDragonBase.updateRiding (LGPL-3.0). */
public final class DragonShoulderLayout {
    public static final int CAPACITY = 3;
    private DragonShoulderLayout() {}
    public static boolean canBeCarried(int days) { return DragonGrowth.stage(days) == 1; }

    public static Vec3 offset(int index, float bodyYaw, boolean crouching, float playerScale) {
        double angle = Math.toRadians(bodyYaw) + (index == 0 ? -Math.PI / 2 : index == 1 ? Math.PI / 2 : 0);
        double radius = index == 2 ? -0.2 : 0.5;
        return new Vec3(-radius * Math.sin(angle), (crouching ? 1.2 : 1.4) + (index == 2 ? 0.4 : 0),
                radius * Math.cos(angle)).scale(playerScale);
    }
}
