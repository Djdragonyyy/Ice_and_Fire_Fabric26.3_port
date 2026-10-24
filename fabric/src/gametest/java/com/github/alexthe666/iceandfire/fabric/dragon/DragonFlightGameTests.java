package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonFlightGameTests {
    @GameTest(structure = "iceandfire:flight_arena", skyAccess = true, padding = 12)
    public void escortTeleportsAt150ToSafeGroundAndHonorsCommands(GameTestHelper helper) {
        floor(helper, 10);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(helper.absoluteVec(new Vec3(24.5, 2, 24.5)));
        owner.setOnGround(true);
        var dragon = dragon(helper, owner, 0);
        dragon.setCommand(2);
        dragon.setPos(owner.position().add(-149, 0, 0));
        require(!DragonEscortTeleport.tryTeleport(dragon), "No teleport before 150 blocks");
        dragon.setPos(owner.position().add(-150, 0, 0));
        require(DragonEscortTeleport.tryTeleport(dragon), "Teleport starts at 150 blocks even for a ground-only baby");
        require(dragon.distanceTo(owner) < 12 && !dragon.flying() && dragon.onGround(), "Teleport finds nearby supported ground");
        require(helper.getLevel().noCollision(dragon) && dragon.getDeltaMovement().equals(Vec3.ZERO), "Arrival is collision-free and clears momentum");
        dragon.setPos(owner.position().add(-151, 0, 0));
        dragon.setCommand(1);
        require(!DragonEscortTeleport.tryTeleport(dragon), "Sit cannot teleport");
        dragon.setCommand(0);
        require(!DragonEscortTeleport.tryTeleport(dragon), "Wandering cannot teleport");
        dragon.setCommand(2);
        dragon.setNoAi(true);
        require(!DragonEscortTeleport.tryTeleport(dragon), "NoAI cannot teleport");
        helper.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", skyAccess = true, padding = 12)
    public void escortRefusesBlockedDestinationsAndProtectsPilot(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(helper.absoluteVec(new Vec3(24.5, 30, 24.5)));
        owner.setOnGround(true);
        var dragon = dragon(helper, owner, 50);
        dragon.setCommand(2);
        dragon.setPos(owner.position().add(-151, 0, 0));
        var start = dragon.position();
        require(!DragonEscortTeleport.tryTeleport(dragon) && start.equals(dragon.position()), "No unsupported teleport into empty air");
        require(owner.startRiding(dragon), "Pilot mounts");
        require(!DragonEscortTeleport.tryTeleport(dragon), "Mounted dragons cannot teleport");
        owner.stopRiding();
        helper.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 340, skyAccess = true, padding = 12)
    public void aerialEscortActuallyCatchesGroundOwnerAndLands(GameTestHelper helper) {
        floor(helper, 10);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        dragon.setPos(helper.absoluteVec(new Vec3(4.5, 2, 24.5)));
        owner.setPos(helper.absoluteVec(new Vec3(42.5, 2, 24.5)));
        owner.setOnGround(true);
        dragon.setCommand(2);
        helper.getLevel().addFreshEntity(dragon);
        helper.runAfterDelay(5, () -> require(dragon.hovering(), "Distant owner triggers takeoff"));
        helper.runAfterDelay(180, () -> require(dragon.distanceTo(owner) < 18, "Stable air route must actually catch up"));
        helper.runAfterDelay(300, () -> {
            require(dragon.distanceTo(owner) < 6 && dragon.onGround() && !dragon.flying(), "Escort returns to ground near its owner");
            helper.succeed();
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 110, skyAccess = true, padding = 12)
    public void wanderingTakesOffAndMovesWithoutAnOwner(GameTestHelper helper) {
        floor(helper, 10);
        var dragon = dragon(helper, null, 25);
        long seed = 0;
        do { dragon.getRandom().setSeed(seed++); } while (dragon.getRandom().nextInt(1500) != 0);
        dragon.getRandom().setSeed(seed - 1);
        dragon.flightManager.tick();
        require(dragon.flying() && dragon.hovering(), "Original random wandering trigger can start flight");
        var start = dragon.position();
        double[] maximumHorizontalExcursion = {0};
        boolean[] finished = {false};
        helper.getLevel().addFreshEntity(dragon);
        helper.onEachTick(() -> {
            if (!finished[0]) maximumHorizontalExcursion[0] = Math.max(maximumHorizontalExcursion[0],
                    dragon.position().subtract(start).horizontalDistance());
        });
        helper.runAfterDelay(85, () -> {
            // Wandering may circle back: measure actual excursion, not only the final displacement.
            require(dragon.flying() && dragon.getY() > start.y + 8
                    && maximumHorizontalExcursion[0] > 1, "Wandering must climb and fly horizontally: start=" + start + " actual=" + dragon.position() + " maximumExcursion=" + maximumHorizontalExcursion[0] + " flying=" + dragon.flying() + " target=" + dragon.flightManager.target() + " ticks=" + dragon.tickCount);
            finished[0] = true;
            helper.succeed();
        });
    }
    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 560, skyAccess = true, padding = 12)
    public void distantRoostTriggersTakeoffAndCompletesReturn(GameTestHelper helper) {
        floor(helper, 10);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        dragon.setPos(helper.absoluteVec(new Vec3(2.5, 2, 24.5)));
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        owner.setShiftKeyDown(true);
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        var home = dragon.position();
        dragon.setPos(home.add(42, 0, 0));
        helper.getLevel().addFreshEntity(dragon);
        helper.runAfterDelay(5, () -> require(dragon.flying() && dragon.hovering(), "A roost over forty blocks away triggers takeoff"));
        helper.runAfterDelay(500, () -> {
            require(dragon.position().distanceTo(home) < 4 && dragon.onGround() && !dragon.flying(),
                    "Distant roost must complete takeoff, flight and landing: home=" + home + " actual=" + dragon.position()
                            + " flying=" + dragon.flying() + " target=" + dragon.flightManager.target());
            helper.succeed();
        });
    }
    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 260, skyAccess = true, padding = 12)
    public void airborneRoostReturnsAndLandsNearHome(GameTestHelper helper) {
        floor(helper, 10);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        owner.setShiftKeyDown(true);
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        var home = dragon.position();
        dragon.setPos(home.add(8, 18, 0));
        dragon.setOnGround(false);
        dragon.setAutonomousFlight(true, false);
        helper.getLevel().addFreshEntity(dragon);
        helper.runAfterDelay(220, () -> {
            require(dragon.position().distanceTo(home) < 4 && dragon.onGround() && !dragon.flying(),
                    "Roost air path must return and land: home=" + home + " actual=" + dragon.position()
                            + " flying=" + dragon.flying() + " ticks=" + dragon.tickCount + " target=" + dragon.flightManager.target());
            helper.succeed();
        });
    }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static FireDragon dragon(GameTestHelper helper, Player owner, int days) {
        var dragon = IafContent.FIRE_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        require(dragon != null, "Dragon creation");
        if (owner != null) { dragon.setTame(true, false); dragon.setOwner(owner); }
        for (int i = 0; i < days; i++) dragon.growOneDay();
        double center = helper.getRelativeBounds().getXsize() > 8 ? 24.5 : 2.5;
        dragon.setPos(helper.absoluteVec(new Vec3(center, 2, center)));
        dragon.setOnGround(true);
        return dragon;
    }
    private static void floor(GameTestHelper helper, int size) {
        int min = helper.getRelativeBounds().getXsize() > 8 ? 0 : -size;
        int max = helper.getRelativeBounds().getXsize() > 8 ? 47 : size;
        for (int x = min; x <= max; x++) for (int z = min; z <= max; z++)
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.STONE.defaultBlockState());
    }
    @GameTest(padding = 12, skyAccess = true)
    public void takeoffHonorsAgeCommandsClearanceAndPassengers(GameTestHelper helper) {
        floor(helper, 5);
        var baby = dragon(helper, null, 24);
        require(!baby.flightManager.takeOff(), "Stage one cannot fly autonomously");
        var dragon = dragon(helper, null, 25);
        dragon.setCommand(1);
        require(!dragon.flightManager.takeOff(), "Sitting blocks takeoff");
        dragon.setCommand(0);
        dragon.setNoAi(true);
        require(!dragon.flightManager.takeOff(), "NoAI blocks takeoff");
        dragon.setNoAi(false);
        var ceiling = BlockPos.containing(dragon.getX(), dragon.getBoundingBox().maxY + 1, dragon.getZ());
        helper.getLevel().setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
        require(!dragon.flightManager.takeOff(), "A low ceiling blocks takeoff");
        helper.getLevel().setBlockAndUpdate(ceiling, Blocks.AIR.defaultBlockState());
        require(dragon.flightManager.takeOff() && dragon.flying() && dragon.hovering(), "Stage two starts in hover");
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var adult = dragon(helper, owner, 50);
        require(owner.startRiding(adult), "Owner can pilot adult");
        require(!adult.flightManager.takeOff(), "Autonomous takeoff must never replace pilot controls");
        owner.stopRiding();
        helper.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 220, skyAccess = true, padding = 12)
    public void escortTakesOffMovesInAirAndLandsWhenOrdered(GameTestHelper helper) {
        floor(helper, 10);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        var start = dragon.position();
        owner.setPos(start.add(0, 40, 0));
        dragon.setCommand(2);
        helper.getLevel().addFreshEntity(dragon);
        helper.runAfterDelay(5, () -> require(dragon.flying() && dragon.hovering(), "Elevated owner triggers autonomous hover"));
        helper.runAfterDelay(65, () -> {
            require(dragon.flying() && !dragon.hovering(), "Hover transitions to flight after forty ticks");
            require(dragon.getY() > start.y + 8 && dragon.distanceTo(owner) < 32, "Escort must actually climb closer to the owner");
            require(dragon.flightManager.target() != null, "Flight manager selects an air target");
            dragon.setCommand(1);
            helper.runAfterDelay(100, () -> {
                require(!dragon.flying() && !dragon.hovering() && dragon.onGround(), "Sit command brings the dragon down to solid ground");
                require(!dragon.isNoGravity() && dragon.getNavigation().isDone(), "Landing restores gravity and stops navigation");
                helper.succeed();
            });
        });
    }
    @GameTest(padding = 12, skyAccess = true)
    public void stageTwoAirStatePersistsAndSynchronizes(GameTestHelper helper) {
        floor(helper, 5);
        var dragon = dragon(helper, null, 25);
        require(dragon.flightManager.takeOff(), "Stage two takeoff");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        dragon.saveWithoutId(output);
        var restored = dragon(helper, null, 0);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        require(restored.stage() == 2 && restored.flying() && restored.hovering(), "Autonomous stage-two flight survives reload without enabling rider mounting");
        var observer = dragon(helper, null, 25);
        observer.getEntityData().assignValues(dragon.getEntityData().getNonDefaultValues());
        require(observer.flying() && observer.hovering(), "Airborne and hover state are synchronized to observers");
        require(!DragonGrowth.canCarryRider(restored.ageDays()), "Stage two still has no rider seat");
        helper.succeed();
    }
    @GameTest(padding = 12, skyAccess = true)
    public void outOfRangeOwnerCannotTriggerFlightOrTeleport(GameTestHelper helper) {
        floor(helper, 5);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        dragon.setCommand(2);
        var before = dragon.position();
        owner.setPos(before.add(2001, 30, 0));
        dragon.flightManager.tick();
        require(!dragon.flying() && dragon.position().equals(before), "No teleport to a distant owner without a safe loaded destination");
        helper.succeed();
    }
    @GameTest(environment = "iceandfire:night", structure = "iceandfire:flight_arena", maxTicks = 220, padding = 12, skyAccess = true)
    public void roostActuallyWalksHomeAndStopsForSit(GameTestHelper helper) {
        floor(helper, 10);
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        owner.setShiftKeyDown(true);
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        var home = dragon.position();
        dragon.setPos(home.add(7, 0, 0));
        helper.getLevel().addFreshEntity(dragon);
        require(dragon.flightManager.wantsRoost(), "Night activates home return");
        helper.runAfterDelay(160, () -> {
            require(dragon.position().distanceTo(home) < 3, "Roost must walk home: start=" + home + " current=" + dragon.position() + " wants=" + dragon.flightManager.wantsRoost() + " move=" + dragon.canMoveOnGround() + " path=" + dragon.getNavigation().getPath() + " home=" + dragon.getHomePosition());
            dragon.setCommand(1);
            require(!dragon.flightManager.wantsRoost(), "Sit overrides roost");
            helper.succeed();
        });
    }
    @GameTest(environment = "iceandfire:night", padding = 12, skyAccess = true)
    public void roostChecksHomeDimensionAndEscortCommand(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner, 25);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        owner.setShiftKeyDown(true);
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        dragon.setPos(dragon.position().add(50, 0, 0));
        require(dragon.flightManager.wantsRoost(), "Distant home is selected at night");
        dragon.setCommand(2);
        require(!dragon.flightManager.wantsRoost(), "Escort has priority over roost");
        dragon.setCommand(0);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        dragon.saveWithoutId(output);
        output.putString("HomeDimension", "minecraft:the_nether");
        dragon.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        require(!dragon.flightManager.wantsRoost(), "A home in another dimension must not trigger local return");
        helper.succeed();
    }
}
