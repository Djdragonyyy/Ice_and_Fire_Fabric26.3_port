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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DragonSkullGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static UseOnContext placement(GameTestHelper h, net.minecraft.world.entity.player.Player p, BlockPos pos, Direction face) {
        return new UseOnContext(p, InteractionHand.MAIN_HAND, new BlockHitResult(h.absoluteVec(Vec3.atCenterOf(pos)), face, h.absolutePos(pos), false));
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 80)
    public void stageMetadataSurvivesPlacementRotationSaveAndOneBreakDrop(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (int stage = 1; stage <= 5; stage++) {
            var data = new DragonSkullData(stage, (stage - 1) * 25 + 12);
            var item = DragonSkullItem.stack(data); item.set(DataComponents.CUSTOM_NAME, Component.literal("Skull " + stage));
            var itemOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            itemOutput.store("Skull", ItemStack.CODEC, item);
            var savedItem = TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), itemOutput.buildResult()).read("Skull", ItemStack.CODEC).orElseThrow();
            player.setItemInHand(InteractionHand.MAIN_HAND, savedItem); player.setYRot(37);
            var pos = new BlockPos(stage * 2, 1, 3); h.setBlock(pos, Blocks.STONE);
            var result = IafContent.DRAGON_SKULL_FIRE.useOn(placement(h, player, pos, Direction.UP));
            require(result.consumesAction() && savedItem.isEmpty(), "Survival placement stage=" + stage + " result=" + result + " count=" + savedItem.getCount() + " build=" + player.mayBuild() + " creative=" + player.isCreative());
            var area = new net.minecraft.world.phys.AABB(h.absolutePos(pos)).inflate(1.5);
            var skull = h.getLevel().getEntitiesOfClass(DragonSkull.class, area).stream().findFirst().orElseThrow();
            require(skull.skullData().equals(data) && skull.dragonYaw() == 37 && !skull.onWall(), "Stage/age/name/orientation preserved");
            require(skull.getCustomName().getString().equals("Skull " + stage), "Custom name preserved");
            player.setShiftKeyDown(true); player.setYRot(92);
            require(player.interactOn(skull, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction() && skull.dragonYaw() == 92, "Native shift interaction rotates skull"); player.setShiftKeyDown(false);
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess()); skull.saveWithoutId(output);
            var restored = IafContent.DRAGON_SKULL.create(h.getLevel(), EntitySpawnReason.LOAD);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            require(restored.skullData().equals(data) && restored.dragonYaw() == 92 && restored.isNoAi() && restored.isPersistenceRequired(), "Native entity persistence");
            require(skull.hurtServer(h.getLevel(), skull.damageSources().playerAttack(player), 1), "Break returns item");
            require(!skull.hurtServer(h.getLevel(), skull.damageSources().playerAttack(player), 1), "Removed skull cannot duplicate drops");
            var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, area);
            require(drops.size() == 1 && drops.getFirst().getItem().getCount() == 1 && DragonSkullItem.data(drops.getFirst().getItem()).equals(data), "Exactly one item retains stage/age");
            require(drops.getFirst().getItem().getHoverName().getString().equals("Skull " + stage), "Break retains custom name"); drops.forEach(ItemEntity::discard);
        }
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 80)
    public void defaultCreativeWallPlacementAndObstructionRespectItemAuthority(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.CREATIVE); GameType.CREATIVE.updatePlayerAbilities(player.getAbilities());
        var stack = new ItemStack(IafContent.DRAGON_SKULL_FIRE); player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var pos = new BlockPos(4, 3, 4); h.setBlock(pos, Blocks.STONE);
        require(IafContent.DRAGON_SKULL_FIRE.useOn(placement(h, player, pos, Direction.EAST)).consumesAction() && stack.getCount() == 1, "Creative does not consume");
        var skull = h.getLevel().getEntitiesOfClass(DragonSkull.class, new net.minecraft.world.phys.AABB(h.absolutePos(pos)).inflate(2)).getFirst();
        require(skull.onWall() && skull.skullData().equals(DragonSkullData.DEFAULT), "Uninitialized creative skull has original stage4 age75 default and wall pose");
        var dragon = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        require(!dragon.canAttack(skull) && DragonFireCombat.friendly(dragon, skull), "Decorative skull excluded from dragon prey and breath targets");
        skull.discard(); h.setBlock(pos.relative(Direction.EAST), Blocks.STONE);
        require(!IafContent.DRAGON_SKULL_FIRE.useOn(placement(h, player, pos, Direction.EAST)).consumesAction() && stack.getCount() == 1, "Solid obstruction does not consume or place");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 80)
    public void fireFleshUsesNativeNutritionConsumptionAndOriginalFireEffect(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL); player.getFoodData().setFoodLevel(10); player.getFoodData().setSaturation(0);
        var flesh = new ItemStack(IafContent.FIRE_DRAGON_FLESH, 2);
        require(flesh.is(net.minecraft.tags.ItemTags.WOLF_FOOD) && flesh.is(net.minecraft.tags.ItemTags.MEAT), "Original wolf-food/meat classification retained");
        var result = flesh.finishUsingItem(h.getLevel(), player);
        require(result.getCount() == 1 && player.getFoodData().getFoodLevel() == 18, "Native consumption applies eight nutrition and one item");
        require(Math.abs(player.getFoodData().getSaturationLevel() - 12.8F) < 0.001F && player.getRemainingFireTicks() == 100, "Original .8 saturation modifier and five-second fire");
        h.succeed();
    }
}
