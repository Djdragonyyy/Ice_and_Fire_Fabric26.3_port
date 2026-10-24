package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Ground pursuit from DragonAIAttackMelee (LGPL-3.0); damage follows the bite animation. */
final class DragonMeleeGoal extends Goal {
    private final FireDragon dragon;
    private int recalculate;

    DragonMeleeGoal(FireDragon dragon) {
        this.dragon = dragon;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean canUse() {
        return dragon.canMoveOnGround() && dragon.getTarget() != null && dragon.canAttack(dragon.getTarget());
    }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { recalculate = 0; }
    @Override public void tick() {
        var target = dragon.getTarget();
        if (target == null) return;
        dragon.getLookControl().setLookAt(target, 30, 30);
        if (dragon.withinBiteReach(target) && dragon.hasLineOfSight(target)) {
            dragon.getNavigation().stop();
        } else if (--recalculate <= 0) {
            recalculate = 4 + dragon.getRandom().nextInt(7);
            dragon.getNavigation().moveTo(target, 1.5);
        }
    }
    @Override public void stop() { dragon.getNavigation().stop(); }
}
