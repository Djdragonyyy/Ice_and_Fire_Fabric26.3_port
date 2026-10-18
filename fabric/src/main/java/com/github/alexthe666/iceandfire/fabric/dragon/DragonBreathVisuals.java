package com.github.alexthe666.iceandfire.fabric.dragon;

/** Particle scale from the original ParticleDragonFlame (LGPL-3.0). */
public final class DragonBreathVisuals {
    private DragonBreathVisuals() {}
    public static float size(float renderSize) { return Math.clamp(renderSize * 0.08F, 0.55F, 3); }
    public static float smooth(float progress) {
        float t = Math.clamp(progress, 0, 1);
        return t * t * (3 - 2 * t);
    }
    public static float extension(int fireTicks) { return smooth((fireTicks - 20) / 20F); }
    public static int count(float renderSize, float extension) {
        return Math.max(1, Math.round((12 + size(renderSize) * 10) * extension));
    }
}
