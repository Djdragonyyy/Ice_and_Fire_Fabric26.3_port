package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import java.util.UUID;
import java.util.Objects;

/** Original mount/dismount perspective changes, applied only at transitions so manual views remain usable. */
final class DragonRiderCamera {
    private static UUID mountedDragon;
    private static ClientLevel mountedLevel;
    private static CameraType previous;
    private static boolean autoApplied;

    static void initialize() { ClientTickEvents.END_CLIENT_TICK.register(DragonRiderCamera::tick); }

    private static void tick(Minecraft client) {
        UUID next = client.player != null && client.player.getVehicle() instanceof FireDragon dragon
                && dragon.isTame() && dragon.isOwnedBy(client.player) && dragon.getControllingPassenger() == client.player
                ? dragon.getUUID() : null;
        ClientLevel level = next == null ? null : client.level;
        if (Objects.equals(next, mountedDragon) && level == mountedLevel) return;
        if (autoApplied && previous != null) client.options.setCameraType(previous);
        mountedDragon = next; mountedLevel = level;
        previous = next == null ? null : client.options.getCameraType();
        autoApplied = next != null && DragonClientConfig.current.dragonAuto3rdPerson;
        if (autoApplied) client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }
}
