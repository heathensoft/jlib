package io.github.heathensoft.jlib.test.gui.interactable;


import io.github.heathensoft.jlib.test.gui.GUI;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


public abstract class CoreInteractable extends Interactable {

    private final static OnCursorHover ON_CURSOR_HOVER = (position) -> {};
    private final static OnCursorLeave ON_CURSOR_LEAVE = () -> {};
    private final static OnCharPress ON_CHAR_PRESS = character -> {};
    private final static OnKeyPress ON_KEY_PRESS = key -> {};
    private final static OnRelease ON_RELEASE = button -> {};
    private final static OnScroll ON_SCROLL = amount -> {};
    private final static OnClick ON_CLICK = (position,button) -> {};
    private final static OnGrab ON_GRAB = (origin, vector, button) -> {};

    protected OnCursorHover onCursorHover = ON_CURSOR_HOVER;
    protected OnCursorLeave onCursorLeave = ON_CURSOR_LEAVE;
    protected OnCharPress onCharPress = ON_CHAR_PRESS;
    protected OnKeyPress onKeyPress = ON_KEY_PRESS;
    protected OnRelease onRelease = ON_RELEASE;
    protected OnScroll onScroll = ON_SCROLL;
    protected OnClick onClick = ON_CLICK;
    protected OnGrab onGrab = ON_GRAB;

    public int cursorHover() {
        return GUI.CURSOR_ARROW;
    }

    public int cursorDrag() {
        return GUI.CURSOR_ARROW;
    }

    public void setOnCursorHover(OnCursorHover func) {
        this.onCursorHover = func;
    }

    public void setOnCursorLeave(OnCursorLeave func) {
        this.onCursorLeave = func;
    }

    public void setOnCharPress(OnCharPress func) {
        this.onCharPress = func;
    }

    public void setOnKeyPress(OnKeyPress func) {
        this.onKeyPress = func;
    }

    public void setOnRelease(OnRelease func) {
        this.onRelease = func;
    }

    public void setOnScroll(OnScroll onScroll) {
        this.onScroll = onScroll;
    }

    public void setOnClick(OnClick func) {
        this.onClick = func;
    }

    public void setOnGrab(OnGrab func) {
        this.onGrab = func;
    }

    public final void onCursorHover(Vector2f position) {
        this.onCursorHover.executeOnCursorHover(position);
    }

    public final void onCursorLeave() {
        this.onCursorLeave.executeOnCursorLeave();
    }

    public final void onCharPress(byte character) {
        this.onCharPress.executeOnCharPress(character);
    }

    public final void onKeyPress(int key) {
        this.onKeyPress.executeOnKeyPress(key);
    }

    public final void onRelease(int button) {
        this.onRelease.executeOnRelease(button);
    }

    public final void onScroll(float amount) {
        this.onScroll.executeOnScroll(amount);
    }

    public final void onClick(Vector2f position, int button) {
        this.onClick.executeOnClick(position,button);
    }

    public final void onGrab(Vector2f origin, Vector2f vector, int button) {
        this.onGrab.executeOnGrab(origin,vector,button);
    }


}
