package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public class DragonVoiceGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @GameTest
    public void voicesChangeAtOriginalStageBoundaries(GameTestHelper h) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int day = 0; day <= 100; day++) {
            int index = d.stage() < 3 ? 0 : d.stage() == 3 ? 1 : 2;
            require(d.getAmbientSound() == IafContent.FIRE_IDLE[index], "Idle voice for day " + day);
            require(d.getHurtSound(d.damageSources().generic()) == IafContent.FIRE_HURT[index], "Hurt voice for day " + day);
            require(d.getDeathSound() == IafContent.FIRE_DEATH[index], "Death voice for day " + day);
            d.growOneDay();
        }
        require(d.getAmbientSoundInterval() == 90, "Original ambient interval");
        h.succeed();
    }
    @GameTest
    public void speechExpiresAndSleepingSuppressesAmbientAnimation(GameTestHelper h) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        d.setNoAi(true); d.setSilent(true); d.ambientSoundTime = -10000;
        d.setPos(h.absoluteVec(new Vec3(2.5, 3, 2.5)));
        d.playAmbientSound(); d.tickCount++; d.tick();
        require(d.attackAnimation() == DragonCombatState.NONE, "NoAI suppresses incidental animation");
        d.setNoAi(false); d.setCommand(1);
        d.playAmbientSound();
        for (int tick = 0; tick < 20; tick++) {
            if (tick == 15) d.playAmbientSound(); // An overlapping call must not restart speech.
            d.tickCount++; d.tick();
            require(d.attackAnimation() == DragonCombatState.SPEAK && d.attackAnimationTick() == tick, "Twenty synchronized speaking frames");
        }
        d.tickCount++; d.tick();
        require(d.attackAnimation() == DragonCombatState.NONE, "Speech expires without resetting or blocking the combat state");
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        d.saveWithoutId(output); output.putInt("Command", 0); output.putBoolean("NoAI", true); output.putBoolean("Sleeping", true);
        d.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        require(d.dragonSleeping(), "Sleeping fixture");
        d.playAmbientSound(); d.tickCount++; d.tick();
        require(d.attackAnimation() == DragonCombatState.NONE, "Sleeping dragon never starts ambient speech");
        h.succeed();
    }
}
