package com.github.alexthe666.iceandfire.fabric.forge;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Original two-ingredient DragonForgeRecipe adapted to native recipe codecs. */
public record DragonForgeRecipe(Ingredient input, Ingredient blood, ItemStackTemplate result, int cookTime, String dragonType)
        implements Recipe<DragonForgeRecipe.Input> {
    public static final MapCodec<DragonForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("input").forGetter(DragonForgeRecipe::input),
            Ingredient.CODEC.fieldOf("blood").forGetter(DragonForgeRecipe::blood),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(DragonForgeRecipe::result),
            Codec.intRange(1, 1000000).fieldOf("cook_time").forGetter(DragonForgeRecipe::cookTime),
            Codec.STRING.fieldOf("dragon_type").forGetter(DragonForgeRecipe::dragonType)).apply(i, DragonForgeRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonForgeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::input, Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::blood,
            ItemStackTemplate.STREAM_CODEC, DragonForgeRecipe::result, ByteBufCodecs.VAR_INT, DragonForgeRecipe::cookTime,
            ByteBufCodecs.STRING_UTF8, DragonForgeRecipe::dragonType, DragonForgeRecipe::new);
    public record Input(ItemStack material, ItemStack blood) implements RecipeInput {
        @Override public ItemStack getItem(int index) { return index == 0 ? material : index == 1 ? blood : ItemStack.EMPTY; }
        @Override public int size() { return 2; }
    }
    @Override public boolean matches(Input inv, Level level) { return dragonType.equals("fire") && input.test(inv.material) && blood.test(inv.blood); }
    @Override public ItemStack assemble(Input inv) { return result.create(); }
    @Override public boolean isSpecial() { return true; }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ""; }
    @Override public RecipeSerializer<DragonForgeRecipe> getSerializer() { return IafDragonForge.SERIALIZER; }
    @Override public RecipeType<DragonForgeRecipe> getType() { return IafDragonForge.RECIPE_TYPE; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeBookCategory recipeBookCategory() { return IafDragonForge.CATEGORY; }
}
