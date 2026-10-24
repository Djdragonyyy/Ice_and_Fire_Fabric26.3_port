package com.github.alexthe666.iceandfire.fabric.client;

import org.joml.Quaternionf;

/** Client render snapshot; never changes the captured mob's physical rotation or AI. */
public interface DragonPreyRenderData {
    Quaternionf iceandfire$preyRotation();
    void iceandfire$setPreyRotation(Quaternionf rotation);
}
