package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;

/** Real mounted menu, render-only state invariants, and real native prey attachments. */
public class DragonInventoryClientTest implements FabricClientGameTest {
    private static volatile FireDragonRenderer.State last;
    private static volatile int lastDragon = -1, lastPassenger = -1;
    private static java.util.List<Object> liveBefore;
    private static java.util.List<Object> live(FireDragon dragon) {
        var result = new java.util.ArrayList<Object>();
        for (var entity : java.util.stream.Stream.concat(java.util.stream.Stream.of(dragon), dragon.getPassengers().stream()).toList()) {
            result.add(entity.getId()); result.add(entity.position()); result.add(entity.getYRot()); result.add(entity.getXRot());
            result.add(entity.getBoundingBox()); result.add(entity.getVehicle() == null ? -1 : entity.getVehicle().getId());
        }
        result.add(dragon.ageDays()); result.add(dragon.hunger()); return java.util.List.copyOf(result);
    }
    public static void before(FireDragon dragon) { liveBefore = live(dragon); }
    public static void after(FireDragon dragon, FireDragonRenderer.State state) {
        if (!liveBefore.equals(live(dragon))) throw new AssertionError("Inventory extraction mutated live entities");
        last = state; lastDragon = dragon.getId(); lastPassenger = dragon.getFirstPassenger() == null ? -1 : dragon.getFirstPassenger().getId();
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void snapshot(boolean prey) {
        require(last != null && last.scale == 1 && !last.firstPersonPilot && last.inventoryPassengers.size() == 1, "Actual GUI snapshot has one passenger and normalized complete dragon");
        var passenger = last.inventoryPassengers.getFirst();
        require(passenger.prey() == prey && passenger.state().passengerOffset == null && passenger.state().nameTag == null
                && passenger.state().shadowPieces.isEmpty() && passenger.state().outlineColor == 0, "GUI passenger has no world offset, duplicate name or shadow");
        require(passenger.state() instanceof LivingEntityRenderState living
                && Math.abs(living.scale - passenger.scale() / last.inventoryDragonScale) < 0.00001, "Native passenger scale is normalized once");
    }
    @Override public void runTest(ClientGameTestContext context) {
        last = null; lastDragon = lastPassenger = -1;
        var originalCamera = context.computeOnClient(client -> client.options.getCameraType());
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var owner = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var level = owner.level(); var p = owner.blockPosition();
                for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.BEDROCK.defaultBlockState());
                    for (int y = 0; y <= 12; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                var d = IafContent.FIRE_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
                d.setTame(true, false); d.setOwner(owner); d.setNoAi(true); d.setHunger(100);
                for (int day = 0; day < 50; day++) d.growOneDay(); d.refreshDimensions(); d.setHealth(d.getMaxHealth());
                d.setCustomName(Component.literal("Ember")); d.setPos(p.getX()+0.5, p.getY(), p.getZ()+0.5); level.addFreshEntity(d);
                require(owner.startRiding(d) && d.getControllingPassenger() == owner, "Native owner mounts"); return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && client.player.getVehicle() == d);
            context.waitTicks(12);
            context.runOnClient(client -> {
                client.options.setCameraType(CameraType.FIRST_PERSON);
                var d = (FireDragon) client.level.getEntity(id);
                var renderer = (FireDragonRenderer) (Object) client.getEntityRenderDispatcher().getRenderer(d);
                var worldSnapshot = renderer.createRenderState(d, 1);
                require(worldSnapshot.firstPersonPilot && worldSnapshot.inventoryPassengers.isEmpty(), "First-person world snapshot stays separate from the complete GUI portrait");
            });
            world.getServer().runOnServer(server -> DragonMenu.open(owner, (FireDragon) owner.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class);
            context.waitFor(client -> client.gui.overlay() == null && lastDragon == id && lastPassenger == client.player.getId());
            context.runOnClient(client -> snapshot(false));
            context.takeScreenshot("dragon-inventory-owner-pilot-0.1.58");
            world.getServer().runOnServer(server -> { require(owner.getVehicle() == owner.level().getEntity(id), "Preview preserves actual riding"); owner.stopRiding(); });
            context.waitFor(client -> client.player.getVehicle() == null && lastDragon == id && lastPassenger == -1 && last.inventoryPassengers.isEmpty());
            context.takeScreenshot("dragon-inventory-after-dismount-0.1.58");
            world.getServer().runOnServer(server -> { var d = (FireDragon) owner.level().getEntity(id); while (d.ageDays() < 75) d.growOneDay(); d.refreshDimensions(); });
            java.util.List<net.minecraft.world.entity.EntityType<? extends Mob>> types = java.util.List.of(EntityTypes.ZOMBIE, EntityTypes.COW, EntityTypes.HORSE);
            String[] labels = {"upright", "quadruped", "horse"};
            for (int index = 0; index < types.size(); index++) {
                var type = types.get(index);
                int preyId = world.getServer().computeOnServer(server -> {
                    var d = (FireDragon) owner.level().getEntity(id); d.setNoAi(false); d.setCommand(0); d.setOnGround(true);
                    var prey = type.create(owner.level(), EntitySpawnReason.TRIGGERED); prey.setNoAi(true); prey.setPersistenceRequired();
                    prey.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(5000); prey.setHealth(5000);
                    prey.setPos(d.position().add(2, 0, 0)); owner.level().addFreshEntity(prey); d.setTarget(prey);
                    require(DragonInventoryPreviewTestAccess.grab(d, prey), "Actual native capture of " + type); return prey.getId();
                });
                context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.grippedPrey() != null
                        && d.grippedPrey().getId() == preyId && d.attackAnimationTick() >= 18
                        && lastDragon == id && lastPassenger == preyId && last.attackTick >= 18);
                context.runOnClient(client -> {
                    snapshot(true);
                    var d = (FireDragon) client.level.getEntity(id);
                    var renderer = (FireDragonRenderer) (Object) client.getEntityRenderDispatcher().getRenderer(d);
                    require(renderer.createRenderState(d, 1).inventoryPassengers.isEmpty(), "World prey rendering stays separate");
                });
                context.takeScreenshot("dragon-inventory-prey-" + labels[index] + "-0.1.58");
                if (index == types.size() - 1) {
                    var dragon = context.computeOnClient(client -> (FireDragon) client.level.getEntity(id));
                    context.setScreen(() -> new AttachmentPreview(dragon));
                    context.waitFor(client -> client.gui.screen() instanceof AttachmentPreview && client.gui.overlay() == null && !client.isPaused());
                    context.waitTicks(3);
                    context.takeScreenshot("dragon-inventory-prey-horse-expanded-proof-0.1.58");
                }
                world.getServer().runOnServer(server -> { var d = (FireDragon) owner.level().getEntity(id); d.setNoAi(true); d.setCommand(1); });
                context.waitFor(client -> ((FireDragon) client.level.getEntity(id)).grippedPrey() == null && lastPassenger == -1);
                world.getServer().runOnServer(server -> owner.level().getEntity(preyId).discard());
            }
            context.setScreen(() -> null);
        } finally { context.runOnClient(client -> client.options.setCameraType(originalCamera)); }
    }
    private static final class AttachmentPreview extends net.minecraft.client.gui.screens.Screen {
        private final FireDragon dragon;
        AttachmentPreview(FireDragon dragon) { super(Component.literal("Inventory attachment GPU proof")); this.dragon = dragon; }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
            graphics.fill(0, 0, width, height, 0xff303640);
            graphics.text(font, "Actual horse prey - native inventory helper / enlarged test viewport", 8, 8, 0xffffffff);
            net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    0, 24, width, height, 70, 0.0625F, width / 2F - 100, height / 2F, dragon);
        }
    }
}
