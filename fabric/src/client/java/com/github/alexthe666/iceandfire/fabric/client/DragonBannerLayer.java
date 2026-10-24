package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Original BodyUpper attachment; vanilla item rendering retains colors and pattern components. */
final class DragonBannerLayer extends RenderLayer<FireDragonRenderer.State, EntityModel<FireDragonRenderer.State>> {
    DragonBannerLayer(FireDragonRenderer parent) { super(parent); }

    @Override public void submit(PoseStack stack, SubmitNodeCollector collector, int light,
                                 FireDragonRenderer.State state, float yRot, float xRot) {
        if (state.isInvisible || state.banner.isEmpty() || state.bannerDragonScale <= 0) return;
        stack.pushPose();
        applyAttachment((FireDragonPoseModel) getParentModel(), state.bannerDragonScale, stack);
        state.banner.submit(stack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        stack.popPose();
    }

    static void applyAttachment(FireDragonPoseModel model, float dragonScale, PoseStack stack) {
        model.translateToBody(stack);
        stack.translate(0, -0.2F, 0.4F);
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(180)));
        float inverse = 1 / dragonScale;
        stack.scale(inverse, inverse, inverse);
    }
}
