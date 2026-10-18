package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonTakeoffTest {
    @Test void briefDedicatedPressSurvivesReleasePacketBeforeServerTick() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        launch.acceptControls(0);
        assertTrue(launch.tick(true, false, false, false, false));
        assertFalse(launch.tick(true, false, false, false, false));
    }

    @Test void continuousJumpTakesOffAfterOneSecondWithoutRepeatedPresses() {
        var launch = new DragonTakeoff();
        for (int i = 0; i < 20; i++) assertFalse(launch.tick(true, false, false, true, false));
        assertTrue(launch.tick(true, false, false, true, false));
        assertFalse(launch.tick(true, true, false, true, false));
    }

    @Test void blockedBackWaitsBrieflyButDoesNotStoreAnIndefiniteLaunch() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        for (int i = 0; i < 10; i++) assertFalse(launch.tick(true, false, true, false, false));
        assertTrue(launch.tick(true, false, false, false, false));
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        for (int i = 0; i < 21; i++) assertFalse(launch.tick(true, false, true, false, false));
        assertFalse(launch.tick(true, false, false, false, false));
    }

    @Test void holdingDedicatedKeyDoesNotRetriggerAfterLandingAndInvalidRequestsAreCleared() {
        var launch = new DragonTakeoff();
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        assertTrue(launch.tick(true, false, false, false, false));
        for (int i = 0; i < 40; i++) {
            launch.acceptControls(DragonControlPayload.TAKEOFF);
            assertFalse(launch.tick(true, i < 20, false, false, false));
        }
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        assertFalse(launch.tick(false, false, false, false, false));
        assertFalse(launch.tick(true, false, false, false, false));
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        assertFalse(launch.tick(true, false, false, true, true));
        assertFalse(launch.tick(true, false, false, false, false));
        launch.acceptControls(0);
        launch.acceptControls(DragonControlPayload.TAKEOFF);
        launch.reset();
        assertFalse(launch.tick(true, false, false, false, false));
    }
}
