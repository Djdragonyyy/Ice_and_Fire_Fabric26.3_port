package com.github.alexthe666.iceandfire.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import java.util.ArrayList;
import java.util.List;

/** Fire terrain from IafBlockRegistry/BlockReturningState/BlockCharedPath (LGPL-3.0).
 * Keep the original registry spelling "chared" for saved blocks and assets.
 */
public final class IafTerrain {
    public static final BooleanProperty REVERT = BooleanProperty.create("revert");
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final Block DIRT = returning("chared_dirt", Blocks.DIRT, 0.5F, 0);
    public static final Block GRASS = returning("chared_grass", Blocks.GRASS_BLOCK, 0.6F, 0);
    public static final Block STONE = returning("chared_stone", Blocks.STONE, 1.5F, 10);
    public static final Block COBBLESTONE = returning("chared_cobblestone", Blocks.COBBLESTONE, 2, 10);
    public static final Block GRAVEL = register("chared_gravel", new ReturningFalling(properties("chared_gravel", Blocks.GRAVEL, 0.6F, 0), Blocks.GRAVEL));
    public static final Block PATH = register("chared_dirt_path", new ReturningPath(properties("chared_dirt_path", Blocks.DIRT_PATH, 0.6F, 0.6F)));
    public static final Block ASH = register("ash", new ReturningFalling(properties("ash", Blocks.SAND, 0.5F, 0), null));
    public static final Block NEST = register("nest", new Block(BlockBehaviour.Properties.of()
            .mapColor(net.minecraft.world.level.material.MapColor.PLANT).sound(SoundType.GRAVEL)
            .strength(0.5F, 0).friction(0.98F).pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)
            .setId(ResourceKey.create(Registries.BLOCK, id("nest")))));
    private IafTerrain() {}
    public static void initialize() {}
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath(IceAndFireFabric.MOD_ID, name); }

    private static BlockBehaviour.Properties properties(String name, Block original, float strength, float resistance) {
        return BlockBehaviour.Properties.ofFullCopy(original).strength(strength, resistance).randomTicks()
                .setId(ResourceKey.create(Registries.BLOCK, id(name)));
    }
    private static Block returning(String name, Block original, float strength, float resistance) {
        return register(name, new ReturningBlock(properties(name, original, strength, resistance), original));
    }
    private static Block register(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(block,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name))))));
        return block;
    }
    private static void revert(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Block original) {
        if (original != null && state.getValue(REVERT) && random.nextInt(3) == 0)
            level.setBlockAndUpdate(pos, original.defaultBlockState());
    }
    static final class ReturningBlock extends Block {
        private final Block original;
        ReturningBlock(Properties properties, Block original) {
            super(properties); this.original = original;
            registerDefaultState(stateDefinition.any().setValue(REVERT, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(REVERT); }
        @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { revert(state, level, pos, random, original); }
    }
    static final class ReturningFalling extends FallingBlock {
        private final Block original;
        ReturningFalling(Properties properties, Block original) {
            super(properties); this.original = original;
            registerDefaultState(stateDefinition.any().setValue(REVERT, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(REVERT); }
        @Override public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) { return 0xff777777; }
        @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { revert(state, level, pos, random, original); }
    }
    static final class ReturningPath extends PathBlock {
        ReturningPath(Properties properties) {
            super(DIRT, properties);
            registerDefaultState(stateDefinition.any().setValue(REVERT, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(REVERT); }
        @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { revert(state, level, pos, random, Blocks.DIRT_PATH); }
    }
}
