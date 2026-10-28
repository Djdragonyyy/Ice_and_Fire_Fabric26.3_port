package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

/** Fire-dragon branch of original LayerDragonRider (LGPL-3.0), before prey's own renderer. */
final class DragonPreyAttachment {
    private DragonPreyAttachment() {}
    static Matrix4f transform(FireDragonPoseModel model, FireDragonRenderer.State state,
                              float width, float height, float preyYaw, boolean upright, boolean horse) {
        return transform(model, state, width, height, preyYaw, upright, horse, state.scale);
    }
    static Matrix4f transform(FireDragonPoseModel model, FireDragonRenderer.State state,
                              float width, float height, float preyYaw, boolean upright, boolean horse, float scale) {
        PoseStack stack = new PoseStack();
        stack.scale(scale, scale, scale);
        rotate(stack, Axis.YP, 180 - state.bodyRot);
        stack.scale(-1, -1, 1);
        rotate(stack, Axis.XP, state.flightPitch);
        stack.translate(0, -1.501F, 0);
        float tick = state.attackTick;
        if (tick == 0 || tick >= 15) model.translateToPreyBody(stack);
        if (tick == 0 || tick >= 15 || state.flying) {
            model.translateToPreyHead(stack);
            if (upright) {
                stack.translate(-0.15F * height, 0.1F * scale - 0.1F * height, -0.1F * scale - 0.1F * width);
                rotate(stack, Axis.ZP, 90); rotate(stack, Axis.YP, 45);
            } else {
                stack.translate((horse ? -0.08F : -0.15F) * width, 0.1F * scale - 0.15F * width, -0.1F * scale - 0.1F * width);
                rotate(stack, Axis.XP, -90);
            }
        } else stack.translate(0, 0.555F * scale, -0.5F * scale);
        rotate(stack, Axis.ZP, 180); rotate(stack, Axis.YP, preyYaw + 180);
        stack.scale(1 / scale, 1 / scale, 1 / scale);
        stack.translate(0, -0.25F, 0);
        return new Matrix4f(stack.last().pose());
    }
    private static void rotate(PoseStack stack, Axis axis, float degrees) {
        stack.mulPose(new Matrix4f().rotation(axis.rotationDegrees(degrees)));
    }
}
