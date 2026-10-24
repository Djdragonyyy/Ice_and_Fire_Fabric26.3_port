package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Bounded chunk tickets replace ForgeChunkManager and the original mutable item singleton. */
public final class CrystalSummons {
    static final int TIMEOUT = 2000;
    public static final TicketType TICKET = Registry.register(BuiltInRegistries.TICKET_TYPE, IafContent.id("summoning_crystal"),
            new TicketType(TIMEOUT + 1L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION
                    | TicketType.FLAG_KEEP_DIMENSION_ACTIVE | TicketType.FLAG_CAN_EXPIRE_IF_UNLOADED));
    private record Request(Player player, InteractionHand hand, ItemStack stack, Vec3 target, float yaw, ChunkPos chunk, long started) {}
    private static final Map<ServerLevel, Map<UUID, Request>> pending = new HashMap<>();
    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof FireDragon d && d.crystalBound()) DragonPositions.get(level).put(d.getUUID(), d.blockPosition());
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof FireDragon d && d.crystalBound()) {
                var reason = d.getRemovalReason();
                if (reason == null || reason == Entity.RemovalReason.UNLOADED_TO_CHUNK) DragonPositions.get(level).put(d.getUUID(), d.blockPosition());
                else DragonPositions.get(level).remove(d.getUUID());
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var level : java.util.List.copyOf(pending.keySet())) if (level.getServer() == server) tick(level, level.getGameTime());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (var level : java.util.List.copyOf(pending.keySet())) if (level.getServer() == server) {
                for (var chunk : pending.get(level).values().stream().map(Request::chunk).distinct().toList()) level.getChunkSource().removeTicketWithRadius(TICKET, chunk, 2);
                pending.remove(level);
            }
        });
    }
    static InteractionResult request(ServerLevel level, Player player, InteractionHand hand, ItemStack stack, UUID uuid, Vec3 target, float yaw) {
        if (pending.getOrDefault(level, Map.of()).containsKey(uuid)) { SummoningCrystalItem.error(player); return InteractionResult.FAIL; }
        var entity = level.getEntity(uuid);
        if (entity != null) {
            if (entity instanceof FireDragon d && recall(level, player, stack, d, target, yaw)) return InteractionResult.SUCCESS;
            SummoningCrystalItem.error(player); return InteractionResult.FAIL;
        }
        var origin = DragonPositions.get(level).position(uuid);
        if (!DragonCombatConfig.current.chunkLoadSummonCrystal || origin == null) { SummoningCrystalItem.error(player); return InteractionResult.FAIL; }
        var chunk = new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
        pending.computeIfAbsent(level, ignored -> new HashMap<>()).put(uuid, new Request(player, hand, stack, target, yaw, chunk, level.getGameTime()));
        level.getChunkSource().addTicketWithRadius(TICKET, chunk, 2);
        return InteractionResult.SUCCESS;
    }
    static void tick(ServerLevel level, long now) {
        var requests = pending.get(level);
        if (requests == null) return;
        var iterator = requests.entrySet().iterator();
        var finished = new java.util.HashSet<ChunkPos>();
        while (iterator.hasNext()) {
            var entry = iterator.next(); var r = entry.getValue(); var p = r.player();
            boolean valid = p.isAlive() && !p.isRemoved() && p.level() == level && !(p instanceof ServerPlayer sp && sp.hasDisconnected())
                    && p.getItemInHand(r.hand()) == r.stack() && SummoningCrystalItem.dragonUuid(r.stack()).filter(entry.getKey()::equals).isPresent();
            var entity = level.getEntity(entry.getKey());
            if (!valid || !DragonCombatConfig.current.chunkLoadSummonCrystal || now - r.started() >= TIMEOUT || entity != null) {
                boolean success = valid && DragonCombatConfig.current.chunkLoadSummonCrystal && now - r.started() < TIMEOUT
                        && entity instanceof FireDragon d && recall(level, p, r.stack(), d, r.target(), r.yaw());
                if (!success && valid) SummoningCrystalItem.error(p);
                iterator.remove(); finished.add(r.chunk());
            }
        }
        for (var chunk : finished) if (requests.values().stream().noneMatch(r -> r.chunk().equals(chunk))) level.getChunkSource().removeTicketWithRadius(TICKET, chunk, 2);
        if (requests.isEmpty()) pending.remove(level);
    }
    static int pendingCount(ServerLevel level) { return pending.getOrDefault(level, Map.of()).size(); }
    private static boolean recall(ServerLevel level, Player player, ItemStack stack, FireDragon dragon, Vec3 target, float yaw) {
        if (!dragon.isAlive() || !SummoningCrystalItem.owns(dragon, player) || dragon.level() != level || dragon.isVehicle()
                || dragon.isPassenger() || dragon.shoulderCarrier() != null) return false;
        dragon.refreshDimensions();
        if (!DragonEscortTeleport.safe(dragon, level, target, false)) return false;
        dragon.getNavigation().stop(); dragon.flightManager.reset(); dragon.setDeltaMovement(Vec3.ZERO);
        dragon.teleportTo(target.x, target.y, target.z); dragon.setYRot(yaw); dragon.setXRot(0); dragon.fallDistance = 0;
        dragon.setCrystalBound(false); DragonPositions.get(level).remove(dragon.getUUID()); SummoningCrystalItem.clear(stack);
        level.playSound(null, dragon.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1, 1);
        level.playSound(null, dragon.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1, 1);
        player.sendOverlayMessage(Component.translatable("message.iceandfire.dragonTeleport"));
        return true;
    }
    private CrystalSummons() {}
}
