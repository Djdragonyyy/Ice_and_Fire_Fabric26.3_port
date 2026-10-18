package com.github.alexthe666.iceandfire.fabric.client;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FireDragonPoseModelTest {
    @Test void onlyFirstPersonPilotSeesAnUntiltedDragon() {
        for (float pitch : new float[]{-60, -30, 0, 20, 40}) {
            assertEquals(0, FireDragonRenderer.viewPitch(pitch, true));
            assertEquals(pitch, FireDragonRenderer.viewPitch(pitch, false));
        }
    }
    @Test void riderPlacementMatchesVanillaModelTransformAtEveryHeadingAndScale() {
        for (float size : new float[]{7F / 3F, 12.5F / 3F, 20F / 3F, 10F}) {
            for (float yaw : new float[]{0, 90, 180, 237}) {
                var state = new FireDragonRenderer.State();
                state.scale = size;
                state.bodyRot = yaw;
                state.x = 100; state.y = 70; state.z = -80;
                var anchor = new org.joml.Vector3f(0.3F, 0.7F, -0.5F);
                var matrix = new com.mojang.blaze3d.vertex.PoseStack();
                matrix.scale(size, size, size);
                matrix.mulPose(new org.joml.Matrix4f().rotation(com.mojang.math.Axis.YP.rotationDegrees(180 - yaw)));
                matrix.scale(-1, -1, 1);
                matrix.translate(0, -1.501F, 0);
                var expected = matrix.last().pose().transformPosition(new org.joml.Vector3f(anchor));
                var actual = FireDragonRenderer.riderRenderPosition(state, anchor);
                assertEquals(state.x + expected.x, actual.x, 0.00001);
                assertEquals(state.y + expected.y - 0.75, actual.y, 0.00001);
                assertEquals(state.z + expected.z, actual.z, 0.00001);
            }
        }
    }

    @Test void seatedHipTouchesRenderedNeckSurfaceAtEveryStageAndFlightPitch() {
        var model = model();
        for (int age : new int[]{50, 75, 100, 125}) {
            for (float pitch : new float[]{-60, 0, 40}) {
                for (float playerScale : new float[]{0.5F, 1, 2}) {
                    var state = new FireDragonRenderer.State();
                    state.scale = com.github.alexthe666.iceandfire.fabric.dragon.DragonGrowth.renderSize(age) / 3F;
                    state.bodyRot = 237;
                    state.flightPitch = pitch;
                    state.flying = pitch != 0;
                    state.flyProgress = state.flying ? 20 : 0;
                    state.flightCycle = 26;
                    state.ageInTicks = 10.25F;
                    model.setupAnim(state);
                    var matrix = new com.mojang.blaze3d.vertex.PoseStack();
                    matrix.scale(state.scale, state.scale, state.scale);
                    matrix.mulPose(new org.joml.Matrix4f().rotation(com.mojang.math.Axis.YP.rotationDegrees(180 - state.bodyRot)));
                    matrix.scale(-1, -1, 1);
                    matrix.mulPose(new org.joml.Matrix4f().rotation(com.mojang.math.Axis.XP.rotationDegrees(pitch)));
                    matrix.translate(0, -1.501F, 0);
                    var renderedSurface = new org.joml.Vector3f(Float.NaN);
                    model.root().visit(matrix, (pose, path, index, cube) -> {
                        if (!path.equals("/BodyUpper/Neck1")) return;
                        pose.pose().transformPosition(new org.joml.Vector3f(
                                (cube.minX + cube.maxX) / 32F, cube.minY / 16F, cube.maxZ / 16F), renderedSurface);
                    });
                    assertTrue(Float.isFinite(renderedSurface.y));
                    var rider = FireDragonRenderer.riderRenderPosition(state, model.riderAnchor(), playerScale);
                    assertEquals(renderedSurface.x, rider.x, 0.00001);
                    assertEquals(renderedSurface.y, rider.y + 0.75F * playerScale, 0.00001,
                            "Hip must stay on top of neck, including stage 5 at 125 days");
                    assertEquals(renderedSurface.z, rider.z, 0.00001);
                }
            }
        }
    }

    private static FireDragonPoseModel model() {
        return new FireDragonPoseModel(name -> {
            Path file = Path.of(System.getProperty("iceandfire.legacyResources"),
                    "assets/iceandfire/models/tabula/firedragon/firedragon_" + name + ".tbl");
            try (var input = Files.newInputStream(file)) { return TabulaLoader.parse(input); }
            catch (IOException error) { throw new UncheckedIOException(error); }
        });
    }

    @Test void cyclesInterpolateAtZeroAndMatchTwoUnitFlightRate() {
        assertEquals(0.5F, FireDragonPoseModel.animationCycle(0, 0.5F, true));
        assertEquals(1F, FireDragonPoseModel.animationCycle(0, 0.5F, false));
        assertEquals(59.8F, FireDragonPoseModel.animationCycle(58, 0.9F, false), 0.0001F);
        assertEquals(0F, FireDragonPoseModel.animationCycle(58, 1F, false));
    }

    @Test void everyWalkAndFlightBoundaryRemainsContinuous() {
        var model = model();
        for (boolean flying : new boolean[]{false, true}) {
            int period = flying ? 60 : 40;
            int rate = flying ? 2 : 1;
            for (int boundary = 0; boundary < period; boundary += 10) {
                var state = new FireDragonRenderer.State();
                state.male = true;
                state.flying = flying;
                state.flyProgress = flying ? 20 : 0;
                state.walkAnimationSpeed = 0.5F;
                state.partialTick = 0.9999F;
                state.walkCycle = Math.floorMod(boundary - 1, period);
                state.flightCycle = Math.floorMod(boundary - rate, period);
                model.setupAnim(state);
                var before = rotations(model);
                state.partialTick = 0.0001F;
                state.walkCycle = boundary;
                state.flightCycle = boundary;
                model.setupAnim(state);
                var after = rotations(model);
                for (int i = 0; i < before.length; i++) {
                    double difference = Math.atan2(Math.sin(after[i] - before[i]), Math.cos(after[i] - before[i]));
                    assertEquals(0, difference, 0.001, "boundary " + boundary + " flying=" + flying);
                }
            }
        }
    }

    @Test void animatedSeatFollowsNeckAndRepeatedFramesDoNotAccumulateOffsets() {
        var model = model();
        var state = new FireDragonRenderer.State();
        state.ageInTicks = 10.25F;
        model.setupAnim(state);
        var seated = model.riderAnchor();
        model.setupAnim(state);
        assertEquals(seated, model.riderAnchor());
        state.ageInTicks += 20;
        model.setupAnim(state);
        assertNotEquals(seated, model.riderAnchor(), "Seat must follow body animation");
    }

    @Test void flightBobFadesContinuouslyAtTakeoffAndLanding() {
        var model = model();
        for (float age : new float[]{10, 25, 40}) {
            var state = new FireDragonRenderer.State();
            state.ageInTicks = age;
            model.setupAnim(state);
            var grounded = model.riderAnchor();
            float groundedY = model.root().getChild("BodyUpper").y;
            state.flyProgress = 0.0001F;
            model.setupAnim(state);
            assertEquals(groundedY, model.root().getChild("BodyUpper").y, 0.001F,
                    "Flight bob must disappear gradually at landing");
            assertEquals(0, grounded.distance(model.riderAnchor()), 0.001F);
            state.flyProgress = 0;
            state.flying = true;
            model.setupAnim(state);
            assertEquals(groundedY, model.root().getChild("BodyUpper").y, 0.001F,
                    "Flight bob must start gradually at takeoff");
        }
    }

    private static float[] rotations(FireDragonPoseModel model) {
        var parts = model.root().getAllParts();
        float[] result = new float[parts.size() * 3];
        for (int i = 0; i < parts.size(); i++) {
            ModelPart p = parts.get(i);
            result[i * 3] = p.xRot; result[i * 3 + 1] = p.yRot; result[i * 3 + 2] = p.zRot;
        }
        return result;
    }
}
