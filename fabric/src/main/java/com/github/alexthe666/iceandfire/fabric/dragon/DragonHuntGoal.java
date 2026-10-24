package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** DragonAITarget/DragonAITargetNonTamed and original prey tags (LGPL-3.0). */
final class DragonHuntGoal extends NearestAttackableTargetGoal<LivingEntity> {
    static final TagKey<net.minecraft.world.entity.EntityType<?>> PREY = TagKey.create(Registries.ENTITY_TYPE, IafContent.id("fire_dragon_targets"));
    private final FireDragon dragon;
    private final boolean wild;

    DragonHuntGoal(FireDragon dragon, boolean wild) {
        super(dragon, LivingEntity.class, wild ? 5 : 3, !wild, false, (candidate, level) ->
                candidate(dragon, candidate, wild)
                        && (!wild || candidate instanceof Player || dragon.getRandom().nextInt(100) > dragon.hunger()));
        this.dragon = dragon;
        this.wild = wild;
    }

    private boolean ready() {
        return dragon.canMoveAutonomously() && dragon.command() == 0 && !dragon.isInLove()
                && !dragon.flightManager.wantsRoost() && (!wild || !dragon.isTame());
    }

    @Override public boolean canUse() {
        return ready() && dragon.getTarget() == null && super.canUse() && !dragon.hasPriorityFood();
    }

    @Override public boolean canContinueToUse() {
        return ready() && dragon.getTarget() != null && candidate(dragon, dragon.getTarget(), wild) && super.canContinueToUse();
    }

    @Override protected AABB getTargetSearchArea(double range) { return dragon.getBoundingBox().inflate(range); }

    static boolean candidate(FireDragon dragon, LivingEntity target, boolean wild) {
        if (!dragon.canAttack(target) || target instanceof FireDragon) return false;
        if (wild) {
            return !dragon.isTame() && (target instanceof Player || target instanceof AbstractVillager || target.getType().builtInRegistryHolder().is(PREY));
        }
        double size = Math.max(dragon.getBbWidth(), dragon.getBbWidth() * DragonGrowth.renderSize(dragon.ageDays()));
        if (target.getBbWidth() > size) return false;
        if (target instanceof Player) return !dragon.isTame();
        if (dragon.hunger() >= 90 || !(target instanceof AgeableMob) || Math.round(target.getBbWidth() * target.getBbHeight() * 10) <= 0) return false;
        if (dragon.isTame() && (target instanceof AbstractVillager || target instanceof AbstractGolem
                || target instanceof TamableAnimal pet && pet.isTame())) return false;
        return target.getType().builtInRegistryHolder().is(PREY) || !dragon.isTame() && target instanceof AbstractVillager;
    }
}
