package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonGrowth;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;

/** Uses the original Tabula geometry, poses and stage/variant textures. */
public final class FireDragonRenderer extends MobRenderer<FireDragon, FireDragonRenderer.State, EntityModel<FireDragonRenderer.State>> {
    private static final String[] COLORS = {"red", "green", "bronze", "gray"};

    public static final class State extends LivingEntityRenderState {
        int variant;
        int stage;
        boolean flying;
        boolean male;
        int walkCycle;
        int flightCycle;
        float flyProgress;
        float hoverProgress;
        float sitProgress;
        float partialTick;
        float flightPitch;
        int attack;
        float attackTick;
        float breathProgress;
        float shoulderProgress;
        float sleepProgress;
        boolean sleeping;
        boolean blinking;
        boolean firstPersonPilot;
        int armor;
        float bannerDragonScale;
        final net.minecraft.client.renderer.item.ItemStackRenderState banner = new net.minecraft.client.renderer.item.ItemStackRenderState();
    }

    public FireDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new FireDragonPoseModel(context.getResourceManager()), 1F);
        addLayer(new DragonEyesLayer(this, new FireDragonPoseModel(context.getResourceManager(), true)));
        addLayer(new DragonArmorLayer(this));
        addLayer(new DragonBannerLayer(this));
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(FireDragon dragon, State state, float partialTick) {
        super.extractRenderState(dragon, state, partialTick);
        state.variant = dragon.variant();
        state.armor = dragon.armorParts();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(state.banner, dragon.dragonBanner(),
                net.minecraft.world.item.ItemDisplayContext.NONE, dragon);
        state.stage = dragon.stage();
        state.flying = dragon.flying();
        state.male = dragon.male();
        state.walkCycle = dragon.walkCycle;
        state.flightCycle = dragon.flightCycle;
        state.flyProgress = dragon.prevFlyProgress + (dragon.flyProgress - dragon.prevFlyProgress) * partialTick;
        state.hoverProgress = dragon.prevHoverProgress + (dragon.hoverProgress - dragon.prevHoverProgress) * partialTick;
        state.sitProgress = dragon.prevSitProgress + (dragon.sitProgress - dragon.prevSitProgress) * partialTick;
        state.partialTick = partialTick;
        state.attack = dragon.attackAnimation();
        state.attackTick = dragon.attackAnimationTick() + partialTick;
        state.breathProgress = dragon.prevBreathProgress + (dragon.breathProgress - dragon.prevBreathProgress) * partialTick;
        state.shoulderProgress = dragon.prevShoulderProgress + (dragon.shoulderProgress - dragon.prevShoulderProgress) * partialTick;
        state.sleeping = dragon.dragonSleeping();
        state.blinking = dragon.isBlinking();
        state.sleepProgress = dragon.prevSleepProgress + (dragon.sleepProgress - dragon.prevSleepProgress) * partialTick;
        var player = dragon.shoulderCarrier();
        if (player != null) {
            float yaw = net.minecraft.util.Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            Vec3 carrierPosition = player.getPosition(partialTick);
            if (player.getVehicle() instanceof FireDragon mount && mount.getControllingPassenger() == player
                    && Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(mount) instanceof FireDragonRenderer renderer) {
                carrierPosition = renderer.riderRenderPosition(mount, partialTick, player.getScale());
            }
            Vec3 position = carrierPosition.add(
                    com.github.alexthe666.iceandfire.fabric.dragon.DragonShoulderLayout.offset(
                            dragon.shoulderSlot(), yaw, player.isShiftKeyDown(), player.getScale()));
            state.x = position.x; state.y = position.y; state.z = position.z;
            state.passengerOffset = null;
            state.walkAnimationSpeed = 0;
            state.bodyRot = net.minecraft.util.Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
            state.yRot = state.xRot = 0;
        }
        var client = Minecraft.getInstance();
        boolean firstPersonRider = client.player != null && client.getCameraEntity() == client.player
                && dragon.getControllingPassenger() == client.player && client.options.getCameraType().isFirstPerson();
        state.firstPersonPilot = firstPersonRider;
        state.flightPitch = viewPitch(dragon.flightPitch(partialTick), firstPersonRider);
        // The legacy renderer used renderSize / 3; the entity's bounding-box
        // scale is separately capped at seven in the original implementation.
        state.scale = DragonGrowth.renderSize(dragon.ageDays()) / 3F;
        // Inventory preview normalizes state.scale after extraction; retain the real scale for the item ratio.
        state.bannerDragonScale = state.scale;
    }

    static float viewPitch(float flightPitch, boolean firstPersonRider) {
        return firstPersonRider ? 0 : flightPitch;
    }

    @Override public Identifier getTextureLocation(State state) {
        return texture(state, state.sleeping || state.blinking ? "_sleeping" : "");
    }
    static Identifier texture(State state, String suffix) {
        return Identifier.fromNamespaceAndPath("iceandfire", "textures/models/firedragon/" + COLORS[state.variant] + "_" + state.stage + suffix + ".png");
    }

    @Override protected void scale(State state, PoseStack stack) {
        // Same placement as RenderDragonBase.scale: after yaw/axis flip, before model translation.
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(state.flightPitch)));
    }

    /** Visual rider follows the animated neck; physical seat retains the original POV offsets. */
    public Vec3 riderRenderPosition(FireDragon dragon, float partialTick, float riderScale) {
        State state = new State();
        extractRenderState(dragon, state, partialTick);
        FireDragonPoseModel pose = (FireDragonPoseModel) getModel();
        pose.setupAnim(state);
        var anchor = pose.riderAnchor();
        return riderRenderPosition(state, anchor, riderScale);
    }

    public record PreyRenderPose(Vec3 position, org.joml.Quaternionf rotation) {}
    public PreyRenderPose preyRenderPose(FireDragon dragon, net.minecraft.world.entity.Mob prey, float partialTick) {
        State state = new State(); extractRenderState(dragon, state, partialTick);
        var model = (FireDragonPoseModel) getModel(); model.setupAnim(state);
        var preyRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(prey);
        Object preyModel = preyRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?, ?> living ? living.getModel() : null;
        boolean horse = preyModel instanceof net.minecraft.client.model.animal.equine.HorseModel;
        boolean upright = (prey.getBbHeight() > prey.getBbWidth() || preyModel instanceof net.minecraft.client.model.HumanoidModel)
                && !(preyModel instanceof net.minecraft.client.model.QuadrupedModel) && !horse;
        float yaw = net.minecraft.util.Mth.rotLerp(partialTick, prey.yRotO, prey.getYRot());
        var matrix = DragonPreyAttachment.transform(model, state, prey.getBbWidth(), prey.getBbHeight(), yaw, upright, horse);
        var offset = matrix.getTranslation(new org.joml.Vector3f());
        return new PreyRenderPose(new Vec3(state.x + offset.x, state.y + offset.y, state.z + offset.z),
                matrix.getNormalizedRotation(new org.joml.Quaternionf()));
    }

    static Vec3 riderRenderPosition(State state, org.joml.Vector3f anchor) {
        return riderRenderPosition(state, anchor, 1);
    }

    static Vec3 riderRenderPosition(State state, org.joml.Vector3f anchor, float riderScale) {
        PoseStack stack = new PoseStack();
        stack.scale(state.scale, state.scale, state.scale);
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees(180 - state.bodyRot)));
        stack.scale(-1, -1, 1);
        stack.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(state.flightPitch)));
        stack.translate(0, -1.501F, 0);
        var surface = stack.last().pose().transformPosition(new org.joml.Vector3f(anchor));
        // The player's seated hip is 12 model pixels above its feet. This offset must
        // follow the PLAYER scale, while the surface follows the uncapped DRAGON render scale.
        return new Vec3(state.x + surface.x, state.y + surface.y - 0.75F * riderScale, state.z + surface.z);
    }
}
