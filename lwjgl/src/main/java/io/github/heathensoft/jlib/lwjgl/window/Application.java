package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.io.Folder;
import io.github.heathensoft.jlib.common.io.Settings;

import java.util.List;

/**
 *
 * Engine.get().run(Application app, String[] args);
 *
 * @author Frederik Dahl
 * 18/10/2022
 */


public abstract class Application {
    
    protected Settings settings;
    
    protected abstract void engine_init(
            List<Resolution> supported,
            BootConfiguration config,
            String[] args);
    
    protected abstract void on_start(Resolution resolution) throws Exception;

    /**
     *
     * @param delta delta time in seconds
     */
    protected abstract void on_update(float delta);
    
    protected abstract void on_render(float frame_time, float alpha);
    
    protected abstract void on_exit();
    
    protected abstract void resolution_request(Resolution resolution) throws Exception;
    
    
    public String name() { return "App"; }
    
    public String version() { return "0.0.1"; }
    
    public String framework() { return "HeathenSoft"; }
    
    public final Folder user_folder() { return Folder.user_home(framework(),name()); }
    
    public final Folder app_folder(Class<?> clazz) { return Folder.jar_adjacent(clazz); }
    
    public final Settings settings() {
        return settings == null ? new Settings(user_folder().file("settings")) : settings;
    }
}
