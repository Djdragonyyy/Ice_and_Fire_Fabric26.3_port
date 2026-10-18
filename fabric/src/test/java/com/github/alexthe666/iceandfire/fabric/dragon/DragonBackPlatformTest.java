package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonBackPlatformTest {
    @Test void onlyFeetOnTopStopDragonNotPlayersInsideAlongsideOrHighAbove() {
        var dragon = new AABB(-2, 0, -2, 2, 8.4, 2);
        assertTrue(DragonBackPlatform.isOnBack(dragon, new AABB(-0.3, 8.4, -0.3, 0.3, 10.2, 0.3)));
        assertTrue(DragonBackPlatform.isOnBack(dragon, new AABB(1.9, 8.4, 0, 2.5, 10.2, 0.6)));
        assertFalse(DragonBackPlatform.isOnBack(dragon, new AABB(-0.3, 6, -0.3, 0.3, 7.8, 0.3)));
        assertFalse(DragonBackPlatform.isOnBack(dragon, new AABB(2.01, 8.4, 0, 2.61, 10.2, 0.6)));
        assertFalse(DragonBackPlatform.isOnBack(dragon, new AABB(0, 9, 0, 0.6, 10.8, 0.6)));
    }
}
