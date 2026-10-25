package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Actual connected owner receives server speed updates and drives native walking. */
public class DragonWalkingClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        var old = DragonCombatConfig.current;
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var cfg = new DragonCombatConfig(); cfg.dragonFlightSpeedMod = 0.5; DragonCombatConfig.current = cfg;
                player.setGameMode(GameType.CREATIVE);
                var base = player.blockPosition().offset(-24, -1, -24);
                for (int x = 0; x < 48; x++) for (int z = 0; z < 48; z++) {
                    player.level().setBlockAndUpdate(base.offset(x, 0, z), Blocks.STONE.defaultBlockState());
                    for (int y = 1; y < 10; y++) player.level().setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                for (int i = 0; i < 50; i++) d.growOneDay();
                d.setOwner(player); d.setTame(true, false); d.setSilent(true); d.setHunger(100);
                d.setPos(Vec3.atBottomCenterOf(base.offset(24, 1, 24))); player.level().addFreshEntity(d);
                require(player.startRiding(d), "Connected owner mounts on actual stone floor"); return d.getId();
            });
            context.waitFor(client -> client.player.getVehicle() instanceof FireDragon d && d.getId() == id && d.onGround()
                    && Math.abs(d.getFlightSpeedModifier() - 0.5) < 1.0E-6);
            context.runOnClient(client -> client.mouseHandler.grabMouse());
            try {
                Vec3 start = context.computeOnClient(client -> client.player.getVehicle().position());
                context.getInput().holdKey(options -> options.keyUp); context.waitTicks(16);
                context.getInput().releaseKey(options -> options.keyUp); context.waitTicks(4);
                Vec3 moved = context.computeOnClient(client -> client.player.getVehicle().position());
                Vec3 serverMoved = world.getServer().computeOnServer(server -> player.getVehicle().position());
                require(moved.subtract(start).horizontalDistance() > 0.15 && serverMoved.subtract(start).horizontalDistance() > 0.1,
                        "Native ground motion reaches both real client and server");
                world.getServer().runOnServer(server -> DragonCombatConfig.current.dragonFlightSpeedMod = 0);
                context.waitFor(client -> ((FireDragon) client.player.getVehicle()).getFlightSpeedModifier() == 0);
                context.waitTicks(15);
                Vec3 stopped = context.computeOnClient(client -> client.player.getVehicle().position());
                context.getInput().holdKey(options -> options.keyUp); context.waitTicks(12);
                context.getInput().releaseKey(options -> options.keyUp); context.waitTicks(3);
                require(context.computeOnClient(client -> client.player.getVehicle().position().subtract(stopped).horizontalDistance()) < 0.02,
                        "Server's zero modifier disables driven horizontal ground motion");
                world.getServer().runOnServer(server -> DragonCombatConfig.current.dragonFlightSpeedMod = 2);
                context.waitFor(client -> ((FireDragon) client.player.getVehicle()).getFlightSpeedModifier() == 2);
                context.runOnClient(client -> {
                    var d = (FireDragon) client.player.getVehicle();
                    require(d.getRiddenSpeed(client.player) > 0.8 && !d.flying(), "Server maximum reaches native client speed without takeoff");
                });
                world.getServer().runOnServer(server -> DragonCombatConfig.current.dragonFlightSpeedMod = 1);
                context.waitFor(client -> ((FireDragon) client.player.getVehicle()).getFlightSpeedModifier() == 1);
            } finally { context.getInput().releaseKey(options -> options.keyUp); }
        } finally { DragonCombatConfig.current = old; }
    }
}
