package com.github.alexthe666.iceandfire.fabric.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import java.io.IOException;

/** Original male skin overlay, composed from current resource-pack images on native reload. */
final class DragonSkinTexture extends ReloadableTexture {
    private final Identifier base;
    private final Identifier overlay;
    DragonSkinTexture(Identifier base, Identifier overlay) {
        super(base); this.base = base; this.overlay = overlay;
    }
    @Override public TextureContents loadContents(ResourceManager manager) throws IOException {
        TextureContents contents = TextureContents.load(manager, base);
        try (TextureContents mask = TextureContents.load(manager, overlay)) {
            compose(contents.image(), mask.image());
            return contents;
        } catch (IOException | RuntimeException | Error error) {
            contents.close(); throw error;
        }
    }
    static void compose(NativeImage base, NativeImage overlay) {
        for (int y = 0; y < Math.min(base.getHeight(), overlay.getHeight()); y++)
            for (int x = 0; x < Math.min(base.getWidth(), overlay.getWidth()); x++)
                base.setPixel(x, y, ARGB.alphaBlend(base.getPixel(x, y), overlay.getPixel(x, y)));
    }
}
