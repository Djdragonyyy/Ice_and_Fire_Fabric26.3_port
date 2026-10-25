package com.github.alexthe666.iceandfire.fabric;

import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;

/** Migrated original dragon options; filename retained for compatibility with existing installations. */
public final class DragonCombatConfig {
    public static DragonCombatConfig current = new DragonCombatConfig();
    public double dragonAttackDamageFire = 2;
    public int dragonGriefing = 0;
    public boolean tamedDragonGriefing = true;
    public boolean canDragonsHealFromBiting = true;
    public int dragonBreakBlockCooldown = 5;
    public double dragonBlockBreakingDropChance = 0.1;
    public boolean doDragonsSleep = true;
    public int dragonTargetSearchLength = 128;
    public double dragonHealth = 500;
    public int dragonAttackDamage = 17;
    public int dragonHungerTickRate = 3000;
    public int dragonEggTime = 7200;
    public int maxDragonFlight = 256;
    public double dragonFlightSpeedMod = 1;
    public int dragonWanderFromHomeDistance = 40;
    public int dragonFluteDistance = 4;
    public int dragonFlapNoiseDistance = 4;
    public boolean chunkLoadSummonCrystal = true;
    public boolean dragonDropBlood = true;
    public boolean dragonDropHeart = true;
    public boolean dragonDropSkull = true;

    boolean validValues() {
        return Double.isFinite(dragonAttackDamageFire) && dragonAttackDamageFire >= 0 && dragonAttackDamageFire <= Float.MAX_VALUE / 5
                && dragonGriefing >= 0 && dragonGriefing <= 2 && dragonBreakBlockCooldown >= 0 && dragonBreakBlockCooldown <= 10000
                && dragonTargetSearchLength >= 1 && dragonTargetSearchLength <= 10000
                && Double.isFinite(dragonBlockBreakingDropChance) && dragonBlockBreakingDropChance >= 0 && dragonBlockBreakingDropChance <= 1
                && Double.isFinite(dragonHealth) && dragonHealth >= 1 && dragonHealth <= 100000
                && dragonAttackDamage >= 1 && dragonAttackDamage <= 10000
                && dragonHungerTickRate >= 1 && dragonHungerTickRate <= 10000 && dragonEggTime >= 1
                && Double.isFinite(dragonFlightSpeedMod) && dragonFlightSpeedMod >= 0 && dragonFlightSpeedMod <= 2
                && maxDragonFlight >= 100 && dragonWanderFromHomeDistance >= 1 && dragonWanderFromHomeDistance <= 10000
                && dragonFluteDistance >= 0 && dragonFluteDistance <= 10000
                && dragonFlapNoiseDistance >= 0 && dragonFlapNoiseDistance <= 10000;
    }

    public static void load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("iceandfire-combat.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(path)) {
                try (var reader = Files.newBufferedReader(path)) {
                    var parsed = gson.fromJson(reader, DragonCombatConfig.class);
                    if (parsed == null || !parsed.validValues()) {
                        throw new IllegalArgumentException("Invalid dragon values");
                    }
                    current = parsed;
                }
            } else {
                Files.createDirectories(path.getParent());
                Files.writeString(path, gson.toJson(current));
            }
        } catch (IOException | RuntimeException error) {
            IceAndFireFabric.LOGGER.error("Cannot load {}; using original combat defaults", path, error);
            current = new DragonCombatConfig();
        }
    }
}
