package com.github.alexthe666.iceandfire.fabric.client;

import com.github.alexthe666.iceandfire.fabric.IafContent;
import com.github.alexthe666.iceandfire.fabric.dragon.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

/** Original dragon_flame sprite, scale and accelerated travel; adapted from ParticleDragonFlame (LGPL-3.0). */
final class DragonFlameParticle extends SingleQuadParticle {
    private final Vec3 acceleration;
    private final Vec3 target;
    private final float fullSize;

    DragonFlameParticle(ClientLevel level, double x, double y, double z, Vec3 travel, float size, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.target = new Vec3(x, y, z).add(travel);
        acceleration = travel.scale(0.015 + random.nextFloat() * 0.015);
        fullSize = size;
        quadSize = size * 0.25F;
        lifetime = 30;
        friction = 1;
        hasPhysics = true;
        xd = yd = zd = 0;
    }

    @Override public void tick() {
        Vec3 remaining = target.subtract(x, y, z);
        Vec3 step = new Vec3(xd, yd, zd).add(acceleration);
        if (step.lengthSqr() >= remaining.lengthSqr() || onGround) { remove(); return; }
        xd = step.x; yd = step.y; zd = step.z;
        super.tick();
        quadSize = fullSize * (0.25F + 0.75F * DragonBreathVisuals.smooth(age / 6F));
        alpha = Math.clamp((lifetime - age) / 6F, 0, 1);
    }

    @Override public float getQuadSize(float partialTick) {
        var client = Minecraft.getInstance();
        if (client.player != null && client.getCameraEntity() == client.player
                && client.player.getVehicle() instanceof FireDragon && client.options.getCameraType().isFirstPerson()
                && client.player.getEyePosition().distanceToSqr(new Vec3(x, y, z)) < Math.pow(fullSize + 0.75, 2)) return 0;
        return super.getQuadSize(partialTick);
    }
    @Override protected Layer getLayer() { return Layer.TRANSLUCENT; }
    @Override protected int getLightCoords(float partialTick) { return 240; }

    static void initialize() {
        ParticleProviderRegistry.getInstance().register(IafContent.DRAGON_FLAME, sprites ->
                (options, level, x, y, z, dx, dy, dz, random) ->
                        new DragonFlameParticle(level, x, y, z, new Vec3(dx, dy, dz), options.size(), sprites.first()));
        ClientPlayNetworking.registerGlobalReceiver(DragonBreathPayload.TYPE, (packet, context) -> {
            var level = context.client().level;
            if (level == null || !(level.getEntity(packet.dragonId()) instanceof FireDragon)) return;
            Vec3 travel = packet.to().subtract(packet.from());
            if (travel.lengthSqr() < 0.01) return;
            float size = DragonBreathVisuals.size(packet.renderSize());
            int count = DragonBreathVisuals.count(packet.renderSize(), packet.extension());
            for (int i = 0; i < count; i++) {
                // Original random spread is a cone that grows with dragon size, never a fixed thin line.
                double spread = size * 1.4;
                var random = context.client().particleEngine.getRandom();
                Vec3 jitter = new Vec3((random.nextDouble() - 0.5) * spread,
                        (random.nextDouble() - 0.5) * spread, (random.nextDouble() - 0.5) * spread);
                Vec3 direction = travel.add(jitter);
                level.addParticle(new DragonFlameOptions(size), packet.from().x, packet.from().y, packet.from().z,
                        direction.x, direction.y, direction.z);
            }
        });
    }
}
