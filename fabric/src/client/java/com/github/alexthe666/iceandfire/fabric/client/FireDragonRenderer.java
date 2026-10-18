package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonGrowth;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Uses the original Tabula geometry, poses and stage/variant textures. */
public final class FireDragonRenderer extends MobRenderer<FireDragon, FireDragonRenderer.State, EntityModel<FireDragonRenderer.State>> {
    private static final String[] COLORS = {"red", "green", "bronze", "gray"};

    public static final class State extends LivingEntityRenderState {
        int variant;
        int stage;
        boolean flying;
        boolean male;
        int walkCycle;
        int flightCycle;
        float flyProgress;
        float sitProgress;
        float partialTick;
    }

    public FireDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new FireDragonPoseModel(context.getResourceManager()), 1F);
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(FireDragon dragon, State state, float partialTick) {
        super.extractRenderState(dragon, state, partialTick);
        state.variant = dragon.variant();
        state.stage = dragon.stage();
        state.flying = dragon.flying();
        state.male = dragon.male();
        state.walkCycle = dragon.walkCycle;
        state.flightCycle = dragon.flightCycle;
        state.flyProgress = dragon.prevFlyProgress + (dragon.flyProgress - dragon.prevFlyProgress) * partialTick;
        state.sitProgress = dragon.prevSitProgress + (dragon.sitProgress - dragon.prevSitProgress) * partialTick;
        state.partialTick = partialTick;
        // The legacy renderer used renderSize / 3; the entity's bounding-box
        // scale is separately capped at seven in the original implementation.
        state.scale = DragonGrowth.renderSize(dragon.ageDays()) / 3F;
    }

    @Override public Identifier getTextureLocation(State state) {
        return IafContent.id("textures/models/firedragon/" + COLORS[state.variant] + "_" + state.stage + ".png");
    }

    /** Visual rider follows the animated neck; physical seat retains the original POV offsets. */
    public Vec3 riderRenderPosition(FireDragon dragon, float partialTick) {
        State state = new State();
        extractRenderState(dragon, state, partialTick);
        FireDragonPoseModel pose = (FireDragonPoseModel) getModel();
        pose.setupAnim(state);
        var anchor = pose.riderAnchor();
        return riderRenderPosition(state, anchor);
    }

    static Vec3 riderRenderPosition(State state, org.joml.Vector3f anchor) {
        double yaw = Math.toRadians(state.bodyRot);
        double right = anchor.x * state.scale;
        double forward = -anchor.z * state.scale;
        return new Vec3(
                state.x - forward * Math.sin(yaw) + right * Math.cos(yaw),
                state.y + (1.501 - anchor.y) * state.scale - 0.25,
                state.z + forward * Math.cos(yaw) + right * Math.sin(yaw));
    }
}
