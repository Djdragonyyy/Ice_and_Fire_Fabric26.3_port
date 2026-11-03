package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class DragonDamageGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int days) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setNoAi(true); d.setSilent(true);
        for (int i = 0; i < days; i++) d.growOneDay();
        d.refreshDimensions(); d.setPos(h.absoluteVec(new Vec3(24.5, 8, 24.5)));
        h.getLevel().addFreshEntity(d); d.tickCount++; d.tick();
        d.setHealth(d.getMaxHealth()); return d;
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void originalEnvironmentalImmunitiesCoverBodyAndMultipartWithoutBlockingOrdinaryDamage(GameTestHelper h) {
        var falling = EntityTypes.FALLING_BLOCK.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int days : new int[]{0, 25, 50, 75, 100}) {
            var d = dragon(h, days); d.setDragonSleeping(true);
            float before = d.getHealth();
            for (var source : new net.minecraft.world.damagesource.DamageSource[]{d.damageSources().inWall(),
                    d.damageSources().fallingBlock(falling), d.damageSources().cramming()}) {
                require(!d.hurtServer(h.getLevel(), source, 5) && d.getHealth() == before && d.dragonSleeping(),
                        "Original environmental source must not damage or wake the body at age " + days);
                require(!d.dragonParts().getFirst().hurtServer(h.getLevel(), source, 5) && d.getHealth() == before,
                        "Multipart must share the parent protection, including cramming");
            }
            require(d.hurtServer(h.getLevel(), d.damageSources().generic(), 3) && d.getHealth() < before,
                    "Ordinary damage still reaches the same dragon and wakes it");
            d.discard();
        }
        var anvilVictim = dragon(h, 50); float before = anvilVictim.getHealth();
        require(anvilVictim.hurtServer(h.getLevel(), anvilVictim.damageSources().anvil(falling), 5)
                && anvilVictim.getHealth() < before, "Do not broaden FALLING_BLOCK to the distinct native ANVIL type");
        h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void realPilotCannotDamageOwnMountButProtectionEndsOnDismount(GameTestHelper h) {
        var pilot = h.makeMockPlayer(GameType.SURVIVAL);
        var d = dragon(h, 50); d.setTame(true, false); d.setOwner(pilot); pilot.setPos(d.position());
        require(pilot.startRiding(d) && d.getControllingPassenger() == pilot, "Real native pilot occupies the mount");
        float before = d.getHealth();
        require(!d.hurtServer(h.getLevel(), d.damageSources().playerAttack(pilot), 5) && d.getHealth() == before,
                "Original current pilot cannot hit the parent");
        require(!d.dragonParts().getFirst().hurtServer(h.getLevel(), d.damageSources().playerAttack(pilot), 5)
                && d.getHealth() == before, "Current pilot cannot hit the parent through the head");
        pilot.stopRiding();
        require(d.hurtServer(h.getLevel(), d.damageSources().playerAttack(pilot), 5) && d.getHealth() < before,
                "Ownership alone does not grant immunity after actual dismount");
        var other = h.makeMockPlayer(GameType.SURVIVAL); other.setUUID(java.util.UUID.randomUUID());
        var second = dragon(h, 50); second.setTame(true, false); second.setOwner(pilot); pilot.setPos(second.position());
        require(pilot.startRiding(second), "Pilot remounts another healthy fixture"); before = second.getHealth();
        require(second.hurtServer(h.getLevel(), second.damageSources().playerAttack(other), 5) && second.getHealth() < before,
                "Another player can still damage a ridden dragon");
        pilot.stopRiding(); h.succeed();
    }
    @GameTest(structure = "iceandfire:flight_arena", padding = 90)
    public void realShoulderCarryProtectsFromArrowsAndCarrierButNotUnrelatedMelee(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var enemy = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        var arrow = EntityTypes.ARROW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int sample = 0; sample < 2; sample++) {
            var d = dragon(h, 0); d.setTame(true, false); d.setOwner(owner); owner.setPos(d.position());
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            require(d.mobInteract(owner, InteractionHand.MAIN_HAND).consumesAction() && d.shoulderCarrier() == owner,
                    "Actual empty-hand interaction places a baby on the shoulder");
            float before = d.getHealth();
            require(!d.hurtServer(h.getLevel(), d.damageSources().arrow(arrow, enemy), 3) && d.getHealth() == before,
                    "Source passenger arrow protection follows the port's shoulder relationship");
            require(!d.hurtServer(h.getLevel(), d.damageSources().playerAttack(owner), 3) && d.getHealth() == before,
                    "Carrier cannot hit its carried baby");
            if (sample == 0) {
                require(d.hurtServer(h.getLevel(), d.damageSources().mobAttack(enemy), 3) && d.getHealth() < before,
                        "Unrelated melee damage is still accepted while carried");
                d.releaseShoulder();
            } else {
                d.releaseShoulder();
                require(d.shoulderCarrier() == null && d.hurtServer(h.getLevel(), d.damageSources().arrow(arrow, enemy), 3)
                        && d.getHealth() < before, "Releasing the shoulder immediately restores arrow damage");
            }
            d.discard();
        }
        h.succeed();
    }
}
