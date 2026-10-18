package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonBackPlatformTest {
    @Test void dismountedPlayerIsSupportedAtEveryRidableStageAndWorldPosition() {
        for (int age : new int[]{50, 75, 100, 125}) {
            double scale = DragonGrowth.scale(age);
            for (double y : new double[]{-40, 70, 200}) {
                var dragon = new AABB(100 - 0.39 * scale, y, -80 - 0.39 * scale,
                        100 + 0.39 * scale, y + 1.2 * scale, -80 + 0.39 * scale);
                var position = DragonBackPlatform.dismountPosition(dragon);
                var player = new AABB(position.x - 0.3, position.y, position.z - 0.3,
                        position.x + 0.3, position.y + 1.8, position.z + 0.3);
                assertFalse(dragon.intersects(player), "Dismount must not put feet inside the dragon");
                assertTrue(DragonBackPlatform.isOnBack(dragon, player), "Dismount must activate the back support scan");
                assertEquals(100, position.x, 0.00001);
                assertEquals(-80, position.z, 0.00001);
                assertEquals(dragon.maxY, position.y);
                assertTrue(player.minX > dragon.minX && player.maxX < dragon.maxX);
                assertTrue(player.minZ > dragon.minZ && player.maxZ < dragon.maxZ);
            }
        }
    }

    @Test void onlyFeetOnTopStopDragonNotPlayersInsideAlongsideOrHighAbove() {
        var dragon = new AABB(-2, 0, -2, 2, 8.4, 2);
        assertTrue(DragonBackPlatform.isOnBack(dragon, new AABB(-0.3, 8.4, -0.3, 0.3, 10.2, 0.3)));
        assertTrue(DragonBackPlatform.isOnBack(dragon, new AABB(1.9, 8.4, 0, 2.5, 10.2, 0.6)));
        assertFalse(DragonBackPlatform.isOnBack(dragon, new AABB(-0.3, 6, -0.3, 0.3, 7.8, 0.3)));
        assertFalse(DragonBackPlatform.isOnBack(dragon, new AABB(2.01, 8.4, 0, 2.61, 10.2, 0.6)));
        assertFalse(DragonBackPlatform.isOnBack(dragon, new AABB(0, 9, 0, 0.6, 10.8, 0.6)));
    }
}
