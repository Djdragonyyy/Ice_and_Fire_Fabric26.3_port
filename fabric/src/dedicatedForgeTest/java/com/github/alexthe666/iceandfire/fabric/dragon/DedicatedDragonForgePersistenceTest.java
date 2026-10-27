package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import com.github.alexthe666.iceandfire.fabric.forge.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.level.Level;
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
public final class DedicatedDragonForgePersistenceTest implements ModInitializer {
    private static final BlockPos FORGE_POS = new BlockPos(36, 82, 36);
    private DragonForgeCore forge;
    private FireDragon smith;
    private int savedCook, savedInput, savedBlood, savedOutput, forgePauseAt = -1;
    private boolean forgeLoaded, forgePauseDone, forgeVerified;
    private MinecraftServer server;
    private int ticks, phaseTick;
    private String phase = "JOIN";
    private ServerPlayer owner, guest;
    private FireDragon dragon, corpse;
    private double startY;
    private boolean finished, reopening;
    private boolean positionedAfterRejoin;
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    @Override public void onInitialize() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyGenerated) -> {
            if (!level.dimension().equals(Level.OVERWORLD) || !NetworkTestFiles.read("restart-required").equals("yes")
                    || chunk.getPos().x() != 2 || chunk.getPos().z() != 2) return;
            require(chunk.getBlockEntity(FORGE_POS) instanceof DragonForgeCore, "Actual forge block entity restored from disk");
            forge = (DragonForgeCore) chunk.getBlockEntity(FORGE_POS);
            savedCook = Integer.parseInt(NetworkTestFiles.read("forge-saved-cook"));
            savedInput = Integer.parseInt(NetworkTestFiles.read("forge-saved-input"));
            savedBlood = Integer.parseInt(NetworkTestFiles.read("forge-saved-blood"));
            savedOutput = Integer.parseInt(NetworkTestFiles.read("forge-saved-output"));
            require(forge.cookTime() == savedCook && sameForgeInventory(), "Partial cook and three slots survive actual chunk disk loading");
            forgeLoaded = true;
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!level.dimension().equals(Level.OVERWORLD) || !NetworkTestFiles.read("restart-required").equals("yes")
                    || !entity.getUUID().toString().equals(NetworkTestFiles.read("forge-smith-uuid"))) return;
            require(entity instanceof FireDragon, "Actual smith type restored"); smith = (FireDragon) entity;
            require(smith.ageDays() == 25 && smith.isTame() && !smith.isNoAi(), "Smith UUID, age and live AI persist");
            smith.setNoAi(true); // Isolate cold saved progress before allowing fresh autonomous heat.
        });
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
            } else {
                DragonCombatConfig.current.dragonFlightSpeedMod = 0.25;
                phase("REJOIN"); NetworkTestFiles.write("ready-rejoin", "yes");
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s != server || finished) return;
            try { if (++ticks > 6000) throw new IllegalStateException("Dedicated world test timeout in " + phase); if (reopening) { tickForgeRestart(); if (smith != null) reopened(); } else network(); }
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
            require(owner.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) == 2, "Nonempty actual harvested owner inventory fixture");
            createForge();
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
            // Deliberately remain mounted: the native player RootVehicle record must restore this dragon.
            dragon.setCustomName(Component.literal("Dedicated persistence dragon")); phase("DISCONNECT");
        } else if (phase.equals("DISCONNECT") && !NetworkTestFiles.read("owner-disconnected").isEmpty() && !NetworkTestFiles.read("guest-disconnected").isEmpty() && server.getPlayerList().getPlayers().isEmpty()) {
            if (!forgeReadyToSave()) return;
            require(dragon.isRemoved() && dragon.getRemovalReason() == net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_WITH_PLAYER,
                    "Mounted logout unloads the root vehicle with the player");
            require(dragon.dragonParts().size() == 10 && corpse.dragonParts().size() == 10, "Both multipart families present before save");
            NetworkTestFiles.write("old-part-uuids", String.join(",", java.util.stream.Stream.of(dragon, corpse, smith).flatMap(actor -> actor.dragonParts().stream()).map(p -> p.getUUID().toString()).toList()));
            NetworkTestFiles.write("restart-required", "yes");
            report("phase1-report.json", false); finished = true; server.halt(false);
        }
    }
    private void reopened() {
        if (ticks < 40) return;
        if (phase.equals("REJOIN_DISCONNECT")) {
            if (server.getPlayerList().getPlayers().isEmpty() && NetworkTestFiles.read("owner-rejoin-disconnected").equals("yes")
                    && NetworkTestFiles.read("guest-rejoin-disconnected").equals("yes")) {
                if (!forgeVerified) return;
                report("report.json", true); finished = true; server.halt(false);
            }
            return;
        }
        var level = server.overworld();
        owner = server.getPlayerList().getPlayer(UUID.fromString(NetworkTestFiles.read("owner-uuid")));
        guest = server.getPlayerList().getPlayer(UUID.fromString(NetworkTestFiles.read("guest-uuid")));
        if (owner == null || guest == null) { require(ticks < 1200, "Both original development profiles must reconnect"); return; }
        var loaded = level.getEntity(UUID.fromString(NetworkTestFiles.read("dragon-uuid")));
        var remains = level.getEntity(UUID.fromString(NetworkTestFiles.read("corpse-uuid")));
        if (loaded == null || remains == null) { require(ticks < 600, "Saved entities must load from same world"); return; }
        require(loaded instanceof FireDragon && remains instanceof FireDragon, "Original registered entity types restored");
        var d = (FireDragon) loaded; var c = (FireDragon) remains;
        require(d.ageDays() == 50 && d.command() == 0 && d.male() && d.armorParts() == 0x1001, "Age, command, gender and armor restored from player RootVehicle");
        require(!d.isNoAi() && d.getCustomName() != null && d.getCustomName().getString().equals("Dedicated persistence dragon"), "Live AI and name preserved");
        require(d.isOwnedBy(owner) && !d.isOwnedBy(guest) && owner.getVehicle() == d && d.getControllingPassenger() == owner, "Native relogin restores actual owner as mounted pilot");
        require(d.flying(), "Saved airborne vehicle resumes flying after mounted relogin");
        require(c.corpse() && c.getHealth() == 0 && c.ageDays() == 75 && c.deathStage() == 2 && c.isNoAi(), "Partially harvested corpse survives disk save and restart");
        require(d.dragonParts().size() == 10 && c.dragonParts().size() == 10, "Both transient families reconstructed");
        var old = Set.of(NetworkTestFiles.read("old-part-uuids").split(","));
        require(java.util.stream.Stream.of(d, c, smith).flatMap(actor -> actor.dragonParts().stream()).noneMatch(p -> old.contains(p.getUUID().toString())), "Parts recreated rather than persisted independently");
        int parts = 0; for (var e : level.getAllEntities()) if (e instanceof DragonPart) parts++;
        require(parts == 30, "Three multipart families restored without saved duplicates or orphans");
        require(smith.isOwnedBy(owner) && !smith.isOwnedBy(guest), "Smith ownership survives actual restart");
        require(owner.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) == 2, "Actual harvested owner inventory survives relogin");
        if (!positionedAfterRejoin) {
            guest.getAbilities().flying = true; guest.onUpdateAbilities(); guest.teleportTo(d.getX() + 28, d.getY() + 12, d.getZ() + 28);
            positionedAfterRejoin = true; NetworkTestFiles.write("rejoin-server-verified", "yes");
        }
        if (NetworkTestFiles.read("owner-rejoined").equals("yes") && NetworkTestFiles.read("guest-rejoined").equals("yes")) phase("REJOIN_DISCONNECT");
    }
    private boolean sameForgeInventory() {
        return forge.getItem(0).is(Items.IRON_INGOT) && forge.getItem(0).getCount() == savedInput
                && forge.getItem(1).is(IafContent.FIRE_DRAGON_BLOOD) && forge.getItem(1).getCount() == savedBlood
                && forge.getItem(2).is(IafDragonForge.INGOT) && forge.getItem(2).getCount() == savedOutput;
    }
    private void createForge() {
        var level = server.overworld();
        for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var block = y == 0 && x == 0 && z == 0 ? IafDragonForge.CORE
                    : y != 0 && x != 0 && z != 0 ? IafDragonForge.BONE : IafDragonForge.BRICK;
            level.setBlockAndUpdate(FORGE_POS.offset(x, y, z), block.defaultBlockState());
        }
        level.setBlockAndUpdate(FORGE_POS.north(), IafDragonForge.INPUT.defaultBlockState());
        forge = (DragonForgeCore) level.getBlockEntity(FORGE_POS);
        require(forge != null && forge.assembled(), "Actual original forge structure");
        forge.setItem(0, new ItemStack(Items.IRON_INGOT, 64)); forge.setItem(1, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, 64));
        smith = spawn(25, 36.5, 25.5); smith.setOwner(owner); smith.setTame(true, false); smith.setCommand(1); smith.setHunger(100);
        NetworkTestFiles.write("forge-smith-uuid", smith.getUUID().toString());
    }
    private boolean forgeReadyToSave() {
        if (!forge.getItem(2).is(IafDragonForge.INGOT) || forge.getItem(2).getCount() < 1 || forge.cookTime() <= 20 || forge.cookTime() >= 980) return false;
        require(smith.fuelingForge() && !smith.isNoAi(), "Real autonomous smith produced before save");
        savedCook = forge.cookTime(); savedInput = forge.getItem(0).getCount(); savedBlood = forge.getItem(1).getCount(); savedOutput = forge.getItem(2).getCount();
        require(savedInput + savedOutput == 64 && savedBlood + savedOutput == 64, "Exact native ingredient conservation before save");
        NetworkTestFiles.write("forge-saved-cook", Integer.toString(savedCook)); NetworkTestFiles.write("forge-saved-input", Integer.toString(savedInput));
        NetworkTestFiles.write("forge-saved-blood", Integer.toString(savedBlood)); NetworkTestFiles.write("forge-saved-output", Integer.toString(savedOutput));
        return true;
    }
    private void tickForgeRestart() {
        if (!forgeLoaded || smith == null) { require(ticks < 1200, "Saved forge and smith must load"); return; }
        if (!forgePauseDone) {
            if (forgePauseAt < 0) forgePauseAt = ticks;
            require(forge.cookTime() == savedCook && sameForgeInventory(), "Cold restart cannot cook or duplicate without new dragon heat");
            if (ticks - forgePauseAt < 20) return;
            forgePauseDone = true; smith.setNoAi(false);
        }
        if (forgeVerified || forge.getItem(2).getCount() == savedOutput) return;
        require(smith.fuelingForge() && forge.getItem(2).is(IafDragonForge.INGOT) && forge.getItem(2).getCount() == savedOutput + 1
                && forge.getItem(0).getCount() == savedInput - 1 && forge.getItem(1).getCount() == savedBlood - 1,
                "Restored smith reacquires naturally and produces exactly one new ingot through real breath");
        forgeVerified = true;
    }
    private void report(String name, boolean restarted) {
        NetworkTestFiles.write(name, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.ofEntries(
                Map.entry("status", "passed"), Map.entry("forgeInventoryAndCookPersist", restarted && forgeLoaded),
                Map.entry("forgeColdRestartPausedCookStable", restarted && forgePauseDone), Map.entry("forgeActualAutonomousProductAfterRestart", restarted && forgeVerified), Map.entry("serverKind", "ordinary DedicatedServer"), Map.entry("independentClientProcesses", restarted ? 4 : 2), Map.entry("distinctPlayerProfiles", 2), Map.entry("mountedLogoutRelogin", restarted),
                Map.entry("loopbackOnly", true), Map.entry("developmentProfiles", true), Map.entry("guestControlsRejected", true),
                Map.entry("ownerFlightObservedByBoth", true), Map.entry("armorHeadTailObservedByBoth", true), Map.entry("actualWorldRestart", restarted),
                Map.entry("ownerInventoryAndCorpsePersist", restarted), Map.entry("transientPartsRebuiltWithoutDuplicates", restarted))));
    }
}
