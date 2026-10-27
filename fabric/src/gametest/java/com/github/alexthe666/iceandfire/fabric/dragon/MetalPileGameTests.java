package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import com.github.alexthe666.iceandfire.fabric.treasure.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.*;
import java.util.*;

public class MetalPileGameTests {
    private static ServerPlayer player(GameTestHelper h) {
        var profile = new com.mojang.authlib.GameProfile(UUID.randomUUID(), "pile-test-player");
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false);
        var p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), profile, cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection, p, cookie); p.setGameMode(GameType.SURVIVAL);
        p.setPos(h.absoluteVec(new Vec3(18.5, 2, 20.5))); h.assertTrue(!p.isCreative(), "Actual survivor fixture"); return p;
    }
    private static void done(GameTestHelper h, ServerPlayer p) { p.connection.disconnect(net.minecraft.network.chat.Component.literal("Pile regression complete")); h.succeed(); }
    private static net.minecraft.world.InteractionResult use(GameTestHelper h, ServerPlayer p, BlockPos pos) {
        return p.gameMode.useItemOn(p, h.getLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(h.absolutePos(pos)), Direction.UP, h.absolutePos(pos), false));
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void allTwentyFourStatesKeepOriginalGeometryAndExactLayerLoot(GameTestHelper h) {
        var pos = new BlockPos(20, 2, 20); h.setBlock(pos.below(), Blocks.STONE);
        for (int metal = 0; metal < 3; metal++) for (int layer = 1; layer <= 8; layer++) {
            var state = IafTreasure.PILES.get(metal).defaultBlockState().setValue(MetalPileBlock.LAYERS, layer);
            h.assertTrue(Math.abs(state.getShape(h.getLevel(), h.absolutePos(pos)).max(Direction.Axis.Y) - layer / 8.0) < 1e-6, "Original visible height");
            var collision = state.getCollisionShape(h.getLevel(), h.absolutePos(pos));
            h.assertTrue(layer == 1 ? collision.isEmpty() : Math.abs(collision.max(Direction.Axis.Y) - (layer - 1) / 8.0) < 1e-6, "Original collision is one layer lower");
            var expectedNugget = IafTreasure.NUGGETS.get(metal);
            var drops = Block.getDrops(state, h.getLevel(), h.absolutePos(pos), null, null, ItemStack.EMPTY);
            h.assertTrue(drops.stream().allMatch(s -> s.is(expectedNugget)) && drops.stream().mapToInt(ItemStack::getCount).sum() == 2 * layer, "Exact native nugget loot: material=" + metal + " layer=" + layer + " actual=" + drops);
        }
        var p = player(h); h.setBlock(pos, IafTreasure.GOLD.defaultBlockState().setValue(MetalPileBlock.LAYERS, 8));
        var rules = h.getLevel().getGameRules(); boolean old = rules.get(GameRules.BLOCK_DROPS);
        try { rules.set(GameRules.BLOCK_DROPS, true, h.getLevel().getServer()); h.assertTrue(p.gameMode.destroyBlock(h.absolutePos(pos)), "Actual survival breaking succeeds"); }
        finally { rules.set(GameRules.BLOCK_DROPS, old, h.getLevel().getServer()); }
        int count = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(pos)).inflate(2)).stream()
                .filter(e -> e.getItem().is(Items.GOLD_NUGGET)).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertTrue(count == 16 && h.getBlockState(pos).isAir(), "Actual eight-layer break returns sixteen nuggets without duplication"); done(h, p);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void supportPathAndOriginalSoundRulesArePreserved(GameTestHelper h) {
        var pos = new BlockPos(20, 2, 20); var state = IafTreasure.GOLD.defaultBlockState();
        for (var block : List.of(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BARRIER)) { h.setBlock(pos.below(), block); h.assertTrue(!state.canSurvive(h.getLevel(), h.absolutePos(pos)), "Original forbidden support"); }
        for (var block : List.of(Blocks.STONE, Blocks.HONEY_BLOCK, Blocks.SOUL_SAND, Blocks.BLUE_ICE)) { h.setBlock(pos.below(), block); h.assertTrue(state.canSurvive(h.getLevel(), h.absolutePos(pos)), "Original allowed support, including blue ice"); }
        h.setBlock(pos.below().below(), Blocks.STONE); h.setBlock(pos.below(), IafTreasure.SILVER.defaultBlockState().setValue(MetalPileBlock.LAYERS, 8));
        h.assertTrue(state.canSurvive(h.getLevel(), h.absolutePos(pos)), "Full pile of another material supports pile");
        h.setBlock(pos, state); h.setBlock(pos.below(), IafTreasure.SILVER.defaultBlockState().setValue(MetalPileBlock.LAYERS, 7));
        h.assertTrue(h.getBlockState(pos).isAir(), "Actual neighbor update removes unsupported upper pile");
        for (int n = 1; n <= 8; n++) {
            var s = state.setValue(MetalPileBlock.LAYERS, n);
            h.assertTrue(s.isPathfindable(PathComputationType.LAND) == (n < 5) && !s.isPathfindable(PathComputationType.WATER) && !s.isPathfindable(PathComputationType.AIR), "Original path thresholds");
        }
        h.assertTrue(state.getSoundType().getStepSound() == IafTreasure.STEP && state.getSoundType().getBreakSound() == IafTreasure.BREAK, "Original custom sound events"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void actualSurvivalAndCreativeStackingUsesNativePlacementAndConsumesExactly(GameTestHelper h) {
        var p = player(h); var pos = new BlockPos(20, 2, 20); h.setBlock(pos.below(), Blocks.STONE); h.setBlock(pos, IafTreasure.GOLD);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafTreasure.GOLD, 10));
        for (int n = 2; n <= 8; n++) { h.assertTrue(use(h, p, pos).consumesAction(), "Actual native stack interaction"); h.assertTrue(h.getBlockState(pos).getValue(MetalPileBlock.LAYERS) == n, "One layer per use"); }
        h.assertTrue(p.getMainHandItem().getCount() == 3, "Survival consumes exactly seven piles");
        h.assertTrue(use(h, p, pos).consumesAction() && h.getBlockState(pos).getValue(MetalPileBlock.LAYERS) == 8
                && h.getBlockState(pos.above()).is(IafTreasure.GOLD) && p.getMainHandItem().getCount() == 2, "Full pile stays eight; vanilla placement starts supported pile above");
        p.setGameMode(GameType.CREATIVE); h.setBlock(pos, IafTreasure.GOLD.defaultBlockState().setValue(MetalPileBlock.LAYERS, 2));
        use(h, p, pos); h.assertTrue(h.getBlockState(pos).getValue(MetalPileBlock.LAYERS) == 3 && p.getMainHandItem().getCount() == 2, "Creative stack consumes nothing"); done(h, p);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void allThreeRecipesConsumeExactlyTwoMatchingNuggetsInRealResultSlot(GameTestHelper h) {
        var p = player(h); var pos = h.absolutePos(new BlockPos(20, 2, 20)); h.getLevel().setBlockAndUpdate(pos, Blocks.CRAFTING_TABLE.defaultBlockState());
        for (int material = 0; material < 3; material++) {
            var menu = new CraftingMenu(10, p.getInventory(), ContainerLevelAccess.create(h.getLevel(), pos));
            menu.getInputGridSlots().get(0).set(new ItemStack(IafTreasure.NUGGETS.get(material)));
            menu.getInputGridSlots().get(1).set(new ItemStack(IafTreasure.NUGGETS.get(material)));
            menu.slotsChanged(menu.getInputGridSlots().getFirst().container);
            var item = IafTreasure.PILES.get(material).asItem(); h.assertTrue(menu.getResultSlot().getItem().is(item), "Actual shapeless recipe selected");
            int before = p.getInventory().countItem(item); menu.quickMoveStack(p, 0);
            h.assertTrue(p.getInventory().countItem(item) == before + 1 && menu.getInputGridSlots().stream().allMatch(s -> s.getItem().isEmpty()), "Native crafting consumes two nuggets and transfers one pile");
        }
        var wrong = new CraftingMenu(11, p.getInventory(), ContainerLevelAccess.create(h.getLevel(), pos));
        wrong.getInputGridSlots().get(0).set(new ItemStack(Items.GOLD_NUGGET)); wrong.getInputGridSlots().get(1).set(new ItemStack(IafTreasure.SILVER_NUGGET));
        wrong.slotsChanged(wrong.getInputGridSlots().getFirst().container); h.assertTrue(wrong.getResultSlot().getItem().isEmpty(), "Mixed metals cannot craft a pile"); done(h, p);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void allThreePileBreaksTriggerOriginalWildDragonAlarmButStackingDoesNot(GameTestHelper h) {
        var p = player(h); var pos = new BlockPos(20, 2, 20); h.setBlock(pos.below(), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED); d.setPos(h.absoluteVec(new Vec3(25.5, 2, 20.5))); h.getLevel().addFreshEntity(d);
        for (var pile : IafTreasure.PILES) {
            d.setTarget(null); d.setDragonSleeping(true); d.setOrderedToSit(true); h.setBlock(pos, pile);
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(pile, 2)); use(h, p, pos);
            h.assertTrue(d.getTarget() == null && d.dragonSleeping(), "Adding treasure does not mimic opening a chest");
            h.assertTrue(p.gameMode.destroyBlock(h.absolutePos(pos)) && d.getTarget() == p && !d.dragonSleeping() && !d.isOrderedToSit(), "Native break of each original metal pile triggers alarm");
        }
        done(h, p);
    }
}
