package io.github.heathensoft.jlib.lwjgl.window;


import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;

/**
 * window methods for public use.
 *
 * @author Frederik Dahl
 * 18/10/2022
 */


interface PublicWindow {
    
    // -----------------------------------------------------------------
    // all these methods should update settings: width, height, refresh rate
    // and return true if desired resolution is the actual resolution on exiting method.
    // whether the resolution changed or not, isn't the concern.
    // for windowed, the resolution is the framebuffer.
    // for windowed, if the resolution equals or is larger than the display,
    // it might be a good idea to remove window decoration.
    // and if not, be sure to set decoration to true.
    
    boolean setDisplay(long monitor, Resolution resolution, int hz);
    
    default boolean setDisplay(long monitor, Resolution resolution) {
        return setDisplay(monitor,resolution,GLFW_DONT_CARE);
    }
    
    default boolean setDisplay(Resolution resolution, int hz) {
        return setDisplay(Monitor.currentMonitor(handle()),resolution,hz);
    }
    
    default boolean setDisplay(Resolution resolution) {
        return setDisplay(resolution,GLFW_DONT_CARE);
    }
    
    boolean setWindowed(Resolution resolution);
    // -----------------------------------------------------------------
    
    void setInputProcessor(InputProcessor processor);
    
    void setTitle(String string);
    void setTargetUPS(int value);
    void setTargetFPS(int value);
    void enableSaveSettingsOnExit(boolean enable);
    void enableAutoColorBits(boolean enable);
    void enableAntialiasing(boolean enable);
    void enableSleepOnSync(boolean enable);
    void enableResizable(boolean enable);
    void enableLimitFPS(boolean enable);
    void enableVSync(boolean enable);
    void enableCursor(boolean enable);
    void hideCursor(boolean enable);
    
    long handle();
    int getTargetUPS();
    int getTargetFPS();
    int getWindowX();
    int getWindowY();
    int getWindowW();
    int getWindowH();
    int getFramebufferW();
    int getFramebufferH();
    double getMouseScreenX();
    double getMouseScreenY();
    
    boolean isMinimized();
    boolean isWindowedMode();
    boolean isResizableEnabled();
    boolean isAutoColorBitsEnabled();
    boolean isCursorEnabled();
    boolean isVSyncEnabled();
    boolean isAntialiasingEnabled();
    boolean isUseDefaultSettingsEnabled();
    boolean isSleepOnSyncEnabled();
    boolean isLimitFPSEnabled();
    
    void saveSettings() throws IOException;
    
    /**
     * All options provided by the Application during its on_init() method.
     * These are potential resolutions the Application can be asked to run in.
     * Depending on the chosen display resolution, an appropriate resolution
     * that most matches that will be chosen from this list.
     * This list contain all valid options for windowed mode
     * @return possible app resolutions
     */
    List<Resolution> appResOptions();
    
    /**
     * @return current application resolution. Also be referred to
     * as the "virtual window" in some places in the doc.
     */
    Resolution appResolution();
    
    /**
     * The Viewport "openGL" will be rendering to. It will automatically
     * adapt to resizing and resolution changes of the Window. Maintaining the
     * desired aspect ratio while keeping the content visible on screen.
     * Also used to temporarily set the glViewport for framebuffer / off-screen
     * rendering. A manually set viewport resets to its prior size each frame
     * after refresh().
     * @return the viewport
     */
    Viewport viewport();
    
    /**
     * Creates and returns a cursor object. Cursors are freed on window termination.
     * If the image format is wrong, no cursor is created.
     * @param image a rgba8 format image (4 channels).
     * @param hotspotX the cursor hotspot offset x
     * @param hotspotY the cursor hotspot offset x
     * @return a cursor if nothing goes wrong. check the logs.
     */
    Optional<CursorObject> createCursor(Bitmap image, int hotspotX, int hotspotY);
    
    /**
     * Creates a standard cursor object. Cursors are freed on window termination.
     * @param shape one of the glfw standard shapes. i.e. GLFW_ARROW_CURSOR
     * @return a cursor if you use a valid enum
     * @see <a href="https://www.glfw.org/docs/3.3/group__shapes.html">standard shapes</a>
     */
    Optional<CursorObject> createCursor(int shape);
    
}
