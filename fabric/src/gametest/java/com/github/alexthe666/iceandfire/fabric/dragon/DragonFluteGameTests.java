package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class DragonFluteGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, Player owner, Vec3 pos) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        if (owner != null) { d.setTame(true, false); d.setOwner(owner); }
        d.setNoAi(true); for (int i = 0; i < 25; i++) d.growOneDay();
        d.setPos(pos); d.setAutonomousFlight(true, true); h.getLevel().addFreshEntity(d); return d;
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 300)
    public void configuredFluteBoxOnlyStopsNearbyOwnedDragons(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonFluteDistance = 1; DragonCombatConfig.current = config;
            var owner = h.makeMockPlayer(GameType.SURVIVAL); owner.setPos(h.absoluteVec(new Vec3(8.5, 2, 8.5)));
            var other = h.makeMockPlayer(GameType.SURVIVAL);
            var near = dragon(h, owner, owner.position().add(8, 0, 0));
            var far = dragon(h, owner, owner.position().add(26, 0, 0));
            var foreign = dragon(h, other, owner.position().add(4, 0, 0));
            var wild = dragon(h, null, owner.position().add(0, 0, 4));
            var high = dragon(h, owner, owner.position().add(0, 260, 0));
            var flute = new ItemStack(IafContent.DRAGON_FLUTE); owner.setItemInHand(InteractionHand.MAIN_HAND, flute);
            require(IafContent.DRAGON_FLUTE.use(h.getLevel(), owner, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS, "Flute use succeeds");
            require(!near.flying() && !near.hovering() && near.isOwnedBy(owner), "Nearby own dragon stops flight and keeps ownership");
            require(far.flying() && foreign.flying() && wild.flying() && high.flying(), "Original horizontal/vertical box and ownership preserved");
            require(flute.getCount() == 1 && owner.getCooldowns().isOnCooldown(flute), "Reusable flute starts cooldown");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
    @GameTest
    public void sixtyTickCooldownBlocksRepeatedUseAndAllowsOffhand(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var flute = new ItemStack(IafContent.DRAGON_FLUTE);
        owner.setItemInHand(InteractionHand.OFF_HAND, flute);
        require(IafContent.DRAGON_FLUTE.use(h.getLevel(), owner, InteractionHand.OFF_HAND) == InteractionResult.SUCCESS, "Offhand supported");
        require(IafContent.DRAGON_FLUTE.use(h.getLevel(), owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL, "Immediate replay rejected");
        for (int i = 0; i < 59; i++) owner.getCooldowns().tick();
        require(owner.getCooldowns().isOnCooldown(flute), "Cooldown remains for the sixtieth tick");
        owner.getCooldowns().tick();
        require(!owner.getCooldowns().isOnCooldown(flute)
                && IafContent.DRAGON_FLUTE.use(h.getLevel(), owner, InteractionHand.OFF_HAND) == InteractionResult.SUCCESS, "Reuse after sixty ticks");
        h.succeed();
    }
    @GameTest
    public void fluteKeepsPilotAndAirFallState(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner, h.absoluteVec(new Vec3(2, 15, 2)));
        for (int i = 25; i < 50; i++) d.growOneDay();
        require(owner.startRiding(d), "Owner pilot fixture");
        owner.fallDistance = 9; d.fallDistance = 8;
        var pos = d.position(); d.onHearFlute(owner);
        require(!d.flying() && !d.hovering() && d.getControllingPassenger() == owner && owner.getVehicle() == d,
                "Flight stops without dismounting the owner pilot");
        require(owner.fallDistance == 9 && d.fallDistance == 8 && d.position().equals(pos), "No teleport or airborne fall immunity is introduced");
        owner.stopRiding(); h.succeed();
    }
    @GameTest
    public void blinkClosesForOriginalSixFramesEachFiftyTicks(GameTestHelper h) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int cycle = 0; cycle < 3; cycle++) for (int tick = 0; tick < 50; tick++) {
            d.tickCount = cycle * 50 + tick;
            require(d.isBlinking() == (tick >= 44), "Original blink boundary at frame " + tick);
            require(!d.dragonSleeping(), "Blink never sets the sleeping AI state");
        }
        h.succeed();
    }
}
