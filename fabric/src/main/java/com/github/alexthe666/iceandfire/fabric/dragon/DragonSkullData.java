package com.github.alexthe666.iceandfire.fabric.dragon;

/** Original skull stage/age growth table, with bounded input for imported item data. */
public record DragonSkullData(int stage, int ageDays) {
    private static final float[] SIZES = {1, 3, 7, 12.5F, 20, 30};
    public static final DragonSkullData DEFAULT = new DragonSkullData(4, 75);

    public DragonSkullData {
        stage = Math.clamp(stage, 1, 5);
        ageDays = Math.max(0, ageDays);
    }

    public float renderSize() {
        int index = stage - 1;
        int factor = Math.clamp(ageDays - 25 * index, 0, 25);
        return SIZES[index] + (SIZES[index + 1] - SIZES[index]) / 25 * factor;
    }
}
