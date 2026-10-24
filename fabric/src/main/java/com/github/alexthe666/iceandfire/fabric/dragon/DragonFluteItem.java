package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Original ItemDragonFlute area and cooldown; entity effects are authoritative on the server (LGPL-3.0). */
public final class DragonFluteItem extends Item {
    public DragonFluteItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!stack.is(this) || player.isSpectator() || player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        player.getCooldowns().addCooldown(stack, 60);
        if (level instanceof ServerLevel server) {
            double radius = 16.0 * DragonCombatConfig.current.dragonFluteDistance;
            var area = new AABB(player.getX(), player.getY(), player.getZ(), player.getX() + 1, player.getY() + 1, player.getZ() + 1)
                    .inflate(radius, 256, radius);
            var dragons = server.getEntitiesOfClass(FireDragon.class, area);
            dragons.sort(java.util.Comparator.comparingDouble(player::distanceToSqr));
            dragons.forEach(dragon -> dragon.onHearFlute(player));
            server.playSound(null, player.blockPosition(), IafContent.DRAGON_FLUTE_SOUND, SoundSource.NEUTRAL, 1, 1.75F);
        }
        return InteractionResult.SUCCESS;
    }
}
