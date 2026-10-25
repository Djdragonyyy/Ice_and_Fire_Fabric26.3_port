package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;

/** Server combat adapted from EntityFireDragon and IafDragonDestructionManager (LGPL-3.0).
 * Charred-block conversions and DragonForge interactions are not migrated here yet.
 */
public final class DragonFireCombat {
    public static final ResourceKey<DamageType> FIRE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, IafContent.id("dragon_fire"));
    private DragonFireCombat() {}

    public static boolean friendly(FireDragon dragon, Entity target) {
        return target.isPassengerOfSameVehicle(dragon) || protectedTarget(dragon, target);
    }

    static boolean protectedTarget(FireDragon dragon, Entity target) {
        if (target instanceof DragonSkull) return true; // Decorative remains, never living combat prey.
        if (target == dragon || dragon.isAlliedTo(target)) return true;
        var owner = dragon.getOwnerReference();
        if (target instanceof LivingEntity living && dragon.isOwnedBy(living)) return true;
        if (owner != null && target instanceof OwnableEntity tame && tame.getOwnerReference() != null
                && owner.getUUID().equals(tame.getOwnerReference().getUUID())) return true;
        return target instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    public static DamageSource fireDamage(FireDragon dragon, Entity direct) {
        return new DamageSource(dragon.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(FIRE_DAMAGE),
                direct, dragon.getControllingPassenger() != null ? dragon.getControllingPassenger() : dragon);
    }

    public static Vec3 clip(FireDragon dragon, Vec3 from, Vec3 to) {
        return dragon.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, dragon)).getLocation();
    }

    public static Vec3 aim(FireDragon dragon, LivingEntity rider, double reach) {
        Vec3 start = rider.getEyePosition();
        return clip(dragon, start, start.add(rider.getLookAngle().scale(reach)));
    }

    public static LivingEntity biteTarget(FireDragon dragon, LivingEntity rider) {
        double range = dragon.stage() + dragon.getBbWidth();
        Vec3 start = rider.getEyePosition();
        Vec3 end = aim(dragon, rider, range);
        LivingEntity nearest = null;
        double distance = start.distanceToSqr(end);
        for (LivingEntity target : dragon.level().getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(2),
                target -> target.isAlive() && target.isPickable() && !friendly(dragon, target))) {
            var hit = DragonCombatGeometry.intersectionDistance(start, end, target.getBoundingBox().inflate(target.getPickRadius() + 2));
            if (hit.isPresent() && hit.getAsDouble() <= distance && dragon.hasLineOfSight(target)) {
                nearest = target;
                distance = hit.getAsDouble();
            }
        }
        return nearest;
    }

    public static void breath(ServerLevel level, FireDragon dragon, Vec3 head, Vec3 aim, float progress) {
        Vec3 requestedEnd = head.lerp(aim, progress);
        Vec3 end = clip(dragon, head, requestedEnd);
        boolean obstructed = end.distanceToSqr(requestedEnd) > 0.01;
        DragonBreathPayload.send(dragon, head, end, progress);
        BlockPos center = BlockPos.containing(end);
        // Original damage radius and fire duration; damage happens when the stream reaches its target.
        if (progress >= 1 || obstructed) {
            int stage = dragon.stage();
            double radius = stage <= 3 ? 3.5 : 2.5 + (stage == 4 ? 2.498 : 3.497) * 1.2;
            Vec3 impact = end.add(head.subtract(end).normalize().scale(0.01));
            damageArea(level, dragon, impact, radius, (float) DragonCombatConfig.current.dragonAttackDamageFire * stage, 5 + stage * 5, dragon);
            DragonFireTerrain.breath(level, center, dragon);
        }
    }

    public static void chargeImpact(ServerLevel level, FireDragon dragon, Entity charge, Vec3 point) {
        int stage = dragon.stage();
        double radius = stage <= 3 ? 2 : (stage == 4 ? 3 : 4);
        damageArea(level, dragon, point, radius, Math.max(1, stage - 1) * 2F, 15, charge);
        DragonFireTerrain.charge(level, BlockPos.containing(point), dragon);
    }

    private static void damageArea(ServerLevel level, FireDragon dragon, Vec3 center, double radius,
                                   float damage, int seconds, Entity direct) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(radius), target -> target.isAlive() && !friendly(dragon, target))) {
            // Check from the impact as well as from the dragon: nearby walls must shield the victim.
            Vec3 eye = target.getEyePosition();
            if (dragon.hasLineOfSight(target) && clip(dragon, center, eye).distanceToSqr(eye) < 0.01) {
                if (target.hurtServer(level, fireDamage(dragon, direct), damage)) target.igniteForSeconds(seconds);
            }
        }
    }

}
