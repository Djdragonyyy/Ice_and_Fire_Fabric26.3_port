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
    FireDragonPoseModel(ResourceManager resources, boolean eyesOnly) {
        this(resources);
        if (eyesOnly) keepOnlyEyeCubes();
    }
    void keepOnlyEyeCubes() { parts.forEach((name, part) -> part.skipDraw = !name.equals("HeadFront")); }

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
        for (String name : List.of("base_male", "base_female", "flying", "hovering", "sitting", "sitting_shoulder", "walk1", "walk2", "walk3", "walk4",
                "flight1", "flight2", "flight3", "flight4", "flight5", "flight6",
                "bite1", "bite2", "bite3", "attack_blast_charge1", "attack_blast_charge2", "attack_blast_charge3", "attack_stream_breath",
                "tail1", "tail2", "tail3", "wing_blast1", "wing_blast2", "wing_blast3", "wing_blast4", "wing_blast5", "wing_blast6", "wing_blast7", "attack_tackle", "sleeping",
                "grab1", "grab2", "grab_shake1", "grab_shake2", "grab_shake3",
                "roar1", "roar2", "roar3", "epic_roar1", "epic_roar2", "epic_roar3")) {
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

    /** Original LayerDragonBanner attaches directly to animated BodyUpper. */
    void translateToBody(PoseStack stack) { parts.get("BodyUpper").translateAndRotate(stack); }
    void translateToPreyBody(PoseStack stack) {
        translateToBody(stack); parts.get("Neck1").translateAndRotate(stack);
    }
    void translateToPreyHead(PoseStack stack) {
        for (String name : List.of("Neck2", "Neck3", "Head")) parts.get(name).translateAndRotate(stack);
    }

    private static TabulaLoader.Model pose(ResourceManager resources, String name) {
        return TabulaLoader.loadModel(resources, IafContent.id("models/tabula/firedragon/firedragon_" + name + ".tbl"));
    }

    @Override public void setupAnim(FireDragonRenderer.State state) {
        super.setupAnim(state);
        boolean walking = !state.flying && state.flyProgress <= 0 && state.hoverProgress <= 0;
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
                float amount = Math.min(20, state.flyProgress + state.hoverProgress) / 20F;
                cube.xRot += amount * distance(cube.xRot, previousPose.xRot + frame * distance(previousPose.xRot, currentPose.xRot));
                cube.yRot += amount * distance(cube.yRot, previousPose.yRot + frame * distance(previousPose.yRot, currentPose.yRot));
                cube.zRot += amount * distance(cube.zRot, previousPose.zRot + frame * distance(previousPose.zRot, currentPose.zRot));
            }
        }
        if (state.hoverProgress > 0) {
            for (var entry : parts.entrySet()) {
                String name = entry.getKey();
                if (name.contains("Wing") || name.contains("Finger") || name.startsWith("arm") || name.contains("Tail")) continue;
                ModelPart hover = poses.get("hovering").parts().get(name);
                if (hover != null) transition(entry.getValue(), hover, state.hoverProgress / 20F);
            }
        }
        applySecondaryMotion(state, walking);
        applyCombat(state);
        if (state.sleepProgress > 0) {
            float weight = state.sleepProgress / 20F;
            for (var entry : parts.entrySet()) {
                ModelPart asleep = poses.get("sleeping").parts().get(entry.getKey());
                if (asleep != null && !entry.getKey().contains("Horn")) transition(entry.getValue(), asleep, weight);
            }
        }
        if (state.shoulderProgress > 0) {
            float weight = state.shoulderProgress / 20F;
            for (var entry : parts.entrySet()) {
                if (entry.getKey().contains("Horn")) continue;
                ModelPart shoulder = poses.get("sitting_shoulder").parts().get(entry.getKey());
                if (shoulder != null) rotateTo(entry.getValue(), shoulder, weight);
            }
            parts.get("BodyUpper").z -= 12F * weight;
        }
        for (String name : List.of("Neck1", "Neck2", "Neck3", "Head")) {
            ModelPart neck = parts.get(name);
            if (neck != null) {
                float awake = 1 - state.sleepProgress / 20F;
                neck.yRot += (float) Math.toRadians(state.yRot) / 8F * awake;
                neck.xRot += (float) Math.toRadians(state.xRot) / 8F * awake;
            }
        }
    }

    record AttackFrame(String from, String to, float blend) {}

    static AttackFrame attackFrame(int attack, float tick) {
        String[] names = attack == DragonCombatState.BITE
                ? new String[]{null, "bite1", "bite2", "bite3", null}
                : new String[]{null, "attack_blast_charge1", "attack_blast_charge2", "attack_blast_charge3", null};
        int[] durations = attack == DragonCombatState.BITE ? new int[]{10, 5, 5, 10} : new int[]{10, 10, 5, 5};
        if (attack == DragonCombatState.TAIL) {
            names = new String[]{null, "tail1", "tail2", "tail3", null};
            durations = new int[]{10, 10, 10, 10};
        } else if (attack == DragonCombatState.WING) {
            names = new String[]{null, "wing_blast1", "wing_blast2", "wing_blast3", "wing_blast4", "wing_blast5", "wing_blast6", "wing_blast7", null};
            durations = new int[]{5, 5, 5, 5, 5, 5, 5, 10};
        } else if (attack == DragonCombatState.SHAKE) {
            names = new String[]{null, "grab1", "grab2", "grab_shake1", "grab_shake2", "grab_shake3", null};
            durations = new int[]{15, 10, 10, 10, 10, 10};
        } else if (attack == DragonCombatState.ROAR) {
            names = new String[]{null, "roar1", "roar2", "roar3", null};
            durations = new int[]{10, 10, 10, 10};
        } else if (attack == DragonCombatState.EPIC_ROAR) {
            names = new String[]{null, "epic_roar1", "epic_roar2", "epic_roar3", "epic_roar2", "epic_roar3", null};
            durations = new int[]{10, 10, 10, 10, 10, 10};
        }
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
        if (state.attack == DragonCombatState.SPEAK) {
            float tick = Math.clamp(state.attackTick, 0, 20);
            float weight = tick < 5 ? (float) Math.sin(tick / 5 * Math.PI * 0.5)
                    : tick < 15 ? 1 : 1 - (float) Math.sin((tick - 15) / 5 * Math.PI * 0.5);
            var jaw = parts.get("Jaw");
            jaw.xRot += (float) Math.toRadians(18) * weight;
            jaw.z += 0.2F * weight;
        } else if (state.attack == DragonCombatState.TACKLE) {
            float weight = Math.clamp(state.attackTick / 40F, 0, 1);
            for (var entry : parts.entrySet())
                addPoseDelta(entry.getValue(), poses.get("attack_tackle").parts().get(entry.getKey()), weight, true);
        } else if (state.attack == DragonCombatState.EAT) {
            float tick = state.attackTick;
            float neck = tick < 5 ? tick / 5 : tick < 10 ? (10 - tick) / 5 : 0;
            float jaw = tick < 5 ? 0 : tick < 10 ? (tick - 5) / 5 : tick < 15 ? 1 : Math.max(0, (20 - tick) / 5);
            for (String name : List.of("Neck1", "Neck2")) parts.get(name).xRot += (float) Math.toRadians(18) * neck;
            parts.get("Jaw").xRot += (float) Math.toRadians(18) * jaw;
            parts.get("Jaw").z += 0.2F * jaw;
        } else if (state.attack != DragonCombatState.NONE && !(state.firstPersonPilot && state.attack == DragonCombatState.CHARGE)) {
            AttackFrame frame = attackFrame(state.attack, state.attackTick);
            for (var entry : parts.entrySet()) {
                ModelPart part = entry.getValue();
                ModelPart from = frame.from == null ? null : poses.get(frame.from).parts().get(entry.getKey());
                ModelPart to = frame.to == null ? null : poses.get(frame.to).parts().get(entry.getKey());
                addPoseDelta(part, from, 1 - frame.blend, true);
                addPoseDelta(part, to, frame.blend, true);
            }
            if (state.attack == DragonCombatState.WING)
                parts.get("BodyUpper").y += wingLift(frame.from) * (1 - frame.blend) + wingLift(frame.to) * frame.blend;
            if (state.attack == DragonCombatState.EPIC_ROAR) {
                // Same additional BodyUpper pitch as the original five keyframes.
                float[] pitch = {0, -0.1F, -0.2F, -0.2F, -0.2F, -0.1F, 0};
                int segment = Math.clamp((int) (state.attackTick / 10), 0, 5);
                if (state.attackTick < 60) parts.get("BodyUpper").xRot += pitch[segment] * (1 - frame.blend) + pitch[segment + 1] * frame.blend;
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

    private static float wingLift(String pose) {
        if (pose == null) return 0;
        return switch (pose) { case "wing_blast2", "wing_blast7" -> -2; case "wing_blast3", "wing_blast4", "wing_blast5", "wing_blast6" -> -4; default -> 0; };
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
