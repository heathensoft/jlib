package io.github.heathensoft.jlib.test.gui.window.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.test.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.test.graphicsOld.TextureRegion;
import io.github.heathensoft.jlib.test.gui.GUI;
import io.github.heathensoft.jlib.test.gui.GUIGraphics;
import io.github.heathensoft.jlib.test.gui.interactable.OnClick;
import io.github.heathensoft.jlib.test.gui.interactable.OnCursorHover;
import io.github.heathensoft.jlib.test.gui.interactable.UInteractable;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

import static io.github.heathensoft.jlib.test.gui.GUI.*;


/**
 * @author Frederik Dahl
 * 23/12/2022
 */


public class NavButtons implements BoxContent {

    private final MaximizeButton maximizeButton;
    private final RestoreButton restoreButton;
    private final CloseButton closeButton;
    private final DynamicWindow window;

    public NavButtons(DynamicWindow window) {
        this.maximizeButton = new MaximizeButton(window);
        this.restoreButton = new RestoreButton(window);
        this.closeButton = new CloseButton(window);
        this.window = window;
    }

    public void render(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        TextureRegion region;
        float default_color = NAV_BTN_COLOR.toFloatBits();
        float inactive_color = NAV_BTN_INACTIVE_COLOR.toFloatBits();
        float color;
        int id;
        // Restore Button
        region = graphics.coreAsset(GUIGraphics.WINDOW_RESTORE_ICON);
        y0 -= (NAV_BAR_PADDING + NAV_BTN_SIZE);
        x0 += NAV_BAR_PADDING;
        if (window.isRestored()) {
            color = inactive_color;
            id = 0;
        } else {
            if (restoreButton.hovered) {
                color = NAV_BTN_RESTORE_HOVER_COLOR.toFloatBits();
            } else {color = default_color;
            }id = restoreButton.iData();
        }batch.draw(region,x0,y0,NAV_BTN_SIZE,NAV_BTN_SIZE,color,id);
        // Maximize Button
        region = graphics.coreAsset(GUIGraphics.WINDOW_MAXIMIZE_ICON);
        x0 += NAV_BTN_SIZE + NAV_BTN_PADDING;
        if (window.isMaximized()) {
            color = inactive_color;
            id = 0;
        } else {
            if (maximizeButton.hovered) {
                color = NAV_BTN_MAXIMIZE_HOVER_COLOR.toFloatBits();
            } else {color = default_color;
            }id = maximizeButton.iData();
        }batch.draw(region,x0,y0,NAV_BTN_SIZE,NAV_BTN_SIZE,color,id);
        // Close Button
        region = graphics.coreAsset(GUIGraphics.WINDOW_CLOSE_ICON);
        x0 += NAV_BTN_SIZE + NAV_BTN_PADDING;
        if (closeButton.hovered) {
            color = NAV_BTN_CLOSE_HOVER_COLOR.toFloatBits();
        } else {color = default_color;
        }id = closeButton.iData();
        batch.draw(region,x0,y0,NAV_BTN_SIZE,NAV_BTN_SIZE,color,id);
    }


    public void update(float dt) {
        maximizeButton.update(dt);
        restoreButton.update(dt);
        closeButton.update(dt);
    }

    public void dispose() {
        Disposable.dispose(
                maximizeButton,
                restoreButton,
                closeButton
        );
    }

    public static abstract class NavButton extends UInteractable implements OnClick, OnCursorHover {
        protected final DynamicWindow window;
        protected boolean hovered;
        NavButton(DynamicWindow window) {
            this.window = window;
            setOnCursorHover(this);
            setOnClick(this);
            iRegisterInteractable();
        }
        public int cursorHover() {
            return GUI.CURSOR_HAND;
        }
        public int cursorDrag() {
            return GUI.CURSOR_HAND;
        }
        public void update(float dt) {
            hovered = false;
        }
        public void dispose() {
            iRemoveInteractable();
        }
        public void executeOnCursorHover(Vector2f position) {
            hovered = true;
        }
    }

    public static final class CloseButton extends NavButton {
        CloseButton(DynamicWindow window) {
            super(window);
        }
        public void executeOnClick(Vector2f position, int button) {
            if (button == Mouse.LEFT) {
                window.close();
            }
        }
    }

    public static final class RestoreButton extends NavButton {
        RestoreButton(DynamicWindow window) {
            super(window);
        }
        public void executeOnClick(Vector2f position, int button) {
            if (button == Mouse.LEFT) {
                boolean was_maximized = window.isMaximized();
                window.restore();
                if (was_maximized || window.navBarOutsideBounds())
                    window.center();
                window.focus();
            }
        }
    }

    public static final class MaximizeButton extends NavButton {
        MaximizeButton(DynamicWindow window) {
            super(window);
        }
        public void executeOnClick(Vector2f position, int button) {
            if (button == Mouse.LEFT) {
                window.maximize();
                window.focus();
            }
        }
    }
}
