package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonCorpseGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int age, boolean male) {
        for (int x = 16; x <= 32; x++) for (int z = 16; z <= 32; z++) h.setBlock(new BlockPos(x, 8, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setMale(male); d.setPos(h.absoluteVec(new Vec3(24.5, 10, 24.5))); d.refreshDimensions(); h.getLevel().addFreshEntity(d); return d;
    }
    private static void kill(GameTestHelper h, FireDragon d) {
        require(d.hurtServer(h.getLevel(), d.damageSources().generic(), 10000), "Lethal damage accepted");
        require(d.corpse() && d.getHealth() == 0 && !d.isRemoved(), "Death retains original corpse");
    }
    private static void harvest(FireDragon d, Player p) {
        require(p.interactOn(d, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction(), "Native player interaction reaches remains");
    }
    private static int drops(GameTestHelper h, FireDragon d, Item item) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class, d.getBoundingBox().inflate(12)).stream()
                .filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void corpseStopsAiGrowthRidingAndStorageAndCanBeAdministrativelyRemoved(GameTestHelper h) {
        var p = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, 100, true);
        d.setTame(true, false); d.setOwner(p); d.setHunger(71); d.dragonInventory.setItem(1, new ItemStack(Items.DIAMOND, 2));
        kill(h, d);
        for (int i = 0; i < 100; i++) { d.tickCount++; d.tick(); }
        require(!d.isRemoved() && d.isNoAi() && !d.flying() && !d.hovering() && d.deathTime == 0 && d.modelDeadProgress == 20, "Persistent stationary death state and complete pose progress");
        require(d.ageDays() == 100 && d.hunger() == 71 && d.getTarget() == null, "Dead dragon does not age, eat, hunt or follow");
        require(d.dragonInventory.isEmpty() && drops(h, d, Items.DIAMOND) == 2 && d.armorParts() == 0, "Inventory released once and armor snapshot cleared");
        require(!p.startRiding(d) && !p.startRiding(d, true, true), "Forced and ordinary riding cannot occupy a corpse");
        var horn = new ItemStack(IafContent.DRAGON_HORN); p.setItemInHand(InteractionHand.MAIN_HAND, horn);
        require(!IafContent.DRAGON_HORN.interactLivingEntity(horn, p, d, InteractionHand.MAIN_HAND).consumesAction() && !DragonHornItem.filled(horn), "Horn rejects remains");
        var crystal = new ItemStack(IafContent.SUMMONING_CRYSTAL); p.setItemInHand(InteractionHand.MAIN_HAND, crystal);
        require(!IafContent.SUMMONING_CRYSTAL.interactLivingEntity(crystal, p, d, InteractionHand.MAIN_HAND).consumesAction(), "Crystal rejects remains");
        require(!new DragonMenu(0, p.getInventory(), d.dragonInventory, d).stillValid(p), "Death invalidates inventory menu");
        require(!d.hurtServer(h.getLevel(), d.damageSources().playerAttack(p), 1), "Ordinary repeated damage cannot award another death");
        d.kill(h.getLevel()); require(d.isRemoved() && drops(h, d, Items.DIAMOND) == 2, "Native kill command cleans remains without a second inventory drop"); h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void mixedHarvestUsesOriginalHeartFemaleEggBonesAndFinalSkull(GameTestHelper h) {
        var p = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, 100, false); kill(h, d);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2)); harvest(d, p); harvest(d, p);
        require(d.deathStage() == 2 && p.getMainHandItem().isEmpty() && p.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) == 2, "Bottles become blood without duplication");
        while (d.deathStage() < 9) harvest(d, p);
        int eggsBefore = java.util.Arrays.stream(IafContent.FIRE_EGGS).mapToInt(item -> drops(h, d, item)).sum();
        harvest(d, p);
        require(d.deathStage() == 10 && d.skeletal() && drops(h, d, IafContent.FIRE_DRAGON_HEART) == 1, "Heart at the exact midpoint before bone collection");
        require(java.util.Arrays.stream(IafContent.FIRE_EGGS).mapToInt(item -> drops(h, d, item)).sum() == eggsBefore + 1, "Adult female heart step adds exactly one original random-color egg");
        while (d.deathStage() < 19) {
            int before = drops(h, d, IafContent.DRAGON_BONE); harvest(d, p); int count = drops(h, d, IafContent.DRAGON_BONE) - before;
            require(count >= 1 && count <= 5, "Original native skeletal loot count (age100)");
        }
        harvest(d, p); require(d.isRemoved() && drops(h, d, IafContent.DRAGON_SKULL_FIRE) == 1, "Final step gives one skull and removes remains");
        var skull = h.getLevel().getEntitiesOfClass(ItemEntity.class, d.getBoundingBox().inflate(12)).stream().filter(e -> e.getItem().is(IafContent.DRAGON_SKULL_FIRE)).findFirst().orElseThrow();
        require(DragonSkullItem.data(skull.getItem()).equals(new DragonSkullData(5, 100)), "Harvested skull retains exact original stage and age");
        p.interactOn(d, InteractionHand.MAIN_HAND, Vec3.ZERO); require(drops(h, d, IafContent.DRAGON_SKULL_FIRE) == 1, "Removed corpse cannot be harvested twice"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void independentDropConfigurationsAndBloodPriorityAreOriginal(GameTestHelper h) {
        var old = DragonCombatConfig.current; var cfg = new DragonCombatConfig(); DragonCombatConfig.current = cfg;
        try {
            var p = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, 100, false); kill(h, d);
            cfg.dragonDropBlood = false; p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 10)); harvest(d, p);
            require(d.deathStage() == 0 && p.getMainHandItem().getCount() == 10, "Blood switch prevents bottle consumption/progression");
            cfg.dragonDropBlood = true; cfg.dragonDropSkull = false;
            p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); harvest(d, p); require(d.deathStage() == 0, "Original skull switch gates every empty-hand harvest");
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 10)); for (int i = 0; i < 10; i++) harvest(d, p);
            require(d.deathStage() == 10 && d.skeletal() && p.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) == 10, "Blood remains available when skull drops disabled");
            require(drops(h, d, IafContent.FIRE_DRAGON_HEART) == 0, "Taking blood at the heart step intentionally foregoes the heart"); d.discard();
            cfg.dragonDropSkull = true; cfg.dragonDropHeart = false; var second = dragon(h, 100, true); kill(h, second);
            p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); for (int i = 0; i < 10; i++) harvest(second, p);
            require(second.deathStage() == 10 && drops(h, second, IafContent.FIRE_DRAGON_HEART) == 0 && drops(h, second, IafContent.DRAGON_BONE) >= 1, "Disabled heart selects the original next skeletal loot");
        } finally { DragonCombatConfig.current = old; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void partiallyHarvestedCorpseSurvivesNativeSaveAndReloadWithoutRevival(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel(); p.setGameMode(GameType.SURVIVAL); var d = dragon(h, 75, true); d.setTame(true, false); d.setOwner(p); d.setVariant(2); kill(h, d);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 3)); harvest(d, p); harvest(d, p); harvest(d, p);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess()); d.saveWithoutId(output); d.discard();
        var restored = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        h.getLevel().addFreshEntity(restored);
        for (int i = 0; i < 30; i++) { restored.tickCount++; restored.tick(); }
        require(restored.corpse() && restored.getHealth() == 0 && !restored.isRemoved() && restored.deathStage() == 3, "Native save/reload preserves partial collection and dead state");
        require(restored.ageDays() == 75 && restored.variant() == 2 && restored.isOwnedBy(p) && restored.male(), "Age/variant/owner/sex actual=" + restored.ageDays() + "/" + restored.variant() + "/" + restored.isOwnedBy(p) + "/" + restored.male() + " owner=" + (restored.getOwnerReference() == null ? null : restored.getOwnerReference().getUUID()) + " expectedOwner=" + p.getUUID());
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_MEAL)); harvest(restored, p);
        require(restored.ageDays() == 75 && restored.getHealth() == 0 && p.getMainHandItem().getCount() == 1, "Meals cannot revive or grow remains"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void deathDetectedInsideBaseTickCannotAdvanceAgeOrHunger(GameTestHelper h) {
        var d = dragon(h, 25, true); d.setHunger(71);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess()); d.saveWithoutId(output);
        var tag = output.buildResult(); tag.putInt("AgeTicks", 26 * DragonGrowth.TICKS_PER_DAY - 1); tag.putFloat("Health", 0); tag.putBoolean("ModelDead", false);
        d.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), tag));
        d.tickCount = DragonCombatConfig.current.dragonHungerTickRate; d.tick();
        require(d.corpse() && d.ageDays() == 25 && d.hunger() == 71, "Base-tick death must stop every later growth/hunger update in the same tick"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void deathReleasesAirPilotShoulderAndPreyWithoutChangingTheirAiOrFallRules(GameTestHelper h) {
        var p = h.makeMockPlayer(GameType.SURVIVAL); var air = dragon(h, 100, true); air.setTame(true, false); air.setOwner(p);
        air.setPos(air.position().add(0, 24, 0)); air.setOnGround(false); air.setAutonomousFlight(true, false); air.setNoGravity(true); p.setPos(air.position());
        require(p.startRiding(air), "Airborne owner seat fixture"); kill(h, air);
        require(!p.isPassenger() && !air.isVehicle() && !p.isIgnoringFallDamageFromCurrentImpulse() && !air.isNoGravity(), "Death ejects the pilot and restores corpse gravity without airborne immunity"); air.discard();
        var baby = dragon(h, 12, true); baby.setTame(true, false); baby.setOwner(p); p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        baby.mobInteract(p, InteractionHand.MAIN_HAND); require(baby.shoulderCarrier() == p, "Shoulder fixture"); baby.tick(); kill(h, baby);
        require(baby.shoulderCarrier() == null && !baby.noPhysics && !baby.isNoGravity(), "Death releases custom shoulder attachment and collision state"); baby.discard();
        var holder = dragon(h, 50, true); holder.setOnGround(true);
        var prey = net.minecraft.world.entity.EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED); prey.setNoAi(true); prey.setPos(holder.position().add(0.5, 0, 0)); h.getLevel().addFreshEntity(prey);
        require(holder.tryGrabPrey(prey), "Combat grip fixture"); kill(h, holder);
        require(!prey.isPassenger() && prey.isNoAi() && holder.grippedPrey() == null && !holder.isVehicle(), "Death releases prey without modifying its NoAI or adding another seat"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void nativeLootCustomizationKeepsVariantAndOriginalAgeCountRanges(GameTestHelper h) {
        var function = CustomizeToDragon.CODEC.codec().parse(com.mojang.serialization.JsonOps.INSTANCE, new com.google.gson.JsonObject()).getOrThrow();
        for (int age : new int[]{12, 25, 75, 125}) for (int color = 0; color < 4; color++) {
            var d = dragon(h, age, true); d.setVariant(color); d.getRandom().setSeed(1024);
            var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(h.getLevel())
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, d)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, d.position())
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, d.damageSources().generic())
                    .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
            var context = new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(java.util.Optional.empty());
            for (int sample = 0; sample < 10; sample++) {
                for (var item : new Item[]{IafContent.DRAGON_BONE, IafContent.FIRE_DRAGON_FLESH}) {
                    var drop = function.apply(new ItemStack(item), context);
                    require(drop.is(item) && drop.getCount() >= 1 && drop.getCount() <= 1 + age / 25, "Original bone/flesh count range");
                }
                var scales = function.apply(new ItemStack(IafContent.FIRE_SCALES[0]), context);
                require((scales.isEmpty() && age < 25 || scales.is(IafContent.FIRE_SCALES[color])) && scales.getCount() >= age / 25
                        && scales.getCount() <= age / 25 + age / 5, "Original zero-capable scales and variant");
                var egg = function.apply(new ItemStack(IafContent.FIRE_EGGS[0]), context);
                require(age >= 75 ? egg.is(IafContent.FIRE_EGGS[color]) && egg.getCount() == 1
                        : egg.is(IafContent.FIRE_SCALES[color]) && egg.getCount() >= 1 && egg.getCount() <= 1 + age / 5, "Original adult egg versus juvenile scales conversion");
            }
            d.discard();
        }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void fullInventoryBloodFallbackAndBuildPermissionCannotDuplicateOrLoseLoot(GameTestHelper h) {
        var p = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, 100, true); kill(h, d); p.setPos(d.position().add(2, 0, 0));
        for (int slot = 0; slot < p.getInventory().getContainerSize(); slot++) p.getInventory().setItem(slot, new ItemStack(Items.DIAMOND, 64));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2)); harvest(d, p);
        require(d.deathStage() == 1 && p.getMainHandItem().getCount() == 1 && p.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) == 0
                && drops(h, d, IafContent.FIRE_DRAGON_BLOOD) == 1, "Full inventory drops the first blood once instead of losing it");
        harvest(d, p);
        require(d.deathStage() == 2 && p.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) + drops(h, d, IafContent.FIRE_DRAGON_BLOOD) == 2, "Last bottle frees a slot without duplicating its blood");
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); p.getAbilities().mayBuild = false;
        p.interactOn(d, InteractionHand.MAIN_HAND, Vec3.ZERO);
        require(d.deathStage() == 2, "Build-disabled players cannot harvest remains"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void youngestDeathRemovalAndFirstHarvestableBabyKeepOriginalBoundary(GameTestHelper h) {
        var young = dragon(h, 4, true); kill(h, young); young.tick(); require(young.isRemoved(), "Under-five-day death has original zero harvest threshold");
        var first = dragon(h, 5, true); kill(h, first); first.tick(); require(!first.isRemoved(), "Five-day corpse survives for its one harvest");
        var p = h.makeMockPlayer(GameType.SURVIVAL); p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); harvest(first, p);
        require(first.isRemoved() && drops(h, first, IafContent.DRAGON_SKULL_FIRE) == 1, "First harvestable baby yields its original final skull"); h.succeed();
    }
}
