package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DragonFoodAttackGameTests {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static FireDragon dragon(GameTestHelper h, int age) {
        for (int x = 1; x < 47; x++) for (int z = 1; z < 47; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        var d = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        for (int i = 0; i < age; i++) d.growOneDay();
        d.setPos(h.absoluteVec(new Vec3(12.5, 2, 24.5)));
        d.setOnGround(true);
        d.getRandom().setSeed(725);
        return d;
    }
    private static ItemEntity food(GameTestHelper h, FireDragon d, int distance, ItemStack stack) {
        Vec3 pos = d.position().add(distance, 0, 0);
        var item = new ItemEntity(h.getLevel(), pos.x, pos.y, pos.z, stack);
        item.setNoPickUpDelay();
        item.setDeltaMovement(Vec3.ZERO);
        h.getLevel().addFreshEntity(item);
        return item;
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 200, padding = 140)
    public void hungryDragonWalksToMeatConsumesOneAndHealsOriginalGroundAmount(GameTestHelper h) {
        var d = dragon(h, 25);
        d.setHunger(80); d.setHealth(40);
        var item = food(h, d, 10, new ItemStack(Items.BEEF, 3));
        var start = d.position();
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(150, () -> {
            require(d.position().distanceTo(start) > 4, "Hungry dragon actually walks to food");
            require(d.hunger() == 100 && item.getItem().getCount() == 2, "One item consumed, fullness stops further consumption");
            require(d.getHealth() == 70 && d.ageDays() == 25, "Dropped meat heals foodPoints without growth, as original");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", padding = 140)
    public void foodHonorsDietCommandsPickupDelayAndActualReach(GameTestHelper h) {
        var d = dragon(h, 0);
        d.setHunger(10);
        var fish = food(h, d, 0, new ItemStack(Items.COD));
        var beef = food(h, d, 0, new ItemStack(Items.BEEF));
        d.eatDroppedFood(fish);
        require(fish.isAlive() && d.hunger() == 10, "Fire dragons reject fish");
        beef.setPickUpDelay(40);
        d.eatDroppedFood(beef);
        require(beef.isAlive() && d.hunger() == 10, "Pickup delay respected");
        beef.setNoPickUpDelay();
        d.setCommand(1); d.eatDroppedFood(beef);
        require(beef.isAlive(), "Sit prevents eating ground items");
        d.setCommand(2); d.eatDroppedFood(beef);
        require(beef.isAlive(), "Follow is not interrupted by food");
        d.setCommand(0);
        beef.setPos(d.position().add(10, 0, 0));
        d.eatDroppedFood(beef);
        require(beef.isAlive(), "No consumption at a distance");
        beef.setPos(d.position().add(0.2, 0, 0));
        d.eatDroppedFood(beef);
        require(!beef.isAlive() && d.hunger() == 40, "Final unit removes empty item entity");
        h.succeed();
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 50, padding = 140)
    public void eatingAnimationSynchronizesAndResets(GameTestHelper h) {
        var d = dragon(h, 0);
        d.setHunger(80);
        var item = food(h, d, 0, new ItemStack(Items.BEEF));
        d.eatDroppedFood(item);
        h.getLevel().addFreshEntity(d);
        h.runAfterDelay(2, () -> {
            require(d.attackAnimation() == DragonCombatState.EAT, "Eating uses synchronized animation state");
            var observer = IafContent.FIRE_DRAGON.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
            observer.getEntityData().assignValues(d.getEntityData().getNonDefaultValues());
            require(observer.attackAnimation() == DragonCombatState.EAT, "Observer receives eating animation");
        });
        h.runAfterDelay(25, () -> {
            require(d.attackAnimation() == DragonCombatState.NONE || d.attackAnimation() == DragonCombatState.SPEAK,
                    "Eating animation ends after twenty ticks; a new idle voice is not eating");
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 700, padding = 140)
    public void autonomousChargeReleasesProjectileOnlyAtFrameTwenty(GameTestHelper h) {
        var d = dragon(h, 25);
        d.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var v = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        v.setNoAi(true);
        v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); v.setHealth(1000);
        v.setPos(d.position().add(16, 0, 0));
        h.getLevel().addFreshEntity(v);
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        boolean[] chargeSeen = {false}, emitted = {false};
        h.onEachTick(() -> {
            if (d.attackAnimation() == DragonCombatState.CHARGE) {
                chargeSeen[0] = true;
                var projectiles = h.getLevel().getEntitiesOfClass(DragonFireCharge.class,
                        d.getBoundingBox().inflate(40), charge -> charge.getOwner() == d);
                if (d.attackAnimationTick() < 20 && !emitted[0]) require(projectiles.isEmpty(), "No projectile before impact frame");
                if (!projectiles.isEmpty()) {
                    require(d.attackAnimationTick() >= 20, "Projectile emitted at frame twenty or later");
                    emitted[0] = true;
                }
            }
        });
        h.runAfterDelay(620, () -> {
            require(chargeSeen[0] && emitted[0], "Seeded autonomous ranged combat actually releases its charge");
            require(v.getHealth() < 1000, "Autonomous ranged attacks damage the target");
            d.setCommand(1);
            h.succeed();
        });
    }

    @GameTest(structure = "iceandfire:flight_arena", maxTicks = 700, padding = 140)
    public void autonomousMeleeUsesTailAndWingWithServerDamage(GameTestHelper h) {
        var d = dragon(h, 50);
        var v = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
        v.setNoAi(true);
        v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); v.setHealth(1000);
        v.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        v.setPos(d.position().add(3, 0, 0));
        h.getLevel().addFreshEntity(v);
        h.getLevel().addFreshEntity(d);
        d.setTarget(v);
        boolean[] saw = new boolean[2];
        h.onEachTick(() -> {
            if (d.attackAnimation() == DragonCombatState.TAIL) saw[0] = true;
            if (d.attackAnimation() == DragonCombatState.WING) saw[1] = true;
        });
        h.runAfterDelay(620, () -> {
            require(saw[0] && saw[1], "Autonomous combat selects tail and wing animations");
            require(v.getHealth() < 900, "Repeated melee attacks deal actual server damage");
            d.setCommand(1);
            h.succeed();
        });
    }
}
