package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.IafTerrain;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Original three-slot forge, assembly and flame timings using native block-entity persistence. */
public final class DragonForgeCore extends BaseContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int cookTime, flameTicks;
    private long poweredAt = Long.MIN_VALUE;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { int n = index < 2 ? cookTime : maxCookTime(); return (index & 1) == 0 ? n & 65535 : n >>> 16; }
        @Override public void set(int index, int value) { }
        @Override public int getCount() { return 4; }
    };
    public DragonForgeCore(BlockPos pos, BlockState state) { super(IafDragonForge.CORE_TYPE, pos, state); }
    @Override public int getContainerSize() { return 3; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> value) { items = value; }
    @Override protected Component getDefaultName() { return Component.translatable("container.dragonforge_fire"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory player) { return new DragonForgeMenu(id, player, this, data); }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); items = NonNullList.withSize(3, ItemStack.EMPTY); ContainerHelper.loadAllItems(input, items);
        cookTime = Math.clamp(input.getIntOr("CookTime", 0), 0, 1000000); flameTicks = 0;
    }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); ContainerHelper.saveAllItems(output, items); output.putInt("CookTime", cookTime); }
    @Override public void setItem(int index, ItemStack stack) {
        if (index < 2 && !ItemStack.isSameItemSameComponents(getItem(index), stack)) cookTime = 0;
        super.setItem(index, stack);
    }
    public int cookTime() { return cookTime; }
    public boolean heated() { return flameTicks > 0; }
    public int maxCookTime() { var recipe = recipe(); return recipe == null ? 100 : recipe.cookTime(); }
    private DragonForgeRecipe recipe() {
        if (!(level instanceof ServerLevel server)) return null;
        return server.getServer().getRecipeManager().getRecipeFor(IafDragonForge.RECIPE_TYPE,
                new DragonForgeRecipe.Input(getItem(0), getItem(1)), server).map(h -> h.value()).orElse(null);
    }
    private ItemStack result() { var recipe = recipe(); return recipe == null ? new ItemStack(IafTerrain.ASH) : recipe.result().create(); }
    public boolean canSmelt() {
        if (getItem(0).isEmpty()) return false;
        ItemStack result = result(), output = getItem(2);
        return !result.isEmpty() && (output.isEmpty() || ItemStack.isSameItemSameComponents(output, result))
                && result.getCount() + output.getCount() <= Math.min(getMaxStackSize(), result.getMaxStackSize());
    }
    public boolean assembled() {
        if (level == null) return false;
        BlockPos p = getBlockPos();
        for (int dy : new int[]{-1, 1}) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var at = p.offset(x, dy, z);
            if (!level.hasChunkAt(at) || !level.getBlockState(at).is(x != 0 && z != 0 ? IafDragonForge.BONE : IafDragonForge.BRICK)) return false;
        }
        for (int x : new int[]{-1, 1}) for (int z : new int[]{-1, 1}) if (!level.getBlockState(p.offset(x, 0, z)).is(IafDragonForge.BRICK)) return false;
        int sides = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) if (level.getBlockState(p.relative(dir)).is(IafDragonForge.BRICK)) sides++;
        return sides >= 3;
    }
    public void receiveFlame() {
        if (!(level instanceof ServerLevel) || !assembled() || !canSmelt()) return;
        if (poweredAt != level.getGameTime()) { cookTime = Math.min(maxCookTime() + 1, cookTime + 1); poweredAt = level.getGameTime(); }
        flameTicks = 40; setChanged();
    }
    public static void tick(Level level, BlockPos pos, BlockState state, DragonForgeCore core) {
        if (!(level instanceof ServerLevel)) return;
        if (core.flameTicks > 0) core.flameTicks--;
        boolean assembled = core.assembled();
        if (state.getValue(IafDragonForge.ACTIVE) != assembled) level.setBlockAndUpdate(pos, state.setValue(IafDragonForge.ACTIVE, assembled));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos at = pos.relative(side); BlockState adjacent = level.getBlockState(at);
            if (adjacent.is(IafDragonForge.BRICK) && adjacent.getValue(IafDragonForge.GRILL) != assembled)
                level.setBlockAndUpdate(at, adjacent.setValue(IafDragonForge.GRILL, assembled));
            if (adjacent.is(IafDragonForge.INPUT) && adjacent.getValue(IafDragonForge.ACTIVE) != (assembled && core.flameTicks > 0)) {
                level.setBlockAndUpdate(at, adjacent.setValue(IafDragonForge.ACTIVE, assembled && core.flameTicks > 0));
                if (assembled && core.flameTicks > 0) level.scheduleTick(at, IafDragonForge.INPUT, 40);
            }
        }
        if (!assembled) return;
        int previous = core.cookTime;
        if (core.cookTime > 0 && core.canSmelt() && core.flameTicks == 0) core.cookTime--;
        if (!core.canSmelt()) core.cookTime = 0;
        else if (core.cookTime > 0 && ++core.cookTime >= core.maxCookTime()) {
            ItemStack result = core.result();
            if (core.getItem(2).isEmpty()) core.items.set(2, result); else core.getItem(2).grow(result.getCount());
            core.getItem(0).shrink(1); core.getItem(1).shrink(1); core.cookTime = 0; core.setChanged();
        }
        if (previous != core.cookTime) core.setChanged();
    }
    @Override public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) return true;
        if (index != 1 || !(level instanceof ServerLevel server)) return false;
        return server.getServer().getRecipeManager().getRecipes().stream().anyMatch(holder -> holder.value() instanceof DragonForgeRecipe recipe
                && recipe.dragonType().equals("fire") && recipe.blood().test(stack));
    }
    @Override public int[] getSlotsForFace(Direction side) { return side == Direction.DOWN ? new int[]{2} : new int[]{0, 1}; }
    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction side) { return canPlaceItem(index, stack); }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        return side != Direction.DOWN || index != 1 || stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET);
    }
}
