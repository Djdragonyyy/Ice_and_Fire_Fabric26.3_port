package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonRiderLayoutTest {
    @Test void seatContinuesGrowingAfterCollisionScaleCaps() {
        assertEquals(7, DragonGrowth.scale(100));
        assertEquals(7, DragonGrowth.scale(125));
        assertTrue(DragonRiderLayout.height(125) > DragonRiderLayout.height(100) + 3);
        assertTrue(DragonRiderLayout.forward(125) > DragonRiderLayout.forward(100) + 3);
        assertEquals(7.29, DragonRiderLayout.height(125), 0.15);
        assertEquals(8.80, DragonRiderLayout.forward(125), 0.15);
    }

    @Test void seatRotatesWithDragonAndUsesWorldUnits() {
        var south = DragonRiderLayout.offset(75, 0, false, 0, 0, 1.8F);
        var west = DragonRiderLayout.offset(75, 90, false, 0, 0, 1.8F);
        assertEquals(0, south.x, 0.00001);
        assertEquals(south.z, -west.x, 0.00001);
        assertEquals(0, west.z, 0.00001);
        assertEquals(south.y, west.y);
        assertEquals(DragonRiderLayout.height(75) + 1.8, south.y, 0.00001);
    }

    @Test void cameraDistanceAccountsForVanillaVehicleScalingAndGrowth() {
        double last = 0;
        for (int age = 50; age <= 200; age++) {
            double actual = DragonRiderLayout.cameraDistance(age) * DragonGrowth.scale(age);
            assertEquals(4 + DragonGrowth.renderSize(age), actual, 0.00001);
            assertTrue(actual >= last);
            last = actual;
        }
        assertEquals(34, last, 0.00001);
    }
}
