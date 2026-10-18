package com.github.alexthe666.iceandfire.fabric.client.mixin;

import com.github.alexthe666.iceandfire.fabric.client.FireDragonRenderer;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric exposes no callback for replacing an existing entity's passenger render position. */
@Mixin(EntityRenderer.class)
abstract class DragonRiderRenderMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void iceandfire$attachRider(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!(entity.getVehicle() instanceof FireDragon dragon) || dragon.getControllingPassenger() != entity) return;
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon) instanceof FireDragonRenderer renderer)) return;
        var position = renderer.riderRenderPosition(dragon, partialTick);
        state.x = position.x;
        state.y = position.y;
        state.z = position.z;
        // Vehicle interpolation was incorporated above, in the same frame as the animated neck.
        state.passengerOffset = null;
    }
}
