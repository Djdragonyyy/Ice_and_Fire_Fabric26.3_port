package com.github.alexthe666.iceandfire.fabric.forge;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

public class DragonForgeAutomationGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon actor(GameTestHelper h, Player owner) {
        for (int x = 12; x <= 36; x++) for (int z = 2; z <= 23; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < 25; i++) d.growOneDay();
        d.setOwner(owner); d.setTame(true, false); d.setCommand(1); d.setHunger(100);
        d.setPos(h.absoluteVec(new Vec3(24.5, 2, 11.5))); d.setYRot(0); d.yBodyRot = d.yHeadRot = 0; d.setOnGround(true);
        owner.setPos(d.position().add(6, 0, 0)); require(h.getLevel().addFreshEntity(d), "Actual ticking owned dragon"); return d;
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12, maxTicks = 900)
    public void actualAutonomousDragonSuppliesTheForgeAndStopsAfterProduction(GameTestHelper h) {
        var core = DragonForgeGameTests.structure(h); DragonForgeGameTests.ingredients(core, 1);
        var d = actor(h, h.makeMockServerPlayerInLevel()); var start = d.position(); boolean[] seen = {false}, finished = {false};
        h.onEachTick(() -> {
            seen[0] |= d.fuelingForge();
            if (finished[0] || !core.getItem(2).is(IafDragonForge.INGOT)) return;
            finished[0] = true;
            require(seen[0] && core.getItem(0).isEmpty() && core.getItem(1).isEmpty() && core.getItem(2).getCount() == 1,
                    "Actual AI and breath produce exactly one ingot with exact consumption");
            require(d.position().subtract(start).horizontalDistance() < 0.05 && !d.flying(), "Forge work stays stationary without wandering or random takeoff");
            require(h.getLevel().getEntitiesOfClass(DragonFireCharge.class, new AABB(core.getBlockPos()).inflate(30)).isEmpty(), "Forge work uses stream, never combat charges");
            h.runAfterDelay(3, () -> { require(!d.fuelingForge() && d.forgeInput() == null, "Empty forge releases dragon and breath mode"); h.succeed(); });
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void sourceRangeSightAndActorPrioritiesProtectTheForgeReservation(GameTestHelper h) {
        var core = DragonForgeGameTests.structure(h); DragonForgeGameTests.ingredients(core, 1);
        var p = h.makeMockServerPlayerInLevel(); var d = actor(h, p); var input = core.getBlockPos().north();
        require(d.canSupplyForgeAt(input), "Awake tame stage2 dragon can supply visible input in original squared range");
        d.setNoAi(true); require(!d.canSupplyForgeAt(input), "NoAI wins"); d.setNoAi(false);
        d.setCommand(2); require(!d.canSupplyForgeAt(input), "Escort wins"); d.setCommand(1);
        DragonForgeActorTestAccess.sleeping(d, true); require(!d.canSupplyForgeAt(input), "Sleep wins"); DragonForgeActorTestAccess.sleeping(d, false);
        DragonForgeActorTestAccess.flying(d, true); require(!d.canSupplyForgeAt(input), "Flight wins"); DragonForgeActorTestAccess.flying(d, false);
        var enemy = EntityTypes.PIG.create(h.getLevel(), EntitySpawnReason.TRIGGERED); d.setCommand(0); d.setTarget(enemy);
        require(d.getTarget() == enemy && !d.canSupplyForgeAt(input), "Combat target wins"); d.setTarget(null); d.setCommand(1);
        d.setInLove(p); require(!d.canSupplyForgeAt(input), "Courtship wins"); d.resetLove();
        var position = d.position(); d.setPos(position.add(30, 0, 0)); require(!d.canSupplyForgeAt(input), "Original115timesStage is a squared-distance limit"); d.setPos(position);
        for (int x = 20; x <= 28; x++) for (int y = 2; y <= 12; y++) h.setBlock(new BlockPos(x, y, 19), Blocks.STONE);
        require(!d.canSupplyForgeAt(input), "Intervening wall blocks the input");
        for (int x = 20; x <= 28; x++) for (int y = 2; y <= 12; y++) h.setBlock(new BlockPos(x, y, 19), Blocks.AIR);
        require(d.claimForgeInput(input), "First transient reservation succeeds");
        var other = input.north(2); h.getLevel().setBlockAndUpdate(other, IafDragonForge.INPUT.defaultBlockState());
        require(d.canSupplyForgeAt(other) && !d.claimForgeInput(other), "A second eligible input position cannot replace a live reservation");
        d.releaseForgeInput(input); require(d.forgeInput() == null, "Reservation released explicitly");
        for (int i = 0; i < 25; i++) d.growOneDay();
        require(p.startRiding(d) && !d.canSupplyForgeAt(input), "Owner's pilot seat wins"); p.stopRiding();
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12, maxTicks = 350)
    public void actualForgeWorkYieldsToEscortNoAiAndADefensiveTarget(GameTestHelper h) {
        var core = DragonForgeGameTests.structure(h); DragonForgeGameTests.ingredients(core, 64);
        var p = h.makeMockServerPlayerInLevel(); var d = actor(h, p); int[] phase = {0}; long[] at = {0};
        h.onEachTick(() -> {
            if (phase[0] == 0 && d.fuelingForge() && d.breathProgress >= 3) { d.setCommand(2); phase[0] = 1; at[0] = h.getTick(); }
            else if (phase[0] == 1 && h.getTick() - at[0] >= 3) {
                require(d.forgeInput() == null && !d.fuelingForge(), "Actual escort command cancels work");
                d.setCommand(1); d.setNoAi(true); phase[0] = 2; at[0] = h.getTick();
            } else if (phase[0] == 2 && h.getTick() - at[0] >= 15) {
                require(!d.fuelingForge(), "Core cannot recapture a NoAI dragon"); d.setNoAi(false); phase[0] = 3;
            } else if (phase[0] == 3 && d.fuelingForge() && d.breathProgress >= 3) {
                var target = EntityTypes.PIG.create(h.getLevel(), EntitySpawnReason.TRIGGERED); target.setNoAi(true); target.setPos(d.position().add(5, 0, 0)); h.getLevel().addFreshEntity(target);
                d.setCommand(0); d.setTarget(target); phase[0] = 4; at[0] = h.getTick();
            } else if (phase[0] == 4 && h.getTick() - at[0] >= 3) {
                require(d.getTarget() != null && !d.fuelingForge() && d.forgeInput() == null, "Native combat retains target and takes priority over forge");
                d.setCommand(1); h.succeed();
            }
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void originalComparatorReflectsTheActualCoreInventory(GameTestHelper h) {
        var core = DragonForgeGameTests.structure(h); var pos = core.getBlockPos(); var state = core.getBlockState();
        require(state.hasAnalogOutputSignal() && state.getAnalogOutputSignal(h.getLevel(), pos, Direction.NORTH) == 0, "Empty native comparator signal");
        core.setItem(0, new ItemStack(Items.IRON_INGOT));
        require(state.getAnalogOutputSignal(h.getLevel(), pos, Direction.NORTH) == 1, "One item raises the comparator");
        core.setItem(0, new ItemStack(Items.IRON_INGOT, 64)); core.setItem(1, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, 64)); core.setItem(2, new ItemStack(IafDragonForge.INGOT, 64));
        require(state.getAnalogOutputSignal(h.getLevel(), pos, Direction.NORTH) == 15, "Full native inventory gives original maximum signal");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12, maxTicks = 200)
    public void removingTheCoreReleasesItsActualWorkingDragon(GameTestHelper h) {
        var core = DragonForgeGameTests.structure(h); DragonForgeGameTests.ingredients(core, 64);
        var d = actor(h, h.makeMockServerPlayerInLevel()); boolean[] removed = {false};
        h.onEachTick(() -> {
            if (removed[0] || !d.fuelingForge() || d.breathProgress < 3) return;
            removed[0] = true;
            h.getLevel().destroyBlock(core.getBlockPos(), false);
            require(d.forgeInput() == null && !d.fuelingForge(), "Native core removal releases its dragon immediately");
            h.runAfterDelay(4, () -> {
                require(d.forgeInput() == null && !d.fuelingForge(), "Orphan input cannot recapture the dragon");
                h.succeed();
            });
        });
    }
}
