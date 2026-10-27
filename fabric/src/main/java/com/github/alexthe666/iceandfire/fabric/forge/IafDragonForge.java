package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import java.util.*;

/** Fire DragonForge blocks and native inventory access; original assets/structure, LGPL-3.0. */
public final class IafDragonForge {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active"), GRILL = BooleanProperty.create("grill");
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final Block BONE = register("dragon_bone_block", new RotatedPillarBlock(properties("dragon_bone_block")
            .sound(SoundType.WOOD).strength(30, 500).requiresCorrectToolForDrops()));
    public static final Block BRICK = register("dragonforge_fire_brick", new BrickBlock(properties("dragonforge_fire_brick")));
    public static final Block INPUT = register("dragonforge_fire_input", new InputBlock(properties("dragonforge_fire_input")));
    public static final Block CORE = register("dragonforge_fire_core", new CoreBlock(properties("dragonforge_fire_core")
            .mapColor(net.minecraft.world.level.material.MapColor.METAL).lightLevel(state -> state.getValue(ACTIVE) ? 15 : 0)));
    public static final Item INGOT = Registry.register(BuiltInRegistries.ITEM, IafContent.id("dragonsteel_fire_ingot"),
            new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, IafContent.id("dragonsteel_fire_ingot")))));
    public static final BlockEntityType<DragonForgeCore> CORE_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            IafContent.id("dragonforge_core"), new BlockEntityType<>(DragonForgeCore::new, Set.of(CORE)));
    public static final MenuType<DragonForgeMenu> MENU = Registry.register(BuiltInRegistries.MENU,
            IafContent.id("dragonforge"), new MenuType<>(DragonForgeMenu::new, FeatureFlags.VANILLA_SET));
    public static final RecipeType<DragonForgeRecipe> RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE,
            IafContent.id("dragonforge"), new RecipeType<>() { @Override public String toString() { return "iceandfire:dragonforge"; } });
    public static final RecipeSerializer<DragonForgeRecipe> SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
            IafContent.id("dragonforge"), new RecipeSerializer<>(DragonForgeRecipe.CODEC, DragonForgeRecipe.STREAM_CODEC));
    public static final RecipeBookCategory CATEGORY = Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY,
            IafContent.id("dragonforge"), new RecipeBookCategory());
    private IafDragonForge() {}
    public static void initialize() {}
    private static BlockBehaviour.Properties properties(String id) {
        return BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).strength(40, 500).sound(SoundType.METAL)
                .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                .setId(ResourceKey.create(Registries.BLOCK, IafContent.id(id)));
    }
    private static Block register(String id, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, IafContent.id(id), block);
        ITEMS.add(Registry.register(BuiltInRegistries.ITEM, IafContent.id(id), new BlockItem(block,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, IafContent.id(id))))));
        return block;
    }
    public static DragonForgeCore connected(LevelAccessor level, BlockPos pos) {
        if (level.getBlockState(pos).is(CORE) && level.getBlockEntity(pos) instanceof DragonForgeCore core) return core;
        for (Direction direction : Direction.values()) {
            var adjacent = pos.relative(direction);
            if (level.hasChunkAt(adjacent) && level.getBlockState(adjacent).is(CORE) && level.getBlockEntity(adjacent) instanceof DragonForgeCore core) return core;
        }
        return null;
    }
    public static void feedInput(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(INPUT)) return;
        var core = connected(level, pos);
        if (core != null) core.receiveFlame();
    }
    private static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        var core = connected(level, pos);
        if (core == null) return InteractionResult.PASS;
        if (player instanceof ServerPlayer server) server.openMenu(core);
        return InteractionResult.SUCCESS;
    }
    static class AccessBlock extends Block implements WorldlyContainerHolder {
        AccessBlock(Properties properties) { super(properties); }
        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player p, BlockHitResult hit) { return open(level, pos, p); }
        @Override public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) { return connected(level, pos); }
        @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block source,
                                                   net.minecraft.world.level.redstone.Orientation orientation, boolean piston) {
            if (!(level instanceof net.minecraft.server.level.ServerLevel)) return;
            var core = connected(level, pos);
            if (core == null || !core.assembled()) {
                var property = state.hasProperty(ACTIVE) ? ACTIVE : GRILL;
                if (state.getValue(property)) level.setBlockAndUpdate(pos, state.setValue(property, false));
            }
        }
    }
    static final class BrickBlock extends AccessBlock {
        BrickBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(GRILL, false)); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(GRILL); }
    }
    static final class InputBlock extends AccessBlock {
        InputBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(ACTIVE, false)); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(ACTIVE); }
        @Override protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
            var core = connected(level, pos);
            if (core == null || !core.assembled() || !core.heated()) level.setBlockAndUpdate(pos, state.setValue(ACTIVE, false));
            else level.scheduleTick(pos, this, 40);
        }
    }
    static final class CoreBlock extends BaseEntityBlock {
        CoreBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(ACTIVE, false)); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(ACTIVE); }
        @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DragonForgeCore(pos, state); }
        @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide() ? null : createTickerHelper(type, CORE_TYPE, DragonForgeCore::tick);
        }
        @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player p, BlockHitResult hit) { return open(level, pos, p); }
    }
}
