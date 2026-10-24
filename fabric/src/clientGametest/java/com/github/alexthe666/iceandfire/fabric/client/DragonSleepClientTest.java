package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.DragonMenu;
import com.github.alexthe666.iceandfire.fabric.dragon.FireDragon;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Frozen saved sleep state exercises the real sleeping skin/pose and eye-layer suppression. */
public class DragonSleepClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            var player = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer());
            int id = world.getServer().computeOnServer(server -> {
                var d = IafContent.FIRE_DRAGON.create(player.level(), EntitySpawnReason.TRIGGERED);
                d.setOwner(player); d.setTame(true, false); d.setNoAi(true);
                for (int i = 0; i < 50; i++) d.growOneDay();
                d.setPos(player.position().add(3, 0, 0));
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.level().registryAccess());
                d.saveWithoutId(output);
                output.putBoolean("Sleeping", true);
                d.load(TagValueInput.create(ProblemReporter.DISCARDING, player.level().registryAccess(), output.buildResult()));
                player.level().addFreshEntity(d);
                return d.getId();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.dragonSleeping());
            world.getServer().runOnServer(server -> DragonMenu.open(player, (FireDragon) player.level().getEntity(id)));
            context.waitForScreen(DragonScreen.class);
            context.waitTicks(45);
            context.takeScreenshot("dragon-sleep-pose-0.1.18");
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setCommand(1));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && !d.dragonSleeping());
            context.waitTicks(45);
            context.takeScreenshot("dragon-awake-eyes-0.1.18");
            context.runOnClient(client -> {
                for (var sounds : new net.minecraft.sounds.SoundEvent[][]{IafContent.FIRE_IDLE, IafContent.FIRE_HURT, IafContent.FIRE_DEATH})
                    for (var sound : sounds) if (client.getSoundManager().getSoundEvent(sound.location()) == null)
                        throw new AssertionError("Original voice must resolve in the real client sound manager: " + sound.location());
            });
            world.getServer().runOnServer(server -> {
                var d = (FireDragon) player.level().getEntity(id);
                d.setNoAi(false); d.playAmbientSound();
            });
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d
                    && d.attackAnimation() == com.github.alexthe666.iceandfire.fabric.dragon.DragonCombatState.SPEAK
                    && d.attackAnimationTick() >= 5 && d.attackAnimationTick() <= 12);
            context.takeScreenshot("dragon-speaking-jaw-0.1.25");
            world.getServer().runOnServer(server -> ((FireDragon) player.level().getEntity(id)).setNoAi(true));
            context.waitFor(client -> client.level.getEntity(id) instanceof FireDragon d && d.attackAnimation() == 0);
            FireDragon[] preview = new FireDragon[1];
            context.runOnClient(client -> {
                var d = (FireDragon) client.level.getEntity(id); preview[0] = d;
                var renderer = (FireDragonRenderer) client.getEntityRenderDispatcher().getRenderer(d);
                int originalTick = d.tickCount;
                try {
                    for (int tick : new int[]{43, 44}) {
                        d.tickCount = tick;
                        var state = renderer.createRenderState(); renderer.extractRenderState(d, state, 1);
                        if (state.blinking != (tick == 44) || state.sleeping || state.sleepProgress != 0)
                            throw new AssertionError("Blink changes eye state without sleeping AI or body pose");
                        if (renderer.getTextureLocation(state).getPath().endsWith("_sleeping.png") != (tick == 44))
                            throw new AssertionError("Blink uses original closed-eye skin");
                    }
                } finally { d.tickCount = originalTick; }
            });
            for (int tick : new int[]{43, 44}) {
                context.setScreen(() -> new BlinkPreview(preview[0], tick)); context.waitTicks(3);
                context.takeScreenshot(tick == 43 ? "dragon-open-eyes-0.1.27" : "dragon-blink-0.1.27");
            }
            context.setScreen(() -> null);
        }
    }
    private static final class BlinkPreview extends net.minecraft.client.gui.screens.Screen {
        private final FireDragon dragon; private final int tick;
        BlinkPreview(FireDragon dragon, int tick) { super(net.minecraft.network.chat.Component.literal("Dragon blink regression")); this.dragon = dragon; this.tick = tick; }
        @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xff303030);
            graphics.text(font, tick == 43 ? "Open eyes" : "Blink", 20, 10, 0xffffffff);
            int originalTick = dragon.tickCount;
            try {
                dragon.tickCount = tick;
                net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                        50, 30, width - 50, height - 20, 70, 0.0625F, 0, height / 2F, dragon);
            } finally { dragon.tickCount = originalTick; }
        }
    }
}
