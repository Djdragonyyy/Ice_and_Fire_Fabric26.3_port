package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonBreathVisualsTest {
    @Test void grownDragonFlameIsWiderAndDenserThanJuvenileFlame() {
        float small = DragonGrowth.renderSize(25);
        float large = DragonGrowth.renderSize(125);
        assertTrue(DragonBreathVisuals.size(large) > DragonBreathVisuals.size(small) * 3);
        assertTrue(DragonBreathVisuals.count(large, 1) > DragonBreathVisuals.count(small, 1));
        assertEquals(0.55F, DragonBreathVisuals.size(0));
        assertEquals(3, DragonBreathVisuals.size(100));
    }
    @Test void streamBeginsAtMouthAfterWarmupAndExtendsWithoutJumps() {
        assertEquals(0, DragonBreathVisuals.extension(20));
        assertTrue(DragonBreathVisuals.extension(21) < 0.01F);
        float previous = 0;
        for (int i = 21; i <= 40; i++) {
            float next = DragonBreathVisuals.extension(i);
            assertTrue(next > previous);
            assertTrue(next - previous < 0.08F);
            previous = next;
        }
        assertEquals(1, previous);
        assertEquals(1, DragonBreathVisuals.extension(500));
    }
}
