package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.OS;
import io.github.heathensoft.jlib.common.io.Settings;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

/**
 * @author Frederik Dahl
 * 27/10/2022
 */


public class Window extends AbstractWindow {

    private InputProcessor current_processor;
    private List<Resolution> app_res_options;
    private CursorObjects cursorObjects;
    private Resolution app_resolution;
    private Viewport viewport;
    private Settings user;
    private String title;
    private long window;
    private int target_ups;
    private int target_fps;
    private int window_pos_x;
    private int window_pos_y;
    private int window_width;
    private int window_height;
    private int framebuffer_width;
    private int framebuffer_height;
    private int settings_width;
    private int settings_height;
    private int settings_refresh_rate;
    private double mouse_screen_x;
    private double mouse_screen_y;
    private boolean should_update_app_res;
    private boolean use_default_settings;
    private boolean resizable_window;
    private boolean auto_resolution;
    private boolean auto_color_bits;
    private boolean cursor_enabled;
    private boolean windowed_mode;
    private boolean vsync_enabled;
    private boolean antialiasing;
    private boolean sleep_on_sync;
    private boolean limit_fps;
    private boolean minimized;
    
    
    @Override
    protected void loadSettings(Settings user, BootConfiguration app_default) {

        if (!app_default.use_default_settings) {
            try { user.validate();
                user.load();
            } catch (IOException e) {
                Logger.warn(e,"unable to access settings file");
            }
        }
        settings_width = user.getInt(SETTINGS_WIDTH,app_default.settings_width);
        settings_height = user.getInt(SETTINGS_HEIGHT,app_default.settings_height);
        settings_refresh_rate = user.getInt(SETTINGS_REFRESH_RATE,app_default.settings_refresh_rate);
        resizable_window = user.getBool(RESIZABLE_WINDOW,app_default.resizable_window);
        auto_resolution = user.getBool(AUTO_RESOLUTION,app_default.auto_resolution);
        auto_color_bits = user.getBool(AUTO_COLOR_BITS,app_default.auto_color_bits);
        cursor_enabled = user.getBool(CURSOR_ENABLED,app_default.cursor_enabled);
        sleep_on_sync = user.getBool(SLEEP_ON_SYNC,app_default.sleep_on_sync);
        windowed_mode = user.getBool(WINDOWED_MODE,app_default.windowed_mode);
        vsync_enabled = user.getBool(VSYNC_ENABLED,app_default.vsync_enabled);
        antialiasing = user.getBool(ANTIALIASING,app_default.antialiasing);
        target_fps = user.getInt(TARGET_FPS,app_default.target_fps);
        target_ups = user.getInt(TARGET_UPS,app_default.target_ups);
        limit_fps = user.getBool(LIMIT_FPS,app_default.limit_fps);
        title = user.getString(WIN_TITLE,app_default.window_title);
        this.user = user;
    }
    
