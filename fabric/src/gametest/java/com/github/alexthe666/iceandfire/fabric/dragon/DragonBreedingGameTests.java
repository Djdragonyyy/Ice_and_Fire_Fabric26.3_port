package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.IafTerrain;
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
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public class DragonBreedingGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void floor(GameTestHelper h) {
        for (int x = 1; x < 47; x++) for (int z = 1; z < 47; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
    }
    private static FireDragon dragon(GameTestHelper h, int days, boolean male, double x) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < days; i++) d.growOneDay();
        d.setMale(male);
        d.setPos(h.absoluteVec(new Vec3(x, 2, 24.5)));
        d.setOnGround(true);
        d.getRandom().setSeed(140);
        return d;
    }
    private static void feed(FireDragon d, Player player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.FIRE_STEW, 2));
        d.mobInteract(player, InteractionHand.MAIN_HAND);
        require(d.isInLove() && player.getMainHandItem().getCount() == 1, "One fire stew starts love");
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 130, padding = 200)
    public void adultPairCourtsCreatesOneEggAndNestAndPersistsCooldown(GameTestHelper h) {
        floor(h);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var male = dragon(h, 75, true, 12.5);
        var female = dragon(h, 75, false, 28.5);
        feed(male, player); feed(female, player);
        h.getLevel().addFreshEntity(male); h.getLevel().addFreshEntity(female);
        double initialDistance = male.distanceTo(female);
        h.runAfterDelay(35, () -> {
            require(male.distanceTo(female) < initialDistance - 1, "Courtship obtains real movement toward partner");
            require(h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds()).isEmpty(), "No egg before sixty courtship ticks");
        });
        h.runAfterDelay(95, () -> {
            var eggs = h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds());
            require(eggs.size() == 1, "Two active mating goals must produce exactly one egg");
            var egg = eggs.getFirst();
            require(egg.variant() >= 0 && egg.variant() <= 3 && egg.ownerId() == null, "Bred fire egg has original random variant and no owner until placed");
            require(!male.isInLove() && !female.isInLove() && male.getAge() > 5900 && female.getAge() > 5900, "Parents clear love and enter six-thousand-tick cooldown");
            int floorY = h.absolutePos(new BlockPos(0, 1, 0)).getY();
            BlockPos center = new BlockPos(egg.getBlockX(), floorY, egg.getBlockZ());
            int nests = 0;
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) if (h.getLevel().getBlockState(p).is(IafTerrain.NEST)) nests++;
            require(nests == 8 && h.getLevel().getBlockState(center).is(Blocks.DIRT_PATH), "Original three-by-three nest with path center: egg=" + egg.position() + " center=" + center + " block=" + h.getLevel().getBlockState(center) + " nests=" + nests);
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            male.saveWithoutId(output);
            var restored = dragon(h, 0, true, 4.5);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            require(restored.getAge() == male.getAge() && !restored.isInLove() && restored.stage() == 4, "Breeding cooldown survives save without changing dragon growth stage");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.FIRE_STEW));
            male.mobInteract(player, InteractionHand.MAIN_HAND);
            require(player.getMainHandItem().getCount() == 1 && !male.isInLove(), "Fire stew cannot erase cooldown");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 200)
    public void matingValidatesAgeSexLoveAndControlStates(GameTestHelper h) {
        floor(h);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var young = dragon(h, 50, true, 10.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.FIRE_STEW));
        young.mobInteract(player, InteractionHand.MAIN_HAND);
        require(!young.isInLove() && player.getMainHandItem().getCount() == 1, "Stage three cannot breed or consume stew");
        var male = dragon(h, 75, true, 12.5);
        var other = dragon(h, 75, true, 24.5);
        feed(male, player); feed(other, player);
        require(!male.canMate(other), "Equal sex cannot mate");
        other.setMale(false); other.resetLove();
        require(!male.canMate(other), "Both partners must be fed");
        feed(other, player);
        require(male.canMate(other), "Opposite adult ready pair accepted");
        other.setCommand(1); require(!male.canMate(other), "Sit prevents mating");
        other.setCommand(2); require(!male.canMate(other), "Follow keeps priority");
        other.setCommand(0); other.setNoAi(true); require(!male.canMate(other), "NoAI prevents mating");
        other.setNoAi(false); other.setAge(6000); require(!male.canMate(other), "Cooldown prevents mating");
        var nether = h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        require(nether != null, "Nether available for dimension validation");
        var foreign = IafContent.FIRE_DRAGON.create(nether, EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < 75; i++) foreign.growOneDay();
        foreign.setMale(false); foreign.setOnGround(true); foreign.setInLoveTime(600);
        require(!male.canMate(foreign), "Partners in different dimensions cannot mate");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 110, padding = 200)
    public void singleFedPartnerDoesNotCreateEgg(GameTestHelper h) {
        floor(h);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var male = dragon(h, 75, true, 12.5);
        var female = dragon(h, 75, false, 28.5);
        feed(male, player);
        h.getLevel().addFreshEntity(male); h.getLevel().addFreshEntity(female);
        h.runAfterDelay(90, () -> {
            require(h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds()).isEmpty(), "Unfed partner cannot produce an egg");
            require(male.getAge() == 0 && female.getAge() == 0, "Unsuccessful courtship does not consume cooldown");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 200)
    public void blockedEggPositionKeepsLoveAndTerrainIntact(GameTestHelper h) {
        floor(h);
        var male = dragon(h, 75, true, 12.5);
        var female = dragon(h, 75, false, 28.5);
        male.setInLoveTime(600); female.setInLoveTime(600);
        h.getLevel().addFreshEntity(male); h.getLevel().addFreshEntity(female);
        h.setBlock(new BlockPos(27, 2, 23), Blocks.STONE);
        var goal = new DragonMateGoal(male);
        require(goal.canUse(), "Pair selected for atomic blocked-position check");
        goal.start();
        for (int i = 0; i < 60; i++) goal.tick();
        require(h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds()).isEmpty(), "No egg inside obstructing block");
        require(male.isInLove() && female.isInLove() && male.getAge() == 0 && female.getAge() == 0, "Failed spawn preserves both parents");
        require(h.getBlockState(new BlockPos(27, 1, 23)).is(Blocks.STONE), "Failed spawn cannot place nest");
        goal.stop();
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 200)
    public void nestHonorsGriefingAndProtectedBlocks(GameTestHelper h) {
        floor(h);
        boolean oldRule = h.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
        try {
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, false, h.getLevel().getServer());
            var male = dragon(h, 75, true, 12.5);
            var female = dragon(h, 75, false, 28.5);
            male.setInLoveTime(600); female.setInLoveTime(600);
            h.getLevel().addFreshEntity(male); h.getLevel().addFreshEntity(female);
            var goal = new DragonMateGoal(male);
            require(goal.canUse(), "Griefing-disabled pair accepted"); goal.start();
            for (int i = 0; i < 60; i++) goal.tick(); goal.stop();
            require(h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds()).size() == 1, "Egg allowed when terrain changes are disabled");
            require(h.getBlockState(new BlockPos(27, 1, 23)).is(Blocks.STONE), "mobGriefing=false preserves ground");
            h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, true, h.getLevel().getServer());
            male.discard(); female.discard();
            h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds()).forEach(FireDragonEgg::discard);
            male = dragon(h, 75, true, 12.5); female = dragon(h, 75, false, 28.5);
            male.setInLoveTime(600); female.setInLoveTime(600);
            h.getLevel().addFreshEntity(male); h.getLevel().addFreshEntity(female);
            h.setBlock(new BlockPos(26, 1, 22), Blocks.CHEST);
            h.setBlock(new BlockPos(28, 1, 24), Blocks.BEDROCK);
            goal = new DragonMateGoal(male);
            require(goal.canUse(), "Second pair accepted"); goal.start();
            for (int i = 0; i < 60; i++) goal.tick(); goal.stop();
            require(h.getBlockState(new BlockPos(26, 1, 23)).is(IafTerrain.NEST), "Nest placement actually runs beside protected cells");
            require(h.getBlockState(new BlockPos(26, 1, 22)).is(Blocks.CHEST) && h.getBlockState(new BlockPos(28, 1, 24)).is(Blocks.BEDROCK), "Nest cannot overwrite chest or unbreakable block");
        } finally { h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, oldRule, h.getLevel().getServer()); }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 200)
    public void bredEggCanBeCollectedPlacedAndHatchedWithNewOwner(GameTestHelper h) {
        floor(h);
        var male = dragon(h, 75, true, 12.5);
        var female = dragon(h, 75, false, 28.5);
        male.setInLoveTime(600); female.setInLoveTime(600);
        h.getLevel().addFreshEntity(male); h.getLevel().addFreshEntity(female);
        var goal = new DragonMateGoal(male);
        require(goal.canUse(), "Pair selected for egg lifecycle check"); goal.start();
        for (int i = 0; i < 60; i++) goal.tick(); goal.stop();
        var eggs = h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds());
        require(eggs.size() == 1 && eggs.getFirst().ownerId() == null, "Breeding creates unclaimed egg");
        var bred = eggs.getFirst();
        int variant = bred.variant();
        var area = bred.getBoundingBox().inflate(2);
        bred.hurtServer(h.getLevel(), bred.damageSources().generic(), 1);
        var drops = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area);
        require(drops.size() == 1 && drops.getFirst().getItem().is(IafContent.FIRE_EGGS[variant]), "Collectible variant preserved");
        var player = h.makeMockServerPlayerInLevel();
        player.setPos(h.absoluteVec(new Vec3(4.5, 2, 4.5)));
        player.setItemInHand(InteractionHand.MAIN_HAND, drops.getFirst().getItem().copy());
        drops.getFirst().discard();
        BlockPos ground = h.absolutePos(new BlockPos(4, 1, 4));
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(ground), net.minecraft.core.Direction.UP, ground, false);
        player.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        eggs = h.getLevel().getEntitiesOfClass(FireDragonEgg.class, h.getBounds());
        require(eggs.size() == 1 && player.getUUID().equals(eggs.getFirst().ownerId()), "Placing claimed egg assigns player ownership");
        var placed = eggs.getFirst();
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        placed.saveWithoutId(output); output.putInt("DragonAge", 7201);
        placed.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        h.getLevel().setBlockAndUpdate(placed.blockPosition(), Blocks.FIRE.defaultBlockState());
        placed.tick();
        var babies = h.getLevel().getEntitiesOfClass(FireDragon.class, h.getBounds(), d -> d.stage() == 1);
        require(placed.isRemoved() && babies.size() == 1 && babies.getFirst().variant() == variant
                && babies.getFirst().getOwnerReference() != null
                && player.getUUID().equals(babies.getFirst().getOwnerReference().getUUID()), "Hatch transfers claimed owner and original variant to baby");
        h.succeed();
    }
}
