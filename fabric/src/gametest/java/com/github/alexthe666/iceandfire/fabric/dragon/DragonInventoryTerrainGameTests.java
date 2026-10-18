package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.IafTerrain;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Runs inside Fabric, including registry remapping and real entity serialization. */
public class DragonInventoryTerrainGameTests {
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    @GameTest
    public void threeOwnedBabiesCanBeCarriedAndReleased(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1)));
        FireDragon[] babies = new FireDragon[4];
        for (int i = 0; i < babies.length; i++) {
            babies[i] = IafContent.FIRE_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
            require(babies[i] != null, "Baby creation");
            babies[i].setOwner(player);
            babies[i].setPos(player.position());
            helper.getLevel().addFreshEntity(babies[i]);
            babies[i].mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
            require((babies[i].shoulderCarrier() == player) == (i < 3), "Exactly three carried babies, no fourth mount");
        }
        player.setPos(player.position().add(2, 0, 1));
        player.yBodyRot = 90;
        for (int i = 0; i < 3; i++) {
            babies[i].tick();
            var expected = player.position().add(DragonShoulderLayout.offset(i, 90, false, player.getScale()));
            require(babies[i].position().distanceTo(expected) < 0.00001, "Carried position must follow the walking player");
        }
        player.setShiftKeyDown(true);
        for (int i = 0; i < 3; i++) {
            babies[i].tick();
            require(babies[i].shoulderCarrier() == null && !babies[i].noPhysics, "Sneaking releases all babies");
        }
        helper.succeed();
    }

    @GameTest
    public void shoulderOwnershipAndGrowthDoNotChangeAdultPilotSeat(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var other = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        other.setUUID(java.util.UUID.randomUUID());
        var dragon = IafContent.FIRE_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        require(dragon != null, "Dragon creation");
        dragon.setOwner(player);
        dragon.mobInteract(other, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(dragon.shoulderCarrier() == null, "Only the owner can pick up a baby");
        dragon.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(dragon.shoulderCarrier() == player, "Owner picks up baby");
        for (int i = 0; i < 25; i++) dragon.growOneDay();
        dragon.tick();
        require(dragon.shoulderCarrier() == null, "A stage two dragon leaves the shoulders");
        dragon.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(!dragon.isPassenger() && !player.isPassenger(), "Stage two cannot be carried or piloted");
        for (int i = 0; i < 25; i++) dragon.growOneDay();
        dragon.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(player.getVehicle() == dragon && dragon.getControllingPassenger() == player, "Stage three retains its original pilot mount");
        require(!other.startRiding(dragon, true, true), "Even a forced second seat stays forbidden");
        player.stopRiding();
        helper.succeed();
    }

    @GameTest
    public void carriedBabyIsIndependentlySavedAndReleasedOnReload(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var dragon = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        var restored = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        require(dragon != null && restored != null, "Dragon creation");
        dragon.setOwner(player);
        dragon.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        dragon.tick();
        require(dragon.shoulderCarrier() == player && !dragon.isPassenger(), "Baby must stay independently saved in its world chunk");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        require(dragon.save(output), "A carried baby must be serializable independently of the player");
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), output.buildResult()));
        require(restored.shoulderCarrier() == null && !restored.noPhysics, "Reload releases the baby");
        require(restored.getOwnerReference() != null && restored.getOwnerReference().getUUID().equals(player.getUUID()), "Reload preserves the owner's UUID even while that player is offline");
        dragon.releaseShoulder();
        helper.succeed();
    }

    @GameTest
    public void carriedBabyReleasesWhenOwnerLeaves(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var dragon = IafContent.FIRE_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        require(dragon != null, "Dragon creation");
        dragon.setOwner(player);
        dragon.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(dragon.shoulderCarrier() == player, "Owner picks up baby");
        player.remove(net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        dragon.tick();
        require(dragon.shoulderCarrier() == null && !dragon.noPhysics && !dragon.isNoGravity(), "Logout restores normal physics without removing baby");
        require(!dragon.isRemoved(), "Baby remains in the world after logout");
        helper.succeed();
    }

    @GameTest
    public void armorSlotsAndOriginalMaterials(GameTestHelper helper) {
        require(DragonArmorItem.ITEMS.size() == 32, "All 32 original armor items must be registered");
        for (var item : DragonArmorItem.ITEMS) {
            DragonArmorItem armor = (DragonArmorItem) item;
            var stack = new ItemStack(item);
            for (int slot = 0; slot < 5; slot++) require((slot == armor.slot + 1) == DragonArmorItem.fits(stack, slot), "Armor in wrong slot");
        }
        var banner = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", "red_banner")));
        require(DragonArmorItem.fits(banner, 0), "Banner slot must accept banners");
        require(!DragonArmorItem.fits(new ItemStack(Items.DIAMOND_CHESTPLATE), 3), "Player armor must not enter dragon slots");
        require(!DragonArmorItem.fits(new ItemStack(Items.DIRT), 0), "Banner slot must reject ordinary items");
        require(DragonArmorItem.bonus(7) == 1.5 && DragonArmorItem.bonus(5) == 10, "Original copper/dragonsteel bonuses");
        helper.succeed();
    }

    @GameTest
    public void dragonInventoryPersistsWithComponents(GameTestHelper helper) {
        var level = helper.getLevel();
        var dragon = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        var restored = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        require(dragon != null && restored != null, "Dragon creation");
        var banner = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", "red_banner")));
        banner.set(DataComponents.CUSTOM_NAME, Component.literal("Bandiera del drago"));
        dragon.dragonInventory.setItem(0, banner);
        dragon.dragonInventory.setItem(1, new ItemStack(DragonArmorItem.ITEMS.get(0)));
        dragon.dragonInventory.setItem(4, new ItemStack(DragonArmorItem.ITEMS.get(31)));
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dragon.saveWithoutId(output);
        output.putInt("AgeTicks", 3000000);
        var input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), output.buildResult());
        restored.load(input);
        for (int i = 0; i < 5; i++) require(ItemStack.matches(dragon.dragonInventory.getItem(i), restored.dragonInventory.getItem(i)), "Persisted slot " + i);
        require(restored.ageDays() == 125, "Growth must coexist with inventory persistence");
        require(restored.armorParts() == (1 | (8 << 12)), "Armor must be synchronized after load");
        helper.succeed();
    }

    @GameTest
    public void terrainConversionsAndRevertState(GameTestHelper helper) {
        require(DragonFireTerrain.transformed(Blocks.GRASS_BLOCK.defaultBlockState()).is(IafTerrain.GRASS), "Grass conversion");
        require(DragonFireTerrain.transformed(Blocks.DIRT.defaultBlockState()).is(IafTerrain.DIRT), "Dirt conversion");
        require(DragonFireTerrain.transformed(Blocks.DIRT_PATH.defaultBlockState()).is(IafTerrain.PATH), "Path conversion");
        require(DragonFireTerrain.transformed(Blocks.GRAVEL.defaultBlockState()).is(IafTerrain.GRAVEL), "Gravel conversion");
        require(DragonFireTerrain.transformed(Blocks.OAK_LOG.defaultBlockState()).is(IafTerrain.ASH), "Logs become falling ash");
        require(DragonFireTerrain.transformed(Blocks.OAK_LEAVES.defaultBlockState()).isAir(), "Leaves burn away");
        require(DragonFireTerrain.transformed(Blocks.OBSIDIAN.defaultBlockState()).is(Blocks.OBSIDIAN), "Unrelated block unchanged");
        require(DragonFireTerrain.transformed(Blocks.DIRT.defaultBlockState()).getValue(IafTerrain.REVERT), "Breath terrain should revert");
        require(!IafTerrain.DIRT.defaultBlockState().getValue(IafTerrain.REVERT), "Placed blocks must not spontaneously revert");
        require(Blocks.CHEST.defaultBlockState().is(DragonFireTerrain.BLACKLIST), "Chest blacklist loaded");
        var expectedLoot = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath("iceandfire", "blocks/chared_dirt"));
        require(IafTerrain.DIRT.getLootTable().orElseThrow().equals(expectedLoot), "Charred blocks need their own loot table");
        helper.succeed();
    }

    @GameTest
    public void inventoryOwnershipAndShiftTransfer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var other = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        other.setUUID(java.util.UUID.randomUUID());
        var dragon = IafContent.FIRE_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        require(dragon != null, "Dragon creation");
        dragon.setOwner(player);
        dragon.setPos(player.position());
        var menu = new DragonMenu(1, player.getInventory(), dragon.dragonInventory, dragon);
        require(menu.stillValid(player), "Owner may use the inventory");
        require(!menu.stillValid(other), "A second player must not use the owner's menu");
        player.getInventory().setItem(9, new ItemStack(DragonArmorItem.ITEMS.get(0)));
        require(!menu.quickMoveStack(player, 5).isEmpty(), "Shift-click must equip from player inventory");
        require(dragon.dragonInventory.getItem(1).getItem() == DragonArmorItem.ITEMS.get(0), "Head armor goes in head slot");
        require(dragon.armorParts() == 1, "Equipped armor state must update immediately");
        require(!menu.quickMoveStack(player, 1).isEmpty(), "Shift-click must unequip to player inventory");
        require(dragon.dragonInventory.getItem(1).isEmpty() && dragon.armorParts() == 0, "No stale armor after removal");
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (player.getInventory().getItem(i).getItem() == DragonArmorItem.ITEMS.get(0)) count += player.getInventory().getItem(i).getCount();
        require(count == 1, "Shift transfers must not duplicate armor");
        player.setPos(player.position().add(9, 0, 0));
        require(!menu.stillValid(player), "Walking away closes the menu");
        menu.removed(player);
        helper.succeed();
    }

    @GameTest
    public void terrainRespectsGriefingAndProtectedBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1));
        var chest = center.east();
        var bedrock = center.west();
        level.setBlockAndUpdate(center, Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(bedrock, Blocks.BEDROCK.defaultBlockState());
        var dragon = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
        require(dragon != null, "Dragon creation");
        for (int i = 0; i < 50; i++) dragon.growOneDay();
        var config = com.github.alexthe666.iceandfire.fabric.DragonCombatConfig.current;
        int previous = config.dragonGriefing;
        try {
            config.dragonGriefing = 2;
            DragonFireTerrain.breath(level, center, dragon);
            DragonFireTerrain.charge(level, center, dragon);
            require(level.getBlockState(center).is(Blocks.DIRT), "Disabled griefing must leave terrain unchanged");
            config.dragonGriefing = 0;
            dragon.getRandom().setSeed(0);
            for (int i = 0; i < 64; i++) DragonFireTerrain.breath(level, center, dragon);
            require(level.getBlockState(center).is(IafTerrain.DIRT), "Breath chars dirt in the world");
            level.getBlockState(center).randomTick(level, center, net.minecraft.util.RandomSource.create(0));
            require(level.getBlockState(center).is(Blocks.DIRT), "Random tick restores burnt dirt");
            DragonFireTerrain.charge(level, center, dragon);
            require(level.getBlockState(chest).is(Blocks.CHEST), "Charge must respect chest blacklist");
            require(level.getBlockState(bedrock).is(Blocks.BEDROCK), "Charge must never destroy bedrock");
        } finally { config.dragonGriefing = previous; }
        helper.succeed();
    }
}
