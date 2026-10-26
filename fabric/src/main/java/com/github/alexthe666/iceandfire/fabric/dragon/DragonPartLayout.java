package com.github.alexthe666.iceandfire.fabric.dragon;

/** Coefficients from EntityDragonBase.resetParts; lengths use renderSize/3, not collision scale. */
public final class DragonPartLayout {
    public record Part(String name, float radius, float yaw, float y, float width, float height, float damage) { }
    public static final java.util.List<Part> PARTS = java.util.List.of(
            new Part("head", 1.55F, 0, 0.6F, 0.5F, 0.35F, 1.5F),
            new Part("neck", 0.85F, 0, 0.7F, 0.5F, 0.2F, 1),
            new Part("right_wing_upper", 1, 90, 0.5F, 0.85F, 0.3F, 0.5F),
            new Part("right_wing_lower", 1.4F, 100, 0.3F, 0.85F, 0.2F, 0.5F),
            new Part("left_wing_upper", 1, -90, 0.5F, 0.85F, 0.3F, 0.5F),
            new Part("left_wing_lower", 1.4F, -100, 0.3F, 0.85F, 0.2F, 0.5F),
            new Part("tail1", -0.75F, 0, 0.6F, 0.35F, 0.35F, 1),
            new Part("tail2", -1.15F, 0, 0.45F, 0.35F, 0.35F, 1),
            new Part("tail3", -1.5F, 0, 0.35F, 0.35F, 0.35F, 1),
            new Part("tail4", -1.95F, 0, 0.25F, 0.45F, 0.3F, 1.5F));
    private DragonPartLayout() { }
}