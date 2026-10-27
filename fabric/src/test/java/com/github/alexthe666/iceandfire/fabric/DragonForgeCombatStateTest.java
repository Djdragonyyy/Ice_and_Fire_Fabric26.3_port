package com.github.alexthe666.iceandfire.fabric;

import com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonForgeCombatStateTest {
    @Test void forgeRetainsOriginalWarmupAndContinuesBeyondCombatTimeouts() {
        var state = new DragonCombatState();
        for (int i = 0; i < 20; i++) state.tickForgeBreath();
        assertFalse(state.emitting()); state.tickForgeBreath(); assertTrue(state.emitting());
        for (int i = 0; i < 1000; i++) state.tickForgeBreath();
        assertTrue(state.breathing()); assertTrue(state.emitting()); assertEquals(1, state.streamProgress());
    }
    @Test void endingForgeBreathCannotCancelAnIncomingMeleeAnimation() {
        var state = new DragonCombatState(); for (int i = 0; i < 40; i++) state.tickForgeBreath();
        state.start(DragonCombatState.BITE); state.stopForgeBreath();
        assertFalse(state.breathing()); assertEquals(DragonCombatState.BITE, state.animation());
        state.tick(true, false, 2); assertEquals(1, state.animationTick());
    }
}
