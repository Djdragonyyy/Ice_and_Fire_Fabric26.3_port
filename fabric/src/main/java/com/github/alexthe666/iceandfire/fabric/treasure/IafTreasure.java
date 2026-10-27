package com.github.alexthe666.iceandfire.fabric.treasure;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import java.util.List;

/** Original treasure piles and the silver nugget needed by their loot/crafting; no ore generation. */
public final class IafTreasure {
    private static SoundEvent sound(String name) {
        var id = IafContent.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
    public static final SoundEvent STEP = sound("gold_pile_step"), BREAK = sound("gold_pile_break");
    public static final SoundType SOUNDS = new SoundType(1, 1, BREAK, STEP, BREAK, STEP, STEP);
    public static final Item SILVER_NUGGET = Registry.register(BuiltInRegistries.ITEM, IafContent.id("silver_nugget"),
            new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, IafContent.id("silver_nugget")))));
    private static MetalPileBlock pile(String metal) {
        var id = IafContent.id(metal + "_pile");
        var block = Registry.register(BuiltInRegistries.BLOCK, id, new MetalPileBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT).strength(0.3F, 1).randomTicks().sound(SOUNDS).setId(ResourceKey.create(Registries.BLOCK, id))));
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
        return block;
    }
    public static final MetalPileBlock GOLD = pile("gold"), SILVER = pile("silver"), COPPER = pile("copper");
    public static final List<MetalPileBlock> PILES = List.of(GOLD, SILVER, COPPER);
    public static final List<Item> NUGGETS = List.of(Items.GOLD_NUGGET, SILVER_NUGGET, Items.COPPER_NUGGET);
    public static boolean isPile(Block block) { return PILES.contains(block); }
    public static void initialize() {}
    private IafTreasure() {}
}
