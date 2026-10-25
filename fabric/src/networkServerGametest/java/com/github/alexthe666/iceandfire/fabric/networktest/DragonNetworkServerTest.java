package com.github.alexthe666.iceandfire.fabric.networktest;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.net.InetAddress;
import java.util.UUID;
import java.util.Map;

/** Standalone native GameTestServer, bound only to loopback, with two real rendering clients. */
public class DragonNetworkServerTest {
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 6000, padding = 100, skyAccess = true)
    public void twoIndependentClientsRespectOwnershipAndObserveServerUpdates(GameTestHelper h) throws Exception {
        var server = h.getLevel().getServer();
        int port = Integer.parseInt(System.getProperty("iaf.network.port"));
        server.setUsesAuthentication(false); // Local development profiles; never enabled in a distributed mod.
        server.getConnection().startTcpServerListener(InetAddress.getByName("127.0.0.1"), port);
        for (int x = 1; x < 46; x++) for (int z = 1; z < 46; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        require(server.getMaxPlayers() == 2, "This isolated test server must expose exactly two slots");
        NetworkTestFiles.write("ready", Integer.toString(port));
        h.onEachTick(new Runnable() {
            int tick, phaseTick;
            String phase = "JOIN";
            ServerPlayer owner, guest;
            FireDragon dragon;
            double startY;
            boolean finished;
            private void phase(String next) { phase = next; phaseTick = tick; NetworkTestFiles.write("phase", next); }
            @Override public void run() {
                if (finished) return;
                // Native GameTestServer normally runs as fast as possible. Keep this network fixture near 20TPS.
                try { Thread.sleep(50); } catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IllegalStateException(error); }
                tick++;
                if (phase.equals("JOIN")) {
                    String a = NetworkTestFiles.read("owner-uuid"), b = NetworkTestFiles.read("guest-uuid");
                    if (a.isEmpty() || b.isEmpty()) return;
                    require(!a.equals(b), "Two distinct actual network players are required");
                    owner = server.getPlayerList().getPlayer(UUID.fromString(a));
                    guest = server.getPlayerList().getPlayer(UUID.fromString(b));
                    if (owner == null || guest == null) return;
                    Vec3 at = h.absoluteVec(new Vec3(24.5, 2, 24.5));
                    owner.setGameMode(GameType.CREATIVE); guest.setGameMode(GameType.CREATIVE);
                    owner.teleportTo(at.x, at.y, at.z); guest.teleportTo(at.x + 5, at.y, at.z);
                    DragonCombatConfig.current.dragonFlightSpeedMod = 0.5;
                    dragon = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
                    for (int day = 0; day < 50; day++) dragon.growOneDay();
                    dragon.setOwner(owner); dragon.setTame(true, false); dragon.setSilent(true); dragon.setHunger(100);
                    dragon.setHealth(dragon.getMaxHealth()); dragon.setPos(at); h.getLevel().addFreshEntity(dragon);
                    require(owner.startRiding(dragon), "Actual owner mounts the server dragon");
                    startY = dragon.getY(); NetworkTestFiles.write("dragon-uuid", dragon.getUUID().toString()); phase("GUEST_CHECK");
                    return;
                }
                if (!phase.equals("DISCONNECT")) {
                require(owner.getVehicle() == dragon && dragon.getControllingPassenger() == owner, "Only the actual owner remains the pilot");
                require(guest.getVehicle() == null, "Guest interaction cannot mount the owned dragon");
                require(dragon.isOwnedBy(owner) && !dragon.isOwnedBy(guest), "Native ownership is unchanged");
                }
                if (phase.equals("GUEST_CHECK") && !NetworkTestFiles.read("guest-forgery-sent").isEmpty() && tick - phaseTick > 25) {
                    require(!dragon.flying() && dragon.breathProgress == 0, "Unridden guest control packets cannot launch or breathe fire"); phase("TAKEOFF");
                } else if (phase.equals("TAKEOFF") && !NetworkTestFiles.read("owner-airborne").isEmpty() && !NetworkTestFiles.read("guest-airborne").isEmpty()) {
                    require(dragon.flying() && dragon.getY() > startY + 0.25, "Owner input produces actual server flight observed by both clients");
                    dragon.setMale(true); DragonCombatConfig.current.dragonFlightSpeedMod = 0.25;
                    dragon.dragonInventory.setItem(1, new net.minecraft.world.item.ItemStack(com.github.alexthe666.iceandfire.fabric.dragon.DragonArmorItem.ITEMS.get(0)));
                    dragon.dragonInventory.setItem(4, new net.minecraft.world.item.ItemStack(com.github.alexthe666.iceandfire.fabric.dragon.DragonArmorItem.ITEMS.get(3)));
                    phase("UPDATE");
                } else if (phase.equals("UPDATE") && !NetworkTestFiles.read("owner-done").isEmpty() && !NetworkTestFiles.read("guest-done").isEmpty()) {
                    require(dragon.male() && dragon.getFlightSpeedModifier() == 0.25 && dragon.armorParts() == 0x1001 && dragon.isAlive(), "Server update and actual entity remain valid");
                    phase("DISCONNECT");
                } else if (phase.equals("DISCONNECT") && !NetworkTestFiles.read("owner-disconnected").isEmpty()
                        && !NetworkTestFiles.read("guest-disconnected").isEmpty() && server.getPlayerList().getPlayers().isEmpty()) {
                    NetworkTestFiles.write("report.json", new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.ofEntries(
                            Map.entry("status", "passed"), Map.entry("serverKind", "standalone native GameTestServer"),
                            Map.entry("independentClientProcesses", 2), Map.entry("loopbackOnly", true), Map.entry("distinctPlayerUUIDs", true),
                            Map.entry("guestMountRejected", true), Map.entry("guestControlsRejected", true),
                            Map.entry("ownerFlightObservedByBoth", true), Map.entry("serverModifierObservedByBoth", true),
                            Map.entry("genderUpdateObservedByBoth", true), Map.entry("armorHeadTailObservedByBoth", true))));
                    finished = true; h.succeed();
                }
            }
        });
    }
}
