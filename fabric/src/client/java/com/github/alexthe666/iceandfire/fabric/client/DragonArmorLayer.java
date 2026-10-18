package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Original per-part dragon armor textures, on the same animated geometry. */
final class DragonArmorLayer extends RenderLayer<FireDragonRenderer.State, EntityModel<FireDragonRenderer.State>> {
    private static final String[] PARTS = {"head", "neck", "body", "tail"};
    DragonArmorLayer(FireDragonRenderer parent) { super(parent); }
    @Override public void submit(PoseStack stack, SubmitNodeCollector collector, int light,
                                 FireDragonRenderer.State state, float yRot, float xRot) {
        if (state.isInvisible) return;
        for (int slot = 0; slot < 4; slot++) {
            int armor = (state.armor >> (slot * 4)) & 15;
            if (armor < 1 || armor > 8) continue;
            collector.submitModel(getParentModel(), state, stack,
                    IafContent.id("textures/models/firedragon/armor_" + PARTS[slot] + "_" + armor + ".png"),
                    light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
    }
}
