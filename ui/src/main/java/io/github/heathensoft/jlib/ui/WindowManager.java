package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.TextAlignment;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;
import org.tinylog.Logger;

import java.util.*;

/**
 * @author Frederik Dahl
 * 07/03/2024
 */


public class WindowManager implements Disposable {

    private final Map<Integer,List<Window>> windows_by_group;
    private final Map<String,Window> windows_by_name;
    private final LinkedList<Window> windows_closed;
    private final LinkedList<Window> windows_opened;
    private final LinkedList<Window> windows_recently_closed;
    private final LinkedList<Window> windows_to_open;
    private final LinkedList<Window> windows_to_close;
    private final LinkedList<Window> windows_to_terminate;
    private final LinkedList<Window> tmp_list;
    private final Tooltips tool_tips;

    private boolean updating_internals;
    private boolean preparing_windows;
    private boolean rendering_windows;

    WindowManager() {
        tool_tips = new Tooltips();
        tmp_list = new LinkedList<>();
        windows_by_name = new HashMap<>();
        windows_by_group = new HashMap<>();
        windows_to_open = new LinkedList<>();
        windows_to_close = new LinkedList<>();
        windows_to_terminate = new LinkedList<>();
        windows_closed = new LinkedList<>();
        windows_opened = new LinkedList<>();
        windows_recently_closed = new LinkedList<>();
    }

    /**
     * Updates internal lists.
     * close, open, focus, terminate requests are handled.
     * And the corresponding window methods are called.
     * Then the window.prepare() method is called for all open windows.
     * The window.prepare() method is used to process and render anything
     * that must happen outside the GUI Renderers begin() and end() methods.
     * I.e. Rendering to other framebuffers (Non-GUI pipelines).
     * @param dt delta time if needed
     */
    protected void update(float dt) {
        assert_not_updating_internals();
        assert_not_rendering_windows();
        assert_not_preparing_windows();
        try {
            updating_internals = true;
            while (!windows_to_open.isEmpty()) {
                Window window = windows_to_open.removeLast();
                windows_opened.addFirst(window);
                window.setStateOpen();
                window.onOpen();
            } if (!windows_to_close.isEmpty()) {
                windows_recently_closed.clear();
                while (!windows_to_close.isEmpty()) {
                    Window window = windows_to_close.removeLast();
                    if (window.isInFocus()) {
                        window.clearStateInFocus();
                        window.onFocusLoss();
                    } window.clearStateOpen();
                    window.clearAutoFocus();
                    window.onClose();
                    windows_closed.addFirst(window);
                    windows_recently_closed.addFirst(window);
                }
            } while (!windows_to_terminate.isEmpty()) {
                Window window = windows_to_terminate.getLast();
                String key = window.name();
                windows_recently_closed.remove(window);
                if (window.isOpen()) windows_opened.remove(window);
                else windows_closed.remove(window);
                if (!windows_by_name.remove(key,window))
                    throw new IllegalStateException("GUI: Window was registered but not in map of names");
                List<Window> group = windows_by_group.get(window.windowGroup());
                group.remove(window);
                window.clearWindowState();
                window.clearAutoFocus();
                window.onTermination();
            } tmp_list.clear();
            boolean front_window = true;
            for (Window window : windows_opened) {
                if (front_window) {
                    if (!window.isInFocus()) {
                        window.setStateInFocus();
                        window.onFocusGain();
                    } front_window = false;
                } else {
                    if (window.isInFocus()) {
                        window.clearStateInFocus();
                        window.onFocusLoss();
                    }
                } tmp_list.addFirst(window);
            } updating_internals = false;
            preparing_windows = true;
            for (Window window : tmp_list) {
                window.prepare(dt);
            } preparing_windows = false;
        } finally { updating_internals = false;
            preparing_windows = false;
        }
    }

    /**
     * Render and update all open windows. Rendered to the GUI Framebuffers.
     * Atp. the render batches have started, and anything rendered
     * within this method should be rendered by the provided Renderer only.
     * All GUI Interactable states (pixel ids) have been updated before
     * this is called. Immediate-mode GUI
     * @param renderer the provided GUI renderer
     * @param dt delta time if needed.
     */
    protected void render(RendererGUI renderer, float dt) {
        assert_not_rendering_windows();
        assert_not_preparing_windows();
        assert_not_updating_internals();
        try { rendering_windows = true;
            Resolution resolution = Engine.get().window().appResolution();
            while (!tmp_list.isEmpty()) {
                Window window = tmp_list.removeFirst();
                window.render(renderer,dt);
            } tool_tips.draw(renderer);
        } finally { rendering_windows = false; }
    }

    public <T extends Window> Optional<T> getByName(String key, Class<T> clazz) {
        Window window = windows_by_name.get(key);
        if (window != null) {
            if (window.getClass() != clazz) {
                throw new ClassCastException("GUI Window: " + window.name());
            } return Optional.of(clazz.cast(window));
        } return Optional.empty();
    }

