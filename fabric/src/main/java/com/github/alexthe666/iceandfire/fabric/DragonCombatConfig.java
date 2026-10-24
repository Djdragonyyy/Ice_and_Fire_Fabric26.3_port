package com.github.alexthe666.iceandfire.fabric;

import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;

/** Only the combat options already migrated from the original ServerConfig are exposed. */
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

    public static void load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("iceandfire-combat.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(path)) {
                try (var reader = Files.newBufferedReader(path)) {
                    var parsed = gson.fromJson(reader, DragonCombatConfig.class);
                    if (parsed == null || !Double.isFinite(parsed.dragonAttackDamageFire)
                            || parsed.dragonAttackDamageFire < 0 || parsed.dragonAttackDamageFire > Float.MAX_VALUE / 5
                            || parsed.dragonGriefing < 0 || parsed.dragonGriefing > 2
                            || parsed.dragonBreakBlockCooldown < 0 || parsed.dragonBreakBlockCooldown > 10000
                            || parsed.dragonTargetSearchLength < 1 || parsed.dragonTargetSearchLength > 10000
                            || !Double.isFinite(parsed.dragonBlockBreakingDropChance)
                            || parsed.dragonBlockBreakingDropChance < 0 || parsed.dragonBlockBreakingDropChance > 1) {
                        throw new IllegalArgumentException("Invalid combat values");
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
