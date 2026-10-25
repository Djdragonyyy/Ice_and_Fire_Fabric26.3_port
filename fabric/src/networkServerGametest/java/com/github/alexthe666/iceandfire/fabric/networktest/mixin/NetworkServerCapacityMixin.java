package com.github.alexthe666.iceandfire.fabric.networktest.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Capacity setting for this opt-in, loopback-only test fixture; not shipped with the mod. */
@Mixin(GameTestServer.class)
abstract class NetworkServerCapacityMixin {
    @Inject(method = "getMaxPlayers", at = @At("HEAD"), cancellable = true)
    private void twoTestSlots(CallbackInfoReturnable<Integer> result) { result.setReturnValue(2); }
}
