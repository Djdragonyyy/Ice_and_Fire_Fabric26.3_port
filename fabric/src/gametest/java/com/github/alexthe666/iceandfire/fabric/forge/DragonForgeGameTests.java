package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.*;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class DragonForgeGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static DragonForgeCore structure(GameTestHelper h) {
        var base = new BlockPos(24, 3, 24);
        for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var block = y == 0 && x == 0 && z == 0 ? IafDragonForge.CORE
                    : y != 0 && x != 0 && z != 0 ? IafDragonForge.BONE : IafDragonForge.BRICK;
            h.setBlock(base.offset(x, y, z), block);
        }
        h.setBlock(base.north(), IafDragonForge.INPUT);
        var core = (DragonForgeCore) h.getLevel().getBlockEntity(h.absolutePos(base));
        require(core != null && core.assembled(), "Native block entity and original3x3x3 structure");
        return core;
    }
    static void ingredients(DragonForgeCore core, int count) {
        core.setItem(0, new ItemStack(Items.IRON_INGOT, count)); core.setItem(1, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, count));
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void actualRecipeAssemblyPowerDeduplicationAndInventoryRules(GameTestHelper h) {
        var core = structure(h); ingredients(core, 2);
        require(core.maxCookTime() == 1000 && core.canSmelt(), "Original iron/blood recipe decoded from actual datapack and tag");
        var recipe = h.getLevel().getServer().getRecipeManager().getRecipeFor(IafDragonForge.RECIPE_TYPE,
                new DragonForgeRecipe.Input(core.getItem(0), core.getItem(1)), h.getLevel()).orElseThrow().value();
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            DragonForgeRecipe.STREAM_CODEC.encode(buffer, recipe);
            var received = DragonForgeRecipe.STREAM_CODEC.decode(buffer);
            require(received.cookTime() == 1000 && received.dragonType().equals("fire")
                    && received.matches(new DragonForgeRecipe.Input(core.getItem(0), core.getItem(1)), h.getLevel())
                    && received.result().create().is(IafDragonForge.INGOT), "Native registry-aware recipe stream roundtrip retains both ingredients, type, duration and result");
        } finally { buffer.release(); }
        core.receiveFlame(); core.receiveFlame();
        require(core.cookTime() == 1, "Multiple impacts in one server tick cannot add multiple power increments");
        require(java.util.Arrays.equals(core.getSlotsForFace(Direction.UP), new int[]{0, 1})
                && java.util.Arrays.equals(core.getSlotsForFace(Direction.DOWN), new int[]{2})
                && java.util.Arrays.equals(core.getSlotsForFace(Direction.NORTH), new int[]{0, 1}), "Original sided automation layout");
        core.setItem(2, new ItemStack(IafDragonForge.INGOT, 64));
        require(!core.canSmelt(), "Full output blocks processing");
        core.setItem(2, new ItemStack(Items.GOLD_INGOT)); require(!core.canSmelt(), "Incompatible output blocks processing");
        core.setItem(2, ItemStack.EMPTY); h.setBlock(new BlockPos(23, 2, 23), Blocks.STONE);
        require(!core.assembled(), "Missing bone corner invalidates structure");
        int before = core.cookTime(); core.receiveFlame(); require(core.cookTime() == before, "Broken structure cannot gain power");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12, maxTicks = 150)
    public void originalAshFallbackAndUnpoweredProgressDoNotCreateFreeProducts(GameTestHelper h) {
        var core = structure(h); core.setItem(0, new ItemStack(Items.COBBLESTONE));
        core.setItem(1, new ItemStack(Items.GLASS_BOTTLE));
        require(core.maxCookTime() == 100 && core.canSmelt(), "Original unmatched material fallback cooks ash in100");
        core.receiveFlame();
        for (int i = 0; i < 140; i++) DragonForgeCore.tick(h.getLevel(), core.getBlockPos(), core.getBlockState(), core);
        require(core.cookTime() == 40 && core.getItem(2).isEmpty(), "After40 heat ticks progress stops without producing a free result");
        h.onEachTick(() -> {
            if (!core.getItem(2).isEmpty()) {
                require(core.getItem(2).is(IafTerrain.ASH.asItem()) && core.getItem(2).getCount() == 1
                        && core.getItem(0).isEmpty() && core.getItem(1).isEmpty(), "Original fallback consumes both slots exactly once");
                h.succeed(); return;
            }
            IafDragonForge.feedInput(h.getLevel(), core.getBlockPos().north());
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void saveReloadRestoresInventoryAndProgressWithoutDuplicatingDrops(GameTestHelper h) {
        var core = structure(h); ingredients(core, 3); core.receiveFlame();
        var pos = core.getBlockPos(); var state = core.getBlockState();
        var data = core.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored = (DragonForgeCore) BlockEntity.loadStatic(pos, state, data, h.getLevel().registryAccess());
        require(restored != null && restored.getItem(0).getCount() == 3 && restored.getItem(1).getCount() == 3
                && restored.cookTime() == 1, "Native persisted stacks and progress");
        restored.setLevel(h.getLevel());
        DragonForgeCore.tick(h.getLevel(), pos, state, core);
        require(h.getLevel().getBlockEntity(pos) == core && core.getItem(0).getCount() == 3, "Changing assembled state preserves actual block entity and inventory");
        require(h.getLevel().getBlockState(pos).getLightEmission() == 15
                && h.getLevel().getBlockState(pos.north()).getValue(IafDragonForge.ACTIVE)
                && h.getLevel().getBlockState(pos.south()).getValue(IafDragonForge.GRILL), "Original assembled light and active aperture/grills before removal");
        var area = new net.minecraft.world.phys.AABB(pos).inflate(4);
        h.getLevel().destroyBlock(pos, false);
        require(!h.getLevel().getBlockState(pos.north()).getValue(IafDragonForge.ACTIVE)
                && !h.getLevel().getBlockState(pos.south()).getValue(IafDragonForge.GRILL), "Native neighbour updates deactivate aperture and grills when core is removed");
        int material = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).stream()
                .filter(e -> e.getItem().is(Items.IRON_INGOT)).mapToInt(e -> e.getItem().getCount()).sum();
        int blood = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).stream()
                .filter(e -> e.getItem().is(IafContent.FIRE_DRAGON_BLOOD)).mapToInt(e -> e.getItem().getCount()).sum();
        require(material == 3 && blood == 3, "Native removal drops contents exactly once");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12, maxTicks = 60)
    public void scheduledApertureCheckClearsHeatWhenCoreDisappearsWithoutNeighbourNotification(GameTestHelper h) {
        var core = structure(h); ingredients(core, 1); core.receiveFlame(); var pos = core.getBlockPos();
        DragonForgeCore.tick(h.getLevel(), pos, core.getBlockState(), core);
        require(h.getLevel().getBlockState(pos.north()).getValue(IafDragonForge.ACTIVE), "Actual heated aperture starts active");
        h.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        require(h.getLevel().getBlockState(pos.north()).getValue(IafDragonForge.ACTIVE), "Fixture deliberately omits neighbour notifications");
        h.runAtTickTime(45, () -> {
            require(!h.getLevel().getBlockState(pos.north()).getValue(IafDragonForge.ACTIVE), "Scheduled native callback deactivates an orphan aperture");
            h.succeed();
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void hopperAccessThroughBricksAndShiftClickUseTheActualCore(GameTestHelper h) {
        var core = structure(h); var level = h.getLevel(); var pos = core.getBlockPos();
        require(HopperBlockEntity.getContainerAt(level, pos.above()) == core
                && HopperBlockEntity.getContainerAt(level, pos.south()) == core, "Native hopper finds inventory through top and side bricks");
        var remainder = HopperBlockEntity.addItem(null, core, new ItemStack(Items.IRON_INGOT, 2), Direction.UP);
        require(remainder.isEmpty() && core.getItem(0).getCount() == 2, "Top insertion reaches material slot");
        remainder = HopperBlockEntity.addItem(null, core, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, 2), Direction.NORTH);
        require(remainder.isEmpty() && core.getItem(1).getCount() == 2, "Side insertion reaches blood slot");
        core.setItem(1, ItemStack.EMPTY);
        remainder = HopperBlockEntity.addItem(null, core, new ItemStack(Items.COBBLESTONE), Direction.NORTH);
        require(!remainder.isEmpty() && core.getItem(1).isEmpty(), "Original recipe blood predicate rejects unrelated side input into an empty slot");
        core.setItem(1, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, 2));
        var player = h.makeMockServerPlayerInLevel(); player.setPos(Vec3.atCenterOf(pos));
        var menu = core.createMenu(1, player.getInventory(), player);
        require(menu.stillValid(player), "Actual core menu validates nearby player");
        core.setItem(2, new ItemStack(IafDragonForge.INGOT));
        require(menu.quickMoveStack(player, 2).is(IafDragonForge.INGOT) && core.getItem(2).isEmpty(), "Shift click removes actual output");
        require(player.getInventory().countItem(IafDragonForge.INGOT) == 1, "Shift click deposits exact result in actual player inventory");
        player.setPos(Vec3.atCenterOf(pos).add(20, 0, 0)); require(!menu.stillValid(player), "Distant menu access rejected");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12, maxTicks = 1100)
    public void actualDragonBreathProducesOneIngotWhileGriefingIsDisabled(GameTestHelper h) {
        var core = structure(h); ingredients(core, 1);
        var level = h.getLevel();
        var d = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        d.setNoAi(true); d.setNoGravity(true); d.setPos(h.absoluteVec(new Vec3(24.5, 2, 10.5))); d.setYRot(0); d.yBodyRot = d.yHeadRot = 0;
        for (int i = 0; i < 25; i++) d.growOneDay(); level.addFreshEntity(d);
        var input = core.getBlockPos().north();
        h.onEachTick(() -> {
            if (core.getItem(2).is(IafDragonForge.INGOT)) {
                require(core.getItem(2).getCount() == 1 && core.getItem(0).isEmpty() && core.getItem(1).isEmpty(), "Exactly one original ingot with exact ingredient consumption");
                require(core.assembled() && level.getBlockState(input).is(IafDragonForge.INPUT), "Dragon flame preserves the forge structure");
                h.succeed(); return;
            }
            boolean old = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.getGameRules().set(GameRules.MOB_GRIEFING, false, level.getServer());
                DragonFireCombat.breath(level, d, d.headPosition(), Vec3.atCenterOf(input), 1);
            } finally { level.getGameRules().set(GameRules.MOB_GRIEFING, old, level.getServer()); }
        });
    }
}
