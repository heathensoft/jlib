package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.io.ExternalFile;
import io.github.heathensoft.jlib.common.io.Settings;

import java.io.IOException;
import java.nio.file.Path;
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
    
    public final Settings settings() throws IOException  {
        if (settings == null) {
            Path appFolder = ExternalFile.APP_DATA(framework(),name());
            Path settingsFile = appFolder.resolve("settings");
            new ExternalFile(settingsFile).createFile(false);
            settings = new Settings(settingsFile);
        } return settings;
    }
}
