package com.github.alexthe666.iceandfire.fabric.dragon;

/** Retains a short key press until the next server tick; holding jump still charges for one second. */
public final class DragonTakeoff {
    private int heldTicks;
    private int requestedTicks;
    private boolean takeoffDown;
    private boolean jumpDown;
    private int jumpWindow;

    public void acceptControls(int flags) {
        boolean pressed = (flags & DragonControlPayload.TAKEOFF) != 0;
        if (pressed && !takeoffDown) requestedTicks = 20;
        takeoffDown = pressed;
        boolean jump = (flags & DragonControlPayload.UP) != 0;
        if (jump && !jumpDown) {
            if (jumpWindow > 0) { requestedTicks = 20; jumpWindow = 0; }
            else jumpWindow = 7;
        }
        jumpDown = jump;
    }

    public void reset() {
        heldTicks = 0;
        requestedTicks = 0;
        takeoffDown = false;
        jumpDown = false;
        jumpWindow = 0;
    }

    public boolean tick(boolean canCarryRider, boolean flying, boolean up, boolean down) {
        if (!canCarryRider || flying || down) {
            heldTicks = requestedTicks = jumpWindow = 0;
            return false;
        }
        if (jumpWindow > 0) jumpWindow--;
        heldTicks = up ? Math.min(21, heldTicks + 1) : 0;
        boolean ready = requestedTicks > 0 || heldTicks > 20;
        if (requestedTicks > 0) requestedTicks--;
        if (!ready) return false;
        heldTicks = requestedTicks = jumpWindow = 0;
        return true;
    }
}
