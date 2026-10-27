package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import java.util.Objects;
import java.util.UUID;

/** Original four dragon camera views; native camera clipping handles the combined distance. */
public final class DragonCameraView {
    private static int view;
    private static UUID mounted;
    private static ClientLevel level;
    private static boolean wasDown;

    private DragonCameraView() {}
    static void initialize() {
        var key = KeyMappingHelper.registerKeyMapping(DragonKeyMappings.createViewBinding());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FireDragon riding = client.player != null && client.player.getVehicle() instanceof FireDragon d ? d : null;
            UUID next = riding == null ? null : riding.getUUID();
            var nextLevel = next == null ? null : client.level;
            if (!Objects.equals(next, mounted) || level != nextLevel) {
                mounted = next; level = nextLevel;
                if (riding != null && riding.isTame() && riding.isOwnedBy(client.player)) view = 2;
            }
            int clicks = 0; while (key.consumeClick()) clicks++;
            if (clicks == 0 && key.isDown() && !wasDown) clicks = 1;
            wasDown = key.isDown();
            if (next != null && clicks > 0) view = (view + clicks) % 4;
        });
    }

    public static float zoomDistance(Entity cameraEntity, float nativeDistance) {
        if (cameraEntity != Minecraft.getInstance().player || !(cameraEntity.getVehicle() instanceof FireDragon dragon)) return nativeDistance;
        float scale = DragonGrowth.renderSize(dragon.ageDays()) / 3;
        float extra = switch (view) { case 1 -> scale * 1.2F; case 2 -> scale * 3; case 3 -> scale * 5; default -> 0; };
        return nativeDistance + extra;
    }
}
