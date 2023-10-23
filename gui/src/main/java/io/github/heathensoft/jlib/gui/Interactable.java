package io.github.heathensoft.jlib.gui;


import static io.github.heathensoft.jlib.gui.Interactables.*;

/**
 * @author Frederik Dahl
 * 23/10/2023
 */


public interface Interactable {

    int interactableID();

    default void iReturnID() {
        if (interactableID() == 0)
            throw new IllegalStateException("no interactable id to return");
        returnID(interactableID());
    }

    default int iObtainID() {
        if (interactableID() != 0)
            throw new IllegalStateException("interactable id already obtained");
        return obtainID();
    }

    default void iYieldFocus() {
        yieldFocus(interactableID());
    }

    default void iFocus() {
        focus(interactableID());
    }

    default float iHoveredDuration() {
        return hoveredDuration();
    }

    default float iPressedDuration() {
        return pressedDuration();
    }

    default boolean iHasID() { return interactableID() != 0; }

    default boolean iHasFocus() {
        if (interactableID() == 0) return false;
        return hasFocus(interactableID());
    }

    default boolean iIsHovered() {
        if (interactableID() == 0) return false;
        return isHovered(interactableID());
    }

    default boolean iIsPressed(int button) {
        if (interactableID() == 0) return false;
        return isPressed(interactableID(),button);
    }

    default boolean iIsGrabbed() {
        if (interactableID() == 0) return false;
        return isGrabbed(interactableID());
    }



}
