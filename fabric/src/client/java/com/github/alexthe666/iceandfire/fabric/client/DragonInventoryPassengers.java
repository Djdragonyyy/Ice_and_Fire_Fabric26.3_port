package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Inventory-only equivalent of LayerDragonRider; live passengers are never changed. */
public final class DragonInventoryPassengers {
    record Passenger(EntityRenderState state, boolean prey, float scale, float width, float height,
                     float yaw, boolean upright, boolean horse) {}
    private DragonInventoryPassengers() {}

    public static void capture(FireDragon dragon, FireDragonRenderer.State state) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        state.inventoryDragonScale = Math.max(0.0001F, state.scale);
        state.firstPersonPilot = false;
        state.flightPitch = dragon.flightPitch(1);
        var passengers = new java.util.ArrayList<Passenger>();
        for (var passenger : dragon.getPassengers()) {
            boolean prey = dragon.grippedPrey() == passenger;
            if (!prey && dragon.getControllingPassenger() != passenger) continue;
            // Native extractEntity allocates a fresh state, independent of world rendering.
            var snapshot = dispatcher.extractEntity(passenger, 1);
            snapshot.shadowPieces.clear(); snapshot.outlineColor = 0; snapshot.passengerOffset = null;
            snapshot.nameTag = null; snapshot.nameTagAttachment = null;
            float scale = passenger instanceof LivingEntity living ? living.getScale() : 1;
            if (snapshot instanceof LivingEntityRenderState living) living.scale /= state.inventoryDragonScale;
            snapshot.boundingBoxWidth /= state.inventoryDragonScale; snapshot.boundingBoxHeight /= state.inventoryDragonScale;
            var renderer = dispatcher.getRenderer(passenger);
            Object model = renderer instanceof LivingEntityRenderer<?, ?, ?> living ? living.getModel() : null;
            boolean horse = model instanceof net.minecraft.client.model.animal.equine.HorseModel;
            boolean upright = (passenger.getBbHeight() > passenger.getBbWidth() || model instanceof net.minecraft.client.model.HumanoidModel)
                    && !(model instanceof net.minecraft.client.model.QuadrupedModel) && !horse;
            passengers.add(new Passenger(snapshot, prey, scale, passenger.getBbWidth(), passenger.getBbHeight(),
                    net.minecraft.util.Mth.rotLerp(1, passenger.yRotO, passenger.getYRot()), upright, horse));
        }
        state.inventoryPassengers = java.util.List.copyOf(passengers);
    }

    static void submit(FireDragonRenderer.State state, FireDragonPoseModel model, PoseStack stack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.inventoryPassengers.isEmpty()) return;
        model.setupAnim(state);
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        for (var passenger : state.inventoryPassengers) {
            stack.pushPose();
            try {
                if (passenger.prey()) {
                    var transform = DragonPreyAttachment.transform(model, state, passenger.width(), passenger.height(),
                            passenger.yaw(), passenger.upright(), passenger.horse(), state.inventoryDragonScale);
                    var offset = transform.getTranslation(new Vector3f()).div(state.inventoryDragonScale);
                    stack.translate(offset.x, offset.y, offset.z);
                    ((DragonPreyRenderData) passenger.state()).iceandfire$setPreyRotation(transform.getNormalizedRotation(new org.joml.Quaternionf()));
                } else {
                    var position = FireDragonRenderer.riderRenderPosition(state, model.riderAnchor(), passenger.scale() / state.inventoryDragonScale);
                    stack.translate(position.x - state.x, position.y - state.y, position.z - state.z);
                    if (passenger.state() instanceof LivingEntityRenderState living) {
                        living.bodyRot = state.bodyRot; living.yRot = 0; living.xRot = 0;
                    }
                }
                dispatcher.getRenderer(passenger.state()).submit(passenger.state(), stack, collector, camera);
            } finally { stack.popPose(); }
        }
    }
}
