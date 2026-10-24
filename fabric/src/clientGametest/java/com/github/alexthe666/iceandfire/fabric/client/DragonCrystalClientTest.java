package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.dragon.SummoningCrystalItem;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonCrystalClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true); d.setCustomName(Component.literal("Crystal client"));
                d.setPos(player.position().add(2, 0, 0)); player.level().addFreshEntity(d);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.SUMMONING_CRYSTAL)); return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon && client.player.getMainHandItem().is(IafContent.SUMMONING_CRYSTAL));
            world.getServer().runOnServer(server -> {
                if (!player.interactOn(player.level().getEntity(id), InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction())
                    throw new AssertionError("Native binding interaction succeeds");
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.crystalBound()
                    && SummoningCrystalItem.dragonUuid(client.player.getMainHandItem()).filter(d.getUUID()::equals).isPresent());
            var icons = new ItemStack[2];
            context.runOnClient(client -> {
                icons[0] = new ItemStack(IafContent.SUMMONING_CRYSTAL); icons[1] = client.player.getMainHandItem().copy();
                for (var item : icons) {
                    var state = new ItemStackRenderState();
                    client.getItemModelResolver().updateForTopItem(state, item, ItemDisplayContext.GUI, client.level, client.player, 0);
                    if (state.isEmpty()) throw new AssertionError("Original inactive/active crystal models resolve");
                }
            });
            context.setScreen(() -> new CrystalIcons(icons)); context.waitTicks(3);
            context.takeScreenshot("dragon-crystal-empty-bound-0.1.28"); context.setScreen(() -> null);
            Vec3 target = world.getServer().computeOnServer(server -> {
                var pos = player.blockPosition().offset(5, -1, 3); player.level().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                var result = player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                if (!result.consumesAction() || SummoningCrystalItem.dragonUuid(player.getMainHandItem()).isPresent())
                    throw new AssertionError("Native useItemOn recalls original dragon and clears the crystal");
                return Vec3.atCenterOf(pos.above());
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && !d.crystalBound()
                    && d.position().subtract(target).horizontalDistance() < 0.2 && SummoningCrystalItem.dragonUuid(client.player.getMainHandItem()).isEmpty());
        }
    }
    private static final class CrystalIcons extends Screen {
        private final ItemStack[] icons;
        CrystalIcons(ItemStack[] icons) { super(Component.literal("Summoning crystal regression")); this.icons = icons; }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xff303030);
            graphics.text(font, "Empty", 100, 60, 0xffffffff); graphics.text(font, "Bound", 220, 60, 0xffffffff);
            graphics.item(icons[0], 100, 90); graphics.item(icons[1], 220, 90);
        }
    }
}
