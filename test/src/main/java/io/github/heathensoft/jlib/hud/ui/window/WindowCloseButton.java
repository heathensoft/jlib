package io.github.heathensoft.jlib.hud.ui.window;

import io.github.heathensoft.jlib.hud.interactable.OnClick;
import io.github.heathensoft.jlib.hud.interactable.OnCursorHover;
import io.github.heathensoft.jlib.hud.interactable.UInteractable;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 17/12/2022
 */


public class WindowCloseButton extends UInteractable implements OnClick, OnCursorHover {

    private final HudWindow window;
    protected boolean hovered;

    WindowCloseButton(HudWindow window) {
        this.window = window;
        setOnCursorHover(this);
        setOnClick(this);
        iRegisterInteractable();
    }

    public void update(float dt) {
        hovered = false;
    }

    public void dispose() {
        iRemoveInteractable();
    }

    @Override
    public void executeOnClick(Vector2f position, int button) {
        if (button == Mouse.LEFT) {
            window.close();
        }
    }

    @Override
    public void executeOnCursorHover(Vector2f position) {
        hovered = true;
    }
}
