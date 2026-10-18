package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.dragon.DragonFireCharge;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.Blocks;

/** The original RenderDragonFireCharge renders a one-block magma cube (LGPL-3.0). */
final class DragonFireChargeRenderer extends EntityRenderer<DragonFireCharge, FallingBlockRenderState> {
    DragonFireChargeRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public FallingBlockRenderState createRenderState() { return new FallingBlockRenderState(); }

    @Override public void extractRenderState(DragonFireCharge charge, FallingBlockRenderState state, float partialTick) {
        super.extractRenderState(charge, state, partialTick);
        var moving = state.movingBlockRenderState;
        moving.blockState = Blocks.MAGMA_BLOCK.defaultBlockState();
        moving.blockPos = charge.blockPosition();
        moving.randomSeedPos = charge.blockPosition();
        if (charge.level() instanceof ClientLevel level) {
            moving.biome = level.getBiome(moving.blockPos);
            moving.cardinalLighting = level.cardinalLighting();
            moving.lightEngine = level.getLightEngine();
        }
    }

    @Override public void submit(FallingBlockRenderState state, PoseStack stack,
                                 SubmitNodeCollector collector, CameraRenderState camera) {
        stack.pushPose();
        stack.translate(-0.5, 0, -0.5);
        collector.submitMovingBlock(stack, state.movingBlockRenderState, state.outlineColor);
        stack.popPose();
        super.submit(state, stack, collector, camera);
    }
}
