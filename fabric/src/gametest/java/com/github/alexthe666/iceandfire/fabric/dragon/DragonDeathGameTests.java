package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DragonDeathGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int age) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setNoAi(true); d.setSilent(true); d.refreshDimensions();
        d.setPos(h.absoluteVec(new Vec3(24.5, 10, 24.5)));
        h.getLevel().addFreshEntity(d); return d;
    }
    private static int totalXp(ServerLevel level, AABB area) {
        int total = 0;
        for (var orb : level.getEntitiesOfClass(ExperienceOrb.class, area)) {
            // Native orb merging stores several equal-value orbs in Count, not in Value.
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            orb.saveWithoutId(output);
            total += orb.getValue() * output.buildResult().getIntOr("Count", 1);
        }
        return total;
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 160)
    public void playerKillsAwardOriginalExperienceAtAllStagesWithoutDuplicatingInventory(GameTestHelper h) {
        for (int x = 16; x <= 32; x++) for (int z = 16; z <= 32; z++) h.setBlock(new BlockPos(x, 8, z), Blocks.STONE);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        int[] ages = {0, 25, 50, 75, 100}, xp = {5, 20, 150, 300, 650};
        for (int i = 0; i < ages.length; i++) {
            var d = dragon(h, ages[i]); var area = d.getBoundingBox().inflate(10);
            d.dragonInventory.setItem(1, new ItemStack(Items.DIAMOND, 2));
            require(d.hurtServer(h.getLevel(), d.damageSources().playerAttack(player), 10000), "Actual lethal player damage");
            require(d.getHealth() <= 0 && totalXp(h.getLevel(), area) == xp[i], "Native XP orb total at age " + ages[i]);
            require(!d.hurtServer(h.getLevel(), d.damageSources().playerAttack(player), 10000), "Dead dragon cannot be killed twice");
            for (int tick = 0; tick < 25 && !d.isRemoved(); tick++) { d.tickCount++; d.tick(); }
            require(totalXp(h.getLevel(), area) == xp[i], "Death ticks must not award XP a second time");
            var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, area);
            require(drops.stream().filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum() == 2,
                    "Saved inventory drops once, independently of XP");
            drops.forEach(ItemEntity::discard);
            h.getLevel().getEntitiesOfClass(ExperienceOrb.class, area).forEach(ExperienceOrb::discard);
            if (!d.isRemoved()) d.discard();
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 160)
    public void environmentalDeathsAndDisabledMobDropsDoNotAwardExperience(GameTestHelper h) {
        var d = dragon(h, 100); var area = d.getBoundingBox().inflate(10);
        require(d.hurtServer(h.getLevel(), d.damageSources().genericKill(), 10000), "Environmental lethal damage");
        require(totalXp(h.getLevel(), area) == 0, "Native player-credit rule is retained"); d.discard();
        var old = h.getLevel().getGameRules().get(GameRules.MOB_DROPS);
        try {
            h.getLevel().getGameRules().set(GameRules.MOB_DROPS, false, h.getLevel().getServer());
            var second = dragon(h, 100); var player = h.makeMockPlayer(GameType.SURVIVAL);
            require(second.hurtServer(h.getLevel(), second.damageSources().playerAttack(player), 10000), "Player lethal damage with drops disabled");
            require(totalXp(h.getLevel(), area) == 0, "Native mob-drops rule is retained"); second.discard();
        } finally { h.getLevel().getGameRules().set(GameRules.MOB_DROPS, old, h.getLevel().getServer()); }
        h.succeed();
    }
}
