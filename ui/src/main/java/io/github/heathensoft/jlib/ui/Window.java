package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.tinylog.Logger;

/**
 * @author Frederik Dahl
 * 07/03/2024
 */


public abstract class Window { // manager: recently closed list

    public static final int STATE_REGISTERED = 0x01;
    public static final int STATE_OPEN = 0x02;
    public static final int STATE_IN_FOCUS = 0x04;

    protected String name;
    /** Will gain focus automatically when window gains focus,
     * and yield focus automatically when the window lose focus.
     * Will be cleared if window is closed or terminated. */
    protected Interactable auto_focused;
    protected int window_state;
    protected int window_group;

    public final String name() { return name; }
    public final int windowGroup() { return window_group; }
    public final boolean isRegistered() { return (window_state & STATE_REGISTERED) == STATE_REGISTERED; }
    public final boolean isOpen() { return (window_state & STATE_OPEN) == STATE_OPEN; }
    public final boolean isInFocus() { return (window_state & STATE_IN_FOCUS) == STATE_IN_FOCUS; }
    public final void setAutoFocus(Interactable interactable) { auto_focused = interactable; }
    public final void clearAutoFocus() {
        if (auto_focused != null) {
            auto_focused.iYieldFocus();
            auto_focused = null;
        }
    }

    protected abstract void prepare(float dt);
    protected abstract void render(RendererGUI renderer, float dt);
    protected abstract void onInit(String name) throws Exception;
    protected abstract void onOpen();
    protected abstract void onClose();
    protected abstract void onTermination();
    protected final void onFocusGain() { if (auto_focused != null) { auto_focused.iFocus(); } }
    protected final void onFocusLoss() { if (auto_focused != null) { auto_focused.iYieldFocus(); } }

    protected final int windowState() { return window_state; }
    protected final void setStateRegistered() { window_state |= STATE_REGISTERED; }
    protected final void setStateOpen() { window_state |= STATE_OPEN; }
    protected final void setStateInFocus() { window_state |= STATE_IN_FOCUS; }
    protected final void clearStateRegistered() { window_state = (window_state &~ STATE_REGISTERED); }
    protected final void clearStateOpen() { window_state = (window_state &~ STATE_OPEN); }
    protected final void clearStateInFocus() { window_state = (window_state &~STATE_IN_FOCUS); }
    protected final void clearWindowState() { window_state = (0); }

    public <T extends Window> T cast(Class<T> clazz) {
        if (getClass() != clazz) {
            throw new ClassCastException("GUI Window: " + name());
        } return clazz.cast(this);
    }

    /**
     * Attempt to set name. Cannot set name once GUI Window is registered.
     * @param name Name to change to
     * @return name of GUI Window after call
     */
    public String setName(String name) {
        if (name == null) throw new RuntimeException("Cannot set GUI Window name to null.");
        if (this.name == null) {
            this.name = name;
        } else {
            if (isRegistered()) {
                Logger.info("GUI Window already registered.");
                Logger.info("Cannot change name of registered GUI Window.");
                Logger.info("failed to change name of GUI Window: from \"{}\" , to \"{}\"",this.name,name);
            } else {
                Logger.debug("Changed name of GUI Window: from \"{}\" , to \"{}\"",this.name,name);
                this.name = name;
            }
        } return this.name;
    }

    public void setGroup(int group) {
        if (isRegistered()) {
            Logger.warn("GUI Window already registered to group: {}", window_group);
        } else window_group = group;
    }


}
