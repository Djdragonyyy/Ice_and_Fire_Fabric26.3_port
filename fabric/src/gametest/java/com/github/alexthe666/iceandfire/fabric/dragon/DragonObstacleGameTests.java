package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class DragonObstacleGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void floor(GameTestHelper h) {
        for (int x = 1; x < 47; x++) for (int z = 1; z < 47; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
    }
    private static FireDragon dragon(GameTestHelper h, Player owner, int age) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        require(d != null, "Dragon created");
        if (owner != null) { d.setOwner(owner); d.setTame(true, false); }
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setPos(h.absoluteVec(new Vec3(12.5, 2, 24.5)));
        d.setOnGround(true);
        return d;
    }
    private static Mob victim(GameTestHelper h, Vec3 pos) {
        var v = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        v.setNoAi(true);
        v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        v.setHealth(1000);
        v.setPos(pos);
        h.getLevel().addFreshEntity(v);
        return v;
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 240, padding = 12)
    public void stageFiveClearsTreesAndActuallyReachesDefensiveTarget(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 125);
        var v = victim(h, h.absoluteVec(new Vec3(40.5, 2, 24.5)));
        for (int z = 1; z < 47; z++) for (int y = 2; y < 14; y++)
            h.setBlock(new BlockPos(19, y, z), y < 8 ? Blocks.OAK_LOG : Blocks.OAK_LEAVES);
        var start = d.position();
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(2, () -> require(d.hurtServer(h.getLevel(), d.damageSources().mobAttack(v), 1), "Real defensive trigger"));
        h.runAfterDelay(200, () -> {
            require(d.getX() > start.x + 10, "Full-size stage five must move through the tree barrier: " + d.position());
            require(h.getBlockState(new BlockPos(19, 3, 24)).isAir(), "Trunk in the approach corridor is removed");
            require(d.withinBiteReach(v) && v.getHealth() < 1000, "Dragon reaches and attacks its threat");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void vegetationHonorsAgeGriefingAndProtectedBlocks(GameTestHelper h) {
        var d = dragon(h, null, 125);
        var v = victim(h, d.position().add(20, 0, 0));
        var log = new BlockPos(14, 3, 24);
        h.setBlock(log, Blocks.OAK_LOG);
        h.setBlock(new BlockPos(14, 4, 24), Blocks.CHEST);
        h.setBlock(new BlockPos(14, 5, 24), Blocks.STONE);
        boolean oldRule = h.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
        var config = DragonCombatConfig.current;
        int oldGrief = config.dragonGriefing;
        boolean oldTamed = config.tamedDragonGriefing;
        try {
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, false, h.getLevel().getServer());
            require(!DragonVegetation.clearApproach(d, v) && h.getBlockState(log).is(Blocks.OAK_LOG), "mobGriefing=false protects trees");
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, true, h.getLevel().getServer());
            config.dragonGriefing = 2;
            require(!DragonVegetation.clearApproach(d, v), "Disabled dragon griefing protects trees");
            config.dragonGriefing = 0;
            var owner = h.makeMockPlayer(GameType.SURVIVAL);
            d.setOwner(owner); d.setTame(true, false);
            config.tamedDragonGriefing = false;
            require(!DragonVegetation.clearApproach(d, v), "Tamed griefing option respected");
            config.tamedDragonGriefing = true;
            var baby = dragon(h, null, 25);
            require(!DragonVegetation.clearApproach(baby, v), "Stage two does not break vegetation");
            require(DragonVegetation.clearApproach(d, v) && h.getBlockState(log).isAir(), "Adult opens vegetation corridor");
            require(h.getBlockState(new BlockPos(14, 4, 24)).is(Blocks.CHEST)
                    && h.getBlockState(new BlockPos(14, 5, 24)).is(Blocks.STONE), "Chest and stone stay intact");
        } finally {
            config.dragonGriefing = oldGrief;
            config.tamedDragonGriefing = oldTamed;
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, oldRule, h.getLevel().getServer());
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void groundedDismountFindsSupportedSideAtEveryRidableStage(GameTestHelper h) {
        floor(h);
        for (int age : new int[]{50, 75, 125}) {
            var owner = h.makeMockPlayer(GameType.SURVIVAL);
            var d = dragon(h, owner, age);
            owner.setPos(d.position());
            require(owner.startRiding(d), "Pilot mounts at age " + age);
            float before = owner.getHealth();
            owner.stopRiding();
            require(Math.abs(owner.getY() - d.getY()) < 0.01, "Ground dismount uses supported side, not tall back");
            require(!owner.getBoundingBox().intersects(d.getBoundingBox()) && h.getLevel().noCollision(owner), "Exit is outside dragon and blocks");
            owner.move(MoverType.SELF, new Vec3(0, -0.1, 0));
            require(owner.getHealth() == before, "Ground dismount causes no falling damage");
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void airborneDismountKeepsHeightAndNormalFallDamage(GameTestHelper h) {
        floor(h);
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner, 125);
        d.setPos(d.position().add(0, 12, 0));
        d.setOnGround(false);
        d.setAutonomousFlight(true, false);
        owner.setPos(d.position());
        require(owner.startRiding(d), "Airborne pilot mounts");
        owner.stopRiding();
        require(owner.getY() >= d.getY(), "No safe ground teleport while airborne");
        require(!owner.isIgnoringFallDamageFromCurrentImpulse(), "No fall immunity granted");
        float before = owner.getHealth();
        for (int i = 0; i < 60 && owner.isAlive(); i++) owner.move(MoverType.SELF, new Vec3(0, -0.5, 0));
        require(owner.getHealth() < before, "Actual airborne fall causes damage");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 120, padding = 12)
    public void doubleJumpActuallyLaunchesMountedDragon(GameTestHelper h) {
        floor(h);
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner, 50);
        owner.setPos(d.position());
        h.getLevel().addFreshEntity(d);
        require(owner.startRiding(d), "Owner mounts");
        d.setRiderControls(DragonControlPayload.UP);
        d.setRiderControls(0);
        d.setRiderControls(DragonControlPayload.UP);
        d.setRiderControls(0);
        double y = d.getY();
        h.runAfterDelay(5, () -> {
            require(d.flying() && d.getY() > y, "Double jump sets flight and actually lifts the dragon");
            owner.stopRiding();
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void groundedDismountWithoutSupportedSideProtectsOnlyItsFall(GameTestHelper h) {
        // A narrow pillar supports the dragon but has no supported side exit.
        h.setBlock(new BlockPos(12, 21, 24), Blocks.STONE);
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner, 125);
        d.setPos(d.position().add(0, 20, 0));
        d.setOnGround(true);
        owner.setPos(d.position());
        require(owner.startRiding(d), "Grounded pilot mounts");
        owner.stopRiding();
        require(owner.getY() >= d.getBoundingBox().maxY - 0.01
                && owner.isIgnoringFallDamageFromCurrentImpulse(), "Only grounded fallback grants current-fall protection");
        float before = owner.getHealth();
        owner.causeFallDamage(12, 1, owner.damageSources().fall());
        require(owner.getHealth() == before, "Fallback grounded dismount fall is protected");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 120, padding = 12)
    public void autonomousBreathWarmsUpDamagesAndStopsForSit(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 50);
        d.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var v = victim(h, d.position().add(20, 0, 0));
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        h.runAfterDelay(18, () -> require(v.getHealth() == 1000, "Breath has original warmup: health=" + v.getHealth() + " ticks=" + d.tickCount + " animation=" + d.attackAnimation() + " distance=" + d.distanceTo(v) + " victim=" + v.position()));
        h.runAfterDelay(65, () -> {
            require(v.getHealth() < 1000 && d.breathProgress > 0, "Autonomous stream causes server damage at range");
            d.setCommand(1);
        });
        h.runAfterDelay(95, () -> {
            require(d.getTarget() == null && d.breathProgress == 0, "Sit cancels autonomous breath");
            h.succeed();
        });
    }
}
