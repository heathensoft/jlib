package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 17/09/2023
 */


public abstract class PTextField extends PColored implements TextProcessor {


    private int pointer;

    protected PTextField(String text, Color32 color) {
        super(text, color);
        pointer = length() - 1;
        pointer = Math.max(0,pointer);
    }


    public void npcPress(int key) {
        if (!isBlank()) {
            if (key == GLFW_KEY_ENTER) {
                onEnter(toString());
            } else if (key == GLFW_KEY_BACKSPACE) {
                if (pointer != 0) {
                    StringBuilder sb = new StringBuilder(toString());

                }
            }
        }

    }

    public void npcRelease(int key) {

    }

    public void printable(byte character) {

    }

    protected abstract void onEnter(String value);


    protected abstract void onEdit(String value);
}
