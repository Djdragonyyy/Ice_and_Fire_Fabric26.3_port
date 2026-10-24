package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Fire-egg incubation and ownership migrated from EntityDragonEgg. */
public final class FireDragonEgg extends Mob {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FireDragonEgg.class, EntityDataSerializers.INT);
    private EntityReference<LivingEntity> owner;
    private int incubationTicks;

    public FireDragonEgg(EntityType<? extends FireDragonEgg> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    public int variant() { return entityData.get(VARIANT); }
    public void setVariant(int value) { entityData.set(VARIANT, Math.clamp(value, 0, 3)); }
    public void setOwner(LivingEntity value) { owner = EntityReference.of(value); }

    @Override public boolean hurtServer(ServerLevel server, net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || isRemoved()) return false;
        if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))
            spawnAtLocation(server, new net.minecraft.world.item.ItemStack(IafContent.FIRE_EGGS[variant()]));
        remove(RemovalReason.KILLED);
        return true;
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        setAirSupply(200);
        if (server.getBlockState(blockPosition()).is(BlockTags.FIRE)) incubationTicks++;
        if (!DragonGrowth.shouldHatch(incubationTicks, DragonGrowth.DEFAULT_EGG_TIME)) return;
        FireDragon dragon = IafContent.FIRE_DRAGON.create(server, EntitySpawnReason.BREEDING);
        if (dragon == null) return;
        dragon.setVariant(variant());
        dragon.setMale(random.nextBoolean());
        dragon.setPos(getX(), getY() + 1, getZ());
        dragon.setCustomName(getCustomName());
        dragon.setHunger(50);
        dragon.setTame(true, false);
        dragon.setOwnerReference(owner);
        if (server.addFreshEntity(dragon)) {
            server.setBlockAndUpdate(blockPosition(), Blocks.AIR.defaultBlockState());
            discard();
        }
    }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Color", variant());
        output.putInt("DragonAge", incubationTicks);
        EntityReference.store(owner, output, "Owner");
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("Color", 0));
        incubationTicks = Math.max(0, input.getIntOr("DragonAge", 0));
        owner = EntityReference.read(input, "Owner");
    }
}
