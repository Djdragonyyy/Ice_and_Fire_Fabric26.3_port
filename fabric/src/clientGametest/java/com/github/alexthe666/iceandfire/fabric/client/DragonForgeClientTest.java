package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import com.github.alexthe666.iceandfire.fabric.forge.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;

public class DragonForgeClientTest implements FabricClientGameTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(c -> c.player != null && c.level != null && c.gui.screen() == null && c.gui.overlay() == null);
            var pos = world.getServer().computeOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); p.setGameMode(GameType.CREATIVE);
                p.getAbilities().flying = true; p.onUpdateAbilities();
                var base = p.blockPosition().offset(2, 0, 2);
                for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) {
                    p.level().setBlockAndUpdate(base.offset(x, -2, z), Blocks.STONE.defaultBlockState());
                    for (int y = -1; y <= 6; y++) p.level().setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                    p.level().setBlockAndUpdate(base.offset(x, y, z), (y == 0 && x == 0 && z == 0 ? IafDragonForge.CORE
                            : y != 0 && x != 0 && z != 0 ? IafDragonForge.BONE : IafDragonForge.BRICK).defaultBlockState());
                p.level().setBlockAndUpdate(base.north(), IafDragonForge.INPUT.defaultBlockState());
                for (int i = 0; i < 4; i++) p.level().setBlockAndUpdate(base.offset(-3 + 2 * i, -1, 3), IafDragonScales.BLOCKS.get(i).defaultBlockState());
                var core = (DragonForgeCore) p.level().getBlockEntity(base);
                core.setItem(0, new ItemStack(Items.COPPER_INGOT, 2)); core.setItem(1, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, 2));
                // Deterministic inventory/progress fixture; actual flame processing is tested on the native server.
                core.setItem(2, new ItemStack(IafDragonForge.INGOT));
                var data = core.saveWithFullMetadata(p.level().registryAccess()); data.putInt("CookTime", 70000);
                core.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, p.level().registryAccess(), data));
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                require(core.assembled() && core.maxCookTime() == 100000, "Test-only long recipe verifies native32bit menu progress");
                return base;
            });
            context.waitFor(c -> c.level.getBlockState(pos).is(IafDragonForge.CORE)
                    && c.level.getBlockState(pos).getValue(IafDragonForge.ACTIVE));
            context.runOnClient(c -> {
                for (Item item : icons()) {
                    var state = new ItemStackRenderState();
                    c.getItemModelResolver().updateForTopItem(state, new ItemStack(item), ItemDisplayContext.GUI, null, null, 0);
                    require(!state.isEmpty(), "Nonempty native model for " + item);
                }
            });
            context.setScreen(Icons::new); context.waitTicks(10);
            context.takeScreenshot("dragonforge-fire-materials-original-icons-0.1.49"); context.setScreen(() -> null);
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); p.getAbilities().flying = true; p.onUpdateAbilities();
                p.teleportTo(pos.getX() - 4, pos.getY() + 1.5, pos.getZ() - 4);
            });
            context.waitFor(c -> c.player.distanceToSqr(pos.getX() - 4, pos.getY() + 1.5, pos.getZ() - 4) < 0.1);
            context.runOnClient(c -> { c.player.setYRot(-45); c.player.setXRot(22); }); context.waitTicks(12);
            context.takeScreenshot("dragonforge-fire-native-multiblock-0.1.47");
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); p.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 3);
            });
            context.waitFor(c -> c.player.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() - 3) < 0.1);
            context.runOnClient(c -> require(c.gameMode.useItemOn(c.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.north()), Direction.NORTH, pos.north(), false)).consumesAction(), "Native C2S aperture interaction accepted"));
            context.waitForScreen(DragonForgeScreen.class);
            context.waitFor(c -> c.player.containerMenu instanceof DragonForgeMenu m && m.progress() == 70000 && m.duration() == 100000
                    && m.getSlot(0).getItem().getCount() == 2 && m.getSlot(1).getItem().is(IafContent.FIRE_DRAGON_BLOOD)
                    && m.getSlot(2).getItem().is(IafDragonForge.INGOT));
            context.waitTicks(10); context.takeScreenshot("dragonforge-fire-native-menu-progress-0.1.47");
            context.runOnClient(c -> c.gameMode.handleContainerInput(c.player.containerMenu.containerId, 2, 0, ContainerInput.QUICK_MOVE, c.player));
            context.waitFor(c -> c.player.containerMenu instanceof DragonForgeMenu m && m.getSlot(2).getItem().isEmpty());
            context.waitTicks(10); // Client prediction empties the slot before the server handles the C2S packet.
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer(); var core = (DragonForgeCore) p.level().getBlockEntity(pos);
                require(core.getItem(2).isEmpty() && p.getInventory().countItem(IafDragonForge.INGOT) == 1,
                        "C2S shift click transfers exactly one actual output: core=" + core.getItem(2) + " playerCount=" + p.getInventory().countItem(IafDragonForge.INGOT));
                p.teleportTo(pos.getX() + 20, pos.getY(), pos.getZ());
            });
            context.waitFor(c -> c.player.containerMenu == c.player.inventoryMenu);
            int dragonId = world.getServer().computeOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                var core = (DragonForgeCore) p.level().getBlockEntity(pos);
                core.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
                core.setItem(1, new ItemStack(IafContent.FIRE_DRAGON_BLOOD, 64)); core.setItem(2, ItemStack.EMPTY);
                for (int x = -8; x <= 8; x++) for (int z = -15; z <= -2; z++) {
                    p.level().setBlockAndUpdate(pos.offset(x, -2, z), Blocks.STONE.defaultBlockState());
                    for (int y = -1; y <= 6; y++) p.level().setBlockAndUpdate(pos.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                var d = IafContent.FIRE_DRAGON.create(p.level(), EntitySpawnReason.TRIGGERED);
                for (int i = 0; i < 25; i++) d.growOneDay();
                d.setOwner(p); d.setTame(true, false); d.setCommand(1); d.setHunger(100);
                d.setPos(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() - 10.5);
                d.setYRot(0); d.yBodyRot = d.yHeadRot = 0; d.setOnGround(true);
                require(p.level().addFreshEntity(d), "Real autonomous forge dragon spawned");
                p.teleportTo(pos.getX() - 10, pos.getY() + 3, pos.getZ() - 6);
                return d.getId();
            });
            context.waitFor(c -> c.level.getEntity(dragonId) instanceof FireDragon d && d.breathProgress >= 9.5F
                    && c.level.getBlockState(pos.north()).getValue(IafDragonForge.ACTIVE));
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                var d = (FireDragon) p.level().getEntity(dragonId); var core = (DragonForgeCore) p.level().getBlockEntity(pos);
                require(d.fuelingForge() && !d.isNoAi() && core.cookTime() > 0 && core.maxCookTime() == 1000,
                        "Actual AI breath heats the original iron recipe on the authoritative server");
            });
            context.waitFor(c -> c.player.distanceToSqr(pos.getX() - 10, pos.getY() + 3, pos.getZ() - 6) < 0.1
                    && c.gui.screen() == null && c.gui.overlay() == null);
            context.runOnClient(c -> { c.player.setYRot(-84.3F); c.player.setXRot(22); }); context.waitTicks(12);
            context.takeScreenshot("dragonforge-autonomous-dragon-stream-0.1.48");
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().teleportTo(pos.getX() - 5, pos.getY() + 2, pos.getZ() + 7));
            context.waitFor(c -> c.player.distanceToSqr(pos.getX() - 5, pos.getY() + 2, pos.getZ() + 7) < 0.1);
            context.runOnClient(c -> { c.player.setYRot(-128.7F); c.player.setXRot(25); }); context.waitTicks(12);
            context.takeScreenshot("dragonforge-four-original-scale-blocks-0.1.49");
        }
    }
    private static Item[] icons() { return java.util.stream.Stream.concat(java.util.stream.Stream.of(IafDragonForge.BONE.asItem(), IafDragonForge.BRICK.asItem(), IafDragonForge.CORE.asItem(), IafDragonForge.INPUT.asItem(), IafDragonForge.INGOT), IafDragonScales.ITEMS.stream()).toArray(Item[]::new); }
    private static final class Icons extends Screen {
        Icons() { super(Component.literal("Original fire forge assets")); }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
            g.fill(0, 0, width, height, 0xff17202a);
            String[] labels = {"Bone block", "Brick", "Core", "Aperture", "Ingot", "Red scales", "Green scales", "Bronze scales", "Gray scales"}; var items = icons();
            for (int i = 0; i < items.length; i++) {
                int row = i < 5 ? 0 : 1, column = row == 0 ? i : i - 5, columns = row == 0 ? 5 : 4;
                int x = (2 * column + 1) * width / (2 * columns), y = height / 2 + (row == 0 ? -36 : 36);
                g.item(new ItemStack(items[i]), x - 8, y - 8);
                g.centeredText(font, Component.literal(labels[i]), x, y + 22, 0xffffffff);
            }
        }
    }
}
