package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonSleepGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h) {
        for (int x = 1; x < 46; x++) for (int z = 1; z < 46; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < 25; i++) d.growOneDay();
        d.setPos(h.absoluteVec(new Vec3(24.5, 2, 24.5)));
        d.setOnGround(true);
        d.setHealth(d.getMaxHealth());
        return d;
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 100, padding = 12)
    public void nightSleepStopsMovementSynchronizesAndSurvivesSave(GameTestHelper h) {
        var d = dragon(h);
        var start = d.position();
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(45, () -> {
            require(d.dragonSleeping() && d.sleepProgress == 20 && !d.canMoveAutonomously(), "Night triggers complete sleep pose");
            require(d.position().subtract(start).horizontalDistance() < 0.1, "Sleeping dragon does not wander");
            var observer = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            observer.getEntityData().assignValues(d.getEntityData().getNonDefaultValues());
            require(observer.dragonSleeping(), "Sleeping state synchronized");
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            d.saveWithoutId(output);
            var restored = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            require(restored.dragonSleeping(), "Sleeping survives save/reload");
            h.succeed();
        });
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 130, padding = 12)
    public void damageWakesSleepingDragonAndRetaliates(GameTestHelper h) {
        var d = dragon(h);
        var attacker = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        attacker.setPersistenceRequired(); attacker.setNoAi(true);
        attacker.setPos(d.position().add(3, 0, 0));
        attacker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
        attacker.setHealth(200);
        h.getLevel().addFreshEntity(attacker);
        h.getLevel().addFreshEntity(d);        var trace = new StringBuilder();
        h.onEachTick(() -> {
            if (!attacker.isRemoved() && d.tickCount <= 50) trace.append("\n").append(d.tickCount)
                    .append(" atk=").append(d.attackAnimation()).append('/').append(d.attackAnimationTick())
                    .append(" dst=").append(d.distanceTo(attacker)).append(" reach=").append(d.withinBiteReach(attacker))
                    .append(" los=").append(d.hasLineOfSight(attacker)).append(" can=").append(d.canAttack(attacker))
                    .append(" flying=").append(d.flying()).append(" water=").append(d.isInWater())
                    .append(" hp=").append(attacker.getHealth());
        });
        h.runAfterDelay(5, () -> {
            require(d.dragonSleeping(), "Dragon asleep before attack");
            require(d.hurtServer(h.getLevel(), d.damageSources().mobAttack(attacker), 1), "Actual attack on sleeping dragon");
            require(!d.dragonSleeping(), "Damage wakes immediately");
        });
        h.runAfterDelay(50, () -> {
            require(!d.dragonSleeping() && d.getTarget() == attacker && attacker.getHealth() < 200, "Awakened dragon actually retaliates: sleeping=" + d.dragonSleeping() + ", target=" + (d.getTarget() == attacker) + ", health=" + attacker.getHealth() + ", distance=" + d.distanceTo(attacker) + ", removed=" + attacker.isRemoved() + trace);
            h.succeed();
        });
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 70, padding = 12)
    public void followCommandAndMissingSupportWakeDragon(GameTestHelper h) {
        var d = dragon(h);
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(5, () -> {
            require(d.dragonSleeping(), "Night sleep starts");
            d.setCommand(2);
            require(!d.dragonSleeping(), "Follow command wakes immediately");
            d.setCommand(0);
            d.setDragonSleeping(true);
            for (int x = 22; x <= 26; x++) for (int z = 22; z <= 26; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.AIR);
        });
        h.runAfterDelay(8, () -> {
            require(!d.dragonSleeping(), "Removing support wakes dragon instead of leaving it floating asleep");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12, skyAccess = true)
    public void daylightWakesTamedDragonAndOwnerIsProtected(GameTestHelper h) {
        var d = dragon(h);
        var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        d.setTame(true, false); d.setOwner(owner);
        d.setDragonSleeping(true);
        require(!d.canAttack(owner), "Sleeping dragon cannot target owner");
        d.tick();
        require(!d.dragonSleeping() && d.canMoveAutonomously(), "Daylight wakes tamed dragon");
        h.succeed();
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 80, padding = 12)
    public void sleepingDragonWakesToDefendOwner(GameTestHelper h) {
        var d = dragon(h);
        var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        d.setTame(true, false); d.setOwner(owner);
        owner.setPos(d.position().add(0, 0, 5));
        // A food animal is now legitimately hunted before sleep; use a non-food aggressor.
        var enemy = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        enemy.setPersistenceRequired(); enemy.setNoAi(true);
        enemy.setPos(d.position().add(3, 0, 0));
        enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
        enemy.setHealth(200);
        h.getLevel().addFreshEntity(enemy);
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(5, () -> {
            require(d.dragonSleeping(), "Owned dragon asleep before attack");
            owner.tickCount = 10;
            require(owner.hurtServer(h.getLevel(), owner.damageSources().mobAttack(enemy), 1), "Owner actually damaged");
        });
        h.runAfterDelay(45, () -> {
            require(!d.dragonSleeping() && d.getTarget() == enemy && enemy.getHealth() < 200,
                    "Sleeping dragon wakes, targets aggressor and protects owner");
            h.succeed();
        });
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", padding = 12)
    public void sleepConfigurationAndFollowOverrideNight(GameTestHelper h) {
        var d = dragon(h);
        d.setDragonSleeping(true);
        var config = com.github.alexthe666.iceandfire.fabric.DragonCombatConfig.current;
        boolean original = config.doDragonsSleep;
        try {
            config.doDragonsSleep = false;
            d.tick();
            require(!d.dragonSleeping(), "Disabled sleep configuration wakes dragon");
        } finally { config.doDragonsSleep = original; }
        d.setCommand(2);
        d.tick();
        require(!d.dragonSleeping(), "Follow prevents night sleep");
        h.succeed();
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", padding = 12)
    public void selectingThreatWakesBeforeMovementGoals(GameTestHelper h) {
        var d = dragon(h);
        d.setDragonSleeping(true);
        var enemy = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        enemy.setPos(d.position().add(3, 0, 0));
        require(d.canAttack(enemy), "Sleep must not prevent defensive target selection");
        d.setTarget(enemy);
        require(!d.dragonSleeping() && d.canMoveOnGround(), "Target assignment wakes before combat navigation");
        d.setTarget(null);
        d.tick();
        require(!d.dragonSleeping(), "Clearing target preserves original twenty-tick sleep delay");
        h.succeed();
    }
}
