package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

public class DragonInteractionGameTests {
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper helper, Player owner) {
        var dragon = IafContent.FIRE_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        require(dragon != null, "Dragon creation");
        if (owner != null) { dragon.setTame(true, false); dragon.setOwner(owner); }
        dragon.setPos(helper.absoluteVec(new Vec3(1, 2, 1)));
        return dragon;
    }

    @GameTest
    public void meatRestoresHungerAndHealthWithoutGrowing(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner);
        dragon.setHunger(10);
        dragon.setHealth(10);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, 3));
        require(DragonFeeding.foodPoints(owner.getMainHandItem()) == 30, "Raw beef uses original nutrition times ten");
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(dragon.hunger() == 40 && dragon.getHealth() == 13, "Meat restores hunger and nutrition points of health");
        require(owner.getMainHandItem().getCount() == 2 && dragon.ageDays() == 0, "One meat consumed, no growth");
        for (var food : new net.minecraft.world.item.Item[]{Items.APPLE, Items.COD}) {
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(food));
            dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
            require(owner.getMainHandItem().getCount() == 1 && dragon.hunger() == 40, "Fire dragons reject non-meat and fish");
        }
        dragon.setHunger(100);
        dragon.setHealth(dragon.getMaxHealth());
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_BEEF, 2));
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(owner.getMainHandItem().getCount() == 2, "Full healthy dragon must not waste meat");
        dragon.setHealth(1);
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(dragon.getHealth() == 9 && dragon.hunger() == 100 && owner.getMainHandItem().getCount() == 1, "A full but injured dragon can still eat");
        var other = helper.makeMockPlayer(GameType.SURVIVAL);
        other.setUUID(java.util.UUID.randomUUID());
        other.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        dragon.mobInteract(other, InteractionHand.MAIN_HAND);
        require(other.getMainHandItem().getCount() == 1 && dragon.getHealth() == 9, "Foreign player cannot feed an owned dragon");
        helper.succeed();
    }

    @GameTest
    public void creativeMealTamesAndCreativeModeKeepsItems(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, null);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.CREATIVE_DRAGON_MEAL, 2));
        dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        require(dragon.isTame() && dragon.isOwnedBy(player), "Creative meal tames the dragon like the original");
        require(player.getMainHandItem().getCount() == 1 && dragon.ageDays() == 0, "Taming meal consumed without aging");
        var creative = helper.makeMockPlayer(GameType.CREATIVE);
        // MockPlayer reports its mode but does not apply the server's ability update.
        creative.getAbilities().instabuild = true;
        creative.setUUID(java.util.UUID.randomUUID());
        creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.CREATIVE_DRAGON_MEAL));
        dragon.mobInteract(creative, InteractionHand.MAIN_HAND);
        require(dragon.isOwnedBy(creative) && creative.getMainHandItem().getCount() == 1, "Original creative meal can rebind ownership and is not consumed in creative");
        creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        dragon.setHunger(0);
        dragon.mobInteract(creative, InteractionHand.MAIN_HAND);
        require(dragon.hunger() == 30 && creative.getMainHandItem().getCount() == 1, "Creative feeding preserves food");
        helper.succeed();
    }

    @GameTest
    public void sicklyMealFreezesGrowthAndPersists(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner);
        dragon.growOneDay();
        dragon.setHealth(1);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.SICKLY_DRAGON_MEAL, 2));
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(dragon.agingDisabled() && dragon.getHealth() == dragon.getMaxHealth(), "Sickly meal freezes growth and fully heals");
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(owner.getMainHandItem().getCount() == 1, "A second sickly meal is not consumed");
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_MEAL));
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        dragon.growOneDay();
        dragon.tick();
        require(dragon.ageDays() == 1, "Ordinary meals and natural ticking cannot undo the growth lock");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        dragon.saveWithoutId(output);
        var restored = dragon(helper, null);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        restored.growOneDay();
        require(restored.agingDisabled() && restored.ageDays() == 1, "Growth lock survives reload");
        helper.succeed();
    }

    @GameTest
    public void staffCyclesCommandsAndSavesHomeAtZeroCoordinates(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        for (int expected : new int[]{1, 2, 0, 1}) {
            dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
            require(dragon.command() == expected && dragon.isOrderedToSit() == (expected == 1), "Staff command cycle and sitting flag");
        }
        dragon.setPos(0, 3, 0);
        owner.setShiftKeyDown(true);
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(dragon.hasDragonHome() && dragon.hasHome() && dragon.getHomePosition().equals(new BlockPos(0, 3, 0)), "Home can include zero coordinates");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        dragon.saveWithoutId(output);
        var restored = dragon(helper, null);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        require(restored.command() == 1 && restored.isOrderedToSit() && restored.hasHome(), "Command and home survive reload");
        require(restored.getHomePosition().equals(new BlockPos(0, 3, 0)) && restored.getHomeRadius() == 40, "Original home position and default radius");
        require(restored.isWithinHome(new BlockPos(39, 3, 0)) && !restored.isWithinHome(new BlockPos(40, 3, 0)), "Wandering must respect the actual home boundary");
        require(!restored.isWithinHome(Vec3.atCenterOf(new BlockPos(40, 3, 0))), "Vector home checks must use the same radius");
        output.putString("HomeDimension", "minecraft:the_nether");
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        require(restored.hasDragonHome() && !restored.hasHome(), "Home in another dimension must not constrain local navigation");
        require(restored.isWithinHome(new BlockPos(1000, 3, 0)), "Foreign-dimension home must not impose a local boundary");
        dragon.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(!dragon.hasDragonHome() && !dragon.hasHome() && owner.getMainHandItem().getCount() == 1, "Shift-staff removes home without consuming staff");
        var other = helper.makeMockPlayer(GameType.SURVIVAL);
        other.setUUID(java.util.UUID.randomUUID());
        other.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        dragon.mobInteract(other, InteractionHand.MAIN_HAND);
        require(dragon.command() == 1 && !dragon.hasDragonHome(), "Foreign player cannot issue commands");
        helper.succeed();
    }

    @GameTest(maxTicks = 120)
    public void escortActuallyWalksAndStopsWhenOrderedToSit(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var dragon = dragon(helper, owner);
        for (int x = 0; x <= 9; x++) for (int z = 0; z <= 4; z++) {
            var floor = helper.absolutePos(new BlockPos(x, 1, z));
            helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        }
        dragon.setPos(helper.absoluteVec(new Vec3(1.5, 2, 2.5)));
        dragon.setOnGround(true);
        owner.setPos(dragon.position().add(6, 0, 0));
        helper.getLevel().addFreshEntity(dragon);
        dragon.setCommand(2);
        var goal = new DragonEscortGoal(dragon);
        require(goal.canUse(), "Escort may run on the ground");
        goal.start();
        goal.tick();
        require(dragon.getNavigation().getPath() != null, "Escort must obtain a real ground path");
        double before = dragon.distanceTo(owner);
        helper.runAfterDelay(60, () -> {
            require(dragon.distanceTo(owner) < before - 0.5, "Escort must actually move closer to the owner");
            dragon.setCommand(1);
            require(!goal.canUse() && dragon.getNavigation().isDone(), "Sit interrupts escort and navigation");
            var sitting = dragon.position();
            helper.runAfterDelay(20, () -> {
                require(dragon.position().subtract(sitting).horizontalDistance() < 0.5, "Seated dragon must not resume random wandering");
                helper.succeed();
            });
        });
    }

    @GameTest
    public void syncedCommandDrivesPoseWithoutServerOnlySitFlag(GameTestHelper helper) {
        var server = dragon(helper, null);
        var observer = dragon(helper, null);
        server.setCommand(1);
        observer.getEntityData().assignValues(server.getEntityData().getNonDefaultValues());
        require(observer.command() == 1 && !observer.isOrderedToSit(), "Entity data sync must not rely on vanilla's unsynchronized sit flag");
        observer.tick();
        require(observer.sitProgress > 0, "The observer must animate the synchronized sit command");
        helper.succeed();
    }
}
