package io.github.heathensoft.jlib.lwjgl.window;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;


import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Static utility methods for querying window monitors.
 *
 * @author Frederik Dahl
 * 27/10/2022
 */


public class Monitor {
    
    
    // return value depends on whether the window is iconified
    public static Optional<GLFWVidMode> videoMode(long monitor) {
        return Optional.ofNullable(glfwGetVideoMode(monitor));
    }
    
    public static Optional<GLFWVidMode.Buffer> videoModes(long monitor) {
        return Optional.ofNullable(glfwGetVideoModes(monitor));
    }
    
    public static String readableName(long monitor) {
        String name = glfwGetMonitorName(monitor);
        return name == null ? "NULL" : name;
    }
    
    public static DisplayOptions displayOptions(long monitor) {
        return new DisplayOptions(monitor);
    }
    
    public static boolean isConnected(long monitor) {
        PointerBuffer buffer = glfwGetMonitors();
        if (buffer != null) {
            for (int i = 0; i < buffer.capacity(); i++)
                if (monitor == (buffer.get(i))) return true;
        } return false;
    }
    
    public static boolean isPrimary(long monitor) {
        if (monitor == 0L) return false;
        return monitor == glfwGetPrimaryMonitor();
    }
    
    /**
     * @param window window handle
     * @return current monitor address or 0L if either
     * windowed or an error occurred.
     */
    public static long currentMonitor(long window) {
        return glfwGetWindowMonitor(window);
    }
    
    public static long primary() {
        return glfwGetPrimaryMonitor();
    }
    
    public static long[] allConnected() {
        PointerBuffer buffer = glfwGetMonitors();
        if (buffer == null) { return new long[0];
        } long[] monitors = new long[buffer.capacity()];
        for (int i = 0; i < monitors.length; i++) {
            monitors[i] = buffer.get(i);
        } return monitors;
    }
}
