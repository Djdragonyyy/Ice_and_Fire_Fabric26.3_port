package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

/** Fire conversions and charge destruction from IafDragonDestructionManager (LGPL-3.0). */
public final class DragonFireTerrain {
    public static final TagKey<Block> BLACKLIST = TagKey.create(Registries.BLOCK,
            net.minecraft.resources.Identifier.fromNamespaceAndPath(IceAndFireFabric.MOD_ID, "dragon_block_break_blacklist"));
    private DragonFireTerrain() {}
    public static boolean allowed(ServerLevel level, FireDragon dragon) {
        return level.getGameRules().get(GameRules.MOB_GRIEFING) && DragonCombatConfig.current.dragonGriefing < 2
                && (!dragon.isTame() || DragonCombatConfig.current.tamedDragonGriefing);
    }
    private static boolean breakable(ServerLevel level, BlockPos pos, BlockState state) {
        return level.hasChunkAt(pos) && state.getDestroySpeed(level, pos) >= 0
                && state.getBlock().getExplosionResistance() < 1200 && !state.is(BLACKLIST);
    }
    public static BlockState transformed(BlockState state) {
        Block result = null;
        if (state.getBlock() instanceof SpreadingSnowyBlock) result = IafTerrain.GRASS;
        else if (state.is(Blocks.DIRT)) result = IafTerrain.DIRT;
        else if (state.is(Blocks.GRAVEL)) result = IafTerrain.GRAVEL;
        else if (state.is(Blocks.COBBLESTONE)) result = IafTerrain.COBBLESTONE;
        else if (state.is(BlockTags.BASE_STONE_OVERWORLD)) result = IafTerrain.STONE;
        else if (state.is(Blocks.DIRT_PATH)) result = IafTerrain.PATH;
        else if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) return IafTerrain.ASH.defaultBlockState();
        else if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS) || state.is(Blocks.SNOW)) return Blocks.AIR.defaultBlockState();
        return result == null ? state : result.defaultBlockState().setValue(IafTerrain.REVERT, true);
    }
    private static void burn(ServerLevel level, BlockPos pos, FireDragon dragon) {
        if (!level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!breakable(level, pos, state)) return;
        BlockState burnt = transformed(state);
        if (burnt != state) level.setBlockAndUpdate(pos, burnt);
        BlockPos above = pos.above();
        if (dragon.getRandom().nextBoolean() && state.canOcclude() && burnt.canOcclude()
                && level.hasChunkAt(above) && level.isEmptyBlock(above)) {
            var fire = BaseFireBlock.getState(level, above);
            if (fire.canSurvive(level, above)) level.setBlockAndUpdate(above, fire);
        }
    }
    public static void breath(ServerLevel level, BlockPos center, FireDragon dragon) {
        if (!allowed(level, dragon)) return;
        int radius = dragon.stage() <= 3 ? 1 : dragon.stage() == 4 ? 2 : 3;
        float area = radius * 0.999F + 0.5F;
        float square = area * area;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            double distance = center.distSqr(pos);
            if (dragon.stage() <= 3 ? dragon.getRandom().nextBoolean() : distance <= square && dragon.getRandom().nextFloat() > distance / square)
                burn(level, pos, dragon);
        }
    }
    public static void charge(ServerLevel level, BlockPos center, FireDragon dragon) {
        if (!allowed(level, dragon)) return;
        int stage = dragon.stage();
        int base = stage <= 4 ? 2 : 3;
        int x = base + (stage > 3 ? dragon.getRandom().nextInt(2) : 0);
        int y = base + (stage > 3 ? dragon.getRandom().nextInt(2) : 0);
        int z = base + (stage > 3 ? dragon.getRandom().nextInt(2) : 0);
        float f = (x + y + z) * 0.333F + 0.5F;
        float square = f * f;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-x, -y, -z), center.offset(x, y, z))) {
            if (!level.hasChunkAt(pos)) continue;
            BlockState state = level.getBlockState(pos);
            double dist = center.distSqr(pos);
            boolean destroy = stage <= 3 ? dragon.getRandom().nextFloat() * 3 > dist
                    : dist <= square && dragon.getRandom().nextFloat() * 3 > dist / square;
            if (destroy && breakable(level, pos, state)) level.destroyBlock(pos, false);
            if (stage <= 3 && dragon.getRandom().nextBoolean()) burn(level, pos, dragon);
        }
        if (stage > 3) for (BlockPos pos : BlockPos.betweenClosed(center.offset(-x - 1, -y - 1, -z - 1), center.offset(x + 1, y + 1, z + 1))) {
            if (center.distSqr(pos) <= square) burn(level, pos, dragon);
        }
    }
}
