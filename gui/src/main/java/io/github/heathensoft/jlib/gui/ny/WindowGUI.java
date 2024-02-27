package io.github.heathensoft.jlib.gui.ny;

import io.github.heathensoft.jlib.gui.ny.gfx.RendererGUI;
import org.tinylog.Logger;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public abstract class WindowGUI {

    public static final int STATE_REGISTERED = 0x01;
    public static final int STATE_OPEN = 0x02;
    public static final int STATE_VISIBLE = 0x04;
    public static final int STATE_IN_FOCUS = 0x08;

    protected int state;
    protected String name;

    protected abstract void onRegistered();
    protected abstract void onOpen();
    protected abstract void onClose();
    protected abstract void onDestroyed();
    protected abstract void update(float dt);
    protected abstract void render(RendererGUI renderer, float dt);

    public boolean isRegistered() { return (state & STATE_REGISTERED) == STATE_REGISTERED; }
    public boolean isOpen() { return (state & STATE_OPEN) == STATE_OPEN; }
    public boolean isVisible() { return (state & STATE_VISIBLE) == STATE_VISIBLE; }
    public boolean isInFocus() { return (state & STATE_IN_FOCUS) == STATE_IN_FOCUS; }
    public String name() { return name; }

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

    public <T extends WindowGUI> T cast(Class<T> clazz) {
        if (getClass() != clazz) {
            throw new ClassCastException("GUI Window: " + name());
        } return clazz.cast(this);
    }

    /**
     * Register the GUI Window in the Window Manager if not already registered.
     * @throws Exception if another GUI Window is registered to the same name.
     * or if name is not set.
     */
    protected void register() throws Exception { GUI.windows.register(this); }

    /**
     * Open closed GUI Window.
     * Open Windows update.
     * Open Windows are rendered if visible.
     * Opening a closed GUI Window will make it visible if previously hidden.
     */
    public void open() { GUI.windows.open(this); }

    /**
     * Close open GUI Window.
     * Closed Windows won't update or render.
     * Closing an Open GUI Window will hide it if previously visible.
     */
    public void close() { GUI.windows.close(this); }

    /**
     * Show hidden GUI Window. Only works for Open windows.
     */
    public void show() { GUI.windows.show(this); }

    /**
     * Hide visible GUI Window. Only works for Open windows.
     * Hidden windows are updated but not rendered.
     */
    public void hide() { GUI.windows.hide(this); }

    /**
     * Request focus. Only works for Open windows.
     * Window is ordered to be drawn last (appear in front)
     */
    public void focus() { GUI.windows.focus(this); }

    /**
     * Dispose Window. Window will call method "onWindowDestroyed"
     * on the next Window Manager update.
     * Window will no longer be registered.
     */
    public void destroy() {  GUI.windows.destroy(this); }


    protected int windowState() { return state; }
    protected void setStateRegistered() { state |= STATE_REGISTERED; }
    protected void setStateOpen() { state |= STATE_OPEN; }
    protected void setStateVisible() { state |= STATE_VISIBLE; }
    protected void setStateInFocus() { state |= STATE_IN_FOCUS; }
    protected void clearStateRegistered() { state = (state &~ STATE_REGISTERED); }
    protected void clearStateOpen() { state = (state &~ STATE_OPEN); }
    protected void clearStateVisible() { state = (state &~ STATE_VISIBLE); }
    protected void clearStateInFocus() { state = (state &~STATE_IN_FOCUS); }
    protected void clearWindowState() { state = (0); }


}
