package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;

public class DragonRoarClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true); d.setCommand(1); d.ambientSoundTime = -10000;
                for (int day = 0; day < 75; day++) d.growOneDay();
                // The original roar box extends in the positive axes: put the real owner inside it.
                d.setPos(player.position().add(-3, 0, -3)); player.level().addFreshEntity(d); return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon);
            context.runOnClient(client -> {
                if (client.getSoundManager().getSoundEvent(IafContent.DRAGON_FLIGHT.location()) == null)
                    throw new AssertionError("Original flight sound must resolve");
                for (var sound : IafContent.FIRE_ROAR)
                    if (client.getSoundManager().getSoundEvent(sound.location()) == null)
                        throw new AssertionError("Age-specific original roar sound must resolve");
            });
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class);
            for (boolean epic : new boolean[]{false, true}) {
                world.getServer().runOnServer(server -> {
                    var d = (FireDragon) player.level().getEntity(id);
                    d.setNoAi(false); d.roar(epic);
                });
                int animation = epic ? DragonCombatState.EPIC_ROAR : DragonCombatState.ROAR;
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d
                        && d.attackAnimation() == animation && d.attackAnimationTick() >= 12 && d.attackAnimationTick() <= 25
                        && client.player.hasEffect(MobEffects.STRENGTH));
                context.takeScreenshot(epic ? "dragon-epic-roar-0.1.29" : "dragon-roar-0.1.29");
                world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setNoAi(true));
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.attackAnimation() == DragonCombatState.NONE);
            }
            context.setScreen(() -> null);
        }
    }
}
