package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DragonFlameOptions(float size) implements ParticleOptions {
    public static final MapCodec<DragonFlameOptions> CODEC = Codec.FLOAT.fieldOf("size").xmap(DragonFlameOptions::new, DragonFlameOptions::size);
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonFlameOptions> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeFloat(value.size), buffer -> new DragonFlameOptions(buffer.readFloat()));
    public DragonFlameOptions { size = Math.clamp(size, 0.05F, 3); }
    @Override public ParticleType<DragonFlameOptions> getType() { return IafContent.DRAGON_FLAME; }
}
