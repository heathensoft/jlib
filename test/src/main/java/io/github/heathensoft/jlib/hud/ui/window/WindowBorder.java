package io.github.heathensoft.jlib.hud.ui.window;

import io.github.heathensoft.jlib.hud.interactable.OnGrab;
import io.github.heathensoft.jlib.hud.interactable.OnRelease;
import io.github.heathensoft.jlib.hud.interactable.UInteractable;
import io.github.heathensoft.jlib.hud.ui.Size;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public abstract class WindowBorder extends UInteractable implements OnGrab, OnRelease {

    protected final HudWindow window;
    protected float start;
    protected float desired;
    protected float previous;
    protected boolean dragging;

    protected WindowBorder(HudWindow window) {
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
            window.focus();
        }
    }

    public static final class Top extends WindowBorder {
        public Top(HudWindow window) {
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
    }

    public static final class Bottom extends WindowBorder {
        public Bottom(HudWindow window) {
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
    }

    public static final class Left extends WindowBorder {
        public Left(HudWindow window) {
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
    }

    public static final class Right extends WindowBorder {
        public Right(HudWindow window) {
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
    }
}
