package com.github.alexthe666.iceandfire.fabric.dragon;

/**
 * Fire dragon growth rules extracted from the original 1.20 EntityDragonBase
 * and EntityFireDragon (LGPL-3.0). Keeping these independent of Minecraft makes
 * the stage transitions testable without starting a game.
 */
public final class DragonGrowth {
    public static final int TICKS_PER_DAY = 24_000;
    public static final int DEFAULT_EGG_TIME = 7_200;
    private static final float[][] SIZES = {
            {1F, 3F}, {3F, 7F}, {7F, 12.5F}, {12.5F, 20F}, {20F, 30F}
    };

    private DragonGrowth() {}

    public static int days(int ageTicks) {
        return Math.max(0, ageTicks) / TICKS_PER_DAY;
    }

    public static int stage(int ageDays) {
        if (ageDays >= 100) return 5;
        if (ageDays >= 75) return 4;
        if (ageDays >= 50) return 3;
        if (ageDays >= 25) return 2;
        return 1;
    }

    public static float renderSize(int ageDays) {
        int days = Math.max(0, ageDays);
        int stageIndex = stage(days) - 1;
        float start = SIZES[stageIndex][0];
        float step = (SIZES[stageIndex][1] - start) / 25;
        int factor = days > 125 ? 25 : days - 25 * stageIndex;
        return start + step * factor;
    }

    public static float scale(int ageDays) {
        return Math.min(renderSize(ageDays) * 0.35F, 7F);
    }

    public static long maxHealth(int ageDays, double configuredMaxHealth) {
        double minHealth = configuredMaxHealth * 0.04;
        return Math.round(minHealth + (configuredMaxHealth - minHealth) / 125F * boundedAge(ageDays));
    }

    public static long attackDamage(int ageDays, int configuredAttackDamage) {
        return Math.round(1 + configuredAttackDamage / 125F * boundedAge(ageDays));
    }

    public static double movementSpeed(int ageDays) {
        return 0.15F + (0.4F - 0.15F) / 125F * boundedAge(ageDays);
    }

    public static boolean canCarryRider(int ageDays) {
        return stage(ageDays) > 2;
    }

    public static int experienceReward(int ageDays) {
        return switch (stage(ageDays)) {
            case 2 -> 20;
            case 3 -> 150;
            case 4 -> 300;
            case 5 -> 650;
            default -> 5;
        };
    }

    public static boolean shouldHatch(long incubationTicks, int configuredEggTime) {
        // The original condition is strictly greater, not greater than or equal.
        return incubationTicks > configuredEggTime;
    }

    private static int boundedAge(int ageDays) {
        return Math.clamp(ageDays, 0, 125);
    }
}