    public Optional<Window> getByName(String key) {
        Window window = windows_by_name.get(key);
        return window == null ? Optional.empty() : Optional.of(window);
    }

    /**
     * Register Window to be processed by the Manager.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window The Window to be processed
     * @throws Exception If the window.onRegister() should trow
     * an exception. The app could resume but the window would not be registered.
     * Before the exception is thrown, the windows.onTermination() method is called.
     */
    public void register(Window window) throws Exception {
        assert_not_updating_internals();
        try { updating_internals = true;
            assert_window_not_null(window);
            String key = window.name();
            if (key == null) { key = "untitled window";
            } Logger.debug("GUI: Registering Window: \"{}\"",key);
            if (window.isRegistered()) {
                Logger.info("Window \"{}\" already registered",key);
            } else { Window existing = windows_by_name.get(key);
                if (existing == window) {
                    throw new IllegalStateException("GUI: Unregistered Window reference in Manager");
                } else if (existing != null) { // name conflict
                    Logger.debug("Another window registered with name: \"{}\"",key);
                    Logger.debug("Incrementing suffix until a unique name is found");
                    {
                        int numeral_suffix = 0;
                        boolean new_name_found = false;
                        while (!new_name_found) {
                            String new_key = key + numeral_suffix;
                            existing = windows_by_name.get(new_key);
                            if (existing == null) {
                                new_name_found = true;
                                key = new_key;
                            } else numeral_suffix++;
                        }
                    }
                } // window.clearWindowState();
                try { window.onInit(key);
                    window.setStateRegistered();
                } catch (Exception e) {
                    window.onTermination();
                    throw e;
                }
                windows_by_name.put(key,window);
                windows_closed.addFirst(window);
                List<Window> window_group = windows_by_group.computeIfAbsent(window.windowGroup(), k -> new LinkedList<>());
                window_group.add(window);
                Logger.debug("GUI: Registered Window: \"{}\" to group: {}",key,window.windowGroup());
            }
        } finally { updating_internals = false; }
    }

    /**
     * Open the window if it's been registered and is currently closed.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window The Window to open
     */
    public void openWindow(Window window) {
        assert_window_not_null(window);
        assert_window_registered(window);
        assert_not_updating_internals();
        updating_internals = true;
        if (!window.isOpen()) {
            if (windows_closed.remove(window)) {
                windows_to_open.addFirst(window);
            }
        } updating_internals = false;
    }

    /**
     * Open all closed windows from the window group.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window_group The Window Group to open
     */
    public void openWindowGroup(int window_group) {
        assert_not_updating_internals();
        updating_internals = true;
        List<Window> group = windows_by_group.get(window_group);
        if (group != null) {
            for (Window window : group) {
                if (!window.isOpen()) {
                    if (windows_closed.remove(window)) {
                        windows_to_open.addFirst(window);
                    }
                }
            }
        } else Logger.info("GUI Window-Group: {} does not exist.");
        updating_internals = false;
    }

    /**
     * Open all recently closed windows.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     */
    public void openRecentlyClosed() {
        // You can call this followed by closeWindow
        // to get the effect: open recently closed except...
        assert_not_updating_internals();
        updating_internals = true;
        while (!windows_recently_closed.isEmpty()) {
            Window window = windows_recently_closed.removeLast();
            if (!window.isOpen()) {
                if (windows_closed.remove(window)) {
                    windows_to_open.addFirst(window);
                }
            }
        }
        updating_internals = false;
    }

    /**
     * Close the window if it's been registered and is currently open.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window The Window to close
     */
    public void closeWindow(Window window) {
        assert_window_not_null(window);
        assert_window_registered(window);
        assert_not_updating_internals();
        updating_internals = true;
        if (window.isOpen()) {
            if (windows_opened.remove(window)) {
                windows_to_close.addFirst(window);
            }
        }
        updating_internals = false;
    }

    /**
     * Close all open windows from the window group.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window_group The Window Group to close
     */
    public void closeWindowGroup(int window_group) {
        assert_not_updating_internals();
        updating_internals = true;
        List<Window> group = windows_by_group.get(window_group);
        if (group != null) {
            for (Window window : group) {
                if (window.isOpen()) {
                    if (windows_opened.remove(window)) {
                        windows_to_close.addFirst(window);
                    }
                }
            }
        } else Logger.info("GUI Window-Group: {} does not exist.");
        updating_internals = false;
    }

    /**
     * Close All Windows
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     */
    public void closeAll() {
        assert_not_updating_internals();
        updating_internals = true;
        while (!windows_opened.isEmpty()) {
            windows_to_close.addFirst(windows_opened.removeLast());
        } updating_internals = false;
    }

