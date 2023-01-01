package io.github.heathensoft.jlib.gui.window.window;

import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.interactable.OnGrab;
import io.github.heathensoft.jlib.gui.interactable.OnRelease;
import io.github.heathensoft.jlib.gui.interactable.UInteractable;
import io.github.heathensoft.jlib.gui.window.Size;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public abstract class WindowBorder extends UInteractable implements OnGrab, OnRelease {

    protected final DynamicWindow window;
    protected float start;
    protected float desired;
    protected float previous;
    protected boolean dragging;

    protected WindowBorder(DynamicWindow window) {
        this.window = window;
        setOnGrab(this);
        setOnRelease(this);
        iRegisterInteractable();
    }

    public void dispose() {
        iRemoveInteractable();
    }


    public void executeOnRelease(int button) {
        if (button == Mouse.LEFT) {
            dragging = false;
            if (window.navBarOutsideBounds())
                window.center();
            window.focus();
        }
    }

    public static final class Top extends WindowBorder {
        public Top(DynamicWindow window) {
            super(window);
        }
        public void executeOnGrab(Vector2f origin, Vector2f vector, int button) {
            if (button == Mouse.LEFT) {
                if (dragging) {
                    float mouse = (origin.y + vector.y);
                    mouse = Math.max(desired,mouse);
                    if (mouse != previous) {
                        float resize = mouse - previous;
                        window.resizeVertical(resize,resize);
                    } previous = mouse;
                } else {
                    dragging = true;
                    window.focus();
                    start = origin.y;
                    Size currentSize = window.currentSize();
                    Size desiredSize = window.desiredSize();
                    float wiggle = currentSize.height() - desiredSize.height();
                    desired = start - wiggle;
                    previous = start;
                    previous = Math.max(desired,previous);
                }
            }
        }

        public int cursorDrag() {
            return GUI.CURSOR_V_RESIZE;
        }

        public int cursorHover() {
            return GUI.CURSOR_V_RESIZE;
        }
    }

    public static final class Bottom extends WindowBorder {
        public Bottom(DynamicWindow window) {
            super(window);
        }
        public void executeOnGrab(Vector2f origin, Vector2f vector, int button) {
            if (button == Mouse.LEFT) {
                if (dragging) {
                    float mouse = (origin.y + vector.y);
                    mouse = Math.min(desired,mouse);
                    if (mouse != previous) {
                        float resize = previous - mouse;
                        window.resizeVertical(0,resize);
                    } previous = mouse;
                } else {
                    dragging = true;
                    window.focus();
                    start = origin.y;
                    Size currentSize = window.currentSize();
                    Size desiredSize = window.desiredSize();
                    float wiggle = currentSize.height() - desiredSize.height();
                    desired = start + wiggle;
                    previous = start;
                    previous = Math.min(desired,previous);
                }
            }
        }

        public int cursorDrag() {
            return GUI.CURSOR_V_RESIZE;
        }

        public int cursorHover() {
            return GUI.CURSOR_V_RESIZE;
        }
    }

    public static final class Left extends WindowBorder {
        public Left(DynamicWindow window) {
            super(window);
        }
        public void executeOnGrab(Vector2f origin, Vector2f vector, int button) {
            if (button == Mouse.LEFT) {
                if (dragging) {
                    float mouse = (origin.x + vector.x);
                    mouse = Math.min(desired,mouse);
                    if (mouse != previous) {
                        float resize = mouse - previous;
                        window.resizeHorizontal(resize,-resize);
                    } previous = mouse;
                } else {
                    dragging = true;
                    window.focus();
                    start = origin.x;
                    Size currentSize = window.currentSize();
                    Size desiredSize = window.desiredSize();
                    float wiggle = currentSize.width() - desiredSize.width();
                    desired = start + wiggle;
                    previous = start;
                    previous = Math.min(desired,previous);
                }
            }
        }

        public int cursorDrag() {
            return GUI.CURSOR_H_RESIZE;
        }

        public int cursorHover() {
            return GUI.CURSOR_H_RESIZE;
        }
    }

    public static final class Right extends WindowBorder {
        public Right(DynamicWindow window) {
            super(window);
        }
        public void executeOnGrab(Vector2f origin, Vector2f vector, int button) {
            if (button == Mouse.LEFT) {
                if (dragging) {
                    float mouse = (origin.x + vector.x);
                    mouse = Math.max(desired,mouse);
                    if (mouse != previous) {
                        float resize = mouse - previous;
                        window.resizeHorizontal(0,resize);
                    } previous = mouse;
                } else {
                    dragging = true;
                    window.focus();
                    start = origin.x;
                    Size currentSize = window.currentSize();
                    Size desiredSize = window.desiredSize();
                    float wiggle = currentSize.width() - desiredSize.width();
                    desired = start - wiggle;
                    previous = start;
                    previous = Math.max(desired,previous);
                }
            }
        }

        public int cursorDrag() {
            return GUI.CURSOR_H_RESIZE;
        }

        public int cursorHover() {
            return GUI.CURSOR_H_RESIZE;
        }
    }
}
