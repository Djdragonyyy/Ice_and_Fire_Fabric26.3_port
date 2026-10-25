package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Real death sync and complete C2S harvest; original corpse/skeletal GPU boards. */
public class DragonCorpseClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null && client.gui.overlay() == null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int[] ids = world.getServer().computeOnServer(server -> {
                player.setGameMode(GameType.SURVIVAL); int[] result = new int[5]; var origin = player.position();
                for (int i = 0; i < 5; i++) {
                    var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                    for (int age = 0; age < i * 25 + 12; age++) d.growOneDay();
                    d.setVariant(i % 4); d.setMale(i % 2 == 1); d.setNoAi(true); d.setSilent(true);
                    d.setTame(true, false); d.setOwner(player); d.refreshDimensions();
                    d.setPos(origin.add(5 + i * 12, 1, 0)); player.level().addFreshEntity(d); result[i] = d.getId();
                }
                return result;
            });
            context.waitFor(client -> java.util.Arrays.stream(ids).allMatch(id -> client.level.getEntity(id) instanceof FireDragon));
            world.getServer().runOnServer(server -> {
                for (int id : ids) {
                    var d = (FireDragon) player.level().getEntity(id);
                    if (!d.hurtServer(player.level(), d.damageSources().generic(), 10000) || !d.corpse()) throw new AssertionError("Actual lethal damage produces corpse");
                }
            });
            context.waitFor(client -> java.util.Arrays.stream(ids).allMatch(id -> client.level.getEntity(id) instanceof FireDragon d && d.corpse() && d.getHealth() == 0));
            context.waitTicks(45);
            for (int i = 0; i < 5; i++) {
                int id = ids[i]; context.setScreen(() -> new Preview(id)); context.waitTicks(3);
                context.takeScreenshot("dragon-corpse-skeletal-stage-" + (i + 1) + "-0.1.40"); context.setScreen(() -> null);
            }
            int adultId = ids[4];
            world.getServer().runOnServer(server -> {
                var d = (FireDragon) player.level().getEntity(adultId);
                player.teleportTo(d.getX() - 2, d.getY(), d.getZ());
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 3));
            });
            context.waitFor(client -> client.player.getMainHandItem().is(Items.GLASS_BOTTLE)
                    && client.player.distanceToSqr(client.level.getEntity(adultId)) < 16);
            for (int step = 1; step <= 22; step++) {
                int expected = step;
                context.runOnClient(client -> {
                    var entity = client.level.getEntity(adultId);
                    if (!client.gameMode.interact(client.player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND).consumesAction())
                        throw new AssertionError("Native C2S harvest action accepted");
                });
                if (step < 22) context.waitFor(client -> client.level.getEntity(adultId) instanceof FireDragon d
                        && d.deathStage() == expected && (expected > 3 || client.player.getMainHandItem().getCount() == 3 - expected));
                else context.waitFor(client -> client.level.getEntity(adultId) == null);
                if (step == 3) world.getServer().runOnServer(server -> {
                    if (player.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) != 3) throw new AssertionError("Three bottles give exactly three blood");
                });
                if (step == 11) context.runOnClient(client -> {
                    var d = (FireDragon) client.level.getEntity(adultId);
                    var renderer = (FireDragonRenderer) (Object) client.getEntityRenderDispatcher().getRenderer(d);
                    var state = renderer.createRenderState(); renderer.extractRenderState(d, state, 0);
                    if (!state.skeletal || !renderer.getTextureLocation(state).equals(DragonSkullRenderer.texture(5))) throw new AssertionError("Actual midpoint switches the synchronized skin to skeleton");
                });
            }
            world.getServer().runOnServer(server -> {
                int skulls = player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(15)).stream()
                        .filter(e -> e.getItem().is(IafContent.DRAGON_SKULL_FIRE) && DragonSkullItem.data(e.getItem()).equals(new DragonSkullData(5, 112)))
                        .mapToInt(e -> e.getItem().getCount()).sum();
                if (skulls != 1) throw new AssertionError("Complete C2S harvest gives one age112/stage5 skull");
            });
        }
    }
    private static final class Preview extends Screen {
        private final int id;
        Preview(int id) { super(Component.literal("Original corpse skin and pose")); this.id = id; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            var dragon = (FireDragon) minecraft.level.getEntity(id);
            var renderer = (FireDragonRenderer) (Object) minecraft.getEntityRenderDispatcher().getRenderer(dragon);
            g.fill(0, 0, width, height, 0xff303030);
            g.text(font, "Stage " + dragon.stage() + " corpse / original model / normalized size", 6, 5, 0xffffffff);
            for (int i = 0; i < 2; i++) {
                var state = renderer.createRenderState(); renderer.extractRenderState(dragon, state, 0);
                if (!state.corpse || state.modelDeadProgress != 20 || state.deathTime != 0) throw new AssertionError("Actual corpse fully settles without vanilla death rotation");
                state.skeletal = i == 1; state.bodyRot = 125; state.xRot = state.yRot = state.flightPitch = 0;
                state.shadowPieces.clear(); state.outlineColor = 0;
                state.boundingBoxWidth /= state.scale; state.boundingBoxHeight /= state.scale; state.scale = 1;
                int x0 = i * width / 2, x1 = (i + 1) * width / 2;
                g.text(font, i == 0 ? "Original skin / closed eyes" : "Original skeletal skin", x0 + 8, 22, 0xffffffff);
                float scale = Math.min((x1 - x0) / 7.5F, (height - 48) / 3.4F) * 0.9F;
                g.entity(state, scale, new Vector3f(0, 0.8F, 0), new Quaternionf().rotateZ((float) Math.PI).rotateX(0.18F),
                        new Quaternionf().rotateX(0.18F), x0 + 2, 35, x1 - 2, height - 4);
            }
        }
    }
}
