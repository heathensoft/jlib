package io.github.heathensoft.jlib.hud.interactable;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */

@FunctionalInterface
public interface OnCharPress {

    void executeOnCharPress(byte character);
}
