package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonShoulderLayoutTest {
    @Test void shoulderSlotsRemainDistinctAndRotateWithPlayer() {
        for (float yaw : new float[]{0, 90, 180, 270}) {
            var left = DragonShoulderLayout.offset(0, yaw, false, 1);
            var right = DragonShoulderLayout.offset(1, yaw, false, 1);
            var head = DragonShoulderLayout.offset(2, yaw, false, 1);
            assertEquals(1, left.distanceTo(right), 0.000001);
            assertEquals(0, left.x + right.x, 0.000001);
            assertEquals(0, left.z + right.z, 0.000001);
            assertTrue(head.y > left.y);
            assertEquals(left.scale(2), DragonShoulderLayout.offset(0, yaw, false, 2));
            assertEquals(0.2, left.y - DragonShoulderLayout.offset(0, yaw, true, 1).y, 0.000001);
        }
    }
    @Test void onlyFirstStageFitsOnShoulders() {
        assertTrue(DragonShoulderLayout.canBeCarried(0));
        assertTrue(DragonShoulderLayout.canBeCarried(24));
        for (int age : new int[]{25, 49, 50, 100, 125}) assertFalse(DragonShoulderLayout.canBeCarried(age));
    }
}
