package com.github.alexthe666.iceandfire.fabric.dragon;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DragonLootTablesTest {
    @Test void nativeLootTablesRetainOriginalRollsWeightsItemsAndCustomization() throws Exception {
        for (String name : new String[]{"fire_dragon_male", "fire_dragon_female", "fire_dragon_skeleton"}) {
            var legacyPath = Path.of(System.getProperty("iceandfire.legacyResources"), "data/iceandfire/loot_tables/entities/dragon/" + name + ".json");
            var portPath = Path.of("src/main/resources/data/iceandfire/loot_table/entities/dragon/" + name + ".json");
            var legacy = JsonParser.parseString(Files.readString(legacyPath)).getAsJsonObject();
            var port = JsonParser.parseString(Files.readString(portPath)).getAsJsonObject();
            assertEquals("minecraft:entity", port.get("type").getAsString());
            var originalPools = legacy.getAsJsonArray("pools"); var pools = port.getAsJsonArray("pools");
            assertEquals(originalPools.size(), pools.size());
            for (int i = 0; i < pools.size(); i++) {
                var a = originalPools.get(i).getAsJsonObject(); var b = pools.get(i).getAsJsonObject();
                assertEquals(a.get("rolls"), b.get("rolls"));
                var ae = a.getAsJsonArray("entries"); var be = b.getAsJsonArray("entries"); assertEquals(ae.size(), be.size());
                for (int j = 0; j < ae.size(); j++) {
                    var x = ae.get(j).getAsJsonObject(); var y = be.get(j).getAsJsonObject();
                    assertEquals(x.get("name"), y.get("name")); assertEquals(x.get("weight"), y.get("weight"));
                    assertEquals("minecraft:item", y.get("type").getAsString());
                    assertEquals(1, y.getAsJsonArray("functions").get(0).getAsJsonObject().get("count").getAsInt());
                    assertEquals("iceandfire:customize_to_dragon", y.getAsJsonArray("functions").get(1).getAsJsonObject().get("function").getAsString());
                }
            }
        }
    }
}
