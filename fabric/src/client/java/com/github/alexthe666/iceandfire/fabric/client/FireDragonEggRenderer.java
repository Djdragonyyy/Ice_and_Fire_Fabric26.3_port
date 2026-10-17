package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragonEgg;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.*;

/** Geometry and UV coordinates from the original ModelDragonEgg, LGPL-3.0. */
public final class FireDragonEggRenderer extends MobRenderer<FireDragonEgg, FireDragonEggRenderer.State, EntityModel<FireDragonEggRenderer.State>> {
    private static final String[] COLORS = {"red", "green", "bronze", "gray"};
    public static final class State extends LivingEntityRenderState { int variant; }
    private static final class Model extends EntityModel<State> { Model(ModelPart root) { super(root); } }

    public FireDragonEggRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(eggModel()), 0.3F);
    }

    private static ModelPart box(int u, int v, float x, float y, float z, float w, float h, float d, Map<String, ModelPart> children) {
        return new ModelPart(List.of(new ModelPart.Cube(u, v, x, y, z, w, h, d, 0, 0, 0, false, 64, 32,
                EnumSet.allOf(Direction.class))), children);
    }

    private static ModelPart eggModel() {
        ModelPart egg4 = box(28, 16, -2, -4.8F, -2, 4, 4, 4, Map.of());
        egg4.setPos(0, -0.9F, 0);
        ModelPart egg3 = box(0, 0, -2.5F, -4.6F, -2.5F, 5, 5, 5, Map.of("Egg4", egg4));
        ModelPart egg2 = box(22, 2, -2.5F, -0.6F, -2.5F, 5, 5, 5, Map.of());
        ModelPart egg1 = box(0, 12, -3, -2.8F, -3, 6, 6, 6, Map.of("Egg2", egg2, "Egg3", egg3));
        egg1.setPos(0, 19.6F, 0);
        ModelPart root = new ModelPart(List.of(), Map.of("Egg1", egg1));
        root.getAllParts().forEach(part -> part.setInitialPose(part.storePose()));
        return root;
    }

    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(FireDragonEgg egg, State state, float partialTick) {
        super.extractRenderState(egg, state, partialTick);
        state.variant = egg.variant();
    }
    @Override public Identifier getTextureLocation(State state) {
        return IafContent.id("textures/models/firedragon/egg_" + COLORS[state.variant] + ".png");
    }
}
