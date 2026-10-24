package com.github.alexthe666.iceandfire.fabric.dragon;

/** Timings from EntityDragonBase, EntityFireDragon and IafDragonLogic (LGPL-3.0). */
public final class DragonCombatState {
    public static final int NONE = 0, BITE = 1, CHARGE = 2, TAIL = 3, WING = 4, EAT = 5, TACKLE = 6, SHAKE = 7, SPEAK = 8, ROAR = 9, EPIC_ROAR = 10;
    public static int duration(int type) {
        return switch (type) { case BITE -> 35; case TAIL, ROAR -> 40; case EPIC_ROAR -> 60; case WING -> 50; case EAT, SPEAK -> 20; case SHAKE -> 65; default -> 30; };
    }
    public static boolean meleeImpact(int type, int tick) {
        return switch (type) {
            case BITE -> tick > 15 && tick < 25;
            case TAIL -> tick > 20 && tick < 30;
            case WING -> tick == 15 || tick == 25 || tick == 35;
            default -> false;
        };
    }
    private int animation;
    private int animationTick;
    private int fireTicks;
    private int fireStopTicks;
    private boolean breathing;

    public void tick(boolean active, boolean fire, int stage) {
        if (!active) { reset(); return; }
        if (animation != NONE && ++animationTick >= duration(animation)) {
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

    public void resume(int type, int tick) {
        animation = type;
        animationTick = Math.clamp(tick, 0, duration(type) - 1);
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
    public float streamProgress() { return DragonBreathVisuals.extension(fireTicks); }
}
