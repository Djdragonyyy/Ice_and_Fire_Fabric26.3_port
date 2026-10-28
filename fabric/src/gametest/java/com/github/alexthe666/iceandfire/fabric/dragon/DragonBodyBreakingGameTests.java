package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class DragonBodyBreakingGameTests {
    private static FireDragon dragon(GameTestHelper h, int age) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.refreshDimensions(); d.setHunger(100); d.setPos(h.absoluteVec(new Vec3(24.5, 2, 24.5))); d.setOnGround(true);
        return d;
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void originalBodyBoundsHardnessProtectionAndNativeDrops(GameTestHelper h) {
        var d = dragon(h, 50); var level = h.getLevel();
        var dirt = h.absolutePos(new BlockPos(24, 3, 24)); var wood = dirt.east(); var stone = dirt.west();
        var iron = dirt.north(); var chest = dirt.south(); var water = dirt.above();
        level.setBlockAndUpdate(dirt, Blocks.DIRT.defaultBlockState()); level.setBlockAndUpdate(wood, Blocks.OAK_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState()); level.setBlockAndUpdate(iron, Blocks.IRON_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState()); level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        var resistant = dirt.north(2); level.setBlockAndUpdate(resistant, Blocks.ENCHANTING_TABLE.defaultBlockState());
        var floor = h.absolutePos(new BlockPos(24, 1, 24)); level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        boolean oldRule = level.getGameRules().get(GameRules.MOB_GRIEFING);
        int oldGrief = DragonCombatConfig.current.dragonGriefing, oldCooldown = DragonCombatConfig.current.dragonBreakBlockCooldown;
        double oldDrops = DragonCombatConfig.current.dragonBlockBreakingDropChance;
        try {
            level.getGameRules().set(GameRules.MOB_GRIEFING, true, level.getServer());
            DragonCombatConfig.current.dragonGriefing = 0; DragonCombatConfig.current.dragonBlockBreakingDropChance = 1;
            DragonBodyBreaking.clearBody(d, level);
            h.assertTrue(level.getBlockState(dirt).isAir() && level.getBlockState(wood).isAir() && level.getBlockState(stone).isAir(), "Stage3 clears actual weak body obstacles");
            h.assertTrue(level.getBlockState(iron).is(Blocks.IRON_BLOCK) && level.getBlockState(chest).is(Blocks.CHEST)
                    && !level.getFluidState(water).isEmpty() && level.getBlockState(floor).is(Blocks.STONE), "Hardness, protected chest, fluids and floor remain");
            var drops = level.getEntitiesOfClass(ItemEntity.class, d.getBoundingBox().inflate(6));
            h.assertTrue(drops.stream().mapToInt(e -> e.getItem().is(Items.OAK_PLANKS) ? e.getItem().getCount() : 0).sum() == 1, "Native destruction drops exactly one plank at chance1");
            h.assertTrue(drops.stream().noneMatch(e -> e.getItem().is(Items.DIRT) || e.getItem().is(Items.COBBLESTONE)), "Original dirt/stone no-drop tag suppresses actual drops");
            h.assertTrue(Blocks.OAK_LEAVES.defaultBlockState().is(DragonBodyBreaking.NO_DROPS) == false, "Original no-drop tag does not invent a leaves exclusion");
            var ceiling = dirt.above(2); level.setBlockAndUpdate(ceiling, Blocks.OAK_PLANKS.defaultBlockState());
            var timer = new DragonBodyBreaking(); DragonCombatConfig.current.dragonBreakBlockCooldown = 5; timer.tick(d, level);
            h.assertTrue(level.getBlockState(ceiling).is(Blocks.OAK_PLANKS), "Positive cooldown waits before clearance");
            DragonCombatConfig.current.dragonBreakBlockCooldown = 0; timer.tick(d, level);
            h.assertTrue(level.getBlockState(ceiling).isAir(), "Changing cooldown to zero takes effect immediately, including a pending timer");
            DragonCombatConfig.current.dragonBreakBlockCooldown = oldCooldown;
            var adult = dragon(h, 75); DragonCombatConfig.current.dragonGriefing = 1; DragonBodyBreaking.clearBody(adult, level);
            h.assertTrue(level.getBlockState(iron).is(Blocks.IRON_BLOCK), "Weak-only grief mode keeps the hardness2 ceiling for stage4");
            DragonCombatConfig.current.dragonGriefing = 0; DragonBodyBreaking.clearBody(adult, level);
            h.assertTrue(level.getBlockState(iron).isAir() && level.getBlockState(chest).is(Blocks.CHEST)
                    && level.getBlockState(resistant).is(Blocks.ENCHANTING_TABLE), "Stage4 reaches hardness5 but keeps protected and blast-resistant blocks");
        } finally {
            DragonCombatConfig.current.dragonGriefing = oldGrief; DragonCombatConfig.current.dragonBlockBreakingDropChance = oldDrops;
            DragonCombatConfig.current.dragonBreakBlockCooldown = oldCooldown;
            level.getGameRules().set(GameRules.MOB_GRIEFING, oldRule, level.getServer());
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void bodyClearanceHonorsStageGriefingSleepAndCommands(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(24, 3, 24));
        level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        var child = dragon(h, 25); var d = dragon(h, 50);
        boolean oldRule = level.getGameRules().get(GameRules.MOB_GRIEFING);
        int oldGrief = DragonCombatConfig.current.dragonGriefing; boolean oldTamed = DragonCombatConfig.current.tamedDragonGriefing;
        try {
            level.getGameRules().set(GameRules.MOB_GRIEFING, true, level.getServer()); DragonCombatConfig.current.dragonGriefing = 0;
            DragonBodyBreaking.clearBody(child, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Stage2 cannot clear body terrain");
            DragonCombatConfig.current.dragonGriefing = 2; DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Config grief2 blocks clearance");
            DragonCombatConfig.current.dragonGriefing = 0; level.getGameRules().set(GameRules.MOB_GRIEFING, false, level.getServer());
            DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Native mobGriefingfalse blocks clearance");
            level.getGameRules().set(GameRules.MOB_GRIEFING, true, level.getServer()); d.setTame(true, false); DragonCombatConfig.current.tamedDragonGriefing = false;
            DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Tamed grief option blocks clearance");
            DragonCombatConfig.current.tamedDragonGriefing = true; d.setCommand(1); DragonBodyBreaking.clearBody(d, level);
            h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Ordered sit blocks unpiloted clearance"); d.setCommand(0);
            d.setDragonSleeping(true); DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Sleeping blocks clearance");
            d.setDragonSleeping(false); d.sleepProgress = 10; DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Recovering sleep pose blocks clearance");
            d.sleepProgress = 0; d.setNoAi(true); DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "NoAI cannot clear body terrain");
            d.setNoAi(false); DragonBodyBreaking.clearBody(d, level); h.assertTrue(level.getBlockState(pos).isAir(), "Eligible awakened adult clears actual block");
        } finally {
            DragonCombatConfig.current.dragonGriefing = oldGrief; DragonCombatConfig.current.tamedDragonGriefing = oldTamed;
            level.getGameRules().set(GameRules.MOB_GRIEFING, oldRule, level.getServer());
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100, maxTicks = 30)
    public void actualNativeAiUsesBodyClearanceCooldown(GameTestHelper h) {
        for (int x = 20; x <= 29; x++) for (int z = 20; z <= 29; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.BEDROCK);
        var d = dragon(h, 50); d.setTame(true, false); h.getLevel().addFreshEntity(d);
        var ceiling = h.absolutePos(new BlockPos(24, 5, 24)); h.getLevel().setBlockAndUpdate(ceiling, Blocks.OAK_PLANKS.defaultBlockState());
        h.assertTrue(DragonCombatConfig.current.dragonBreakBlockCooldown == 5, "Native fixture uses original default cooldown5");
        h.runAtTickTime(4, () -> h.assertTrue(h.getLevel().getBlockState(ceiling).is(Blocks.OAK_PLANKS), "Body clearance does not run before its cooldown"));
        h.runAtTickTime(8, () -> { h.assertTrue(!d.isNoAi() && h.getLevel().getBlockState(ceiling).isAir(), "Native customServerAiStep clears obstacle on its own cooldown"); h.succeed(); });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void actualMountedOwnerKeepsOriginalBodyClearanceAndGriefingGate(GameTestHelper h) {
        var d = dragon(h, 50); var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        d.setTame(true, false); d.setOwner(owner); d.setCommand(1);
        h.assertTrue(owner.startRiding(d) && d.getControllingPassenger() == owner && !d.canMoveAutonomously(), "Real native passenger is the pilot while autonomous movement is blocked");
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(24, 3, 24)); level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        boolean old = level.getGameRules().get(GameRules.MOB_GRIEFING);
        try {
            level.getGameRules().set(GameRules.MOB_GRIEFING, false, level.getServer()); DragonBodyBreaking.clearBody(d, level);
            h.assertTrue(level.getBlockState(pos).is(Blocks.DIRT), "Piloting does not bypass native mobGriefingfalse");
            level.getGameRules().set(GameRules.MOB_GRIEFING, true, level.getServer()); DragonBodyBreaking.clearBody(d, level);
            h.assertTrue(level.getBlockState(pos).isAir(), "Mounted owner retains original body clearance despite command1");
        } finally { owner.stopRiding(); level.getGameRules().set(GameRules.MOB_GRIEFING, old, level.getServer()); }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100, maxTicks = 240)
    public void actualNativePursuitClearsSoftOverhangWithoutBreakingProtectedTunnel(GameTestHelper h) {
        for (int x = 21; x <= 28; x++) for (int z = 19; z <= 39; z++) { h.setBlock(new BlockPos(x, 1, z), Blocks.STONE); h.setBlock(new BlockPos(x, 5, z), Blocks.BEDROCK); }
        for (int z = 19; z <= 39; z++) for (int y = 2; y <= 4; y++) { h.setBlock(new BlockPos(22, y, z), Blocks.BEDROCK); h.setBlock(new BlockPos(27, y, z), Blocks.BEDROCK); }
        for (int x = 23; x <= 26; x++) for (int y = 2; y <= 4; y++) { h.setBlock(new BlockPos(x, y, 20), Blocks.BEDROCK); h.setBlock(new BlockPos(x, y, 38), Blocks.BEDROCK); }
        for (int x = 23; x <= 26; x++) for (int y = 3; y <= 4; y++) h.setBlock(new BlockPos(x, y, 28), Blocks.DIRT);
        var d = dragon(h, 50); d.setPos(h.absoluteVec(new Vec3(24.5, 2, 23.5)));
        var enemy = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        enemy.setNoAi(true); enemy.setPersistenceRequired(); enemy.setPos(h.absoluteVec(new Vec3(24.5, 2, 35.5)));
        enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(5000); enemy.setHealth(5000);
        h.getLevel().addFreshEntity(enemy); h.getLevel().addFreshEntity(d); d.setTarget(enemy);
        var trace = new StringBuilder();
        double[] furthestZ = {d.getZ()};
        boolean[] passedWhilePursuing = {false};
        h.onEachTick(() -> { furthestZ[0] = Math.max(furthestZ[0], d.getZ());
            if (d.getZ() > h.absolutePos(new BlockPos(0, 0, 29)).getZ() && d.getTarget() == enemy) passedWhilePursuing[0] = true;
            if (d.tickCount % 20 == 0) trace.append("\n").append(d.tickCount).append(" pos=").append(d.position().subtract(h.absoluteVec(Vec3.ZERO)))
                .append(" target=").append(d.getTarget() == enemy).append(" move=").append(d.canMoveAutonomously()).append(" flight=").append(d.flying())
                .append(" grip=").append(d.grippedPrey() == enemy)
                .append(" sleep=").append(d.dragonSleeping()).append(" block=").append(h.getLevel().getBlockState(h.absolutePos(new BlockPos(24, 3, 28))))
                .append(" grief=").append(h.getLevel().getGameRules().get(GameRules.MOB_GRIEFING)); });
        h.runAtTickTime(200, () -> {
            System.out.println("Body pursuit furthestZ=" + (furthestZ[0] - h.absolutePos(BlockPos.ZERO).getZ()) + trace);
            // Subsequent prey-gripping legitimately clears the combat target; require it during crossing.
            h.assertTrue(!d.isNoAi() && passedWhilePursuing[0], "Real AI passes the soft overhang while pursuing its actual target" + trace);
            h.assertTrue(h.getLevel().getBlockState(h.absolutePos(new BlockPos(24, 3, 28))).isAir(), "The actual body obstacle was removed");
            h.assertTrue(h.getLevel().getBlockState(h.absolutePos(new BlockPos(24, 5, 28))).is(Blocks.BEDROCK)
                    && h.getLevel().getBlockState(h.absolutePos(new BlockPos(24, 1, 28))).is(Blocks.STONE), "Protected roof and floor remain intact");
            h.succeed();
        });
    }
}
