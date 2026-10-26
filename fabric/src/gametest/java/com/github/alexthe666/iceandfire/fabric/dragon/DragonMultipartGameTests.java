package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonMultipartGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void close(double actual, double expected, String message) { require(Math.abs(actual - expected) < 0.003, message + " actual=" + actual + " expected=" + expected); }
    private static void advance(FireDragon dragon) { dragon.tickCount++; dragon.tick(); }
    private static FireDragon dragon(GameTestHelper h, int age) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setNoAi(true); d.setSilent(true); d.setYRot(0); d.yBodyRot = d.yHeadRot = 0;
        d.setPos(h.absoluteVec(new Vec3(24.5, 10, 24.5))); d.refreshDimensions(); h.getLevel().addFreshEntity(d); return d;
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void tenOriginalPartsUseRenderScaleAtEveryStageWithoutChangingPhysicalCollider(GameTestHelper h) {
        for (int age : new int[]{0, 25, 50, 75, 125}) {
            var d = dragon(h, age); float width = d.getBbWidth(), height = d.getBbHeight(); advance(d);
            var parts = d.dragonParts(); require(parts.size() == 10, "Ten original parts");
            close(d.getBbWidth(), width, "Body width remains unchanged"); close(d.getBbHeight(), height, "Body height remains unchanged");
            float scale = DragonGrowth.renderSize(age) / 3;
            for (var part : parts) {
                require(part.parent() == d && part.is(d) && part.isPickable() && !part.shouldBeSaved(), "Correct transient parent and pick identity");
                require(!part.canBeCollidedWith(d) && !part.isPushable(), "Hitboxes do not obstruct navigation or push entities");
                var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
                require(!part.save(out), "Parts are not independently persisted");
            }
            var head = parts.get(0); var tail = parts.get(9); var wing = parts.get(2);
            close(head.getX(), d.getX(), "Head centered at body yaw0"); close(head.getZ(), d.getZ() + 1.55 * scale, "Original head radius"); close(head.getY(), d.getY() + 0.6 * scale, "Original head offset");
            close(head.getBbWidth(), 0.5 * scale, "Head width"); close(head.getBbHeight(), 0.35 * scale, "Head height");
            close(tail.getZ(), d.getZ() - 1.95 * scale, "Tail radius"); close(tail.getBbWidth(), 0.45 * scale, "Tail width");
            close(wing.getX(), d.getX() - scale, "Right wing original yaw90"); close(wing.getY(), d.getY() + 0.5 * scale, "Wing height");
            d.yBodyRot = 90; for (var part : parts) part.follow(d);
            close(head.getX(), d.getX() - 1.55 * scale, "Head follows body yaw90"); close(head.getZ(), d.getZ(), "Head yaw90 z");
            d.discard(); require(parts.stream().allMatch(Entity::isRemoved), "Parent removal cleans every child");
        }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void serverDamageMultipliersAndSelfChargeProtectionUseTheActualParent(GameTestHelper h) {
        var d = dragon(h, 125); d.setHealth(d.getMaxHealth()); advance(d); float health = d.getHealth();
        var source = d.damageSources().genericKill();
        require(d.dragonParts().get(0).hurtServer(h.getLevel(), source, 20), "Head damage forwarded"); close(health - d.getHealth(), 30, "Head multiplier1.5"); health = d.getHealth();
        require(!d.dragonParts().get(2).hurtServer(h.getLevel(), source, 20) && d.getHealth() == health, "All parts share the parent damage cooldown");
        for (int tick = 0; tick < 20; tick++) advance(d);
        require(d.dragonParts().get(2).hurtServer(h.getLevel(), source, 20), "Wing damage forwarded"); close(health - d.getHealth(), 10, "Wing multiplier.5"); health = d.getHealth();
        for (int tick = 0; tick < 20; tick++) advance(d);
        require(d.dragonParts().get(9).hurtServer(h.getLevel(), source, 20), "Tail-tip damage forwarded"); close(health - d.getHealth(), 30, "Tail-tip multiplier1.5");
        require(!d.dragonParts().get(0).hurtServer(h.getLevel(), d.damageSources().inWall(), 100), "Parts retain original environmental immunities");
        var charge = IafContent.FIRE_DRAGON_CHARGE.create(h.getLevel(), EntitySpawnReason.TRIGGERED); charge.setOwner(d);
        require(DragonFireCombat.friendly(d, d.dragonParts().get(0)) && !charge.canHitEntity(d.dragonParts().get(0)), "Own head cannot intercept owner's charge");
        var p = h.makeMockPlayer(GameType.SURVIVAL); d.setTame(true, false); d.setOwner(p);
        require(!p.startRiding(d.dragonParts().get(0), true, true), "No forced extra seat on a hitbox");
        require(p.startRiding(d) && p.getRootVehicle() == d.dragonParts().get(0).getRootVehicle(), "Pilot and hitboxes share root vehicle for self-picking exclusion"); p.stopRiding();
        for (int tick = 0; tick < 25; tick++) advance(d);
        var sword = new ItemStack(Items.NETHERITE_SWORD); p.setItemInHand(InteractionHand.MAIN_HAND, sword);
        health = d.getHealth(); p.attack(d.dragonParts().get(0));
        require(d.getHealth() < health && sword.getDamageValue() == 1, "Survival native attack damages parent and wears the weapon exactly once"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void nativePartInteractionDelegatesStaffAndHornExactlyOnceThenRebuildsOnRelease(GameTestHelper h) {
        var p = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, 50); d.setTame(true, false); d.setOwner(p); advance(d);
        var parts = d.dragonParts(); var tail = parts.get(9); p.setPos(tail.position().add(1, 0, 0));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafContent.DRAGON_STAFF));
        require(p.interactOn(tail, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction() && d.command() == 1, "Staff cycle forwarded exactly once");
        var horn = new ItemStack(IafContent.DRAGON_HORN); p.setItemInHand(InteractionHand.MAIN_HAND, horn);
        require(p.interactOn(tail, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction() && DragonHornItem.filled(horn) && d.isRemoved(), "Native item delegation captures through tail");
        require(parts.stream().allMatch(Entity::isRemoved), "Horn cannot leave orphan parts");
        h.runAfterDelay(2, () -> {
            var pos = new BlockPos(24, 8, 24); for (int x = 18; x <= 30; x++) for (int z = 18; z <= 30; z++) h.setBlock(new BlockPos(x, 8, z), Blocks.STONE);
            var result = IafContent.DRAGON_HORN.useOn(new UseOnContext(p, InteractionHand.MAIN_HAND,
                    new BlockHitResult(h.absoluteVec(Vec3.atCenterOf(pos)), Direction.UP, h.absolutePos(pos), false)));
            require(result.consumesAction() && !DragonHornItem.filled(horn), "Horn release succeeds");
            var restored = (FireDragon) h.getLevel().getEntity(d.getUUID()); advance(restored);
            require(restored.dragonParts().size() == 10 && restored.dragonParts().stream().noneMatch(parts::contains), "Release builds a fresh transient family"); h.succeed();
        });
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void corpsePartsPermitBloodAndFinalHarvestAndDisappearWithTheRemains(GameTestHelper h) {
        var d = dragon(h, 25); require(d.hurtServer(h.getLevel(), d.damageSources().generic(), 10000), "Death before first multipart tick"); advance(d);
        var parts = d.dragonParts(); require(parts.size() == 10 && d.corpse(), "Corpses also initialize their original parts");
        var p = h.makeMockPlayer(GameType.SURVIVAL); var head = parts.get(0); p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2));
        p.interactOn(head, InteractionHand.MAIN_HAND, Vec3.ZERO); p.interactOn(head, InteractionHand.MAIN_HAND, Vec3.ZERO);
        require(d.deathStage() == 2 && p.getInventory().countItem(IafContent.FIRE_DRAGON_BLOOD) == 2, "Blood collection through head");
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        for (int step = 0; step < 3; step++) p.interactOn(head, InteractionHand.MAIN_HAND, Vec3.ZERO);
        require(d.deathStage() == 5 && parts.stream().allMatch(Entity::isRemoved), "Final skull removes body and all parts together"); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void growthAndMissingPartRebuildDoNotLeakAndUnattachedEntitiesExpire(GameTestHelper h) {
        var d = dragon(h, 25); advance(d); var original = d.dragonParts(); var head = original.get(0); float oldWidth = head.getBbWidth();
        for (int i = 0; i < 100; i++) d.growOneDay(); advance(d);
        require(d.dragonParts().equals(original) && head.getBbWidth() > oldWidth, "Growth resizes the same family instead of leaking old entities");
        close(head.getBbWidth(), 5, "Adult uncapped render scale"); close(d.getBbWidth(), 0.78 * 7, "Physical cap stays seven");
        original.get(4).discard(); advance(d); require(d.dragonParts().size() == 10 && original.stream().allMatch(Entity::isRemoved), "Missing part rebuild cleans the old family");
        var orphan = IafContent.DRAGON_MULTIPART.create(h.getLevel(), EntitySpawnReason.TRIGGERED); h.getLevel().addFreshEntity(orphan);
        for (int i = 0; i < 22; i++) { orphan.tickCount++; orphan.tick(); }
        require(orphan.isRemoved() && !orphan.isPickable(), "Unattached part expires without forcing parent chunks");
        var last = d.dragonParts(); d.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        require(last.stream().allMatch(Entity::isRemoved), "Chunk unload cleans transient children too"); h.succeed();
    }
}
