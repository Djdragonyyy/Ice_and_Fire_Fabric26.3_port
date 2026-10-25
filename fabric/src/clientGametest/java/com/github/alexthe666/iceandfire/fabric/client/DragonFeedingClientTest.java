package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Real hand interaction/data synchronization, followed by deterministic GPU pose previews. */
public class DragonFeedingClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var owner = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            for (int age : new int[]{0, 25, 50, 75, 100}) {
                int id = world.getServer().computeOnServer(server -> {
                    owner.setGameMode(GameType.SURVIVAL);
                    var d = IafContent.FIRE_DRAGON.create(owner.level(), EntitySpawnReason.TRIGGERED);
                    for (int i = 0; i < age; i++) d.growOneDay();
                    d.setOwner(owner); d.setTame(true, false); d.setCommand(1); d.setMale(false);
                    d.setHunger(10); d.setHealth(10); d.setPos(owner.position().add(3, 0, 0));
                    owner.level().addFreshEntity(d);
                    owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, 3));
                    return d.getId();
                });
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon && client.player.getMainHandItem().is(Items.BEEF));
                context.runOnClient(client -> {
                    var d = (FireDragon) client.level.getEntity(id);
                    client.gameMode.interact(client.player, d, new EntityHitResult(d), InteractionHand.MAIN_HAND);
                });
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.attackAnimation() == DragonCombatState.EAT);
                world.getServer().runOnServer(server -> {
                    var d = (FireDragon) owner.level().getEntity(id);
                    require(d.hunger() == 40 && d.getHealth() == 13 && d.ageDays() == age, "Real server hand-food stats at age " + age);
                    require(owner.getMainHandItem().getCount() == 2, "Server consumed one beef");
                });
                var dragon = context.computeOnClient(client -> (FireDragon) client.level.getEntity(id));
                context.setScreen(() -> new Preview(dragon, 0)); context.waitTicks(3);
                context.takeScreenshot("dragon-feeding-stage-" + dragon.stage() + "-0.1.37");
                context.setScreen(() -> new Preview(dragon, 1)); context.waitTicks(3);
                context.takeScreenshot("dragon-body-wings-stage-" + dragon.stage() + "-0.1.37");
                if (age == 100) {
                    context.setScreen(() -> new Preview(dragon, 2)); context.waitTicks(3);
                    context.takeScreenshot("dragon-flight-secondary-motion-0.1.37");
                    context.setScreen(() -> new Preview(dragon, 3)); context.waitTicks(3);
                    context.takeScreenshot("dragon-wing-attack-with-gait-0.1.37");
                    context.setScreen(() -> new Preview(dragon, 4)); context.waitTicks(3);
                    context.takeScreenshot("dragon-flight-secondary-motion-late-0.1.37");
                }
                context.setScreen(() -> null);
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.attackAnimation() != DragonCombatState.EAT);
                world.getServer().runOnServer(server -> owner.level().getEntity(id).discard());
                context.waitFor(client -> client.level.getEntity(id) == null);
            }
        }
    }

    private static final class Preview extends Screen {
        private final FireDragon dragon; private final int mode;
        Preview(FireDragon dragon, int mode) { super(Component.literal("Original dragon model audit")); this.dragon = dragon; this.mode = mode; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            minecraft.gui.toastManager().clear();
            g.fill(0, 0, width, height, 0xff303640);
            boolean feeding = mode == 0;
            String title = switch (mode) { case 0 -> "feeding keyframes"; case 1 -> "original body / wings";
                case 2, 4 -> "original flight secondary motion"; default -> "wing attack / walking gait"; };
            g.text(font, "Stage " + dragon.stage() + " - " + title + " (normalized size)", 8, 6, 0xffffffff);
            String[] labels = switch (mode) {
                case 0 -> new String[]{"Start / tick 0", "Neck / tick 5", "Jaw / tick 10", "Recovery / tick 20"};
                case 1 -> new String[]{"Ground / side", "Ground / rear", "Sitting / folded wings", "Flight / extended wings"};
                case 2 -> new String[]{"Flight / tick 0", "Flight / tick 10"};
                case 4 -> new String[]{"Flight / tick 20", "Flight / tick 30"};
                default -> new String[]{"Wing blast / stationary", "Wing blast / walking", "Epic roar / stationary", "Epic roar / walking"};
            };
            var renderer = (FireDragonRenderer) (Object) minecraft.getEntityRenderDispatcher().getRenderer(dragon);
            int rows = mode == 2 || mode == 4 ? 1 : 2;
            for (int i = 0; i < rows * 2; i++) {
                int x0 = i % 2 * width / 2, x1 = x0 + width / 2;
                int y0 = 20 + i / 2 * (height - 20) / rows, y1 = y0 + (height - 20) / rows;
                g.text(font, labels[i], x0 + 8, y0, 0xffffffff);
                var s = renderer.createRenderState(); renderer.extractRenderState(dragon, s, 0);
                s.shadowPieces.clear(); s.outlineColor = 0;
                s.boundingBoxWidth /= s.scale; s.boundingBoxHeight /= s.scale; s.scale = 1;
                s.ageInTicks = 0; s.walkAnimationPos = s.walkAnimationSpeed = 0; s.partialTick = 0;
                s.flyProgress = s.hoverProgress = s.sleepProgress = s.shoulderProgress = s.breathProgress = 0;
                s.sitProgress = 0; s.sleeping = s.blinking = false; s.flying = false;
                s.yRot = s.xRot = s.flightPitch = 0; s.bodyRot = mode == 1 && i == 1 ? 35 : 125;
                s.attack = feeding ? DragonCombatState.EAT : DragonCombatState.NONE;
                s.attackTick = new int[]{0, 5, 10, 20}[i];
                if (mode == 1 && i == 2) s.sitProgress = 20;
                if (mode == 1 && i == 3) { s.flying = true; s.flyProgress = 20; s.flightCycle = 20; }
                if (mode == 2 || mode == 4) {
                    s.flying = true; s.flyProgress = 20; s.ageInTicks = (mode == 4 ? 20 : 0) + i * 10;
                    s.flightCycle = (int) (s.ageInTicks * 2) % 60;
                } else if (mode == 3) {
                    s.attack = i < 2 ? DragonCombatState.WING : DragonCombatState.EPIC_ROAR;
                    s.attackTick = i < 2 ? 15 : 25; s.walkCycle = 20; s.walkAnimationPos = 8;
                    s.walkAnimationSpeed = i % 2;
                }
                float previewScale = Math.min((x1 - x0) / 7.5F, (y1 - y0 - 12) / 3.4F) * (mode >= 2 ? 0.78F : 1);
                g.entity(s, previewScale,
                        new Vector3f(0, 0.8F, 0), new Quaternionf().rotateZ((float) Math.PI).rotateX(0.18F),
                        new Quaternionf().rotateX(0.18F), x0 + 2, y0 + 12, x1 - 2, y1 - 2);
            }
        }
    }
}
