package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
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
    
    private final long cursor;
    private final long window;
    
    /**
     * @param window handle
     * @param shape enum
     * @throws Exception caught and logged by Window.
     */
    protected CursorObject(long window, int shape) throws Exception {
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
    protected CursorObject(Image image, long window, int xH, int yH) throws Exception {
        ByteBuffer rgba = image.data();
        int w = image.width();
        int h = image.height();
        int c = image.format().channels;
        if (c != 4) throw new Exception("image must have 4 color components");
        try (MemoryStack stack = stackPush()) {
            GLFWImage img = GLFWImage.malloc(stack).set(w,h,rgba);
            image.dispose();
            cursor = glfwCreateCursor(img,xH,yH);
            if (cursor == 0L) {
                throw new Exception("could not create cursor");
            } this.window = window;
        }
    }
    
    public void use() {
        glfwSetCursor(window, cursor);
    }
    
    @Override
    public void dispose() {
        glfwDestroyCursor(cursor);
    }
}
