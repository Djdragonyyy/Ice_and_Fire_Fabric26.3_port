package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DragonFlightSteeringTest {
    @Test void intermediateLegRetainsOriginalDistantCruiseAccelerationAndHeading() {
        for (Vec3 offset : new Vec3[]{new Vec3(0, 0, 32), new Vec3(8, 8, 8), new Vec3(-8, -8, 8)}) {
            var original = DragonFlightSteering.towards(offset.scale(20), 37, 1.2);
            var local = DragonFlightSteering.towardsWaypoint(offset, 37, 1.2, true);
            assertEquals(0, original.acceleration().distanceTo(local.acceleration()), 1.0E-9);
            assertEquals(original.yaw(), local.yaw()); assertEquals(original.speed(), local.speed());
        }
    }
    @Test void finalLegKeepsArrivalSlowdownAndCoincidentIntermediateRemainsFinite() {
        Vec3 offset = new Vec3(0, 0, 4);
        var last = DragonFlightSteering.towardsWaypoint(offset, 0, 1.2, false);
        assertEquals(DragonFlightSteering.towards(offset, 0, 1.2), last);
        assertTrue(DragonFlightSteering.towardsWaypoint(offset, 0, 1.2, true).acceleration().length() > last.acceleration().length());
        assertEquals(Vec3.ZERO, DragonFlightSteering.towardsWaypoint(Vec3.ZERO, 0, 0, true).acceleration());
    }
    @Test void steepTargetsMustNotStallHorizontalFlight() {
        var step = DragonFlightSteering.towards(new Vec3(8, 16, 0), -90, 1);
        assertTrue(step.acceleration().x > 0, "Steep wandering targets still require forward progress");
        assertTrue(step.acceleration().y > 0);
    }
    @Test void verticalAndCoincidentTargetsRemainFiniteAtZeroSpeed() {
        for (Vec3 target : new Vec3[]{Vec3.ZERO, new Vec3(0, 50, 0), new Vec3(0, -50, 0), new Vec3(1.0e-12, 20, 0)}) {
            var step = DragonFlightSteering.towards(target, 37, 0);
            assertTrue(Double.isFinite(step.acceleration().lengthSqr()));
            assertTrue(Double.isFinite(step.speed()));
            assertEquals(37, step.yaw());
            assertEquals(0, step.acceleration().x, 1.0e-10);
            assertEquals(0, step.acceleration().z, 1.0e-10);
            assertTrue(step.acceleration().y * target.y >= 0);
        }
    }
    @Test void steeringTurnsByAtMostFourDegreesAcrossYawWrap() {
        for (float yaw : new float[]{-181, -179, 0, 179, 181}) {
            for (Vec3 target : new Vec3[]{new Vec3(-20, 0, 1), new Vec3(20, 0, -1), new Vec3(0, 0, -20)}) {
                var step = DragonFlightSteering.towards(target, yaw, 1);
                assertTrue(Math.abs(Mth.wrapDegrees(step.yaw() - yaw)) <= 4.0001);
            }
        }
    }
    @Test void bothSignsOfAccelerationRespectTheOriginalCap() {
        for (int x : new int[]{-100, 100}) for (int y : new int[]{-40, 40}) for (int z : new int[]{-100, 100}) {
            var step = DragonFlightSteering.towards(new Vec3(x, y, z), 140, 1.8);
            assertTrue(Math.abs(step.acceleration().x) <= 0.2);
            assertTrue(Math.abs(step.acceleration().y) <= 0.2);
            assertTrue(Math.abs(step.acceleration().z) <= 0.2);
        }
    }
    @Test void alignedFlightMovesCloserUsingAccelerationAndDrag() {
        var position = Vec3.ZERO;
        var velocity = Vec3.ZERO;
        var target = new Vec3(0, 0, 50);
        float yaw = 0;
        double speed = 0.2;
        for (int tick = 0; tick < 60; tick++) {
            var step = DragonFlightSteering.towards(target.subtract(position), yaw, speed);
            speed = step.speed(); yaw = step.yaw();
            velocity = velocity.add(step.acceleration());
            position = position.add(velocity);
            velocity = velocity.scale(0.9);
        }
        assertTrue(position.distanceTo(target) < 35, "Flight must make real progress towards its target");
        assertEquals(0, position.x, 0.00001);
    }
    @Test void configuredAccelerationRemainsFiniteCappedAndHasNoCumulativeMultiplier() {
        var target = new Vec3(0, 0, 100);
        var normal = DragonFlightSteering.towards(target, 0, 0.2);
        var half = DragonFlightSteering.towards(target, 0, 0.2, 0.5);
        assertEquals(normal.acceleration().scale(0.5), half.acceleration());
        assertEquals(normal.speed(), half.speed());
        for (double modifier : new double[]{0, 0.5, 1, 2}) {
            double speed = 0;
            for (int tick = 0; tick < 200; tick++) {
                var step = DragonFlightSteering.towards(target, 0, speed, modifier);
                speed = step.speed();
                assertTrue(Double.isFinite(speed) && speed <= 1.8);
                assertTrue(step.acceleration().length() <= 0.20000001);
                if (modifier == 0) assertEquals(Vec3.ZERO, step.acceleration());
            }
        }
    }
}
