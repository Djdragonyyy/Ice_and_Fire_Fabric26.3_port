package com.github.alexthe666.iceandfire.fabric;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class DragonVoiceResourcesTest {
    @Test void originalFlightSamplesArePackagedUnchanged() throws Exception {
        Path legacy = Path.of(System.getProperty("iceandfire.legacyResources"));
        var original = JsonParser.parseString(Files.readString(legacy.resolve("assets/iceandfire/sounds.json"))).getAsJsonObject();
        try (var input = getClass().getClassLoader().getResourceAsStream("assets/iceandfire/sounds.json")) {
            var migrated = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals(original.get("dragon_flight"), migrated.get("dragon_flight"));
            for (int i = 0; i < 4; i++) {
                String path = "assets/iceandfire/sounds/mobs/dragon/flight_" + i + ".ogg";
                try (var sound = getClass().getClassLoader().getResourceAsStream(path)) {
                    assertNotNull(sound, path);
                    assertArrayEquals(Files.readAllBytes(legacy.resolve(path)), sound.readAllBytes(), path);
                }
            }
        }
    }
    @Test void originalVoiceDefinitionsAndReferencedOggFilesArePackagedUnchanged() throws Exception {
        Path legacy = Path.of(System.getProperty("iceandfire.legacyResources"));
        var original = JsonParser.parseString(Files.readString(legacy.resolve("assets/iceandfire/sounds.json"))).getAsJsonObject();
        try (var stream = getClass().getClassLoader().getResourceAsStream("assets/iceandfire/sounds.json")) {
            assertNotNull(stream);
            var migrated = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            for (String age : new String[]{"child", "teen", "adult"}) for (String action : new String[]{"idle", "hurt", "death", "roar"}) {
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
