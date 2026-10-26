package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

/** Optional charge explosion from IafDragonDestructionManager, using the native explosion pipeline. */
final class DragonFireExplosion {
    static void trigger(ServerLevel level, FireDragon dragon, Entity charge, Vec3 point) {
        float power = Math.min(2, dragon.stage() - 2);
        if (!DragonCombatConfig.current.explosiveDragonBreath || power <= 0 || dragon.corpse() || dragon.isRemoved()) return;
        var calculator = new ExplosionDamageCalculator() {
            private boolean protectedEntity(Entity entity) {
                // The body receives area damage once; its ten proxy hitboxes must never multiply it.
                return entity instanceof DragonPart || DragonFireCombat.friendly(dragon, entity);
            }
            @Override public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                return !protectedEntity(entity) && super.shouldDamageEntity(explosion, entity);
            }
            @Override public float getKnockbackMultiplier(Entity entity) {
                return protectedEntity(entity) ? 0 : super.getKnockbackMultiplier(entity);
            }
            @Override public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter world,
                    BlockPos pos, BlockState state, FluidState fluid) {
                return state.is(DragonFireTerrain.BLACKLIST) || state.getDestroySpeed(world, pos) < 0
                        ? Optional.of(Float.POSITIVE_INFINITY) : super.getBlockExplosionResistance(explosion, world, pos, state, fluid);
            }
            @Override public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float remainingPower) {
                return !state.is(DragonFireTerrain.BLACKLIST) && state.getDestroySpeed(world, pos) >= 0
                        && super.shouldBlockExplode(explosion, world, pos, state, remainingPower);
            }
        };
        level.explode(dragon, DragonFireCombat.fireDamage(dragon, charge), calculator, point, power, false,
                DragonFireTerrain.allowed(level, dragon) ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
    }
    private DragonFireExplosion() { }
}
