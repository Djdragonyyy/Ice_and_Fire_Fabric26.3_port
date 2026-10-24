package com.github.alexthe666.iceandfire.fabric;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class DragonVoiceResourcesTest {
    @Test void originalVoiceDefinitionsAndReferencedOggFilesArePackagedUnchanged() throws Exception {
        Path legacy = Path.of(System.getProperty("iceandfire.legacyResources"));
        var original = JsonParser.parseString(Files.readString(legacy.resolve("assets/iceandfire/sounds.json"))).getAsJsonObject();
        try (var stream = getClass().getClassLoader().getResourceAsStream("assets/iceandfire/sounds.json")) {
            assertNotNull(stream);
            var migrated = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            for (String age : new String[]{"child", "teen", "adult"}) for (String action : new String[]{"idle", "hurt", "death"}) {
                String key = "firedragon_" + age + "_" + action;
                assertEquals(original.get(key), migrated.get(key), key);
                for (var sound : migrated.getAsJsonObject(key).getAsJsonArray("sounds")) {
                    String path = "assets/iceandfire/sounds/" + sound.getAsJsonObject().get("name").getAsString().split(":", 2)[1] + ".ogg";
                    try (var audio = getClass().getClassLoader().getResourceAsStream(path)) {
                        assertNotNull(audio, path);
                        assertArrayEquals(Files.readAllBytes(legacy.resolve(path)), audio.readAllBytes(), path);
                    }
                }
            }
        }
    }
}
