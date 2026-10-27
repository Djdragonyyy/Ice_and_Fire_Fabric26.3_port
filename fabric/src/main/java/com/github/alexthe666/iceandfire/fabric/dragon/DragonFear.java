package com.github.alexthe666.iceandfire.fabric.dragon;

import com.github.alexthe666.iceandfire.fabric.DragonCombatConfig;
import com.github.alexthe666.iceandfire.fabric.IafContent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

/** Original ServerEvents fear tags and VillagerAIFearUntamed, adapted to native entity loading (LGPL-3.0). */
public final class DragonFear {
    static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE, IafContent.id("fear_dragons"));
    static final TagKey<EntityType<?>> VILLAGERS = TagKey.create(Registries.ENTITY_TYPE, IafContent.id("villagers"));

    public static void initialize() {
        // Goals are transient: restore them on saved entity loading as well as fresh spawns.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof PathfinderMob mob) install(mob);
        });
    }

    static void install(PathfinderMob mob) {
        var selector = mob.getGoalSelector();
        if (selector.getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof FearGoal)) return;
        if (DragonCombatConfig.current.villagersFearDragons && mob.getType().builtInRegistryHolder().is(VILLAGERS)) {
            selector.addGoal(1, new FearGoal(mob, true));
        } else if (DragonCombatConfig.current.animalsFearDragons && mob.getType().builtInRegistryHolder().is(LIVESTOCK)) {
            selector.addGoal(1, new FearGoal(mob, false));
        }
    }

    static final class FearGoal extends AvoidEntityGoal<FireDragon> {
        FearGoal(PathfinderMob mob, boolean villager) {
            super(mob, FireDragon.class, villager ? 8 : 30, villager ? 0.8 : 1, villager ? 0.8 : 0.5,
                    dragon -> villager || (!mob.getType().builtInRegistryHolder().is(VILLAGERS)
                            && !(mob instanceof AbstractVillager) && !(mob instanceof AbstractGolem)
                            && (!(mob instanceof TamableAnimal animal) || !animal.isTame())));
        }
    }

    private DragonFear() {}
}
