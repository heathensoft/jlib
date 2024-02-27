package io.github.heathensoft.jlib.lwjgl.window;


import io.github.heathensoft.jlib.common.thread.ThreadService;
import org.lwjgl.Version;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 *
 *
 * @author Frederik Dahl
 * 28/10/2022
 */


public final class Engine {

    private static Engine instance;
    private static final int service_core_pool_size = 4;
    private static final int service_max_pool_size = 24;
    private static final int service_keep_alive_time_ms = 3000;
    
    private Engine() {}
    
    private Time time;
    private Window window;
    private Application app;
    private DefaultInput input;
    private GLContext glContext;
    private ThreadService threadPool;

    public void run(Application app, String[] args) {
        if (this.app == null) {
            this.app = app;
            this.window = new Window();
            this.time = new Time();
            List<Resolution> app_res = new ArrayList<>();
            BootConfiguration config = new BootConfiguration();
            config.logger("writer","console");
            config.logger("writer.format","{date: HH:mm:ss.SS} {level}: {message}");
            app.set_state(Application.State.INITIALIZING);
            app.engine_init(app_res,config,args);
            app.set_state(Application.State.WAITING_TO_START);
            Logger.info("logger configured, welcome");
            int memory = (int)(Runtime.getRuntime().maxMemory() / 1000000L);
            int processors = Runtime.getRuntime().availableProcessors();
            String os_name = System.getProperty("os.name");
            String os_arch = System.getProperty("os.arch");
            String os_version = System.getProperty("os.version");
            Logger.info("running on: {} version {}, {} platform", os_name,os_version,os_arch);
            Logger.info("java version: {}", System.getProperty("java.version"));
            Logger.info("lwjgl version: {}", Version.getVersion());
            Logger.info("reserved memory: {}MB", memory);
            Logger.info("available processors: {}", processors);
            Logger.info("application has provided: {} resolution options",app_res.size());
            try {
                Logger.info("loading window user settings");
                window.loadSettings(app.settings(),config);
                Logger.info("initializing window");
                window.initialize(app_res);
            } catch (Exception e) {
                Logger.error(e);
                return;
            } try {
                float delta;
                float alpha;
                float frameTime;
                float accumulator = 0f;
                glContext = new GLContext(window.handle());
                Logger.info("starting application");
                app.set_state(Application.State.STARTING_UP);
                app.on_start(window.appResolution());
                app.set_state(Application.State.MAIN_LOOP_WAITING);
                Logger.info("application is running");
                time.init();
                while (!window.shouldClose()) {
                    delta = 1f / window.getTargetUPS();
                    frameTime = time.frameTime();
                    accumulator += frameTime;
                    while (accumulator >= delta) {
                        if (threadPool != null)
                            threadPool.update();
                        if (!window.isMinimized()) {
                            window.processInput(delta);
                        } app.set_state(Application.State.UPDATING);
                        app.on_update(delta);
                        app.set_state(Application.State.MAIN_LOOP_WAITING);
                        time.incUpsCount();
                        accumulator -= delta;
                    } alpha = accumulator / delta;
                    if (!window.isMinimized()) {
                        if (window.shouldUpdateRes()) {
                            app.set_state(Application.State.UPDATING_RESOLUTION);
                            window.updateAppResolution();
                            app.set_state(Application.State.MAIN_LOOP_WAITING);
                        } window.refreshViewport();
                        app.set_state(Application.State.RENDERING);
                        app.on_render(frameTime,alpha);
                        app.set_state(Application.State.MAIN_LOOP_WAITING);
                        window.swapBuffers();
                    } window.pollEvents();
                    time.incFpsCount();
                    time.update();
                    if (!window.isVSyncEnabled()) {
                        if (window.isLimitFPSEnabled()) {
                            double lastFrame = time.lastFrame();
                            double now = time.timeSeconds();
                            float targetTime = 0.96f / window.getTargetFPS();
                            boolean sleep = window.isSleepOnSyncEnabled();
                            while (now - lastFrame < targetTime) {
                                if (sleep) { Thread.yield();
                                    Thread.sleep(1);
                                } now = time.timeSeconds();
                            }
                        }
                    }
                } app.set_state(Application.State.WAITING_TO_EXIT);
                Logger.info("exiting main loop");
            } catch (Exception e) {
                Logger.error(e);
                Logger.error("App State: {}",app.state().description);
            } finally {
                Logger.info("exiting application");
                app.set_state(Application.State.EXITING);
                app.on_exit();
                Logger.info("terminating window");
                window.terminate();
                if (threadPool != null) {
                    try {
                        for (int i = 0; i < 60; i++) {
                            Thread.sleep(16);
                            threadPool.update();
                        } Logger.info("thread pool, shutdown");
                        threadPool.dispose();
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }
            }
        }
    }
    
    public void exit() {
        instance.window().signalToClose();
        Logger.info("window signaled to close");
    }

    public Time time() {
        return time;
    }

    public DefaultInput input() {
        if (input == null) {
            input = new DefaultInput();
            window.setInputProcessor(input);
        } else { InputProcessor current = window.inputProcessor();
            if (current != input) window.setInputProcessor(input);
        } return input;
    }
    
    public Window window() {
        return window;
    }

    public GLContext glContext() { return glContext; }
    
    public Application app() {
        return app;
    }
    
    public <T extends Application> T app(Class<T> clazz) {
        if (app.getClass() != clazz) {
            throw new ClassCastException("wrong cast of application");
        } return clazz.cast(app);
    }
    
    public ThreadService threadPool() {
        if (threadPool == null) {
            threadPool = new ThreadService(
            service_core_pool_size,
            service_max_pool_size,
            service_keep_alive_time_ms);
        } return threadPool;
    }

    public static Engine get() {
        return instance == null ? (instance = new Engine()) : instance;
    }

}
