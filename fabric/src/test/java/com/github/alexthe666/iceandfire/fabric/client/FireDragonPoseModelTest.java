package com.github.alexthe666.iceandfire.fabric.client;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FireDragonPoseModelTest {
    @Test void idleChainsKeepOriginalLengthNormalizedPhaseAndBodyCounterBob() {
        var model = model(); var s = new FireDragonRenderer.State(); s.male = true;
        model.setupAnim(s);
        // Independent original Citadel reference at t=0, rootOffset=-2, chain length=4.
        float[] yaw = {0.125F, 0.08838835F, 0, -0.08838835F};
        float[] pitch = {0.075F, 0.05303301F, 0, -0.05303301F};
        for (int i = 0; i < 4; i++) {
            var p = find(model.root(), "Tail" + (i + 1)); var initial = p.getInitialPose();
            assertEquals(initial.yRot() + yaw[i], p.yRot, 0.00001F);
            assertEquals(initial.xRot() + pitch[i], p.xRot, 0.00001F);
        }
        for (String name : new String[]{"BodyUpper", "ThighL", "ThighR", "armL1", "armR1"}) {
            var p = find(model.root(), name);
            assertEquals(p.getInitialPose().y() + (name.equals("BodyUpper") ? -0.65F : 0.65F), p.y, 0.00001F,
                    "Original bob baseline and opposite leg/arm motion: " + name);
        }
        var neck = find(model.root(), "Neck1");
        assertEquals(neck.getInitialPose().xRot() - 0.05F, neck.xRot, 0.00001F);
    }

    @Test void flyingBodyNeckToesAndTailUseOriginalSecondaryMotion() {
        var model = model(); var s = new FireDragonRenderer.State();
        s.male = true; s.flying = true; s.flyProgress = 20; model.setupAnim(s);
        var frame = original("flight6");
        String[] names = {"BodyUpper", "BodyLower", "Tail1", "Tail4", "Neck1", "Neck2", "ToeL2", "ToeR2", "ThighR", "ThighL"};
        float[] delta = {0.05F, -0.05F, -0.05F, -0.05F, 0.1F, 0, 0.05F, 0.05F, 0.05F, -0.05F};
        for (int i = 0; i < names.length; i++)
            assertEquals(frame.parts().get(names[i]).xRot + delta[i], find(model.root(), names[i]).xRot, 0.00001F, names[i]);
        assertEquals(original("flying").parts().get("BodyUpper").y - 0.65F - 2.5F,
                find(model.root(), "BodyUpper").y, 0.00001F, "Original flight bob includes its vertical baseline");
        var pose = rotations(model); model.setupAnim(s); assertArrayEquals(pose, rotations(model), 0.00001F);
    }

    @Test void hoverAndTackleKeepTheOriginalWingJointsOnTheirFlightCycle() {
        var model = model(); var s = new FireDragonRenderer.State(); s.male = true; s.flying = true;
        String[] joints = {"armL1", "armR1", "armL2", "armR2", "membraneL6", "membraneR6"};
        for (boolean hovering : new boolean[]{true, false}) {
            s.hoverProgress = hovering ? 20 : 0; s.flyProgress = hovering ? 0 : 20;
            s.attack = hovering ? 0 : com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.TACKLE;
            s.attackTick = 40; model.setupAnim(s);
            var frame = original("flight6");
            for (String name : joints) {
                var actual = find(model.root(), name); var expected = frame.parts().get(name);
                assertEquals(expected.xRot, actual.xRot, 0.00001F, name + " pitch hovering=" + hovering);
                assertEquals(expected.yRot, actual.yRot, 0.00001F, name + " yaw hovering=" + hovering);
                assertEquals(expected.zRot, actual.zRot, 0.00001F, name + " roll hovering=" + hovering);
            }
        }
    }

    @Test void walkingDoesNotDistortWingBlastOrEpicRoarButStillAnimatesTheLegs() {
        var model = model(); var s = new FireDragonRenderer.State(); s.male = true; s.walkCycle = 20; s.walkAnimationPos = 8;
        for (int attack : new int[]{4, 10}) {
            s.attack = attack; s.attackTick = attack == 4 ? 15 : 25; s.walkAnimationSpeed = 0; model.setupAnim(s);
            var before = new java.util.HashMap<String, net.minecraft.client.model.geom.PartPose>();
            for (String name : new String[]{"armL1", "armR1", "armL2", "armR2", "membraneL6", "membraneR6", "ThighL"})
                before.put(name, find(model.root(), name).storePose());
            s.walkAnimationSpeed = 1; model.setupAnim(s);
            for (String name : new String[]{"armL1", "armR1", "armL2", "armR2", "membraneL6", "membraneR6"}) {
                var p = find(model.root(), name); var expected = before.get(name);
                assertEquals(expected.xRot(), p.xRot, 0.00001F, name);
                assertEquals(expected.yRot(), p.yRot, 0.00001F, name);
                assertEquals(expected.zRot(), p.zRot, 0.00001F, name);
            }
            assertNotEquals(before.get("ThighL").xRot(), find(model.root(), "ThighL").xRot,
                    "Wing protection must not disable the walking gait");
        }
    }
    @Test void feedingKeepsBodyAndWingsAndRecoversContinuouslyAtEveryStage() {
        var model = model();
        for (int stage = 1; stage <= 5; stage++) for (boolean sitting : new boolean[]{false, true}) {
            var s = new FireDragonRenderer.State(); s.stage = stage; s.male = true;
            s.sitProgress = sitting ? 20 : 0; s.scale = stage;
            model.setupAnim(s); var idle = rotations(model);
            float neck = find(model.root(), "Neck1").xRot;
            float jaw = find(model.root(), "Jaw").xRot;
            s.attack = com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.EAT;
            s.attackTick = 5; model.setupAnim(s);
            assertEquals(neck + Math.toRadians(18), find(model.root(), "Neck1").xRot, 0.00001);
            s.attackTick = 10; model.setupAnim(s);
            assertEquals(jaw + Math.toRadians(18), find(model.root(), "Jaw").xRot, 0.00001);
            for (float tick : new float[]{0, 2.5F, 5, 7.5F, 10, 15, 17.5F, 20}) {
                s.attackTick = tick; model.setupAnim(s);
                for (String name : new String[]{"BodyUpper", "BodyLower", "armL1", "armR1"}) {
                    var actual = find(model.root(), name); var pose = actual.storePose();
                    s.attack = 0; model.setupAnim(s);
                    assertEquals(pose, find(model.root(), name).storePose(), "Feeding cannot deform the original body or folded wing " + name);
                    s.attack = com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.EAT; model.setupAnim(s);
                }
            }
            for (int boundary : new int[]{0, 5, 10, 15, 20}) {
                s.attackTick = boundary - 0.0001F; model.setupAnim(s); var before = rotations(model);
                s.attackTick = boundary + 0.0001F; model.setupAnim(s);
                assertArrayEquals(before, rotations(model), 0.0001F, "No feeding snap at " + boundary);
            }
            s.attackTick = 20; model.setupAnim(s);
            assertArrayEquals(idle, rotations(model), 0.00001F, "Twenty-tick recovery returns exactly to the original rest pose");
        }
    }
    @Test void preyVisualAttachmentKeepsItsSizeAndFollowsAnimatedHead() {
        var model = model();
        var state = new FireDragonRenderer.State(); state.attack = 7;
        for (float scale : new float[]{0.3F, 7F / 3, 10}) {
            state.scale = scale; state.attackTick = 30; model.setupAnim(state);
            var first = DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 40, false, false);
            var dimensions = first.getScale(new org.joml.Vector3f());
            assertEquals(1, dimensions.x, 0.0001F); assertEquals(1, dimensions.y, 0.0001F); assertEquals(1, dimensions.z, 0.0001F);
            assertEquals(1, first.determinant(), 0.0001F, "Dragon growth must cancel before the mob's own renderer");
            assertTrue(first.equals(DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 40, false, false), 0.00001F));
            var a = first.getTranslation(new org.joml.Vector3f());
            state.attackTick = 45; model.setupAnim(state);
            var b = DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 40, false, false).getTranslation(new org.joml.Vector3f());
            assertTrue(a.distance(b) > 0.01, "Visual prey must follow the shaking head rather than a static passenger seat");
        }
    }

    @Test void preyVisualAnchorRotatesWithDragonAndSupportsBothBodyOrientations() {
        var model = model(); var state = new FireDragonRenderer.State(); state.scale = 3; state.attack = 7; state.attackTick = 35;
        model.setupAnim(state);
        var a = DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 0, false, false).getTranslation(new org.joml.Vector3f());
        state.bodyRot = 90;
        var b = DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 0, false, false).getTranslation(new org.joml.Vector3f());
        assertEquals(-a.z, b.x, 0.00001F); assertEquals(a.x, b.z, 0.00001F); assertEquals(a.y, b.y, 0.00001F);
        var quadruped = DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 0, false, false);
        var upright = DragonPreyAttachment.transform(model, state, 0.9F, 1.4F, 0, true, false);
        assertFalse(quadruped.equals(upright, 0.01F), "Tall mobs and quadrupeds retain original distinct carry orientations");
    }
    @Test void bannerFollowsOriginalBodyTransformAndKeepsWorldSizeAcrossGrowth() {
        var model = model();
        var state = new FireDragonRenderer.State();
        for (float progress : new float[]{0, 10, 20}) {
            state.flyProgress = progress;
            state.hoverProgress = progress;
            state.sleepProgress = 20 - progress;
            model.setupAnim(state);
            org.joml.Vector3f normalizedAnchor = null;
            for (float scale : new float[]{0.3F, 1, 3, 10}) {
                var actual = new com.mojang.blaze3d.vertex.PoseStack();
                actual.scale(scale, scale, scale);
                DragonBannerLayer.applyAttachment(model, scale, actual);
                var anchor = actual.last().pose().transformPosition(new org.joml.Vector3f()).div(scale);
                if (normalizedAnchor == null) normalizedAnchor = anchor;
                else assertEquals(0, normalizedAnchor.distance(anchor), 0.00001F,
                        "Attachment location must scale with dragon while the item keeps its world size");
                assertEquals(1, actual.last().pose().transformDirection(new org.joml.Vector3f(0, 1, 0)).length(), 0.00001F,
                        "Banner size must not multiply with uncapped stage-five render scale");
                var body = new com.mojang.blaze3d.vertex.PoseStack();
                model.translateToBody(body);
                var bodyUp = body.last().pose().transformDirection(new org.joml.Vector3f(0, 1, 0)).normalize();
                var bannerUp = actual.last().pose().transformDirection(new org.joml.Vector3f(0, 1, 0)).normalize();
                assertEquals(-1, bodyUp.dot(bannerUp), 0.00001F, "Original banner flips relative to animated body");
            }
        }
    }
    @Test void sleepPoseMatchesOriginalAndEyeModelKeepsOnlyHeadFront() throws IOException {
        var model = model();
        var state = new FireDragonRenderer.State();
        state.sleepProgress = 20;
        state.sleeping = true;
        model.setupAnim(state);
        Path file = Path.of(System.getProperty("iceandfire.legacyResources"), "assets/iceandfire/models/tabula/firedragon/firedragon_sleeping.tbl");
        try (var input = Files.newInputStream(file)) {
            var original = TabulaLoader.parse(input);
            for (String name : new String[]{"BodyUpper", "Neck1", "Head"}) {
                var actual = find(model.root(), name);
                var expected = original.parts().get(name);
                // Original sleeping secondary motion at age zero: neck chain + Neck1.walk.
                float pitch = name.equals("Neck1") ? -0.025F : name.equals("Head") ? 0.03464548F : 0;
                assertEquals(Math.sin(expected.xRot + pitch), Math.sin(actual.xRot), 0.00001);
                assertEquals(Math.cos(expected.xRot + pitch), Math.cos(actual.xRot), 0.00001);
                assertEquals(expected.y - (name.equals("BodyUpper") ? 0.325F : 0), actual.y, 0.00001);
            }
        }
        var asleep = rotations(model);
        model.setupAnim(state);
        assertArrayEquals(asleep, rotations(model), 0.00001F);
        model.keepOnlyEyeCubes();
        assertFalse(find(model.root(), "HeadFront").skipDraw);
        for (String name : new String[]{"BodyUpper", "Head", "Jaw", "Neck1"}) assertTrue(find(model.root(), name).skipDraw);
    }

    @Test void everyVariantAndStageHasOriginalSleepAndEyeTextures() throws IOException {
        for (int variant = 0; variant < 4; variant++) for (int stage = 1; stage <= 5; stage++) {
            var state = new FireDragonRenderer.State(); state.variant = variant; state.stage = stage;
            for (String suffix : new String[]{"_sleeping", "_eyes"}) {
                var texture = FireDragonRenderer.texture(state, suffix);
                Path file = Path.of(System.getProperty("iceandfire.legacyResources"), "assets/iceandfire", texture.getPath());
                assertTrue(Files.size(file) > 100, "Original texture must exist: " + texture);
            }
        }
    }
    @Test void tailWingAndEatingPosesResetWithoutAccumulation() {
        var model = model();
        for (int attack : new int[]{3, 4, 5, 6, 7}) {
            var state = new FireDragonRenderer.State();
            state.attack = attack;
            for (float tick : new float[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65}) {
                state.attackTick = tick;
                model.setupAnim(state);
                var first = rotations(model);
                model.setupAnim(state);
                assertArrayEquals(first, rotations(model), 0.00001F);
            }
            state.attack = 0;
            model.setupAnim(state);
            var reset = rotations(model);
            model.setupAnim(new FireDragonRenderer.State());
            assertArrayEquals(reset, rotations(model), 0.00001F);
        }
        assertEquals("tail2", FireDragonPoseModel.attackFrame(3, 20).from());
        assertEquals("wing_blast7", FireDragonPoseModel.attackFrame(4, 35).from());
        assertEquals("grab2", FireDragonPoseModel.attackFrame(7, 25).from());
        assertEquals("grab_shake3", FireDragonPoseModel.attackFrame(7, 55).from());
    }
    @Test void hoveringUsesOriginalBodyPoseAndKeepsWingCycleWithoutAccumulation() throws IOException {
        var model = model();
        var state = new FireDragonRenderer.State();
        state.flying = true;
        state.hoverProgress = 20;
        model.setupAnim(state);
        Path file = Path.of(System.getProperty("iceandfire.legacyResources"),
                "assets/iceandfire/models/tabula/firedragon/firedragon_hovering.tbl");
        try (var input = Files.newInputStream(file)) {
            var original = TabulaLoader.parse(input);
            for (String name : new String[]{"BodyUpper", "ThighR", "ThighL"}) {
                var actual = find(model.root(), name);
                var expected = original.parts().get(name);
                float pitch = name.equals("ThighL") ? -0.05F : 0.05F;
                assertEquals(Math.sin(expected.xRot + pitch), Math.sin(actual.xRot), 0.00001);
                assertEquals(Math.cos(expected.zRot), Math.cos(actual.zRot), 0.00001);
            }
        }
        var first = rotations(model);
        model.setupAnim(state);
        assertArrayEquals(first, rotations(model), 0.00001F);
        state.flightCycle = 20;
        model.setupAnim(state);
        assertFalse(java.util.Arrays.equals(first, rotations(model)), "Hovering still flaps the wings");
        state.hoverProgress = 0;
        state.flying = false;
        model.setupAnim(state);
        var ground = rotations(model);
        model.setupAnim(new FireDragonRenderer.State());
        assertArrayEquals(ground, rotations(model), 0.00001F, "Hover pose must reset on landing");
    }
    @Test void carriedDragonUsesOriginalFoldedPoseAndResetsAfterRelease() throws IOException {
        var model = model();
        var state = new FireDragonRenderer.State();
        model.setupAnim(state);
        float[] ground = rotations(model);
        state.shoulderProgress = 20;
        model.setupAnim(state);
        Path file = Path.of(System.getProperty("iceandfire.legacyResources"),
                "assets/iceandfire/models/tabula/firedragon/firedragon_sitting_shoulder.tbl");
        try (var input = Files.newInputStream(file)) {
            var original = TabulaLoader.parse(input);
            for (String name : new String[]{"armR1", "armL1", "ThighR", "ThighL", "BodyUpper"}) {
                // Compare named parts through the same ground hierarchy, independent of atlas/render scale.
                var actual = find(model.root(), name);
                var expected = original.parts().get(name);
                assertNotNull(actual);
                assertEquals(Math.cos(expected.xRot), Math.cos(actual.xRot), 0.00001);
                assertEquals(Math.sin(expected.zRot), Math.sin(actual.zRot), 0.00001);
            }
        }
        var shoulder = rotations(model);
        model.setupAnim(state);
        assertArrayEquals(shoulder, rotations(model), 0.00001F);
        state.shoulderProgress = 0;
        model.setupAnim(state);
        assertArrayEquals(ground, rotations(model), 0.00001F);
    }

    private static ModelPart find(ModelPart root, String name) {
        if (root.hasChild(name)) return root.getChild(name);
        for (var part : root.getAllParts()) if (part.hasChild(name)) return part.getChild(name);
        return null;
    }
    @Test void firstPersonFireCannotLiftOrMovePilotsNeckAnchor() {
        var model = model();
        var state = new FireDragonRenderer.State();
        state.firstPersonPilot = true;
        model.setupAnim(state);
        var anchor = model.riderAnchor();
        for (float progress : new float[]{0, 1, 5, 10}) for (float tick : new float[]{0, 10, 20, 25, 29.9F}) {
            state.breathProgress = progress;
            state.attack = 2;
            state.attackTick = tick;
            model.setupAnim(state);
            assertEquals(anchor, model.riderAnchor(), "Fire must not push the animated neck into the first-person camera");
        }
        state.firstPersonPilot = false;
        state.attackTick = 20;
        model.setupAnim(state);
        assertNotEquals(anchor, model.riderAnchor(), "Other views keep the original body animation");
    }
    @Test void attackKeyframesKeepOriginalPoseOrderAndDurations() {
        int bite = com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.BITE;
        int charge = com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.CHARGE;
        assertEquals("bite1", FireDragonPoseModel.attackFrame(bite, 10).from());
        assertEquals("bite2", FireDragonPoseModel.attackFrame(bite, 15).from());
        assertEquals("bite3", FireDragonPoseModel.attackFrame(bite, 20).from());
        assertNull(FireDragonPoseModel.attackFrame(bite, 30).from());
        assertEquals("attack_blast_charge1", FireDragonPoseModel.attackFrame(charge, 10).from());
        assertEquals("attack_blast_charge2", FireDragonPoseModel.attackFrame(charge, 20).from());
        assertEquals("attack_blast_charge3", FireDragonPoseModel.attackFrame(charge, 25).from());
        assertNull(FireDragonPoseModel.attackFrame(charge, 30).to());
    }

    @Test void originalAttackPosesBlendContinuouslyWithoutAccumulationOrStaleSeat() {
        var model = model();
        var state = new FireDragonRenderer.State();
        for (int attack : new int[]{1, 2}) {
            state.attack = attack;
            for (float boundary : new float[]{0, 10, 15, 20, 25, 30, 35}) {
                state.attackTick = Math.max(0, boundary - 0.0001F);
                model.setupAnim(state);
                var before = rotations(model);
                state.attackTick = boundary;
                model.setupAnim(state);
                var after = rotations(model);
                for (int i = 0; i < before.length; i++) assertEquals(before[i], after[i], 0.001F);
                var anchor = model.riderAnchor();
                model.setupAnim(state);
                assertArrayEquals(after, rotations(model), 0.00001F);
                assertEquals(anchor, model.riderAnchor());
            }
        }
        state.attack = 0;
        for (float progress : new float[]{0, 0.001F, 5, 10, 5, 0}) {
            state.breathProgress = progress;
            model.setupAnim(state);
            var expected = rotations(model);
            model.setupAnim(state);
            assertArrayEquals(expected, rotations(model), 0.00001F);
        }
    }
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
        return new FireDragonPoseModel(FireDragonPoseModelTest::original);
    }

    private static TabulaLoader.Model original(String name) {
        Path file = Path.of(System.getProperty("iceandfire.legacyResources"),
                "assets/iceandfire/models/tabula/firedragon/firedragon_" + name + ".tbl");
        try (var input = Files.newInputStream(file)) { return TabulaLoader.parse(input); }
        catch (IOException error) { throw new UncheckedIOException(error); }
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

    @Test void speechMovesOnlyJawAndReturnsWithoutAccumulation() {
        var model = model(); var state = new FireDragonRenderer.State();
        state.male = true; model.setupAnim(state);
        var jaw = model.root().getAllParts().stream().filter(p -> p == model.root().getChild("BodyUpper").getChild("Neck1").getChild("Neck2").getChild("Neck3").getChild("Head").getChild("Jaw")).findFirst().orElseThrow();
        float pitch = jaw.xRot, z = jaw.z; var idle = rotations(model);
        state.attack = com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.SPEAK;
        state.attackTick = 10; model.setupAnim(state);
        assertEquals(pitch + Math.toRadians(18), jaw.xRot, 0.00001);
        assertEquals(z + 0.2F, jaw.z, 0.00001);
        model.setupAnim(state); assertEquals(z + 0.2F, jaw.z, 0.00001);
        state.attackTick = 20; model.setupAnim(state);
        assertArrayEquals(idle, rotations(model), 0.00001F);
        assertEquals(z, jaw.z, 0.00001);
    }

    @Test void roarKeyframesUseOriginalSequenceAndReturnToIdle() {
        var model = model(); var state = new FireDragonRenderer.State(); state.male = true;
        model.setupAnim(state); var idle = rotations(model);
        for (int type : new int[]{9, 10}) {
            String prefix = type == 9 ? "roar" : "epic_roar";
            assertEquals(prefix + "1", FireDragonPoseModel.attackFrame(type, 10).from());
            assertEquals(prefix + "2", FireDragonPoseModel.attackFrame(type, 20).from());
            assertEquals(prefix + "3", FireDragonPoseModel.attackFrame(type, 30).from());
            state.attack = type; state.attackTick = 20; model.setupAnim(state);
            assertFalse(java.util.Arrays.equals(idle, rotations(model)), "Original roar visibly changes pose");
            var active = rotations(model); model.setupAnim(state);
            assertArrayEquals(active, rotations(model), 0.00001F, "Repeated frame cannot accumulate pitch");
            state.attackTick = type == 9 ? 40 : 60; model.setupAnim(state);
            assertArrayEquals(idle, rotations(model), 0.00001F, "End of roar returns to original pose");
        }
    }

    @Test void epicRoarAddsOriginalBodyPitchAndInterpolatesContinuously() {
        var model = model(); var state = new FireDragonRenderer.State(); state.male = true;
        state.attack = 10;
        for (int boundary : new int[]{10, 20, 30, 40, 50, 60}) {
            state.attackTick = boundary - 0.0001F; model.setupAnim(state); var before = rotations(model);
            state.attackTick = boundary; model.setupAnim(state);
            assertArrayEquals(before, rotations(model), 0.001F, "Continuous epic roar boundary " + boundary);
        }
    }

    @Test void corpseUsesOriginalDeathPoseWithoutIdleTrackingOrAttackMotion() {
        var model = model(); var s = new FireDragonRenderer.State(); s.male = true; s.corpse = true; s.modelDeadProgress = 20;
        model.setupAnim(s); var expected = original("dead");
        for (String name : new String[]{"BodyUpper", "Neck1", "Head", "ThighL", "ThighR", "armL1", "Tail3"}) {
            var actual = find(model.root(), name); var ref = expected.parts().get(name);
            for (float[] pair : new float[][]{{actual.xRot, ref.xRot}, {actual.yRot, ref.yRot}, {actual.zRot, ref.zRot}}) {
                assertEquals(Math.sin(pair[1]), Math.sin(pair[0]), 0.00001, name);
                assertEquals(Math.cos(pair[1]), Math.cos(pair[0]), 0.00001, name);
            }
        }
        var before = rotations(model); s.ageInTicks = 999; s.yRot = 90; s.xRot = 30;
        s.flying = true; s.flyProgress = 20; s.attack = 4; s.attackTick = 15; s.walkAnimationSpeed = 1;
        model.setupAnim(s); assertArrayEquals(before, rotations(model), 0.00001F, "Corpse ignores all live secondary and attack movement");
        s.corpse = false; s.flying = false; s.flyProgress = 0; s.attack = 0; s.walkAnimationSpeed = 0; model.setupAnim(s);
        s.corpse = true; model.setupAnim(s); assertArrayEquals(before, rotations(model), 0.00001F, "Returning to corpse cannot accumulate prior poses");
    }

    @Test void corpseThighsUseOriginalLinearAngleInterpolationAtHalfProgress() {
        var model = model(); var s = new FireDragonRenderer.State(); s.male = true; s.corpse = true; s.modelDeadProgress = 10;
        var base = original("ground"); var dead = original("dead"); model.setupAnim(s);
        for (String name : new String[]{"ThighL", "ThighR"}) {
            var start = base.parts().get(name); var end = dead.parts().get(name); var actual = find(model.root(), name);
            assertEquals((start.xRot + end.xRot) / 2, actual.xRot, 0.00001F);
            assertEquals((start.yRot + end.yRot) / 2, actual.yRot, 0.00001F);
            assertEquals((start.zRot + end.zRot) / 2, actual.zRot, 0.00001F);
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
