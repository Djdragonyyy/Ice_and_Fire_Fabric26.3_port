package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractChestBlock;

/** Original ServerEvents chest interaction/break alarm; including the original three metal piles on break (LGPL-3.0). */
public final class DragonChestGuard {
    public static void initialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level instanceof ServerLevel server && level.getBlockState(hit.getBlockPos()).getBlock() instanceof AbstractChestBlock)
                alarm(server, player);
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel server && (state.getBlock() instanceof AbstractChestBlock
                    || com.github.alexthe666.iceandfire.fabric.treasure.IafTreasure.isPile(state.getBlock()))) alarm(server, player);
            return true;
        });
    }

    private static void alarm(ServerLevel level, Player player) {
        if (player.isCreative()) return;
        double radius = DragonCombatConfig.current.dragonGoldSearchLength;
        // The original region surrounds the player, not the interacted chest, and has no LOS gate.
        for (var dragon : level.getEntitiesOfClass(FireDragon.class, player.getBoundingBox().inflate(radius))) {
            if (dragon.isTame() || dragon.corpse() || !dragon.isAlive() || dragon.isOwnedBy(player)) continue;
            dragon.setDragonSleeping(false);
            dragon.setOrderedToSit(false);
            dragon.setTarget(player);
        }
    }

    private DragonChestGuard() {}
}
