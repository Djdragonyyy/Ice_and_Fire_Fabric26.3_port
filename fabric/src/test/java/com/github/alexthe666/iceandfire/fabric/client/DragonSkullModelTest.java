package com.github.alexthe666.iceandfire.fabric.client;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DragonSkullModelTest {
    @Test void originalHeadSubtreeKeepsItsGeometryAndResetsBetweenWallAndFloorPoses() throws Exception {
        var path = Path.of(System.getProperty("iceandfire.legacyResources"), "assets/iceandfire/models/tabula/firedragon/firedragon_ground.tbl");
        try (var input = Files.newInputStream(path)) {
            var original = TabulaLoader.parse(input); var head = original.parts().get("Head");
            var model = new DragonSkullRenderer.HeadModel(head); var state = new DragonSkullRenderer.State();
            assertSame(head, model.root());
            assertTrue(head.getAllParts().size() > 5); // Teeth, jaw, horns and eye geometry are retained.
            assertFalse(head.getAllParts().contains(original.parts().get("BodyUpper")));
            var pivot = head.storePose(); state.wall = true; model.setupAnim(state);
            assertEquals(Math.toRadians(50), head.xRot, 0.000001);
            assertEquals(pivot.x(), head.x); assertEquals(pivot.y(), head.y); assertEquals(pivot.z(), head.z);
            head.getChild("Jaw").xRot = 1;
            state.wall = false; model.setupAnim(state);
            assertEquals(0, head.xRot); assertEquals(0, head.yRot); assertEquals(0, head.zRot);
            assertEquals(head.getChild("Jaw").getInitialPose().xRot(), head.getChild("Jaw").xRot);
        }
    }
}
