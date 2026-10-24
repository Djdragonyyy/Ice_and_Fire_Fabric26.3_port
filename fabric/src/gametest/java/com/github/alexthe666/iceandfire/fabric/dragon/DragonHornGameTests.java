package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonHornGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, Player owner) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setTame(true, false); d.setOwner(owner); d.setNoAi(true);
        d.setPos(h.absoluteVec(new Vec3(8.5, 2, 8.5))); h.getLevel().addFreshEntity(d); return d;
    }
    private static InteractionResult release(GameTestHelper h, Player owner, BlockPos pos, Direction face) {
        return IafContent.DRAGON_HORN.useOn(new UseOnContext(owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(h.absoluteVec(Vec3.atCenterOf(pos)), face, h.absolutePos(pos), false)));
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 140, maxTicks = 40)
    public void hornPersistsCompleteDragonAndInventoryThenReleasesExactlyOnce(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, owner);
        for (int i = 0; i < 75; i++) d.growOneDay();
        d.setVariant(2); d.setMale(true); d.setCustomName(Component.literal("Horn persistence"));
        d.setHunger(73); d.setHealth(160); d.setCommand(2); d.refreshDimensions();
        d.dragonInventory.setItem(0, new ItemStack(Items.BANNER.blue()));
        d.dragonInventory.setItem(1, new ItemStack(DragonArmorItem.ITEMS.get(0)));
        var horn = new ItemStack(IafContent.DRAGON_HORN); owner.setItemInHand(InteractionHand.MAIN_HAND, horn);
        var uuid = d.getUUID();
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, owner, d, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS
                && d.isRemoved() && DragonHornItem.filled(horn), "Owner captures a single complete dragon");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        output.store("Horn", ItemStack.CODEC, horn);
        var restoredHorn = TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()).read("Horn", ItemStack.CODEC).orElseThrow();
        owner.setItemInHand(InteractionHand.MAIN_HAND, restoredHorn);
        var tooltip = new java.util.ArrayList<Component>();
        IafContent.DRAGON_HORN.appendHoverText(restoredHorn, net.minecraft.world.item.Item.TooltipContext.of(h.getLevel()),
                net.minecraft.world.item.component.TooltipDisplay.DEFAULT, tooltip::add, net.minecraft.world.item.TooltipFlag.NORMAL);
        require(tooltip.size() == 4 && tooltip.get(1).getString().equals("Horn persistence"), "Stored horn tooltip preserves the name and four original details");
        h.runAfterDelay(2, () -> {
            var pos = new BlockPos(20, 1, 20); h.setBlock(pos, Blocks.STONE);
            require(release(h, owner, pos, Direction.UP) == InteractionResult.SUCCESS, "Saved native component releases after old entity is removed");
            require(h.getLevel().getEntity(uuid) instanceof FireDragon, "Original UUID restored");
            var restored = (FireDragon) h.getLevel().getEntity(uuid);
            // makeMockPlayer is not registered in the level: verify its persisted UUID reference here.
            // The connected-client test separately verifies actual isOwnedBy resolution after release.
            require(restored.isTame() && restored.getOwnerReference() != null && restored.getOwnerReference().matches(owner)
                    && restored.ageDays() == 75 && restored.variant() == 2 && restored.male()
                    && restored.getCustomName().getString().equals("Horn persistence") && restored.hunger() == 73
                    && restored.getHealth() == 160 && restored.command() == 2, "Lifecycle roundtrip: owner=" + restored.isOwnedBy(owner)
                    + " days=" + restored.ageDays() + " variant=" + restored.variant() + " male=" + restored.male()
                    + " name=" + restored.getCustomName() + " hunger=" + restored.hunger() + " health=" + restored.getHealth()
                    + " maxHealth=" + restored.getMaxHealth() + " command=" + restored.command());
            require(restored.dragonInventory.getItem(0).is(Items.BANNER.blue()) && restored.dragonInventory.getItem(1).is(DragonArmorItem.ITEMS.get(0)), "Equipment remains inside the dragon");
            require(!DragonHornItem.filled(restoredHorn) && !restoredHorn.has(DataComponents.CUSTOM_MODEL_DATA), "Successful release clears content and filled icon");
            require(release(h, owner, pos, Direction.UP) == InteractionResult.PASS
                    && h.getLevel().getEntitiesOfClass(FireDragon.class, h.getBounds()).size() == 1, "Empty horn cannot release again");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140, maxTicks = 40)
    public void failedPlacementAndDuplicateUuidPreserveHornData(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var d = dragon(h, owner);
        var horn = new ItemStack(IafContent.DRAGON_HORN); owner.setItemInHand(InteractionHand.MAIN_HAND, horn);
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, owner, d, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS, "Capture fixture");
        var before = horn.get(DataComponents.CUSTOM_DATA);
        h.runAfterDelay(2, () -> {
            var pos = new BlockPos(20, 1, 20); h.setBlock(pos, Blocks.STONE); h.setBlock(pos.above(), Blocks.BEDROCK);
            require(release(h, owner, pos, Direction.UP) == InteractionResult.FAIL && before.equals(horn.get(DataComponents.CUSTOM_DATA)), "Obstruction preserves the stored dragon");
            h.setBlock(pos.above(), Blocks.AIR);
            require(release(h, owner, pos, Direction.NORTH) == InteractionResult.FAIL && DragonHornItem.filled(horn), "Only top face releases");
            var copy = horn.copy();
            require(release(h, owner, pos, Direction.UP) == InteractionResult.SUCCESS, "Unblocked release");
            owner.setItemInHand(InteractionHand.MAIN_HAND, copy);
            require(release(h, owner, pos, Direction.UP) == InteractionResult.FAIL && DragonHornItem.filled(copy), "Copied horn cannot duplicate a loaded UUID");
            require(h.getLevel().getEntitiesOfClass(FireDragon.class, h.getBounds()).size() == 1, "One dragon remains");
            h.succeed();
        });
    }

    @GameTest
    public void malformedDataAndAdventurePlacementCannotConsumeStoredDragon(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.ADVENTURE); var d = dragon(h, owner);
        GameType.ADVENTURE.updatePlayerAbilities(owner.getAbilities());
        var horn = new ItemStack(IafContent.DRAGON_HORN); owner.setItemInHand(InteractionHand.MAIN_HAND, horn);
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, owner, d, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS, "Owner can store in adventure like original interaction");
        var before = horn.get(DataComponents.CUSTOM_DATA);
        require(!owner.mayBuild() && release(h, owner, new BlockPos(1, 1, 1), Direction.UP) == InteractionResult.FAIL
                && before.equals(horn.get(DataComponents.CUSTOM_DATA)), "Adventure placement permission is respected before consuming data");
        var survival = h.makeMockPlayer(GameType.SURVIVAL); survival.setItemInHand(InteractionHand.MAIN_HAND, horn);
        var malformed = new net.minecraft.nbt.CompoundTag(); malformed.putString("DragonHornEntityID", "iceandfire:fire_dragon");
        var entity = new net.minecraft.nbt.CompoundTag(); entity.putString("id", "iceandfire:fire_dragon"); malformed.put("EntityTag", entity);
        horn.set(DataComponents.CUSTOM_DATA, CustomData.of(malformed));
        before = horn.get(DataComponents.CUSTOM_DATA);
        require(release(h, survival, new BlockPos(1, 1, 1), Direction.UP) == InteractionResult.FAIL
                && before.equals(horn.get(DataComponents.CUSTOM_DATA)), "Missing UUID is rejected without silently creating a new default dragon");
        h.succeed();
    }
    @GameTest
    public void creativeNativeInteractionUpdatesRealHeldHorn(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.CREATIVE); var d = dragon(h, owner);
        GameType.CREATIVE.updatePlayerAbilities(owner.getAbilities());
        var horn = new ItemStack(IafContent.DRAGON_HORN); owner.setItemInHand(InteractionHand.MAIN_HAND, horn);
        require(owner.hasInfiniteMaterials(), "Creative fixture uses the native item-copy branch");
        require(owner.interactOn(d, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction()
                && d.isRemoved() && DragonHornItem.filled(owner.getMainHandItem()) && owner.getMainHandItem() == horn,
                "Creative native interaction updates the real horn instead of a discarded temporary copy");
        h.succeed();
    }
    @GameTest
    public void ownershipHandPassengersAndOccupiedHornPreventCapture(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL); var stranger = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, owner); var horn = new ItemStack(IafContent.DRAGON_HORN);
        stranger.setItemInHand(InteractionHand.MAIN_HAND, horn);
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, stranger, d, InteractionHand.MAIN_HAND) == InteractionResult.FAIL && !d.isRemoved(), "Stranger cannot capture");
        owner.setItemInHand(InteractionHand.OFF_HAND, horn);
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, owner, d, InteractionHand.OFF_HAND) == InteractionResult.FAIL, "Capture is main hand only");
        owner.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY); owner.setItemInHand(InteractionHand.MAIN_HAND, horn);
        var data = new net.minecraft.nbt.CompoundTag(); data.putString("DragonHornEntityID", "unsupported:type");
        horn.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, owner, d, InteractionHand.MAIN_HAND) == InteractionResult.FAIL, "Unsupported occupied data never overwritten");
        horn.remove(DataComponents.CUSTOM_DATA);
        for (int i = 0; i < 50; i++) d.growOneDay();
        require(owner.startRiding(d), "Pilot fixture");
        require(IafContent.DRAGON_HORN.interactLivingEntity(horn, owner, d, InteractionHand.MAIN_HAND) == InteractionResult.FAIL && owner.getVehicle() == d, "Capture cannot discard a pilot's mount");
        owner.stopRiding(); h.succeed();
    }
}
