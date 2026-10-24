package com.github.alexthe666.iceandfire.fabric.dragon;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.Comparator;
import java.util.EnumSet;

/** Ground food pursuit from DragonAITargetItems/FoodUtils (LGPL-3.0). */
final class DragonFoodGoal extends Goal {
    private final FireDragon dragon;
    private ItemEntity food;
    private long nextScan;
    private int recalculate;
    DragonFoodGoal(FireDragon dragon) {
        this.dragon = dragon;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean available() {
        return dragon.canMoveOnGround() && dragon.command() == 0 && dragon.getTarget() == null && dragon.hunger() < 100;
    }
    private boolean edible(ItemEntity item) {
        return item.isAlive() && !item.hasPickUpDelay() && DragonFeeding.foodPoints(item.getItem()) > 0
                && dragon.hasLineOfSight(item);
    }
    @Override public boolean canUse() {
        if (!available()) return false;
        // Target selection may reserve food before the movement selector gets its turn.
        if (food != null && edible(food)) return true;
        if (dragon.level().getGameTime() < nextScan) return false;
        nextScan = dragon.level().getGameTime() + 20;
        double range = dragon.getAttributeValue(Attributes.FOLLOW_RANGE);
        food = dragon.level().getEntitiesOfClass(ItemEntity.class, dragon.getBoundingBox().inflate(range, 4, range), this::edible)
                .stream().sorted(Comparator.comparingDouble(dragon::distanceToSqr))
                .filter(item -> {
                    var path = dragon.getNavigation().createPath(item, 0);
                    return path != null && path.canReach();
                }).findFirst().orElse(null);
        return food != null;
    }
    @Override public boolean canContinueToUse() { return available() && food != null && edible(food); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { recalculate = 0; dragon.seekingFood = true; }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        dragon.getLookControl().setLookAt(food, 30, 30);
        double reach = dragon.getBbWidth() * 2 + dragon.getBbHeight() / 2;
        if (dragon.distanceToSqr(food) < reach || dragon.headPosition().distanceToSqr(food.position()) < dragon.getBbHeight()) {
            dragon.eatDroppedFood(food);
            food = null;
            dragon.getNavigation().stop();
        } else if (--recalculate <= 0) {
            recalculate = 10;
            if (!dragon.getNavigation().moveTo(food, 1)) food = null;
        }
    }
    @Override public void stop() { food = null; dragon.seekingFood = false; dragon.getNavigation().stop(); }
}
