package com.github.alexthe666.iceandfire.fabric.treasure;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Original BlockGoldPile eight-layer shapes and support rules (LGPL-3.0). */
public final class MetalPileBlock extends Block {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
    private static final VoxelShape[] SHAPES = new VoxelShape[9];
    static {
        SHAPES[0] = Shapes.empty();
        for (int i = 1; i <= 8; i++) SHAPES[i] = Block.box(0, 0, 0, 16, 2 * i, 16);
    }
    MetalPileBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(LAYERS, 1)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LAYERS); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPES[state.getValue(LAYERS)]; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPES[state.getValue(LAYERS) - 1]; }
    @Override protected boolean useShapeForLightOcclusion(BlockState state) { return true; }
    @Override protected boolean isPathfindable(BlockState state, PathComputationType type) { return type == PathComputationType.LAND && state.getValue(LAYERS) < 5; }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var support = level.getBlockState(pos.below()); var block = support.getBlock();
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BARRIER) return false;
        return block == Blocks.HONEY_BLOCK || block == Blocks.SOUL_SAND
                || Block.isFaceFull(support.getCollisionShape(level, pos.below()), Direction.UP)
                || block instanceof MetalPileBlock && support.getValue(LAYERS) == 8;
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                                Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return state.canSurvive(level, pos) ? super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random)
                : Blocks.AIR.defaultBlockState();
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = context.getLevel().getBlockState(context.getClickedPos());
        return state.is(this) ? state.setValue(LAYERS, Math.min(8, state.getValue(LAYERS) + 1)) : super.getStateForPlacement(context);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                     Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(asItem()) && state.getValue(LAYERS) < 8) {
            level.setBlock(pos, state.setValue(LAYERS, state.getValue(LAYERS) + 1), 3);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
