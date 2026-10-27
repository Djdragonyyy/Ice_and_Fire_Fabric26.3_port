package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class DragonTemptGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setPos(h.absoluteVec(new Vec3(20.5, 2, 24.5)));
        d.setOnGround(true); d.setTame(true, false); d.setHunger(100);
        require(h.getLevel().addFreshEntity(d), "Registered dragon"); return d;
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100)
    public void originalStewTagWorksInBothHandsButRespectsCommandsAndDefence(GameTestHelper h) {
        var d = dragon(h); var p = h.makeMockServerPlayerInLevel();
        p.setPos(d.position().add(6, 0, 0)); d.setOwner(p);
        var goal = new DragonTemptGoal(d);
        require(new ItemStack(IafContent.FIRE_STEW).is(DragonTemptGoal.FOOD), "Original tag resolves in native item registry");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        require(!goal.canUse(), "Ordinary hand food does not replace the original fire-stew lure");
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(IafContent.FIRE_STEW));
        require(goal.canUse(), "Stew in offhand attracts the dragon");
        p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.FIRE_STEW));
        require(goal.canUse(), "Stew in main hand also works");
        d.setCommand(1); require(!goal.canUse(), "Sitting order wins");
        d.setCommand(2); require(!goal.canUse(), "Escort order cannot be redirected to a lure");
        d.setCommand(0); d.setDragonSleeping(true); require(!goal.canUse(), "Sleeping dragon stays asleep");
        d.setDragonSleeping(false); d.setNoAi(true); require(!goal.canUse(), "NoAI is respected"); d.setNoAi(false);
        d.setAutonomousFlight(true, true); require(!goal.canUse(), "Lure does not replace flight"); d.setAutonomousFlight(false, false);
        var enemy = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        enemy.setPos(d.position().add(4, 0, 0)); d.setTarget(enemy);
        require(!goal.canUse(), "Defensive target wins"); d.setTarget(null);
        d.seekingFood = true; require(!goal.canUse(), "Already reserved dropped food wins"); d.seekingFood = false;
        d.setInLove(p); require(!goal.canUse(), "Courtship wins"); d.resetLove();
        for (int i = 0; i < 50; i++) d.growOneDay();
        require(p.startRiding(d), "Owner mounts actual dragon");
        require(!goal.canUse(), "Pilot cannot also lure the mounted dragon"); p.stopRiding();
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 100, maxTicks = 180)
    public void actualGoalSelectorApproachesStewThenStopsWhenItemIsRemoved(GameTestHelper h) {
        for (int x = 12; x < 38; x++) for (int z = 16; z < 33; z++)
            h.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.STONE);
        var d = dragon(h); var p = h.makeMockServerPlayerInLevel();
        p.setPos(d.position().add(8, 0, 0)); d.setOwner(p);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.FIRE_STEW, 2));
        double original = d.distanceToSqr(p);
        h.runAtTickTime(100, () -> {
            require(d.isTempted() && d.distanceToSqr(p) < original - 16, "Native selector actually walks towards the lure");
            require(d.hunger() == 100 && !d.isInLove() && p.getMainHandItem().getCount() == 2, "Holding stew consumes nothing and does not breed/feed");
            p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        });
        h.runAtTickTime(110, () -> {
            require(!d.isTempted(), "Native selector stops temptation after hand item disappears");
            h.succeed();
        });
    }
}
