package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.treasure.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

public class MetalPileClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String text) { if (!ok) throw new AssertionError(text); }
    @Override public void runTest(ClientGameTestContext context) {
        net.minecraft.client.CameraType[] previous = {null};
        context.runOnClient(c -> { previous[0] = c.options.getCameraType(); c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON); });
        try (var world = context.worldBuilder().create()) {
            context.waitFor(c -> c.player != null && c.level != null && c.gui.screen() == null && c.gui.overlay() == null);
            var base = world.getServer().computeOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); p.setGameMode(GameType.CREATIVE);
                p.getAbilities().flying = true; p.onUpdateAbilities(); var origin = p.blockPosition().offset(3, 0, 3);
                for (int x = -2; x <= 9; x++) for (int z = -2; z <= 16; z++) {
                    p.level().setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    for (int y = 0; y <= 15; y++) p.level().setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                for (int metal = 0; metal < 3; metal++) for (int layer = 1; layer <= 8; layer++)
                    p.level().setBlockAndUpdate(origin.offset(metal * 3, 0, (layer - 1) * 2), IafTreasure.PILES.get(metal).defaultBlockState().setValue(MetalPileBlock.LAYERS, layer));
                p.teleportTo(origin.getX() + 3.5, origin.getY() + 12, origin.getZ() - 9);
                return origin;
            });
            context.waitFor(c -> {
                for (int metal = 0; metal < 3; metal++) for (int layer = 1; layer <= 8; layer++) {
                    var state = c.level.getBlockState(base.offset(metal * 3, 0, (layer - 1) * 2));
                    if (!state.is(IafTreasure.PILES.get(metal)) || state.getValue(MetalPileBlock.LAYERS) != layer) return false;
                }
                return c.player.distanceToSqr(base.getX() + 3.5, base.getY() + 12, base.getZ() - 9) < 0.1;
            });
            context.runOnClient(c -> {
                for (String metal : new String[]{"gold", "silver", "copper"}) for (int height = 2; height <= 16; height += 2)
                    require(c.getResourceManager().getResource(IafContent.id("models/block/" + metal + "pile_height" + height + ".json")).isPresent(), "Original layer model present");
                for (String sound : new String[]{"gold_pile_step", "gold_pile_break"})
                    require(c.getResourceManager().getResource(IafContent.id("sounds/effects/" + sound + ".ogg")).isPresent(), "Original audio asset present");
                c.player.setYRot(0); c.player.setXRot(37);
            });
            context.waitTicks(12); context.takeScreenshot("treasure-piles-all-eight-layers-original-0.1.55");
            context.runOnClient(c -> {
                for (Item item : icons()) {
                    var state = new ItemStackRenderState();
                    c.getItemModelResolver().updateForTopItem(state, new ItemStack(item), ItemDisplayContext.GUI, null, null, 0);
                    require(!state.isEmpty(), "Nonempty pile/nugget native item model");
                }
            });
            context.setScreen(Icons::new); context.waitTicks(10); context.takeScreenshot("treasure-pile-original-item-icons-0.1.55"); context.setScreen(() -> null);
            var stackPos = base.offset(8, 0, 0);
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); p.setGameMode(GameType.SURVIVAL);
                p.level().setBlockAndUpdate(stackPos, IafTreasure.GOLD.defaultBlockState());
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IafTreasure.GOLD, 8)); p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                p.teleportTo(base.getX() + 6.5, base.getY(), base.getZ() - 1.5); p.inventoryMenu.broadcastChanges();
            });
            context.waitFor(c -> c.gui.screen() == null && c.gui.overlay() == null && !c.player.isCreative()
                    && c.player.getMainHandItem().is(IafTreasure.GOLD.asItem()) && c.player.getMainHandItem().getCount() == 8
                    && c.level.getBlockState(stackPos).is(IafTreasure.GOLD));
            context.runOnClient(c -> require(c.gameMode.useItemOn(c.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(new Vec3(stackPos.getX() + 0.5, stackPos.getY() + 0.125, stackPos.getZ() + 0.5), Direction.UP, stackPos, false)).consumesAction(), "Native pile C2S interaction accepted"));
            context.waitFor(c -> c.level.getBlockState(stackPos).getValue(MetalPileBlock.LAYERS) == 2 && c.player.getMainHandItem().getCount() == 7);
            context.waitTicks(10);
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                require(p.level().getBlockState(stackPos).getValue(MetalPileBlock.LAYERS) == 2 && p.getMainHandItem().getCount() == 7,
                        "Authoritative C2S result increments exactly one layer and consumes one pile");
            });
        } finally { context.runOnClient(c -> c.options.setCameraType(previous[0])); }
    }
    private static Item[] icons() { return new Item[]{IafTreasure.GOLD.asItem(), IafTreasure.SILVER.asItem(), IafTreasure.COPPER.asItem(), IafTreasure.SILVER_NUGGET}; }
    private static final class Icons extends Screen {
        Icons() { super(Component.literal("Original treasure pile assets")); }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
            g.fill(0, 0, width, height, 0xff17202a); String[] names = {"Gold pile", "Silver pile", "Copper pile", "Silver nugget"}; var items = icons();
            for (int i = 0; i < 4; i++) { int x = (2 * i + 1) * width / 8, y = height / 2;
                g.item(new ItemStack(items[i]), x - 8, y - 8); g.centeredText(font, Component.literal(names[i]), x, y + 22, 0xffffffff); }
        }
    }
}
