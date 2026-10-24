package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DragonDefenseGameTests {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static FireDragon dragon(GameTestHelper h, Player owner, int days) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        require(d != null, "Dragon created");
        if (owner != null) { d.setTame(true, false); d.setOwner(owner); }
        for (int i = 0; i < days; i++) d.growOneDay();
        d.setPos(h.absoluteVec(new Vec3(12.5, 2, 12.5)));
        d.setOnGround(true);
        d.setHealth(d.getMaxHealth());
        return d;
    }
    private static Mob victim(GameTestHelper h, Vec3 pos) {
        var v = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        require(v != null, "Victim created");
        v.setNoAi(true);
        v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        v.setHealth(200);
        v.setPos(pos);
        h.getLevel().addFreshEntity(v);
        return v;
    }
    private static void floor(GameTestHelper h) {
        for (int x = 1; x < 46; x++) for (int z = 1; z < 46; z++)
            h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 180, padding = 12, skyAccess = true)
    public void retaliationSelectsPursuesAndBitesWithoutRider(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 0);
        var v = victim(h, d.position().add(10, 0, 0));
        h.getLevel().addFreshEntity(d);
        var start = d.position();
        h.runAfterDelay(2, () -> require(d.hurtServer(h.getLevel(), d.damageSources().mobAttack(v), 1), "Actual incoming damage"));
        h.runAfterDelay(15, () -> require(d.getTarget() == v, "Retaliation goal selects its attacker"));
        h.runAfterDelay(150, () -> {
            require(d.position().distanceTo(start) > 3, "Ground pursuit must actually move");
            require(v.getHealth() < 200, "Autonomous bite damages its target");
            require(d.breathProgress == 0 && !d.flying(), "Hatchling remains a ground melee fighter");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 80, padding = 12)
    public void biteWaitsForImpactAndHitsOnlyOncePerAnimation(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 50);
        var v = victim(h, d.position().add(3, 0, 0));
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        h.runAfterDelay(12, () -> require(v.getHealth() == 200, "No damage before original bite impact window"));
        h.runAfterDelay(28, () -> {
            float damage = (int) d.getAttributeValue(Attributes.ATTACK_DAMAGE);
            require(Math.abs(v.getHealth() - (200 - damage)) < 0.01, "One hit within original 16..24 tick window");
            d.setCommand(1);
            require(d.getTarget() == null, "Sit clears combat target immediately");
        });
        h.runAfterDelay(60, () -> {
            float damage = (int) d.getAttributeValue(Attributes.ATTACK_DAMAGE);
            require(Math.abs(v.getHealth() - (200 - damage)) < 0.01 && d.attackAnimation() == DragonCombatState.NONE,
                    "Sit cancels animation and further damage");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 100, padding = 12)
    public void ownerAttackAndOwnerDefenseUseActualTargetGoals(GameTestHelper h) {
        floor(h);
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner, 25);
        owner.setPos(d.position().add(0, 0, 5));
        var first = victim(h, d.position().add(3, 0, 0));
        var second = victim(h, d.position().add(-3, 0, 0));
        h.getLevel().addFreshEntity(d);
        d.setCommand(2);
        h.runAfterDelay(2, () -> { owner.tickCount = 10; owner.setLastHurtMob(first); });
        h.runAfterDelay(12, () -> require(d.getTarget() == first, "Owner attack goal overrides ground escort"));
        h.runAfterDelay(35, () -> {
            require(first.getHealth() < 200, "Owner's target receives real bite damage");
            first.discard();
            owner.tickCount = 50;
            require(owner.hurtServer(h.getLevel(), owner.damageSources().mobAttack(second), 1), "Owner receives real damage");
        });
        h.runAfterDelay(65, () -> {
            require(d.getTarget() == second, "Owner defense selects new aggressor");
            require(second.getHealth() < 200, "Aggressor receives autonomous bite damage");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void alliesAndControlStatesCannotBeAutonomousTargets(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner, 50);
        var ally = dragon(h, owner, 0);
        var v = victim(h, d.position().add(3, 0, 0));
        require(!d.canAttack(owner) && !d.canAttack(ally), "Owner and same-owner pets protected");
        var creative = h.makeMockPlayer(GameType.CREATIVE);
        creative.getAbilities().instabuild = true;
        require(!d.canAttack(creative), "Creative players protected");
        d.setCommand(1);
        require(!d.canAttack(v), "Sit excludes defense");
        d.setCommand(0);
        d.setNoAi(true);
        require(!d.canAttack(v), "NoAI excludes defense");
        d.setNoAi(false);
        require(d.canAttack(v) && owner.startRiding(d), "Owner can mount normal adult");
        require(!d.canAttack(v), "Pilot takes precedence over autonomous combat");
        owner.stopRiding();
        ally.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(ally.shoulderCarrier() == owner && !ally.canAttack(v), "Shoulder baby cannot fight");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 60, padding = 12)
    public void wallShieldsVictimAndDeadTargetIsReleased(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 0);
        var v = victim(h, d.position().add(2, 0, 0));
        for (int y = 2; y < 7; y++) for (int z = 1; z < 46; z++)
            h.setBlock(new BlockPos(13, y, z), Blocks.STONE);
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        h.runAfterDelay(30, () -> {
            require(v.getHealth() == 200, "Wall blocks autonomous bite");
            v.discard();
        });
        h.runAfterDelay(40, () -> {
            require(d.getTarget() == null && d.attackAnimation() == DragonCombatState.NONE, "Removed victim cancels combat");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 70, padding = 12)
    public void targetChangeRestartsWindupAndNoAiCancelsDamage(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 50);
        var first = victim(h, d.position().add(3, 0, 0));
        var second = victim(h, d.position().add(-3, 0, 0));
        h.getLevel().addFreshEntity(d);
        d.setTarget(first);
        h.runAfterDelay(10, () -> d.setTarget(second));
        h.runAfterDelay(23, () -> require(first.getHealth() == 200 && second.getHealth() == 200,
                "Target switch cancels old bite and starts a fresh windup"));
        h.runAfterDelay(30, () -> {
            require(first.getHealth() == 200 && second.getHealth() < 200, "Only the new target receives the new bite");
            d.setNoAi(true);
        });
        h.runAfterDelay(60, () -> {
            float damage = (int) d.getAttributeValue(Attributes.ATTACK_DAMAGE);
            require(d.getTarget() == null && d.attackAnimation() == DragonCombatState.NONE
                    && Math.abs(second.getHealth() - (200 - damage)) < 0.01, "NoAI clears target and prevents further hits");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 160, padding = 12, skyAccess = true)
    public void airborneDefenseLandsBeforeGroundAttack(GameTestHelper h) {
        floor(h);
        var d = dragon(h, null, 25);
        d.setPos(d.position().add(0, 8, 0));
        d.setOnGround(false);
        d.setAutonomousFlight(true, false);
        var v = victim(h, d.position().add(3, -8, 0));
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        h.runAfterDelay(110, () -> {
            require(!d.flying() && d.onGround(), "Threat interrupts wandering flight and causes actual landing");
            require(v.getHealth() < 200, "Landed dragon engages threat");
            h.succeed();
        });
    }
}
