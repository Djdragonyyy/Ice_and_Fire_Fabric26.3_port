package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Item;

/** Original fire-stew TemptGoal, below commands and defence and above wandering. */
final class DragonTemptGoal extends TemptGoal {
    static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, IafContent.id("tempt_dragon"));
    private final FireDragon dragon;

    DragonTemptGoal(FireDragon dragon) {
        super(dragon, 1, stack -> stack.is(FOOD), false);
        this.dragon = dragon;
    }

    private boolean available() {
        return dragon.canMoveOnGround() && dragon.command() == 0 && dragon.getTarget() == null
                && !dragon.seekingFood && !dragon.isInLove();
    }
    @Override public boolean canUse() { return available() && super.canUse(); }
    @Override public boolean canContinueToUse() { return available() && super.canContinueToUse(); }
}
