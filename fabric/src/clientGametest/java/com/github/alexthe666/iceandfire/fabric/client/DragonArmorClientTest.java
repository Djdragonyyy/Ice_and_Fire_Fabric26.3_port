package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.dragon.DragonArmorItem;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Uses the actual resource manager, baked item models and GPU GUI renderer. */
public class DragonArmorClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
        context.waitFor(client -> client.player != null && client.level != null);
        context.runOnClient(client -> {
            for (var item : java.util.List.of(com.github.alexthe666.iceandfire.fabric.IafContent.FIRE_STEW,
                    com.github.alexthe666.iceandfire.fabric.IafTerrain.NEST.asItem())) {
                var state = new ItemStackRenderState();
                client.getItemModelResolver().updateForTopItem(state, new ItemStack(item), ItemDisplayContext.GUI, null, null, 0);
                if (state.isEmpty()) throw new AssertionError("Empty breeding item model: " + item);
            }
            for (var item : DragonArmorItem.ITEMS) {
                var state = new ItemStackRenderState();
                client.getItemModelResolver().updateForTopItem(state, new ItemStack(item), ItemDisplayContext.GUI, null, null, 0);
                if (state.isEmpty()) throw new AssertionError("Empty armor item model: " + item);
            }
        });
        context.setScreen(ArmorIcons::new);
        context.waitTicks(12);
        context.takeScreenshot("dragon-armor-icons-0.1.13");
        context.setScreen(() -> null);
        var serverPlayer = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
        int entityId = world.getServer().computeOnServer(server -> {
            var dragon = com.github.alexthe666.iceandfire.fabric.IafContent.FIRE_DRAGON.create(serverPlayer.level(),
                    net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (dragon == null) throw new AssertionError("Dragon creation");
            dragon.setTame(true, false);
            dragon.setOwner(serverPlayer);
            for (int i = 0; i < 50; i++) dragon.growOneDay();
            dragon.setCommand(1);
            dragon.setPos(serverPlayer.position().add(3, 0, 0));
            dragon.dragonInventory.setItem(1, new ItemStack(DragonArmorItem.ITEMS.get(0)));
            dragon.dragonInventory.setItem(4, new ItemStack(DragonArmorItem.ITEMS.get(3)));
            serverPlayer.level().addFreshEntity(dragon);
            return dragon.getId();
        });
        context.waitFor(client -> client.level.getEntity(entityId) != null);
        world.getServer().runOnServer(server -> com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu.open(serverPlayer,
                (com.github.alexthe666.iceandfire.fabric.dragon.FireDragon) serverPlayer.level().getEntity(entityId)));
        context.waitForScreen(DragonScreen.class);
        context.waitTicks(12);
        context.runOnClient(client -> {
            if (!(client.player.containerMenu instanceof com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu menu)
                    || !menu.getSlot(1).getItem().is(DragonArmorItem.ITEMS.get(0))
                    || !menu.getSlot(4).getItem().is(DragonArmorItem.ITEMS.get(3)))
                throw new AssertionError("Head and tail stacks must be synchronized to the client inventory");
        });
        context.takeScreenshot("dragon-armor-head-tail-inventory-0.1.13");
        context.setScreen(() -> null);
        }
    }
    private static final class ArmorIcons extends Screen {
        ArmorIcons() { super(Component.literal("Dragon armor regression")); }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xff303030);
            graphics.item(new ItemStack(com.github.alexthe666.iceandfire.fabric.IafContent.FIRE_STEW), 340, 12);
            graphics.item(new ItemStack(com.github.alexthe666.iceandfire.fabric.IafTerrain.NEST.asItem()), 365, 12);
            for (int slot = 0; slot < 4; slot++) graphics.text(font, DragonArmorItem.PARTS[slot], 150 + slot * 40, 12, 0xffffffff);
            for (int material = 0; material < 8; material++) {
                int y = 34 + material * 23;
                graphics.text(font, DragonArmorItem.MATERIALS[material], 8, y + 4, 0xffffffff);
                for (int slot = 0; slot < 4; slot++) graphics.item(new ItemStack(DragonArmorItem.ITEMS.get(material * 4 + slot)), 150 + slot * 40, y);
            }
        }
    }
}
