package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class DragonFeedingGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, Player owner, int age) {
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setOwner(owner); d.setTame(true, false); d.setCommand(1);
        d.setPos(h.absoluteVec(new Vec3(20, 3, 20))); d.setHunger(10); d.setHealth(10);
        return d;
    }
    private static void feed(FireDragon d, Player owner) {
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, 3));
        d.mobInteract(owner, InteractionHand.MAIN_HAND);
        require(owner.getMainHandItem().getCount() == 2, "One beef consumed");
    }
    private static void tick(FireDragon d) { d.tickCount++; d.tick(); }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void manualFeedingAllFiveStagesSynchronizesRecoversAndDoesNotRestartOnEveryClick(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        for (int age : new int[]{0, 25, 50, 75, 100}) {
            var d = dragon(h, owner, age); feed(d, owner); tick(d);
            require(d.hunger() == 40 && d.getHealth() == 13 && d.ageDays() == age, "Original hand-food stats at age " + age);
            require(d.attackAnimation() == DragonCombatState.EAT && d.attackAnimationTick() == 0, "Hand feeding starts at first frame at age " + age);
            for (int i = 0; i < 7; i++) tick(d);
            feed(d, owner); tick(d);
            require(d.attackAnimationTick() == 8, "A second click does not restart the neck animation");
            var observer = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            observer.getEntityData().assignValues(d.getEntityData().getNonDefaultValues());
            require(observer.attackAnimation() == DragonCombatState.EAT && observer.attackAnimationTick() == 8, "Observer receives the actual feeding frame");
            for (int i = 0; i < 14; i++) tick(d);
            require(d.attackAnimation() != DragonCombatState.EAT, "Feeding recovers after twenty ticks");
        }
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 12)
    public void feedingNeverOverridesNoAiFlightRiderOrAnIncomingCombatTarget(GameTestHelper h) {
        var owner = h.makeMockPlayer(GameType.SURVIVAL);
        var frozen = dragon(h, owner, 50); frozen.setNoAi(true); feed(frozen, owner); tick(frozen);
        require(frozen.attackAnimation() == DragonCombatState.NONE, "NoAI remains visually static");
        var airborne = dragon(h, owner, 50); airborne.setAutonomousFlight(true, true); feed(airborne, owner); tick(airborne);
        require(airborne.attackAnimation() != DragonCombatState.EAT, "Flight retains its pose");
        var ridden = dragon(h, owner, 50); owner.setPos(ridden.position());
        require(owner.startRiding(ridden), "Owner mounts"); feed(ridden, owner); tick(ridden);
        require(ridden.attackAnimation() != DragonCombatState.EAT, "Feeding cannot move the mounted neck"); owner.stopRiding();
        var defender = dragon(h, owner, 50); defender.setCommand(0); feed(defender, owner); tick(defender);
        require(defender.attackAnimation() == DragonCombatState.EAT, "Feeding starts before interruption");
        var target = net.minecraft.world.entity.EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        target.setPos(defender.position().add(8, 0, 0)); defender.setTarget(target); tick(defender);
        require(defender.attackAnimation() != DragonCombatState.EAT, "Defence interrupts feeding immediately");
        defender.setTarget(null); tick(defender);
        require(defender.attackAnimation() != DragonCombatState.EAT, "Interrupted feeding cannot resume halfway later");
        h.succeed();
    }
}
