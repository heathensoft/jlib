package io.github.heathensoft.jlib.lwjgl.window;


import java.util.List;

/**
 * @author Frederik Dahl
 * 13/10/2022
 */


public abstract class InputProcessor {
    
    /**
     * @param x mouse x at the moment of activation
     * @param y mouse y at the moment of activation
     */
    protected void on_activation(double x, double y) {}
    
    protected void on_char_press(int codepoint) {}
    
    /**
     * Called when a keyboard key was initially pressed or released.
     * The keys' value correspond to the GLFW key-layout, but is negative
     * when released. So use abs(key) to get the GLFW equivalent.
     * @param key the key value
     */
    protected void on_key_event(int key, int mods) {}
    
    /**
     * Called when the cursor hovers over the window.
     * These values are already converted from screen space by the Window,
     * as the Application only need to know the position in viewport space.
     * Use the Application's current resolution (or virtual window) to convert
     * the position from normalized viewport to viewport: i.e. (x * res.width() and y * res.height()).
     * OR to get normalized device coordinates: 2 * x - 1 and 2 * y - 1.
     *
     * @param x mouse x position in normalized viewport-space
     * @param y mouse y position in normalized viewport-space
     */
    protected void on_mouse_hover(double x, double y) {}
    
    protected void on_mouse_press(int button, boolean press) {}
    
    protected void on_mouse_scroll(float amount) {}
    
    protected void on_mouse_enter(boolean enter) {}

    protected void on_file_drop(List<String> droppedFiles) {}

    protected void controller_connect(int id, boolean connect) {}

    protected void process_input(float delta) {}


    
}
