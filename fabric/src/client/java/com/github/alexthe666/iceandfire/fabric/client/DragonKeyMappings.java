package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.dragon.DragonControlPayload;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

final class DragonKeyMappings {
    private DragonKeyMappings() {}

    static void initialize() {
        // Original defaults: R = fire, G = strike, X = descend, jump = ascend.
        KeyMapping fire = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.dragon_fireAttack", 82, KeyMapping.Category.GAMEPLAY));
        KeyMapping strike = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.dragon_strike", 71, KeyMapping.Category.GAMEPLAY));
        KeyMapping down = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.dragon_down", 88, KeyMapping.Category.GAMEPLAY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !(client.player.getVehicle() instanceof FireDragon dragon)
                    || dragon.getControllingPassenger() != client.player || !ClientPlayNetworking.canSend(DragonControlPayload.TYPE)) return;
            int flags = 0;
            if (client.mouseHandler.isMouseGrabbed()) {
                if (client.options.keyJump.isDown()) flags |= DragonControlPayload.UP;
                if (down.isDown()) flags |= DragonControlPayload.DOWN;
                if (fire.isDown()) flags |= DragonControlPayload.FIRE;
                if (strike.isDown()) flags |= DragonControlPayload.STRIKE;
            }
            dragon.setRiderControls(flags);
            ClientPlayNetworking.send(new DragonControlPayload(flags));
        });
    }
}
