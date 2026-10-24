package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Frozen saved sleep state exercises the real sleeping skin/pose and eye-layer suppression. */
public class DragonSleepClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true);
                for (int i = 0; i < 50; i++) d.growOneDay();
                d.setPos(player.position().add(3, 0, 0));
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.level().registryAccess());
                d.saveWithoutId(output);
                output.putBoolean("Sleeping", true);
                d.load(TagValueInput.create(ProblemReporter.DISCARDING, player.level().registryAccess(), output.buildResult()));
                player.level().addFreshEntity(d);
                return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.dragonSleeping());
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class);
            context.waitTicks(45);
            context.takeScreenshot("dragon-sleep-pose-0.1.18");
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setCommand(1));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && !d.dragonSleeping());
            context.waitTicks(45);
            context.takeScreenshot("dragon-awake-eyes-0.1.18");
            context.setScreen(() -> null);
        }
    }
}
