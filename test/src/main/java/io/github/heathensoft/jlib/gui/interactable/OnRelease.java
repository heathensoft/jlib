package io.github.heathensoft.jlib.gui.interactable;


/**
 * @author Frederik Dahl
 * 27/11/2022
 */


@FunctionalInterface
public interface OnRelease {

    void executeOnRelease(int button);
}
