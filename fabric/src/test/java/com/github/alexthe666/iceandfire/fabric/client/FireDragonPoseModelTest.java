package com.github.alexthe666.iceandfire.fabric.client;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FireDragonPoseModelTest {
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
                assertEquals(Math.sin(expected.xRot), Math.sin(actual.xRot), 0.00001);
                assertEquals(Math.cos(expected.xRot), Math.cos(actual.xRot), 0.00001);
                assertEquals(expected.y, actual.y, 0.00001);
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
                assertEquals(Math.sin(expected.xRot), Math.sin(actual.xRot), 0.00001);
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
