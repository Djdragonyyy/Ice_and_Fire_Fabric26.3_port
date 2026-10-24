package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Vegetation subset of EntityDragonBase.breakBlocks/isBreakable (LGPL-3.0). */
final class DragonVegetation {
    static boolean clearApproach(FireDragon dragon, LivingEntity target) {
        if (!(dragon.level() instanceof ServerLevel level) || dragon.stage() < 3
                || !dragon.canMoveOnGround() || !DragonFireTerrain.allowed(level, dragon)) return false;
        Vec3 direction = target.position().subtract(dragon.position()).multiply(1, 0, 1).normalize();
        var body = dragon.getBoundingBox();
        var sweep = body.minmax(body.move(direction.scale(2))).inflate(0.25, 0, 0.25);
        float hardness = DragonCombatConfig.current.dragonGriefing == 1 || dragon.stage() <= 3 ? 2 : 5;
        boolean changed = false;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(sweep.minX, body.minY + 0.01, sweep.minZ),
                BlockPos.containing(sweep.maxX, body.maxY + 1, sweep.maxZ))) {
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if ((!state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES)) || state.is(DragonFireTerrain.BLACKLIST)
                    || state.hasBlockEntity() || !state.getFluidState().isEmpty()) continue;
            float strength = state.getDestroySpeed(level, pos);
            if (strength < 0 || strength > hardness) continue;
            boolean drops = !state.is(BlockTags.LEAVES)
                    && dragon.getRandom().nextFloat() < DragonCombatConfig.current.dragonBlockBreakingDropChance;
            changed |= level.destroyBlock(pos, drops, dragon);
        }
        return changed;
    }
    private DragonVegetation() {}
}
