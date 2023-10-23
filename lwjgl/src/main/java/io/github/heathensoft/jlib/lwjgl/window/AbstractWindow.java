package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.io.Settings;

import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwTerminate;

/**
 * @author Frederik Dahl
 * 18/10/2022
 */


abstract class AbstractWindow implements PublicWindow {
    
    protected final int RED_BITS = 8;
    protected final int GREEN_BITS = 8;
    protected final int BLUE_BITS = 8;
    protected final int DEPTH_BITS = 24;
    protected final int STENCIL_BITS = 8;
    
    protected static final String LOAD_SETTINGS_ON_INIT = "load-settings-on-init";
    protected static final String SAVE_SETTINGS_ON_EXIT = "save-settings-on-exit";
    protected static final String SETTINGS_REFRESH_RATE = "settings-refresh-rate";
    protected static final String SETTINGS_HEIGHT = "settings-height";
    protected static final String SETTINGS_WIDTH = "settings-width";
    protected static final String RESIZABLE_WINDOW = "resizable-window";
    protected static final String AUTO_RESOLUTION = "auto-resolution";
    protected static final String AUTO_COLOR_BITS = "auto-color-bits";
    protected static final String CURSOR_ENABLED = "cursor-enabled";
    protected static final String SLEEP_ON_SYNC = "sleep-on-sync";
    protected static final String WINDOWED_MODE = "windowed-mode";
    protected static final String VSYNC_ENABLED = "vsync-enabled";
    protected static final String ANTIALIASING = "antialiasing";
    protected static final String WIN_TITLE = "window-title";
    protected static final String TARGET_FPS = "target-fps";
    protected static final String TARGET_UPS = "target-ups";
    protected static final String LIMIT_FPS = "limit-fps";

    protected abstract void loadSettings(Settings user, BootConfiguration app_default);
    protected abstract void initialize(List<Resolution> app_res_options) throws Exception;
    protected abstract void updateAppResolution() throws Exception;
    protected abstract void processInput(float dt);
    protected abstract void refreshViewport();
    protected abstract void show();
    protected abstract void hide();
    protected abstract void focus();
    protected abstract void restore();
    protected abstract void maximize();
    protected abstract void minimize();
    protected abstract void center();
    protected abstract void pollEvents();
    protected abstract void swapBuffers();
    protected abstract void terminate();
    protected abstract void signalToClose();
    protected abstract boolean shouldClose();
    protected abstract boolean shouldUpdateRes();
    protected abstract void setErrorCallback();
    protected abstract void setInputCallbacks();
    protected abstract void setDisplayCallbacks();
    protected abstract void freeErrorCallback();
    protected abstract void freeInputCallbacks();
    protected abstract void freeDisplayCallbacks();
    protected abstract void freeCursorObjects();
    protected final InputProcessor placeholder_processor = new InputProcessor() {};
    
    protected Exception failInit(String message) {
        glfwTerminate();
        freeErrorCallback();
        return new Exception(message);
    }
}
