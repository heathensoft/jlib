package io.github.heathensoft.jlib.hud.ui.window;

import io.github.heathensoft.jlib.hud.interactable.OnGrab;
import io.github.heathensoft.jlib.hud.interactable.OnRelease;
import io.github.heathensoft.jlib.hud.interactable.UInteractable;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 17/12/2022
 */


public class WindowDragZone extends UInteractable implements OnGrab, OnRelease {

    private final HudWindow window;
    private final Vector2f start;
    private boolean dragging;

    WindowDragZone(HudWindow window) {
        this.start = new Vector2f();
        this.window = window;
        setOnGrab(this);
        setOnRelease(this);
        iRegisterInteractable();
    }

    public void executeOnGrab(Vector2f origin, Vector2f vector, int button) {
        if (button == Mouse.LEFT) {
            if (dragging) {
                Vector2f position = MathLib.vec2();
                position.set(start).add(vector);
                window.setPosition(position);
            } else {
                dragging = true;
                window.focus();
                start.set(window.position());
            }
        }
    }

    public void executeOnRelease(int button) {
        if (button == Mouse.LEFT) {
            window.focus();
            dragging = false;
            if (window.navBarOutsideBounds())
                window.center();
        }
    }

    public void dispose() {
        iRemoveInteractable();
    }
}
