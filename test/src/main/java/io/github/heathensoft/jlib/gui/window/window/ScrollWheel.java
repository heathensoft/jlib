package io.github.heathensoft.jlib.gui.window.window;

import io.github.heathensoft.jlib.gui.interactable.OnGrab;
import io.github.heathensoft.jlib.gui.interactable.OnRelease;
import io.github.heathensoft.jlib.gui.interactable.OnScroll;
import io.github.heathensoft.jlib.gui.interactable.UInteractable;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 26/12/2022
 */


public class ScrollWheel extends UInteractable implements OnScroll, OnGrab, OnRelease {

    private final ScrollableBox<?> scrollableBox;
    private float window_drag_origin;
    private boolean dragging;

    public ScrollWheel(ScrollableBox<?> scrollableBox) {
        this.scrollableBox = scrollableBox;
        setOnGrab(this);
        setOnScroll(this);
        setOnRelease(this);
        iRegisterInteractable();
    }

    public void dispose() {
        iRemoveInteractable();
    }

    public void executeOnGrab(Vector2f origin, Vector2f vector, int button) {
        if (button == Mouse.LEFT) {
            if (dragging) {
                VirtualWindow virtualWindow = scrollableBox.content();
                float scroll_delta_pixels = virtualWindow.scrollDelta();
                float scroll_delta_relative = scroll_delta_pixels * scrollableBox.scrollbarSizeNormalized();
                int ticks_from_origin = (int)(-vector.y / scroll_delta_relative);
                float wanted_win_pos = window_drag_origin + (ticks_from_origin * scroll_delta_pixels);
                float max = virtualWindow.currentSize().height() - scrollableBox.windowHeight();
                wanted_win_pos = Math.min(max,Math.max(0,wanted_win_pos));
                float actual_win_pos = scrollableBox.windowPosition();
                float dy = (actual_win_pos - wanted_win_pos) / scroll_delta_pixels;
                scrollableBox.onScroll(-dy);
            } else {
                dragging = true;
                window_drag_origin = scrollableBox.windowPosition();
            }
        }
    }

    public void executeOnRelease(int button) {
        if (button == Mouse.LEFT) {
            dragging = false;
        }
    }

    public void executeOnScroll(float amount) {
        scrollableBox.onScroll(-amount);
    }
}
