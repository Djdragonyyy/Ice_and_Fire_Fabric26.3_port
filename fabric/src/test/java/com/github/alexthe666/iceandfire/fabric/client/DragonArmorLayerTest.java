package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DragonArmorLayerTest {
    @Test void everyArmorItemResolvesToItsOriginalPackagedIcon() throws Exception {
        var legacy = java.nio.file.Path.of(System.getProperty("iceandfire.legacyResources"));
        var packaged = java.nio.file.Path.of("build/resources/main/assets/iceandfire");
        for (String material : new String[]{"iron", "gold", "diamond", "silver", "dragonsteel_fire", "dragonsteel_ice", "copper", "dragonsteel_lightning"}) {
            for (String part : new String[]{"head", "neck", "body", "tail"}) {
                String name = "dragonarmor_" + material + "_" + part;
                var definition = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(packaged.resolve("items/" + name + ".json"))).getAsJsonObject();
                assertEquals("iceandfire:item/" + name, definition.getAsJsonObject("model").get("model").getAsString());
                var model = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(packaged.resolve("models/item/" + name + ".json"))).getAsJsonObject();
                assertEquals("iceandfire:item/" + name, model.getAsJsonObject("textures").get("layer0").getAsString());
                assertArrayEquals(java.nio.file.Files.readAllBytes(legacy.resolve("assets/iceandfire/textures/item/" + name + ".png")),
                        java.nio.file.Files.readAllBytes(packaged.resolve("textures/item/" + name + ".png")));
            }
        }
    }
    @Test void allArmorTexturesAreSubmittedAfterSkinInSlotOrder() {
        for (int material = 1; material <= 8; material++) {
            List<Integer> orders = new ArrayList<>();
            List<String> textures = new ArrayList<>();
            var ordered = (OrderedSubmitNodeCollector) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{OrderedSubmitNodeCollector.class}, (proxy, method, args) -> {
                        if (method.getName().equals("submitModel")) textures.add(((Identifier) args[3]).getPath());
                        return null;
                    });
            var collector = (SubmitNodeCollector) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                        if (method.getName().equals("order")) { orders.add((Integer) args[0]); return ordered; }
                        fail("Armor must not share the skin's unordered submission");
                        return null;
                    });
            var state = new FireDragonRenderer.State();
            state.armor = material | material << 4 | material << 8 | material << 12;
            DragonArmorLayer.submitLayers(null, state, new PoseStack(), collector, 0);
            assertEquals(List.of(2, 3, 4, 5), orders);
            assertEquals(List.of("head", "neck", "body", "tail").stream()
                    .map(part -> "textures/models/firedragon/armor_" + part + "_" + materialOrdinal(state) + ".png").toList(), textures);
        }
    }
    private static int materialOrdinal(FireDragonRenderer.State state) { return state.armor & 15; }
}
