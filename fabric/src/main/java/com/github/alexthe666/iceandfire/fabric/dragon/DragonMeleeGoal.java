package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Ground pursuit from DragonAIAttackMelee (LGPL-3.0); damage follows the bite animation. */
final class DragonMeleeGoal extends Goal {
    private final FireDragon dragon;
    private int recalculate;
    private int vegetationDelay;

    DragonMeleeGoal(FireDragon dragon) {
        this.dragon = dragon;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean canUse() {
        return dragon.canMoveOnGround() && dragon.getTarget() != null && dragon.canAttack(dragon.getTarget());
    }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { recalculate = vegetationDelay = 0; }
    @Override public void tick() {
        var target = dragon.getTarget();
        if (target == null) return;
        dragon.getLookControl().setLookAt(target, 30, 30);
        if (dragon.withinBiteReach(target) && dragon.hasLineOfSight(target)) {
            dragon.getNavigation().stop();
        } else if (--recalculate <= 0) {
            recalculate = 4 + dragon.getRandom().nextInt(7);
            if (!dragon.getNavigation().moveTo(target, 1.5)) {
                // Vanilla cannot plan through a tree that the original dragon navigator marks breakable.
                // Approach locally while the bounded vegetation sweep opens a normal ground path.
                dragon.getMoveControl().setWantedPosition(target.getX(), dragon.getY(), target.getZ(), 1.5);
            }
        }
        if ((!dragon.withinBiteReach(target) || !dragon.hasLineOfSight(target)) && --vegetationDelay <= 0) {
            vegetationDelay = Math.max(1, com.github.alexthe666.iceandfire.fabric.DragonCombatConfig.current.dragonBreakBlockCooldown);
            if (DragonVegetation.clearApproach(dragon, target)) recalculate = 0;
        }
    }
    @Override public void stop() { dragon.getNavigation().stop(); }
}
