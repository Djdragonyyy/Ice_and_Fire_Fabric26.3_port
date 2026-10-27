package com.github.alexthe666.iceandfire.fabric;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class DragonForgeResourcesTest {
    @Test void originalForgeModelsTexturesAndGuiArePackagedUnchanged() throws Exception {
        Path legacy = Path.of(System.getProperty("iceandfire.legacyResources")); int checked = 0;
        try (var files = Files.walk(legacy.resolve("assets/iceandfire"))) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String path = legacy.relativize(file).toString().replace('\\', '/');
                if (path.contains("/blockstates/") || !(path.contains("/models/") || path.contains("/textures/"))) continue;
                String name = file.getFileName().toString();
                if (!(name.startsWith("dragonforge_fire") || name.startsWith("dragon_bone_block") || name.startsWith("dragonsteel_fire_ingot"))) continue;
                try (var input = getClass().getClassLoader().getResourceAsStream(path)) { assertNotNull(input, path); assertArrayEquals(Files.readAllBytes(file), input.readAllBytes(), path); checked++; }
            }
        }
        assertTrue(checked >= 20, "Original resource set must include models, block textures, animation metadata, GUI and ingot");
    }
}
