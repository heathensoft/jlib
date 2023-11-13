package io.github.heathensoft.jlib.lwjgl.window;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 19/11/2022
 */


public class Mouse {

    private static final float FRAME_TIME = 0.01666667f;
    private static final float DRAG_TIME = 6 * FRAME_TIME;
    private static final int NUM_BUTTONS = 3;

    public static final int LEFT  = GLFW_MOUSE_BUTTON_LEFT;
    public static final int RIGHT = GLFW_MOUSE_BUTTON_RIGHT;
    public static final int WHEEL = GLFW_MOUSE_BUTTON_MIDDLE;

    private final float[] timer = new float[NUM_BUTTONS];
    private final boolean[] current_dragging = new boolean[NUM_BUTTONS];
    private final boolean[] last_dragging = new boolean[NUM_BUTTONS];
    private final boolean[] last_button = new boolean[NUM_BUTTONS];
    private final boolean[] current_button = new boolean[NUM_BUTTONS];
    private final boolean[] callback_button = new boolean[NUM_BUTTONS];

    private final Vector2f delta_vector = new Vector2f();
    private final Vector2f last_position = new Vector2f();
    private final Vector2f current_position = new Vector2f();
    private final Vector2f callback_position = new Vector2f();
    private final Vector2f normalized_device = new Vector2f();

    private final Vector2f[] drag_origin = new Vector2f[NUM_BUTTONS];
    private final Vector2f[] drag_vector = new Vector2f[NUM_BUTTONS];

    private boolean mouse_left_window;
    private float callback_scroll;
    private float current_scroll;

    public Mouse() {
        for (int i = 0; i < NUM_BUTTONS; i++) {
            drag_origin[i] = new Vector2f();
            drag_vector[i] = new Vector2f();
        }
    }

    public void on_activation(double x, double y) {
        last_position.set((float)x,(float)y);
        current_position.set(last_position);
        callback_position.sub(current_position);
        for (int i = 0; i < NUM_BUTTONS; i++) {
            timer[i] = 0f;
            last_button[i] = false;
            current_button[i] = false;
            current_dragging[i] = false;
            callback_button[i] = false;
            drag_vector[i].zero();
            drag_origin[i].zero();
        } callback_scroll = 0;
        current_scroll = 0;
    }

    public void process_input(float delta) {
        last_position.set(current_position);
        current_position.set(callback_position);
        delta_vector.set(current_position).sub(last_position);
        normalized_device.set(current_position).mul(2).sub(1,1);
        current_scroll = callback_scroll;
        callback_scroll = 0;
        for (int b = 0; b < NUM_BUTTONS; b++) {
            last_dragging[b] = current_dragging[b];
            last_button[b] = current_button[b];
            current_button[b] = callback_button[b];
            if (current_button[b]) {
                timer[b] += delta;
                if (!last_button[b]) {
                    drag_origin[b].set(current_position);
                    drag_vector[b].zero();
                } else {
                    Vector2f d_vec = drag_vector[b];
                    d_vec.set(current_position).sub(drag_origin[b]);
                    if (!current_dragging[b]) {
                        if (timer[b] > DRAG_TIME && drag_vector[b].length() > 0.008f) {
                            current_dragging[b] = true;
                        }
                    }
                }
            } else {
                timer[b] = 0f;
                if (current_dragging[b]) {
                    current_dragging[b] = false;
                    drag_vector[b].zero();
                }
            }
        }
    }

    public float get_scroll() {
        return current_scroll;
    }

    public Vector2f last_position() {
        return last_position;
    }

    public Vector2f position() {
        return current_position;
    }

    public Vector2f delta_vector() {
        return delta_vector;
    }

    public Vector2f ndc() {
        return normalized_device;
    }

    public Vector2f drag_vector(int button) {
        return drag_vector[button];
    }

    public boolean scrolled() {
        return current_scroll != 0;
    }

    public boolean just_clicked(int button) {
        return current_button[button] && !last_button[button];
    }

    public boolean just_released(int button) {
        return !current_button[button] && last_button[button];
    }

    public boolean button_pressed(int button) {
        return current_button[button];
    }

    public boolean is_dragging(int button) {
        return current_dragging[button];
    }

    public boolean just_started_drag(int button) {
        return current_dragging[button] && !last_dragging[button];
    }

    // same frame as just released (button)
    public boolean just_released_drag(int button) {
        return !current_dragging[button] && last_dragging[button];
    }

    // ** CALLBACKS ** //

    public void on_mouse_hover(double x, double y) {
        callback_position.set((float)x,(float)y);
    }

    public void on_mouse_press(int button, boolean press) {
        callback_button[button] = press;
    }

    public void on_mouse_scroll(float amount) {
        callback_scroll += amount;
    }

    public void on_mouse_enter(boolean enter) {
        /* no use for this yet */
    }

}
