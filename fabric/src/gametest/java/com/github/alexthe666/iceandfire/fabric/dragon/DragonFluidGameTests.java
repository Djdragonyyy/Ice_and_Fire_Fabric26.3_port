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
}
