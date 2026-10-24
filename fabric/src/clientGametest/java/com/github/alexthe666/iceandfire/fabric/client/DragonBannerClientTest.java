package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;

/** Real inventory -> entity tracking -> vanilla patterned banner item rendering. */
public class DragonBannerClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true); d.setCommand(1);
                for (int i = 0; i < 50; i++) d.growOneDay();
                d.setPos(player.position().add(3, 0, 0));
                var banner = new ItemStack(Items.BANNER.yellow());
                banner.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                        .addIfRegistered(player.level().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN), BannerPatterns.CROSS, DyeColor.BLUE)
                        .addIfRegistered(player.level().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN), BannerPatterns.BORDER, DyeColor.RED).build());
                d.dragonInventory.setItem(0, banner);
                player.level().addFreshEntity(d);
                return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && !d.dragonBanner().isEmpty());
            context.runOnClient(client -> {
                var d = (FireDragon) client.level.getEntity(id);
                var patterns = d.dragonBanner().get(DataComponents.BANNER_PATTERNS);
                if (!d.dragonBanner().is(Items.BANNER.yellow()) || patterns == null || patterns.layers().size() != 2)
                    throw new AssertionError("Tracked banner must preserve both pattern layers and base color");
                var renderer = (FireDragonRenderer) client.getEntityRenderDispatcher().getRenderer(d);
                var state = renderer.createRenderState();
                renderer.extractRenderState(d, state, 1);
                if (state.banner.isEmpty()) throw new AssertionError("Vanilla banner special model must resolve on dragon");
            });
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class);
            context.waitTicks(45);
            context.takeScreenshot("dragon-patterned-banner-0.1.20");
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).dragonInventory.removeItem(0, 1));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.dragonBanner().isEmpty());
            context.runOnClient(client -> {
                var d = (FireDragon) client.level.getEntity(id);
                var renderer = (FireDragonRenderer) client.getEntityRenderDispatcher().getRenderer(d);
                var state = renderer.createRenderState();
                renderer.extractRenderState(d, state, 1);
                if (!state.banner.isEmpty()) throw new AssertionError("Unequipped banner must clear item render state");
            });
            context.waitTicks(2);
            context.takeScreenshot("dragon-banner-removed-0.1.20");
            context.setScreen(() -> null);
        }
    }
}
