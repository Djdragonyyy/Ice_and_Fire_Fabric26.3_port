package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;

/** Movement pose interpolation adapted from DragonTabulaModelAnimator (LGPL-3.0). */
final class FireDragonPoseModel extends EntityModel<FireDragonRenderer.State> {
    private final Map<String, ModelPart> parts;
    private final Map<String, TabulaLoader.Model> poses = new HashMap<>();

    FireDragonPoseModel(ResourceManager resources) {
        this(resources, pose(resources, "ground"));
    }

    private FireDragonPoseModel(ResourceManager resources, TabulaLoader.Model ground) {
        super(ground.root());
        parts = ground.parts();
        for (String name : List.of("base_male", "base_female", "flying", "sitting", "walk1", "walk2", "walk3", "walk4",
                "flight1", "flight2", "flight3", "flight4", "flight5", "flight6")) {
            poses.put(name, pose(resources, name));
        }
    }

    private static TabulaLoader.Model pose(ResourceManager resources, String name) {
        return TabulaLoader.loadModel(resources, IafContent.id("models/tabula/firedragon/firedragon_" + name + ".tbl"));
    }

    @Override public void setupAnim(FireDragonRenderer.State state) {
        super.setupAnim(state);
        boolean walking = !state.flying && state.flyProgress <= 0;
        int cycle = walking ? state.walkCycle : state.flightCycle;
        int current = cycle / 10;
        int previous = current == 0 ? (walking ? 3 : 5) : current - 1;
        float delta = (cycle / 10F) % 1F;
        float frame = delta == 0 ? 0 : delta + state.partialTick / 10F;
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
            if (!walking && flight != null && !sameRotation(currentPose, flight)) {
                cube.xRot += distance(cube.xRot, previousPose.xRot + frame * distance(previousPose.xRot, currentPose.xRot));
                cube.yRot += distance(cube.yRot, previousPose.yRot + frame * distance(previousPose.yRot, currentPose.yRot));
                cube.zRot += distance(cube.zRot, previousPose.zRot + frame * distance(previousPose.zRot, currentPose.zRot));
            }
        }
        for (String name : List.of("Neck1", "Neck2", "Neck3", "Head")) {
            ModelPart neck = parts.get(name);
            if (neck != null) {
                neck.yRot += (float) Math.toRadians(state.yRot) / 8F;
                neck.xRot += (float) Math.toRadians(state.xRot) / 8F;
            }
        }
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
