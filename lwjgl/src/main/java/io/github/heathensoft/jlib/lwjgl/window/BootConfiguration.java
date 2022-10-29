package io.github.heathensoft.jlib.lwjgl.window;

import org.tinylog.configuration.Configuration;

import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;


/**
 * If user settings are not found/set or if the Application runs for the first time,
 * these settings are read as default when initializing a Window.
 * There is also a method to configure the logger.
 *
 * Notes:
 *
 * "target_fps" is only used when "vsync_enabled" is false and "limit_fps" is true.
 * vsync is very much recommended. This avoids screen tearing. Even with a monitor refresh rate
 * equal to "target_fps", screen tearing will occur.
 *
 * Disabling "limit-fps" and vsync is useful to find bottlenecks in the rendering cycle.
 * This gets you unlimited fps.
 *
 * "target_ups" is a hard value. The application will always update at a fixed rate.
 * InputProcessor callbacks are executed during the rendering-loop. (Polling of input events
 * from OS is called in the rendering cycle) But the InputProcessors process_input(float delta)
 * method is called in the update loop. (if the window is not minimized)
 * So keep this in mind for lower end ups values.
 *
 * @author Frederik Dahl
 * 18/10/2022
 */


public class BootConfiguration {
    
    /**
     * Before the window is created, the logger can be configured.
     * By default, the logger prints all severity levels to console.
     * There are many options, and they are easy to configure.
     * (See reference link). The singleton Logger exists statically,
     * and is available from any class. Override to implement.
     * @param key key
     * @param value value
     * @see <a href="https://tinylog.org/v2/configuration/">tinylog</a>
     */
    public void logger(final String key, final String value) {
        Configuration.set(key, value);
    }
    
    public String window_title = "";
    
    public boolean use_default_settings = true;
    public boolean resizable_window = false;
    public boolean auto_resolution = false;
    public boolean auto_color_bits = true;
    public boolean cursor_enabled = true;
    public boolean windowed_mode = true;
    public boolean vsync_enabled = true;
    public boolean antialiasing = false;
    public boolean sleep_on_sync = true;
    public boolean limit_fps = true;
    
    public int target_ups = 60;
    public int target_fps = 60;
    
    // display or windowed resolution of previous session
    public int settings_refresh_rate = GLFW_DONT_CARE;
    public int settings_height = GLFW_DONT_CARE;
    public int settings_width = GLFW_DONT_CARE;
    
    
}
