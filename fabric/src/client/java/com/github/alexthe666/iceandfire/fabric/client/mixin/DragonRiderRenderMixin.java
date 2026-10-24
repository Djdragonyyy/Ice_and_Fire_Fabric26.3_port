package com.github.alexthe666.iceandfire.fabric.client.mixin;

import com.github.alexthe666.iceandfire.fabric.client.FireDragonRenderer;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric exposes no callback for replacing an existing entity's passenger render position. */
@Mixin(EntityRenderer.class)
abstract class DragonRiderRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void iceandfire$preyVisibility(Entity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                         double x, double y, double z, float partialTick, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getVehicle() instanceof FireDragon dragon && dragon.grippedPrey() == entity) {
            // Original LayerDragonRider rendered prey whenever the carrier was rendered.
            cir.setReturnValue(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon)
                    .shouldRender(dragon, frustum, x, y, z, partialTick));
        }
    }
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void iceandfire$attachRider(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        if (state instanceof com.github.alexthe666.iceandfire.fabric.client.DragonPreyRenderData data) data.iceandfire$setPreyRotation(null);
        if (!(entity.getVehicle() instanceof FireDragon dragon)) return;
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon) instanceof FireDragonRenderer renderer)) return;
        if (entity instanceof net.minecraft.world.entity.Mob prey && dragon.grippedPrey() == prey) {
            var pose = renderer.preyRenderPose(dragon, prey, partialTick);
            state.x = pose.position().x; state.y = pose.position().y; state.z = pose.position().z;
            ((com.github.alexthe666.iceandfire.fabric.client.DragonPreyRenderData) state).iceandfire$setPreyRotation(pose.rotation());
            state.passengerOffset = null;
            return;
        }
        if (dragon.getControllingPassenger() != entity) return;
        float riderScale = entity instanceof LivingEntity living ? living.getScale() : 1;
        var position = renderer.riderRenderPosition(dragon, partialTick, riderScale);
        state.x = position.x;
        state.y = position.y;
        state.z = position.z;
        // Vehicle interpolation was incorporated above, in the same frame as the animated neck.
        state.passengerOffset = null;
    }
}
