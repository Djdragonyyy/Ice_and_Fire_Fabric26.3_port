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
            ItemStack[] icons = new ItemStack[2];
            context.runOnClient(client -> {
                icons[0] = new ItemStack(IafContent.DRAGON_HORN); icons[1] = client.player.getMainHandItem().copy();
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
        }
    }
    private static final class HornIcons extends Screen {
        private final ItemStack[] icons;
        HornIcons(ItemStack[] icons) { super(Component.literal("Dragon horn regression")); this.icons = icons; }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xff303030);
            graphics.text(font, "Empty", 100, 60, 0xffffffff); graphics.text(font, "Fire dragon", 190, 60, 0xffffffff);
            graphics.item(icons[0], 100, 90); graphics.item(icons[1], 190, 90);
        }
    }
}
