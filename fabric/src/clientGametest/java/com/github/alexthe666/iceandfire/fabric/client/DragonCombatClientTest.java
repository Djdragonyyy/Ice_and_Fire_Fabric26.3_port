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
            int[] capturedPrey = {-1};
            for (int attack : new int[]{DragonCombatState.TAIL, DragonCombatState.WING, DragonCombatState.SHAKE}) {
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d
                        && d.attackAnimation() == attack && d.attackAnimationTick() >= 21 && d.attackAnimationTick() <= 28, 1000);
                if (attack == DragonCombatState.SHAKE) context.runOnClient(client -> {
                    var d = (FireDragon) client.level.getEntity(id);
                    if (d.grippedPrey() == null || d.grippedPrey().getVehicle() != d || d.getControllingPassenger() != null)
                        throw new AssertionError("Client receives native prey passenger and grip identity, never a pilot");
                    var prey = d.grippedPrey(); capturedPrey[0] = prey.getId();
                    var state = client.getEntityRenderDispatcher().getRenderer(prey).createRenderState(prey, 1);
                    if (((DragonPreyRenderData) state).iceandfire$preyRotation() == null || state.passengerOffset != null)
                        throw new AssertionError("Captured mob must use animated head attachment and original carry rotation");
                    var dragonOnlyFrustum = new net.minecraft.client.renderer.culling.Frustum(new org.joml.Matrix4f(), new org.joml.Matrix4f()) {
                        @Override public boolean isVisible(net.minecraft.world.phys.AABB box) {
                            return box.getCenter().distanceToSqr(d.getBoundingBox().getCenter()) < 1;
                        }
                    };
                    var renderer = client.getEntityRenderDispatcher().getRenderer(d);
                    if (!renderer.shouldRender(d, dragonOnlyFrustum, client.player.getX(), client.player.getY(), client.player.getZ(), 1))
                        throw new AssertionError("Frustum fixture must include dragon body");
                    if (!client.getEntityRenderDispatcher().getRenderer(prey).shouldRender(prey, dragonOnlyFrustum,
                            client.player.getX(), client.player.getY(), client.player.getZ(), 1))
                        throw new AssertionError("Held visual prey must remain visible with its dragon when physical passenger bounds are outside frustum");
                });
                var screenshot = context.takeScreenshot(attack == DragonCombatState.TAIL ? "dragon-tail-attack-0.1.17"
                        : attack == DragonCombatState.WING ? "dragon-wing-attack-0.1.17" : "dragon-shake-prey-0.1.22");
                int[] gui = new int[2];
                context.runOnClient(client -> {
                    gui[0] = client.getWindow().getGuiScaledWidth();
                    gui[1] = client.getWindow().getGuiScaledHeight();
                });
                assertStatisticsVisible(screenshot, gui[0], gui[1]);
                if (attack == DragonCombatState.SHAKE) {
                    double[] focus = new double[3];
                    context.runOnClient(client -> {
                        var d = (FireDragon) client.level.getEntity(id);
                        var renderer = (FireDragonRenderer) client.getEntityRenderDispatcher().getRenderer(d);
                        var pos = renderer.preyRenderPose(d, d.grippedPrey(), 1).position();
                        focus[0] = pos.x; focus[1] = pos.y; focus[2] = pos.z;
                    });
                    context.setScreen(() -> null);
                    world.getServer().runOnServer(server -> {
                        player.teleportTo(focus[0] + 4, focus[1] + 1.5, focus[2] + 5);
                        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                                new net.minecraft.world.phys.Vec3(focus[0], focus[1] + 0.7, focus[2]));
                    });
                    context.waitTicks(2);
                    context.takeScreenshot("dragon-prey-in-mouth-world-0.1.22");
                }
            }
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setCommand(1));
            context.waitTicks(25);
            context.runOnClient(client -> {
                if (!(client.level.getEntity(id) instanceof FireDragon d)
                        || d.attackAnimation() != DragonCombatState.NONE && d.attackAnimation() != DragonCombatState.SPEAK)
                    throw new AssertionError("Attack state must clear when ordered to sit");
                if (client.level.getEntity(capturedPrey[0]) instanceof net.minecraft.world.entity.Mob prey) {
                    var state = client.getEntityRenderDispatcher().getRenderer(prey).createRenderState(prey, 1);
                    if (((DragonPreyRenderData) state).iceandfire$preyRotation() != null)
                        throw new AssertionError("Reused mob render state must clear carry rotation after release");
                }
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
