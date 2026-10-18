package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original GuiDragon texture, stats and layout adapted to the extraction renderer (LGPL-3.0). */
final class DragonScreen extends AbstractContainerScreen<DragonMenu> {
    DragonScreen(DragonMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 176, 214); }
    @Override public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, IafContent.id("textures/gui/dragon.png"),
                leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        var dragon = menu.dragon;
        if (dragon != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + 25, topPos + 5,
                    leftPos + 150, topPos + 70, Math.max(1, (int) (23 / dragon.getScale())),
                    0.0625F, mouseX, mouseY, dragon);
            graphics.centeredText(font, dragon.getDisplayName(), leftPos + 88, topPos + 75, 0xffffffff);
            graphics.centeredText(font, Component.translatable("gui.iceandfire.health", (int) dragon.getHealth(), (int) dragon.getMaxHealth()), leftPos + 88, topPos + 84, 0xffffffff);
            graphics.centeredText(font, Component.translatable(dragon.male() ? "gui.iceandfire.male" : "gui.iceandfire.female"), leftPos + 88, topPos + 93, 0xffffffff);
            graphics.centeredText(font, Component.translatable("gui.iceandfire.hunger", dragon.hunger()), leftPos + 88, topPos + 102, 0xffffffff);
            graphics.centeredText(font, Component.translatable("gui.iceandfire.stage", dragon.stage(), dragon.ageDays()), leftPos + 88, topPos + 111, 0xffffffff);
            graphics.centeredText(font, dragon.getOwner() != null ? dragon.getOwner().getDisplayName() : Component.translatable("gui.iceandfire.untamed"), leftPos + 88, topPos + 120, 0xffffffff);
        }
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}
}
