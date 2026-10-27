package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IceAndFireFabric;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.io.IOException;

/** Original client option, kept local to the client rather than the server combat configuration. */
public final class DragonClientConfig {
    public static DragonClientConfig current = new DragonClientConfig();
    public boolean dragonAuto3rdPerson = true;

    static void load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("iceandfire-client.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(path)) {
                try (var reader = Files.newBufferedReader(path)) {
                    var parsed = gson.fromJson(reader, DragonClientConfig.class);
                    if (parsed == null) throw new IllegalArgumentException("Empty dragon client configuration");
                    current = parsed;
                }
            } else {
                Files.createDirectories(path.getParent()); Files.writeString(path, gson.toJson(current));
            }
        } catch (IOException | RuntimeException error) {
            IceAndFireFabric.LOGGER.error("Cannot load {}; using original client defaults", path, error);
            current = new DragonClientConfig();
        }
    }
}
