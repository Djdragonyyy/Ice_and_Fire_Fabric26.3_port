package com.github.alexthe666.iceandfire.fabric;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonCombatConfigTest {
    @Test void oldJsonRetainsOriginalLifecycleDefaults() {
        var config = new Gson().fromJson("{\"dragonGriefing\":1}", DragonCombatConfig.class);
        assertEquals(500, config.dragonHealth); assertEquals(17, config.dragonAttackDamage);
        assertEquals(3000, config.dragonHungerTickRate); assertEquals(7200, config.dragonEggTime);
        assertTrue(config.validValues());
    }
    @Test void malformedAndOutOfRangeValuesCannotReachTickArithmetic() {
        for (String json : new String[]{"{\"dragonHealth\":0}", "{\"dragonHealth\":100001}",
                "{\"dragonAttackDamage\":0}", "{\"dragonAttackDamage\":10001}",
                "{\"dragonHungerTickRate\":0}", "{\"dragonHungerTickRate\":10001}", "{\"dragonEggTime\":0}"})
            assertFalse(new Gson().fromJson(json, DragonCombatConfig.class).validValues(), json);
        var config = new DragonCombatConfig(); config.dragonHealth = Double.NaN; assertFalse(config.validValues());
        config.dragonHealth = 500; config.dragonEggTime = Integer.MAX_VALUE; assertTrue(config.validValues());
    }
}
