package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.Assert;
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
    private static final int service_max_pool_size = 16;
    private static final int service_keep_alive_time_ms = 3000;
    
    private Engine() {}
    
    private Time time;
    private Window window;
    private Application app;
    private ThreadService service;
    
    public void run(Application app, String[] args) {
        Assert.notNull(app);
        if (this.app == null) {
            this.app = app;
            this.window = new Window();
            this.time = new Time();
            BootConfiguration config = new BootConfiguration();
            List<Resolution> app_res = new ArrayList<>();
            app.engine_init(app_res,config,args);
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
            Logger.info("loading window user settings");
            window.loadSettings(app.settings(),config);
            Logger.info("initializing window");
            try { window.initialize(app_res);
            } catch (Exception e) {
                Logger.error(e);
                return;
            } try {
                float delta;
                float alpha;
                float frameTime;
                float accumulator = 0f;
                Logger.info("starting application");
                app.on_start(window.appResolution());
                Logger.info("application is running");
                time.init();
                while (!window.shouldClose()) {
                    delta = 1f / window.getTargetUPS();
                    frameTime = time.frameTime();
                    accumulator += frameTime;
                    while (accumulator >= delta) {
                        if (!window.isMinimized()) {
                            window.processInput(delta);
                        } app.on_update(delta);
                        time.incUpsCount();
                        accumulator -= delta;
                    } alpha = accumulator / delta;
                    if (!window.isMinimized()) {
                        if (window.shouldUpdateRes()) {
                            window.updateAppResolution();
                        } window.refreshViewport();
                        app.on_render(frameTime,alpha);
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
                } Logger.info("exiting main loop");
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                Logger.info("exiting application");
                app.on_exit();
                Logger.info("terminating window");
                window.terminate();
                if (service != null) {
                    Logger.info("thread pool shutdown");
                    service.dispose();
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
    
    public Window window() {
        return window;
    }
    
    public Application app() {
        return app;
    }
    
    public <T extends Application> T app(Class<T> clazz) {
        if (app.getClass() != clazz) {
            throw new ClassCastException("wrong cast of application");
        } return clazz.cast(app);
    }
    
    public ThreadService service() {
        if (service == null) {
            service = new ThreadService(
            service_core_pool_size,
            service_max_pool_size,
            service_keep_alive_time_ms);
        } return service;
    }
    
    
    public static Engine get() {
        return instance == null ? (instance = new Engine()) : instance;
    }
    
    
}
