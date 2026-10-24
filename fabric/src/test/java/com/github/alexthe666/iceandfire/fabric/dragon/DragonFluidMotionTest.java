package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonFluidMotionTest {
    @Test void waterAndLavaVerticalControlsHaveOriginalSpeedsAndExclusiveDirections() {
        for (boolean lava : new boolean[]{false, true}) {
            assertEquals(lava ? 0.8F : 0.5F, DragonFluidMotion.vertical(lava, true, false));
            assertEquals(lava ? -0.8F : -0.5F, DragonFluidMotion.vertical(lava, false, true));
            assertEquals(0, DragonFluidMotion.vertical(lava, false, false));
            assertEquals(0, DragonFluidMotion.vertical(lava, true, true));
        }
    }
    @Test void lavaSpeedUsesOriginalGrowthCurveAndSprintFactor() {
        assertEquals(0.042, DragonFluidMotion.lavaSpeed(0.15F, false), 1.0E-7);
        assertEquals(0.172, DragonFluidMotion.lavaSpeed(0.4F, false), 1.0E-7);
        for (float speed : new float[]{0, 0.15F, 0.25F, 0.4F, 0.8F}) {
            assertEquals(DragonFluidMotion.lavaSpeed(speed, false) * 1.4F, DragonFluidMotion.lavaSpeed(speed, true), 1.0E-7);
            assertTrue(Float.isFinite(DragonFluidMotion.lavaSpeed(speed, true)));
        }
    }
}
