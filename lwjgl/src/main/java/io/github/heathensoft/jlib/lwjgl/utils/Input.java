package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.InputProcessor;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;

/**
 * Default input theme with common mouse and keyboard functionality.
 * Initializing this will make it the current input processor
 *
 * @author Frederik Dahl
 * 20/11/2022
 */


public class Input extends InputProcessor {

    private static Input instance;

    private final Mouse mouse;
    private final Keyboard keyboard;

    private Input() {
        this.keyboard = new Keyboard();
        this.mouse = new Mouse();
    }

    public static void initialize() {
        if (instance == null) instance = new Input();
        Engine.get().window().setInputProcessor(instance);
    }

    public static Input get() {
        return instance;
    }

    public Keyboard keyboard() {
        return keyboard;
    }

    public Mouse mouse() {
        return mouse;
    }

    @Override
    protected void on_activation(double x, double y) {
        mouse.on_activation(x, y);
    }

    @Override
    protected void on_char_press(int codepoint) {
        keyboard.on_char_press(codepoint);
    }

    @Override
    protected void on_key_event(int key) {
        keyboard.on_key_event(key);
    }

    @Override
    protected void on_mouse_hover(double x, double y) {
        mouse.on_mouse_hover(x, y);
    }

    @Override
    protected void on_mouse_press(int button, boolean press) {
        mouse.on_mouse_press(button, press);
    }

    @Override
    protected void on_mouse_scroll(float amount) {
        mouse.on_mouse_scroll(amount);
    }

    @Override
    protected void on_mouse_enter(boolean enter) {
        mouse.on_mouse_enter(enter);
    }

    @Override
    protected void controller_connect(int id, boolean connect) {
        super.controller_connect(id, connect);
    }

    @Override
    protected void process_input(float delta) {
        keyboard.process_input();
        mouse.process_input(delta);
    }
}
