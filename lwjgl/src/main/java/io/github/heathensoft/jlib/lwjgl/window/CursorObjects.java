package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * @author Frederik Dahl
 * 28/10/2023
 */


public class CursorObjects implements Disposable {

    public static final int STANDARD_CURSOR_COUNT = 6;
    public static final int CURSOR_ARROW = 0;
    public static final int CURSOR_TEXT_INPUT = 1;
    public static final int CURSOR_CROSS_HAIR = 2;
    public static final int CURSOR_HAND = 3;
    public static final int CURSOR_H_RESIZE = 4;
    public static final int CURSOR_V_RESIZE = 5;

    private final long window;
    private Cursor currentCursor;
    private final Cursor[] standardCursors;
    private final Map<String, Cursor> customCursors;


    protected CursorObjects(long window) {
        this.window = window;
        this.customCursors = new HashMap<>(31);
        this.standardCursors = createStandardCursors(window);
    }


    public void useStandard(int shape) {
        if (shape < 0 || shape >= STANDARD_CURSOR_COUNT) {
            Logger.warn("cursor-shape: {} not recognized.", shape);
            return;
        }
        Cursor cursor = standardCursors[shape];
        if (cursor == null) {
            Logger.warn("unable to set cursor-shape: {} (not created)", shape);
            return;
        }
        if (currentCursor != cursor) {
            currentCursor = cursor;
            currentCursor.use();
        }
    }

    public void useCustom(String name) {
        Cursor cursor = customCursors.get(name);
        if (cursor == null) {
            Logger.warn("custom cursor: {} not recognized.", name);
            return;
        }
        if (currentCursor != cursor) {
            currentCursor = cursor;
            currentCursor.use();
        }
    }

    public void createCustom(int standard_cursor_type, Bitmap image, int xH, int yH) {
        Logger.info("Replacing standard cursor: {}",standard_cursor_type);
        if (standard_cursor_type < 0 || standard_cursor_type >= STANDARD_CURSOR_COUNT) {
            Logger.warn("cursor-slot: {} out-of-bounds", standard_cursor_type);
        } else try { Cursor cursor = new Cursor(null,image,window,xH,yH);
            Disposable.dispose(standardCursors[standard_cursor_type]);
            standardCursors[standard_cursor_type] = cursor;
        } catch (Exception e) {
            Logger.warn(e,"Unable to create custom cursor");
        }
    }

    public void createCustom(String name, Bitmap image, int xH, int yH) {
        Logger.info("Creating new cursor: {}",name);
        try { Cursor cursor = new Cursor(name,image,window,xH,yH);
            Cursor previous = customCursors.put(name,cursor);
            if (previous != null) {
                Logger.info("Disposing previous cursor: " + name);
                previous.dispose();
            }
        } catch (Exception e) {
            Logger.warn(e,"Unable to create custom cursor: " + name);
        }
    }


    private Cursor[] createStandardCursors(long window) {
        Cursor[] cursorArray = new Cursor[STANDARD_CURSOR_COUNT];
        for (int i = 0; i < STANDARD_CURSOR_COUNT; i++) {
            try { cursorArray[i] = new Cursor(window,GLFW_ARROW_CURSOR + i);
            } catch (Exception e) {
                Logger.warn(e,"unable to create standard cursor: " + i);
            }
        } return cursorArray;
    }

    public void dispose() {
        for (var entry : customCursors.entrySet()) {
            Disposable.dispose(entry.getValue());
        } for (int i = 0; i < STANDARD_CURSOR_COUNT; i++) {
            Disposable.dispose(standardCursors[i]);
        }
    }

    private static final class Cursor implements Disposable {

        private String name;
        private Bitmap bitmap;
        private final long cursor;
        private final long window;

        /**
         * @param window handle
         * @param shape enum
         * @throws Exception caught and logged by Cursors.
         */
        Cursor(long window, int shape) throws Exception {
            cursor = glfwCreateStandardCursor(shape);
            if (cursor == 0L) {
                String message;
                int code = glfwGetError(null);
                if (code == GLFW_INVALID_ENUM) {
                    message = "could not create cursor - invalid glfw enum";
                } else message = "could not create cursor";
                throw new Exception(message);
            } this.window = window;
        }

        /**
         * @param image image
         * @param window handle
         * @param xH hotspot
         * @param yH hotspot
         * @throws Exception caught and logged by Window.
         */
        Cursor(String name, Bitmap image, long window, int xH, int yH) throws Exception {
            ByteBuffer rgba = image.pixels();
            int w = image.width();
            int h = image.height();
            int c = image.channels();
            if (c != 4) throw new Exception("image must have 4 color components");
            try (MemoryStack stack = stackPush()) {
                GLFWImage img = GLFWImage.malloc(stack).set(w,h,rgba);
                cursor = glfwCreateCursor(img,xH,yH);
                if (cursor == 0L) {
                    throw new Exception("could not create cursor");
                } this.window = window;
                this.bitmap = image;
            } this.name = name;
        }


        void use() { glfwSetCursor(window, cursor); }

        String name() { return name == null ? "" : name; }


        public void dispose() {
            Disposable.dispose(bitmap);
            glfwDestroyCursor(cursor);
        }

    }
}
