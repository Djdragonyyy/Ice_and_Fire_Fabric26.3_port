package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** A single visual packet per tracking player; never accepts damage or targets from clients. */
public record DragonBreathPayload(int dragonId, Vec3 from, Vec3 to, float renderSize, float extension) implements CustomPacketPayload {
    public static final Type<DragonBreathPayload> TYPE = new Type<>(IafContent.id("dragon_breath"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonBreathPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.dragonId);
        buffer.writeDouble(value.from.x); buffer.writeDouble(value.from.y); buffer.writeDouble(value.from.z);
        buffer.writeDouble(value.to.x); buffer.writeDouble(value.to.y); buffer.writeDouble(value.to.z);
        buffer.writeFloat(value.renderSize); buffer.writeFloat(value.extension);
    }, buffer -> new DragonBreathPayload(buffer.readVarInt(),
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat(), buffer.readFloat()));
    @Override public Type<DragonBreathPayload> type() { return TYPE; }
    public static void initialize() { PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC); }
    public static void send(FireDragon dragon, Vec3 from, Vec3 to, float extension) {
        var packet = new DragonBreathPayload(dragon.getId(), from, to, DragonGrowth.renderSize(dragon.ageDays()), extension);
        var watchers = new java.util.HashSet<>(PlayerLookup.tracking(dragon));
        if (dragon.getControllingPassenger() instanceof ServerPlayer pilot) watchers.add(pilot);
        for (ServerPlayer player : watchers) ServerPlayNetworking.send(player, packet);
    }
}
