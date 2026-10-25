package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Original fire flesh: eight nutrition, .8 saturation modifier, five seconds of fire. */
public final class FireDragonFleshItem extends Item {
    public FireDragonFleshItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()));
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (level instanceof ServerLevel) consumer.igniteForSeconds(5);
        return super.finishUsingItem(stack, level, consumer);
    }
}
