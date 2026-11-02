package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class DragonSexSkinClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            try (var base = new NativeImage(3, 2, true); var overlay = new NativeImage(2, 1, true)) {
                for (int y = 0; y < 2; y++) for (int x = 0; x < 3; x++) base.setPixel(x, y, 0xFF0000FF);
                overlay.setPixel(0, 0, 0); overlay.setPixel(1, 0, 0x80FF0000);
                DragonSkinTexture.compose(base, overlay);
                require(base.getPixel(0, 0) == 0xFF0000FF, "Transparent overlay preserves the base");
                require(base.getPixel(1, 0) == 0xFF80007F, "Half-alpha red composes over opaque blue in native ARGB order");
                require(base.getPixel(2, 0) == 0xFF0000FF && base.getPixel(1, 1) == 0xFF0000FF, "Original minimum bounds preserve uncovered pixels");
            }
            for (String color : new String[]{"red", "green", "bronze", "gray"}) for (int stage = 1; stage <= 5; stage++)
                for (String suffix : new String[]{"", "_sleeping"}) {
                    Identifier baseId = IafContent.id("textures/models/firedragon/" + color + "_" + stage + suffix + ".png");
                    var texture = new DragonSkinTexture(baseId, IafContent.id("textures/models/firedragon/male_" + color + ".png"));
                    try (var base = net.minecraft.client.renderer.texture.TextureContents.load(client.getResourceManager(), baseId);
                         var composed = texture.loadContents(client.getResourceManager())) {
                        int changed = 0;
                        require(base.image().getWidth() == composed.image().getWidth() && base.image().getHeight() == composed.image().getHeight(), "Original dimensions preserved");
                        for (int y = 0; y < base.image().getHeight(); y++) for (int x = 0; x < base.image().getWidth(); x++)
                            if (base.image().getPixel(x, y) != composed.image().getPixel(x, y)) changed++;
                        require(changed > 0 && changed <= 1957, "Only the original overlay can change pixels: " + color + stage + suffix + " changed=" + changed);
                    } catch (java.io.IOException error) { throw new AssertionError(error); }
                    finally { texture.close(); }
                }
        });
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int newbornId = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true); d.setMale(true);
                d.setPos(player.position().add(4, 0, 0)); player.level().addFreshEntity(d);
                return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(newbornId) instanceof FireDragon d && d.male() && d.ageDays() == 0);
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(newbornId)));
            context.waitForScreen(DragonScreen.class);
            context.waitFor(client -> client.gui.overlay() == null);
            for (int days : new int[]{0, 9, 10}) {
                world.getServer().runOnServer(server -> {
                    var d = (FireDragon) player.level().getEntity(newbornId);
                    while (d.ageDays() < days) d.growOneDay();
                });
                context.waitFor(client -> ((FireDragon) client.level.getEntity(newbornId)).ageDays() == days);
                context.runOnClient(client -> {
                    var d = (FireDragon) client.level.getEntity(newbornId);
                    var renderer = (FireDragonRenderer) (Object) client.getEntityRenderDispatcher().getRenderer(d);
                    var state = renderer.createRenderState(d, 0);
                    require(d.isAlive() && !d.corpse() && !d.skeletal() && !state.corpse && !state.skeletal,
                            "The newborn skin filter must not turn the living dragon into a corpse or skeleton");
                    if (days < 10) require(renderer.getTextureLocation(state).equals(IafContent.id("textures/models/firedragon/red_1.png")),
                            "Original live male has no overlay before day10");
                    else assertMaleTexture(client, d);
                });
                context.waitTicks(5);
                context.takeScreenshot("dragon-male-skin-day" + days + "-0.1.60");
            }
            context.setScreen(() -> null);
            world.getServer().runOnServer(server -> player.level().getEntity(newbornId).discard());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                for (int day = 0; day < 50; day++) d.growOneDay();
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true); d.setMale(false);
                d.setPos(player.position().add(4, 0, 0)); player.level().addFreshEntity(d); return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon);
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class); context.waitTicks(5);
            context.takeScreenshot("dragon-female-original-skin-0.1.34");
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setMale(true));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.male());
            context.waitTicks(5); context.takeScreenshot("dragon-male-original-overlay-0.1.34");
            context.runOnClient(client -> assertMaleTexture(client, (FireDragon) client.level.getEntity(id)));
            AtomicReference<CompletableFuture<Void>> reload = new AtomicReference<>();
            context.runOnClient(client -> reload.set(client.reloadResourcePacks()));
            context.waitFor(client -> reload.get().isDone());
            context.runOnClient(client -> {
                reload.get().join(); assertMaleTexture(client, (FireDragon) client.level.getEntity(id));
            });
            context.waitFor(client -> client.gui.overlay() == null && client.gui.screen() instanceof DragonScreen, 200);
            context.waitTicks(2); context.takeScreenshot("dragon-male-after-resource-reload-0.1.34");
        }
    }
    private static void assertMaleTexture(net.minecraft.client.Minecraft client, FireDragon d) {
        var renderer = (FireDragonRenderer) (Object) client.getEntityRenderDispatcher().getRenderer(d);
        var state = renderer.createRenderState(); renderer.extractRenderState(d, state, 0);
        var male = renderer.getTextureLocation(state);
        require(client.getTextureManager().getTexture(male) instanceof DragonSkinTexture, "Male uses a native reloadable composite texture");
        require(male.equals(renderer.getTextureLocation(state)), "Repeated rendering reuses the same stable key");
        state.male = false;
        require(!male.equals(renderer.getTextureLocation(state)), "Female retains the unmodified original base texture");
    }
}
