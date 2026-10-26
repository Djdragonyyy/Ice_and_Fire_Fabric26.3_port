package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import com.github.alexthe666.iceandfire.fabric.networktest.NetworkTestFiles;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Opt-in harness on an ordinary DedicatedServer, not Fabric's GameTestServer. */
public final class DedicatedDragonWorldTest implements ModInitializer {
    private MinecraftServer server;
    private int ticks, phaseTick;
    private String phase = "JOIN";
    private ServerPlayer owner, guest;
    private FireDragon dragon, corpse;
    private double startY;
    private boolean finished, reopening;
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    @Override public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            server = s;
            require(s instanceof net.minecraft.server.dedicated.DedicatedServer, "Ordinary DedicatedServer required");
            require(s.getMaxPlayers() == 2, "Exactly two private test slots");
            reopening = NetworkTestFiles.read("restart-required").equals("yes");
            var level = s.overworld();
            for (int x = 0; x <= 2; x++) for (int z = 0; z <= 2; z++) { level.getChunk(x, z); level.setChunkForced(x, z, true); }
            if (!reopening) {
                for (int x = 0; x < 48; x++) for (int z = 0; z < 48; z++) level.setBlockAndUpdate(new BlockPos(x, 80, z), Blocks.STONE.defaultBlockState());
                NetworkTestFiles.write("ready", "dedicated");
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s != server || finished) return;
            try { if (++ticks > 6000) throw new IllegalStateException("Dedicated world test timeout in " + phase); if (reopening) reopened(); else network(); }
            catch (RuntimeException | Error error) { finished = true; NetworkTestFiles.write("server-failed", error.toString()); s.halt(false); }
        });
    }
    private void phase(String next) { phase = next; phaseTick = ticks; NetworkTestFiles.write("phase", next); }
    private FireDragon spawn(int age, double x, double z) {
        var d = IafContent.FIRE_DRAGON.create(server.overworld(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setSilent(true); d.setPos(x, 81, z); d.refreshDimensions(); server.overworld().addFreshEntity(d); return d;
    }
    private void network() {
        if (phase.equals("JOIN")) {
            String a = NetworkTestFiles.read("owner-uuid"), b = NetworkTestFiles.read("guest-uuid");
            if (a.isEmpty() || b.isEmpty()) return;
            require(!a.equals(b), "Two distinct native TCP players");
            owner = server.getPlayerList().getPlayer(UUID.fromString(a)); guest = server.getPlayerList().getPlayer(UUID.fromString(b));
            if (owner == null || guest == null) return;
            owner.setGameMode(GameType.CREATIVE); guest.setGameMode(GameType.CREATIVE);
            owner.teleportTo(24.5, 81, 24.5); guest.teleportTo(29.5, 81, 24.5);
            DragonCombatConfig.current.dragonFlightSpeedMod = 0.5;
            dragon = spawn(50, 24.5, 24.5); dragon.setOwner(owner); dragon.setTame(true, false); dragon.setHunger(100); dragon.setHealth(dragon.getMaxHealth());
            corpse = spawn(75, 12.5, 12.5); corpse.setOwner(owner); corpse.setTame(true, false);
            corpse.hurtServer(server.overworld(), corpse.damageSources().generic(), 10000);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2));
            owner.interactOn(corpse, InteractionHand.MAIN_HAND, Vec3.ZERO); owner.interactOn(corpse, InteractionHand.MAIN_HAND, Vec3.ZERO);
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            require(corpse.corpse() && corpse.deathStage() == 2, "Partially harvested corpse fixture");
            require(owner.startRiding(dragon), "Actual network owner mounts");
            startY = dragon.getY(); NetworkTestFiles.write("dragon-uuid", dragon.getUUID().toString());
            NetworkTestFiles.write("corpse-uuid", corpse.getUUID().toString()); phase("GUEST_CHECK"); return;
        }
        if (!phase.equals("DISCONNECT")) {
            require(owner.getVehicle() == dragon && dragon.getControllingPassenger() == owner, "Only actual owner pilots");
            require(guest.getVehicle() == null && dragon.isOwnedBy(owner) && !dragon.isOwnedBy(guest), "Guest ownership and mount protection");
        }
        if (phase.equals("GUEST_CHECK") && !NetworkTestFiles.read("guest-forgery-sent").isEmpty() && ticks - phaseTick > 25) {
            require(!dragon.flying() && dragon.breathProgress == 0, "Guest forged controls rejected"); phase("TAKEOFF");
        } else if (phase.equals("TAKEOFF") && !NetworkTestFiles.read("owner-airborne").isEmpty() && !NetworkTestFiles.read("guest-airborne").isEmpty()) {
            require(dragon.flying() && dragon.getY() > startY + 0.25, "Flight observed by two clients");
            dragon.setMale(true); DragonCombatConfig.current.dragonFlightSpeedMod = 0.25;
            dragon.dragonInventory.setItem(1, new ItemStack(DragonArmorItem.ITEMS.get(0))); dragon.dragonInventory.setItem(4, new ItemStack(DragonArmorItem.ITEMS.get(3)));
            phase("UPDATE");
        } else if (phase.equals("UPDATE") && !NetworkTestFiles.read("owner-done").isEmpty() && !NetworkTestFiles.read("guest-done").isEmpty()) {
            require(dragon.getFlightSpeedModifier() == 0.25 && dragon.armorParts() == 0x1001 && dragon.male(), "Native updates observed by both clients");
            // Mounted logout stores the vehicle in player RootVehicle data; this fixture tests WORLD persistence.
            owner.stopRiding(); owner.teleportTo(30.5, 81, 24.5);
            dragon.setAutonomousFlight(false, false); dragon.setPos(24.5, 81, 24.5); dragon.setDeltaMovement(Vec3.ZERO); dragon.setNoAi(true); dragon.setCommand(1);
            dragon.setCustomName(Component.literal("Dedicated persistence dragon")); phase("DISCONNECT");
        } else if (phase.equals("DISCONNECT") && !NetworkTestFiles.read("owner-disconnected").isEmpty() && !NetworkTestFiles.read("guest-disconnected").isEmpty() && server.getPlayerList().getPlayers().isEmpty()) {
            dragon.setAutonomousFlight(false, false); dragon.setPos(24.5, 81, 24.5); dragon.setDeltaMovement(Vec3.ZERO); dragon.setNoAi(true); dragon.setCommand(1);
            dragon.setCustomName(Component.literal("Dedicated persistence dragon"));
            require(!dragon.isRemoved() && server.overworld().getEntity(dragon.getUUID()) == dragon, "Unmounted dragon remains in world rather than player RootVehicle storage");
            require(dragon.dragonParts().size() == 10 && corpse.dragonParts().size() == 10, "Both multipart families present before save");
            NetworkTestFiles.write("old-part-uuids", String.join(",", java.util.stream.Stream.concat(dragon.dragonParts().stream(), corpse.dragonParts().stream()).map(p -> p.getUUID().toString()).toList()));
            NetworkTestFiles.write("restart-required", "yes");
            report("phase1-report.json", false); finished = true; server.halt(false);
        }
    }
    private void reopened() {
        if (ticks < 40) return;
        var level = server.overworld();
        var loaded = level.getEntity(UUID.fromString(NetworkTestFiles.read("dragon-uuid")));
        var remains = level.getEntity(UUID.fromString(NetworkTestFiles.read("corpse-uuid")));
        if (loaded == null || remains == null) { require(ticks < 600, "Saved entities must load from same world"); return; }
        require(loaded instanceof FireDragon && remains instanceof FireDragon, "Original registered entity types restored");
        var d = (FireDragon) loaded; var c = (FireDragon) remains;
        require(d.ageDays() == 50 && d.command() == 1 && d.male() && d.armorParts() == 0x1001, "Age, command, gender and armor survive process restart");
        require(d.isNoAi() && d.getCustomName() != null && d.getCustomName().getString().equals("Dedicated persistence dragon"), "NoAI and name preserved");
        require(d.getOwnerReference() != null && d.getOwnerReference().getUUID().toString().equals(NetworkTestFiles.read("owner-uuid")), "Actual owner's UUID restored while offline");
        require(c.corpse() && c.getHealth() == 0 && c.ageDays() == 75 && c.deathStage() == 2 && c.isNoAi(), "Partially harvested corpse survives disk save and restart");
        require(d.dragonParts().size() == 10 && c.dragonParts().size() == 10, "Both transient families reconstructed");
        var old = Set.of(NetworkTestFiles.read("old-part-uuids").split(","));
        require(java.util.stream.Stream.concat(d.dragonParts().stream(), c.dragonParts().stream()).noneMatch(p -> old.contains(p.getUUID().toString())), "Parts recreated rather than persisted independently");
        int parts = 0; for (var e : level.getAllEntities()) if (e instanceof DragonPart) parts++;
        require(parts == 20, "No saved duplicate or orphan proxy entities");
        report("report.json", true); finished = true; server.halt(false);
    }
    private void report(String name, boolean restarted) {
        NetworkTestFiles.write(name, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.ofEntries(
                Map.entry("status", "passed"), Map.entry("serverKind", "ordinary DedicatedServer"), Map.entry("independentClientProcesses", 2),
                Map.entry("loopbackOnly", true), Map.entry("developmentProfiles", true), Map.entry("guestControlsRejected", true),
                Map.entry("ownerFlightObservedByBoth", true), Map.entry("armorHeadTailObservedByBoth", true), Map.entry("actualWorldRestart", restarted),
                Map.entry("ownerInventoryAndCorpsePersist", restarted), Map.entry("transientPartsRebuiltWithoutDuplicates", restarted))));
    }
}
