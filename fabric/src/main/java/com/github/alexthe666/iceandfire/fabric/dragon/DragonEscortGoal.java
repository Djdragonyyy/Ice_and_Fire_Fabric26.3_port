package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Ground path of DragonAIEscort (LGPL-3.0). Autonomous flight is migrated separately. */
final class DragonEscortGoal extends Goal {
    private final FireDragon dragon;
    private int recalculate;
    DragonEscortGoal(FireDragon dragon) {
        this.dragon = dragon;
        setFlags(EnumSet.of(Flag.MOVE));
    }
    @Override public boolean canUse() {
        var owner = dragon.getOwner();
        return dragon.canMoveOnGround() && dragon.command() == 2 && dragon.getTarget() == null
                && owner != null && owner.isAlive() && owner.level() == dragon.level()
                && dragon.distanceTo(owner) <= 2000
                && dragon.distanceTo(owner) > dragon.getBoundingBox().getSize();
    }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public void start() { recalculate = 0; }
    @Override public void tick() {
        var owner = dragon.getOwner();
        if (owner != null && --recalculate <= 0) {
            recalculate = 10;
            dragon.getNavigation().moveTo(owner, 1);
        }
    }
    @Override public void stop() { dragon.getNavigation().stop(); }
}
