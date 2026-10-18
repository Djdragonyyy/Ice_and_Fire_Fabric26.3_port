package com.github.alexthe666.iceandfire.fabric.client;

import com.google.gson.*;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class TabulaLoaderTest {
    /** Reference face layout: Citadel TabulaModelRenderUtils.ModelBox/TexturedQuad (LGPL-3.0).
     * https://github.com/AlexModGuy/Citadel/blob/master/src/main/java/com/github/alexthe666/citadel/client/model/TabulaModelRenderUtils.java
     */
    @Test void everySpikeVertexKeepsOriginalTabulaFaceOrientationAndUv() throws Exception {
        Path file = modelDirectory().resolve("firedragon_ground.tbl");
        JsonObject original;
        try (var zip = new ZipFile(file.toFile());
             var reader = new InputStreamReader(zip.getInputStream(zip.getEntry("model.json")), java.nio.charset.StandardCharsets.UTF_8)) {
            original = JsonParser.parseReader(reader).getAsJsonObject();
        }
        java.util.Map<String, JsonObject> cubes = new java.util.HashMap<>();
        collectSpikes(original.getAsJsonArray("cubes"), cubes);
        try (var input = Files.newInputStream(file)) {
            var parsed = TabulaLoader.parse(input);
            for (var entry : cubes.entrySet()) {
                var json = entry.getValue();
                float x = number(json, "offset", 0), y = number(json, "offset", 1), z = number(json, "offset", 2);
                float w = number(json, "dimensions", 0), h = number(json, "dimensions", 1), d = number(json, "dimensions", 2);
                float u = number(json, "txOffset", 0), v = number(json, "txOffset", 1);
                boolean mirror = json.get("txMirror").getAsBoolean();
                float x0 = mirror ? x + w : x, x1 = mirror ? x : x + w;
                float[][] positions = {{x0,y,z},{x1,y,z},{x1,y+h,z},{x0,y+h,z},
                        {x0,y,z+d},{x1,y,z+d},{x1,y+h,z+d},{x0,y+h,z+d}};
                int[][] faces = {{5,4,0,1},{2,3,7,6},{0,4,7,3},{1,0,3,2},{5,1,2,6},{4,5,6,7}};
                float[][] rects = {{u+d,v,u+d+w,v+d},{u+d+w,v+d,u+d+2*w,v},
                        {u,v+d,u+d,v+d+h},{u+d,v+d,u+d+w,v+d+h},
                        {u+d+w,v+d,u+2*d+w,v+d+h},{u+2*d+w,v+d,u+2*d+2*w,v+d+h}};
                float[][] normals = {{0,-1,0},{0,1,0},{-1,0,0},{0,0,-1},{1,0,0},{0,0,1}};
                var checked = new java.util.concurrent.atomic.AtomicInteger();
                parsed.parts().get(entry.getKey()).visit(new com.mojang.blaze3d.vertex.PoseStack(), (pose, path, index, cube) -> {
                    if (!path.isEmpty()) return;
                    checked.incrementAndGet();
                    assertEquals(6, cube.polygons.length);
                    for (int f = 0; f < faces.length; f++) {
                        var face = cube.polygons[f];
                        assertEquals(normals[f][0] * (mirror ? -1 : 1), face.normal().x(), 0.000001);
                        assertEquals(normals[f][1], face.normal().y(), 0.000001);
                        assertEquals(normals[f][2], face.normal().z(), 0.000001);
                        for (int corner = 0; corner < 4; corner++) {
                            int originalCorner = mirror ? 3 - corner : corner;
                            float[] point = positions[faces[f][originalCorner]];
                            var vertex = face.vertices()[corner];
                            assertEquals(point[0], vertex.x(), 0.000001, entry.getKey());
                            assertEquals(point[1], vertex.y(), 0.000001, entry.getKey());
                            assertEquals(point[2], vertex.z(), 0.000001, entry.getKey());
                            float[] rect = rects[f];
                            float expectedU = rect[originalCorner == 0 || originalCorner == 3 ? 2 : 0] / original.get("textureWidth").getAsFloat();
                            float expectedV = rect[originalCorner < 2 ? 1 : 3] / original.get("textureHeight").getAsFloat();
                            assertEquals(expectedU, vertex.u(), 0.000001, entry.getKey());
                            assertEquals(expectedV, vertex.v(), 0.000001, entry.getKey());
                        }
                    }
                });
                assertEquals(1, checked.get());
            }
        }
        assertEquals(22, cubes.size());
    }

    private static float number(JsonObject cube, String key, int i) {
        return cube.getAsJsonArray(key).get(i).getAsFloat();
    }

    private static void collectSpikes(JsonArray children, java.util.Map<String, JsonObject> result) {
        for (var child : children) {
            var cube = child.getAsJsonObject();
            if (cube.get("name").getAsString().startsWith("spike")) result.put(cube.get("name").getAsString(), cube);
            collectSpikes(cube.getAsJsonArray("children"), result);
        }
    }

    @Test void dorsalSpikesRetainAllFacesAndFiniteTextureCoordinates() throws Exception {
        try (var stream = Files.newInputStream(modelDirectory().resolve("firedragon_ground.tbl"))) {
            ModelPart root = TabulaLoader.load(stream);
            java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
            root.visit(new com.mojang.blaze3d.vertex.PoseStack(), (pose, path, index, cube) -> {
                if (!path.substring(path.lastIndexOf('/') + 1).startsWith("spike")) return;
                count.incrementAndGet();
                assertEquals(6, cube.polygons.length, path);
                assertTrue(cube.maxX > cube.minX && cube.maxY > cube.minY && cube.maxZ > cube.minZ, path);
                for (var face : cube.polygons) {
                    assertEquals(4, face.vertices().length);
                    for (var vertex : face.vertices()) {
                        assertTrue(Float.isFinite(vertex.u()) && Float.isFinite(vertex.v()), path);
                        assertTrue(vertex.u() >= 0 && vertex.u() <= 1 && vertex.v() >= 0 && vertex.v() <= 1, path);
                    }
                }
            });
            assertEquals(22, count.get(), "Every original dorsal spike must be present");
        }
    }

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
