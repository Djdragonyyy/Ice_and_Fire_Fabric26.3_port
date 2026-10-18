package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.github.alexthe666.iceandfire.fabric.IceAndFireFabric;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonControlPayload;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

final class DragonKeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(IceAndFireFabric.MOD_ID, "dragon"));
    private DragonKeyMappings() {}

    record Bindings(KeyMapping fire, KeyMapping strike, KeyMapping down, KeyMapping takeoff) {}

    static Bindings createBindings() {
        // 26.3 uses SDL scancodes, not the GLFW key codes used by 1.20.1.
        // Namespaced IDs also avoid reloading the incorrect defaults saved by checkpoint 01.
        return new Bindings(
                new KeyMapping("key.iceandfire.dragon_fire", InputConstants.KEY_R, CATEGORY),
                new KeyMapping("key.iceandfire.dragon_strike", InputConstants.KEY_G, CATEGORY),
                new KeyMapping("key.iceandfire.dragon_down", InputConstants.KEY_X, CATEGORY),
                new KeyMapping("key.iceandfire.dragon_takeoff", InputConstants.KEY_V, CATEGORY));
    }

    static void initialize() {
        Bindings bindings = createBindings();
        KeyMapping fire = KeyMappingHelper.registerKeyMapping(bindings.fire());
        KeyMapping strike = KeyMappingHelper.registerKeyMapping(bindings.strike());
        KeyMapping down = KeyMappingHelper.registerKeyMapping(bindings.down());
        KeyMapping takeoff = KeyMappingHelper.registerKeyMapping(bindings.takeoff());
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            boolean launchPressed = takeoff.isDown();
            while (takeoff.consumeClick()) launchPressed = true;
            if (client.player == null || !(client.player.getVehicle() instanceof FireDragon dragon)
                    || dragon.getControllingPassenger() != client.player || !ClientPlayNetworking.canSend(DragonControlPayload.TYPE)) return;
            int flags = 0;
            if (client.mouseHandler.isMouseGrabbed()) {
                if (client.options.keyJump.isDown()) flags |= DragonControlPayload.UP;
                if (down.isDown()) flags |= DragonControlPayload.DOWN;
                if (fire.isDown()) flags |= DragonControlPayload.FIRE;
                if (strike.isDown()) flags |= DragonControlPayload.STRIKE;
                if (launchPressed) flags |= DragonControlPayload.TAKEOFF;
            }
            dragon.setRiderControls(flags);
            ClientPlayNetworking.send(new DragonControlPayload(flags));
        });
    }
}
