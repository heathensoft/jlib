package io.github.heathensoft.jlib.lwjgl.window.interactable;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */

@FunctionalInterface
public interface OnKeyPress {

    void execute(int key);
}
