package com.github.alexthe666.iceandfire.fabric.client.testmixin;

import com.github.alexthe666.iceandfire.fabric.client.*;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Test observer for snapshots actually extracted by the inventory pipeline. */
@Mixin(DragonInventoryPassengers.class)
abstract class InventorySnapshotTestMixin {
    @Inject(method = "capture", at = @At("HEAD"))
    private static void before(FireDragon dragon, FireDragonRenderer.State state, CallbackInfo ci) {
        DragonInventoryClientTest.before(dragon);
    }
    @Inject(method = "capture", at = @At("TAIL"))
    private static void after(FireDragon dragon, FireDragonRenderer.State state, CallbackInfo ci) {
        DragonInventoryClientTest.after(dragon, state);
    }
}
