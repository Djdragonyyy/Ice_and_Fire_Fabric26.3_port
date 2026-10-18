package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonCombatGeometryTest {
    @Test void rayMissCannotBecomeAFalseTargetAtWorldOrigin() {
        var from = new Vec3(100, 70, -80);
        var end = from.add(0, 0, 30);
        assertTrue(DragonCombatGeometry.intersectionDistance(from, end, new AABB(105, 70, -60, 106, 72, -59)).isEmpty());
        assertTrue(DragonCombatGeometry.intersectionDistance(from, end, new AABB(99, 70, -40, 101, 72, -39)).isEmpty());
        assertEquals(400, DragonCombatGeometry.intersectionDistance(from, end, new AABB(99, 69, -60, 101, 72, -59)).orElseThrow());
        assertEquals(0, DragonCombatGeometry.intersectionDistance(from, end, new AABB(99, 69, -81, 101, 72, -79)).orElseThrow());
    }

    @Test void groundedHeadMatchesOriginalSizeRatiosAndRotatesWithHeading() {
        for (int age : new int[]{0, 25, 50, 75, 100, 125}) {
            float size = DragonGrowth.renderSize(age);
            for (float yaw : new float[]{0, 90, 180, 237}) {
                Vec3 head = DragonCombatGeometry.headOffset(size, yaw, 0, 0, 0);
                assertEquals(0.51 * size, Math.hypot(head.x, head.z), 0.00001);
                assertEquals(0.21 * size, head.y, 0.00001);
                assertEquals(-Math.sin(Math.toRadians(yaw)) * 0.51 * size, head.x, 0.00001);
            }
        }
    }

    @Test void headPositionStaysFiniteAndContinuousThroughFlightTransition() {
        for (float pitch : new float[]{-60, -30, 0, 20, 40}) {
            var previous = DragonCombatGeometry.headOffset(30, 237, pitch, 0.01F, 0);
            for (int i = 2; i <= 2000; i++) {
                var next = DragonCombatGeometry.headOffset(30, 237, pitch, i / 100F, 0);
                assertTrue(Double.isFinite(next.x + next.y + next.z));
                assertTrue(previous.distanceTo(next) < 0.01, "No jumps while flight pose blends");
                previous = next;
            }
        }
    }
}
