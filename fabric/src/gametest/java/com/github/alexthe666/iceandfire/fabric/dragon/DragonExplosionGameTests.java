package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class DragonExplosionGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int age, Vec3 pos) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setNoAi(true); d.setSilent(true); d.setPos(pos); d.refreshDimensions(); h.getLevel().addFreshEntity(d); return d;
    }
    private static net.minecraft.world.entity.animal.pig.Pig victim(GameTestHelper h, Vec3 pos) {
        var pig = EntityTypes.PIG.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); pig.setHealth(1000); pig.setNoAi(true);
        pig.setPos(pos); h.getLevel().addFreshEntity(pig); return pig;
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 30)
    public void disabledOptionAndJuvenileStagesCannotExplodeButAdultChargeCan(GameTestHelper h) {
        var previous = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); DragonCombatConfig.current = config; config.dragonGriefing = 2;
            var point = h.absoluteVec(new Vec3(24.5, 12, 24.5));
            var adult = dragon(h, 125, point.add(12, 0, 0)); var target = victim(h, point.add(1, 0, 0));
            DragonFireExplosion.trigger(h.getLevel(), adult, adult, point);
            require(target.getHealth() == 1000 && target.getDeltaMovement().equals(Vec3.ZERO), "Default false means no explosion damage or impulse");
            config.explosiveDragonBreath = true;
            var juvenile = dragon(h, 25, point.add(-12, 0, 0));
            DragonFireExplosion.trigger(h.getLevel(), juvenile, juvenile, point);
            require(target.getHealth() == 1000 && target.getDeltaMovement().equals(Vec3.ZERO), "Stage two zero-power explosion is omitted");
            DragonFireCombat.chargeImpact(h.getLevel(), adult, adult, point);
            require(target.getHealth() < 992 && target.getDeltaMovement().lengthSqr() > 0, "Actual charge impact applies optional native explosion beyond ordinary8 damage");
        } finally { DragonCombatConfig.current = previous; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 30)
    public void explosionProtectsOwnerAlliedDragonAndProxyPartsFromDamageAndImpulse(GameTestHelper h) {
        var previous = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.explosiveDragonBreath = true; config.dragonGriefing = 2; DragonCombatConfig.current = config;
            var point = h.absoluteVec(new Vec3(24.5, 12, 24.5));
            var owner = h.makeMockServerPlayerInLevel(); owner.setGameMode(GameType.SURVIVAL); owner.setPos(point.add(1, 0, 0));
            var source = dragon(h, 125, point.add(12, 0, 0)); source.setTame(true, false); source.setOwner(owner);
            var ally = dragon(h, 50, point.add(-1, 0, 0)); ally.setTame(true, false); ally.setOwner(owner); ally.tick();
            var target = victim(h, point.add(0, 0, 1)); float health = owner.getHealth(), allyHealth = ally.getHealth();
            owner.setDeltaMovement(Vec3.ZERO); ally.setDeltaMovement(Vec3.ZERO);
            DragonFireExplosion.trigger(h.getLevel(), source, source, point);
            require(target.getHealth() < 1000 && target.getDeltaMovement().lengthSqr() > 0, "Hostile target receives native damage and impulse");
            require(owner.getHealth() == health && owner.getDeltaMovement().equals(Vec3.ZERO), "Owner protected from damage AND knockback");
            require(ally.getHealth() == allyHealth && ally.getDeltaMovement().equals(Vec3.ZERO), "Same-owner dragon protected");
            require(ally.dragonParts().size() == 10 && ally.dragonParts().stream().allMatch(p -> p.getDeltaMovement().equals(Vec3.ZERO)), "Multipart proxies cannot multiply explosion damage or drift");
        } finally { DragonCombatConfig.current = previous; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 30)
    public void nativeExplosionHonorsAllTerrainGatesAndOriginalBlacklist(GameTestHelper h) {
        var previous = DragonCombatConfig.current; boolean rule = h.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
        try {
            var config = new DragonCombatConfig(); config.explosiveDragonBreath = true; DragonCombatConfig.current = config;
            var center = new BlockPos(24, 12, 24); var point = h.absoluteVec(Vec3.atCenterOf(center));
            var source = dragon(h, 125, point.add(12, 0, 0));
            h.setBlock(center, Blocks.WOOL.white());
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, false, h.getLevel().getServer());
            DragonFireExplosion.trigger(h.getLevel(), source, source, point); require(h.getBlockState(center).is(Blocks.WOOL.white()), "mobGriefing false preserves blocks");
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, true, h.getLevel().getServer());
            config.dragonGriefing = 2;
            DragonFireExplosion.trigger(h.getLevel(), source, source, point); require(h.getBlockState(center).is(Blocks.WOOL.white()), "No-grief config preserves blocks");
            config.dragonGriefing = 0; source.setTame(true, false); config.tamedDragonGriefing = false;
            DragonFireExplosion.trigger(h.getLevel(), source, source, point); require(h.getBlockState(center).is(Blocks.WOOL.white()), "Tamed-grief false preserves blocks");
            config.tamedDragonGriefing = true;
            DragonFireExplosion.trigger(h.getLevel(), source, source, point); require(h.getBlockState(center).isAir(), "Allowed explosion destroys ordinary wool at center");
            for (var protectedBlock : new net.minecraft.world.level.block.Block[]{Blocks.CHEST, Blocks.IRON_BARS, Blocks.BEDROCK}) {
                h.setBlock(center, protectedBlock);
                DragonFireExplosion.trigger(h.getLevel(), source, source, point);
                require(h.getBlockState(center).is(protectedBlock), "Blacklist/unbreakable block preserved: " + protectedBlock);
            }
        } finally { DragonCombatConfig.current = previous; h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, rule, h.getLevel().getServer()); }
        h.succeed();
    }
}
