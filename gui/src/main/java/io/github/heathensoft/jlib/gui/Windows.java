package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.window.WindowGUI;
import org.tinylog.Logger;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

/**
 * GUI Windows manager
 *
 * Windows should have unique names. If a window with a name already
 * inhabits the active windows, it will be ignored.
 *
 *
 * @author Frederik Dahl
 * 21/11/2023
 */


public class Windows implements Disposable {

    private final LinkedList<WindowGUI> active_windows = new LinkedList<>();
    private final LinkedList<WindowGUI> focus_queue = new LinkedList<>();
    private final LinkedList<WindowGUI> new_windows = new LinkedList<>();
    private boolean hide_all;

    Windows() { }

    /**
     * Prepare windows before starting the renderer.
     * Any newly focused or added windows are sorted and / or
     * added to the list of windows.
     * If a window is focused. The window.onFocus() method is called.
     * If a window is not queued for destruction, the window.prepare() method is called
     * The prepare method is not propagated through its contents. Its purpose
     * is to potentially refresh internal graphical elements / framebuffers outside
     * the batch renderer.
     * @param dt delta time
     */
    public void prepare(float dt) {
        while (!new_windows.isEmpty()) {
            WindowGUI window = new_windows.removeLast();
            if (!active_windows.contains(window)) {
                active_windows.addFirst(window); // add last.
                // focus
            }
        }
        while (!focus_queue.isEmpty()) {
            WindowGUI window = focus_queue.removeLast();
            if (!active_windows.isEmpty()) {
                if (active_windows.getFirst() != window) {
                    if (active_windows.remove(window)) {
                        active_windows.addFirst(window);
                        window.onFocus();
                    }
                }
            }
        }
        for (WindowGUI window : active_windows) {
            if (!window.queued_for_destruction())
                window.prepare(dt);
        }
    }

    /**
     * Call this between renderer: begin / end.
     * If hide_all is false: Renders all OPEN gui windows.
     * Any windows queued for destruction are disposed here.
     * And the window is obviously not rendered.
     * @param renderer gui renderer
     * @param dt delta time
     */
    public void renderAll(RendererGUI renderer, float dt) {
        if (!active_windows.isEmpty()) {
            ListIterator<WindowGUI> iterator;
            iterator = active_windows.listIterator(active_windows.size());
            while (iterator.hasPrevious()) {
                WindowGUI window = iterator.previous();
                if (window.queued_for_destruction()) {
                    Disposable.dispose(window);
                    iterator.remove();
                } else if (window.isOpen() &! hide_all) {
                    window.render(renderer, dt);
                }
            }
        }
    }

    /**
     * Terminates all windows. New windows can be added after this is called.
     */
    public void disposeAll() {
        while (!new_windows.isEmpty()) {
            WindowGUI window = new_windows.removeLast();
            if (!active_windows.contains(window)) {
                active_windows.addFirst(window); }
        } while (!focus_queue.isEmpty()) {
            WindowGUI window = focus_queue.removeLast();
            if (!active_windows.contains(window)) {
                active_windows.addFirst(window); }
        } while (!active_windows.isEmpty()) {
            WindowGUI window = active_windows.removeLast();
            Disposable.dispose(window);
        }
    }

    /**
     * Get window by name. Windows should have unique names.
     * @param name window name
     * @return the window or empty optional
     */
    public Optional<WindowGUI> get(String name) {
        for (WindowGUI window : active_windows) {
            if (name.equals(window.name())) return Optional.of(window);
        } for (WindowGUI window : new_windows) {
            if (name.equals(window.name())) {
                // Just want to see if this ever happens
                Logger.info("GUI.windows.get: from new_windows");
                return Optional.of(window);}
        } return Optional.empty();
    }

    public void addNew(WindowGUI window) { if (!new_windows.contains(window)) new_windows.addFirst(window); }

    public void focusOn(WindowGUI window) { if (!focus_queue.contains(window)) focus_queue.addFirst(window); }

    public boolean isInFocus(WindowGUI window) { return !active_windows.isEmpty() && active_windows.getFirst() == window; }

    public boolean areHidden() { return hide_all; }

    public void hideAll() { hide_all = true; }

    public void showAll() { hide_all = false; }

    public void dispose() { disposeAll(); }
}
