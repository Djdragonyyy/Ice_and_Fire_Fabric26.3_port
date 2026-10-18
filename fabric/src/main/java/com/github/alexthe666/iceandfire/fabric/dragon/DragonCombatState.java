package com.github.alexthe666.iceandfire.fabric.dragon;

/** Timings from EntityDragonBase, EntityFireDragon and IafDragonLogic (LGPL-3.0). */
public final class DragonCombatState {
    public static final int NONE = 0, BITE = 1, CHARGE = 2;
    private int animation;
    private int animationTick;
    private int fireTicks;
    private int fireStopTicks;
    private boolean breathing;

    public void tick(boolean active, boolean fire, int stage) {
        if (!active) { reset(); return; }
        if (animation != NONE && ++animationTick >= (animation == BITE ? 35 : 30)) {
            animation = NONE;
            animationTick = 0;
        }
        if (fire && stage > 1) {
            if (!breathing) { breathing = true; fireTicks = 0; }
            fireStopTicks = 10;
        }
        if (breathing) {
            fireTicks++;
            if (fireTicks > stage * 25 || (!fire && --fireStopTicks <= 0)) {
                breathing = false;
                fireTicks = 0;
            }
        }
    }

    public void start(int type) {
        if (animation != type) { animation = type; animationTick = 0; }
    }

    public void reset() {
        animation = animationTick = fireTicks = fireStopTicks = 0;
        breathing = false;
    }

    public int animation() { return animation; }
    public int animationTick() { return animationTick; }
    public boolean breathing() { return breathing; }
    public boolean emitting() { return breathing && fireTicks > 20; }
    public float burnProgress() { return Math.min(40, fireTicks) / 40F; }
}
