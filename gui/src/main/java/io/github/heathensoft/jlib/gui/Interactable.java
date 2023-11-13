package io.github.heathensoft.jlib.gui;


import static io.github.heathensoft.jlib.gui.GUI.State.*;

/**
 * @author Frederik Dahl
 * 23/10/2023
 */


public interface Interactable {

    default int interactableID() { return 0; }

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

    default void iSetCursorIcon(int index) { setCursorIcon(index); }

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

    default boolean iAnyInteractableFocused() {
        return anyInteractableFocused();
    }

    default boolean iHovered() {
        if (interactableID() == 0) return false;
        return isHovered(interactableID());
    }

    default boolean iAnyInteractableHovered() {
        return anyInteractableHovered();
    }

    default boolean iPressed(int button) {
        if (interactableID() == 0) return false;
        return isPressed(interactableID(),button);
    }

    default boolean iPressed() {
        if (interactableID() == 0) return false;
        return isPressed(interactableID());
    }

    default boolean iJustPressed(int button) {
        if (interactableID() == 0) return false;
        return justPressed(interactableID(),button);
    }

    default boolean iJustPressed() {
        if (interactableID() == 0) return false;
        return justPressed(interactableID());
    }

    default boolean iJustReleased(int button) {
        if (interactableID() == 0) return false;
        return justReleased(interactableID(),button);
    }

    default boolean iJustReleased() {
        if (interactableID() == 0) return false;
        return justReleased(interactableID());
    }

    default boolean iClicked(int button) {
        if (interactableID() == 0) return false;
        return clicked(interactableID(),button);
    }

    default boolean iClickedNotGrabbed(int button) {
        if (interactableID() == 0) return false;
        return clickedNotGrabbed(interactableID(),button);
    }

    default boolean iAnyInteractablePressed() { return anyInteractablePressed(); }

    default boolean iGrabbed() {
        if (interactableID() == 0) return false;
        return isGrabbed(interactableID());
    }

    default boolean iGrabbed(int button) {
        if (interactableID() == 0) return false;
        return isGrabbed(interactableID(),button);
    }

    default boolean iJustGrabbed(int button) {
        if (interactableID() == 0) return false;
        return justGrabbed(interactableID(),button);
    }

    default boolean iJustGrabbed() {
        if (interactableID() == 0) return false;
        return justGrabbed(interactableID());
    }


}
