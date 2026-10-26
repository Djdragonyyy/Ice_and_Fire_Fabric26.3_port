package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Original dragon multipart hitboxes as transient, normally networked native entities. */
public final class DragonPart extends Entity {
    private static final EntityDataAccessor<Integer> PARENT = SynchedEntityData.defineId(DragonPart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PARENT_MOST = SynchedEntityData.defineId(DragonPart.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> PARENT_LEAST = SynchedEntityData.defineId(DragonPart.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> INDEX = SynchedEntityData.defineId(DragonPart.class, EntityDataSerializers.INT);
    private float lastScale = -1;

    public DragonPart(EntityType<? extends DragonPart> type, Level level) { super(type, level); setNoGravity(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PARENT, -1); builder.define(PARENT_MOST, 0L); builder.define(PARENT_LEAST, 0L); builder.define(INDEX, 0);
    }
    public int partIndex() { return Math.clamp(entityData.get(INDEX), 0, DragonPartLayout.PARTS.size() - 1); }
    public DragonPartLayout.Part specification() { return DragonPartLayout.PARTS.get(partIndex()); }
    public FireDragon parent() {
        if (!(level().getEntity(entityData.get(PARENT)) instanceof FireDragon dragon) || dragon.isRemoved()) return null;
        var uuid = dragon.getUUID();
        return uuid.getMostSignificantBits() == entityData.get(PARENT_MOST) && uuid.getLeastSignificantBits() == entityData.get(PARENT_LEAST) ? dragon : null;
    }
    void attach(FireDragon dragon, int index) {
        if (dragon.level() != level() || index < 0 || index >= DragonPartLayout.PARTS.size()) throw new IllegalArgumentException("Invalid multipart parent/index");
        entityData.set(PARENT_MOST, dragon.getUUID().getMostSignificantBits()); entityData.set(PARENT_LEAST, dragon.getUUID().getLeastSignificantBits());
        entityData.set(INDEX, index); entityData.set(PARENT, dragon.getId()); follow(dragon);
    }
    void follow(FireDragon dragon) {
        float scale = DragonGrowth.renderSize(dragon.ageDays()) / 3;
        if (scale != lastScale) { lastScale = scale; refreshDimensions(); }
        var spec = specification();
        float angle = (float) (dragon.yBodyRot * (Math.PI / 180) + (spec.yaw() + 90) * ((float) Math.PI / 180));
        setPos(dragon.getX() + spec.radius() * scale * Mth.cos(angle), dragon.getY() + spec.y() * scale,
                dragon.getZ() + spec.radius() * scale * Mth.sin(angle));
        setYRot(dragon.yBodyRot);
    }
    @Override public EntityDimensions getDimensions(Pose pose) {
        var dragon = parent();
        if (dragon == null) return super.getDimensions(pose);
        float scale = DragonGrowth.renderSize(dragon.ageDays()) / 3; var spec = specification();
        return EntityDimensions.scalable(spec.width() * scale, spec.height() * scale);
    }
    @Override public void tick() {
        super.tick();
        var dragon = parent();
        if (dragon != null && (level().isClientSide() || dragon.ownsPart(this))) follow(dragon);
        else if (!level().isClientSide() && tickCount > 20) discard();
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == PARENT || key == INDEX || key == PARENT_MOST || key == PARENT_LEAST) { lastScale = -1; refreshDimensions(); }
    }
    @Override public boolean hurtServer(ServerLevel server, DamageSource source, float damage) {
        var dragon = parent();
        if (server != level() || isRemoved() || dragon == null || !dragon.ownsPart(this) || !Float.isFinite(damage) || damage <= 0
                || isInvulnerableToBase(source) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.LAVA)
                || source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return false;
        float scaled = (float) Math.min(Float.MAX_VALUE, (double) damage * specification().damage());
        return dragon.hurtServer(server, source, scaled);
    }
    @Override public InteractionResult interact(Player player, InteractionHand hand, Vec3 localHit) {
        var dragon = parent();
        if (isRemoved() || dragon == null || !level().isClientSide() && !dragon.ownsPart(this)) return InteractionResult.PASS;
        // Run the complete native parent interaction, including item delegation (horn/crystal), once.
        return player.interactOn(dragon, hand, localHit.add(position().subtract(dragon.position())));
    }
    @Override public boolean isPickable() { return !isRemoved() && parent() != null; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeCollidedWith(Entity other) { return false; }
    @Override protected boolean couldAcceptPassenger() { return false; }
    @Override public boolean is(Entity other) { return this == other || parent() == other; }
    @Override public Entity getRootVehicle() { var dragon = parent(); return dragon == null ? super.getRootVehicle() : dragon.getRootVehicle(); }
    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void addAdditionalSaveData(ValueOutput out) { }
    @Override protected void readAdditionalSaveData(ValueInput in) { }
}
