package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.*;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class DragonEggClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null && client.gui.overlay() == null);
            var previous = world.getServer().computeOnServer(server -> DragonCombatConfig.current);
            try {
                int[] eggs = world.getServer().computeOnServer(server -> {
                    var config = new DragonCombatConfig(); config.dragonEggTime = 24000; DragonCombatConfig.current = config;
                    var p = world.getConnection().getServerPlayer(); p.setGameMode(GameType.CREATIVE);
                    var base = p.blockPosition().offset(2, 0, 2);
                    for (int x = 0; x < 13; x++) for (int z = 0; z < 7; z++) {
                        p.level().setBlockAndUpdate(base.offset(x, -1, z), Blocks.NETHERRACK.defaultBlockState());
                        for (int y = 0; y < 7; y++) p.level().setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                    int[] ids = new int[8];
                    for (int color = 0; color < 4; color++) for (int warm = 0; warm < 2; warm++) {
                        var at = base.offset(color * 3, 0, warm * 3);
                        if (warm == 1) p.level().setBlockAndUpdate(at, (color % 2 == 0 ? Blocks.FIRE : Blocks.LAVA).defaultBlockState());
                        if (warm == 1 && color % 2 != 0) for (var side : net.minecraft.core.Direction.Plane.HORIZONTAL)
                            p.level().setBlockAndUpdate(at.relative(side), Blocks.GLASS.defaultBlockState());
                        var e = IafContent.DRAGON_EGG.create(p.level(), EntitySpawnReason.TRIGGERED);
                        e.setVariant(color); e.setOwner(p); e.setNoAi(true); e.setNoGravity(true);
                        e.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
                        if (color == 0 && warm == 1) e.setCustomName(Component.literal("Ember45"));
                        p.level().addFreshEntity(e); ids[color * 2 + warm] = e.getId();
                    }
                    return ids;
                });
                context.waitFor(client -> {
                    for (int i = 0; i < eggs.length; i++) if (!(client.level.getEntity(eggs[i]) instanceof FireDragonEgg e) || e.variant() != i / 2) return false;
                    for (int i = 0; i < eggs.length; i++) if (FireDragonEgg.canIncubate(client.level.getBlockState(client.level.getEntity(eggs[i]).blockPosition())) != ((i & 1) == 1)) return false;
                    return true;
                });
                context.runOnClient(client -> {
                    require(client.getResourceManager().getResource(IafContent.id("sounds/effects/egg_hatch.ogg")).isPresent()
                            && client.getSoundManager().getSoundEvent(IafContent.EGG_HATCH.location()) != null, "Original hatch audio resolves in the running client");
                    var hot = (FireDragonEgg) client.level.getEntity(eggs[1]); var cold = (FireDragonEgg) client.level.getEntity(eggs[0]);
                    var renderer = (FireDragonEggRenderer) client.getEntityRenderDispatcher().getRenderer(hot);
                    var state = renderer.createRenderState(); renderer.extractRenderState(hot, state, 0); state.ageInTicks = 10;
                    renderer.getModel().setupAnim(state); var root = renderer.getModel().root().getChild("Egg1");
                    require(state.warming && Math.abs(root.xRot) + Math.abs(root.zRot) > 0.01, "Hot egg receives the original two-axis movement");
                    renderer.extractRenderState(cold, state, 0); renderer.getModel().setupAnim(state);
                    require(!state.warming && root.xRot == 0 && root.zRot == 0, "Shared renderer resets a cold egg after a hot egg");
                });
                context.setScreen(() -> new EggBoard(eggs)); context.waitTicks(10);
                context.takeScreenshot("dragon-eggs-four-colors-cold-fire-lava-0.1.45"); context.setScreen(() -> null);
                world.getServer().runOnServer(server -> {
                    var level = world.getConnection().getServerPlayer().level();
                    for (int i = 0; i < eggs.length; i++) if (i != 1) level.getEntity(eggs[i]).discard();
                    DragonCombatConfig.current.dragonEggTime = 1;
                });
                context.waitFor(client -> client.level.getEntity(eggs[1]) == null);
                int born = world.getServer().computeOnServer(server -> {
                    var p = world.getConnection().getServerPlayer();
                    var d = p.level().getEntitiesOfClass(FireDragon.class, p.getBoundingBox().inflate(30)).stream()
                            .filter(e -> e.getCustomName() != null && e.getCustomName().getString().equals("Ember45")).findFirst().orElseThrow();
                    require(d.variant() == 0 && d.ageDays() == 0 && d.hunger() == 50 && d.isOwnedBy(p), "Actual hatch preserves newborn variant/name/owner/hunger"); return d.getId();
                });
                context.waitFor(client -> client.level.getEntity(born) instanceof FireDragon d && d.ageDays() == 0 && d.isOwnedBy(client.player)
                        && d.getCustomName() != null && d.getCustomName().getString().equals("Ember45"));
                context.setScreen(() -> new BabyBoard(born)); context.waitTicks(10);
                context.takeScreenshot("dragon-egg-named-owned-hatchling-0.1.45"); context.setScreen(() -> null);
            } finally { world.getServer().runOnServer(server -> DragonCombatConfig.current = previous); }
        }
    }
    private static final class EggBoard extends Screen {
        private final int[] ids;
        EggBoard(int[] ids) { super(Component.literal("Original egg geometry and incubation")); this.ids = ids; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            g.fill(0, 0, width, height, 0xff17202a);
            String[] colors = {"Red", "Green", "Bronze", "Gray"};
            for (int c = 0; c < 4; c++) for (int warm = 0; warm < 2; warm++) {
                int x = c * width / 4, y = warm * height / 2;
                g.centeredText(font, Component.literal(colors[c] + (warm == 0 ? " - cold / still" : c % 2 == 0 ? " - fire / moving" : " - lava / moving")), x + width / 8, y + 12, 0xffffffff);
                var egg = (FireDragonEgg) Minecraft.getInstance().level.getEntity(ids[c * 2 + warm]);
                InventoryScreen.extractEntityInInventoryFollowsMouse(g, x + 8, y + 35, x + width / 4 - 8, y + height / 2 - 15, 75, 0.0625F, x + width / 8, y + height / 4, egg);
            }
        }
        @Override public boolean isPauseScreen() { return false; }
    }
    private static final class BabyBoard extends Screen {
        private final int id;
        BabyBoard(int id) { super(Component.literal("Named hatchling")); this.id = id; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            g.fill(0, 0, width, height, 0xff17202a);
            g.centeredText(font, Component.literal("Ember45 - stage 1 - owner and name retained"), width / 2, 18, 0xffffffff);
            var d = (FireDragon) Minecraft.getInstance().level.getEntity(id);
            InventoryScreen.extractEntityInInventoryFollowsMouse(g, 35, 45, width - 35, height - 30, 35, 0.0625F, width / 2, height / 2, d);
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
