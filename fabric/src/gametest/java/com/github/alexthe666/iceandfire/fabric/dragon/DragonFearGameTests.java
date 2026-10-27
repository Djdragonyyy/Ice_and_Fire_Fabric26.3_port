package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonFearGameTests {
    private static long goals(PathfinderMob mob) {
        return mob.getGoalSelector().getAvailableGoals().stream().filter(g -> g.getGoal() instanceof DragonFear.FearGoal).count();
    }
    private static <T extends PathfinderMob> T spawn(GameTestHelper h, EntityType<T> type, double x, double z) {
        var mob = type.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        mob.setPos(h.absoluteVec(new Vec3(x, 2, z))); h.getLevel().addFreshEntity(mob); return mob;
    }
    private static void flee(GameTestHelper h, EntityType<? extends PathfinderMob> type) {
        for (int x = 2; x < 46; x++) for (int z = 2; z < 46; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setPos(h.absoluteVec(new Vec3(20.5, 2, 24.5))); d.setNoAi(true); d.setSilent(true);
        h.getLevel().addFreshEntity(d);
        var mob = spawn(h, type, 24.5, 24.5); double start = mob.distanceToSqr(d);
        h.assertTrue(goals(mob) == 1, "Native load event installed one original fear goal");
        boolean[] fearRan = {false};
        double[] furthestWhileFleeing = {start};
        for (int tick = 5; tick < 300; tick += 5) h.runAtTickTime(tick, () -> {
            boolean running = mob.getGoalSelector().getAvailableGoals().stream()
                    .anyMatch(g -> g.getGoal() instanceof DragonFear.FearGoal && g.isRunning());
            fearRan[0] |= running;
            if (running) furthestWhileFleeing[0] = Math.max(furthestWhileFleeing[0], mob.distanceToSqr(d));
        });
        h.runAtTickTime(300, () -> {
            h.assertTrue(fearRan[0] && furthestWhileFleeing[0] > start + 16,
                    "Actual fear selector must move away: " + mob.getClass().getSimpleName() + " running=" + fearRan[0]
                            + " start=" + start + " maxWhileRunning=" + furthestWhileFleeing[0]
                            + " end=" + mob.distanceToSqr(d) + " mob=" + mob.position() + " dragon=" + d.position()
                            + " pathTarget=" + mob.getNavigation().getTargetPos() + " pathDone=" + mob.getNavigation().isDone());
            h.assertTrue(mob.getHealth() == mob.getMaxHealth(), "Fleeing was not caused by damage or combat"); h.succeed();
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100, maxTicks = 400)
    public void actualCowAiFleesDragon(GameTestHelper h) { flee(h, EntityTypes.COW); }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100, maxTicks = 400)
    public void actualVillagerBrainAndGoalSelectorFleeDragon(GameTestHelper h) { flee(h, EntityTypes.VILLAGER); }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100, maxTicks = 400)
    public void actualTraderAiFleesDragon(GameTestHelper h) { flee(h, EntityTypes.WANDERING_TRADER); }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void originalTagsConfigurationAndSavedMobReloadHaveNoDuplicateGoals(GameTestHelper h) {
        var cow = spawn(h, EntityTypes.COW, 20, 20); var villager = spawn(h, EntityTypes.VILLAGER, 30, 30);
        h.assertTrue(goals(cow) == 1 && goals(villager) == 1, "Both original options default on");
        var config = DragonCombatConfig.current;
        boolean animals = config.animalsFearDragons, villagers = config.villagersFearDragons;
        try {
            config.animalsFearDragons = false; config.villagersFearDragons = false;
            h.assertTrue(goals(spawn(h, EntityTypes.COW, 18, 18)) == 0 && goals(spawn(h, EntityTypes.VILLAGER, 32, 32)) == 0,
                    "Disabled options prevent fear installation for newly loaded entities");
        } finally { config.animalsFearDragons = animals; config.villagersFearDragons = villagers; }
        DragonFear.install(cow); DragonFear.install(villager);
        h.assertTrue(goals(cow) == 1 && goals(villager) == 1, "Repeated callback cannot duplicate transient goals");
        h.assertTrue(goals(spawn(h, EntityTypes.ZOMBIE, 8, 8)) == 0, "Untagged mobs retain their own vanilla goals");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        cow.saveWithoutId(output); cow.discard();
        var restored = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        h.assertTrue(h.getLevel().addFreshEntity(restored) && goals(restored) == 1, "Actual saved cow reload restores exactly one goal");
        h.assertTrue(EntityTypes.CAT.builtInRegistryHolder().is(DragonFear.LIVESTOCK) && EntityTypes.HOGLIN.builtInRegistryHolder().is(DragonFear.LIVESTOCK)
                && EntityTypes.WANDERING_TRADER.builtInRegistryHolder().is(DragonFear.VILLAGERS), "Original entity tags resolve natively");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void tamePetsAndDistantOrDeadDragonsCannotStartFear(GameTestHelper h) {
        for (int x = 2; x < 46; x++) for (int z = 2; z < 46; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setPos(h.absoluteVec(new Vec3(8.5, 2, 24.5))); d.setNoAi(true); h.getLevel().addFreshEntity(d);
        var cat = spawn(h, EntityTypes.CAT, 12.5, 24.5); cat.setTame(true, false);
        h.assertTrue(!new DragonFear.FearGoal(cat, false).canUse(), "Original animal predicate excludes tamed pets");
        var cow = spawn(h, EntityTypes.COW, 42.5, 24.5);
        h.assertTrue(!new DragonFear.FearGoal(cow, false).canUse(), "Native search respects original thirty-block radius");
        var villager = spawn(h, EntityTypes.VILLAGER, 18.5, 24.5);
        h.assertTrue(!new DragonFear.FearGoal(villager, true).canUse(), "Villager native search uses eight blocks, not livestock thirty");
        cow.setPos(d.position().add(4, 0, 0)); d.setHealth(0);
        h.assertTrue(!new DragonFear.FearGoal(cow, false).canUse(), "Dead dragons do not trigger native avoidance targeting");
        h.succeed();
    }

}
