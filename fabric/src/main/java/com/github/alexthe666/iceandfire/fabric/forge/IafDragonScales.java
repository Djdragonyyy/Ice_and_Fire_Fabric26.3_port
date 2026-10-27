package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import java.util.*;
import java.util.function.Consumer;

/** Original fire scale storage blocks; dragon-proof protection is supplied by the block tag. */
public final class IafDragonScales {
    public static final String[] COLORS = {"red", "green", "bronze", "gray"};
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final List<Item> ITEMS = new ArrayList<>();
    static {
        ChatFormatting[] styles = {ChatFormatting.DARK_RED, ChatFormatting.DARK_GREEN, ChatFormatting.GOLD, ChatFormatting.GRAY};
        for (int i = 0; i < COLORS.length; i++) {
            String color = COLORS[i]; var style = styles[i]; var id = IafContent.id("dragonscale_" + color);
            var block = Registry.register(BuiltInRegistries.BLOCK, id, new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).dynamicShape()
                    .strength(30, 500).sound(SoundType.STONE).requiresCorrectToolForDrops()
                    .setId(ResourceKey.create(Registries.BLOCK, id))));
            BLOCKS.add(block);
            ITEMS.add(Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block,
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))) {
                @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                                       Consumer<Component> tooltip, TooltipFlag flag) {
                    super.appendHoverText(stack, context, display, tooltip, flag);
                    tooltip.accept(Component.translatable("dragon." + color).withStyle(style));
                }
            }));
        }
    }
    private IafDragonScales() {}
    public static void initialize() {}
}
