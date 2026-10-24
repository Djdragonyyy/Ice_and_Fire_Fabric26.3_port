package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonPreyMotionTest {
    @Test void originalLiftTimingAndYawKeepPreyAtTheMouth() {
        var neutral = DragonPreyMotion.offset(12.5F, 0, 0);
        assertEquals(5.625, neutral.horizontalDistance(), 0.00001);
        assertTrue(neutral.z > 5.62 && Math.abs(neutral.x) < 0.05);
        assertEquals(0, DragonPreyMotion.offset(12.5F, 0, 20).y);
        assertEquals(0.5104166667, DragonPreyMotion.offset(12.5F, 0, 25).y, 0.00001);
        assertEquals(0.875, DragonPreyMotion.offset(12.5F, 0, 30).y, 0.00001);
        assertEquals(0.875, DragonPreyMotion.offset(12.5F, 0, 55).y, 0.00001);
        var quarterTurn = DragonPreyMotion.offset(12.5F, 90, 0);
        assertEquals(-neutral.z, quarterTurn.x, 0.00001);
        assertEquals(neutral.x, quarterTurn.z, 0.00001);
    }

    @Test void shakeChangesDirectionWithoutChangingReachOrAccumulating() {
        var a = DragonPreyMotion.offset(12.5F, 0, 30);
        var b = DragonPreyMotion.offset(12.5F, 0, 40);
        assertTrue(a.x > 0.5 && b.x < -0.5, "Prey swings across mouth instead of remaining at a static seat");
        for (int tick = 0; tick <= 65; tick++) {
            var base = DragonPreyMotion.offset(12.5F, 37, tick);
            assertEquals(5.625, base.horizontalDistance(), 0.00001);
            assertEquals(base, DragonPreyMotion.offset(12.5F, 37, tick));
            assertEquals(0, base.scale(2).distanceTo(DragonPreyMotion.offset(25, 37, tick)), 0.00001);
        }
    }
}