    /**
     * Request focus for the window if it's been registered and is currently open.
     * This will order the window to be rendered last (appear in front).
     * If the window is not already in focus, it will gain focus before the next render call.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window The Window to focus
     */
    public void focusRequest(Window window) {
        assert_window_not_null(window);
        assert_window_registered(window);
        assert_not_updating_internals();
        try { updating_internals = true;
            if (window.isOpen()) {
                // If window not queued to close
                if (!windows_to_close.contains(window)) {
                    // Window should exist in open list atp
                    if (!windows_opened.isEmpty()) {
                        if (windows_opened.peekFirst() == window) return;
                        if (windows_opened.remove(window)) {
                            windows_opened.addFirst(window);
                            return;
                        }
                    } throw new RuntimeException("GUI Window should exist in open list atp.");
                }
            }
        } finally { updating_internals = false; }

    }

    /**
     * Will add the window to a list of windows to terminate before
     * the next render call. Terminated windows will lose all state, and
     * must be re-registered. But atp. it's better to provide a new instance
     * of the window instead.
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     * @param window The Window to terminate
     */
    public void terminateWindow(Window window) {
        assert_window_not_null(window);
        assert_window_registered(window);
        assert_not_updating_internals();
        updating_internals = true;
        if (!windows_to_terminate.contains(window)) {
            windows_to_terminate.addFirst(window);
        } updating_internals = false;
    }

    /**
     * Will terminate all windows and clear the Manager.
     * dispose() must be called from outside any window related
     * methods. This should be called when terminating the GUI
     * Cannot be called from within window.render();
     * Cannot be called from within window.prepare();
     * Cannot be called from within window.onInit();
     * Cannot be called from within window.onOpen();
     * Cannot be called from within window.onClose();
     * Cannot be called from within window.onTermination();
     */
    public void dispose() {  // Call on application / application state exit
        assert_not_updating_internals();
        assert_not_preparing_windows();
        assert_not_rendering_windows();
        updating_internals = true;
        Logger.debug("GUI: Disposing Window Manager...");
        tmp_list.clear();
        windows_opened.clear();
        windows_closed.clear();
        windows_to_open.clear();
        windows_to_close.clear();
        windows_by_group.clear();
        windows_to_terminate.clear();
        windows_recently_closed.clear();
        for (var entry : windows_by_name.entrySet()) {
            Logger.debug("Disposing GUI Window: \"{}\"",entry.getKey());
            Window window = entry.getValue();
            window.clearAutoFocus();
            window.clearWindowState();
            window.onTermination();
        } windows_by_name.clear();
        updating_internals = false;
    }

    protected Tooltips helpText() { return tool_tips; }

    private void assert_window_registered(Window window) {
        if (!window.isRegistered()) throw new IllegalStateException("GUI: Unregistered Window");
    }

    private void assert_window_not_null(Window window) {
        if (window == null) throw new NullPointerException("GUI: Window cannot be null");
    }

    private void assert_not_updating_internals() {
        if (updating_internals) throw new IllegalStateException("GUI: Illegal call while manager is busy updating internal states");
    }

    private void assert_not_preparing_windows() {
        if (preparing_windows) throw new IllegalStateException("GUI: Illegal call while manager is busy preparing windows");
    }

    private void assert_not_rendering_windows() {
        if (rendering_windows) throw new IllegalStateException("GUI: Illegal call while manager is busy rendering windows");
    }

    public static final class Tooltips {
        private int color;
        private int font;
        private int padding;
        private boolean display;
        private String string;
        private final Rectanglef bounds = new Rectanglef();
        private void draw(RendererGUI renderer) {
            if (display) { display = false;
                renderer.drawElement(bounds,0x99000000);
                renderer.drawStringFixedSize(string,TextAlignment.LEFT,bounds,font,color,padding);
            }
        }

        public void display(String string, Vector2f mouse_position) { display(string,mouse_position, Color.rgb_to_intBits(GUI.variables.tooltips_default_text_color)); }
        public void display(String string, Vector2f mouse_position, int color) {
            if (!string.isBlank()) {
                this.color = color;
                this.string = string;
                this.font = GUI.variables.tooltips_font;
                this.padding = GUI.variables.tooltips_padding;
                int font_size = GUI.variables.tooltips_fontsize;
                FontsGUI fonts = GUI.fonts;
                fonts.bindFontMetrics(font);
                Resolution resolution = GUI.resolution();
                float help_window_height = font_size + 2 * padding;
                float help_window_width = fonts.advanceSumSized(string,font_size) + 2 * padding;
                float mouse_x = U.clamp(mouse_position.x,0,resolution.width());
                float mouse_y = U.clamp(mouse_position.y,0,resolution.height());
                bounds.minY = mouse_y + 16;
                bounds.maxY = bounds.minY + help_window_height;
                bounds.minX = mouse_x;
                bounds.maxX = bounds.minX + help_window_width;
                if (bounds.maxY > resolution.height()) {
                    bounds.translate(0,-(help_window_height + 32));
                } if (bounds.maxX > resolution.width()) {
                    bounds.translate(resolution.width() - bounds.maxX,0);
                } display = true;
            }
        }
    }

}
