package io.github.heathensoft.jlib.lwjgl.window;

import java.util.LinkedList;
import java.util.List;

/**
 * Default input theme with common mouse and keyboard functionality.
 *
 * @author Frederik Dahl
 * 20/11/2022
 */


public class DefaultInput extends InputProcessor {

    private final Mouse mouse;
    private final Keyboard keyboard;
    private final List<String> droppedFiles;

    protected DefaultInput() {
        this.droppedFiles = new LinkedList<>();
        this.keyboard = new Keyboard();
        this.mouse = new Mouse();
    }

    public Keyboard keys() {
        return keyboard;
    }

    public Mouse mouse() {
        return mouse;
    }

    public List<String> droppedFiles() { return droppedFiles; }

    public boolean anyFilesDropped() { return !droppedFiles.isEmpty(); }

    protected void on_activation(double x, double y) {
        mouse.on_activation(x, y);
    }

    protected void on_char_press(int codepoint) {
        keyboard.on_char_press(codepoint);
    }

    protected void on_key_event(int key, int mods, int action) {
        keyboard.on_key_event(key,mods,action);
    }

    protected void on_mouse_hover(double x, double y) {
        mouse.on_mouse_hover(x, y);
    }

    protected void on_mouse_press(int button, boolean press) {
        if (button >= 0 && button <= 3)  mouse.on_mouse_press(button, press);
    }

    protected void on_mouse_scroll(float amount) {
        mouse.on_mouse_scroll(amount);
    }

    protected void on_mouse_enter(boolean enter) {
        mouse.on_mouse_enter(enter);
    }

    protected void on_file_drop(List<String> droppedFiles) {
        this.droppedFiles.clear();
        this.droppedFiles.addAll(droppedFiles);
    }

    protected void controller_connect(int id, boolean connect) {
        super.controller_connect(id, connect);
    }

    protected void process_input(float delta) {
        keyboard.process_input();
        mouse.process_input(delta);
    }
}
