package com.github.alexthe666.iceandfire.fabric;

import net.fabricmc.api.ModInitializer;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonControlPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IceAndFireFabric implements ModInitializer {
    public static final String MOD_ID = "iceandfire";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        DragonCombatConfig.load();
        IafContent.initialize();
        DragonControlPayload.initialize();
        com.github.alexthe666.iceandfire.fabric.dragon.DragonBreathPayload.initialize();
        LOGGER.info("Ice and Fire Fabric port: development build; milestone 1 is not complete.");
    }
}
