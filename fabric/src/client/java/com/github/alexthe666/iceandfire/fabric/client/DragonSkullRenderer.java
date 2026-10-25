package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonSkull;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonSkullData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** RenderDragonSkull's original head subtree, transform and five skeletal skins. */
final class DragonSkullRenderer extends EntityRenderer<DragonSkull, DragonSkullRenderer.State> {
    static final class State extends EntityRenderState {
        DragonSkullData skull = DragonSkullData.DEFAULT;
        float yaw;
        boolean wall;
    }
    static final class HeadModel extends EntityModel<State> {
        private final ModelPart head;
        HeadModel(ModelPart head) { super(head); this.head = head; }
        @Override public void setupAnim(State state) {
            super.setupAnim(state);
            head.xRot = state.wall ? (float) Math.toRadians(50) : 0;
            head.yRot = head.zRot = 0;
        }
    }
    private final HeadModel model;
    DragonSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
        var original = TabulaLoader.loadModel(context.getResourceManager(), IafContent.id("models/tabula/firedragon/firedragon_ground.tbl"));
        model = new HeadModel(original.parts().get("Head"));
    }
    @Override public State createRenderState() { return new State(); }
    @Override protected boolean affectedByCulling(DragonSkull skull) { return false; }
    @Override public void extractRenderState(DragonSkull skull, State state, float partialTick) {
        super.extractRenderState(skull, state, partialTick);
        state.skull = skull.skullData(); state.yaw = skull.dragonYaw(); state.wall = skull.onWall();
    }
    static Identifier texture(int stage) { return IafContent.id("textures/models/firedragon/fire_skeleton_" + Math.clamp(stage, 1, 5) + ".png"); }
    @Override public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        stack.pushPose();
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(-180)));
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.YN.rotationDegrees(-180 - state.yaw)));
        float size = state.skull.renderSize() / 3;
        stack.scale(size, size, size);
        stack.translate(0, state.wall ? -0.24F : -0.12F, state.wall ? 0.4F : 0.5F);
        collector.submitModel(model, state, stack, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(texture(state.skull.stage())),
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0xffffffff, null, state.outlineColor);
        stack.popPose();
        super.submit(state, stack, collector, camera);
    }
}
