package com.github.alexthe666.iceandfire.fabric.client;

import com.google.gson.*;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class TabulaLoaderTest {
    private static Path modelDirectory() {
        return Path.of(System.getProperty("iceandfire.legacyResources"), "assets/iceandfire/models/tabula/firedragon");
    }

    @Test void everyOriginalFireDragonPoseRetainsEveryCube() throws Exception {
        try (var paths = Files.list(modelDirectory())) {
            var models = paths.filter(path -> path.toString().endsWith(".tbl")).toList();
            assertTrue(models.size() >= 40, "The full original pose library must be available");
            for (Path model : models) {
                try (var zip = new ZipFile(model.toFile()); var stream = Files.newInputStream(model)) {
                    JsonObject json;
                    try (var reader = new InputStreamReader(zip.getInputStream(zip.getEntry("model.json")), java.nio.charset.StandardCharsets.UTF_8)) {
                        json = JsonParser.parseReader(reader).getAsJsonObject();
                    }
                    ModelPart parsed = TabulaLoader.load(stream);
                    assertEquals(cubeCount(json.getAsJsonArray("cubes")) + 1, parsed.getAllParts().size(), model.toString());
                }
            }
        }
    }

    @Test void groundPoseKeepsOriginalPivotRotationAndGeometry() throws Exception {
        try (var stream = Files.newInputStream(modelDirectory().resolve("firedragon_ground.tbl"))) {
            ModelPart body = TabulaLoader.load(stream).getChild("BodyUpper");
            assertEquals(11.6F, body.y, 0.0001F);
            assertEquals(-10F, body.z, 0.0001F);
            assertEquals(Math.toRadians(5.22), body.xRot, 0.0001F);
            assertTrue(body.hasChild("BodyLower"));
            assertTrue(body.hasChild("Neck1"));
            body.xRot = 1F;
            body.resetPose();
            assertEquals(Math.toRadians(5.22), body.xRot, 0.0001F);
        }
    }

    private static int cubeCount(JsonArray cubes) {
        int count = 0;
        for (JsonElement cube : cubes) count += 1 + cubeCount(cube.getAsJsonObject().getAsJsonArray("children"));
        return count;
    }
}
