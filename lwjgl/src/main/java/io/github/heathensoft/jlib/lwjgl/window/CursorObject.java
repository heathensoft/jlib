package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * To hide, disable, enable cursor. Do it via. Window.
 * Keep track of cursor objects on your own. Not tracked by Window but:
 * Cursors are destroyed when the Window terminates.
 *
 * @author Frederik Dahl
 * 19/10/2022
 */


public class CursorObject implements Disposable {

    private final String name;
    private boolean disposed;
    private final long cursor;
    private final long window;
    private Bitmap bitmap;
    
    /**
     * @param window handle
     * @param shape enum
     * @throws Exception caught and logged by Window.
     */
    protected CursorObject(String name, long window, int shape) throws Exception {
        cursor = glfwCreateStandardCursor(shape);
        if (cursor == 0L) {
            String message;
            int code = glfwGetError(null);
            if (code == GLFW_INVALID_ENUM) {
                message = "could not create cursor - invalid glfw enum";
            } else message = "could not create cursor";
            throw new Exception(message);
        } this.window = window;
        this.name = name;
    }
    
    /**
     * @param image image
     * @param window handle
     * @param xH hotspot
     * @param yH hotspot
     * @throws Exception caught and logged by Window.
     */
    protected CursorObject(String name, Bitmap image, long window, int xH, int yH) throws Exception {
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

    
    public void use() {
        if (disposed) throw new IllegalStateException("cursor is disposed");
        glfwSetCursor(window, cursor);
    }

    public String name() {
        return name;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            Disposable.dispose(bitmap);
            glfwDestroyCursor(cursor);
        }
    }
}
