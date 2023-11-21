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


    public static void gui_initialize(Resolution resolution) throws Exception {
        if (!GUI_INITIALIZED) {
            Renderer = new RendererGUI(resolution.width(),resolution.height());
            ScreenQuad = new ScreenQuad();
            Fonts = Renderer.fonts();
            GUI_INITIALIZED = true;
            new ColorPickerWindow();
        }
    }

    public static void gui_terminate() {
        if (GUI_INITIALIZED) {
            Windows.gui_windows_dispose();
            Disposable.dispose(Renderer, ScreenQuad);
            GUI_INITIALIZED = false;
        }
    }

    /** 1. WITHIN APPLICATION "ON UPDATE" */
    public static void gui_pre_render_update(float dt) {
        Windows.gui_windows_prepare(dt);
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
        private static int pressed_element = ELEMENT_NONE;
        private static int hovered_element = ELEMENT_NONE;
        private static int grabbed_element = ELEMENT_NONE;
        private static int focused_element = ELEMENT_NONE;
        private static int last_pressed_element = ELEMENT_NONE;
        private static int last_hovered_element = ELEMENT_NONE;
        private static int last_grabbed_element = ELEMENT_NONE;
        private static int active_mouse_button = -1;
        private static int active_mouse_button_last = -1;
        private static boolean mouse_grab_this_frame;
        private static boolean mouse_grab_last_frame;
        private static boolean pause_processing;
        private static final IDPool id_pool = new IDPool();
        static { id_pool.obtainID(); }

        public static void update(int pixelID, float dt) {
            if (pause_processing) pixelID = ELEMENT_NONE;
            if (current_cursor_icon != desired_cursor_icon) {
                current_cursor_icon = desired_cursor_icon;
                Engine.get().window().cursorObjects().useStandard(current_cursor_icon);
            } Mouse mouse = Engine.get().input().mouse();
            last_hovered_element = hovered_element;
            last_pressed_element = pressed_element;
            last_grabbed_element = grabbed_element;
            active_mouse_button_last = active_mouse_button;
            mouse_grab_last_frame = mouse_grab_this_frame;
            if (active_mouse_button == -1) { // If not currently pressing
                for (int button = 0; button < 3; button++) {
                    if (mouse.just_clicked(button)) {
                        active_mouse_button = button;
                        pressed_element = pixelID;
                        if (pressed_element == ELEMENT_NONE) {
                            focused_element = ELEMENT_NONE;
                        } break;
                    }
                }
            } else if (mouse.button_pressed(active_mouse_button)) {
                pressed_duration += dt;
                if (mouse.is_dragging(active_mouse_button)) {
                    grabbed_element = pressed_element;
                    mouse_grab_this_frame = true;
                }
            } else {
                mouse_grab_this_frame = false;
                pressed_duration = 0f;
                pressed_element = ELEMENT_NONE;
                grabbed_element = ELEMENT_NONE;
                active_mouse_button = -1;
            }
            if (hovered_element == pixelID) {
                if (pixelID > ELEMENT_NONE)
                    hovered_duration += dt;
            } else { hovered_element = pixelID;
                hovered_duration = 0f;
            }
            if (hovered_element > ELEMENT_NONE || pressed_element > ELEMENT_NONE) {
                desired_cursor_icon = CursorObjects.CURSOR_HAND;
            } else desired_cursor_icon = CursorObjects.CURSOR_ARROW;
        }

        public static void setCursorIcon(int index) {
            if (index < 0 || index >= CursorObjects.STANDARD_CURSOR_COUNT) return;
            desired_cursor_icon = index;
        }

        public static float hoveredDuration() { return hovered_duration; }
        public static float pressedDuration() { return pressed_duration; }

        public static void pauseWindowsEventProcessing() { pause_processing = true; }
        public static void unpauseWindowEventProcessing() { pause_processing = false; }

        public static boolean anyInteractableFocused() { return focused_element != ELEMENT_NONE; }
        public static boolean anyInteractableHovered() { return hovered_element != ELEMENT_NONE; }
        public static boolean anyInteractablePressed() { return pressed_element != ELEMENT_NONE; }

        public static boolean mouseIsPressed() { return (active_mouse_button != -1); }
        public static boolean mouseIsPressed(int button) { return (active_mouse_button == button); }
        public static boolean mouseJustPressed(int button) { return mouseIsPressed(button) && (active_mouse_button_last != button); }
        public static boolean mouseJustPressed() { return mouseIsPressed() && (active_mouse_button_last == -1); }
        public static boolean mouseJustReleased(int button) { return (active_mouse_button == -1) && (active_mouse_button_last == button); }
        public static boolean mouseJustReleased() { return (active_mouse_button == -1) && (active_mouse_button_last != -1); }
        public static boolean mouseIsDragging() { return mouse_grab_this_frame; }
        public static boolean mouseIsDragging(int button) { return mouseIsDragging() && mouseIsPressed(button); }

        public static boolean worldHovered() { return hovered_element == ELEMENT_NONE; }
        public static boolean worldJustHovered() { return worldHovered() && last_hovered_element != ELEMENT_NONE; }
        public static boolean worldPressed(int button) { return pressed_element == ELEMENT_NONE && button == active_mouse_button; }
        public static boolean worldPressed() { return pressed_element == ELEMENT_NONE && active_mouse_button != -1; }
        public static boolean worldJustPressed(int button) { return worldPressed(button) && mouseJustPressed(button); }
        public static boolean worldJustPressed() { return worldPressed() && mouseJustPressed(); }
        public static boolean worldJustReleased(int button) { return last_pressed_element == ELEMENT_NONE && mouseJustReleased(button); }
        public static boolean worldJustReleased() { return last_pressed_element == ELEMENT_NONE && mouseJustReleased(); }
        public static boolean worldIsGrabbed(int button) {return worldIsGrabbed() && (active_mouse_button == button); }
        public static boolean worldIsGrabbed() { return grabbed_element == ELEMENT_NONE && mouse_grab_this_frame; }
        public static boolean worldJustReleasedFromGrab() { return last_grabbed_element == ELEMENT_NONE && mouse_grab_last_frame &! mouse_grab_this_frame; }
        public static boolean worldJustReleasedFromGrab(int button) { return worldJustReleasedFromGrab() && active_mouse_button_last == button; }

        static void yieldFocus(int id) { if (focused_element == id) focused_element = ELEMENT_NONE; }
        static void focus(int id) { focused_element = id; }
        static boolean hasFocus(int id) { return focused_element == id; }
        static boolean isHovered(int id) { return id == hovered_element; }
        static boolean justHovered(int id) { return isHovered(id) && last_hovered_element != id; }
        static boolean isPressed(int id, int button) { return id == pressed_element && button == active_mouse_button; }
        static boolean isPressed(int id) { return id == pressed_element; }
        static boolean justPressed(int id, int button) { return isPressed(id,button) && (id != last_pressed_element); }
        static boolean justPressed(int id) { return isPressed(id) && (id != last_pressed_element); }
        static boolean isGrabbed(int id) { return id == grabbed_element; }
        static boolean isGrabbed(int id, int button) { return isGrabbed(id) && button == active_mouse_button; }
        static boolean justGrabbed(int id) { return isGrabbed(id) && id != last_grabbed_element; }
        static boolean justGrabbed(int id, int button) { return justGrabbed(id) && active_mouse_button == button; }
        static boolean justReleased(int id) { return !isPressed(id) && last_pressed_element == id; }
        static boolean justReleased(int id, int button) { return justReleased(id) && active_mouse_button_last == button;}
        static boolean justReleasedFromGrab(int id) { return !isGrabbed(id) && last_grabbed_element == id; }
        static boolean justReleasedFromGrab(int id, int button) { return justReleasedFromGrab(id) && active_mouse_button_last == button; }
        static boolean clickedNotGrabbed(int id, int button) { return clicked(id, button) &! justReleasedFromGrab(id); }
        static boolean clicked(int id, int button) { return isHovered(id) && justReleased(id,button); }

        static int obtainID() { return id_pool.obtainID(); }
        static void returnID(int id) {
            if (id > 0) { id_pool.returnID(id); }
            if (pressed_element == id) pressed_element = ELEMENT_NONE;
            if (grabbed_element == id) grabbed_element = ELEMENT_NONE;
            if (focused_element == id) focused_element = ELEMENT_NONE;
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

        public static void gui_windows_prepare(float dt) {
            while (!new_windows.isEmpty()) {
                WindowGUI window = new_windows.removeLast();
                if (!windows.contains(window)) {
                    windows.addFirst(window);
                }
            }
            while (!focus_queue.isEmpty()) {
                WindowGUI window = focus_queue.removeLast();
                if (!windows.isEmpty()) {
                    if (windows.getFirst() != window) {
                        if (windows.remove(window)) {
                            windows.addFirst(window);
                            window.onFocus();
                        }
                    }
                }
            }
            for (WindowGUI window : windows) {
                window.prepare(dt);
            }
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
            }
        }

        public static void gui_windows_dispose() {
            while (!new_windows.isEmpty()) {
                WindowGUI window = new_windows.removeLast();
                if (!windows.contains(window)) {
                    windows.addFirst(window); }
            } while (!focus_queue.isEmpty()) {
                WindowGUI window = focus_queue.removeLast();
                if (!windows.contains(window)) {
                    windows.addFirst(window); }
            } while (!windows.isEmpty()) {
                WindowGUI window = windows.removeLast();
                Disposable.dispose(window);
            }
        }

        public static void gui_windows_add_new_window(WindowGUI window) {
            if (!new_windows.contains(window)) new_windows.addFirst(window);
        }

        public static void gui_windows_focus(WindowGUI window) {
            if (!focus_queue.contains(window)) focus_queue.addFirst(window);
        }

        public static boolean gui_windows_is_focused(WindowGUI window) {
            return !windows.isEmpty() && windows.getFirst() == window;
        }
    }
}
