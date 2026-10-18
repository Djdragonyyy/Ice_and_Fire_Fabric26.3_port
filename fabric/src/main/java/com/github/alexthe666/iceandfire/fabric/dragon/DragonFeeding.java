package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** Fire dragon diet adapted from FoodUtils (LGPL-3.0); fire dragons are not piscivores. */
public final class DragonFeeding {
    private DragonFeeding() {}
    public static int foodPoints(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD);
        return food != null && stack.is(ItemTags.MEAT) ? food.nutrition() * 10 : 0;
    }
}
