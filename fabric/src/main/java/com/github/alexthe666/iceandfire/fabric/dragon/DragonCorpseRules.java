package com.github.alexthe666.iceandfire.fabric.dragon;

/** Original EntityDragonBase corpse thresholds and harvest order (LGPL-3.0). */
public final class DragonCorpseRules {
    public enum Harvest { NONE, BLOOD, HEART, RANDOM, SKULL }
    private DragonCorpseRules() { }
    public static int limit(int ageDays) { return Math.min(Math.max(0, ageDays) / 5, 25); }
    public static boolean skeletal(int ageDays, int deathStage) { return deathStage >= (Math.max(0, ageDays) / 5) / 2; }
    public static boolean exhausted(int ageDays, int deathStage) { return deathStage >= Math.max(0, ageDays) / 5; }
    public static Harvest harvest(int ageDays, int deathStage, boolean bottle, boolean emptyHand,
                                  boolean blood, boolean heart, boolean skull) {
        int last = limit(ageDays);
        if (deathStage < 0 || deathStage >= last) return Harvest.NONE;
        if (bottle && deathStage < last / 2 && blood) return Harvest.BLOOD;
        // Original skull configuration gates the entire empty-hand branch, including bones.
        if (!emptyHand || !skull) return Harvest.NONE;
        if (deathStage >= last - 1) return Harvest.SKULL;
        if (deathStage == last / 2 - 1 && heart) return Harvest.HEART;
        return Harvest.RANDOM;
    }
}
