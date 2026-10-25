package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import java.util.function.Consumer;

/** Fire variant of original ItemDragonSkull; legacy keys retained in CUSTOM_DATA. */
public final class DragonSkullItem extends Item {
    public DragonSkullItem(Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.CUSTOM_DATA, encoded(DragonSkullData.DEFAULT)));
    }
    private static CustomData encoded(DragonSkullData data) {
        var tag = new CompoundTag();
        tag.putInt("Stage", data.stage()); tag.putInt("DragonAge", data.ageDays()); tag.putInt("DragonType", 0);
        return CustomData.of(tag);
    }
    public static DragonSkullData data(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return new DragonSkullData(tag.getIntOr("Stage", 4), tag.getIntOr("DragonAge", 75));
    }
    public static ItemStack stack(DragonSkullData data) {
        var stack = new ItemStack(IafContent.DRAGON_SKULL_FIRE);
        stack.set(DataComponents.CUSTOM_DATA, encoded(data)); return stack;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer(); var stack = context.getItemInHand();
        if (player == null || !stack.is(this) || stack.getCount() != 1 || player.isSpectator()
                || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack)) return InteractionResult.FAIL;
        if (!(context.getLevel() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var skull = IafContent.DRAGON_SKULL.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (skull == null) return InteractionResult.FAIL;
        var pos = context.getClickedPos().relative(context.getClickedFace());
        skull.setSkullData(data(stack)); skull.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        skull.setDragonYaw(context.getClickedFace() == Direction.UP ? player.getYRot() : player.getDirection().toYRot());
        skull.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        if (!server.getWorldBorder().isWithinBounds(skull.getBoundingBox()) || pos.getY() < server.getMinY()
                || skull.getBoundingBox().maxY > server.getMaxY() || !server.noCollision(skull, skull.getBoundingBox())
                || !server.addFreshEntity(skull)) return InteractionResult.FAIL;
        stack.consume(1, player); return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                          Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("dragon.fire").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("dragon.stage").append(" " + data(stack).stage()).withStyle(ChatFormatting.GRAY));
    }
}
