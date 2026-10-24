package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonCrystalGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, Player owner) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setTame(true, false); d.setOwner(owner); d.setNoAi(true); d.setPos(h.absoluteVec(new Vec3(8.5, 2, 8.5)));
        h.getLevel().addFreshEntity(d); return d;
    }
    private static ItemStack bind(Player owner, FireDragon d) {
        var stack = new ItemStack(IafContent.SUMMONING_CRYSTAL); owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
        require(IafContent.SUMMONING_CRYSTAL.interactLivingEntity(stack, owner, d, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS, "Binding");
        return stack;
    }
    private static InteractionResult use(GameTestHelper h, Player owner, BlockPos pos) {
        return IafContent.SUMMONING_CRYSTAL.useOn(new UseOnContext(owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(h.absoluteVec(Vec3.atCenterOf(pos)), Direction.UP, h.absolutePos(pos), false)));
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 180)
    public void bindingItemAndDragonPersistThenLoadedRecallReusesSameEntity(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, owner); var crystal = bind(owner, d); var uuid = d.getUUID();
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        output.store("Crystal", ItemStack.CODEC, crystal); d.saveWithoutId(output.child("DragonEntity"));
        var input = TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult());
        var item = input.read("Crystal", ItemStack.CODEC).orElseThrow(); owner.setItemInHand(InteractionHand.MAIN_HAND, item);
        var loaded = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.LOAD); loaded.load(input.childOrEmpty("DragonEntity"));
        require(loaded.crystalBound() && SummoningCrystalItem.dragonUuid(item).orElseThrow().equals(uuid), "Native entity and item codecs retain bond");
        var pos = new BlockPos(20, 1, 20); h.setBlock(pos, Blocks.STONE); var target = h.absoluteVec(Vec3.atCenterOf(pos.above()));
        int id = d.getId();
        require(use(h, owner, pos) == InteractionResult.SUCCESS && h.getLevel().getEntity(uuid) == d && d.getId() == id
                && d.position().distanceTo(target) < 0.001, "Recall teleports the original entity without copying it");
        require(!d.crystalBound() && SummoningCrystalItem.dragonUuid(item).isEmpty() && !item.has(DataComponents.CUSTOM_MODEL_DATA)
                && DragonPositions.get(h.getLevel()).position(uuid) == null, "Successful recall clears item, entity marker and position index");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 180)
    public void blockedForeignAndOccupiedRecallsRetainBond(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, owner); var crystal = bind(owner, d); var original = d.position();
        var pos = new BlockPos(20, 1, 20); h.setBlock(pos.above(), Blocks.BEDROCK);
        require(use(h, owner, pos) == InteractionResult.FAIL && d.position().equals(original) && d.crystalBound()
                && SummoningCrystalItem.dragonUuid(crystal).isPresent(), "Obstruction retains both sides of the bond");
        h.setBlock(pos.above(), Blocks.AIR);
        var stranger = h.makeMockPlayer(GameType.SURVIVAL); stranger.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        require(use(h, stranger, pos) == InteractionResult.FAIL && d.position().equals(original), "Current ownership is checked at recall, not only binding");
        owner.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        for (int i = 0; i < 50; i++) d.growOneDay(); require(owner.startRiding(d), "Pilot fixture");
        require(use(h, owner, pos) == InteractionResult.FAIL && owner.getVehicle() == d, "Occupied mount cannot be teleported away from its rider");
        owner.stopRiding(); h.succeed();
    }
    @GameTest
    public void creativeBindingUpdatesRealStackAndCannotOverwriteOccupiedCrystal(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.CREATIVE); GameType.CREATIVE.updatePlayerAbilities(owner.getAbilities());
        var d = dragon(h, owner); var item = new ItemStack(IafContent.SUMMONING_CRYSTAL); owner.setItemInHand(InteractionHand.MAIN_HAND, item);
        require(owner.interactOn(d, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction() && SummoningCrystalItem.dragonUuid(item).orElseThrow().equals(d.getUUID()), "Native creative copy branch updates actual held crystal");
        var other = dragon(h, owner);
        require(IafContent.SUMMONING_CRYSTAL.interactLivingEntity(item, owner, other, InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                && SummoningCrystalItem.dragonUuid(item).orElseThrow().equals(d.getUUID()) && !other.crystalBound(), "Occupied crystal cannot be rebound silently");
        h.succeed();
    }
    @GameTest
    public void nativeSavedDataSurvivesDiskAndDisabledChunkLoadingKeepsItem(GameTestHelper h) throws Exception {
        var path = java.nio.file.Files.createTempDirectory(net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir(), "crystal-data-test-");
        var uuid = java.util.UUID.randomUUID(); var pos = new BlockPos(-1234, 0, 5678);
        try (var store = new net.minecraft.world.level.storage.SavedDataStorage(path, net.minecraft.util.datafix.DataFixers.getDataFixer(), h.getLevel().registryAccess())) {
            var data = store.computeIfAbsent(DragonPositions.TYPE); data.put(uuid, pos); store.saveAndJoin();
        }
        try (var store = new net.minecraft.world.level.storage.SavedDataStorage(path, net.minecraft.util.datafix.DataFixers.getDataFixer(), h.getLevel().registryAccess())) {
            require(pos.equals(store.computeIfAbsent(DragonPositions.TYPE).position(uuid)), "Native SavedData codec persists UUID/zero/negative coordinates to disk");
        }
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, owner); var item = bind(owner, d);
        var old = DragonCombatConfig.current; var config = new DragonCombatConfig(); config.chunkLoadSummonCrystal = false; DragonCombatConfig.current = config;
        try {
            d.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_TO_CHUNK);
            // Removal lookup drains at the end of a world tick; direct request with an unknown saved UUID isolates disabled loading.
            var missing = java.util.UUID.randomUUID(); DragonPositions.get(h.getLevel()).put(missing, new BlockPos(0, 0, 0));
            require(CrystalSummons.request(h.getLevel(), owner, InteractionHand.MAIN_HAND, item, missing, Vec3.ZERO, 0) == InteractionResult.FAIL
                    && CrystalSummons.pendingCount(h.getLevel()) == 0 && SummoningCrystalItem.dragonUuid(item).isPresent(), "Disabled chunk loading preserves crystal and adds no request");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
    private static boolean hasTicket(GameTestHelper h, ChunkPos chunk) {
        var tickets = h.getLevel().getDataStorage().computeIfAbsent(net.minecraft.world.level.TicketStorage.TYPE).getTickets(chunk.pack());
        return tickets != null && tickets.stream().anyMatch(t -> t.getType() == CrystalSummons.TICKET);
    }
    private static ItemStack missingCrystal(Player owner, java.util.UUID uuid) {
        var item = new ItemStack(IafContent.SUMMONING_CRYSTAL);
        var tag = new net.minecraft.nbt.CompoundTag(); var bound = new net.minecraft.nbt.CompoundTag();
        bound.store("DragonUUID", net.minecraft.core.UUIDUtil.CODEC, uuid); tag.put("Dragon", bound);
        item.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag)); owner.setItemInHand(InteractionHand.MAIN_HAND, item); return item;
    }
    @GameTest
    public void sharedOriginTicketStaysUntilLastRequestIsCancelled(GameTestHelper h) {
        var level = h.getLevel(); var origin = h.absolutePos(new BlockPos(1, 1, 1)); var chunk = ChunkPos.containing(origin);
        var one = h.makeMockPlayer(GameType.SURVIVAL); var two = h.makeMockPlayer(GameType.SURVIVAL);
        var a = java.util.UUID.randomUUID(); var b = java.util.UUID.randomUUID();
        var first = missingCrystal(one, a); var second = missingCrystal(two, b);
        DragonPositions.get(level).put(a, origin); DragonPositions.get(level).put(b, origin);
        require(CrystalSummons.request(level, one, InteractionHand.MAIN_HAND, first, a, Vec3.ZERO, 0) == InteractionResult.SUCCESS
                && CrystalSummons.request(level, two, InteractionHand.MAIN_HAND, second, b, Vec3.ZERO, 0) == InteractionResult.SUCCESS, "Independent requests sharing one source chunk");
        one.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); CrystalSummons.tick(level, level.getGameTime());
        require(CrystalSummons.pendingCount(level) == 1 && hasTicket(h, chunk), "One cancellation preserves the other request's source ticket");
        two.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); CrystalSummons.tick(level, level.getGameTime());
        require(CrystalSummons.pendingCount(level) == 0 && !hasTicket(h, chunk)
                && SummoningCrystalItem.dragonUuid(first).isPresent() && SummoningCrystalItem.dragonUuid(second).isPresent(), "Last cancellation releases ticket and preserves both crystals");
        DragonPositions.get(level).remove(a); DragonPositions.get(level).remove(b); h.succeed();
    }
    @GameTest
    public void timeoutBoundaryPreservesItemAndReleasesNonPersistentTicket(GameTestHelper h) {
        var level = h.getLevel(); var owner = h.makeMockPlayer(GameType.SURVIVAL); var uuid = java.util.UUID.randomUUID();
        var item = missingCrystal(owner, uuid); var origin = h.absolutePos(new BlockPos(1, 1, 1)); var chunk = ChunkPos.containing(origin);
        DragonPositions.get(level).put(uuid, origin); long start = level.getGameTime();
        require(CrystalSummons.request(level, owner, InteractionHand.MAIN_HAND, item, uuid, Vec3.ZERO, 0) == InteractionResult.SUCCESS, "Pending fixture");
        CrystalSummons.tick(level, start + CrystalSummons.TIMEOUT - 1);
        require(CrystalSummons.pendingCount(level) == 1 && hasTicket(h, chunk), "Request remains before original 2000-tick tolerance");
        CrystalSummons.tick(level, start + CrystalSummons.TIMEOUT);
        require(CrystalSummons.pendingCount(level) == 0 && !hasTicket(h, chunk) && SummoningCrystalItem.dragonUuid(item).isPresent()
                && !CrystalSummons.TICKET.persist() && CrystalSummons.TICKET.timeout() == 2001, "Timeout releases bounded non-persistent ticket without consuming bond");
        DragonPositions.get(level).remove(uuid); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 300, maxTicks = 4000)
    public void unloadedDragonIsActuallyLoadedRecalledAndTicketReleased(GameTestHelper h) {
        var level = h.getLevel(); var owner = h.makeMockServerPlayerInLevel();
        owner.gameMode.changeGameModeForPlayer(GameType.CREATIVE); owner.getAbilities().flying = true;
        owner.setPos(h.absoluteVec(new Vec3(10, 4, 10)));
        var origin = h.absolutePos(new BlockPos(4096, 5, 4096)); var chunk = new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
        var ready = level.getChunkSource().addTicketAndLoadWithRadius(TicketType.PORTAL, chunk, 2);
        // Vanilla GameTestServer deliberately has no tick wait; pump generation before advancing the fixture.
        level.getChunkSource().getChunk(chunk.x(), chunk.z(), net.minecraft.world.level.chunk.status.ChunkStatus.FULL, true);
        var phase = new int[1]; var uuid = new java.util.UUID[1]; var target = h.absoluteVec(Vec3.atCenterOf(new BlockPos(20, 2, 20)));
        Runnable[] poll = new Runnable[1];
        poll[0] = () -> {
            if (phase[0] == 0 && ready.isDone()) {
                ready.join();
                var d = dragon(h, owner); d.setPos(Vec3.atCenterOf(origin)); d.setNoGravity(true); uuid[0] = d.getUUID(); bind(owner, d);
                level.save(null, true, false); level.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunk, 2); phase[0] = 1;
                IafContentLogger.log("source saved and ticket released");
            } else if (phase[0] == 1 && level.getEntity(uuid[0]) == null) {
                require(DragonPositions.get(level).position(uuid[0]) != null, "Unload tracker retains last position");
                require(CrystalSummons.request(level, owner, InteractionHand.MAIN_HAND, owner.getMainHandItem(), uuid[0], target, 0) == InteractionResult.SUCCESS,
                        "Unloaded entity request accepted");
                require(CrystalSummons.pendingCount(level) == 1, "A bounded delayed request is queued"); phase[0] = 2;
                IafContentLogger.log("source unloaded, summon queued");
            } else if (phase[0] == 2 && SummoningCrystalItem.dragonUuid(owner.getMainHandItem()).isEmpty()) {
                require(level.getEntity(uuid[0]) instanceof FireDragon, "Original unloaded dragon returns from native entity storage");
                var d = (FireDragon) level.getEntity(uuid[0]);
                require(d.position().subtract(target).horizontalDistance() < 1 && d.isOwnedBy(owner) && CrystalSummons.pendingCount(level) == 0,
                        "Delayed recall preserves owner and releases pending request");
                require(!hasTicket(h, chunk), "Temporary origin ticket released");
                phase[0] = 3; level.getServer().getPlayerList().remove(owner); h.succeed(); return;
            }
            // Yield a bounded 5ms to real disk/chunk workers; do not alter world time or production deadlines.
            java.util.concurrent.locks.LockSupport.parkNanos(5_000_000L);
            // GameTestInfo keys scheduled work by Runnable and removes it after invocation.
            // Reusing the currently executing instance would delete its own next poll.
            h.runAfterDelay(5, new Runnable() { @Override public void run() { poll[0].run(); } });
        };
        h.runAfterDelay(3900, () -> require(phase[0] == 3, "Unload fixture stalled: phase=" + phase[0] + " ready=" + ready.isDone()
                + " ownerAlive=" + owner.isAlive() + " disconnected=" + owner.hasDisconnected() + " pending=" + CrystalSummons.pendingCount(level)
                + " entity=" + (uuid[0] == null ? null : level.getEntity(uuid[0]))));
        h.runAfterDelay(1, poll[0]);
    }
    private static final class IafContentLogger {
        static void log(String phase) { com.github.alexthe666.iceandfire.fabric.IceAndFireFabric.LOGGER.info("Crystal unload fixture: {}", phase); }
    }
}
