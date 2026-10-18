package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState;
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
    private final Vector3f seatSurface = new Vector3f(Float.NaN);

    FireDragonPoseModel(ResourceManager resources) {
        this(name -> pose(resources, name));
    }

    FireDragonPoseModel(Function<String, TabulaLoader.Model> loader) {
        this(loader, loader.apply("ground"));
    }

    private FireDragonPoseModel(Function<String, TabulaLoader.Model> loader, TabulaLoader.Model ground) {
        super(ground.root());
        parts = ground.parts();
        // Read the actual neck cube's upper rear edge, rather than its internal pivot.
        parts.get("Neck1").visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) seatSurface.set(
                    (cube.minX + cube.maxX) / 32F, cube.minY / 16F, cube.maxZ / 16F);
        });
        if (!Float.isFinite(seatSurface.y)) throw new IllegalStateException("Missing neck seat geometry");
        for (String name : List.of("base_male", "base_female", "flying", "sitting", "walk1", "walk2", "walk3", "walk4",
                "flight1", "flight2", "flight3", "flight4", "flight5", "flight6",
                "bite1", "bite2", "bite3", "attack_blast_charge1", "attack_blast_charge2", "attack_blast_charge3", "attack_stream_breath")) {
            poses.put(name, loader.apply(name));
        }
    }

    /** Same animated BodyUpper -> Neck1 attachment used by the original LayerDragonRider. */
    Vector3f riderAnchor() {
        PoseStack stack = new PoseStack();
        parts.get("BodyUpper").translateAndRotate(stack);
        parts.get("Neck1").translateAndRotate(stack);
        return stack.last().pose().transformPosition(new Vector3f(seatSurface));
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
        applyCombat(state);
        for (String name : List.of("Neck1", "Neck2", "Neck3", "Head")) {
            ModelPart neck = parts.get(name);
            if (neck != null) {
                neck.yRot += (float) Math.toRadians(state.yRot) / 8F;
                neck.xRot += (float) Math.toRadians(state.xRot) / 8F;
            }
        }
    }

    record AttackFrame(String from, String to, float blend) {}

    static AttackFrame attackFrame(int attack, float tick) {
        String[] names = attack == DragonCombatState.BITE
                ? new String[]{null, "bite1", "bite2", "bite3", null}
                : new String[]{null, "attack_blast_charge1", "attack_blast_charge2", "attack_blast_charge3", null};
        int[] durations = attack == DragonCombatState.BITE ? new int[]{10, 5, 5, 10} : new int[]{10, 10, 5, 5};
        float time = Math.max(0, tick);
        for (int i = 0; i < durations.length; i++) {
            if (time < durations[i]) {
                // Citadel ModelAnimator uses sine interpolation for keyframes.
                return new AttackFrame(names[i], names[i + 1], (float) Math.sin(time / durations[i] * Math.PI * 0.5));
            }
            time -= durations[i];
        }
        return new AttackFrame(null, null, 0);
    }

    private void applyCombat(FireDragonRenderer.State state) {
        if (state.attack != DragonCombatState.NONE && !(state.firstPersonPilot && state.attack == DragonCombatState.CHARGE)) {
            AttackFrame frame = attackFrame(state.attack, state.attackTick);
            for (var entry : parts.entrySet()) {
                ModelPart part = entry.getValue();
                ModelPart from = frame.from == null ? null : poses.get(frame.from).parts().get(entry.getKey());
                ModelPart to = frame.to == null ? null : poses.get(frame.to).parts().get(entry.getKey());
                addPoseDelta(part, from, 1 - frame.blend, true);
                addPoseDelta(part, to, frame.blend, true);
            }
        }
        if (state.breathProgress > 0) {
            float stream = com.github.alexthe666.iceandfire.fabric.dragon.DragonBreathVisuals.smooth(state.breathProgress / 10);
            for (var entry : parts.entrySet()) {
                String name = entry.getKey();
                if (state.firstPersonPilot && !name.equals("Jaw")) continue;
                if (name.contains("Finger") || name.startsWith("arm") || name.startsWith("Wing")) continue;
                addPoseDelta(entry.getValue(), poses.get("attack_stream_breath").parts().get(name), stream, false);
            }
        }
    }

    private static void addPoseDelta(ModelPart part, ModelPart pose, float weight, boolean position) {
        if (pose == null || weight == 0) return;
        var initial = part.getInitialPose();
        part.xRot += distance(initial.xRot(), pose.xRot) * weight;
        part.yRot += distance(initial.yRot(), pose.yRot) * weight;
        part.zRot += distance(initial.zRot(), pose.zRot) * weight;
        if (position) {
            part.x += (pose.x - initial.x()) * weight;
            part.y += (pose.y - initial.y()) * weight;
            part.z += (pose.z - initial.z()) * weight;
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
