package com.github.alexthe666.iceandfire.fabric.mixin;

import com.github.alexthe666.iceandfire.fabric.dragon.DragonPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Resolve native weapon callbacks to the living parent after part-scaled damage was applied. */
@Mixin(Player.class)
abstract class DragonPartWeaponMixin {
    @ModifyVariable(method = "itemAttackInteraction", at = @At("HEAD"), argsOnly = true)
    private Entity iceandfire$weaponTarget(Entity target) {
        return target instanceof DragonPart part && part.parent() != null ? part.parent() : target;
    }
}
