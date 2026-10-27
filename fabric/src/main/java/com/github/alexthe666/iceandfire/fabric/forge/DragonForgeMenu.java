package com.github.alexthe666.iceandfire.fabric.forge;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Native synchronized original three-slot forge layout. */
public final class DragonForgeMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;
    public DragonForgeMenu(int id, Inventory player) { this(id, player, new SimpleContainer(3), new SimpleContainerData(4)); }
    DragonForgeMenu(int id, Inventory player, Container inventory, ContainerData data) {
        super(IafDragonForge.MENU, id); this.inventory = inventory; this.data = data;
        checkContainerSize(inventory, 3); checkContainerDataCount(data, 4); inventory.startOpen(player.player);
        addSlot(new Slot(inventory, 0, 68, 34)); addSlot(new Slot(inventory, 1, 86, 34));
        addSlot(new Slot(inventory, 2, 148, 35) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(player, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    public int progress() { return data.get(0) & 65535 | (data.get(1) & 65535) << 16; }
    public int duration() { return Math.max(1, data.get(2) & 65535 | (data.get(3) & 65535) << 16); }
    @Override public boolean stillValid(Player player) { return inventory.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); inventory.stopOpen(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 3) { if (!moveItemStackTo(stack, 3, slots.size(), true)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, stack.is(com.github.alexthe666.iceandfire.fabric.IafContent.FIRE_DRAGON_BLOOD) ? 1 : 0,
                stack.is(com.github.alexthe666.iceandfire.fabric.IafContent.FIRE_DRAGON_BLOOD) ? 2 : 1, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(player, stack); return original;
    }
}
