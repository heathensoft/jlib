package io.github.heathensoft.jlib.lwjgl.window.interactable;


import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


public abstract class CoreInteractable extends Interactable {

    private final static OnCursorHover ON_CURSOR_HOVER = () -> {};
    private final static OnCursorLeave ON_CURSOR_LEAVE = () -> {};
    private final static OnCharPress ON_CHAR_PRESS = character -> {};
    private final static OnKeyPress ON_KEY_PRESS = key -> {};
    private final static OnRelease ON_RELEASE = () -> {};
    private final static OnClick ON_CLICK = position -> {};
    private final static OnGrab ON_GRAB = (origin, vector) -> {};

    protected OnCursorHover onCursorHover = ON_CURSOR_HOVER;
    protected OnCursorLeave onCursorLeave = ON_CURSOR_LEAVE;
    protected OnCharPress onCharPress = ON_CHAR_PRESS;
    protected OnKeyPress onKeyPress = ON_KEY_PRESS;
    protected OnRelease onRelease = ON_RELEASE;
    protected OnClick onClick = ON_CLICK;
    protected OnGrab onGrab = ON_GRAB;

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

    public void setOnClick(OnClick func) {
        this.onClick = func;
    }

    public void setOnGrab(OnGrab func) {
        this.onGrab = func;
    }

    public final void onCursorHover() {
        this.onCursorHover.execute();
    }

    public final void onCursorLeave() {
        this.onCursorLeave.execute();
    }

    public final void onCharPress(byte character) {
        this.onCharPress.execute(character);
    }

    public final void onKeyPress(int key) {
        this.onKeyPress.execute(key);
    }

    public final void onRelease() {
        this.onRelease.execute();
    }

    public final void onClick(Vector2f position) {
        this.onClick.execute(position);
    }

    public final void onGrab(Vector2f origin, Vector2f vector) {
        this.onGrab.execute(origin,vector);
    }
}
