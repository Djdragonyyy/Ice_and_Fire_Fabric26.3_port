package com.github.alexthe666.iceandfire.fabric.client.mixin;

import com.github.alexthe666.iceandfire.fabric.client.DragonPreyRenderData;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
abstract class DragonPreyStateMixin implements DragonPreyRenderData {
    @Unique private Quaternionf iceandfire$preyRotation;
    @Override public Quaternionf iceandfire$preyRotation() { return iceandfire$preyRotation; }
    @Override public void iceandfire$setPreyRotation(Quaternionf rotation) { iceandfire$preyRotation = rotation; }
}
