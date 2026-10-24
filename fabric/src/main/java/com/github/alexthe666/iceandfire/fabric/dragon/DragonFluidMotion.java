package com.github.alexthe666.iceandfire.fabric.dragon;

/** Fluid controls and fire-dragon lava speed from original EntityDragonBase/EntityFireDragon (LGPL-3.0). */
final class DragonFluidMotion {
    private DragonFluidMotion() {}
    static double vertical(boolean lava, boolean up, boolean down) {
        return up == down ? 0 : (up ? 1 : -1) * (lava ? 0.8F : 0.5F);
    }
    static float lavaSpeed(double movementSpeed, boolean sprinting) {
        float speed = (float) movementSpeed;
        float lavaModifier = (float) (0.28F + 0.1 * ((speed - 0.15F) / (0.4F - 0.15F) * 1.5F));
        return speed * lavaModifier * (sprinting ? 1.4F : 1);
    }
}
