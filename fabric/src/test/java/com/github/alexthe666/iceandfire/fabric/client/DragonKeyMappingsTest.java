package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DragonKeyMappingsTest {
    @Test void defaultsResolveToOriginalLettersWithModernInputSystem() {
        var bindings = DragonKeyMappings.createBindings();
        assertEquals(InputConstants.getKey("key.keyboard.x"), bindings.down().getDefaultKey());
        assertEquals(InputConstants.getKey("key.keyboard.r"), bindings.fire().getDefaultKey());
        assertEquals(InputConstants.getKey("key.keyboard.g"), bindings.strike().getDefaultKey());
        assertEquals(InputConstants.getKey("key.keyboard.v"), bindings.takeoff().getDefaultKey());
        assertEquals("iceandfire:dragon", bindings.down().getCategory().id().toString());
    }

    @Test void takeoffCanBeReboundAndQuickClicksAreRetained() {
        var takeoff = DragonKeyMappings.createBindings().takeoff();
        var key = InputConstants.getKey("key.keyboard.b");
        takeoff.setKey(key);
        KeyMapping.resetMapping();
        KeyMapping.set(key, true);
        KeyMapping.click(key);
        KeyMapping.set(key, false);
        assertFalse(takeoff.isDown());
        assertTrue(takeoff.consumeClick(), "A brief press must survive until the input callback");
        assertFalse(takeoff.consumeClick());
        assertEquals(key, InputConstants.getKey(takeoff.saveString()));
        KeyMapping.releaseAll();
    }

    @Test void descentFollowsReboundKeyAndSurvivesOptionsSerialization() {
        var down = DragonKeyMappings.createBindings().down();
        var x = InputConstants.getKey("key.keyboard.x");
        var v = InputConstants.getKey("key.keyboard.v");
        down.setKey(v);
        KeyMapping.resetMapping();
        KeyMapping.set(x, true);
        assertFalse(down.isDown(), "Old default must stop controlling descent after rebinding");
        KeyMapping.set(v, true);
        assertTrue(down.isDown());
        KeyMapping.set(v, false);
        assertFalse(down.isDown());
        assertEquals(v, InputConstants.getKey(down.saveString()));
        down.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();
        KeyMapping.set(v, true);
        assertFalse(down.isDown(), "Unbinding must disable descent");
        KeyMapping.releaseAll();
    }
}
