package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DragonFluidGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void tank(GameTestHelper h, boolean lava, int depth) {
        for (int x = 2; x <= 15; x++) for (int z = 2; z <= 15; z++) {
            h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            for (int y = 2; y <= 12; y++) h.setBlock(new BlockPos(x, y, z),
                    x == 2 || x == 15 || z == 2 || z == 15 ? Blocks.STONE : Blocks.AIR);
        }
        // Clear the previous liquid before switching: neighboring water would solidify fresh lava.
        for (int x = 3; x < 15; x++) for (int z = 3; z < 15; z++) for (int y = 2; y < 2 + depth; y++)
            h.setBlock(new BlockPos(x, y, z), lava ? Blocks.LAVA : Blocks.WATER);
    }
    private static FireDragon dragon(GameTestHelper h, int age, double y) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setNoAi(true); d.setSilent(true);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.refreshDimensions(); d.setPos(h.absoluteVec(new Vec3(8.5, y, 8.5)));
        d.tickCount++; d.tick(); return d;
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void originalWaterImmersionDependsOnGrowthAndActualFluidDepth(GameTestHelper h) {
        for (int depth : new int[]{1, 2, 3}) {
            tank(h, false, depth);
            for (int age : new int[]{0, 25, 50, 75, 100}) {
                var d = dragon(h, age, 2);
                require(d.getFluidHeight(FluidTags.WATER) > 0, "Real native water interaction detected");
                boolean expected = depth > d.stage() / 2;
                require(d.isInWater() == expected, "Original water threshold: age=" + age + " depth=" + depth + " height=" + d.getFluidHeight(FluidTags.WATER));
                require(d.isInLiquid() == expected, "Native travel dispatcher honors the stage-aware water state");
            }
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void ownedPilotReceivesBothFluidVerticalControlsAndOriginalLavaMotion(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        for (boolean lava : new boolean[]{false, true}) {
            tank(h, lava, 6); var d = dragon(h, 50, 4); d.setOwner(owner); d.setTame(true, false);
            owner.setPos(d.position()); require(owner.startRiding(d), "Owner mounts swimming-age adult");
            require(d.isLocalInstanceAuthoritative(), "Native mock pilot simulates authority on server");
            for (int flags : new int[]{DragonControlPayload.UP, DragonControlPayload.DOWN}) {
                d.setDeltaMovement(Vec3.ZERO); d.setRiderControls(flags);
                Vec3 input = d.getRiddenInput(owner, Vec3.ZERO); double before = d.getY();
                require(Math.abs(input.y - DragonFluidMotion.vertical(lava, flags == DragonControlPayload.UP, flags == DragonControlPayload.DOWN)) < 1.0E-8,
                        "Original vertical input selected: lava=" + lava + " flags=" + flags + " input=" + input + " water=" + d.isInWater() + " lavaState=" + d.isInLava() + " flying=" + d.flying() + " heightW=" + d.getFluidHeight(FluidTags.WATER) + " heightL=" + d.getFluidHeight(FluidTags.LAVA));
                d.travel(input);
                require(flags == DragonControlPayload.UP ? d.getY() > before : d.getY() < before, "Actual fluid movement follows the control");
            }
            if (lava) {
                d.setDeltaMovement(0, 0.5, 0); d.setRiderControls(DragonControlPayload.UP | DragonControlPayload.DOWN);
                d.travel(d.getRiddenInput(owner, Vec3.ZERO));
                require(Math.abs(d.getDeltaMovement().y - 0.105) < 1.0E-6, "Both lava controls retain original0.3 hover damping then0.7 drag");
                owner.setSprinting(false); float normal = d.getRiddenSpeed(owner); owner.setSprinting(true);
                require(Math.abs(d.getRiddenSpeed(owner) - normal * 1.4F) < 1.0E-7, "Lava sprint has original1.4 factor");
                owner.setSprinting(false);
            }
            require(!d.flying(), "Swimming inputs alone do not force an immediate flight"); owner.stopRiding();
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void unpilotedLavaUsesOriginalDragAndExistingFireImmunity(GameTestHelper h) {
        tank(h, true, 6); var d = dragon(h, 50, 4);
        require(d.isInLava(), "Real lava state"); float health = d.getHealth();
        for (int tick = 0; tick < 40; tick++) { d.tickCount++; d.tick(); }
        require(d.isAlive() && d.getHealth() == health, "Existing fire/lava immunity preserved");
        d.setNoAi(false); d.setSpeed(0.25F); d.setDeltaMovement(0, 0.2, 0);
        Vec3 before = d.position(); d.travel(new Vec3(1, 0, 0));
        require(d.getX() > before.x && Math.abs(d.getY() - before.y - 0.2) < 1.0E-6, "Unpiloted original lava movement remains physical");
        require(Math.abs(d.getDeltaMovement().y - 0.14) < 1.0E-6, "Lava drag0.7 is applied to existing motion");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void lavaSurfaceUsesOriginalPilotInputWalkingPhysicsAndDrag(GameTestHelper h) {
        tank(h, true, 1);
        var d = dragon(h, 50, 3.001);
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        d.setOwner(owner); d.setTame(true, false); owner.setPos(d.position());
        require(owner.startRiding(d), "Owner mounts above the real lava surface");
        require(!d.isInLava() && h.getLevel().getFluidState(d.getBlockPosBelowThatAffectsMyMovement()).is(FluidTags.LAVA),
                "Fixture is above lava, distinct from immersed lava");
        owner.xxa = 1; owner.zza = 1; owner.setSprinting(true);
        var input = d.getRiddenInput(owner, new Vec3(0, -0.25, 0));
        require(Math.abs(input.x - 0.05) < 1.0E-6 && Math.abs(input.z - 1.2) < 1.0E-6 && input.y == -0.25,
                "Original surface strafe, sprint and incoming vertical input");
        require(Math.abs(d.getRiddenSpeed(owner) - d.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) * 1.8) < 1.0E-6,
                "Surface original default ground speed, independent from lava swimming curve");
        owner.zza = -1; owner.setSprinting(false);
        require(Math.abs(d.getRiddenInput(owner, Vec3.ZERO).z + 0.2) < 1.0E-6, "Original reverse factor");
        d.setDeltaMovement(Vec3.ZERO); d.setOnGround(false); d.setYRot(0);
        var before = d.position();
        d.travel(new Vec3(input.x, 0, input.z));
        require(d.getX() > before.x && d.getZ() > before.z, "Native physical movement consumes the surface input");
        require(Math.abs(d.getDeltaMovement().y + 0.08 * 0.98 * 0.7) < 1.0E-6,
                "Native gravity is followed by original surface drag: " + d.getDeltaMovement());
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void surfaceRulesRemainDistinctFromOrdinaryGroundAndFlyingPilot(GameTestHelper h) {
        tank(h, true, 1); var d = dragon(h, 50, 3.001);
        var owner = h.makeMockPlayer(GameType.SURVIVAL); d.setOwner(owner); d.setTame(true, false);
        owner.setPos(d.position()); require(owner.startRiding(d), "Owner mounts");
        owner.xxa = 1; owner.zza = -1;
        d.setAutonomousFlight(true, false);
        require(d.getRiddenInput(owner, Vec3.ZERO).x == 0.5 && d.getRiddenInput(owner, Vec3.ZERO).z == -0.25,
                "Surface scaling does not replace aerial input");
        d.setAutonomousFlight(false, false);
        h.setBlock(d.getBlockPosBelowThatAffectsMyMovement().subtract(h.absolutePos(BlockPos.ZERO)), Blocks.STONE);
        require(d.getRiddenInput(owner, Vec3.ZERO).x == 0.5 && Math.abs(d.getRiddenInput(owner, Vec3.ZERO).z + d.getRiddenSpeed(owner) * 0.2) < 1.0E-6,
                "Ground uses its original forward-speed factor, distinct from surface lava");
        require(Math.abs(d.getRiddenSpeed(owner) - d.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) * 1.8) < 1.0E-6,
                "Ground has the same original base factor, with different input scaling");
        h.succeed();
    }
}
