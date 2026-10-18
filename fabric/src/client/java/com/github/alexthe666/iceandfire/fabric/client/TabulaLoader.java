package com.github.alexthe666.iceandfire.fabric.client;

import com.google.gson.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Reads the original Tabula v4 assets without changing their geometry or UV coordinates. */
final class TabulaLoader {
    record Model(ModelPart root, Map<String, ModelPart> parts) {}
    private TabulaLoader() {}

    static ModelPart load(ResourceManager resources, Identifier asset) {
        return loadModel(resources, asset).root();
    }

    static Model loadModel(ResourceManager resources, Identifier asset) {
        try (InputStream input = resources.open(asset)) {
            return parse(input);
        } catch (IOException | RuntimeException error) {
            throw new IllegalStateException("Cannot load original Tabula model " + asset, error);
        }
    }

    static ModelPart load(InputStream input) throws IOException {
        return parse(input).root();
    }

    static Model parse(InputStream input) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(input)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                if (!entry.getName().equals("model.json")) continue;
                JsonObject json = JsonParser.parseReader(new InputStreamReader(zip, StandardCharsets.UTF_8)).getAsJsonObject();
                int width = json.get("textureWidth").getAsInt();
                int height = json.get("textureHeight").getAsInt();
                Map<String, ModelPart> roots = new LinkedHashMap<>();
                Map<String, ModelPart> parts = new LinkedHashMap<>();
                for (JsonElement element : json.getAsJsonArray("cubes")) {
                    JsonObject cube = element.getAsJsonObject();
                    roots.put(cube.get("name").getAsString(), part(cube, width, height, parts));
                }
                return new Model(new ModelPart(List.of(), roots), Collections.unmodifiableMap(parts));
            }
            throw new IOException("model.json missing");
        }
    }

    private static ModelPart part(JsonObject json, int width, int height, Map<String, ModelPart> parts) {
        Map<String, ModelPart> children = new LinkedHashMap<>();
        for (JsonElement element : json.getAsJsonArray("children")) {
            JsonObject child = element.getAsJsonObject();
            children.put(child.get("name").getAsString(), part(child, width, height, parts));
        }
        float inflate = json.get("mcScale").getAsFloat();
        ModelPart.Cube cube = new ModelPart.Cube(
                (int) value(json, "txOffset", 0), (int) value(json, "txOffset", 1),
                value(json, "offset", 0), value(json, "offset", 1), value(json, "offset", 2),
                value(json, "dimensions", 0), value(json, "dimensions", 1), value(json, "dimensions", 2),
                inflate, inflate, inflate, json.get("txMirror").getAsBoolean(), width, height,
                EnumSet.allOf(Direction.class));
        ModelPart part = new ModelPart(List.of(cube), children);
        part.setPos(value(json, "position", 0), value(json, "position", 1), value(json, "position", 2));
        part.setRotation(radians(json, 0), radians(json, 1), radians(json, 2));
        part.xScale = value(json, "scale", 0);
        part.yScale = value(json, "scale", 1);
        part.zScale = value(json, "scale", 2);
        part.skipDraw = json.get("hidden").getAsBoolean();
        part.setInitialPose(part.storePose());
        String name = json.get("name").getAsString();
        if (parts.putIfAbsent(name, part) != null) throw new IllegalArgumentException("Duplicate Tabula cube: " + name);
        return part;
    }

    private static float value(JsonObject json, String key, int index) {
        return json.getAsJsonArray(key).get(index).getAsFloat();
    }

    private static float radians(JsonObject json, int index) {
        return (float) Math.toRadians(value(json, "rotation", index));
    }
}
