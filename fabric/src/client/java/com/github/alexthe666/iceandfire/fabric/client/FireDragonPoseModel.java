package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;
import java.util.function.Function;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

/** Movement pose interpolation adapted from DragonTabulaModelAnimator (LGPL-3.0). */
final class FireDragonPoseModel extends EntityModel<FireDragonRenderer.State> {
    private final Map<String, ModelPart> parts;
    private final Map<String, TabulaLoader.Model> poses = new HashMap<>();

    FireDragonPoseModel(ResourceManager resources) {
        this(name -> pose(resources, name));
    }

    FireDragonPoseModel(Function<String, TabulaLoader.Model> loader) {
        this(loader, loader.apply("ground"));
    }

    private FireDragonPoseModel(Function<String, TabulaLoader.Model> loader, TabulaLoader.Model ground) {
        super(ground.root());
        parts = ground.parts();
        for (String name : List.of("base_male", "base_female", "flying", "sitting", "walk1", "walk2", "walk3", "walk4",
                "flight1", "flight2", "flight3", "flight4", "flight5", "flight6")) {
            poses.put(name, loader.apply(name));
        }
    }

    /** Same animated BodyUpper -> Neck1 attachment used by the original LayerDragonRider. */
    Vector3f riderAnchor() {
        PoseStack stack = new PoseStack();
        parts.get("BodyUpper").translateAndRotate(stack);
        parts.get("Neck1").translateAndRotate(stack);
        return stack.last().pose().transformPosition(new Vector3f(0, -0.01F, -0.035F));
    }

    private static TabulaLoader.Model pose(ResourceManager resources, String name) {
        return TabulaLoader.loadModel(resources, IafContent.id("models/tabula/firedragon/firedragon_" + name + ".tbl"));
    }

    @Override public void setupAnim(FireDragonRenderer.State state) {
        super.setupAnim(state);
        boolean walking = !state.flying && state.flyProgress <= 0;
        float cycle = animationCycle(walking ? state.walkCycle : state.flightCycle, state.partialTick, walking);
        int current = (int) (cycle / 10);
        int previous = current == 0 ? (walking ? 3 : 5) : current - 1;
        float frame = (cycle / 10F) % 1F;
        Map<String, ModelPart> from = poses.get((walking ? "walk" : "flight") + (previous + 1)).parts();
        Map<String, ModelPart> to = poses.get((walking ? "walk" : "flight") + (current + 1)).parts();
        for (var entry : parts.entrySet()) {
            String name = entry.getKey();
            ModelPart cube = entry.getValue();
            ModelPart male = poses.get("base_male").parts().get(name);
            ModelPart female = poses.get("base_female").parts().get(name);
            if (!state.male && male != null && female != null && !sameRotation(male, female)) rotateTo(cube, female, 1);
            ModelPart previousPose = from.get(name);
            ModelPart currentPose = to.get(name);
            if (previousPose == null || currentPose == null) continue;
            if (walking) {
                if (!name.contains("Horn")) {
                    float amount = Math.min(state.walkAnimationSpeed * 2, 1);
                    var initial = cube.getInitialPose();
                    cube.xRot += amount * distance(initial.xRot(), previousPose.xRot + frame * distance(previousPose.xRot, currentPose.xRot));
                    cube.yRot += amount * distance(initial.yRot(), previousPose.yRot + frame * distance(previousPose.yRot, currentPose.yRot));
                    cube.zRot += amount * distance(initial.zRot(), previousPose.zRot + frame * distance(previousPose.zRot, currentPose.zRot));
                }
            }
            ModelPart flight = poses.get("flying").parts().get(name);
            if (flight != null && state.flyProgress > 0) transition(cube, flight, state.flyProgress / 20F);
            if (state.sitProgress > 0) {
                ModelPart sitting = poses.get("sitting").parts().get(name);
                if (sitting != null) transition(cube, sitting, state.sitProgress / 20F);
            }
            if (!walking && flight != null) {
                float amount = state.flyProgress / 20F;
                cube.xRot += amount * distance(cube.xRot, previousPose.xRot + frame * distance(previousPose.xRot, currentPose.xRot));
                cube.yRot += amount * distance(cube.yRot, previousPose.yRot + frame * distance(previousPose.yRot, currentPose.yRot));
                cube.zRot += amount * distance(cube.zRot, previousPose.zRot + frame * distance(previousPose.zRot, currentPose.zRot));
            }
        }
        applySecondaryMotion(state, walking);
        for (String name : List.of("Neck1", "Neck2", "Neck3", "Head")) {
            ModelPart neck = parts.get(name);
            if (neck != null) {
                neck.yRot += (float) Math.toRadians(state.yRot) / 8F;
                neck.xRot += (float) Math.toRadians(state.xRot) / 8F;
            }
        }
    }

    static float animationCycle(int tick, float partialTick, boolean walking) {
        // Flight advances two units per tick. Interpolate across every boundary, including zero.
        return (tick + Math.clamp(partialTick, 0, 1) * (walking ? 1 : 2)) % (walking ? 40 : 60);
    }

    private void applySecondaryMotion(FireDragonRenderer.State state, boolean walking) {
        float age = state.ageInTicks;
        float idle = (float) Math.sin(age * 0.05F) * 0.65F;
        bob("BodyUpper", idle);
        for (String name : List.of("ThighR", "ThighL", "armR1", "armL1")) bob(name, -idle);
        if (walking) {
            float phase = state.walkAnimationPos;
            float amount = state.walkAnimationSpeed;
            bob("BodyUpper", (float) Math.sin(phase * 0.4F) * amount * 0.85F);
            bob("ThighR", (float) Math.sin(phase * 0.2F) * amount * 0.85F);
            bob("ThighL", (float) Math.sin(phase * 0.2F) * amount * 0.85F);
        } else {
            // Fade with the pose so the last landing frame cannot move the body/seat abruptly.
            bob("BodyUpper", (float) Math.sin(-age * 0.2F) * 2.5F * (state.flyProgress / 20F));
        }
        // Original chainWave/chainSwing idle motion, evaluated at render time rather than at 20 Hz.
        String[] tail = {"Tail1", "Tail2", "Tail3", "Tail4"};
        for (int i = 0; i < tail.length; i++) {
            ModelPart part = parts.get(tail[i]);
            if (part == null) continue;
            part.yRot += (float) Math.cos(age * 0.05F - 2 * i) * 0.125F;
            part.xRot += (float) Math.cos(age * 0.05F - 2 * i) * 0.075F;
        }
    }

    private void bob(String name, float offset) {
        ModelPart part = parts.get(name);
        if (part != null) part.y += offset;
    }

    private static float distance(float from, float to) {
        return (float) Math.atan2(Math.sin(to - from), Math.cos(to - from));
    }

    private static boolean sameRotation(ModelPart a, ModelPart b) {
        return a.xRot == b.xRot && a.yRot == b.yRot && a.zRot == b.zRot;
    }

    private static void rotateTo(ModelPart from, ModelPart to, float amount) {
        from.xRot += amount * distance(from.xRot, to.xRot);
        from.yRot += amount * distance(from.yRot, to.yRot);
        from.zRot += amount * distance(from.zRot, to.zRot);
    }

    private static void transition(ModelPart from, ModelPart to, float amount) {
        rotateTo(from, to, amount);
        from.x += (to.x - from.x) * amount;
        from.y += (to.y - from.y) * amount;
        from.z += (to.z - from.z) * amount;
    }
}
