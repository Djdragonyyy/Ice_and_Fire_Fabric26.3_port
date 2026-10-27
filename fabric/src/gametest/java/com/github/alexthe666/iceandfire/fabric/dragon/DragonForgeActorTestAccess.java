package com.github.alexthe666.iceandfire.fabric.dragon;

/** Test-only access to actor state without widening the production API. */
public final class DragonForgeActorTestAccess {
    private DragonForgeActorTestAccess() {}

    public static void sleeping(FireDragon dragon, boolean sleeping) {
        dragon.setDragonSleeping(sleeping);
    }

    public static void flying(FireDragon dragon, boolean flying) {
        dragon.setAutonomousFlight(flying, flying);
    }
}
