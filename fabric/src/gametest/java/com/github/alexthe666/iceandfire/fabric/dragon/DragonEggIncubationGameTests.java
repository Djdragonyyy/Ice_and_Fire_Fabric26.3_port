package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DragonEggIncubationGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragonEgg egg(GameTestHelper h, BlockPos at, int variant) {
        h.setBlock(at.below(), Blocks.NETHERRACK); h.setBlock(at, Blocks.FIRE);
        var e = IafContent.DRAGON_EGG.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        e.setVariant(variant); e.setNoAi(true); e.setNoGravity(true);
        e.setPos(h.absoluteVec(new Vec3(at.getX() + 0.8, at.getY() + 0.2, at.getZ() + 0.2)));
        h.getLevel().addFreshEntity(e); return e;
    }
    private static net.minecraft.nbt.CompoundTag save(GameTestHelper h, FireDragonEgg e) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess()); e.saveWithoutId(out); return out.buildResult();
    }
    private static void load(GameTestHelper h, FireDragonEgg e, net.minecraft.nbt.CompoundTag tag) {
        e.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), tag));
    }
    private static void advance(FireDragonEgg e) { e.tickCount++; e.tick(); }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void fireLavaAndSoulFireIncubateWhileWaterPausesWithoutReset(GameTestHelper h) {
        var previous = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonEggTime = 100; DragonCombatConfig.current = config;
            var at = new BlockPos(10, 10, 10); var e = egg(h, at, 0); advance(e);
            require(save(h, e).getLongOr("DragonAge", -1) == 1, "Ordinary fire advances incubation");
            h.setBlock(at, Blocks.WATER); advance(e); require(save(h, e).getLongOr("DragonAge", -1) == 1, "Cold water pauses without resetting progress");
            h.setBlock(at, Blocks.LAVA); advance(e); require(save(h, e).getLongOr("DragonAge", -1) == 2, "Lava restores the original Forge incubation condition");
            h.setBlock(at.below(), Blocks.SOUL_SOIL); h.setBlock(at, Blocks.SOUL_FIRE); advance(e);
            require(save(h, e).getLongOr("DragonAge", -1) == 3, "Existing native fire-tag support retained");
            require(!FireDragonEgg.canIncubate(Blocks.MAGMA_BLOCK.defaultBlockState()) && !FireDragonEgg.canIncubate(Blocks.CAMPFIRE.defaultBlockState()), "No invented generic hot-block incubation");
        } finally { DragonCombatConfig.current = previous; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void everyColorHatchesOnceAtOriginalBlockCenterWithNameOwnerAndHunger(GameTestHelper h) {
        var previous = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonEggTime = 1; DragonCombatConfig.current = config;
            var owner = h.makeMockServerPlayerInLevel();
            for (int variant = 0; variant < 4; variant++) {
                var at = new BlockPos(6 + variant * 9, 10, 24); var e = egg(h, at, variant);
                if ((variant & 1) != 0) h.setBlock(at, Blocks.LAVA);
                e.setOwner(owner); e.setCustomName(Component.literal("Hatch " + variant));
                var tag = save(h, e); tag.putLong("DragonAge", 1); load(h, e, tag); advance(e);
                var born = h.getLevel().getEntitiesOfClass(FireDragon.class, new AABB(h.absolutePos(at)).inflate(3));
                require(e.isRemoved() && born.size() == 1, "One hatchling and no remaining egg for color " + variant);
                var d = born.getFirst(); var expected = h.absoluteVec(new Vec3(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5));
                require(d.position().distanceToSqr(expected) < 0.000001 && d.variant() == variant && d.ageDays() == 0 && d.hunger() == 50, "Original centered position, color, newborn age and hunger");
                require(d.isTame() && d.isOwnedBy(owner) && d.getCustomName().getString().equals("Hatch " + variant), "Name and actual owner retained");
                require(h.getBlockState(at).isAir(), "Consumed incubation block cleared"); d.discard();
            }
        } finally { DragonCombatConfig.current = previous; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void saveLoadPreservesProgressAndStrictThresholdAcrossColdTicks(GameTestHelper h) {
        var previous = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonEggTime = 3; DragonCombatConfig.current = config;
            var at = new BlockPos(24, 10, 24); var original = egg(h, at, 2); advance(original); advance(original);
            var tag = save(h, original); original.discard();
            var restored = IafContent.DRAGON_EGG.create(h.getLevel(), EntitySpawnReason.LOAD); load(h, restored, tag);
            require(h.getLevel().addFreshEntity(restored), "Reloaded egg is registered in the actual level");
            h.setBlock(at, Blocks.WATER); advance(restored); require(save(h, restored).getLongOr("DragonAge", -1) == 2 && !restored.isRemoved(), "Saved progress survives a cold tick");
            h.setBlock(at, Blocks.LAVA); advance(restored); require(!restored.isRemoved() && save(h, restored).getLongOr("DragonAge", -1) == 3, "Equality is not enough to hatch");
            advance(restored); require(restored.isRemoved() && h.getLevel().getEntitiesOfClass(FireDragon.class, new AABB(h.absolutePos(at)).inflate(3)).size() == 1, "Strict greater-than threshold hatches once after resumed heat");
        } finally { DragonCombatConfig.current = previous; }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void eggCollectedDuringNativeEffectTickCannotAlsoProduceAHatchling(GameTestHelper h) {
        var previous = DragonCombatConfig.current;
        try {
            var config = new DragonCombatConfig(); config.dragonEggTime = 1; DragonCombatConfig.current = config;
            var at = new BlockPos(24, 10, 24); var e = egg(h, at, 1); var area = new AABB(h.absolutePos(at)).inflate(3);
            var tag = save(h, e); tag.putLong("DragonAge", 2); load(h, e, tag);
            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 3)); advance(e);
            require(e.isRemoved(), "Native wither effect collects the ready egg inside super.tick");
            require(h.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream().filter(i -> i.getItem().is(IafContent.FIRE_EGGS[1])).mapToInt(i -> i.getItem().getCount()).sum() == 1, "Exactly one returned egg item");
            require(h.getLevel().getEntitiesOfClass(FireDragon.class, area).isEmpty(), "No hatchling from an egg already removed during the same tick");
        } finally { DragonCombatConfig.current = previous; }
        h.succeed();
    }
}
