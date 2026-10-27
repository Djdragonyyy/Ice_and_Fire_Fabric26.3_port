package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.forge.DragonForgeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original fire forge GUI, native slot/data synchronization. */
final class DragonForgeScreen extends AbstractContainerScreen<DragonForgeMenu> {
    DragonForgeScreen(DragonForgeMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 176, 166); }
    @Override public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        var texture = IafContent.id("textures/gui/dragonforge_fire.png");
        g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int progress = (int) Math.clamp((long) menu.progress() * 126 / menu.duration(), 0, 126);
        if (progress > 0) g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 12, topPos + 23, 0, 166, progress, 38, 256, 256);
        super.extractContents(g, mouseX, mouseY, partialTick);
    }
}
