package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonHornItem;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonHornClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setTame(true, false); d.setOwner(player); d.setNoAi(true); d.setCustomName(Component.literal("Horn client"));
                d.setPos(player.position().add(2, 0, 0)); player.level().addFreshEntity(d);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_HORN)); return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) != null && client.player.getMainHandItem().is(IafContent.DRAGON_HORN));
            world.getServer().runOnServer(server -> {
                var d = player.level().getEntity(id);
                if (!player.interactOn(d, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction() || !d.isRemoved())
                    throw new AssertionError("Native player interaction must reach the horn after dragon mob interaction");
            });
            context.waitFor(client -> DragonHornItem.filled(client.player.getMainHandItem()) && client.level.getEntity(id) == null);
            ItemStack[] icons = new ItemStack[3];
            context.runOnClient(client -> {
                icons[0] = new ItemStack(IafContent.DRAGON_HORN); icons[1] = client.player.getMainHandItem().copy();
                icons[2] = new ItemStack(IafContent.DRAGON_FLUTE);
                if (client.getSoundManager().getSoundEvent(IafContent.DRAGON_FLUTE_SOUND.location()) == null)
                    throw new AssertionError("Original flute sound resolves in the real client");
                for (var stack : icons) {
                    var state = new ItemStackRenderState();
                    client.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GUI, client.level, client.player, 0);
                    if (state.isEmpty()) throw new AssertionError("Both original horn models must resolve");
                }
            });
            context.setScreen(() -> new HornIcons(icons)); context.waitTicks(5);
            context.takeScreenshot("dragon-horn-empty-filled-0.1.26"); context.setScreen(() -> null);
            int released = world.getServer().computeOnServer(server -> {
                var pos = player.blockPosition().offset(5, -1, 3); player.level().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                var result = IafContent.DRAGON_HORN.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
                if (!result.consumesAction() || DragonHornItem.filled(player.getMainHandItem())) throw new AssertionError("Release clears the server horn");
                return player.level().getEntitiesOfClass(com.github.alexthe666.iceandfire.fabric.dragon.FireDragon.class,
                        player.getBoundingBox().inflate(10)).stream().filter(d -> d.isOwnedBy(player)).findFirst().orElseThrow().getId();
            });
            context.waitFor(client -> !DragonHornItem.filled(client.player.getMainHandItem()) && client.level.getEntity(released) != null);
            world.getServer().runOnServer(server -> {
                var d = (com.github.alexthe666.iceandfire.fabric.dragon.FireDragon) player.level().getEntity(released);
                for (int i = 0; i < 25; i++) d.growOneDay();
                var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, player.level().registryAccess());
                d.saveWithoutId(output); output.putBoolean("Flying", true);
                d.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, player.level().registryAccess(), output.buildResult()));
                d.setPos(player.position().add(5, 15, 3));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_FLUTE));
            });
            context.waitFor(client -> client.level.getEntity(released) instanceof com.github.alexthe666.iceandfire.fabric.dragon.FireDragon d
                    && d.flying() && client.player.getMainHandItem().is(IafContent.DRAGON_FLUTE));
            world.getServer().runOnServer(server -> {
                if (!player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND).consumesAction())
                    throw new AssertionError("Native useItem activates the flute");
            });
            context.waitFor(client -> client.level.getEntity(released) instanceof com.github.alexthe666.iceandfire.fabric.dragon.FireDragon d
                    && !d.flying() && client.player.getCooldowns().isOnCooldown(client.player.getMainHandItem()));
        }
    }
    private static final class HornIcons extends Screen {
        private final ItemStack[] icons;
        HornIcons(ItemStack[] icons) { super(Component.literal("Dragon horn regression")); this.icons = icons; }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xff303030);
            graphics.text(font, "Empty", 100, 60, 0xffffffff); graphics.text(font, "Fire dragon", 190, 60, 0xffffffff);
            graphics.item(icons[0], 100, 90); graphics.item(icons[1], 190, 90);
            graphics.text(font, "Flute", 280, 60, 0xffffffff); graphics.item(icons[2], 280, 90);
        }
    }
}
