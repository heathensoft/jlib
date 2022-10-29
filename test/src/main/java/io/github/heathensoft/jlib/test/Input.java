package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.InputProcessor;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * @author Frederik Dahl
 * 29/10/2022
 */


public class Input extends InputProcessor {
    
    @Override
    protected void on_key_event(int key) {
        if (key == GLFW_KEY_ESCAPE) Engine.get().exit();
    }
}
