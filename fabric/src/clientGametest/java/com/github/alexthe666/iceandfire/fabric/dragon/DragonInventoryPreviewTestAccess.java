package com.github.alexthe666.iceandfire.fabric.dragon;

/** Test-only access to the real capture path, without widening the production API. */
public final class DragonInventoryPreviewTestAccess {
    public static boolean grab(FireDragon dragon, net.minecraft.world.entity.Mob prey) { return dragon.tryGrabPrey(prey); }
    private DragonInventoryPreviewTestAccess() {}
}
