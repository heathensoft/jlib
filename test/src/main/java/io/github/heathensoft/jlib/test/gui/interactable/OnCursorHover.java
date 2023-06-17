package io.github.heathensoft.jlib.test.gui.interactable;


import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


@FunctionalInterface
public interface OnCursorHover {

    void executeOnCursorHover(Vector2f position);
}
