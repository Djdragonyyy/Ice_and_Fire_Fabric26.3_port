package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class DragonRoarGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int days) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setSilent(true); d.setCommand(1); d.ambientSoundTime = -10000;
        for (int i = 0; i < days; i++) d.growOneDay();
        d.setPos(h.absoluteVec(new Vec3(8.5, 2, 8.5))); return d;
    }
    private static void tick(FireDragon d) { d.tickCount++; d.tick(); }

    @GameTest
    public void bothRoarsHaveOriginalDurationAndNoAiCancels(GameTestHelper h) {
        var d = dragon(h, 50);
        for (boolean epic : new boolean[]{false, true}) {
            int animation = epic ? DragonCombatState.EPIC_ROAR : DragonCombatState.ROAR;
            d.roar(epic);
            for (int frame = 0; frame < (epic ? 60 : 40); frame++) {
                tick(d);
                require(d.attackAnimation() == animation && d.attackAnimationTick() == frame, "Synchronized roar frame " + frame);
                if (frame < (epic ? 59 : 39)) d.roar(!epic); // Active roars cannot restart.
            }
            tick(d); require(d.attackAnimation() == DragonCombatState.NONE || d.attackAnimation() == DragonCombatState.SPEAK,
                    "Roar ends at original boundary; a subsequent voice is allowed: animation=" + d.attackAnimation());
        }
        d.roar(true); tick(d); d.setNoAi(true); tick(d);
        require(d.attackAnimation() == DragonCombatState.NONE, "NoAI cancels a running roar");
        d.roar(false); tick(d); require(d.attackAnimation() == DragonCombatState.NONE, "NoAI blocks new roars");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 200)
    public void adultAuraKeepsOriginalBoxOwnershipAndStrongerDragonExclusion(GameTestHelper h) {
        for (int days : new int[]{75, 100}) for (boolean epic : new boolean[]{false, true}) {
            var d = dragon(h, days);
            var owner = h.makeMockPlayer(GameType.SURVIVAL); owner.setPos(d.position().add(2, 0, 2)); h.getLevel().addFreshEntity(owner);
            d.setOwner(owner); d.setTame(true, false);
            var pet = EntityTypes.WOLF.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            pet.setNoAi(true); pet.setTame(true, false); pet.setOwner(owner); pet.setPos(d.position().add(3, 0, 3)); h.getLevel().addFreshEntity(pet);
            var victim = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            victim.setNoAi(true); victim.setPos(d.position().add(5, 0, 5)); h.getLevel().addFreshEntity(victim);
            var negative = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            negative.setNoAi(true); negative.setPos(d.position().add(-20, 0, -20)); h.getLevel().addFreshEntity(negative);
            var stronger = dragon(h, 100); stronger.setNoAi(true); stronger.setPos(d.position().add(8, 0, 8)); h.getLevel().addFreshEntity(stronger);
            var smaller = dragon(h, 25); smaller.setNoAi(true); smaller.setPos(d.position().add(10, 0, 10)); h.getLevel().addFreshEntity(smaller);
            d.roar(epic);
            int duration = (epic ? 50 : 30) * (d.stage() - 3) * 30;
            require(owner.hasEffect(MobEffects.STRENGTH) && owner.getEffect(MobEffects.STRENGTH).getDuration() == duration, "Owner strength with original duration");
            require(pet.hasEffect(MobEffects.STRENGTH) && !pet.hasEffect(MobEffects.WEAKNESS), "Owner's pet gains strength");
            require(victim.hasEffect(MobEffects.WEAKNESS) && victim.getEffect(MobEffects.WEAKNESS).getDuration() == duration, "Other mob gains weakness");
            require(!negative.hasEffect(MobEffects.WEAKNESS), "Original positive expandTowards box is preserved");
            require(!stronger.hasEffect(MobEffects.WEAKNESS) && smaller.hasEffect(MobEffects.WEAKNESS), "Equal/stronger dragons excluded; smaller dragon affected");
            owner.discard(); pet.discard(); victim.discard(); negative.discard(); stronger.discard(); smaller.discard();
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 200)
    public void juvenilesHaveNoAuraAndCombatAndSleepSuppressRoars(GameTestHelper h) {
        var d = dragon(h, 50);
        var victim = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        victim.setNoAi(true); victim.setPos(d.position().add(3, 0, 3)); h.getLevel().addFreshEntity(victim);
        d.roar(true); require(!victim.hasEffect(MobEffects.WEAKNESS), "No aura through stage three");
        d.setCommand(0); d.setTarget(victim); tick(d);
        require(d.attackAnimation() != DragonCombatState.ROAR && d.attackAnimation() != DragonCombatState.EPIC_ROAR, "Combat interrupts cosmetic roar");
        d.setTarget(null); d.setDragonSleeping(true); d.roar(false);
        require(d.dragonSleeping(), "Calling roar does not wake a sleeping dragon");
        for (int day : new int[]{0, 50, 75, 100}) {
            var voice = dragon(h, day);
            require(voice.getRoarSound() == IafContent.FIRE_ROAR[day < 50 ? 0 : day < 75 ? 1 : 2], "Original age-specific roar sound");
            require(voice.getSoundSource() == net.minecraft.sounds.SoundSource.HOSTILE, "Original hostile sound category");
        }
        h.succeed();
    }
}
