package io.github.heathensoft.jlib.gui.ny;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.ny.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public class WindowsGUI implements Disposable {

    private final Map<String,WindowGUI> windows_by_name;
    private final LinkedList<WindowGUI> windows_to_open;
    private final LinkedList<WindowGUI> windows_to_close;
    private final LinkedList<WindowGUI> windows_to_destroy;
    private final LinkedList<WindowGUI> windows_to_render;
    private final LinkedList<WindowGUI> windows_closed;
    private final LinkedList<WindowGUI> windows_open;
    private boolean show_windows;
    private boolean preparing;
    private boolean rendering;


    WindowsGUI() {
        windows_by_name = new HashMap<>();
        windows_to_open = new LinkedList<>();
        windows_to_close = new LinkedList<>();
        windows_to_destroy = new LinkedList<>();
        windows_to_render = new LinkedList<>();
        windows_closed = new LinkedList<>();
        windows_open = new LinkedList<>();
        show_windows = true;
    }

    protected void update(float dt) {
        assert_manager_not_busy();
        preparing = true;
        while (!windows_to_open.isEmpty()) {
            WindowGUI window = windows_to_open.removeLast();
            windows_open.addFirst(window);
            window.setStateOpen();
            window.setStateVisible();
            window.onOpen();
        }
        while (!windows_to_close.isEmpty()) {
            WindowGUI window = windows_to_close.removeLast();
            windows_closed.addFirst(window);
            window.clearStateInFocus();
            window.clearStateOpen();
            window.clearStateVisible();
            window.onClose();
        }
        while (!windows_to_destroy.isEmpty()) {
            WindowGUI window = windows_to_destroy.getLast();
            String key = window.name();
            if (window.isOpen()) windows_open.remove(window);
            else windows_closed.remove(window);
            if (!windows_by_name.remove(key,window))
                throw new RuntimeException("GUI Window not in map of names");
            window.clearWindowState();
            window.onDestroyed();
        }
        windows_to_render.clear();
        int index = 0;
        for (WindowGUI window : windows_open) {
            if (window.isVisible()) {
                if (index == 0) window.setStateInFocus();
                else window.clearStateInFocus();
                // first added here should be last to render
                windows_to_render.addFirst(window);
            } else window.clearStateInFocus();
            window.update(dt);
            index++;
        } preparing = false;
    }

    protected void render(RendererGUI renderer, float dt) {
        assert_manager_not_busy();
        rendering = true;
        if (show_windows) {
            Resolution resolution = Engine.get().window().appResolution();
            while (!windows_to_render.isEmpty()) {
                WindowGUI window = windows_to_render.removeFirst();
                window.render(renderer, dt);
            }
        } rendering = false;
    }

    public <T extends WindowGUI> Optional<T> getByName(String key, Class<T> clazz) {
        WindowGUI window = windows_by_name.get(key);
        if (window != null) {
            if (window.getClass() != clazz) {
                throw new ClassCastException("GUI Window: " + window.name());
            } return Optional.of(clazz.cast(window));
        } return Optional.empty();
    }

    public Optional<WindowGUI> getByName(String key) {
        WindowGUI window = windows_by_name.get(key);
        return window == null ? Optional.empty() : Optional.of(window);
    }

    public void showWindows() { show_windows = true; }

    public void hideWindows() { show_windows = false; }

    public void closeAll() {
        while (!windows_open.isEmpty()) {
            windows_to_close.addFirst(windows_open.removeLast());
        }
    }

    protected void register(WindowGUI window) throws Exception {
        String key = window.name();
        if (key == null) throw new Exception("Attempt to register unnamed GUI Window");
        if (window.isRegistered()) {
            Logger.info("GUI Window \"{}\" already registered",key);
        } else { WindowGUI existing = windows_by_name.putIfAbsent(key,window);
            if (existing == null) { // No windows associated with that name
                Logger.debug("Registering GUI Window: \"{}\"",key);
                window.clearWindowState();
                window.setStateRegistered();
                windows_closed.addFirst(window);
                window.onRegistered();
            } else if (existing != window) { // Name associated with another window
                throw new Exception("Another GUI Window already registered with name: " + key);
            } else { // Window is already registered. This should never happen
                throw new IllegalStateException("GUI Window in map but not registered");
            }
        }
    }

    protected void open(WindowGUI window) {
        assert_window_registered(window);
        if (!window.isOpen()) {
            if (windows_closed.remove(window)) {
                windows_to_open.addFirst(window);
            }
        }
    }

    protected void close(WindowGUI window) {
        assert_window_registered(window);
        if (window.isOpen()) {
            if (windows_open.remove(window)) {
                windows_to_close.addFirst(window);
            }
        }
    }

    protected void show(WindowGUI window) {
        assert_window_registered(window);
        if (window.isOpen()) window.setStateVisible();
    }

    protected void hide(WindowGUI window) {
        assert_window_registered(window);
        if (window.isOpen()) {
            window.clearStateInFocus();
            window.clearStateVisible();
        }
    }

    /**
     * Sets the window up to be rendered last (appears in front).
     * But only if the window is open.
     * @param window window
     */
    protected void focus(WindowGUI window) {
        assert_window_registered(window);
        if (window.isOpen()) {
            // If window not queued to close
            if (!windows_to_close.contains(window)) {
                // Window should exist in open list atp
                if (!windows_open.isEmpty()) {
                    if (windows_open.peekFirst() == window) return;
                    if (windows_open.remove(window)) {
                        windows_open.addFirst(window);
                        return;
                    }
                } throw new RuntimeException("GUI Window should exist in open list atp.");
            }
        }
    }

    /**
     * Adds the window to a list of windows to be removed
     * from manager and disposed, during the next "prepare" call.
     * @param window window
     */
    protected void destroy(WindowGUI window) {
        assert_window_registered(window);
        if (!windows_to_destroy.contains(window)) {
            windows_to_destroy.addFirst(window);
        }
    }

    public void dispose() { // Call on application exit
        assert_manager_not_busy();
        Logger.debug("Disposing GUI Window Manager...");
        windows_open.clear();
        windows_closed.clear();
        windows_to_open.clear();
        windows_to_close.clear();
        windows_to_render.clear();
        windows_to_destroy.clear();
        for (var entry : windows_by_name.entrySet()) {
            Logger.debug("Disposing GUI Window: \"{}\"",entry.getKey());
            WindowGUI window = entry.getValue();
            window.clearWindowState();
            window.onDestroyed();
        } windows_by_name.clear();
    }

    private void assert_window_registered(WindowGUI window) {
        if (!window.isRegistered()) throw new IllegalStateException("Unregistered GUI Window");
    }

    private void assert_manager_not_busy() {
        if (preparing || rendering) throw new IllegalStateException("GUI Window Manager busy");
    }
}
