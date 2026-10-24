package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;

/** Natural prey selection, real server attacks, synchronized poses and GPU-rendered preview. */
public class DragonCombatClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                BlockPos base = player.blockPosition();
                for (int x = -8; x <= 10; x++) for (int z = -8; z <= 8; z++) {
                    player.level().setBlockAndUpdate(base.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    for (int y = 0; y < 8; y++) player.level().setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                var dragon = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                dragon.setOwner(player); dragon.setTame(true, false);
                for (int i = 0; i < 50; i++) dragon.growOneDay();
                dragon.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
                dragon.setPos(player.position().add(3, 0, 0));
                dragon.setOnGround(true);
                dragon.getRandom().setSeed(725);
                var victim = EntityTypes.COW.create(player.level(), EntitySpawnReason.TRIGGERED);
                victim.setNoAi(true);
                victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); victim.setHealth(1000);
                victim.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                victim.setPos(dragon.position().add(3, 0, 0));
                player.level().addFreshEntity(victim);
                player.level().addFreshEntity(dragon);
                // Hunting must select the tagged prey; do not inject a combat target.
                return dragon.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon);
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class);
            for (int attack : new int[]{DragonCombatState.TAIL, DragonCombatState.WING}) {
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d
                        && d.attackAnimation() == attack && d.attackAnimationTick() >= 21 && d.attackAnimationTick() <= 28, 1000);
                var screenshot = context.takeScreenshot(attack == DragonCombatState.TAIL ? "dragon-tail-attack-0.1.17" : "dragon-wing-attack-0.1.17");
                int[] gui = new int[2];
                context.runOnClient(client -> {
                    gui[0] = client.getWindow().getGuiScaledWidth();
                    gui[1] = client.getWindow().getGuiScaledHeight();
                });
                assertStatisticsVisible(screenshot, gui[0], gui[1]);
            }
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setCommand(1));
            context.waitTicks(25);
            context.runOnClient(client -> {
                if (!(client.level.getEntity(id) instanceof FireDragon d) || d.attackAnimation() != DragonCombatState.NONE)
                    throw new AssertionError("Attack state must clear when ordered to sit");
            });
            context.setScreen(() -> null);
        }
    }

    private static void assertStatisticsVisible(java.nio.file.Path screenshot, int guiWidth, int guiHeight) {
        try {
            var image = javax.imageio.ImageIO.read(screenshot.toFile());
            int left = (guiWidth - 176) / 2, top = (guiHeight - 214) / 2;
            for (int row : new int[]{75, 84, 93, 102, 111, 120}) {
                int bright = 0;
                for (int x = (left + 25) * image.getWidth() / guiWidth; x < (left + 150) * image.getWidth() / guiWidth; x++)
                    for (int y = (top + row) * image.getHeight() / guiHeight; y < (top + row + 8) * image.getHeight() / guiHeight; y++) {
                        int rgb = image.getRGB(x, y);
                        if ((rgb >> 16 & 255) > 180 && (rgb >> 8 & 255) > 180 && (rgb & 255) > 180) bright++;
                    }
                if (bright < 15) throw new AssertionError("Animated preview hid statistics row " + row);
            }
        } catch (java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
    }
}
