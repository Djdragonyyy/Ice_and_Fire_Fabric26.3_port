package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static com.github.alexthe666.iceandfire.fabric.dragon.DragonCorpseRules.*;
import static org.junit.jupiter.api.Assertions.*;

class DragonCorpseRulesTest {
    @Test void thresholdsPreserveYoungRemovalAdultLimitAndOldDragonSkeletonBoundary() {
        assertTrue(exhausted(4, 0)); assertFalse(exhausted(5, 0)); assertEquals(1, limit(5));
        assertEquals(20, limit(100)); assertEquals(25, limit(125)); assertEquals(25, limit(200));
        assertFalse(skeletal(125, 11)); assertTrue(skeletal(125, 12));
        assertFalse(skeletal(200, 19)); assertTrue(skeletal(200, 20));
        assertFalse(exhausted(200, 25)); // Original removal threshold is not the interaction cap.
    }
    @Test void originalHarvestOrderHasBloodPriorityHeartAtMidpointAndSkullLast() {
        assertEquals(Harvest.BLOOD, harvest(100, 9, true, false, true, true, true));
        assertEquals(Harvest.HEART, harvest(100, 9, false, true, true, true, true));
        assertEquals(Harvest.RANDOM, harvest(100, 8, false, true, true, true, true));
        assertEquals(Harvest.NONE, harvest(100, 10, true, false, true, true, true));
        assertEquals(Harvest.RANDOM, harvest(100, 18, false, true, true, true, true));
        assertEquals(Harvest.SKULL, harvest(100, 19, false, true, true, true, true));
        assertEquals(Harvest.NONE, harvest(100, 20, false, true, true, true, true));
        assertEquals(Harvest.SKULL, harvest(5, 0, false, true, true, true, true));
    }
    @Test void independentConfigurationsKeepOriginalEmptyHandGate() {
        assertEquals(Harvest.NONE, harvest(100, 0, true, false, false, true, true));
        assertEquals(Harvest.NONE, harvest(100, 0, false, true, true, true, false));
        assertEquals(Harvest.BLOOD, harvest(100, 0, true, false, true, true, false));
        assertEquals(Harvest.RANDOM, harvest(100, 9, false, true, true, false, true));
        assertEquals(Harvest.SKULL, harvest(100, 19, false, true, false, false, true));
    }
}
