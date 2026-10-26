package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Real client picking and C2S interaction/attack against the far tail of a stage-five dragon. */
public class DragonMultipartClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null && client.gui.overlay() == null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                player.setGameMode(GameType.CREATIVE); player.getAbilities().flying = true; player.onUpdateAbilities();
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                for (int i = 0; i < 125; i++) d.growOneDay();
                d.setNoAi(true); d.setSilent(true); d.setTame(true, false); d.setOwner(player); d.setMale(true); d.setVariant(2);
                d.setPos(player.position().add(5, 0, 4)); d.setYRot(0); d.yBodyRot = d.yHeadRot = 0; d.refreshDimensions(); d.setHealth(d.getMaxHealth());
                player.level().addFreshEntity(d); return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon && client.level.getEntitiesOfClass(DragonPart.class,
                    client.level.getEntity(id).getBoundingBox().inflate(30)).stream().filter(p -> p.parent() != null && p.parent().getId() == id).count() == 10);
            int[] originalParts = world.getServer().computeOnServer(server -> ((FireDragon) player.level().getEntity(id)).dragonParts().stream().mapToInt(DragonPart::getId).toArray());
            world.getServer().runOnServer(server -> {
                var d = (FireDragon) player.level().getEntity(id); player.teleportTo(d.getX() + 28, d.getY() + 16, d.getZ() - 32);
            });
            context.waitFor(client -> client.player.distanceToSqr(client.level.getEntity(id)) > 1000);
            var oldDebug = new DebugScreenEntryStatus[1];
            context.runOnClient(client -> {
                client.options.setCameraType(CameraType.FIRST_PERSON);
                client.player.lookAt(EntityAnchorArgument.Anchor.EYES, client.level.getEntity(id).position().add(0, 3, 0));
                oldDebug[0] = client.debugEntries.getStatus(DebugScreenEntries.ENTITY_HITBOXES);
                client.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.ALWAYS_ON);
                for (int partId : originalParts) {
                    var part = (DragonPart) client.level.getEntity(partId);
                    require(part != null && part.parent() == client.level.getEntity(id) && part.isPickable(), "All ten client parts resolve the tracked parent");
                }
                require(Math.abs(client.level.getEntity(originalParts[0]).getBbWidth() - 5) < 0.001, "Uncapped head dimension synchronized");
            });
            context.waitTicks(5); context.takeScreenshot("dragon-multipart-stage5-native-hitboxes-0.1.41");
            context.runOnClient(client -> client.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, oldDebug[0]));
            world.getServer().runOnServer(server -> {
                var tail = player.level().getEntity(originalParts[9]); var center = tail.getBoundingBox().getCenter();
                player.teleportTo(center.x, center.y - player.getEyeHeight(), center.z - 4);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_SWORD));
            });
            context.waitFor(client -> client.player.getMainHandItem().is(Items.NETHERITE_SWORD) && client.player.distanceToSqr(client.level.getEntity(originalParts[9])) < 25);
            context.runOnClient(client -> client.player.lookAt(EntityAnchorArgument.Anchor.EYES, client.level.getEntity(originalParts[9]).getBoundingBox().getCenter()));
            context.waitFor(client -> client.hitResult instanceof EntityHitResult hit && hit.getEntity().getId() == originalParts[9]);
            float before = world.getServer().computeOnServer(server -> ((FireDragon) player.level().getEntity(id)).getHealth());
            context.runOnClient(client -> client.gameMode.attack(client.player, ((EntityHitResult) client.hitResult).getEntity()));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.getHealth() < before);
            world.getServer().runOnServer(server -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF)));
            context.waitFor(client -> client.player.getMainHandItem().is(IafContent.DRAGON_STAFF));
            context.runOnClient(client -> client.gameMode.interact(client.player, client.level.getEntity(originalParts[9]),
                    new EntityHitResult(client.level.getEntity(originalParts[9])), InteractionHand.MAIN_HAND));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.command() == 1);
            world.getServer().runOnServer(server -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_HORN)));
            context.waitFor(client -> client.player.getMainHandItem().is(IafContent.DRAGON_HORN));
            context.runOnClient(client -> client.gameMode.interact(client.player, client.level.getEntity(originalParts[9]),
                    new EntityHitResult(client.level.getEntity(originalParts[9])), InteractionHand.MAIN_HAND));
            context.waitFor(client -> DragonHornItem.filled(client.player.getMainHandItem()) && client.level.getEntity(id) == null
                    && java.util.Arrays.stream(originalParts).allMatch(partId -> client.level.getEntity(partId) == null));
            var floor = world.getServer().computeOnServer(server -> {
                var pos = player.blockPosition().offset(0, -1, 24); player.level().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                player.teleportTo(pos.getX() - 3.5, pos.getY() + 1, pos.getZ() + 0.5); return pos;
            });
            context.waitFor(client -> client.level.getBlockState(floor).is(Blocks.STONE)
                    && client.player.distanceToSqr(floor.getX() - 3.5, floor.getY() + 1, floor.getZ() + 0.5) < 1);
            context.runOnClient(client -> require(client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false)).consumesAction(), "Native C2S release accepted"));
            context.waitFor(client -> !DragonHornItem.filled(client.player.getMainHandItem()));
            int restored = world.getServer().computeOnServer(server -> player.level().getEntitiesOfClass(FireDragon.class, player.getBoundingBox().inflate(15)).stream()
                    .filter(d -> d.isOwnedBy(player)).findFirst().orElseThrow().getId());
            context.waitFor(client -> client.level.getEntity(restored) instanceof FireDragon && client.level.getEntitiesOfClass(DragonPart.class,
                    client.level.getEntity(restored).getBoundingBox().inflate(30)).stream().filter(p -> p.parent() != null && p.parent().getId() == restored).count() == 10);
            int tailId = world.getServer().computeOnServer(server -> {
                var d = (FireDragon) player.level().getEntity(restored); var tail = d.dragonParts().get(9); var center = tail.getBoundingBox().getCenter();
                player.teleportTo(center.x, center.y - player.getEyeHeight(), center.z - 4); player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); return tail.getId();
            });
            context.waitFor(client -> client.player.getMainHandItem().isEmpty() && client.level.getEntity(tailId) != null
                    && client.player.distanceToSqr(client.level.getEntity(tailId)) < 25);
            context.runOnClient(client -> client.gameMode.interact(client.player, client.level.getEntity(tailId), new EntityHitResult(client.level.getEntity(tailId)), InteractionHand.MAIN_HAND));
            context.waitFor(client -> client.player.getVehicle() != null && client.player.getVehicle().getId() == restored);
            context.waitTicks(3);
            context.runOnClient(client -> {
                require(!(client.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof DragonPart part && part.parent() == client.player.getVehicle()), "Pilot must not pick its own multipart hitbox");
                require(client.level.getEntitiesOfClass(DragonPart.class, client.player.getBoundingBox().inflate(40)).stream()
                        .filter(p -> p.parent() != null && p.parent().getId() == restored).allMatch(p -> p.getRootVehicle() == client.player.getRootVehicle()), "Client root-vehicle identity follows the owner seat");
            });
        }
    }
}
