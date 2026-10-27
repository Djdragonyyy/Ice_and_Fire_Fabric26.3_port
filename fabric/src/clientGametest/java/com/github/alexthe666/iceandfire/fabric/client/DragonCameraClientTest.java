package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.UncheckedIOException;

public class DragonCameraClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static Path configFile() { return FabricLoader.getInstance().getConfigDir().resolve("iceandfire-client.json"); }
    private static byte[] readConfig() {
        try { return Files.readAllBytes(configFile()); } catch (IOException error) { throw new UncheckedIOException(error); }
    }
    private static void writeConfig(byte[] contents) {
        try { Files.write(configFile(), contents); } catch (IOException error) { throw new UncheckedIOException(error); }
    }
    private static void persistedOption(boolean enabled) {
        writeConfig(("{\"dragonAuto3rdPerson\":" + enabled + "}").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        DragonClientConfig.load();
        require(DragonClientConfig.current.dragonAuto3rdPerson == enabled, "Actual client configuration reload");
    }
    private static void mount(ClientGameTestContext context, int id) {
        context.runOnClient(c -> {
            var d = c.level.getEntity(id);
            require(c.gameMode.interact(c.player, d, new EntityHitResult(d), InteractionHand.MAIN_HAND).consumesAction(), "Native C2S mount interaction");
        });
        context.waitFor(c -> c.player.getVehicle() instanceof FireDragon && c.player.getVehicle().getId() == id);
        context.waitTicks(5);
    }
    @Override public void runTest(ClientGameTestContext context) {
        var oldConfig = DragonClientConfig.current;
        var oldFile = readConfig();
        var oldCamera = context.computeOnClient(c -> c.options.getCameraType());
        try {
            context.runOnClient(c -> { DragonClientConfig.current = new DragonClientConfig(); c.options.setCameraType(CameraType.FIRST_PERSON); });
            try (var world = context.worldBuilder().create()) {
                context.waitFor(c -> c.player != null && c.level != null && c.gui.screen() == null && c.gui.overlay() == null);
                int id = world.getServer().computeOnServer(server -> {
                    var p = world.getConnection().getServerPlayer(); p.setGameMode(GameType.CREATIVE);
                    p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    var base = p.blockPosition();
                    for (int x = -5; x <= 8; x++) for (int z = -5; z <= 5; z++) p.level().setBlockAndUpdate(base.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    var d = IafContent.FIRE_DRAGON.create(p.level(), EntitySpawnReason.TRIGGERED);
                    for (int i = 0; i < 50; i++) d.growOneDay();
                    d.setOwner(p); d.setTame(true, false); d.setCommand(1); d.setHunger(100); d.setNoAi(true);
                    d.setPos(p.position().add(3, 0, 0)); require(p.level().addFreshEntity(d), "Actual adult dragon");
                    return d.getId();
                });
                context.waitFor(c -> c.level.getEntity(id) instanceof FireDragon d && d.isOwnedBy(c.player));
                context.runOnClient(c -> c.options.setCameraType(CameraType.FIRST_PERSON)); mount(context, id);
                require(context.computeOnClient(c -> c.options.getCameraType() == CameraType.THIRD_PERSON_BACK), "Enabled default switches only on mounting");
                context.waitTicks(80); // Let the native dismount hint fade before the visual checkpoint.
                context.takeScreenshot("dragon-original-auto-third-person-mounted-0.1.50");
                context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_FRONT)); context.waitTicks(20);
                require(context.computeOnClient(c -> c.options.getCameraType() == CameraType.THIRD_PERSON_FRONT), "Manual perspective choice remains usable while mounted");
                world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().stopRiding());
                context.waitFor(c -> !c.player.isPassenger() && c.options.getCameraType() == CameraType.FIRST_PERSON);

                context.runOnClient(c -> persistedOption(false)); mount(context, id); context.waitTicks(10);
                require(context.computeOnClient(c -> c.options.getCameraType() == CameraType.FIRST_PERSON), "Disabled option preserves mounting perspective");
                context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
                world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().stopRiding());
                context.waitFor(c -> !c.player.isPassenger()); context.waitTicks(5);
                require(context.computeOnClient(c -> c.options.getCameraType() == CameraType.THIRD_PERSON_FRONT), "Disabled option preserves the user's dismount view");

                context.runOnClient(c -> persistedOption(true)); mount(context, id);
                require(context.computeOnClient(c -> c.options.getCameraType() == CameraType.THIRD_PERSON_BACK), "Re-enabled option captures the previous front view");
                // Close the actual world while mounted, exercising client disconnect cleanup.
            }
            context.waitFor(c -> c.player == null); context.waitTicks(5);
            require(context.computeOnClient(c -> c.options.getCameraType() == CameraType.THIRD_PERSON_FRONT), "Disconnect restores the captured perspective");
        } finally {
            context.runOnClient(c -> { writeConfig(oldFile); DragonClientConfig.current = oldConfig; c.options.setCameraType(oldCamera); });
        }
    }
}
