package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Ground branch of DragonAIReturnToRoost; the manager handles the air branch. */
final class DragonReturnToRoostGoal extends Goal {
    private final FireDragon dragon;
    private int recalculate;
    DragonReturnToRoostGoal(FireDragon dragon) {
        this.dragon = dragon;
        setFlags(EnumSet.of(Flag.MOVE));
    }
    @Override public boolean canUse() { return dragon.canMoveOnGround() && dragon.flightManager.wantsRoost(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public void start() { recalculate = 0; }
    @Override public void tick() {
        if (--recalculate <= 0) {
            recalculate = 10;
            var home = dragon.getHomePosition();
            dragon.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1);
        }
    }
    @Override public void stop() { dragon.getNavigation().stop(); }
}