    @Override
    protected void initialize(List<Resolution> app_res_options) throws Exception {
        if (app_res_options.isEmpty()) {
            throw new Exception("the application failed provide any resolutions");
        } this.app_res_options = app_res_options;
    
        Logger.debug("setting glfw error callback");
        
        setErrorCallback();
        if (!glfwInit()) {
            freeErrorCallback();
            throw new Exception("unable to initialize glfw");
        }
        Logger.debug("glfw library initialized");
    
        Logger.debug("querying for primary monitor");
        long monitor = Monitor.primary();
        if (monitor == 0L) {
            throw failInit("unable to detect primary monitor");
        }
        Optional<GLFWVidMode> opt = Monitor.videoMode(monitor);
        if (opt.isEmpty()) throw failInit("unable to use monitor display");
        GLFWVidMode display = opt.get();
        Logger.debug("current monitor display: {}:{} and hz: {}",
        display.width(),display.height(),display.refreshRate());
    
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CENTER_CURSOR, GLFW_TRUE);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_RESIZABLE, resizable_window ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, antialiasing  ? 4 : 0);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);
        glfwWindowHint(GLFW_RED_BITS, auto_color_bits ? display.redBits() : RED_BITS);
        glfwWindowHint(GLFW_BLUE_BITS, auto_color_bits ? display.blueBits() : BLUE_BITS);
        glfwWindowHint(GLFW_GREEN_BITS, auto_color_bits ? display.greenBits() : GREEN_BITS);
        if (OS.name == OS.NAME.MAC) {
            Logger.debug("using forward compatibility for MAC user");
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            Logger.debug("using opengl core profile for non-MAC user");
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        }
        
        if (!auto_resolution) {
            Resolution desiredRes = new Resolution(settings_width,settings_height);
            if (windowed_mode) {
                if (app_res_options.contains(desiredRes)) {
                    window = glfwCreateWindow(desiredRes.width(),
                    desiredRes.height(),title,0L,0L);
                } else auto_resolution = true;
            } else {
                int desiredHZ = settings_refresh_rate;
                DisplayOptions options = Monitor.displayOptions(monitor);
                if (options.supports(desiredRes,desiredHZ)) {
                    glfwWindowHint(GLFW_REFRESH_RATE,desiredHZ);
                    window = glfwCreateWindow(desiredRes.width(),
                    desiredRes.height(),title,monitor,0L);
                } else auto_resolution = true;
            }
        }
        if (auto_resolution) {
            glfwWindowHint(GLFW_REFRESH_RATE,display.refreshRate());
            user.setInt(SETTINGS_REFRESH_RATE,display.refreshRate());
            user.setInt(SETTINGS_HEIGHT,display.height());
            user.setInt(SETTINGS_WIDTH,display.width());
            user.setBool(WINDOWED_MODE,windowed_mode = false);
            user.setBool(AUTO_RESOLUTION,auto_resolution = true);
            window = glfwCreateWindow(display.width(),
            display.height(),title,monitor,0L);
        }
        if (window == 0L) throw failInit("failed to create window");
    
        Logger.debug("querying actual window and framebuffer size");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetWindowPos(window,w,h);
            window_pos_x = w.get(0);
            window_pos_y = h.get(0);
            glfwGetWindowSize(window,w,h);
            window_width = w.get(0);
            window_height = h.get(0);
            glfwGetFramebufferSize(window,w,h);
            framebuffer_width = w.get(0);
            framebuffer_height = h.get(0);
            Logger.debug("created window: {},{},{},{}",
            window_pos_x,window_pos_y,window_width,window_height);
            Logger.debug("with a window framebuffer of res: {}:{}",
            framebuffer_width,framebuffer_height);
            DoubleBuffer mx = stack.mallocDouble(1);
            DoubleBuffer my = stack.mallocDouble(1);
            glfwGetCursorPos(window,mx,my);
            mouse_screen_x = mx.get(0);
            mouse_screen_y = window_height - my.get(0);
        }
    
        Logger.debug("choosing resolution for application");
        Resolution framebuffer = new Resolution(framebuffer_width,framebuffer_height);
        List<Resolution> list = new ArrayList<>(app_res_options);
        Resolution.sortByClosest(framebuffer,list);
        app_resolution = list.get(0);
        Logger.debug("application resolution: {}:{}",
        app_resolution.width(),app_resolution.height());
    
        viewport = new Viewport(framebuffer,app_resolution);
        
        if (windowed_mode) { // centering relative to primary
            glfwSetWindowPos( window,
            Math.round((display.width() - window_width) / 2f),
            Math.round((display.height() - window_height) / 2f));
        }
    
        Logger.debug("setting up callbacks");
        current_processor = placeholder_processor;
        setDisplayCallbacks();
        setInputCallbacks();

        glfwMakeContextCurrent(window);
        Logger.debug("opengl-context current in Thread: {}",
        Thread.currentThread().getName());
        glfwSetInputMode(window, GLFW_CURSOR,
        cursor_enabled ? GLFW_CURSOR_NORMAL: GLFW_CURSOR_DISABLED);
        glfwSwapInterval(vsync_enabled ? 1 : 0);
        Logger.debug("initializing standard cursor objects");
        cursorObjects = new CursorObjects(window);
        glfwShowWindow(window);
        GL.createCapabilities();
        
    }
    
    @Override
    protected void updateAppResolution() throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(window,w,h);
            framebuffer_width = w.get(0);
            framebuffer_height = h.get(0);
            Resolution framebuffer = new Resolution(framebuffer_width,framebuffer_height);
            List<Resolution> list = new ArrayList<>(app_res_options);
            Resolution.sortByClosest(framebuffer,list);
            Resolution closest = list.get(0);
            if (!app_resolution.equals(closest)) {
                Engine.get().app().resolution_request(closest);
                Logger.debug("updating app resolution from: {}:{}",app_resolution.width(),app_resolution.height());
                Logger.debug("updating app resolution to:   {}:{}",closest.width(),closest.height());
                Logger.debug("setting viewport to reflect changes");
                viewport.requestResolution(framebuffer_width,framebuffer_height,closest);
                app_resolution = closest;
            } should_update_app_res = false;
        }
    }
    
    @Override
    protected void processInput(float dt) {
        current_processor.process_input(dt);
    }
    
    @Override
    protected void refreshViewport() {
        viewport.refresh();
    }
    
    @Override
    protected void show() {
        glfwShowWindow(window);
    }
    
    @Override
    protected void hide() {
        glfwHideWindow(window);
    }
    
    @Override
    protected void focus() {
        glfwFocusWindow(window);
    }
    
    @Override
    protected void restore() {
        glfwRestoreWindow(window);
    }
    
    @Override
    protected void maximize() {
        glfwIconifyWindow(window);
    }
    
    @Override
    protected void minimize() {
        glfwMaximizeWindow(window);
    }
    
    @Override
    protected void center() {
    
    }
    
    @Override
    protected void pollEvents() {
        glfwPollEvents();
    }
    
    @Override
    protected void swapBuffers() {
        glfwSwapBuffers(window);
    }
    
    @Override
    protected void terminate() {
        if (!use_default_settings) {
            Logger.info("saving user settings");
            try { saveSettings();
            } catch (IOException e) {
                Logger.warn(e,"unable to save settings");
            }
        }
        Logger.debug("freeing cursor objects");
        freeCursorObjects();
        Logger.debug("clearing opengl capabilities");
        GL.setCapabilities(null);
        Logger.debug("freeing glfw input and display callbacks");
        freeInputCallbacks();
        freeDisplayCallbacks();
        Logger.debug("destroying the glfw window");
        glfwDestroyWindow(window);
        Logger.debug("terminating glfw");
        glfwTerminate();
        Logger.debug("freeing error callback");
        freeErrorCallback();
    }
    
    @Override
    protected void signalToClose() {
        glfwSetWindowShouldClose(window,true);
    }
    
    @Override
    protected boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }
    
    @Override
    protected boolean shouldUpdateRes() {
        return should_update_app_res;
    }
    
    @Override
    protected void setErrorCallback() {
        glfwSetErrorCallback(error_callback);
    }
    
    @Override
    protected void setInputCallbacks() {
        glfwSetDropCallback(window,drop_callback);
        glfwSetKeyCallback(window,key_callback);
        glfwSetCharCallback(window,char_callback);
        glfwSetCursorEnterCallback(window,cursor_enter_callback);
        glfwSetCursorPosCallback(window,cursor_hover_callback);
        glfwSetMouseButtonCallback(window,mouse_button_callback);
        glfwSetScrollCallback(window,mouse_scroll_callback);
        glfwSetJoystickCallback(joystick_callback);
    }
    
    @Override
    protected void setDisplayCallbacks() {
        glfwSetWindowSizeCallback(window,window_size_callback);
        glfwSetWindowPosCallback(window,window_position_callback);
        glfwSetWindowIconifyCallback(window,window_iconify_callback);
        glfwSetFramebufferSizeCallback(window,framebuffer_size_callback);
        glfwSetMonitorCallback(monitor_callback);
    }
    
    @Override
    protected void freeErrorCallback() {
        GLFWErrorCallback c = glfwSetErrorCallback(null);
        if (c != null) c.free();
    }
    
    @Override
    protected void freeInputCallbacks() {
        List<Callback> list = new ArrayList<>();
        list.add(glfwSetDropCallback(window,null));
        list.add(glfwSetKeyCallback(window,null));
        list.add(glfwSetCharCallback(window,null));
        list.add(glfwSetCursorEnterCallback(window,null));
        list.add(glfwSetCursorPosCallback(window,null));
        list.add(glfwSetMouseButtonCallback(window,null));
        list.add(glfwSetScrollCallback(window,null));
        list.add(glfwSetJoystickCallback(null));
        for (Callback c : list) if (c != null) c.free();
    }
    
    @Override
    protected void freeDisplayCallbacks() {
        List<Callback> list = new ArrayList<>();
        list.add(glfwSetMonitorCallback(null));
        list.add(glfwSetWindowSizeCallback(window,null));
        list.add(glfwSetWindowPosCallback(window,null));
        list.add(glfwSetWindowIconifyCallback(window,null));
        list.add(glfwSetFramebufferSizeCallback(window,null));
        for (Callback c : list) if (c != null) c.free();
    }

    @Override
    protected void freeCursorObjects() {
        Disposable.dispose(cursorObjects);
    }

    @Override
    public boolean setDisplay(long monitor, Resolution resolution, int hz) {
        Logger.info("attempting to set display");
        if (Monitor.isConnected(monitor)) {
            DisplayOptions options = Monitor.displayOptions(monitor);
            if (options.supports(resolution,hz)) {
                Logger.debug("calling glfwSetMonitor");
                glfwSetWindowMonitor(window,monitor,0,0,
                resolution.width(),resolution.height(),hz);
                should_update_app_res = true;
                long currentMonitor = Monitor.currentMonitor(window);
                if (monitor == currentMonitor) {
                    Optional<GLFWVidMode> optional = Monitor.videoMode(monitor);
                    if (optional.isPresent()) {
                        GLFWVidMode vidMode = optional.get();
                        int w = vidMode.width();
                        int h = vidMode.height();
                        int r = hz == GLFW_DONT_CARE ? hz : vidMode.refreshRate();
                        Resolution newResolution = new Resolution(w,h);
                        if (resolution.equals(newResolution) && r == hz) {
                            Logger.info("successfully set display to desired");
                            settings_refresh_rate = r;
                            settings_height = h;
                            settings_width = w;
                            windowed_mode = false;
                            return true;
                        }
                    }
                } Logger.info("new display does not match desired display exactly");
            }
            else Logger.warn("monitor does not support resolution");
        }
        else Logger.warn("could not locate monitor: {}",monitor);
        windowed_mode = Monitor.currentMonitor(window) == 0L;
        return false;
    }
    
    @Override
    public boolean setWindowed(Resolution resolution) {
        if (app_res_options.contains(resolution)) {
            long monitor = Monitor.currentMonitor(window);
            if (monitor == 0L) { // already in windowed mode
                glfwSetWindowSize(window,resolution.width(),resolution.height());
            } else {
                glfwSetWindowMonitor(window,0L,0,0,
                resolution.width(),resolution.height(), GLFW_DONT_CARE);
                // todo
                center();
            } windowed_mode = Monitor.currentMonitor(window) == 0L;
            settings_refresh_rate = GLFW_DONT_CARE;
            settings_height = resolution.height();
            settings_width = resolution.width();
            should_update_app_res = true;
            return true;
        } Logger.warn("app does not support resolution");
        return false;
    }
    
    @Override
    public void setInputProcessor(InputProcessor processor) {
        if (processor != current_processor) {
            String descriptor;
            if (processor == null) {
                descriptor = "none";
                current_processor = placeholder_processor;
            }
            else {
                descriptor = processor.getClass().getSimpleName();
                current_processor = processor;
                double x = mouse_screen_x;
                double y = mouse_screen_y;
                x *= ((float)framebuffer_width / window_width);
                y *= ((float)framebuffer_height / window_height);
                x = (x - viewport.x()) * viewport.w_inv();
                y = (y - viewport.y()) * viewport.h_inv();
                current_processor.on_activation(x,y);
            }
            Logger.info("setting window processor: " + descriptor);
        }

    }

    @Override
    public void setTitle(String string) {
        if (string != null) { title = string;
            glfwSetWindowTitle(window,title);
        }
    }

    @Override
    public void setClipboard(String string) {
        if (string != null) glfwSetClipboardString(window,string);
    }

    @Override
    public void setTargetUPS(int value) {
        target_ups = Math.max(1,value);
    }
    
    @Override
    public void setTargetFPS(int value) {
        target_fps = Math.max(1,value);
    }
    
    @Override
    public void enableSaveSettingsOnExit(boolean enable) {
        use_default_settings = !enable;
    }
    
    @Override
    public void enableAutoColorBits(boolean enable) {
        auto_color_bits = enable;
    }
    
    @Override
    public void enableAntialiasing(boolean enable) {
        antialiasing = enable;
    }
    
    @Override
    public void enableSleepOnSync(boolean enable) {
        sleep_on_sync = enable;
    }
    
    @Override
    public void enableResizable(boolean enable) {
        resizable_window = enable;
        glfwSetWindowAttrib(window,GLFW_RESIZABLE,enable ? GLFW_TRUE : GLFW_FALSE);
    }
    
    @Override
    public void enableLimitFPS(boolean enable) {
        limit_fps = enable;
    }
    
    @Override
    public void enableVSync(boolean enable) {
        vsync_enabled = enable;
    }
    
    @Override
    public void enableCursor(boolean enable) {
        cursor_enabled = enable;
        glfwSetInputMode(window,GLFW_CURSOR,
        enable ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);
    }
    
    @Override
    public void hideCursor(boolean enable) {
        if (enable) { glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_HIDDEN);
        } else { glfwSetInputMode(window,GLFW_CURSOR,
        cursor_enabled ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);}
    }
    
    @Override
    public long handle() {
        return window;
    }
    
    @Override
    public int getTargetUPS() {
        return target_ups;
    }
    
    @Override
    public int getTargetFPS() {
        return target_fps;
    }
    
    @Override
    public int getWindowX() {
        return window_pos_x;
    }
    
    public int getWindowY() {
        return window_pos_y;
    }
    
    @Override
    public int getWindowW() {
        return window_width;
    }
    
    @Override
    public int getWindowH() {
        return window_height;
    }
    
    @Override
    public int getFramebufferW() {
        return framebuffer_width;
    }
    
    @Override
    public int getFramebufferH() {
        return framebuffer_height;
    }
    
    @Override
    public double getMouseScreenX() {
        return mouse_screen_x;
    }
    
    @Override
    public double getMouseScreenY() {
        return mouse_screen_y;
    }
    
    @Override
    public boolean isMinimized() {
        return minimized;
    }
    
    @Override
    public boolean isWindowedMode() {
        return windowed_mode;
    }
    
    @Override
    public boolean isResizableEnabled() {
        return resizable_window;
    }
    
    @Override
    public boolean isAutoColorBitsEnabled() {
        return auto_color_bits;
    }
    
    @Override
    public boolean isCursorEnabled() {
        return cursor_enabled;
    }
    
    @Override
    public boolean isVSyncEnabled() {
        return vsync_enabled;
    }
    
    @Override
    public boolean isAntialiasingEnabled() {
        return antialiasing;
    }
    
    @Override
    public boolean isUseDefaultSettingsEnabled() {
        return use_default_settings;
    }
    
    @Override
    public boolean isSleepOnSyncEnabled() {
        return sleep_on_sync;
    }
    
    @Override
    public boolean isLimitFPSEnabled() {
        return limit_fps;
    }
    
    @Override
    public void saveSettings() throws IOException {
        user.setInt(SETTINGS_WIDTH,settings_width);
        user.setInt(SETTINGS_HEIGHT,settings_height);
        user.setInt(SETTINGS_REFRESH_RATE,settings_refresh_rate);
        user.setBool(RESIZABLE_WINDOW,resizable_window);
        user.setBool(AUTO_RESOLUTION,auto_resolution);
        user.setBool(AUTO_COLOR_BITS,auto_color_bits);
        user.setBool(CURSOR_ENABLED,cursor_enabled);
        user.setBool(SLEEP_ON_SYNC,sleep_on_sync);
        user.setBool(WINDOWED_MODE,windowed_mode);
        user.setBool(VSYNC_ENABLED,vsync_enabled);
        user.setBool(ANTIALIASING,antialiasing);
        user.setInt(TARGET_FPS,target_fps);
        user.setInt(TARGET_UPS,target_ups);
        user.setBool(LIMIT_FPS,limit_fps);
        user.save();
    }

    @Override
    public String getClipboard() {
        return glfwGetClipboardString(window);
    }

    @Override
    public InputProcessor inputProcessor() {
        return current_processor;
    }

    @Override
    public List<Resolution> appResOptions() {
        return app_res_options;
    }
    
    @Override
    public Resolution appResolution() {
        return app_resolution;
    }
    
    @Override
    public Viewport viewport() {
        return viewport;
    }

    @Override
    public CursorObjects cursorObjects() {
        return cursorObjects;
    }

    private final GLFWDropCallback drop_callback = new GLFWDropCallback() {
        public void invoke(long window, int count, long names) {
            List<String> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(GLFWDropCallback.getName(names,i));
                current_processor.on_file_drop(list);
            }
        }
    };

    private final GLFWMonitorCallback monitor_callback = new GLFWMonitorCallback() {
        public void invoke(long monitor, int event) {
            if (event == GLFW_DISCONNECTED) {
                long current = Monitor.currentMonitor(window);
                if (monitor == current) {
                    windowed_mode = true;
                }
            }
        }
    };
    
    private final GLFWWindowSizeCallback window_size_callback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int width, int height) {
            window_width = width;
            window_height = height;
        }
    };
    
    private final GLFWWindowPosCallback window_position_callback = new GLFWWindowPosCallback() {
        @Override
        public void invoke(long window, int x, int y) {
            window_pos_x = x;
            window_pos_y = y;
        }
    };
    
    private final GLFWWindowIconifyCallback window_iconify_callback = new GLFWWindowIconifyCallback() {
        @Override
        public void invoke(long window, boolean iconified) {
            minimized = iconified;
        }
    };
    
    private final GLFWFramebufferSizeCallback framebuffer_size_callback = new GLFWFramebufferSizeCallback() {
        @Override
        public void invoke(long window, int width, int height) {
            if (width > 0 && height > 0) {
                framebuffer_width = width;
                framebuffer_height = height;
                viewport.fit(width,height);
            }
        }
    };
    
    private final GLFWKeyCallback key_callback = new GLFWKeyCallback() {
        //private int lastKey = GLFW_KEY_UNKNOWN;
        public void invoke(long window, int key, int scancode, int action, int mods) {
            /*  if (key != GLFW_KEY_UNKNOWN && key < GLFW_KEY_LAST && action != GLFW_REPEAT) {
                key = action == GLFW_PRESS ? key : -key;
                if (key != lastKey) current_processor.on_key_event(lastKey = key);
            }*/
            if (key != GLFW_KEY_UNKNOWN && key < GLFW_KEY_LAST) {
                key = action != GLFW_RELEASE ? key : -key;
                current_processor.on_key_event(key,mods);
            }
        }
    };
    
    private final GLFWCharCallback char_callback = new GLFWCharCallback() {
        @Override
        public void invoke(long window, int codepoint) {
            if ((codepoint & 0x7F) == codepoint) {
                current_processor.on_char_press(codepoint);
            }

        }
    };
    
    private final GLFWCursorEnterCallback cursor_enter_callback = new GLFWCursorEnterCallback() {
        @Override
        public void invoke(long window, boolean enter) {
            current_processor.on_mouse_enter(enter);
        }
    };
    
    private final GLFWCursorPosCallback cursor_hover_callback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            y = window_height - y;
            mouse_screen_x = x;
            mouse_screen_y = y;
            x *= ((float)framebuffer_width / window_width);
            y *= ((float)framebuffer_height / window_height);
            x = (x - viewport.x()) * viewport.w_inv();
            y = (y - viewport.y()) * viewport.h_inv();
            current_processor.on_mouse_hover(x,y);
        }
    };
    
    private final GLFWMouseButtonCallback mouse_button_callback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            current_processor.on_mouse_press(button, action == GLFW_PRESS);
        }
    };
    
    private final GLFWScrollCallback mouse_scroll_callback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double vertical, double horizontal) {
            current_processor.on_mouse_scroll((float)horizontal);
        }
    };
    
    private final GLFWJoystickCallback joystick_callback = new GLFWJoystickCallback() {
        @Override
        public void invoke(int id, int event) {
            if (event == GLFW_CONNECTED) {
                current_processor.controller_connect(id,true);
            } else if (event == GLFW_DISCONNECTED) {
                current_processor.controller_connect(id,false);
            }
        }
    };
    
    private final GLFWErrorCallback error_callback = new GLFWErrorCallback() {
        @Override
        public void invoke(int error, long description) {
            if (error != GLFW_NO_ERROR) {
                Logger.error("GLFW ERROR[{}]: {}",error,GLFWErrorCallback.getDescription(description));
            }
        }
    };

}
