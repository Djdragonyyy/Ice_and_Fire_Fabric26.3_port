package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

/** Original configured search budget, retaining native ground path evaluation. */
final class DragonGroundNavigation extends GroundPathNavigation {
    // The superclass constructor invokes createPathFinder; no initializer may erase its result.
    private PathFinder dragonFinder;

    DragonGroundNavigation(FireDragon dragon, Level level) { super(dragon, level); }

    private static int nodeBudget(float range) {
        // AbstractPathJob also limited visits to maxRange squared.
        return Math.max(1, (int) Math.min(DragonCombatConfig.current.maxDragonPathingNodes,
                Math.max(1, (double) range * range)));
    }

    @Override protected PathFinder createPathFinder(int nativeBudget) {
        dragonFinder = super.createPathFinder(nodeBudget(nativeBudget / 16F));
        return dragonFinder;
    }

    @Override public void updatePathfinderMaxVisitedNodes() {
        if (dragonFinder != null) dragonFinder.setMaxVisitedNodes(DragonCombatConfig.current.maxDragonPathingNodes);
    }

    @Override public void setMaxVisitedNodesMultiplier(float multiplier) {
        // Native retries may reduce the budget, but must not exceed the configured maximum.
        super.setMaxVisitedNodesMultiplier(Math.min(1, multiplier));
    }

    @Override protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean above,
                                        int accuracy, float range) {
        dragonFinder.setMaxVisitedNodes(nodeBudget(range));
        return super.createPath(targets, regionOffset, above, accuracy, range);
    }
}
