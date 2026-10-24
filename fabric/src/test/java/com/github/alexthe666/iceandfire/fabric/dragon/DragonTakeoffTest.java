package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonTakeoffTest {
    @Test void doubleJumpRetainsBothEdgesBetweenServerTicks() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.UP);
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.UP);
        launch.acceptControls(0);
        assertTrue(launch.tick(true, false, false, false));
        assertFalse(launch.tick(true, false, false, false));
    }

    @Test void slowSecondJumpAndContinuousPacketsAreNotDoubleTaps() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.UP);
        for (int i = 0; i < 8; i++) {
            launch.acceptControls(DragonControlPayload.UP);
            assertFalse(launch.tick(true, false, true, false));
        }
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.UP);
        assertFalse(launch.tick(true, false, true, false));
    }

    @Test void doubleJumpIsDiscardedForYoungDragonDownAndDismount() {
        for (int mode = 0; mode < 3; mode++) {
            var launch = new DragonTakeoff();
            launch.acceptControls(DragonControlPayload.UP);
            launch.acceptControls(0);
            launch.acceptControls(DragonControlPayload.UP);
            if (mode == 2) launch.reset();
            assertFalse(launch.tick(mode != 0, false, false, mode == 1));
            assertFalse(launch.tick(true, false, false, false));
        }
    }
    @Test void briefDedicatedPressSurvivesReleasePacketBeforeServerTick() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        launch.acceptControls(0);
        assertTrue(launch.tick(true, false, false, false));
        assertFalse(launch.tick(true, false, false, false));
    }

    @Test void continuousJumpTakesOffAfterOneSecondWithoutRepeatedPresses() {
        var launch = new DragonTakeoff();
        for (int i = 0; i < 20; i++) assertFalse(launch.tick(true, false, true, false));
        assertTrue(launch.tick(true, false, true, false));
        assertFalse(launch.tick(true, true, true, false));
    }

    @Test void holdingDedicatedKeyDoesNotRetriggerAfterLandingAndInvalidRequestsAreCleared() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        assertTrue(launch.tick(true, false, false, false));
        for (int i = 0; i < 40; i++) {
            launch.acceptControls(DragonControlPayload.TAKEOFF);
            assertFalse(launch.tick(true, i < 20, false, false));
        }
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        assertFalse(launch.tick(false, false, false, false));
        assertFalse(launch.tick(true, false, false, false));
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        assertFalse(launch.tick(true, false, true, true));
        assertFalse(launch.tick(true, false, false, false));
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        launch.reset();
        assertFalse(launch.tick(true, false, false, false));
    }
}
