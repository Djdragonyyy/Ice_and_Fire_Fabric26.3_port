package com.github.alexthe666.iceandfire.fabric.dragon;

import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Bounded local detours for autonomous flight; not an arbitrary-maze pathfinder. */
final class DragonFlightPath {
    private final FireDragon dragon;
    private final ArrayDeque<Vec3> points = new ArrayDeque<>();
    private Vec3 goal;
    private long expires;
    private int checkedAt;

    DragonFlightPath(FireDragon dragon) { this.dragon = dragon; }
    void reset() { points.clear(); goal = null; expires = 0; }
    boolean detouring() { return points.size() > 1; }
    boolean intermediate() { return points.size() == 1 && goal != null && goal.distanceToSqr(points.getLast()) > 4; }

    boolean reaches(Vec3 destination) {
        if (intermediate() && dragon.position().distanceToSqr(points.getLast()) < 4) reset();
        if (goal != null && goal.distanceToSqr(destination) < 4 && !points.isEmpty()
                && (dragon.tickCount - checkedAt < 5 || recheck())) {
            // Keep a valid detour instead of restarting its climb every twenty ticks.
            if (dragon.level().getGameTime() >= expires) {
                expires = dragon.level().getGameTime() + 20;
                if (dragon.position().distanceToSqr(destination) <= 64 * 64
                        && DragonFlightClearance.clear(dragon, dragon.position(), destination)) {
                    goal = destination; points.clear(); points.add(destination);
                }
            }
            return true;
        }
        reset();
        List<Vec3> route = find(destination);
        if (route == null) return false;
        goal = destination; expires = dragon.level().getGameTime() + 20; checkedAt = dragon.tickCount; points.addAll(route);
        return true;
    }

    private boolean recheck() {
        checkedAt = dragon.tickCount;
        return DragonFlightClearance.clear(dragon, dragon.position(), points.getFirst());
    }

    Vec3 next(Vec3 destination) {
        if (!reaches(destination)) return null;
        while (points.size() > 1 && dragon.position().distanceToSqr(points.getFirst()) < 1.21) {
            points.removeFirst();
            if (!DragonFlightClearance.clear(dragon, dragon.position(), points.getFirst())) {
                reset(); return reaches(destination) ? points.getFirst() : null;
            }
        }
        return points.getFirst();
    }

    private List<Vec3> find(Vec3 destination) {
        Vec3 start = dragon.position();
        if (!Double.isFinite(destination.x) || !Double.isFinite(destination.y) || !Double.isFinite(destination.z)) return null;
        if (destination.y > dragon.flightManager.maxHeight()) return null;
        // Preserve long escort/home intents without an unbounded swept-volume query.
        if (start.distanceToSqr(destination) > 64 * 64)
            destination = start.add(destination.subtract(start).normalize().scale(32));
        if (DragonFlightClearance.clear(dragon, start, destination)) return List.of(destination);
        for (int rise : new int[]{4, 8, 12, 16, 24, 32}) {
            double y = Math.max(start.y, destination.y) + rise;
            if (y > dragon.flightManager.maxHeight()) continue;
            Vec3 aboveStart = new Vec3(start.x, y, start.z), aboveEnd = new Vec3(destination.x, y, destination.z);
            List<Vec3> candidate = List.of(aboveStart, aboveEnd, destination);
            if (clearRoute(start, candidate)) return candidate;
        }
        Vec3 horizontal = destination.subtract(start).multiply(1, 0, 1);
        Vec3 side = horizontal.horizontalDistanceSqr() < 1.0E-6 ? new Vec3(1, 0, 0)
                : new Vec3(-horizontal.z, 0, horizontal.x).normalize();
        double width = Math.max(4, dragon.getBbWidth() * 1.5);
        for (int multiple : new int[]{1, 2, 3, 4}) for (int sign : new int[]{1, -1}) {
            Vec3 offset = side.scale(width * multiple * sign);
            List<Vec3> candidate = List.of(start.add(offset), destination.add(offset), destination);
            if (clearRoute(start, candidate)) return candidate;
        }
        return null;
    }

    private boolean clearRoute(Vec3 start, List<Vec3> route) {
        for (Vec3 end : route) {
            if (!DragonFlightClearance.clear(dragon, start, end)) return false;
            start = end;
        }
        return true;
    }
}
