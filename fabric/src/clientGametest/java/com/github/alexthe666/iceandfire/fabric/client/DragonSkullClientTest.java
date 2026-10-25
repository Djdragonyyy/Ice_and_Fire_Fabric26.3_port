package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Native C2S item placement and entity sync, then deterministic GPU model/icon boards. */
public class DragonSkullClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null && client.gui.overlay() == null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            var base = world.getServer().computeOnServer(server -> { player.setGameMode(GameType.SURVIVAL); return player.blockPosition().offset(2, -1, 0); });
            int[] ids = new int[5];
            for (int i = 0; i < 5; i++) {
                int stage = i + 1; var data = new DragonSkullData(stage, i * 25 + 12); var pos = base.offset(i * 3, 0, 0);
                world.getServer().runOnServer(server -> {
                    player.level().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                    player.teleportTo(pos.getX() - 1.5, pos.getY() + 1, pos.getZ() + 0.5);
                    player.setItemInHand(InteractionHand.MAIN_HAND, DragonSkullItem.stack(data));
                });
                context.waitFor(client -> client.player.getMainHandItem().is(IafContent.DRAGON_SKULL_FIRE)
                        && DragonSkullItem.data(client.player.getMainHandItem()).equals(data) && client.level.getBlockState(pos).is(Blocks.STONE)
                        && client.player.distanceToSqr(pos.getX() - 1.5, pos.getY() + 1, pos.getZ() + 0.5) < 1);
                context.runOnClient(client -> {
                    if (!client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false)).consumesAction())
                        throw new AssertionError("Native client placement accepted at stage " + stage);
                });
                context.waitFor(client -> client.player.getMainHandItem().isEmpty());
                ids[i] = world.getServer().computeOnServer(server -> player.level().getEntitiesOfClass(DragonSkull.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(1.5)).stream().filter(s -> s.skullData().equals(data)).findFirst().orElseThrow().getId());
                int id = ids[i]; context.waitFor(client -> client.level.getEntity(id) instanceof DragonSkull skull && skull.skullData().equals(data));
            }
            context.runOnClient(client -> {
                for (int stage = 1; stage <= 5; stage++) {
                    if (client.getResourceManager().getResource(DragonSkullRenderer.texture(stage)).isEmpty()) throw new AssertionError("Original skeletal texture resolves");
                }
                for (String texture : new String[]{"dragonbone", "scales_red", "scales_green", "scales_bronze", "scales_gray", "fire_dragon_heart", "fire_dragon_blood", "fire_dragon_meat", "dragon_skull_fire"})
                    if (client.getResourceManager().getResource(IafContent.id("textures/item/" + texture + ".png")).isEmpty()) throw new AssertionError("Original item texture resolves: " + texture);
                for (var stack : icons()) {
                    var state = new ItemStackRenderState();
                    client.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GUI, client.level, client.player, 0);
                    if (state.isEmpty()) throw new AssertionError("Harvest item model resolves: " + stack);
                }
            });
            context.setScreen(() -> new Preview(ids)); context.waitTicks(5); context.takeScreenshot("dragon-skulls-five-stages-and-harvest-0.1.39"); context.setScreen(() -> null);
            context.runOnClient(client -> client.gameMode.attack(client.player, client.level.getEntity(ids[4])));
            context.waitFor(client -> client.level.getEntity(ids[4]) == null);
            world.getServer().runOnServer(server -> {
                var drops = player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, player.getBoundingBox().inflate(8));
                if (drops.stream().filter(e -> e.getItem().is(IafContent.DRAGON_SKULL_FIRE)).mapToInt(e -> e.getItem().getCount()).sum() != 1)
                    throw new AssertionError("C2S attack returns exactly one skull");
            });
        }
    }
    private static ItemStack[] icons() {
        return new ItemStack[]{new ItemStack(IafContent.DRAGON_BONE), new ItemStack(IafContent.FIRE_SCALES[0]), new ItemStack(IafContent.FIRE_SCALES[1]),
                new ItemStack(IafContent.FIRE_SCALES[2]), new ItemStack(IafContent.FIRE_SCALES[3]), new ItemStack(IafContent.FIRE_DRAGON_HEART),
                new ItemStack(IafContent.FIRE_DRAGON_BLOOD), new ItemStack(IafContent.FIRE_DRAGON_FLESH), new ItemStack(IafContent.DRAGON_SKULL_FIRE)};
    }
    private static final class Preview extends Screen {
        private final int[] ids;
        Preview(int[] ids) { super(Component.literal("Original fire skull geometry")); this.ids = ids; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            g.fill(0, 0, width, height, 0xff303030);
            g.text(font, "Original fire skulls / normalized size / floor + wall pose", 6, 5, 0xffffffff);
            int panelHeight = (height - 72) / 2;
            for (int i = 0; i < 5; i++) for (int wall = 0; wall < 2; wall++) {
                var skull = (DragonSkull) minecraft.level.getEntity(ids[i]);
                var renderer = (DragonSkullRenderer) (Object) minecraft.getEntityRenderDispatcher().getRenderer(skull);
                var state = renderer.createRenderState(); renderer.extractRenderState(skull, state, 0);
                state.wall = wall == 1; state.yaw = 140; state.shadowPieces.clear(); state.outlineColor = 0;
                int x0 = width * i / 5, x1 = width * (i + 1) / 5, y0 = 20 + wall * panelHeight;
                g.text(font, "Stage " + (i + 1) + (wall == 0 ? " floor" : " wall"), x0 + 4, y0, 0xffffffff);
                float scale = Math.min((x1 - x0 - 8) / 2.2F, (panelHeight - 16) / 2F) * 3 / state.skull.renderSize();
                g.entity(state, scale, new Vector3f(0, 0, 0), new Quaternionf().rotateZ((float) Math.PI).rotateX(0.1F),
                        new Quaternionf().rotateX(0.1F), x0 + 2, y0 + 12, x1 - 2, y0 + panelHeight - 2);
            }
            var items = icons(); String[] labels = {"Bone", "Red", "Green", "Bronze", "Gray", "Heart", "Blood", "Flesh", "Skull"};
            for (int i = 0; i < items.length; i++) {
                int x = i * width / items.length + 6;
                g.item(items[i], x, height - 40); g.text(font, labels[i], x, height - 19, 0xffffffff);
            }
        }
    }
}
