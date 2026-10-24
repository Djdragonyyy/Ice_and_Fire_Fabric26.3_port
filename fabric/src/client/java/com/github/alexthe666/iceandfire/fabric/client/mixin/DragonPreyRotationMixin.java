package com.github.alexthe666.iceandfire.fabric.client.mixin;

import com.github.alexthe666.iceandfire.fabric.client.DragonPreyRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Apply the original rider-layer rotation inside the renderer's own pushed pose. */
@Mixin(LivingEntityRenderer.class)
abstract class DragonPreyRotationMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V"))
    private void iceandfire$rotatePrey(LivingEntityRenderState state, PoseStack stack, SubmitNodeCollector collector,
                                      CameraRenderState camera, CallbackInfo ci) {
        var rotation = ((DragonPreyRenderData) state).iceandfire$preyRotation();
        if (rotation != null) stack.mulPose(new org.joml.Matrix4f().rotation(rotation));
    }
}
