package com.github.alexthe666.iceandfire.fabric.networktest;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonControlPayload;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.world.InteractionHand;
import java.util.UUID;

public class DragonNetworkClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        try { runConnectedTest(context); }
        catch (RuntimeException | Error error) {
            NetworkTestFiles.write(System.getProperty("iaf.network.role") + "-failed", error.toString());
            throw error;
        }
    }
    private void runConnectedTest(ClientGameTestContext context) {
        String role = System.getProperty("iaf.network.role");
        require(role.equals("owner") || role.equals("guest"), "Explicit test role");
        String address = "127.0.0.1:" + System.getProperty("iaf.network.port");
        context.runOnClient(client -> ConnectScreen.startConnecting(client.gui.screen(), client, ServerAddress.parseString(address),
                new ServerData("Local Ice and Fire network test", address, ServerData.Type.OTHER), false, null));
        context.waitFor(client -> client.player != null && client.level != null && client.gui.overlay() == null, 1200);
        NetworkTestFiles.write(role + "-uuid", context.computeOnClient(client -> client.player.getUUID().toString()));
        context.waitFor(client -> !NetworkTestFiles.read("dragon-uuid").isEmpty(), 1200);
        UUID dragonId = UUID.fromString(NetworkTestFiles.read("dragon-uuid"));
        context.waitFor(client -> find(client, dragonId) != null && find(client, dragonId).getFlightSpeedModifier() == 0.5, 1200);
        context.runOnClient(client -> {
            FireDragon dragon = find(client, dragonId);
            require(DragonCombatConfig.current.dragonFlightSpeedMod == 1, "Client config differs from authoritative server value");
            require(dragon.getOwnerReference().getUUID().toString().equals(NetworkTestFiles.read("owner-uuid")), "Native owner UUID reaches both clients");
        });
        if (role.equals("guest")) {
            context.runOnClient(client -> client.gameMode.interact(client.player, find(client, dragonId), new net.minecraft.world.phys.EntityHitResult(find(client, dragonId), find(client, dragonId).getBoundingBox().getCenter()), InteractionHand.MAIN_HAND));
            for (int i = 0; i < 5; i++) {
                context.runOnClient(client -> ClientPlayNetworking.send(new DragonControlPayload(DragonControlPayload.TAKEOFF | DragonControlPayload.FIRE | DragonControlPayload.STRIKE)));
                context.waitTicks(2);
            }
            context.runOnClient(client -> require(client.player.getVehicle() == null, "Guest remains unmounted"));
            NetworkTestFiles.write("guest-forgery-sent", "yes");
        }
        context.waitFor(client -> NetworkTestFiles.read("phase").equals("TAKEOFF"), 1200);
        double initialY = context.computeOnClient(client -> find(client, dragonId).getY());
        if (role.equals("owner")) {
            context.waitFor(client -> client.player.getVehicle() instanceof FireDragon d && d.getUUID().equals(dragonId), 600);
            context.runOnClient(client -> ClientPlayNetworking.send(new DragonControlPayload(DragonControlPayload.TAKEOFF)));
        }
        context.waitFor(client -> find(client, dragonId).flying() && find(client, dragonId).getY() > initialY + 0.25, 1200);
        NetworkTestFiles.write(role + "-airborne", "yes");
        context.waitFor(client -> find(client, dragonId).male() && find(client, dragonId).getFlightSpeedModifier() == 0.25 && find(client, dragonId).armorParts() == 0x1001, 1200);
        context.runOnClient(client -> require(find(client, dragonId).getUUID().equals(dragonId), "Same native dragon receives server updates"));
        context.waitFor(client -> client.gui.screen() == null && client.gui.overlay() == null, 600);
        if (role.equals("owner")) {
            context.takeScreenshot("dragon-network-owner-first-person-0.1.35");
            context.runOnClient(client -> client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK));
            context.getInput().lookAt(0, 15);
        } else {
            float[] angles = context.computeOnClient(client -> {
                var delta = find(client, dragonId).getBoundingBox().getCenter().subtract(client.player.getEyePosition());
                return new float[]{(float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90,
                        (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()))};
            });
            context.getInput().lookAt(angles[0], angles[1]);
        }
        context.waitTicks(5);
        context.takeScreenshot("dragon-network-" + role + "-0.1.35");
        NetworkTestFiles.write(role + "-done", "yes");
        context.waitFor(client -> NetworkTestFiles.read("phase").equals("DISCONNECT"), 1200);
        context.runOnClient(client -> client.disconnectWithProgressScreen());
        context.waitFor(client -> client.player == null && client.level == null, 600);
        context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);
        NetworkTestFiles.write(role + "-disconnected", "yes");
    }
    private static FireDragon find(net.minecraft.client.Minecraft client, UUID uuid) {
        if (client.level == null) return null;
        for (var entity : client.level.entitiesForRendering()) if (entity instanceof FireDragon d && d.getUUID().equals(uuid)) return d;
        return null;
    }
}
