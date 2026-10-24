package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.IafTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.EnumSet;

/** Courtship, egg and nest from DragonAIMate (LGPL-3.0), with protected terrain and pair validation. */
final class DragonMateGoal extends Goal {
    private final FireDragon dragon;
    private FireDragon mate;
    private int courtship;
    private long nextScan;
    DragonMateGoal(FireDragon dragon) {
        this.dragon = dragon;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean canUse() {
        if (!dragon.readyToMate() || dragon.level().getGameTime() < nextScan) return false;
        nextScan = dragon.level().getGameTime() + 20;
        mate = dragon.level().getEntitiesOfClass(FireDragon.class, dragon.getBoundingBox().inflate(180), dragon::canMate)
                .stream().min(Comparator.comparingDouble(dragon::distanceToSqr)).orElse(null);
        return mate != null;
    }
    @Override public boolean canContinueToUse() { return mate != null && dragon.canMate(mate) && dragon.distanceTo(mate) <= 180; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { courtship = 0; }
    @Override public void stop() { mate = null; courtship = 0; dragon.getNavigation().stop(); }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        dragon.getLookControl().setLookAt(mate, 10, dragon.getMaxHeadXRot());
        dragon.getNavigation().moveTo(mate, 1);
        if (++courtship >= 60 && dragon.distanceTo(mate) < 35 && dragon.hasLineOfSight(mate)) {
            if (!spawnEgg()) courtship = 40;
        }
    }
    private boolean spawnEgg() {
        if (!(dragon.level() instanceof ServerLevel server) || !dragon.canMate(mate)) return false;
        FireDragon female = dragon.male() ? mate : dragon;
        BlockPos center = female.blockPosition().below().offset(-1, 0, -1);
        if (!server.hasChunkAt(center) || !server.getWorldBorder().isWithinBounds(center)) return false;
        var egg = IafContent.DRAGON_EGG.create(server, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
        if (egg == null) return false;
        egg.setVariant(dragon.getRandom().nextInt(4));
        // Bred eggs have no owner in the original; collecting and placing them assigns one.
        Vec3 point = Vec3.atBottomCenterOf(center.above());
        egg.setPos(point);
        if (!server.noCollision(egg, egg.getBoundingBox()) || !server.addFreshEntity(egg)) return false;
        dragon.finishBreeding();
        mate.finishBreeding();
        if (DragonFireTerrain.allowed(server, female)) {
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
                if (!server.hasChunkAt(pos) || !server.getWorldBorder().isWithinBounds(pos)) continue;
                var state = server.getBlockState(pos);
                float hardness = state.getDestroySpeed(server, pos);
                if (state.hasBlockEntity() || state.is(DragonFireTerrain.BLACKLIST) || !state.getFluidState().isEmpty()
                        || hardness < 0 || hardness >= 5) continue;
                server.setBlockAndUpdate(pos, IafTerrain.NEST.defaultBlockState());
            }
            if (server.getBlockState(center).is(IafTerrain.NEST)) server.setBlockAndUpdate(center, Blocks.DIRT_PATH.defaultBlockState());
        }
        server.sendParticles(ParticleTypes.HEART, dragon.getX(), dragon.getY() + 0.5, dragon.getZ(),
                17, dragon.getBbWidth() * 0.5, dragon.getBbHeight() * 0.5, dragon.getBbWidth() * 0.5, 0.02);
        if (server.getGameRules().get(GameRules.MOB_DROPS)) ExperienceOrb.award(server, dragon.position(), dragon.getRandom().nextInt(15) + 10);
        return true;
    }
}
