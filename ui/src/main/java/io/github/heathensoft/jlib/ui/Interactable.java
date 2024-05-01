package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;

import java.util.List;

import static io.github.heathensoft.jlib.ui.GUI.*;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public interface Interactable extends Disposable {

    final class Instance implements Interactable {  private final int id;
        public Instance() {
            id = iObtainID();
        } public int interactableID() { return id; }
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

    default boolean iHasFocus() { return state.hasFocus(interactableID()); }

    default boolean iAnyInteractableFocused() {
        return state.anyInteractableFocused();
    }

    default boolean iHovered() { return state.isHovered(interactableID()); }

    default boolean iAnyInteractableHovered() {
        return state.anyInteractableHovered();
    }

    default boolean iPressed(int button) { return state.isPressed(interactableID(),button); }

    default boolean iPressed() { return state.isPressed(interactableID()); }

    default boolean iJustPressed(int button) { return state.justPressed(interactableID(),button); }

    default boolean iJustPressed() { return state.justPressed(interactableID()); }

    default boolean iJustReleased(int button) { return state.justReleased(interactableID(),button); }

    default boolean iJustReleased() { return state.justReleased(interactableID()); }

    default boolean iClicked(int button) { return state.clicked(interactableID(),button); }

    default boolean iClickedNotGrabbed(int button) { return state.clickedNotGrabbed(interactableID(),button); }

    default boolean iDoubleClicked(int button) { return state.doubleClicked(interactableID(),button); }

    default boolean iAnyInteractablePressed() {
        return state.anyInteractablePressed();
    }

    default boolean iAnyInteractableDragged() { return state.anyInteractableGrabbed(); }

    default boolean iGrabbed() { return state.isGrabbed(interactableID()); }

    default boolean iGrabbed(int button) { return state.isGrabbed(interactableID(),button); }

    default boolean iJustGrabbed(int button) { return state.justGrabbed(interactableID(),button); }

    default boolean iJustGrabbed() { return state.justGrabbed(interactableID()); }

    default void iRegisterAsConsumer() {
        state.registerAsConsumer(this);
    }

    default void iUnRegisterAsConsumer() { state.unRegisterConsumer(this); }

    default Interactable iSearchForItemConsumer() { return state.searchForItemConsumer(this); }
    /**
     * Attempt to "Drop" this item in the GUI and see if any "Consumer" accepts the "Drop"
     * @return whether the "Drop" was accepted
     */
    default boolean iAttemptItemDrop() { return state.attemptItemDrop(this); }

    default boolean iAcceptItemDrop(Interactable drop) { return false; }

    default <T extends Interactable>boolean iAcceptItemDrop(List<T> drops) { return false; }

    default void iOnConsumerHovered(Interactable drop) { }

    default void dispose() { if (iHasID()) iReturnID(); }

}
