package com.github.alexthe666.iceandfire.fabric.dragon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonSkullDataTest {
    @Test void skullUsesOriginalGrowthAtEveryStageAndCapsOldAdults() {
        int[] ages = {0, 12, 24, 25, 49, 50, 74, 75, 99, 100, 125, 200};
        float[] sizes = {1, 1.96F, 2.92F, 3, 6.84F, 7, 12.28F, 12.5F, 19.7F, 20, 30, 30};
        for (int i = 0; i < ages.length; i++)
            assertEquals(sizes[i], new DragonSkullData(DragonGrowth.stage(ages[i]), ages[i]).renderSize(), 0.00001F);
    }
    @Test void importedMalformedMetadataCannotIndexOutsideTexturesOrInvertScale() {
        assertEquals(1, new DragonSkullData(Integer.MIN_VALUE, -1).renderSize());
        assertEquals(30, new DragonSkullData(Integer.MAX_VALUE, Integer.MAX_VALUE).renderSize());
        assertEquals(12.5F, new DragonSkullData(4, 0).renderSize());
        assertEquals(12.5F, DragonSkullData.DEFAULT.renderSize());
    }
}
