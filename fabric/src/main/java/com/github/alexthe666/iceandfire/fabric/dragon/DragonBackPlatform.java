package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Unmounted players can stand on the collision box, with a still timeout like HappyGhast. */
public final class DragonBackPlatform {
    private DragonBackPlatform() {}

    public static Vec3 dismountPosition(AABB dragon) {
        return new Vec3((dragon.minX + dragon.maxX) * 0.5, dragon.maxY,
                (dragon.minZ + dragon.maxZ) * 0.5);
    }

    public static boolean isOnBack(AABB dragon, AABB player) {
        return player.maxX > dragon.minX && player.minX < dragon.maxX
                && player.maxZ > dragon.minZ && player.minZ < dragon.maxZ
                && player.minY >= dragon.maxY - 1.0E-4
                && player.minY <= dragon.maxY + 0.1;
    }
}
