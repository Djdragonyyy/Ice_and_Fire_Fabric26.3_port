package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import java.util.Arrays;

public class DragonCameraZoomClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static KeyMapping key(Options options) {
        return Arrays.stream(options.keyMappings).filter(k -> k.getName().equals("key.iceandfire.dragon_change_view")).findFirst().orElseThrow();
    }
    private static double distance(Minecraft c) { return c.gameRenderer.mainCamera().position().distanceTo(c.player.getEyePosition()); }
    private static double base(Minecraft c) {
        var d = (FireDragon) c.player.getVehicle();
        return Math.max(c.player.getScale() * c.player.getAttributeValue(Attributes.CAMERA_DISTANCE), d.getScale() * d.getAttributeValue(Attributes.CAMERA_DISTANCE));
    }
    private static void waitDistance(ClientGameTestContext context, double expected) {
        context.waitFor(c -> Math.abs(distance(c) - expected) < 0.6);
    }
    private static void press(ClientGameTestContext context, int ticks) {
        context.getInput().holdKey(DragonCameraZoomClientTest::key); context.waitTicks(ticks);
        context.getInput().releaseKey(DragonCameraZoomClientTest::key); context.waitTicks(3);
    }
    private static void screenshot(ClientGameTestContext context, String name) {
        context.waitFor(c -> c.level.hasChunkAt(c.gameRenderer.mainCamera().blockPosition()));
        context.waitTicks(10);
        context.takeScreenshot(name);
    }
    @Override public void runTest(ClientGameTestContext context) {
        var old = context.computeOnClient(c -> c.options.getCameraType());
        var oldRenderDistance = context.computeOnClient(c -> c.options.renderDistance().get());
        try (var world = context.worldBuilder().create()) {
            context.waitFor(c -> c.player != null && c.level != null && c.gui.screen() == null && c.gui.overlay() == null);
            context.runOnClient(c -> { c.options.renderDistance().set(6); c.options.broadcastOptions(); c.options.setCameraType(CameraType.FIRST_PERSON); });
            world.getServer().runOnServer(server -> server.getPlayerList().setViewDistance(6));
            int id = world.getServer().computeOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); p.setGameMode(GameType.CREATIVE);
                var d = IafContent.FIRE_DRAGON.create(p.level(), EntitySpawnReason.TRIGGERED);
                for (int i = 0; i < 50; i++) d.growOneDay();
                d.setOwner(p); d.setTame(true, false); d.setNoAi(true); d.setCommand(1); d.setHunger(100);
                d.setPos(p.position().add(3, 0, 0)); require(p.level().addFreshEntity(d), "Native adult dragon");
                require(p.startRiding(d), "Actual connected owner is mounted"); return d.getId();
            });
            context.waitFor(c -> c.player.getVehicle() instanceof FireDragon && c.player.getVehicle().getId() == id);
            context.runOnClient(c -> { c.player.setYRot(0); c.player.setXRot(0); }); context.waitTicks(85);
            world.getServer().runOnServer(server -> require(world.getConnection().getServerPlayer().clientInformation().viewDistance() == 6,
                    "The integrated server received the actual client chunk-distance request"));
            int[] ages = {50, 75, 100, 125}; float[] originalSizes = {7, 12.5F, 20, 30};
            for (int i = 0; i < ages.length; i++) {
                int age = ages[i]; double size = originalSizes[i];
                world.getServer().runOnServer(server -> {
                    var d = (FireDragon) world.getConnection().getServerPlayer().level().getEntity(id);
                    while (d.ageDays() < age) d.growOneDay();
                });
                context.waitFor(c -> ((FireDragon) c.player.getVehicle()).ageDays() == age); context.waitTicks(10);
                double nativeBase = context.computeOnClient(DragonCameraZoomClientTest::base);
                waitDistance(context, nativeBase + size);
                screenshot(context, "dragon-original-medium-camera-age" + age + "-0.1.51");
                press(context, i == 0 ? 20 : 3); // Holding F7 must not cycle every tick.
                waitDistance(context, nativeBase + size / 3 * 5);
                press(context, 3); waitDistance(context, nativeBase);
                press(context, 3); waitDistance(context, nativeBase + size / 3 * 1.2);
                press(context, 3); waitDistance(context, nativeBase + size);
            }
            double expected = context.computeOnClient(DragonCameraZoomClientTest::base) + 30;
            context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_FRONT)); waitDistance(context, expected);
            screenshot(context, "dragon-original-front-camera-age125-0.1.51");
            context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_BACK)); waitDistance(context, expected);
            BlockPos wall = world.getServer().computeOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); var at = p.blockPosition().north(6);
                for (int x = -4; x <= 4; x++) for (int y = -8; y <= 8; y++) p.level().setBlockAndUpdate(at.offset(x, y, 0), Blocks.STONE.defaultBlockState());
                return at;
            });
            context.waitFor(c -> c.level.getBlockState(wall).is(Blocks.STONE));
            context.waitFor(c -> distance(c) < 7.5 && distance(c) > 0.1);
            screenshot(context, "dragon-camera-native-wall-clipping-0.1.51");
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                for (int x = -4; x <= 4; x++) for (int y = -8; y <= 8; y++) p.level().setBlockAndUpdate(wall.offset(x, y, 0), Blocks.AIR.defaultBlockState());
            });
            context.waitFor(c -> c.level.getBlockState(wall).isAir()); waitDistance(context, expected);
            context.runOnClient(c -> c.options.setCameraType(CameraType.FIRST_PERSON));
            context.waitFor(c -> distance(c) < 0.6);
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().stopRiding());
            context.waitFor(c -> !c.player.isPassenger());
            context.waitTicks(10); // Let the mount-transition listener restore the previous view before choosing a new one.
            context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            try {
                context.waitFor(c -> Math.abs(distance(c) - c.player.getScale() * c.player.getAttributeValue(Attributes.CAMERA_DISTANCE)) < 0.6);
            } catch (AssertionError error) {
                throw new AssertionError(context.computeOnClient(c -> "Dismounted camera: view=" + c.options.getCameraType()
                        + " distance=" + distance(c) + " expected=" + c.player.getScale() * c.player.getAttributeValue(Attributes.CAMERA_DISTANCE)
                        + " pitch=" + c.player.getXRot() + " passenger=" + c.player.isPassenger()), error);
            }
        } finally {
            context.getInput().releaseKey(DragonCameraZoomClientTest::key);
            context.runOnClient(c -> { c.options.setCameraType(old); c.options.renderDistance().set(oldRenderDistance); c.options.broadcastOptions(); });
        }
    }
}
