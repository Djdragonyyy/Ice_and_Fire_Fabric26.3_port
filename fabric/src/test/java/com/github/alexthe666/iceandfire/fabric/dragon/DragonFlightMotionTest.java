package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonFlightMotionTest {
    @Test void climbAndDescentTiltInOppositeDirectionsAndStayWithinOriginalLimits() {
        float up = 0, down = 0;
        for (int i = 0; i < 100; i++) {
            up = DragonFlightMotion.nextPitch(up, 0.5, 0.1, true);
            down = DragonFlightMotion.nextPitch(down, -0.5, 0.1, true);
            assertTrue(up >= -60 && up <= 0);
            assertTrue(down >= 0 && down <= 40);
        }
        assertTrue(up < -45);
        assertTrue(down > 25);
        for (int i = 0; i < 100; i++) {
            up = DragonFlightMotion.nextPitch(up, 0, 0, false);
            down = DragonFlightMotion.nextPitch(down, 0, 0, false);
        }
        assertEquals(0, up);
        assertEquals(0, down);
    }

    @Test void takeoffImpulseIsAppliedByMovementSimulatorOnlyOnceOnGround() {
        assertEquals(0.8, DragonFlightMotion.takeoffVelocity(-0.1, true, true));
        assertEquals(1.2, DragonFlightMotion.takeoffVelocity(1.2, true, true));
        assertEquals(-0.1, DragonFlightMotion.takeoffVelocity(-0.1, false, true));
        assertEquals(-0.1, DragonFlightMotion.takeoffVelocity(-0.1, true, false));
    }
}
