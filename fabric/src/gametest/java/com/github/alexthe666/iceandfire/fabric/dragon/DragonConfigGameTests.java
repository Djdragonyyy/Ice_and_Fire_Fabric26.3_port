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
}
