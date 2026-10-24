package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonPreyGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void advance(FireDragon dragon) {
        // ServerLevel increments this before Entity.tick; direct fixture ticks must do the same.
        dragon.tickCount++;
        dragon.tick();
    }
    private static FireDragon dragon(GameTestHelper h, int days) {
        for (int x = 1; x < 47; x++) for (int z = 1; z < 47; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < days; i++) d.growOneDay();
        d.refreshDimensions();
        d.setPos(h.absoluteVec(new Vec3(24.5, 2, 24.5))); d.setOnGround(true);
        d.setHunger(50); d.getRandom().setSeed(725);
        return d;
    }
    private static net.minecraft.world.entity.Mob prey(GameTestHelper h, FireDragon d) {
        var p = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        p.setNoAi(true); p.setPos(d.position().add(3, 0, 0));
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); p.setHealth(1000);
        return p;
    }

    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 100, padding = 140)
    public void gripCarriesPreyHitsAfterFiftyFiveAndReleasesOnce(GameTestHelper h) {
        var d = dragon(h, 50); var p = prey(h, d);
        h.getLevel().addFreshEntity(d); h.getLevel().addFreshEntity(p);
        require(d.tryGrabPrey(p), "Valid small mob actually mounts into combat grip: dragonWidth=" + d.getBbWidth()
                + " preyWidth=" + p.getBbWidth() + " ground=" + d.onGround() + " move=" + d.canMoveOnGround()
                + " attack=" + d.canAttack(p) + " sight=" + d.hasLineOfSight(p));
        var start = d.position();
        float initialHealth = d.getHealth();
        h.runAfterDelay(30, () -> {
            require(d.grippedPrey() == p && p.getVehicle() == d && d.getControllingPassenger() == null, "Prey is tracked as prey, never pilot");
            require(p.getHealth() == 1000 && d.attackAnimation() == DragonCombatState.SHAKE, "No damage during windup and shake");
            var expected = d.position().add(DragonPreyMotion.offset(DragonGrowth.renderSize(d.ageDays()), d.getYRot(), d.attackAnimationTick()));
            require(p.position().distanceTo(expected) < 0.1, "Actual passenger follows mouth lift and shake");
            require(d.position().subtract(start).horizontalDistance() < 0.1 && !d.flying(), "Holder cannot wander or take off");
        });
        h.runAfterDelay(60, () -> require(!d.dragonSleeping() && d.attackAnimation() == DragonCombatState.SHAKE
                && !p.isPassenger(), "Night sleep waits for the final recovery frames after prey release"));
        h.runAfterDelay(70, () -> {
            float damage = (float) d.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2;
            require(d.grippedPrey() == null && !p.isPassenger() && d.attackAnimation() != DragonCombatState.SHAKE, "Prey released and animation ends");
            require(Math.abs(p.getHealth() - (1000 - damage)) < 0.01 && d.hunger() == 51, "Exactly one double-damage impact and one hunger point");
            require(Math.abs(d.getHealth() - Math.min(d.getMaxHealth(), initialHealth + damage * 0.5F)) < 0.01,
                    "Original shake impact heals half the successful damage");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void captureHonorsSizeSightControlsAndSinglePilotSeat(GameTestHelper h) {
        var d = dragon(h, 50); var p = prey(h, d);
        var owner = h.makeMockPlayer(GameType.SURVIVAL); d.setOwner(owner); d.setTame(true, false);
        require(!d.tryGrabPrey(owner), "Player is never a combat prey passenger");
        d.setCommand(2); require(!d.tryGrabPrey(p), "Follow prevents grab");
        d.setCommand(0); d.setNoAi(true); require(!d.tryGrabPrey(p), "NoAI prevents grab");
        d.setNoAi(false); d.setAutonomousFlight(true, false); require(!d.tryGrabPrey(p), "Airborne dragon cannot grab");
        d.setAutonomousFlight(false, false);
        for (int y = 2; y < 12; y++) h.setBlock(new BlockPos(26, y, 24), Blocks.STONE);
        require(!d.tryGrabPrey(p), "Wall blocks capture");
        for (int y = 2; y < 12; y++) h.setBlock(new BlockPos(26, y, 24), Blocks.AIR);
        require(d.tryGrabPrey(p), "Capture succeeds after obstruction removed");
        require(!owner.startRiding(d) && d.getPassengers().size() == 1, "No extra pilot seat while holding prey");
        d.setCommand(1);
        require(!p.isPassenger() && d.grippedPrey() == null && d.attackAnimation() == DragonCombatState.NONE, "Sit immediately releases prey");
        var baby = dragon(h, 0); var large = prey(h, baby);
        require(!baby.tryGrabPrey(large), "Hatchling cannot capture larger prey");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void noAiDeathAndNewAlliesReleaseWithoutDamageOrAiMutation(GameTestHelper h) {
        var d = dragon(h, 50); var p = prey(h, d);
        require(d.tryGrabPrey(p), "Prepare NoAI cancellation");
        d.setNoAi(true); advance(d);
        require(!p.isPassenger() && p.getHealth() == 1000 && p.isNoAi(), "NoAI cancels without damaging or rewriting prey AI");
        d = dragon(h, 50); p = prey(h, d);
        require(d.tryGrabPrey(p), "Prepare holder removal"); d.discard();
        require(!p.isPassenger() && p.getHealth() == 1000, "Removing holder releases prey immediately");
        d = dragon(h, 50);
        var owner = h.makeMockPlayer(GameType.SURVIVAL); d.setOwner(owner); d.setTame(true, false);
        var wolf = EntityTypes.WOLF.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        wolf.setNoAi(true); wolf.setPos(d.position().add(3, 0, 0));
        float health = wolf.getHealth();
        require(d.tryGrabPrey(wolf), "Wild small wolf can be captured");
        wolf.setOwner(owner); wolf.setTame(true, false); advance(d);
        require(!wolf.isPassenger() && wolf.getHealth() == health, "Becoming an allied pet cancels hold before damage");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void nativePassengerSaveRestoresGripUuidAndRemainingWindup(GameTestHelper h) {
        var d = dragon(h, 50); var p = prey(h, d);
        require(d.tryGrabPrey(p), "Prepare save during grip");
        for (int i = 0; i < 20; i++) advance(d);
        require(d.grippedPrey() == p && d.attackAnimationTick() == 20, "Source still holds original prey at saved tick twenty");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        d.saveWithoutId(output);
        var id = p.getUUID();
        d.discard(); p.discard();
        var decoded = EntityType.loadEntityRecursive(IafContent.FIRE_DRAGON,
                TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()),
                h.getLevel(), EntitySpawnReason.LOAD, EntityProcessor.NOP);
        require(decoded instanceof FireDragon, "Native recursive entity loader restores dragon and passengers");
        var restored = (FireDragon) decoded;
        require(restored.getFirstPassenger() != null && restored.getFirstPassenger().getUUID().equals(id), "Native passenger graph preserves prey UUID before first tick");
        advance(restored);
        require(restored.grippedPrey() != null && restored.grippedPrey().getUUID().equals(id)
                && restored.attackAnimation() == DragonCombatState.SHAKE && restored.attackAnimationTick() == 21,
                "UUID and windup resume without restarting or changing prey: width=" + restored.getBbWidth()
                        + " passengers=" + restored.getPassengers() + " attack=" + restored.attackAnimation() + " tick=" + restored.attackAnimationTick());
        var restoredPrey = restored.grippedPrey();
        for (int i = 0; i < 49; i++) advance(restored);
        float damage = (float) restored.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2;
        require(!restoredPrey.isPassenger() && Math.abs(restoredPrey.getHealth() - (1000 - damage)) < 0.01,
                "Restored grip releases with one remaining impact");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void disabledBiteHealingAlsoDisablesShakeHealing(GameTestHelper h) {
        var config = com.github.alexthe666.iceandfire.fabric.DragonCombatConfig.current;
        boolean old = config.canDragonsHealFromBiting;
        try {
            config.canDragonsHealFromBiting = false;
            var d = dragon(h, 50); var p = prey(h, d); p.setNoAi(false);
            float initialHealth = d.getHealth();
            require(d.tryGrabPrey(p), "Prepare disabled-healing impact");
            for (int i = 0; i < 56; i++) advance(d);
            require(p.getHealth() < 1000 && d.getHealth() == initialHealth && d.hunger() == 51
                    && !p.isPassenger() && !p.isNoAi(), "Damage and hunger retained while configured healing is disabled and prey AI remains enabled: preyHealth="
                    + p.getHealth() + " holderHealth=" + d.getHealth() + " initial=" + initialHealth + " hunger=" + d.hunger()
                    + " passenger=" + p.isPassenger() + " preyNoAI=" + p.isNoAi() + " animation=" + d.attackAnimation() + " tick=" + d.attackAnimationTick());
            var owner = h.makeMockPlayer(GameType.SURVIVAL); d.setOwner(owner); d.setTame(true, false);
            require(owner.startRiding(d), "Owner can board the freed pilot seat during recovery");
            advance(d);
            require(d.getControllingPassenger() == owner && d.attackAnimation() == DragonCombatState.NONE,
                    "Boarding owner immediately takes priority over remaining shake recovery frames");
        } finally { config.canDragonsHealFromBiting = old; }
        h.succeed();
    }
}
