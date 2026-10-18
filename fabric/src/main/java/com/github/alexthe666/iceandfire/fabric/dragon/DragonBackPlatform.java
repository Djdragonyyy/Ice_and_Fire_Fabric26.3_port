package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.AABB;

/** Unmounted players can stand on the collision box, with a still timeout like HappyGhast. */
public final class DragonBackPlatform {
    private DragonBackPlatform() {}

    public static boolean isOnBack(AABB dragon, AABB player) {
        return player.maxX > dragon.minX && player.minX < dragon.maxX
                && player.maxZ > dragon.minZ && player.minZ < dragon.maxZ
                && player.minY >= dragon.maxY - 1.0E-4
                && player.minY <= dragon.maxY + 0.1;
    }
}
