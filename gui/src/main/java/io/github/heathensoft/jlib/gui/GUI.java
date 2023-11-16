package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.IDPool;
import io.github.heathensoft.jlib.gui.dev.ColorPickerWindow;
import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.window.WindowGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector4f;
import org.tinylog.Logger;

import java.util.LinkedList;
import java.util.ListIterator;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 11/11/2023
 */


public class GUI {


    private static boolean GUI_INITIALIZED;
    public static FontsGUI Fonts;
    public static RendererGUI Renderer;
    public static ScreenQuad ScreenQuad;

    public static final Vector4f color_screen_clear_color = new Vector4f(0.18f,0.03f,0.11f,1);


    public static void INITIALIZE_GUI(Resolution resolution) throws Exception {
        if (!GUI_INITIALIZED) {
            Renderer = new RendererGUI(resolution.width(),resolution.height());
            ScreenQuad = new ScreenQuad();
            Fonts = Renderer.fonts();
            GUI_INITIALIZED = true;
            new ColorPickerWindow();
        }
    }

    public static void DISPOSE_GUI() {
        if (GUI_INITIALIZED) {
            Windows.gui_windows_dispose();
            Disposable.dispose(Renderer, ScreenQuad);
            GUI_INITIALIZED = false;
        }
    }

    /** 1. WITHIN APPLICATION "ON UPDATE" */
    public static void gui_pre_render_update(float dt) {
        Windows.gui_windows_pre_render_update(dt);
        State.update(Renderer.pixelID(),dt);
    }

    /** 2. WITHIN APPLICATION "ON UPDATE" */
    public static void gui_start_rendering() {
        DefaultInput input = Engine.get().input();
        Renderer.begin(input.mouse().position());
    }

    /** 3. WITHIN APPLICATION "ON UPDATE" */
    public static void gui_render_windows(float dt) {
        Windows.gui_windows_render(Renderer,dt);
    }

    /** 4. WITHIN APPLICATION "ON UPDATE" */
    public static void gui_stop_rendering() {
        Renderer.end();
    }

    public static void gui_resolution_request(Resolution resolution) throws Exception {
        Renderer.updateResolution(resolution.width(),resolution.height());
    }

    /** WITHIN APPLICATION "ON RENDER" */
    public static void gui_render_to_screen() {
        Framebuffer.bindDefault();
        Framebuffer.setClearColor(color_screen_clear_color);
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        Texture diffuse = Renderer.framebufferDiffuseTexture();
        ScreenQuad.render(diffuse);
    }


    public static final class State {
        private static final int ELEMENT_NONE = 0;
        private static float hovered_duration = 0;
        private static float pressed_duration = 0;
        private static int desired_cursor_icon = 0;
        private static int current_cursor_icon = 0;
        private static int pressed = ELEMENT_NONE;
        private static int hovered = ELEMENT_NONE;
        private static int grabbed = ELEMENT_NONE;
        private static int focused = ELEMENT_NONE;
        private static int last_pressed = ELEMENT_NONE;
        private static int last_hovered = ELEMENT_NONE;
        private static int last_grabbed = ELEMENT_NONE;
        private static int mouse_button = -1;
        private static int mouse_button_last = -1;
        private static boolean pause_processing;
        private static final IDPool id_pool = new IDPool();
        static { id_pool.obtainID(); }

        public static void update(int pixelID, float dt) {
            if (pause_processing) pixelID = ELEMENT_NONE;
            if (current_cursor_icon != desired_cursor_icon) {
                current_cursor_icon = desired_cursor_icon;
                Engine.get().window().cursorObjects().useStandard(current_cursor_icon);
            }
            last_hovered = hovered;
            last_pressed = pressed;
            last_grabbed = grabbed;
            mouse_button_last = mouse_button;
            Mouse mouse = Engine.get().input().mouse();
            if (pressed > ELEMENT_NONE) {
                if (mouse.button_pressed(mouse_button)) {
                    pressed_duration += dt;
                    if (mouse.is_dragging(mouse_button))
                        grabbed = pressed;
                } else { pressed_duration = 0f;
                    pressed = ELEMENT_NONE;
                    grabbed = ELEMENT_NONE;
                    mouse_button = -1; }
            } else if (pixelID > ELEMENT_NONE) {
                for (int button = 0; button < 3; button++) {
                    if (mouse.just_clicked(button)) {
                        mouse_button = button;
                        pressed = pixelID;
                        break;
                    }
                }
            }
            if (hovered == pixelID) {
                if (pixelID > ELEMENT_NONE)
                    hovered_duration += dt;
            } else { hovered = pixelID;
                hovered_duration = 0f;
            }
            if (hovered > ELEMENT_NONE || pressed > ELEMENT_NONE) {
                desired_cursor_icon = CursorObjects.CURSOR_HAND;
            } else desired_cursor_icon = CursorObjects.CURSOR_ARROW;
        }

