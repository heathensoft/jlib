package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import org.tinylog.Logger;

import static io.github.heathensoft.jlib.ui.GUI.*;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public interface Interactable extends Disposable {

    final class Instance implements Interactable {
        private int id;
        public Instance() {
            id = iObtainID();
        } public int interactableID() { return id; }
        public void dispose() {
            Interactable.super.dispose();
            id = 0;
        }
    }

    default int interactableID() { return 0; }

    default void iReturnID() {
        if (interactableID() == 0)
            throw new IllegalStateException("no interactable id to return");
        state.returnID(interactableID());
    }

    default int iObtainID() {
        if (interactableID() != 0)
            throw new IllegalStateException("interactable id already obtained");
        return state.obtainID();
    }

    default int iObtainIDAndRegisterAsConsumer() {
        if (interactableID() != 0)
            throw new IllegalStateException("interactable id already obtained");
        return state.obtainIDAAndRegisterAsConsumer(this);
    }

    default void iSetCursorIcon(int slot) { state.useCursorIcon(slot); }

    default void iYieldFocus() { state.yieldFocus(interactableID()); }

    default void iFocus() { state.focus(interactableID()); }

    default float iHoveredDuration() { return state.hoveredDuration(); }

    default float iPressedDuration() { return state.pressedDuration(); }

    default boolean iHasID() { return interactableID() != 0; }

    default boolean iHasFocus() {
        if (interactableID() == 0) return false;
        return state.hasFocus(interactableID());
    }

    default boolean iAnyInteractableFocused() {
        return state.anyInteractableFocused();
    }

    default boolean iHovered() {
        if (interactableID() == 0) return false;
        return state.isHovered(interactableID());
    }

    default boolean iAnyInteractableHovered() {
        return state.anyInteractableHovered();
    }

    default boolean iPressed(int button) {
        if (interactableID() == 0) return false;
        return state.isPressed(interactableID(),button);
    }

    default boolean iPressed() {
        if (interactableID() == 0) return false;
        return state.isPressed(interactableID());
    }

    default boolean iJustPressed(int button) {
        if (interactableID() == 0) return false;
        return state.justPressed(interactableID(),button);
    }

    default boolean iJustPressed() {
        if (interactableID() == 0) return false;
        return state.justPressed(interactableID());
    }

    default boolean iJustReleased(int button) {
        if (interactableID() == 0) return false;
        return state.justReleased(interactableID(),button);
    }

    default boolean iJustReleased() {
        if (interactableID() == 0) return false;
        return state.justReleased(interactableID());
    }

    default boolean iClicked(int button) {
        if (interactableID() == 0) return false;
        return state.clicked(interactableID(),button);
    }

    default boolean iClickedNotGrabbed(int button) {
        if (interactableID() == 0) return false;
        return state.clickedNotGrabbed(interactableID(),button);
    }

    default boolean iAnyInteractablePressed() {
        return state.anyInteractablePressed();
    }

    default boolean iGrabbed() {
        if (interactableID() == 0) return false;
        return state.isGrabbed(interactableID());
    }

    default boolean iGrabbed(int button) {
        if (interactableID() == 0) return false;
        return state.isGrabbed(interactableID(),button);
    }

    default boolean iJustGrabbed(int button) {
        if (interactableID() == 0) return false;
        return state.justGrabbed(interactableID(),button);
    }

    default boolean iJustGrabbed() {
        if (interactableID() == 0) return false;
        return state.justGrabbed(interactableID());
    }

    default void iRegisterAsConsumer() {
        state.registerAsConsumer(this);
    }

    default void iUnRegisterAsConsumer() {
        state.unRegisterConsumer(this);
    }

    default Interactable iFindValidConsumer(int consumerID) {
        if (interactableID() == 0 || consumerID == 0) return null;
        return state.findValidConsumer(this,consumerID);
    }

    default boolean iIsValidDrop(Interactable drop) {
        return false;
    }

    default void dispose() { if (iHasID()) iReturnID(); }

}
