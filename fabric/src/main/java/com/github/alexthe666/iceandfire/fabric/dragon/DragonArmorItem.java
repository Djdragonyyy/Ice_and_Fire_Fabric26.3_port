package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IceAndFireFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BannerItem;
import java.util.ArrayList;
import java.util.List;

/** ItemDragonArmor and original ordinal order/bonuses (LGPL-3.0). */
public final class DragonArmorItem extends Item {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final String[] MATERIALS = {"iron", "gold", "diamond", "silver", "dragonsteel_fire", "dragonsteel_ice", "copper", "dragonsteel_lightning"};
    public static final String[] PARTS = {"head", "neck", "body", "tail"};
    public final int ordinal;
    public final int slot;
    private DragonArmorItem(Properties properties, int ordinal, int slot) { super(properties); this.ordinal = ordinal; this.slot = slot; }
    public static void initialize() {
        if (!ITEMS.isEmpty()) return;
        for (int material = 0; material < MATERIALS.length; material++) for (int slot = 0; slot < 4; slot++) {
            String name = "dragonarmor_" + MATERIALS[material] + "_" + PARTS[slot];
            var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(IceAndFireFabric.MOD_ID, name));
            ITEMS.add(Registry.register(BuiltInRegistries.ITEM, key,
                    new DragonArmorItem(new Item.Properties().setId(key).stacksTo(1), material + 1, slot)));
        }
    }
    public static boolean fits(ItemStack stack, int inventorySlot) {
        return inventorySlot == 0 ? stack.getItem() instanceof BannerItem
                : stack.getItem() instanceof DragonArmorItem armor && armor.slot == inventorySlot - 1;
    }
    public static double bonus(int ordinal) {
        return switch (ordinal) { case 1 -> 2; case 2, 4 -> 3; case 3 -> 5; case 5, 6, 8 -> 10; case 7 -> 1.5; default -> 0; };
    }
    public static int ordinal(ItemStack stack) { return stack.getItem() instanceof DragonArmorItem armor ? armor.ordinal : 0; }
}
