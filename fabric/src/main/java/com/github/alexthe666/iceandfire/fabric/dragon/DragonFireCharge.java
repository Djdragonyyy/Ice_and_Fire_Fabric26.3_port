package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Fire projectile from EntityDragonCharge/EntityDragonFireCharge (LGPL-3.0). */
public final class DragonFireCharge extends AbstractHurtingProjectile {
    public DragonFireCharge(EntityType<? extends DragonFireCharge> type, Level level) {
        super(type, level);
        accelerationPower = 0.07;
    }

    @Override protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(target instanceof Projectile)
                && getOwner() instanceof FireDragon dragon && !DragonFireCombat.friendly(dragon, target);
    }

    @Override protected void onHit(HitResult hit) {
        if (!(level() instanceof ServerLevel server)) return;
        if (getOwner() instanceof FireDragon dragon) {
            if (hit instanceof EntityHitResult entityHit && !DragonFireCombat.friendly(dragon, entityHit.getEntity())) {
                entityHit.getEntity().hurtServer(server, DragonFireCombat.fireDamage(dragon, this),
                        (float) com.github.alexthe666.iceandfire.fabric.DragonCombatConfig.current.dragonAttackDamageFire * dragon.stage());
            }
            DragonFireCombat.chargeImpact(server, dragon, this, hit.getLocation());
        }
        discard();
    }

    @Override public void tick() {
        // Do not keep orphaned projectiles alive or force-load chunks.
        if (!level().isClientSide() && (isInWater() || !(getOwner() instanceof FireDragon dragon)
                || !dragon.isAlive() || !level().hasChunkAt(blockPosition()))) { discard(); return; }
        super.tick();
    }

    @Override public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) { return false; }
}
