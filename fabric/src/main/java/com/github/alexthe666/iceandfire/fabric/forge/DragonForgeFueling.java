package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import java.util.*;

/** Original input lure with a bounded, transient reservation for one dragon/input. */
final class DragonForgeFueling {
    private final DragonForgeCore core;
    private UUID selected;
    private BlockPos input;
    private long nextScan;
    DragonForgeFueling(DragonForgeCore core) { this.core = core; }
    void release() { stopDragon(); input = null; }
    private void stopDragon() {
        if (selected != null && core.getLevel() instanceof ServerLevel level && level.getEntity(selected) instanceof FireDragon dragon)
            dragon.releaseForgeInput(input);
        selected = null;
    }
    void tick(boolean active) {
        if (!(core.getLevel() instanceof ServerLevel level)) return;
        BlockPos at = null;
        if (active) for (Direction side : Direction.Plane.HORIZONTAL) {
            var candidate = core.getBlockPos().relative(side);
            if (level.getBlockState(candidate).is(IafDragonForge.INPUT) && IafDragonForge.connected(level, candidate) == core) { at = candidate.immutable(); break; }
        }
        if (at == null) { release(); return; }
        if (!at.equals(input)) { release(); input = at; }
        if (selected != null) {
            if (level.getEntity(selected) instanceof FireDragon dragon && dragon.claimForgeInput(input)) return;
            stopDragon();
        }
        if (level.getGameTime() < nextScan) return;
        nextScan = level.getGameTime() + 10;
        Vec3 target = Vec3.atCenterOf(input);
        var candidates = new ArrayList<>(level.getEntitiesOfClass(FireDragon.class, new AABB(input).inflate(50), dragon -> dragon.canSupplyForgeAt(input)));
        candidates.sort(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(target)));
        for (var dragon : candidates) if (dragon.claimForgeInput(input)) { selected = dragon.getUUID(); break; }
    }
}
