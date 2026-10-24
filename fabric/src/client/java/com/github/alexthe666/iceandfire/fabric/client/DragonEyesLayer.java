package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Original HeadFront-only eye overlay, before armor; disabled while sleeping/dead. */
final class DragonEyesLayer extends RenderLayer<FireDragonRenderer.State, EntityModel<FireDragonRenderer.State>> {
    private final FireDragonPoseModel eyes;
    DragonEyesLayer(FireDragonRenderer parent, FireDragonPoseModel eyes) { super(parent); this.eyes = eyes; }
    @Override public void submit(PoseStack stack, SubmitNodeCollector collector, int light,
                                 FireDragonRenderer.State state, float yRot, float xRot) {
        if (state.isInvisible || state.sleeping || state.deathTime > 0) return;
        collector.order(1).submitModel(eyes, state, stack, RenderTypes.eyes(FireDragonRenderer.texture(state, "_eyes")),
                15728880, OverlayTexture.NO_OVERLAY, state.outlineColor);
    }
}
