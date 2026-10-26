package com.github.alexthe666.iceandfire.fabric;

import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragonEgg;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonFireCharge;
import net.minecraft.sounds.SoundEvent;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class IafContent {
    public static final net.minecraft.world.inventory.MenuType<com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu> DRAGON_MENU =
            Registry.register(BuiltInRegistries.MENU, id("dragon"), new net.fabricmc.fabric.api.menu.v1.ExtendedMenuType<>(
                    com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu::new, net.minecraft.network.codec.ByteBufCodecs.VAR_INT));
    public static final net.minecraft.core.particles.ParticleType<com.github.alexthe666.iceandfire.fabric.dragon.DragonFlameOptions> DRAGON_FLAME =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("dragon_flame"), new net.minecraft.core.particles.ParticleType<>(false) {
                @Override public com.mojang.serialization.MapCodec<com.github.alexthe666.iceandfire.fabric.dragon.DragonFlameOptions> codec() { return com.github.alexthe666.iceandfire.fabric.dragon.DragonFlameOptions.CODEC; }
                @Override public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, com.github.alexthe666.iceandfire.fabric.dragon.DragonFlameOptions> streamCodec() { return com.github.alexthe666.iceandfire.fabric.dragon.DragonFlameOptions.STREAM_CODEC; }
            });
    public static final EntityType<FireDragon> FIRE_DRAGON = registerDragon();
    public static final EntityType<com.github.alexthe666.iceandfire.fabric.dragon.DragonPart> DRAGON_MULTIPART = registerPart();
    public static final EntityType<FireDragonEgg> DRAGON_EGG = registerEgg();
    public static final EntityType<com.github.alexthe666.iceandfire.fabric.dragon.DragonSkull> DRAGON_SKULL = registerSkull();
    public static final EntityType<DragonFireCharge> FIRE_DRAGON_CHARGE = registerCharge();
    public static final SoundEvent FIRE_BREATH = Registry.register(BuiltInRegistries.SOUND_EVENT,
            id("firedragon_breath"), SoundEvent.createVariableRangeEvent(id("firedragon_breath")));
    public static final SoundEvent[] FIRE_IDLE = dragonSounds("idle");
    public static final SoundEvent[] FIRE_HURT = dragonSounds("hurt");
    public static final SoundEvent[] FIRE_DEATH = dragonSounds("death");
    public static final SoundEvent[] FIRE_ROAR = dragonSounds("roar");
    public static final SoundEvent DRAGON_FLIGHT = Registry.register(BuiltInRegistries.SOUND_EVENT,
            id("dragon_flight"), SoundEvent.createVariableRangeEvent(id("dragon_flight")));
    public static final SoundEvent DRAGON_FLUTE_SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT,
            id("dragonflute"), SoundEvent.createVariableRangeEvent(id("dragonflute")));
    public static final Item DRAGON_MEAL = registerItem("dragon_meal", properties("dragon_meal"));
    public static final Item SICKLY_DRAGON_MEAL = registerItem("sickly_dragon_meal", properties("sickly_dragon_meal"));
    public static final Item CREATIVE_DRAGON_MEAL = registerItem("creative_dragon_meal", properties("creative_dragon_meal"));
    public static final Item DRAGON_STAFF = registerItem("dragon_stick", properties("dragon_stick").stacksTo(1));
    public static final Item DRAGON_HORN = Registry.register(BuiltInRegistries.ITEM, id("dragon_horn"),
            new com.github.alexthe666.iceandfire.fabric.dragon.DragonHornItem(properties("dragon_horn")));
    public static final Item DRAGON_FLUTE = Registry.register(BuiltInRegistries.ITEM, id("dragon_flute"),
            new com.github.alexthe666.iceandfire.fabric.dragon.DragonFluteItem(properties("dragon_flute")));
    public static final Item SUMMONING_CRYSTAL = Registry.register(BuiltInRegistries.ITEM, id("summoning_crystal_fire"),
            new com.github.alexthe666.iceandfire.fabric.dragon.SummoningCrystalItem(properties("summoning_crystal_fire")));
    public static final Item FIRE_STEW = registerItem("fire_stew", properties("fire_stew"));
    public static final Item[] FIRE_EGGS = {
            registerEggItem("dragonegg_red", 0), registerEggItem("dragonegg_green", 1),
            registerEggItem("dragonegg_bronze", 2), registerEggItem("dragonegg_gray", 3)
    };

    public static final Item DRAGON_BONE = registerItem("dragonbone", properties("dragonbone"));
    public static final Item[] FIRE_SCALES = {
            registerItem("dragonscales_red", properties("dragonscales_red")), registerItem("dragonscales_green", properties("dragonscales_green")),
            registerItem("dragonscales_bronze", properties("dragonscales_bronze")), registerItem("dragonscales_gray", properties("dragonscales_gray"))
    };
    public static final Item FIRE_DRAGON_HEART = registerItem("fire_dragon_heart", properties("fire_dragon_heart"));
    public static final Item FIRE_DRAGON_BLOOD = registerItem("fire_dragon_blood", properties("fire_dragon_blood"));
    public static final Item FIRE_DRAGON_FLESH = Registry.register(BuiltInRegistries.ITEM, id("fire_dragon_flesh"),
            new com.github.alexthe666.iceandfire.fabric.dragon.FireDragonFleshItem(properties("fire_dragon_flesh")));
    public static final Item DRAGON_SKULL_FIRE = Registry.register(BuiltInRegistries.ITEM, id("dragon_skull_fire"),
            new com.github.alexthe666.iceandfire.fabric.dragon.DragonSkullItem(properties("dragon_skull_fire")));

    private IafContent() {}

    private static EntityType<com.github.alexthe666.iceandfire.fabric.dragon.DragonSkull> registerSkull() {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, id("dragon_skull"));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(com.github.alexthe666.iceandfire.fabric.dragon.DragonSkull::new, MobCategory.MISC)
                        .sized(0.9F, 0.65F).clientTrackingRange(10).build(key));
    }

    private static SoundEvent[] dragonSounds(String action) {
        return java.util.Arrays.stream(new String[]{"child", "teen", "adult"}).map(age -> {
            var key = id("firedragon_" + age + "_" + action);
            return Registry.register(BuiltInRegistries.SOUND_EVENT, key, SoundEvent.createVariableRangeEvent(key));
        }).toArray(SoundEvent[]::new);
    }
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(IceAndFireFabric.MOD_ID, path); }

    private static EntityType<com.github.alexthe666.iceandfire.fabric.dragon.DragonPart> registerPart() {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, id("dragon_multipart"));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(com.github.alexthe666.iceandfire.fabric.dragon.DragonPart::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F).fireImmune().noSave().noSummon().clientTrackingRange(16).updateInterval(1).build(key));
    }

    private static EntityType<FireDragon> registerDragon() {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, id("fire_dragon"));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(FireDragon::new, MobCategory.CREATURE)
                        .sized(0.78F, 1.2F).fireImmune().clientTrackingRange(16).updateInterval(1).build(key));
    }

    private static EntityType<FireDragonEgg> registerEgg() {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, id("dragon_egg"));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(FireDragonEgg::new, MobCategory.MISC)
                        .sized(0.45F, 0.55F).fireImmune().clientTrackingRange(10).build(key));
    }

    private static EntityType<DragonFireCharge> registerCharge() {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, id("fire_dragon_charge"));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.of(DragonFireCharge::new, MobCategory.MISC).sized(0.8F, 0.8F)
                        .fireImmune().clientTrackingRange(16).updateInterval(1).build(key));
    }

    private static Item.Properties properties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)));
    }

    private static Item registerItem(String name, Item.Properties properties) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), new Item(properties));
    }

    private static Item registerEggItem(String name, int variant) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), new Item(properties(name).stacksTo(1)) {
            @Override public InteractionResult useOn(UseOnContext context) {
                if (!(context.getLevel() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
                if (context.getPlayer() == null) return InteractionResult.PASS;
                var pos = context.getClickedPos().relative(context.getClickedFace());
                FireDragonEgg egg = DRAGON_EGG.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
                if (egg == null) return InteractionResult.FAIL;
                egg.setVariant(variant);
                egg.setOwner(context.getPlayer());
                egg.setCustomName(context.getItemInHand().get(DataComponents.CUSTOM_NAME));
                egg.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (!server.noCollision(egg, egg.getBoundingBox())) return InteractionResult.FAIL;
                if (!server.addFreshEntity(egg)) return InteractionResult.FAIL;
                context.getItemInHand().consume(1, context.getPlayer());
                return InteractionResult.SUCCESS;
            }
        });
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id("customize_to_dragon"), com.github.alexthe666.iceandfire.fabric.dragon.CustomizeToDragon.CODEC);
        IafTerrain.initialize();
        com.github.alexthe666.iceandfire.fabric.dragon.DragonArmorItem.initialize();
        FabricDefaultAttributeRegistry.register(FIRE_DRAGON, FireDragon.attributes());
        FabricDefaultAttributeRegistry.register(DRAGON_EGG, FireDragonEgg.attributes());
        FabricDefaultAttributeRegistry.register(DRAGON_SKULL, com.github.alexthe666.iceandfire.fabric.dragon.DragonSkull.attributes());
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("spawn_eggs"))).register(output -> {
            for (Item egg : FIRE_EGGS) output.accept(egg);
        });
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients"))).register(output -> {
                    output.accept(DRAGON_MEAL);
                    output.accept(SICKLY_DRAGON_MEAL);
                    output.accept(CREATIVE_DRAGON_MEAL);
                    output.accept(DRAGON_STAFF);
                    output.accept(DRAGON_HORN);
                    output.accept(DRAGON_FLUTE);
                    output.accept(SUMMONING_CRYSTAL);
                    output.accept(FIRE_STEW);
                    output.accept(DRAGON_BONE);
                    for (Item scale : FIRE_SCALES) output.accept(scale);
                    output.accept(FIRE_DRAGON_HEART); output.accept(FIRE_DRAGON_BLOOD);
                    output.accept(FIRE_DRAGON_FLESH); output.accept(DRAGON_SKULL_FIRE);
                });
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("building_blocks"))).register(output -> IafTerrain.ITEMS.forEach(output::accept));
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("combat"))).register(output -> com.github.alexthe666.iceandfire.fabric.dragon.DragonArmorItem.ITEMS.forEach(output::accept));
    }
}
