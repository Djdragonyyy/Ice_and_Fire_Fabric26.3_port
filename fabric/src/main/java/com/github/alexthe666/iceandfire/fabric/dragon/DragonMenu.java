package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Five-slot original ContainerDragon layout, with owner validation on the server (LGPL-3.0). */
public final class DragonMenu extends AbstractContainerMenu {
    private final Container inventory;
    public final FireDragon dragon;
    public DragonMenu(int id, Inventory playerInventory, int entityId) {
        this(id, playerInventory, new SimpleContainer(5),
                playerInventory.player.level().getEntity(entityId) instanceof FireDragon fire ? fire : null);
    }
    DragonMenu(int id, Inventory playerInventory, Container inventory, FireDragon dragon) {
        super(IafContent.DRAGON_MENU, id);
        this.inventory = inventory; this.dragon = dragon;
        checkContainerSize(inventory, 5);
        inventory.startOpen(playerInventory.player);
        int[][] coordinates = {{8, 54}, {8, 18}, {8, 36}, {153, 18}, {153, 36}};
        for (int i = 0; i < 5; i++) {
            final int slot = i;
            addSlot(new Slot(inventory, slot, coordinates[i][0], coordinates[i][1]) {
                @Override public boolean mayPlace(ItemStack stack) { return DragonArmorItem.fits(stack, slot); }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 132 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 8 + col * 18, 190));
    }
    @Override public boolean stillValid(Player player) {
        return dragon != null && dragon.isAlive() && dragon.isOwnedBy(player) && dragon.distanceToSqr(player) < 64;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 5) {
            if (!moveItemStackTo(stack, 5, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < 5; i++) if (slots.get(i).mayPlace(stack) && !slots.get(i).hasItem()) {
                moved = moveItemStackTo(stack, i, i + 1, false); break;
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
    @Override public void removed(Player player) { super.removed(player); inventory.stopOpen(player); }
    public static void open(ServerPlayer player, FireDragon dragon) {
        if (!dragon.isAlive() || !dragon.isOwnedBy(player) || dragon.distanceToSqr(player) >= 64) return;
        player.openMenu(new ExtendedMenuProvider<Integer>() {
            @Override public Integer getScreenOpeningData(ServerPlayer ignored) { return dragon.getId(); }
            @Override public Component getDisplayName() { return dragon.getDisplayName(); }
            @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player ignored) {
                return new DragonMenu(id, inventory, dragon.dragonInventory, dragon);
            }
        });
    }
}
