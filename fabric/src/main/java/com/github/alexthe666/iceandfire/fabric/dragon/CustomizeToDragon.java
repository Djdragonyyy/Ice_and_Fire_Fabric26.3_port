package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import java.util.Optional;
import java.util.Set;

/** Fire portion of the original customize_to_dragon loot function; native26 codec. */
public final class CustomizeToDragon extends LootItemConditionalFunction {
    public static final MapCodec<CustomizeToDragon> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, CustomizeToDragon::new));
    private CustomizeToDragon(Optional<Holder<LootItemCondition>> condition) { super(condition); }
    @Override public MapCodec<CustomizeToDragon> codec() { return CODEC; }
    @Override public Set<ContextKey<?>> getReferencedContextParams() { return Set.of(LootContextParams.THIS_ENTITY); }
    @Override protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(context.getOptional(LootContextParams.THIS_ENTITY) instanceof FireDragon dragon)) return stack;
        int age = dragon.ageDays(); var random = dragon.getRandom();
        if (stack.is(IafContent.DRAGON_BONE) || stack.is(IafContent.FIRE_DRAGON_FLESH)) {
            stack.setCount(1 + random.nextInt(1 + age / 25)); return stack;
        }
        for (var scale : IafContent.FIRE_SCALES) if (stack.is(scale))
            return new ItemStack(IafContent.FIRE_SCALES[dragon.variant()], age / 25 + random.nextInt(1 + age / 5));
        for (var egg : IafContent.FIRE_EGGS) if (stack.is(egg)) {
            return dragon.stage() >= 4 ? new ItemStack(IafContent.FIRE_EGGS[dragon.variant()], stack.getCount())
                    : new ItemStack(IafContent.FIRE_SCALES[dragon.variant()], 1 + random.nextInt(1 + age / 5));
        }
        return stack;
    }
}
