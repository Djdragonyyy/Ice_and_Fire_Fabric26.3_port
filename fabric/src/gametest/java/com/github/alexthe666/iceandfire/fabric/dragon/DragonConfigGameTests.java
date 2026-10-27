package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonConfigGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @GameTest
    public void configuredHomeRadiusAndFlightCeilingRespectWorldBounds(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.maxDragonFlight = 100; config.dragonWanderFromHomeDistance = 8;
            DragonCombatConfig.current = config;
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            var home = h.absolutePos(new BlockPos(2, 2, 2));
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            d.saveWithoutId(output);
            output.putBoolean("HasHomePosition", true);
            output.putInt("HomeAreaX", home.getX()); output.putInt("HomeAreaY", home.getY()); output.putInt("HomeAreaZ", home.getZ());
            output.putString("HomeDimension", h.getLevel().dimension().identifier().toString());
            d.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            require(d.getHomeRadius() == 8 && d.isWithinHome(Vec3.atCenterOf(home).add(7, 0, 0))
                    && !d.isWithinHome(Vec3.atCenterOf(home).add(8, 0, 0)), "Loaded home uses configured strict radius");
            require(d.flightManager.maxHeight() == 100, "Configured absolute flight ceiling");
            config.maxDragonFlight = Integer.MAX_VALUE;
            require(d.flightManager.maxHeight() == h.getLevel().getMaxY() - (int) Math.ceil(d.getBbHeight()) - 1,
                    "Native world ceiling still leaves room for the dragon body");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
    @GameTest
    public void configuredGrowthStatsAndHungerDriveRealAttributesAndTicks(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonHealth = 1000; config.dragonAttackDamage = 100; config.dragonHungerTickRate = 2;
            DragonCombatConfig.current = config;
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            d.setNoAi(true); d.setPos(h.absoluteVec(new Vec3(2.5, 3, 2.5)));
            for (int i = 0; i < 125; i++) d.growOneDay();
            require(d.getMaxHealth() == 1000 && d.getAttributeValue(Attributes.ATTACK_DAMAGE) == 101, "Original growth formulas use configured real attributes");
            d.setHunger(50);
            for (int i = 0; i < 4; i++) { d.tickCount++; d.tick(); }
            require(d.hunger() == 48, "Configured hunger interval applies at ticks two and four");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void configuredEggTimeReadsLegacyIntAndCannotOverflowAtMaximum(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); DragonCombatConfig.current = config;
            for (int sample = 0; sample < 2; sample++) {
                config.dragonEggTime = sample == 0 ? 1 : Integer.MAX_VALUE;
                var egg = IafContent.DRAGON_EGG.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
                BlockPos pos = new BlockPos(8 + sample * 12, 2, 8);
                h.setBlock(pos.below(), Blocks.STONE); egg.setPos(h.absoluteVec(Vec3.atBottomCenterOf(pos)));
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
                egg.saveWithoutId(output);
                if (sample == 0) output.putInt("DragonAge", 1); else output.putLong("DragonAge", Integer.MAX_VALUE);
                egg.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
                h.setBlock(pos, Blocks.FIRE); egg.tick();
                require(egg.isRemoved(), "Configured threshold hatches after one more fire tick for legacy int and maximum configured time");
            }
            require(h.getLevel().getEntitiesOfClass(FireDragon.class, h.getBounds()).size() == 2, "Exactly two babies for the two threshold cases");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void originalWalkingInputAndMovementUseServerModifier(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var cfg = new DragonCombatConfig(); cfg.dragonFlightSpeedMod = 0.5; DragonCombatConfig.current = cfg;
            for (int x = 1; x < 24; x++) for (int z = 1; z < 24; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            for (int i = 0; i < 50; i++) d.growOneDay();
            d.setNoAi(true); d.setPos(h.absoluteVec(new Vec3(12.5, 2, 12.5))); d.setOnGround(true);
            var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            d.setOwner(owner); d.setTame(true, false); owner.setPos(d.position());
            require(owner.startRiding(d), "Owner mounts grounded adult");
            float speed = (float) d.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.9F;
            owner.xxa = 1; owner.zza = 1; owner.setSprinting(true);
            var input = d.getRiddenInput(owner, new Vec3(0, -0.3, 0));
            require(input.x == 0.5 && input.y == -0.3 && Math.abs(input.z - speed * 1.2F) < 1.0E-7,
                    "Original ground strafe, incomingY, speed and sprint");
            owner.zza = -1; owner.setSprinting(false);
            require(Math.abs(d.getRiddenInput(owner, Vec3.ZERO).z + speed * 0.2F) < 1.0E-7, "Original reverse factor");
            owner.xxa = 0; owner.zza = 1;
            var before = d.position(); d.setSpeed(d.getRiddenSpeed(owner)); d.setDeltaMovement(Vec3.ZERO);
            d.travel(d.getRiddenInput(owner, Vec3.ZERO));
            require(d.getZ() > before.z && !d.flying(), "Native ground physics moves the configured adult");
            cfg.dragonFlightSpeedMod = 0; d.tickCount++; d.tick();
            d.setDeltaMovement(Vec3.ZERO); d.setSpeed(d.getRiddenSpeed(owner)); before = d.position();
            d.travel(d.getRiddenInput(owner, Vec3.ZERO));
            require(d.position().subtract(before).horizontalDistance() < 1.0E-8, "Zero modifier produces no driven forward motion");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }

    @GameTest
    public void serverModifierIsUpdatedAndCopiedToNativeObserverWithoutPersistence(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var cfg = new DragonCombatConfig(); cfg.dragonFlightSpeedMod = 0.5; DragonCombatConfig.current = cfg;
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED); d.setNoAi(true);
            var observer = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            for (float value : new float[]{0, 2, 0.75F}) {
                cfg.dragonFlightSpeedMod = value; d.tickCount++; d.tick();
                observer.getEntityData().assignValues(d.getEntityData().getNonDefaultValues());
                require(d.getFlightSpeedModifier() == value && observer.getFlightSpeedModifier() == value,
                        "Native synchronized snapshot carries server value " + value);
            }
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            d.saveWithoutId(output); cfg.dragonFlightSpeedMod = 1.5;
            var restored = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            require(restored.getFlightSpeedModifier() == 1.5, "Server configuration replaces stale saved-world speed");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void autonomousEscortUsesConfiguredAccelerationWithoutChangingItsRoute(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            var cfg = new DragonCombatConfig(); cfg.dragonFlightSpeedMod = 0; DragonCombatConfig.current = cfg;
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            for (int i = 0; i < 25; i++) d.growOneDay();
            var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            d.setOwner(owner); d.setTame(true, false); d.setHunger(100);
            d.setPos(h.absoluteVec(new Vec3(20.5, 16, 20.5))); owner.setPos(d.position().add(10, 0, 0));
            d.setCommand(2); d.setOnGround(false); d.setAutonomousFlight(true, false); h.getLevel().addFreshEntity(d);
            d.tick();
            require(d.flying() && d.flightManager.target() != null, "Escort route remains valid at zero modifier");
            require(d.getDeltaMovement().lengthSqr() < 1.0E-8, "Zero modifier contributes no cruise or climb acceleration");
            Vec3 firstTarget = d.flightManager.target();
            // Test acceleration, not the first turning frame: a perpendicular heading has
            // zero X acceleration and vanilla legitimately removes sub-.003 axis motion.
            d.setYRot(-90); d.yBodyRot = d.yHeadRot = -90;
            float headingBefore = d.getYRot();
            Vec3 predictedAcceleration = DragonFlightSteering.towardsWaypoint(firstTarget.subtract(d.position()), headingBefore, 0.6, false, 1).acceleration();
            require(predictedAcceleration.x > 0.003, "Speed fixture faces its eastward route above the native dead zone");
            cfg.dragonFlightSpeedMod = 1; d.tick();
            require(firstTarget.equals(d.flightManager.target()), "Changing speed preserves the same escort destination");
            require(d.getFlightSpeedModifier() == 1 && d.getDeltaMovement().horizontalDistance() > 1.0E-5,
                    "Same escort route resumes actual horizontal acceleration when server restores speed: pos=" + d.position()
                            + " velocity=" + d.getDeltaMovement() + " modifier=" + d.getFlightSpeedModifier()
                            + " targetBefore=" + firstTarget + " targetAfter=" + d.flightManager.target()
                            + " flying=" + d.flying() + " hovering=" + d.hovering() + " collision=" + d.horizontalCollision
                            + " owner=" + owner.position() + " ownerVelocity=" + owner.getDeltaMovement()
                            + " directClear=" + (firstTarget != null && DragonFlightClearance.clear(d, d.position(), firstTarget))
                            + " headingBefore=" + headingBefore + " predictedAcceleration=" + predictedAcceleration);
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
}
