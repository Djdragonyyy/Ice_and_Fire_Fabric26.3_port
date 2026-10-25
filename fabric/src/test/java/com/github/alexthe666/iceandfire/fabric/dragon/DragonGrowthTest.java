package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonGrowthTest {
    @Test void originalDeathExperienceChangesAtEveryStageBoundary() {
        int[] ages = {-1, 0, 24, 25, 49, 50, 74, 75, 99, 100, 125, 200};
        int[] xp = {5, 5, 5, 20, 20, 150, 150, 300, 300, 650, 650, 650};
        for (int i = 0; i < ages.length; i++) assertEquals(xp[i], DragonGrowth.experienceReward(ages[i]), "age " + ages[i]);
    }
    @Test void stageTransitionsAndRidingMatchOriginal() {
        int[] ages = {0, 24, 25, 49, 50, 74, 75, 99, 100, 125, 200};
        int[] stages = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5};
        for (int i = 0; i < ages.length; i++) {
            assertEquals(stages[i], DragonGrowth.stage(ages[i]), "age " + ages[i]);
            assertEquals(ages[i] >= 50, DragonGrowth.canCarryRider(ages[i]));
        }
    }

    @Test void sizeIsContinuousAtEveryStageAndStopsAtMaturity() {
        int[] ages = {0, 25, 50, 75, 100, 125, 200};
        float[] sizes = {1F, 3F, 7F, 12.5F, 20F, 30F, 30F};
        for (int i = 0; i < ages.length; i++) {
            assertEquals(sizes[i], DragonGrowth.renderSize(ages[i]), 0.0001F);
        }
        assertEquals(7F, DragonGrowth.scale(125));
    }

    @Test void defaultFireDragonStatsMatchOriginalEndpoints() {
        assertEquals(20, DragonGrowth.maxHealth(0, 500));
        assertEquals(500, DragonGrowth.maxHealth(125, 500));
        assertEquals(500, DragonGrowth.maxHealth(200, 500));
        assertEquals(1, DragonGrowth.attackDamage(0, 17));
        assertEquals(18, DragonGrowth.attackDamage(125, 17));
        assertEquals(0.15, DragonGrowth.movementSpeed(0), 0.000001);
        assertEquals(0.4, DragonGrowth.movementSpeed(125), 0.000001);
    }

    @Test void incubationAndNaturalGrowthKeepOriginalTickBoundaries() {
        assertFalse(DragonGrowth.shouldHatch(7200, DragonGrowth.DEFAULT_EGG_TIME));
        assertTrue(DragonGrowth.shouldHatch(7201, DragonGrowth.DEFAULT_EGG_TIME));
        assertEquals(0, DragonGrowth.days(23999));
        assertEquals(1, DragonGrowth.days(24000));
        assertEquals(50, DragonGrowth.days(1_200_000));
    }
}
