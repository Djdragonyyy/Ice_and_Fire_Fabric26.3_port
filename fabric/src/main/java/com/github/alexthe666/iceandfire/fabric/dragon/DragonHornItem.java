package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import java.util.List;
import java.util.function.Consumer;

/** Original ItemDragonHorn storage and tooltip, migrated to native data components (LGPL-3.0). */
public final class DragonHornItem extends Item {
    public DragonHornItem(Properties properties) { super(properties.stacksTo(1)); }
    private static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    public static boolean filled(ItemStack stack) {
        var tag = data(stack);
        return tag.getStringOr("DragonHornEntityID", "").equals("iceandfire:fire_dragon") && !tag.getCompoundOrEmpty("EntityTag").isEmpty();
    }

    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        // Creative Player.interactOn passes a copy; storage must update the real held horn.
        stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !stack.is(this) || stack.getCount() != 1
                || !(target instanceof FireDragon dragon) || (!dragon.isAlive() || dragon.corpse()) || !dragon.isOwnedBy(player)
                || dragon.level() != player.level() || player.isSpectator() || dragon.isPassenger() || dragon.isVehicle()
                || dragon.shoulderCarrier() != null) return InteractionResult.FAIL;
        var tag = data(stack);
        // Never overwrite an occupied or unsupported horn, even if its entity data is malformed.
        if (tag.contains("DragonHornEntityID") || tag.contains("EntityTag")) return InteractionResult.FAIL;
        if (player.level() instanceof ServerLevel server) {
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
            if (!dragon.save(output)) return InteractionResult.FAIL;
            tag.put("EntityTag", output.buildResult());
            tag.putString("DragonHornEntityID", "iceandfire:fire_dragon");
            if (dragon.getCustomName() != null) tag.putString("DragonName", dragon.getCustomName().getString()); else tag.remove("DragonName");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(true), List.of(), List.of()));
            dragon.discard();
            server.playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, net.minecraft.sounds.SoundSource.NEUTRAL, 3, 0.75F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        if (!filled(stack)) return InteractionResult.PASS;
        if (context.getClickedFace() != Direction.UP || stack.getCount() != 1) return InteractionResult.FAIL;
        var player = context.getPlayer();
        if (player != null && (player.isSpectator() || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack))) return InteractionResult.FAIL;
        if (!(context.getLevel() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var tag = data(stack);
        var saved = tag.getCompoundOrEmpty("EntityTag");
        if (!saved.getStringOr("id", "").equals("iceandfire:fire_dragon")
                || saved.read(net.minecraft.world.entity.Entity.TAG_UUID, net.minecraft.core.UUIDUtil.CODEC).isEmpty()) return InteractionResult.FAIL;
        var dragon = IafContent.FIRE_DRAGON.create(server, EntitySpawnReason.LOAD);
        if (dragon == null) return InteractionResult.FAIL;
        dragon.load(TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), saved));
        if ((!dragon.isAlive() || dragon.corpse())) return InteractionResult.FAIL;
        for (var level : server.getServer().getAllLevels()) if (level.getEntity(dragon.getUUID()) != null) return InteractionResult.FAIL;
        dragon.setPos(context.getClickedPos().getX() + 0.5, context.getClickedPos().getY() + 1, context.getClickedPos().getZ() + 0.5);
        dragon.setYRot(180 + context.getHorizontalDirection().toYRot());
        dragon.setXRot(0);
        dragon.refreshDimensions();
        var box = dragon.getBoundingBox();
        if (box.minY < server.getMinY() || box.maxY > server.getMaxY() || !server.getWorldBorder().isWithinBounds(box)) return InteractionResult.FAIL;
        for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++)
            for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++)
                if (!server.hasChunkAt(new net.minecraft.core.BlockPos(x << 4, context.getClickedPos().getY(), z << 4))) return InteractionResult.FAIL;
        if (!server.noCollision(dragon, box) || !server.addFreshEntity(dragon)) return InteractionResult.FAIL;
        tag.remove("DragonHornEntityID"); tag.remove("EntityTag"); tag.remove("EntityUUID"); tag.remove("DragonName");
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        return InteractionResult.SUCCESS;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!filled(stack)) return;
        var tag = data(stack); var dragon = tag.getCompoundOrEmpty("EntityTag");
        tooltip.accept(Component.translatable("entity.iceandfire.fire_dragon").withStyle(ChatFormatting.DARK_RED));
        tooltip.accept((tag.contains("DragonName") ? Component.literal(tag.getStringOr("DragonName", ""))
                : Component.translatable("dragon.unnamed")).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(dragon.getBooleanOr("Gender", false) ? "gui.iceandfire.male" : "gui.iceandfire.female").withStyle(ChatFormatting.GRAY));
        int days = Math.max(0, dragon.getIntOr("AgeTicks", 0)) / DragonGrowth.TICKS_PER_DAY;
        tooltip.accept(Component.translatable("gui.iceandfire.stage", DragonGrowth.stage(days), days).withStyle(ChatFormatting.GRAY));
    }
}
