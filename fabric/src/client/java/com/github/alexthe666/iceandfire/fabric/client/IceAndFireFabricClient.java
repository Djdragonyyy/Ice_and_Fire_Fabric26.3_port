package com.github.alexthe666.iceandfire.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class IceAndFireFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(IafContent.FIRE_DRAGON, FireDragonRenderer::new);
        EntityRendererRegistry.register(IafContent.DRAGON_EGG, FireDragonEggRenderer::new);
        EntityRendererRegistry.register(IafContent.FIRE_DRAGON_CHARGE, DragonFireChargeRenderer::new);
        EntityRendererRegistry.register(IafContent.DRAGON_SKULL, DragonSkullRenderer::new);
        EntityRendererRegistry.register(IafContent.DRAGON_MULTIPART, net.minecraft.client.renderer.entity.NoopRenderer::new);
        DragonKeyMappings.initialize();
        DragonFlameParticle.initialize();
        net.minecraft.client.gui.screens.MenuScreens.register(IafContent.DRAGON_MENU, DragonScreen::new);
    }
}
