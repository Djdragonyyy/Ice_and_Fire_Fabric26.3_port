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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;

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
        float flightPitch;
        int attack;
        float attackTick;
        float breathProgress;
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
        state.attack = dragon.attackAnimation();
        state.attackTick = dragon.attackAnimationTick() + partialTick;
        state.breathProgress = dragon.prevBreathProgress + (dragon.breathProgress - dragon.prevBreathProgress) * partialTick;
        var client = Minecraft.getInstance();
        boolean firstPersonRider = client.player != null && client.getCameraEntity() == client.player
                && dragon.getControllingPassenger() == client.player && client.options.getCameraType().isFirstPerson();
        state.flightPitch = viewPitch(dragon.flightPitch(partialTick), firstPersonRider);
        // The legacy renderer used renderSize / 3; the entity's bounding-box
        // scale is separately capped at seven in the original implementation.
        state.scale = DragonGrowth.renderSize(dragon.ageDays()) / 3F;
    }

    static float viewPitch(float flightPitch, boolean firstPersonRider) {
        return firstPersonRider ? 0 : flightPitch;
    }

    @Override public Identifier getTextureLocation(State state) {
        return IafContent.id("textures/models/firedragon/" + COLORS[state.variant] + "_" + state.stage + ".png");
    }

    @Override protected void scale(State state, PoseStack stack) {
        // Same placement as RenderDragonBase.scale: after yaw/axis flip, before model translation.
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(state.flightPitch)));
    }

    /** Visual rider follows the animated neck; physical seat retains the original POV offsets. */
    public Vec3 riderRenderPosition(FireDragon dragon, float partialTick, float riderScale) {
        State state = new State();
        extractRenderState(dragon, state, partialTick);
        FireDragonPoseModel pose = (FireDragonPoseModel) getModel();
        pose.setupAnim(state);
        var anchor = pose.riderAnchor();
        return riderRenderPosition(state, anchor, riderScale);
    }

    static Vec3 riderRenderPosition(State state, org.joml.Vector3f anchor) {
        return riderRenderPosition(state, anchor, 1);
    }

    static Vec3 riderRenderPosition(State state, org.joml.Vector3f anchor, float riderScale) {
        PoseStack stack = new PoseStack();
        stack.scale(state.scale, state.scale, state.scale);
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees(180 - state.bodyRot)));
        stack.scale(-1, -1, 1);
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(state.flightPitch)));
        stack.translate(0, -1.501F, 0);
        var surface = stack.last().pose().transformPosition(new org.joml.Vector3f(anchor));
        // The player's seated hip is 12 model pixels above its feet. This offset must
        // follow the PLAYER scale, while the surface follows the uncapped DRAGON render scale.
        return new Vec3(state.x + surface.x, state.y + surface.y - 0.75F * riderScale, state.z + surface.z);
    }
}
