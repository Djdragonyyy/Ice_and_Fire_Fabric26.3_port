package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Only transmits input. Entity selection, ownership and effects are checked by the server. */
public record DragonControlPayload(int flags) implements CustomPacketPayload {
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int FIRE = 4;
    public static final int STRIKE = 8;
    public static final int ALL = UP | DOWN | FIRE | STRIKE;
    public static final Type<DragonControlPayload> TYPE = new Type<>(IafContent.id("dragon_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonControlPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeByte(payload.flags), buffer -> new DragonControlPayload(buffer.readUnsignedByte()));

    public DragonControlPayload {
        if ((flags & ~ALL) != 0) throw new IllegalArgumentException("Invalid dragon controls");
    }

    @Override public Type<DragonControlPayload> type() { return TYPE; }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            var player = context.player();
            if (player.getVehicle() instanceof FireDragon dragon && dragon.getControllingPassenger() == player) {
                dragon.setRiderControls(payload.flags);
            }
        });
    }
}
