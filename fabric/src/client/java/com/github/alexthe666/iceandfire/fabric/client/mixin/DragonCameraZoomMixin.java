package com.github.alexthe666.iceandfire.fabric.client.mixin;

import com.github.alexthe666.iceandfire.fabric.client.DragonCameraView;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
abstract class DragonCameraZoomMixin {
    @ModifyArg(method = "alignWithEntity(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"), index = 0)
    private float iceandfire$dragonCameraDistance(float distance) {
        return DragonCameraView.zoomDistance(((Camera) (Object) this).entity(), distance);
    }
}
