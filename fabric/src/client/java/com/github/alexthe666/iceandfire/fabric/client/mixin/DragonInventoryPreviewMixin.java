package com.github.alexthe666.iceandfire.fabric.client.mixin;

import com.github.alexthe666.iceandfire.fabric.client.DragonInventoryPassengers;
import com.github.alexthe666.iceandfire.fabric.client.FireDragonRenderer;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native inventory extraction has no passenger callback; world extraction is unaffected. */
@Mixin(InventoryScreen.class)
abstract class DragonInventoryPreviewMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    private static void iceandfire$inventoryPassengers(LivingEntity entity, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity instanceof FireDragon dragon && cir.getReturnValue() instanceof FireDragonRenderer.State state)
            DragonInventoryPassengers.capture(dragon, state);
    }
}
