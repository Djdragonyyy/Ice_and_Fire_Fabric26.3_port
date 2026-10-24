package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Real local vehicle authority, keyboard payload and server position tracking in both fluids. */
public class DragonFluidClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        for (boolean lava : new boolean[]{false, true}) try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> createTankAndMount(player, lava));
            context.waitFor(client -> client.player.getVehicle() instanceof FireDragon d && d.getId() == id
                    && d.getControllingPassenger() == client.player && (lava ? d.isInLava() : d.isInWater()));
            context.runOnClient(client -> client.mouseHandler.grabMouse());
            // Let the initial fluid-entry momentum settle naturally before measuring the controls.
            context.waitTicks(24);
            try {
                double start = context.computeOnClient(client -> client.player.getVehicle().getY());
                double initialVelocity = context.computeOnClient(client -> client.player.getVehicle().getDeltaMovement().y);
                context.getInput().holdKey(options -> options.keyJump); context.waitTicks(6);
                context.runOnClient(client -> {
                    var d = (FireDragon) client.player.getVehicle();
                    require(d.getRiddenInput(client.player, Vec3.ZERO).y > 0,
                            "UP key reaches native ridden input: grabbed=" + client.mouseHandler.isMouseGrabbed() + " key=" + client.options.keyJump.isDown()
                                    + " flying=" + d.flying() + " water=" + d.isInWater());
                });
                context.waitTicks(6);
                context.getInput().releaseKey(options -> options.keyJump); context.waitTicks(3);
                double up = context.computeOnClient(client -> client.player.getVehicle().getY());
                double serverUp = world.getServer().computeOnServer(server -> player.getVehicle().getY());
                require(up > start + 0.03 && serverUp > start + 0.01, "Native UP moves client-owned vehicle and server tracks rise in " + (lava ? "lava" : "water") + ": start=" + start + " client=" + up + " server=" + serverUp + " initialV=" + initialVelocity + " finalV=" + context.computeOnClient(client -> client.player.getVehicle().getDeltaMovement().y));
                context.runOnClient(client -> require(!((FireDragon) client.player.getVehicle()).flying(), "Short swim UP does not trigger takeoff"));
                context.getInput().holdKey(InputConstants.KEY_X); context.waitTicks(12);
                context.getInput().releaseKey(InputConstants.KEY_X); context.waitTicks(3);
                double down = context.computeOnClient(client -> client.player.getVehicle().getY());
                double serverDown = world.getServer().computeOnServer(server -> player.getVehicle().getY());
                require(down < up - 0.05 && serverDown < serverUp - 0.03, "Native DOWN payload controls both client and tracked server descent");
                if (lava) {
                    context.getInput().holdKey(options -> options.keyJump); context.getInput().holdKey(InputConstants.KEY_X);
                    context.waitTicks(6);
                    context.runOnClient(client -> {
                        var d = (FireDragon) client.player.getVehicle();
                        require(!d.flying() && Math.abs(d.getDeltaMovement().y) < 0.03, "Both lava controls damp vertical drift without launching");
                    });
                }
            } finally {
                context.getInput().releaseKey(options -> options.keyJump); context.getInput().releaseKey(InputConstants.KEY_X);
            }
        }
    }
    private static int createTankAndMount(ServerPlayer player, boolean lava) {
        player.setGameMode(GameType.CREATIVE);
        var level = player.level(); var base = player.blockPosition().offset(-8, -1, -8);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            level.setBlockAndUpdate(base.offset(x, 0, z), Blocks.STONE.defaultBlockState());
            for (int y = 1; y <= 12; y++) {
                var block = x == 0 || x == 15 || z == 0 || z == 15 ? Blocks.STONE
                        : y <= 9 ? lava ? Blocks.LAVA : Blocks.WATER : Blocks.AIR;
                level.setBlockAndUpdate(base.offset(x, y, z), block.defaultBlockState());
            }
        }
        var d = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        d.setOwner(player); d.setTame(true, false); d.setSilent(true);
        for (int day = 0; day < 50; day++) d.growOneDay();
        d.setHunger(100); d.setPos(Vec3.atBottomCenterOf(base.offset(8, 3, 8))); level.addFreshEntity(d);
        require(player.startRiding(d), "Real connected owner mounts the dragon in the tank"); return d.getId();
    }
}
