package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Original fire summoning crystal binding, migrated to native components and per-request server state. */
public final class SummoningCrystalItem extends Item {
    public SummoningCrystalItem(Properties properties) { super(properties.stacksTo(1)); }
    static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    public static Optional<UUID> dragonUuid(ItemStack stack) { return data(stack).getCompoundOrEmpty("Dragon").read("DragonUUID", UUIDUtil.CODEC); }
    static boolean owns(FireDragon dragon, Player player) { return !dragon.corpse() && dragon.isTame() && dragon.getOwnerReference() != null && dragon.getOwnerReference().matches(player); }
    @Override public InteractionResult interactLivingEntity(ItemStack ignored, Player player, LivingEntity target, InteractionHand hand) {
        var stack = player.getItemInHand(hand); // Native creative interactions pass a temporary copy.
        if (!stack.is(this) || stack.getCount() != 1 || player.isSpectator() || !(target instanceof FireDragon dragon)
                || !dragon.isAlive() || dragon.level() != player.level() || !owns(dragon, player)) return InteractionResult.FAIL;
        var tag = data(stack);
        if (tag.contains("Dragon")) return InteractionResult.FAIL;
        if (player.level() instanceof ServerLevel server) {
            var bound = new CompoundTag(); bound.store("DragonUUID", UUIDUtil.CODEC, dragon.getUUID());
            if (dragon.getCustomName() != null) bound.putString("CustomName", dragon.getCustomName().getString());
            tag.put("Dragon", bound); stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(true), List.of(), List.of()));
            dragon.setCrystalBound(true); DragonPositions.get(server).put(dragon.getUUID(), dragon.blockPosition());
            server.playSound(null, dragon.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1, 1);
        }
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand(); var uuid = dragonUuid(stack); var player = context.getPlayer();
        if (uuid.isEmpty()) return data(stack).contains("Dragon") ? InteractionResult.FAIL : InteractionResult.PASS;
        if (player == null || player.isSpectator() || stack.getCount() != 1
                || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack)) return InteractionResult.FAIL;
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (level.dimension() != Level.OVERWORLD) { error(player); return InteractionResult.FAIL; }
        var target = Vec3.atCenterOf(context.getClickedPos().relative(context.getClickedFace()));
        return CrystalSummons.request(level, player, context.getHand(), stack, uuid.get(), target, player.getYRot());
    }
    static void clear(ItemStack stack) {
        var tag = data(stack); tag.remove("Dragon");
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.remove(DataComponents.CUSTOM_MODEL_DATA);
    }
    static void error(Player player) { player.sendOverlayMessage(Component.translatable("message.iceandfire.noDragonTeleport")); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        var bound = data(stack).getCompoundOrEmpty("Dragon");
        if (dragonUuid(stack).isPresent()) {
            Component name = bound.getString("CustomName").<Component>map(Component::literal).orElseGet(() -> Component.translatable("entity.iceandfire.fire_dragon"));
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.bound", name).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.desc_0").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.desc_1").withStyle(ChatFormatting.GRAY));
        }
    }
}
