package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.utils.IDPool;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;

/**
 * @author Frederik Dahl
 * 22/10/2023
 */


public class Interactables {

    private static final int ELEMENT_NONE = 0;
    private static float hovered_duration = 0;
    private static float pressed_duration = 0;
    private static int pressed = ELEMENT_NONE;
    private static int hovered = ELEMENT_NONE;
    private static int grabbed = ELEMENT_NONE;
    private static int focused = ELEMENT_NONE;
    private static int mouse_button = -1;
    private static final IDPool id_pool = new IDPool();
    static { id_pool.obtainID(); }

    public static void refresh(int current, float dt) {
        Mouse mouse = Engine.get().input().mouse();
        if (pressed > ELEMENT_NONE) {
            if (mouse.button_pressed(mouse_button)) {
                pressed_duration += dt;
                if (mouse.is_dragging(mouse_button))
                    grabbed = pressed;
            } else { pressed_duration = 0f;
                pressed = ELEMENT_NONE;
                grabbed = ELEMENT_NONE;
                mouse_button = -1; }
        } else { if (current > ELEMENT_NONE) {
                for (int button = 0; button < 3; button++) {
                    if (mouse.just_clicked(button)) {
                        mouse_button = button;
                        pressed = current;
                        break;
                    }
                }
            }
        }
        if (hovered == current) {
            if (current > ELEMENT_NONE)
                hovered_duration += dt;
        } else { hovered = current;
            hovered_duration = 0f;
        }
    }

    protected static void yieldFocus(int id) { if (focused == id) focused = ELEMENT_NONE; }

    protected static void focus(int id) { focused = id; }

    protected static boolean isHovered(int id) { return id == hovered; }

    protected static boolean isPressed(int id, int button) { return id == pressed && button == mouse_button; }

    protected static boolean isGrabbed(int id) { return id == grabbed; }

    protected static float hoveredDuration() { return hovered_duration; }

    protected static float pressedDuration() { return pressed_duration; }

    protected static boolean hasFocus(int id) { return focused == id; }

    protected static int obtainID() { return id_pool.obtainID(); }

    protected static void returnID(int id) {
        if (id > 0) { id_pool.returnID(id); }
        if (pressed == id) pressed = ELEMENT_NONE;
        if (grabbed == id) grabbed = ELEMENT_NONE;
        if (focused == id) focused = ELEMENT_NONE;
    }


}
