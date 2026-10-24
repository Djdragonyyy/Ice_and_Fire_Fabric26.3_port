package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DragonHuntingGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, Player owner) {
        for (int x = 1; x < 47; x++) for (int z = 1; z < 47; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < 25; i++) d.growOneDay();
        if (owner != null) { d.setOwner(owner); d.setTame(true, false); }
        d.setPos(h.absoluteVec(new Vec3(12.5, 2, 24.5)));
        d.setOnGround(true); d.setHunger(50); d.getRandom().setSeed(725);
        return d;
    }
    private static net.minecraft.world.entity.Mob cow(GameTestHelper h, FireDragon d) {
        var cow = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        cow.setNoAi(true); cow.setPos(d.position().add(12, 0, 0));
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); cow.setHealth(1000);
        h.getLevel().addFreshEntity(cow);
        return cow;
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 150, padding = 140)
    public void hungryTamedDragonSelectsAndAttacksOriginalTaggedPrey(GameTestHelper h) {
        var d = dragon(h, h.makeMockPlayer(GameType.SURVIVAL));
        var cow = cow(h, d);
        require(cow.getType().builtInRegistryHolder().is(DragonHuntGoal.PREY), "Original nested prey tag resolves under singular 26.3 directory");
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(20, () -> require(d.getTarget() == cow, "Hungry tame dragon finds prey without retaliation or owner attack"));
        h.runAfterDelay(115, () -> {
            require(cow.getHealth() < 1000, "Selected prey receives real autonomous combat damage");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void candidateRulesProtectPlayersPetsAndNonFood(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner);
        var cow = cow(h, d);
        require(DragonHuntGoal.candidate(d, cow, false), "Hungry tame dragon accepts cow");
        d.setHunger(90); require(!DragonHuntGoal.candidate(d, cow, false), "Ninety hunger stops new prey hunting");
        d.setHunger(89); require(DragonHuntGoal.candidate(d, cow, false), "Threshold is original strict below ninety");
        var stranger = h.makeMockPlayer(GameType.SURVIVAL); stranger.setUUID(java.util.UUID.randomUUID());
        require(!DragonHuntGoal.candidate(d, owner, false) && !DragonHuntGoal.candidate(d, stranger, false), "Tamed dragons do not hunt owner or stranger players");
        var wolf = EntityTypes.WOLF.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        wolf.setOwner(stranger); wolf.setTame(true, false);
        require(!DragonHuntGoal.candidate(d, wolf, false), "Other players' tamed pets are not prey");
        var villager = EntityTypes.VILLAGER.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        var zombie = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        require(!DragonHuntGoal.candidate(d, villager, false) && !DragonHuntGoal.candidate(d, zombie, false), "Villagers and non-food hostiles excluded from tame hunting");
        d.setTame(false, false); d.setOwner(null); d.setHunger(100);
        require(DragonHuntGoal.candidate(d, stranger, true) && DragonHuntGoal.candidate(d, villager, true), "Wild aggression includes survival players and villagers independently of tame diet");
        var creative = h.makeMockPlayer(GameType.CREATIVE);
        require(!DragonHuntGoal.candidate(d, creative, true), "Creative players remain protected");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void huntingRespectsCommandsSleepLoveAndNoAi(GameTestHelper h) {
        var d = dragon(h, h.makeMockPlayer(GameType.SURVIVAL)); cow(h, d);
        var goal = new DragonHuntGoal(d, false);
        d.setCommand(1); require(!goal.canUse(), "Sit prevents hunting");
        d.setCommand(2); require(!goal.canUse(), "Follow prevents hunting");
        d.setCommand(0); d.setNoAi(true); require(!goal.canUse(), "NoAI prevents hunting");
        d.setNoAi(false); d.setDragonSleeping(true); require(!goal.canUse(), "Sleeping is not interrupted by hunting");
        d.setDragonSleeping(false); d.setInLoveTime(600); require(!goal.canUse(), "Courtship keeps priority");
        d.resetLove();
        boolean found = false;
        for (int i = 0; i < 40; i++) found |= goal.canUse();
        require(found, "Removing controls actually re-enables search");
        goal.start();
        d.setCommand(2); require(!goal.canContinueToUse(), "Switching to follow cancels ongoing hunt"); goal.stop();
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 150, padding = 140)
    public void reachableDroppedMeatTakesPriorityOverHunting(GameTestHelper h) {
        var d = dragon(h, h.makeMockPlayer(GameType.SURVIVAL));
        d.setHunger(80);
        var cow = cow(h, d);
        var p = d.position().add(1, 0, 0);
        var meat = new net.minecraft.world.entity.item.ItemEntity(h.getLevel(), p.x, p.y, p.z, new ItemStack(Items.BEEF));
        meat.setNoPickUpDelay(); meat.setDeltaMovement(Vec3.ZERO); h.getLevel().addFreshEntity(meat);
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(110, () -> {
            require(!meat.isAlive() && d.hunger() == 100, "Original dropped-food priority reaches fullness");
            require(cow.getHealth() == 1000 && d.getTarget() == null, "Available meat prevents unnecessary prey attack");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 60, padding = 140)
    public void retaliationPreemptsCloserFoodPrey(GameTestHelper h) {
        var d = dragon(h, h.makeMockPlayer(GameType.SURVIVAL));
        cow(h, d);
        var attacker = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        attacker.setPersistenceRequired(); attacker.setNoAi(true); attacker.setPos(d.position().add(16, 0, 0)); h.getLevel().addFreshEntity(attacker);
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(2, () -> require(d.hurtServer(h.getLevel(), d.damageSources().mobAttack(attacker), 1), "Retaliation fixture must actually damage dragon"));
        h.runAfterDelay(12, () -> {
            require(d.getTarget() == attacker, "Defensive priority preempts non-defensive hunt even for non-food attacker: target="
                    + d.getTarget() + " lastHurt=" + d.getLastHurtByMob() + " attacker=" + attacker + " ticks=" + d.tickCount);
            h.succeed();
        });
    }

    @GameTest
    public void originalSearchConfigUsesDefaultAndAttributeCap(GameTestHelper h) {
        var old = DragonCombatConfig.current;
        try {
            DragonCombatConfig.current = new DragonCombatConfig();
            var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED); d.growOneDay();
            require(d.getAttributeValue(Attributes.FOLLOW_RANGE) == 128, "Original default search range");
            DragonCombatConfig.current.dragonTargetSearchLength = 32; d.growOneDay();
            require(d.getAttributeValue(Attributes.FOLLOW_RANGE) == 32, "Configured range used by real attribute");
            DragonCombatConfig.current.dragonTargetSearchLength = 10000; d.growOneDay();
            require(d.getAttributeValue(Attributes.FOLLOW_RANGE) == 2048, "Original attribute cap retained");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
}
