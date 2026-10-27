package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import java.util.*;

public class DragonForgeCraftingGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static CraftingMenu menu(GameTestHelper h, ServerPlayer player, ItemStack... grid) {
        var pos = h.absolutePos(new BlockPos(2, 2, 2)); h.getLevel().setBlockAndUpdate(pos, Blocks.CRAFTING_TABLE.defaultBlockState());
        var menu = new CraftingMenu(10, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), pos));
        require(grid.length == 9, "Native3x3 grid");
        for (int i = 0; i < 9; i++) menu.getInputGridSlots().get(i).set(grid[i]);
        menu.slotsChanged(menu.getInputGridSlots().getFirst().container); return menu;
    }
    private static ItemStack[] filled(Item item, int count) {
        var grid = new ItemStack[9]; Arrays.setAll(grid, i -> new ItemStack(item, count)); return grid;
    }
    private static void take(CraftingMenu menu, ServerPlayer player, Item result, int count) {
        require(menu.getResultSlot().getItem().is(result) && menu.getResultSlot().getItem().getCount() == count, "Actual crafting preview and count");
        int before = player.getInventory().countItem(result); var taken = menu.quickMoveStack(player, 0);
        require(!taken.isEmpty() && player.getInventory().countItem(result) == before + count, "Native result transfer exact count");
        require(menu.getInputGridSlots().stream().allMatch(s -> s.getItem().isEmpty()), "Native result slot consumed exactly all ingredients");
    }
    @GameTest
    public void fourScaleColorsCompressAndDecompressThroughNativeResultSlots(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel();
        for (int i = 0; i < 4; i++) {
            var block = IafDragonScales.ITEMS.get(i);
            take(menu(h, p, filled(IafContent.FIRE_SCALES[i], 1)), p, block, 1);
            var grid = new ItemStack[9]; Arrays.fill(grid, ItemStack.EMPTY);
            for (int slot = 0; slot < p.getInventory().getContainerSize(); slot++)
                if (p.getInventory().getItem(slot).is(block)) { grid[4] = p.getInventory().removeItem(slot, 1); break; }
            require(!grid[4].isEmpty() && p.getInventory().countItem(block) == 0, "Decompression uses the actual compressed output");
            take(menu(h, p, grid), p, IafContent.FIRE_SCALES[i], 9);
            var missing = filled(IafContent.FIRE_SCALES[i], 1); missing[8] = ItemStack.EMPTY;
            require(menu(h, p, missing).getResultSlot().getItem().isEmpty(), "Eight scales cannot compress");
        }
        h.succeed();
    }
    @GameTest
    public void originalForgeConstructionCostsUseTheRealCraftingMenu(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel();
        take(menu(h, p, filled(IafContent.DRAGON_BONE, 1)), p, IafDragonForge.BONE.asItem(), 1);
        ItemStack[] brick = new ItemStack[9];
        for (int i = 0; i < 9; i++) brick[i] = new ItemStack(i % 2 == 0 ? IafDragonScales.ITEMS.get(i % 4) : Items.STONE_BRICKS);
        take(menu(h, p, brick), p, IafDragonForge.BRICK.asItem(), 4);
        var core = filled(IafDragonForge.BRICK.asItem(), 1); core[4] = new ItemStack(IafContent.FIRE_DRAGON_HEART);
        take(menu(h, p, core), p, IafDragonForge.CORE.asItem(), 1);
        ItemStack[] input = new ItemStack[9];
        for (int i = 0; i < 9; i++) input[i] = i == 4 ? ItemStack.EMPTY : new ItemStack(i % 2 == 0 ? IafDragonForge.BRICK.asItem() : Items.IRON_INGOT);
        take(menu(h, p, input), p, IafDragonForge.INPUT.asItem(), 1);
        core = filled(IafDragonForge.BRICK.asItem(), 1); core[4] = new ItemStack(Items.ROTTEN_FLESH);
        require(menu(h, p, core).getResultSlot().getItem().isEmpty(), "A heart cannot be substituted by flesh");
        h.succeed();
    }
    @GameTest
    public void scaleBlocksKeepOriginalProtectionMiningAndMaterialProperties(GameTestHelper h) {
        var protectedTag = TagKey.create(Registries.BLOCK, IafContent.id("dragon_block_break_blacklist"));
        var storage = TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "storage_blocks/scales/dragon/fire"));
        var allStorage = TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "storage_blocks"));
        require(new ItemStack(IafDragonForge.BONE).is(allStorage), "Original bone block belongs to common storage blocks");
        for (var block : IafDragonScales.BLOCKS) {
            var state = block.defaultBlockState();
            require(state.is(protectedTag) && state.requiresCorrectToolForDrops(), "Dragon-proof and correct-tool mining");
            require(new ItemStack(block).is(storage) && block.getExplosionResistance() == 500, "Fire storage tag and original resistance");
            require(new ItemStack(block).is(allStorage), "Fire storage subtype participates in the original common parent tag");
            require(state.getDestroySpeed(h.getLevel(), h.absolutePos(BlockPos.ZERO)) == 30, "Original mining strength");
            var drops = Block.getDrops(state, h.getLevel(), h.absolutePos(new BlockPos(2, 2, 2)), null, null, new ItemStack(Items.DIAMOND_PICKAXE));
            require(drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1, "Native loot returns exactly the original scale block");
        }
        h.succeed();
    }
    @GameTest(maxTicks = 100)
    public void nativeInventoryChangesUnlockTheConstructionRecipeBook(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel();
        var bone = ResourceKey.create(Registries.RECIPE, IafContent.id("dragon_bone_block"));
        require(!p.getRecipeBook().contains(bone), "Fresh player's recipe is not pre-unlocked");
        p.getInventory().add(new ItemStack(IafContent.DRAGON_BONE));
        p.getInventory().add(new ItemStack(Items.STONE_BRICKS));
        p.getInventory().add(new ItemStack(IafDragonForge.BRICK));
        for (int i = 0; i < 4; i++) {
            p.getInventory().add(new ItemStack(IafContent.FIRE_SCALES[i]));
            p.getInventory().add(new ItemStack(IafDragonScales.ITEMS.get(i)));
        }
        h.runAfterDelay(10, () -> {
            List<String> ids = new ArrayList<>(List.of("dragon_bone_block", "dragonforge_fire_brick", "dragonforge_fire_core", "dragonforge_fire_input"));
            for (String c : IafDragonScales.COLORS) {
                ids.add("dragonscales_" + c + "_to_dragonscale_" + c); ids.add("dragonscale_" + c + "_to_dragonscales_" + c);
            }
            for (String id : ids) require(p.getRecipeBook().contains(ResourceKey.create(Registries.RECIPE, IafContent.id(id))), "Native inventory criteria unlock " + id);
            h.succeed();
        });
    }
}
