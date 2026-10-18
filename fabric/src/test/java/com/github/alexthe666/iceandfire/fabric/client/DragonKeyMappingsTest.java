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
        assertEquals("iceandfire:dragon", bindings.down().getCategory().id().toString());
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
