package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonChestGuardGameTests {
    private static final java.util.Map<java.util.UUID, Integer> uses = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, Integer> breaks = new java.util.HashMap<>();
    static {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((p, level, hand, hit) -> {
            uses.merge(p.getUUID(), 1, Integer::sum); return net.minecraft.world.InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((level, p, pos, state, be) -> {
            breaks.merge(p.getUUID(), 1, Integer::sum); return true;
        });
    }
    private static void finish(GameTestHelper h, ServerPlayer player) {
        uses.remove(player.getUUID()); breaks.remove(player.getUUID());
        player.connection.disconnect(net.minecraft.network.chat.Component.literal("Chest regression complete"));
        h.succeed();
    }
    private static ServerPlayer player(GameTestHelper h, GameType mode) {
        var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "chest-test-player");
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), profile, cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(mode);
        h.assertTrue(player.isCreative() == (mode == GameType.CREATIVE), "Fixture uses real ServerPlayer game mode, not creative-only native mock");
        player.setPos(h.absoluteVec(new Vec3(20.5, 2, 20.5))); return player;
    }
    private static FireDragon sleeper(GameTestHelper h, double x) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setSilent(true); d.setPos(h.absoluteVec(new Vec3(x, 2, 20.5)));
        d.setDragonSleeping(true); d.setOrderedToSit(true); h.getLevel().addFreshEntity(d); return d;
    }
    private static void open(GameTestHelper h, ServerPlayer player, BlockPos relative) {
        h.setBlock(relative, Blocks.CHEST);
        var pos = h.absolutePos(relative);
        int before = uses.getOrDefault(player.getUUID(), 0);
        var result = player.gameMode.useItemOn(player, h.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        h.assertTrue(uses.getOrDefault(player.getUUID(), 0) > before,
                "Native use dispatches callback, result=" + result + " range=" + DragonCombatConfig.current.dragonGoldSearchLength);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void actualChestOpeningWakesWildDragonAndKeepsNativeMenu(GameTestHelper h) {
        var p = player(h, GameType.SURVIVAL); var d = sleeper(h, 30.5);
        h.assertTrue(!p.isCreative() && !d.isTame() && d.isAlive() && !d.corpse() && !d.isNoAi() && !d.isOwnedBy(p), "Actual survival and wild living unowned fixture");
        h.assertTrue(h.getLevel().getEntitiesOfClass(FireDragon.class, p.getBoundingBox().inflate(30)).contains(d),
                "Native spatial query includes fixture dragon: p=" + p.position() + " d=" + d.position() + " box=" + d.getBoundingBox());
        var menu = p.containerMenu; open(h, p, new BlockPos(21, 2, 20));
        h.assertTrue(d.getTarget() == p && !d.dragonSleeping() && !d.isOrderedToSit(), "Actual chest callback wakes and targets the nearby survivor: range=" + DragonCombatConfig.current.dragonGoldSearchLength + " target=" + d.getTarget() + " sleeping=" + d.dragonSleeping() + " sit=" + d.isOrderedToSit());
        h.assertTrue(p.containerMenu != menu && h.getBlockState(new BlockPos(21, 2, 20)).is(Blocks.CHEST), "Callback does not cancel native chest opening");
        finish(h, p);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void actualChestBreakingTargetsWildDragonButOrdinaryBlocksDoNot(GameTestHelper h) {
        var p = player(h, GameType.SURVIVAL); var d = sleeper(h, 30.5);
        var pos = new BlockPos(21, 2, 20); h.setBlock(pos, Blocks.STONE);
        h.assertTrue(p.gameMode.destroyBlock(h.absolutePos(pos)), "Native survivor breaks ordinary stone");
        h.assertTrue(breaks.getOrDefault(p.getUUID(), 0) > 0, "Native breaking dispatches before-break event");
        h.assertTrue(d.getTarget() == null && d.dragonSleeping(), "Ordinary blocks are not a chest alarm");
        h.setBlock(pos, Blocks.CHEST);
        h.assertTrue(p.gameMode.destroyBlock(h.absolutePos(pos)), "Native survivor breaks chest");
        h.assertTrue(h.getBlockState(pos).isAir() && d.getTarget() == p && !d.dragonSleeping(), "Chest break remains allowed and wakes the wild dragon"); finish(h, p);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void creativeTamedOwnedAndDeadDragonsAreExcluded(GameTestHelper h) {
        var p = player(h, GameType.CREATIVE); var wild = sleeper(h, 25.5);
        open(h, p, new BlockPos(21, 2, 20));
        h.assertTrue(wild.getTarget() == null && wild.dragonSleeping(), "Creative player cannot trigger original alarm");
        wild.discard(); p.setGameMode(GameType.SURVIVAL);
        var tame = sleeper(h, 25.5); tame.setTame(true, false);
        var owned = sleeper(h, 26.5); owned.setOwner(p); owned.setTame(false, false);
        h.assertTrue(owned.isOwnedBy(p) && !owned.isTame(), "Owner exclusion is independently exercised");
        var dead = sleeper(h, 27.5); dead.setHealth(0);
        open(h, p, new BlockPos(21, 2, 20));
        h.assertTrue(tame.getTarget() == null && owned.getTarget() == null && dead.getTarget() == null,
                "Tame, actual owner and dead exclusions remain separate"); finish(h, p);
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void configuredRegionIsCenteredOnPlayerNotChestAndUsesOriginalAabb(GameTestHelper h) {
        var p = player(h, GameType.SURVIVAL); var near = sleeper(h, 19.5); var far = sleeper(h, 24.5);
        var config = DragonCombatConfig.current; int old = config.dragonGoldSearchLength;
        try {
            config.dragonGoldSearchLength = 2;
            open(h, p, new BlockPos(23, 2, 20));
            h.assertTrue(near.getTarget() == p && far.getTarget() == null && far.dragonSleeping(),
                    "Region is the original player AABB, not chest position or default thirty-block search");
        } finally { config.dragonGoldSearchLength = old; }
        finish(h, p);
    }
}
