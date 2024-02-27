package io.github.heathensoft.jlib.gui.ny;

import io.github.heathensoft.jlib.common.utils.IDPool;
import io.github.heathensoft.jlib.lwjgl.window.CursorObjects;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public class StateGUI {

    private final int ELEMENT_NONE = 0;
    private float hovered_duration = 0;
    private float pressed_duration = 0;
    private int desired_cursor_icon = CursorObjects.CURSOR_DEFAULT;
    private int current_cursor_icon = CursorObjects.CURSOR_DEFAULT;
    private int pressed_element = ELEMENT_NONE;
    private int hovered_element = ELEMENT_NONE;
    private int grabbed_element = ELEMENT_NONE;
    private int focused_element = ELEMENT_NONE;
    private int last_pressed_element = ELEMENT_NONE;
    private int last_hovered_element = ELEMENT_NONE;
    private int last_grabbed_element = ELEMENT_NONE;
    private int active_mouse_button = -1;
    private int active_mouse_button_last = -1;
    private boolean mouse_grab_this_frame;
    private boolean mouse_grab_last_frame;
    private boolean pause_processing;
    private final IDPool id_pool = new IDPool();

    StateGUI() { id_pool.obtainID(); }

    public void update(int pixelID, float dt) {
        if (pause_processing) pixelID = ELEMENT_NONE;
        if (current_cursor_icon != desired_cursor_icon) {
            current_cursor_icon = desired_cursor_icon;
            Engine.get().window().cursorObjects().useCursor(current_cursor_icon);
        } Mouse mouse = Engine.get().input().mouse();
        last_hovered_element = hovered_element;
        last_pressed_element = pressed_element;
        last_grabbed_element = grabbed_element;
        active_mouse_button_last = active_mouse_button;
        mouse_grab_last_frame = mouse_grab_this_frame;
        if (active_mouse_button == -1) { // If not currently pressing
            for (int button = 0; button < 3; button++) {
                if (mouse.just_clicked(button)) {
                    active_mouse_button = button;
                    pressed_element = pixelID;
                    if (pressed_element == ELEMENT_NONE) {
                        focused_element = ELEMENT_NONE;
                    } break;
                }
            }
        } else if (mouse.button_pressed(active_mouse_button)) {
            pressed_duration += dt;
            if (mouse.is_dragging(active_mouse_button)) {
                grabbed_element = pressed_element;
                mouse_grab_this_frame = true; }
        } else { mouse_grab_this_frame = false;
            pressed_duration = 0f;
            pressed_element = ELEMENT_NONE;
            grabbed_element = ELEMENT_NONE;
            active_mouse_button = -1;
        } if (hovered_element == pixelID) {
            if (pixelID > ELEMENT_NONE)
                hovered_duration += dt;
        } else { hovered_element = pixelID;
            hovered_duration = 0f;
        } if (hovered_element > ELEMENT_NONE || pressed_element > ELEMENT_NONE) {
            desired_cursor_icon = CursorObjects.CURSOR_HAND;
        } else desired_cursor_icon = CursorObjects.CURSOR_ARROW;
    }

    public void useCursorIcon(int slot) {
        if (Engine.get().window().cursorObjects().validCursor(slot)) {
            desired_cursor_icon = slot;
        }

    }

    public float hoveredDuration() { return hovered_duration; }
    public float pressedDuration() { return pressed_duration; }

    public void pauseWindowsEventProcessing() { pause_processing = true; }
    public void unpauseWindowEventProcessing() { pause_processing = false; }

    public boolean anyInteractableFocused() { return focused_element != ELEMENT_NONE; }
    public boolean anyInteractableHovered() { return hovered_element != ELEMENT_NONE; }
    public boolean anyInteractablePressed() { return pressed_element != ELEMENT_NONE; }

    public boolean mouseIsPressed() { return (active_mouse_button != -1); }
    public boolean mouseIsPressed(int button) { return (active_mouse_button == button); }
    public boolean mouseJustPressed(int button) { return mouseIsPressed(button) && (active_mouse_button_last != button); }
    public boolean mouseJustPressed() { return mouseIsPressed() && (active_mouse_button_last == -1); }
    public boolean mouseJustReleased(int button) { return (active_mouse_button == -1) && (active_mouse_button_last == button); }
    public boolean mouseJustReleased() { return (active_mouse_button == -1) && (active_mouse_button_last != -1); }
    public boolean mouseIsDragging() { return mouse_grab_this_frame; }
    public boolean mouseIsDragging(int button) { return mouseIsDragging() && mouseIsPressed(button); }

    public boolean worldHovered() { return hovered_element == ELEMENT_NONE; }
    public boolean worldJustHovered() { return worldHovered() && last_hovered_element != ELEMENT_NONE; }
    public boolean worldPressed(int button) { return pressed_element == ELEMENT_NONE && button == active_mouse_button; }
    public boolean worldPressed() { return pressed_element == ELEMENT_NONE && active_mouse_button != -1; }
    public boolean worldJustPressed(int button) { return worldPressed(button) && mouseJustPressed(button); }
    public boolean worldJustPressed() { return worldPressed() && mouseJustPressed(); }
    public boolean worldJustReleased(int button) { return last_pressed_element == ELEMENT_NONE && mouseJustReleased(button); }
    public boolean worldJustReleased() { return last_pressed_element == ELEMENT_NONE && mouseJustReleased(); }
    public boolean worldIsGrabbed(int button) {return worldIsGrabbed() && (active_mouse_button == button); }
    public boolean worldIsGrabbed() { return grabbed_element == ELEMENT_NONE && mouse_grab_this_frame; }
    public boolean worldJustReleasedFromGrab() { return last_grabbed_element == ELEMENT_NONE && mouse_grab_last_frame &! mouse_grab_this_frame; }
    public boolean worldJustReleasedFromGrab(int button) { return worldJustReleasedFromGrab() && active_mouse_button_last == button; }

    void focus(int id) { focused_element = id; }
    void yieldFocus(int id) { if (focused_element == id) focused_element = ELEMENT_NONE; }
    boolean hasFocus(int id) { return focused_element == id; }
    boolean isHovered(int id) { return id == hovered_element; }
    boolean justHovered(int id) { return isHovered(id) && last_hovered_element != id; }
    boolean isPressed(int id, int button) { return id == pressed_element && button == active_mouse_button; }
    boolean isPressed(int id) { return id == pressed_element; }
    boolean justPressed(int id, int button) { return isPressed(id,button) && (id != last_pressed_element); }
    boolean justPressed(int id) { return isPressed(id) && (id != last_pressed_element); }
    boolean isGrabbed(int id) { return id == grabbed_element; }
    boolean isGrabbed(int id, int button) { return isGrabbed(id) && button == active_mouse_button; }
    boolean justGrabbed(int id) { return isGrabbed(id) && id != last_grabbed_element; }
    boolean justGrabbed(int id, int button) { return justGrabbed(id) && active_mouse_button == button; }
    boolean justReleased(int id) { return !isPressed(id) && last_pressed_element == id; }
    boolean justReleased(int id, int button) { return justReleased(id) && active_mouse_button_last == button;}
    boolean justReleasedFromGrab(int id) { return !isGrabbed(id) && last_grabbed_element == id; }
    boolean justReleasedFromGrab(int id, int button) { return justReleasedFromGrab(id) && active_mouse_button_last == button; }
    boolean clickedNotGrabbed(int id, int button) { return clicked(id, button) &! justReleasedFromGrab(id); }
    boolean clicked(int id, int button) { return isHovered(id) && justReleased(id,button); }

    int obtainID() { return id_pool.obtainID(); }

    void returnID(int id) {
        if (id > 0) {
            id_pool.returnID(id);
            if (pressed_element == id) pressed_element = ELEMENT_NONE;
            if (grabbed_element == id) grabbed_element = ELEMENT_NONE;
            if (focused_element == id) focused_element = ELEMENT_NONE;
        }
    }

}
