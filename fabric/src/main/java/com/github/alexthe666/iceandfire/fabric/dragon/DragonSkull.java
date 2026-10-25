package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Original decorative skull entity, restricted to the fire dragon in stage one. */
public final class DragonSkull extends Mob {
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(DragonSkull.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(DragonSkull.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(DragonSkull.class, EntityDataSerializers.FLOAT);
    public DragonSkull(EntityType<? extends DragonSkull> type, Level level) {
        super(type, level); setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, 0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(STAGE, 4); builder.define(AGE, 75); builder.define(YAW, 0F);
    }
    public DragonSkullData skullData() { return new DragonSkullData(entityData.get(STAGE), entityData.get(AGE)); }
    public void setSkullData(DragonSkullData data) { entityData.set(STAGE, data.stage()); entityData.set(AGE, data.ageDays()); }
    public float dragonYaw() { return entityData.get(YAW); }
    public void setDragonYaw(float value) { entityData.set(YAW, Float.isFinite(value) ? net.minecraft.util.Mth.wrapDegrees(value) : 0); }
    public boolean onWall() { return level().isEmptyBlock(blockPosition().below()); }
    @Override public boolean isNoAi() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) { }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override public boolean hurtServer(ServerLevel server, DamageSource source, float damage) {
        if (isRemoved() || server != level()) return false;
        var item = DragonSkullItem.stack(skullData());
        if (getCustomName() != null) item.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, getCustomName());
        discard(); spawnAtLocation(server, item); return true;
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isSpectator() || !player.mayBuild()) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            if (level() instanceof ServerLevel) setDragonYaw(player.getYRot());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out); var data = skullData();
        out.putInt("Type", 0); out.putInt("Stage", data.stage()); out.putInt("DragonAge", data.ageDays()); out.putFloat("DragonYaw", dragonYaw());
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setSkullData(new DragonSkullData(in.getIntOr("Stage", 4), in.getIntOr("DragonAge", 75)));
        setDragonYaw(in.getFloatOr("DragonYaw", 0));
    }
}