        public static void setCursorIcon(int index) {
            if (index < 0 || index >= CursorObjects.STANDARD_CURSOR_COUNT) return;
            desired_cursor_icon = index;
        }
        public static void pauseWindowsEventProcessing() { pause_processing = true; }
        public static void unpauseWindowEventProcessing() { pause_processing = false; }
        public static boolean anyInteractableFocused() { return focused != ELEMENT_NONE; }
        public static boolean anyInteractableHovered() { return hovered != ELEMENT_NONE; }
        public static boolean anyInteractablePressed() { return pressed != ELEMENT_NONE; }
        static void yieldFocus(int id) { if (focused == id) focused = ELEMENT_NONE; }
        static void focus(int id) { focused = id; }
        static boolean hasFocus(int id) { return focused == id; }
        static boolean isHovered(int id) { return id == hovered; }
        static boolean justHovered(int id) { return isHovered(id) && last_hovered != id; }
        static boolean isPressed(int id, int button) { return id == pressed && button == mouse_button; }
        static boolean isPressed(int id) { return id == pressed; }
        static boolean justPressed(int id, int button) { return isPressed(id,button) && (id != last_pressed); }
        static boolean justPressed(int id) { return isPressed(id) && (id != last_pressed); }
        static boolean isGrabbed(int id) { return id == grabbed; }
        static boolean isGrabbed(int id, int button) { return isGrabbed(id) && button == mouse_button; }
        static boolean justGrabbed(int id) { return isGrabbed(id) && id != last_grabbed; }
        static boolean justGrabbed(int id, int button) { return justGrabbed(id) && mouse_button == button; }
        static boolean justReleased(int id) { return !isPressed(id) && last_pressed == id; }
        static boolean justReleased(int id, int button) { return justReleased(id) && mouse_button_last == button;}
        static boolean justReleasedFromGrab(int id) { return !isGrabbed(id) && last_grabbed == id; }
        static boolean justReleasedFromGrab(int id, int button) { return justReleasedFromGrab(id) && mouse_button_last == button; }
        static boolean clickedNotGrabbed(int id, int button) { return clicked(id, button) &! justReleasedFromGrab(id); }
        static boolean clicked(int id, int button) { return isHovered(id) && justReleased(id,button); }
        static float hoveredDuration() { return hovered_duration; }
        static float pressedDuration() { return pressed_duration; }
        static int obtainID() { return id_pool.obtainID(); }
        static void returnID(int id) {
            if (id > 0) { id_pool.returnID(id); }
            if (pressed == id) pressed = ELEMENT_NONE;
            if (grabbed == id) grabbed = ELEMENT_NONE;
            if (focused == id) focused = ELEMENT_NONE;
        }
    }

    public static final class Windows {
        private static final LinkedList<WindowGUI> windows = new LinkedList<>();
        private static final LinkedList<WindowGUI> focus_queue = new LinkedList<>();
        private static final LinkedList<WindowGUI> new_windows = new LinkedList<>();
        public static WindowGUI gui_windows_get(String name) {
            for (WindowGUI window : windows) {
                if (name.equals(window.name())) return window;
            } for (WindowGUI window : new_windows) {
                if (name.equals(window.name())) {
                    Logger.info("gui_windows_get: from new_windows");
                    return window;
                }
            } return null;
        }

        public static void gui_add_new_window(WindowGUI window) {
            new_windows.addFirst(window);
        }
        public static void gui_windows_pre_render_update(float dt) {
            for (WindowGUI window : windows) window.pre_render_update(dt);
        }
        public static void gui_windows_render(RendererGUI renderer, float dt) {
            if (!windows.isEmpty()) {
                ListIterator<WindowGUI> iterator;
                iterator = windows.listIterator(windows.size());
                while (iterator.hasPrevious()) {
                    WindowGUI window = iterator.previous();
                    if (window.queued_for_destruction()) {
                        Disposable.dispose(window);
                        iterator.remove();
                    } else if (window.isOpen()) {
                        window.render(renderer, dt);
                    }
                }
            } while (!focus_queue.isEmpty()) {
                WindowGUI focused = focus_queue.removeLast();
                if (windows.remove(focused)) {
                    windows.addFirst(focused);}
            } while (!new_windows.isEmpty()) {
                windows.addFirst(new_windows.removeLast());
            }
        }
        public static void gui_windows_dispose() {
            while (!new_windows.isEmpty()) {
                WindowGUI window = windows.removeLast();
                Disposable.dispose(window);
            }
            while (!windows.isEmpty()) {
                WindowGUI window = windows.removeLast();
                Disposable.dispose(window);
            }
        }
        public static void gui_windows_focus(WindowGUI window) {
            focus_queue.addFirst(window);
            if (!windows.isEmpty()) {
                if (windows.getFirst() != window) {
                    if (windows.contains(window)) {
                        focus_queue.addFirst(window);
                    }
                }
            }
        }
    }
}
