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

    @Test void takeoffImpulseRetriesUntilGroundContactEnds() {
        assertEquals(0.8, DragonFlightMotion.takeoffVelocity(-0.1, true, true, false));
        assertEquals(1.2, DragonFlightMotion.takeoffVelocity(1.2, true, true, true));
        assertEquals(0.8, DragonFlightMotion.takeoffVelocity(-0.1, true, false, false),
                "A brief loss of ground contact must not discard the launch impulse");
        assertEquals(0.8, DragonFlightMotion.takeoffVelocity(-0.1, false, true, true));
        assertEquals(-0.1, DragonFlightMotion.takeoffVelocity(-0.1, false, false, true));
        assertEquals(-0.1, DragonFlightMotion.takeoffVelocity(-0.1, false, true, false));
    }

    @Test void descentLevelsNearTerrainAtEveryRidableAgeWithoutLimitingClimb() {
        for (int age : new int[]{50, 75, 100, 125}) {
            float size = DragonGrowth.renderSize(age);
            double distance = DragonFlightMotion.descentFadeDistance(size);
            assertEquals(0, DragonFlightMotion.limitDescentPitch(40, 0, size));
            assertEquals(0, DragonFlightMotion.limitDescentPitch(40, -1, size));
            assertEquals(40, DragonFlightMotion.limitDescentPitch(40, distance, size));
            assertEquals(-60, DragonFlightMotion.limitDescentPitch(-60, 0, size));
            float previous = 0;
            for (int i = 0; i <= 100; i++) {
                float pitch = DragonFlightMotion.limitDescentPitch(40, distance * i / 100, size);
                assertTrue(pitch >= previous && pitch <= 40);
                assertTrue(pitch - previous < 1, "No sudden jump in the visual terrain fade");
                previous = pitch;
            }
        }
    }
}
