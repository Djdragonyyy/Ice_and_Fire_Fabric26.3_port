package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Native codec-backed migration of DragonPosWorldData (LGPL-3.0). */
public final class DragonPositions extends SavedData {
    public static final Codec<DragonPositions> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, BlockPos.CODEC)
            .xmap(DragonPositions::new, data -> Map.copyOf(data.positions));
    public static final SavedDataType<DragonPositions> TYPE = new SavedDataType<>(IafContent.id("dragon_positions"), DragonPositions::new, CODEC, null);
    private final Map<UUID, BlockPos> positions;
    public DragonPositions() { this(Map.of()); }
    private DragonPositions(Map<UUID, BlockPos> positions) { this.positions = new HashMap<>(positions); }
    public static DragonPositions get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    public BlockPos position(UUID uuid) { return positions.get(uuid); }
    public void put(UUID uuid, BlockPos pos) { if (!pos.equals(positions.put(uuid, pos.immutable()))) setDirty(); }
    public void remove(UUID uuid) { if (positions.remove(uuid) != null) setDirty(); }
}
