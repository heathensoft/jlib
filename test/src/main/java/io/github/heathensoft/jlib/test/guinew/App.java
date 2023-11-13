package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.primitives.Rectanglef;

import java.util.List;
import java.util.Objects;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */

public class App extends Application {

    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        config.settings_width = 1280;
        config.settings_height = 720;
        //config.settings_width = 1920;
        //config.settings_height = 1080;
        config.windowed_mode = true;
        config.auto_resolution = false;
        supported.add(Resolution.R_1280x720);
        supported.add(Resolution.R_1920x1080);
    }

    protected void on_start(Resolution resolution) throws Exception {
       GUI.INITIALIZE_GUI(resolution);
       Objects.requireNonNull(GUI.Windows.gui_windows_get("ColorPicker")).open();
    }

    protected void on_update(float delta) {
        GUI.gui_pre_render_update(delta);
        GUI.gui_start_rendering();
        GUI.gui_render_windows(delta);
        Rectanglef bounds = MathLib.rectf(40,140 - 40,100,140);

        GUI.Renderer.drawElement(bounds,0xFF000000);
        GUI.Renderer.drawStringDynamicSize("FFFFF",bounds,0,0xFFFFFFFF,0,true);
        //GUI.Renderer.drawStringFixedSize("|128| gg",0,0xFFFFFFFF,40,720,1000,40,0);
        //GUI.Renderer.drawStringDynamicSize("FFFFF",0,0xFFFFFFFF,40 ,360,100,40,0,false);
        //GUI.Renderer.drawStringDynamicSize("FFFFF",0,0xFFFFFFFF,40 ,200,80,40,0,false);
        //GUI.Renderer.drawStringDynamicSize("FFFFF",0,0xFFFFFFFF,40 ,140,60,40,0,false);
        GUI.gui_stop_rendering();
    }

    protected void on_render(float frame_time, float alpha) { GUI.gui_render_to_screen(); }

    protected void on_exit() { GUI.DISPOSE_GUI(); }

    protected void resolution_request(Resolution resolution) throws Exception {
        GUI.gui_resolution_request(resolution);
    }

    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }


}
