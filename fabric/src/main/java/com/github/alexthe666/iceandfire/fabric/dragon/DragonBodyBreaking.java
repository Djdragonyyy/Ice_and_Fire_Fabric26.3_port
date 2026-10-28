package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Ordinary body clearance from EntityDragonBase.breakBlocks(false) (LGPL-3.0). */
final class DragonBodyBreaking {
    static final TagKey<Block> NO_DROPS = TagKey.create(Registries.BLOCK, IafContent.id("dragon_block_break_no_drops"));
    private int remaining;

    void tick(FireDragon dragon, ServerLevel level) {
        int cooldown = DragonCombatConfig.current.dragonBreakBlockCooldown;
        if (cooldown == 0) { remaining = 0; clearBody(dragon, level); return; }
        if (remaining <= 0) remaining = cooldown;
        if (remaining > 0) remaining--;
        if (remaining == 0) clearBody(dragon, level);
    }

    static boolean breakable(FireDragon dragon, ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.hasChunkAt(pos) || !state.isSolid() || state.isAir() || !state.getFluidState().isEmpty()
                || state.getShape(level, pos).isEmpty() || state.is(DragonFireTerrain.BLACKLIST)
                || state.getBlock().getExplosionResistance() >= 1200) return false;
        float hardness = state.getDestroySpeed(level, pos);
        float limit = DragonCombatConfig.current.dragonGriefing == 1 || dragon.stage() <= 3 ? 2 : 5;
        return hardness >= 0 && hardness <= limit;
    }

    static void clearBody(FireDragon dragon, ServerLevel level) {
        if (dragon.stage() < 3 || dragon.corpse() || !dragon.isAlive() || dragon.isNoAi()
                || !(dragon.canMoveAutonomously() || dragon.getControllingPassenger() != null)
                || !DragonFireTerrain.allowed(level, dragon)) return;
        var box = dragon.getBoundingBox();
        int down = 1;
        var path = dragon.getNavigation().getPath();
        if (path != null && path.getNodeCount() > 0
                && path.getEntityPosAtNode(dragon, Math.min(path.getNodeCount() - 1, path.getNextNodeIndex() + 1)).y < dragon.getY() - 1) down = -1;
        int top = Mth.floor(box.maxY) + (dragon.flying() && dragon.getTarget() != null ? 0 : 2);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(Mth.floor(box.minX) - 1, Mth.floor(box.minY) + down, Mth.floor(box.minZ) - 1),
                new BlockPos(Mth.floor(box.maxX) + 1, top, Mth.floor(box.maxZ) + 1))) {
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if (!breakable(dragon, level, pos, state)) continue;
            boolean drops = !state.is(NO_DROPS) && dragon.getRandom().nextFloat() <= DragonCombatConfig.current.dragonBlockBreakingDropChance;
            if (level.destroyBlock(pos, drops, dragon)) dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.6F, 1, 0.6F));
        }
    }
}
