package io.github.heathensoft.jlib.lwjgl.window.interactable;

import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */

@FunctionalInterface
public interface OnClick {

    void execute(Vector2f position);
}
