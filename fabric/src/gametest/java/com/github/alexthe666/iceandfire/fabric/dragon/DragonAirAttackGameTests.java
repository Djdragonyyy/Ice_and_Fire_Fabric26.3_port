package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DragonAirAttackGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h) {
        for (int x = 1; x < 47; x++) for (int z = 1; z < 47; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < 25; i++) d.growOneDay();
        d.setPos(h.absoluteVec(new Vec3(8.5, 14, 24.5)));
        d.setOnGround(false);
        d.setAutonomousFlight(true, false);
        d.getRandom().setSeed(13);
        return d;
    }
    private static Mob victim(GameTestHelper h) {
        var v = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        v.setPos(h.absoluteVec(new Vec3(26.5, 2, 24.5)));
        v.setNoAi(true);
        v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300);
        v.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        v.setHealth(300);
        h.getLevel().addFreshEntity(v);
        return v;
    }
    private static void begin(GameTestHelper h, FireDragon d, Mob v, DragonFlightManager.AirAttack mode) {
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        d.flightManager.beginAirAttack(v, mode);
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 150, padding = 12, skyAccess = true)
    public void tackleActuallyDivesHitsAndReturnsToGroundCombat(GameTestHelper h) {
        var d = dragon(h);
        var v = victim(h);
        Vec3 start = d.position();
        begin(h, d, v, DragonFlightManager.AirAttack.TACKLE);
        boolean[] hit = {false};
        h.onEachTick(() -> { if (v.getHealth() <= 294) hit[0] = true; });
        h.runAfterDelay(115, () -> {
            require(d.position().subtract(start).horizontalDistance() > 5 && d.getY() < start.y - 4, "Tackle must dive toward the threat");
            require(hit[0], "Tackle applies original stage*3 damage");
            require(!d.flying() && d.onGround(), "Tackle impact returns to ground fighting");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 150, padding = 12, skyAccess = true)
    public void hoverBlastMovesToAltitudeAndReleasesRealProjectile(GameTestHelper h) {
        var d = dragon(h);
        var v = victim(h);
        Vec3 start = d.position();
        begin(h, d, v, DragonFlightManager.AirAttack.HOVER_BLAST);
        boolean[] projectile = {false};
        h.onEachTick(() -> {
            if (!h.getLevel().getEntitiesOfClass(DragonFireCharge.class, d.getBoundingBox().inflate(40), c -> c.getOwner() == d).isEmpty()) projectile[0] = true;
        });
        h.runAfterDelay(115, () -> {
            require(d.flying() && d.getY() > v.getY() + 5, "Hover blast stays above the threat");
            require(d.position().subtract(start).horizontalDistance() > 2, "Hover blast actually moves to its firing position");
            require(projectile[0] && v.getHealth() < 300, "Hover blast emits projectile and inflicts real damage");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 170, padding = 12, skyAccess = true)
    public void scorchStreamPassesOverTargetAndTurnsForReturn(GameTestHelper h) {
        var d = dragon(h);
        var v = victim(h);
        begin(h, d, v, DragonFlightManager.AirAttack.SCORCH_STREAM);
        boolean[] reversed = {false};
        h.onEachTick(() -> {
            Vec3 point = d.flightManager.target();
            if (point != null && point.x < v.getX()) reversed[0] = true;
        });
        h.runAfterDelay(140, () -> {
            require(d.flying() && reversed[0], "Scorch stream flies past threat and selects return side");
            require(v.getHealth() < 300, "Scorch stream deals server fire damage");
            d.setCommand(1);
        });
        h.runAfterDelay(155, () -> {
            require(d.getTarget() == null && d.breathProgress == 0, "Sit cancels aerial fire and target");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 60, padding = 12, skyAccess = true)
    public void landingCancelsAirChargeBeforeGroundBite(GameTestHelper h) {
        var d = dragon(h);
        var v = victim(h);
        begin(h, d, v, DragonFlightManager.AirAttack.HOVER_BLAST);
        boolean[] landed = {false};
        h.onEachTick(() -> {
            if (!landed[0] && d.attackAnimation() == DragonCombatState.CHARGE && d.attackAnimationTick() >= 5) {
                landed[0] = true;
                d.setAutonomousFlight(false, false);
                d.setPos(v.position().add(-3, 0, 0));
                d.setDeltaMovement(Vec3.ZERO);
                d.setOnGround(true);
                h.runAfterDelay(2, () -> {
                    require(d.attackAnimation() == DragonCombatState.BITE && d.attackAnimationTick() < 5,
                            "Landing discards aerial charge and starts ground attack with fresh windup");
                    h.succeed();
                });
            }
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 60, padding = 12, skyAccess = true)
    public void elevatedThreatTriggersTakeoffAndKeepsTarget(GameTestHelper h) {
        var d = dragon(h);
        d.setAutonomousFlight(false, false);
        d.setPos(h.absoluteVec(new Vec3(8.5, 2, 24.5)));
        d.setOnGround(true);
        var v = victim(h);
        v.setNoGravity(true);
        v.setPos(v.position().add(0, 14, 0));
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        h.runAfterDelay(10, () -> {
            require(d.flying() && d.getY() > h.absoluteVec(new Vec3(0, 3, 0)).y && d.getTarget() == v,
                    "Target height difference triggers actual autonomous takeoff");
            h.succeed();
        });
    }
}
