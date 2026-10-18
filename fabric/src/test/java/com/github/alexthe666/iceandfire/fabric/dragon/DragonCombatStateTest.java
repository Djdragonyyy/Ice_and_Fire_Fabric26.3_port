package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonCombatStateTest {
    @Test void breathHasTwentyTickWarmupAndFortyTickExtension() {
        var combat = new DragonCombatState();
        for (int tick = 1; tick <= 40; tick++) {
            combat.tick(true, true, 3);
            assertTrue(combat.breathing());
            assertEquals(tick > 20, combat.emitting());
            assertEquals(tick / 40F, combat.burnProgress());
        }
    }

    @Test void releasedKeyExpiresAfterTenTicksAndDismountCancelsImmediately() {
        var combat = new DragonCombatState();
        for (int i = 0; i < 30; i++) combat.tick(true, true, 3);
        for (int i = 0; i < 9; i++) { combat.tick(true, false, 3); assertTrue(combat.breathing()); }
        combat.tick(true, false, 3);
        assertFalse(combat.breathing());
        combat.tick(true, true, 3);
        combat.start(DragonCombatState.CHARGE);
        combat.tick(false, true, 3);
        assertFalse(combat.breathing());
        assertEquals(DragonCombatState.NONE, combat.animation());
        assertEquals(0, combat.burnProgress());
    }

    @Test void maximumBreathDurationScalesWithStageAndDoesNotSkipNextWarmup() {
        for (int stage = 2; stage <= 5; stage++) {
            var combat = new DragonCombatState();
            for (int i = 0; i < stage * 25; i++) { combat.tick(true, true, stage); assertTrue(combat.breathing()); }
            combat.tick(true, true, stage);
            assertFalse(combat.breathing());
            combat.tick(true, true, stage);
            assertTrue(combat.breathing());
            assertFalse(combat.emitting());
        }
    }

    @Test void animationDurationsAndChargeImpactFrameMatchOriginal() {
        for (int type : new int[]{DragonCombatState.BITE, DragonCombatState.CHARGE}) {
            var combat = new DragonCombatState();
            combat.start(type);
            int duration = type == DragonCombatState.BITE ? 35 : 30;
            int chargeFrames = 0;
            for (int tick = 1; tick < duration; tick++) {
                combat.tick(true, false, 3);
                assertEquals(type, combat.animation());
                assertEquals(tick, combat.animationTick());
                if (type == DragonCombatState.CHARGE && combat.animationTick() == 20) chargeFrames++;
            }
            assertEquals(type == DragonCombatState.CHARGE ? 1 : 0, chargeFrames);
            combat.tick(true, false, 3);
            assertEquals(DragonCombatState.NONE, combat.animation());
        }
    }

    @Test void holdingAttackDoesNotRestartAnimationEveryTick() {
        var combat = new DragonCombatState();
        combat.start(DragonCombatState.BITE);
        for (int i = 1; i < 35; i++) {
            combat.tick(true, false, 3);
            combat.start(DragonCombatState.BITE);
            assertEquals(i, combat.animationTick());
        }
    }

    @Test void hatchlingsCannotStartBreathing() {
        var combat = new DragonCombatState();
        for (int i = 0; i < 100; i++) { combat.tick(true, true, 1); assertFalse(combat.breathing()); }
    }
}
