package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DragonFlightPathGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int age) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setNoAi(true); d.setSilent(true);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.refreshDimensions(); d.setPos(h.absoluteVec(new Vec3(10.5, 2, 10.5))); return d;
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void stageFiveDetectsMidRouteTreeOutsideCentralRayWithoutShrinking(GameTestHelper h) {
        var d = dragon(h, 125); Vec3 start = d.position(), end = h.absoluteVec(new Vec3(34.5, 2, 10.5));
        double width = d.getBbWidth();
        h.setBlock(new BlockPos(22, 2, 12), Blocks.OAK_LOG);
        Vec3 center = start.add(0, d.getBbHeight() / 2, 0);
        require(h.getLevel().clip(new ClipContext(center, center.add(end.subtract(start)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, d)).getType() == HitResult.Type.MISS, "Original central ray misses side obstacle");
        require(h.getLevel().noCollision(d, d.getBoundingBox().move(end.subtract(start))), "Destination itself is clear");
        require(!DragonFlightClearance.clear(d, start, end), "Swept full-size adult body detects the tree midway");
        var small = dragon(h, 0);
        require(DragonFlightClearance.clear(small, start, end), "A small dragon really fits the same corridor");
        require(d.getBbWidth() == width, "Clearance never shrinks the adult hitbox"); h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void boundedDetourChecksEveryLegAndInvalidatesAfterObstruction(GameTestHelper h) {
        var d = dragon(h, 125); Vec3 end = h.absoluteVec(new Vec3(34.5, 2, 10.5));
        h.setBlock(new BlockPos(22, 2, 12), Blocks.STONE);
        var path = new DragonFlightPath(d);
        require(path.reaches(end) && path.detouring(), "Blocked direct path has an above-obstacle detour");
        Vec3 first = path.next(end);
        require(first.y > d.getY() && DragonFlightClearance.clear(d, d.position(), first), "First leg is a safe climb");
        h.setBlock(new BlockPos(10, 11, 10), Blocks.STONE);
        d.tickCount += 5; // Advance the bounded route validation clock without changing world time.
        Vec3 changed = path.next(end);
        require(changed != null && !changed.equals(first) && DragonFlightClearance.clear(d, d.position(), changed),
                "New obstacle invalidates cached climb and selects a safe side detour");
        h.setBlock(new BlockPos(10, 11, 10), Blocks.AIR);
        int legs = 0;
        while (d.position().distanceToSqr(end) > 0.01 && legs++ < 6) {
            Vec3 next = path.next(end);
            require(next != null && DragonFlightClearance.clear(d, d.position(), next), "Every selected leg clears full body");
            d.setPos(next);
        }
        require(d.position().distanceToSqr(end) < 0.01, "Bounded route reaches the original destination");
        path.reset(); require(!path.detouring(), "Command/reset removes cached waypoints");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void clearanceRejectsUnloadedChunksAndInvalidCoordinatesWithoutLoading(GameTestHelper h) {
        var d = dragon(h, 125); Vec3 unloaded = d.position().add(100000, 0, 100000);
        BlockPos chunk = BlockPos.containing(unloaded);
        require(!h.getLevel().hasChunkAt(chunk), "Unloaded fixture");
        require(!DragonFlightClearance.clear(d, unloaded, unloaded.add(8, 0, 0)) && !h.getLevel().hasChunkAt(chunk), "No new chunk request for route");
        require(!DragonFlightClearance.clear(d, d.position(), new Vec3(Double.NaN, 2, 3)), "Reject non-finite input");
        Vec3 high = new Vec3(d.getX(), h.getLevel().getMaxY() - 1, d.getZ());
        require(!DragonFlightClearance.clear(d, high, high), "Full body respects world ceiling even when feet are inside it");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void sealedEnclosureHasNoInventedRouteOrChangedSize(GameTestHelper h) {
        var d = dragon(h, 125); double width = d.getBbWidth();
        for (int x = 6; x <= 15; x++) for (int z = 6; z <= 15; z++) {
            h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            h.setBlock(new BlockPos(x, 13, z), Blocks.STONE);
            if (x == 6 || x == 15 || z == 6 || z == 15)
                for (int y = 2; y < 13; y++) h.setBlock(new BlockPos(x, y, z), Blocks.STONE);
        }
        require(h.getLevel().noCollision(d, d.getBoundingBox().deflate(1.0E-5)), "Dragon fits inside the enclosure");
        var path = new DragonFlightPath(d);
        require(!path.reaches(h.absoluteVec(new Vec3(34.5, 2, 10.5))), "A sealed room has no invented way through blocks");
        require(d.getBbWidth() == width, "Failure keeps full adult size"); h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void distantHomeIntentPlansLoadedLocalLegInsteadOfRejectingWholeJourney(GameTestHelper h) {
        var d = dragon(h, 125); var path = new DragonFlightPath(d);
        Vec3 goal = d.position().add(300, 0, 0);
        require(path.reaches(goal), "A long home goal can start within the known loaded area");
        Vec3 next = path.next(goal);
        require(next != null && next.distanceTo(d.position()) <= 32.01 && next.x > d.getX()
                && DragonFlightClearance.clear(d, d.position(), next), "First leg is bounded, loaded and directed toward the distant goal");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void dragonLandingIgnoresFallButAirDismountedPlayerStillTakesDamage(GameTestHelper h) {
        for (int x = 1; x < 20; x++) for (int z = 1; z < 20; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = dragon(h, 125); d.setPos(d.position().add(0, 24, 0)); d.setAutonomousFlight(true, false); d.setOnGround(false);
        var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        d.setOwner(owner); d.setTame(true, false); owner.setPos(d.position());
        require(owner.startRiding(d), "Owner boards airborne adult"); owner.stopRiding();
        require(!owner.isIgnoringFallDamageFromCurrentImpulse(), "No dismount immunity while in flight");
        float dragonHealth = d.getHealth(), playerHealth = owner.getHealth();
        for (int tick = 0; tick < 120; tick++) {
            d.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(0, -0.5, 0));
            if (owner.isAlive()) owner.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(0, -0.5, 0));
        }
        require(d.isAlive() && d.onGround() && d.getHealth() == dragonHealth, "Original dragon body survives its own real landing without fall damage");
        require(owner.getHealth() < playerHealth, "Air-dismounted player retains ordinary falling damage"); h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 420, skyAccess = true, padding = 100)
    public void stageFiveEscortActuallyNavigatesWallAndLandsWithoutClipping(GameTestHelper h) {
        for (int x = 0; x < 48; x++) for (int z = 0; z < 48; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        for (int y = 2; y <= 18; y++) for (int z = 0; z < 48; z++) h.setBlock(new BlockPos(24, y, z), Blocks.STONE);
        var owner = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        owner.setPos(h.absoluteVec(new Vec3(40.5, 2, 24.5))); owner.setOnGround(true);
        var d = dragon(h, 125); d.setOwner(owner); d.setTame(true, false); d.setHunger(100); d.setNoAi(false); d.setCommand(2);
        d.setPos(h.absoluteVec(new Vec3(8.5, 8, 24.5))); d.setOnGround(false); d.setAutonomousFlight(true, false);
        double width = d.getBbWidth(); boolean[] crossedInFlight = {false}, finished = {false};
        h.getLevel().addFreshEntity(d);
        h.onEachTick(() -> {
            if (finished[0]) return; // Native succeed() discards test entities immediately.
            require(d.isAlive() && d.getBbWidth() == width && h.getLevel().noCollision(d, d.getBoundingBox().deflate(1.0E-5)),
                    "Moving adult retains its whole body outside blocks: pos=" + d.position() + " health=" + d.getHealth()
                            + " alive=" + d.isAlive() + " removed=" + d.isRemoved() + " removal=" + d.getRemovalReason()
                            + " width=" + d.getBbWidth() + " expectedWidth=" + width
                            + " clear=" + h.getLevel().noCollision(d, d.getBoundingBox().deflate(1.0E-5)));
            if (d.getX() > h.absoluteVec(new Vec3(25, 0, 0)).x && d.flying()) crossedInFlight[0] = true;
        });
        h.runAfterDelay(380, () -> {
            require(d.isAlive() && d.getBbWidth() == width && h.getLevel().noCollision(d, d.getBoundingBox().deflate(1.0E-5)),
                    "Adult remains alive and outside blocks at the final observation");
            require(crossedInFlight[0] && d.distanceTo(owner) < 11 && d.onGround() && !d.flying(),
                    "Adult flies around obstacle and lands near owner: pos=" + d.position() + " owner=" + owner.position()
                            + " flying=" + d.flying() + " target=" + d.flightManager.target());
            require(h.getBlockState(new BlockPos(24, 8, 24)).is(Blocks.STONE), "Route preserves solid obstacle");
            finished[0] = true;
            h.succeed();
        });
    }
}
