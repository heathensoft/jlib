package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.gui.ny.GUI;
import io.github.heathensoft.jlib.gui.ny.WindowAnchor;
import io.github.heathensoft.jlib.gui.ny.box.BoxWindow;
import io.github.heathensoft.jlib.gui.ny.box.HBoxContainer;
import io.github.heathensoft.jlib.gui.ny.box.VBoxContainer;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector4f;

import java.util.List;

/**
 * @author Frederik Dahl
 * 23/02/2024
 */


public class App2 extends Application {

    private Vector4f clear_color;


    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        config.settings_width = 1280;
        config.settings_height = 720;
        config.windowed_mode = true;
        config.auto_resolution = false;
        supported.add(Resolution.R_1280x720);
    }

    protected void on_start(Resolution resolution) throws Exception {
        GUI.initialize(resolution);
        clear_color = new Vector4f(0.3f,0.3f,0.3f,1.0f);


        for (int j = 0; j < 0; j++) {
            String name = "Window Test" + j;
            WindowAnchor anchor = WindowAnchor.NONE;
            RootTest rootContainer = new RootTest();
            rootContainer.setBorderPadding(6);
            rootContainer.setStateInteractable(true);




            VBoxContainer vBoxContainer = new VBoxContainer();
            vBoxContainer.setInnerSpacing(3);
            for (int i = 0; i < 4; i++) {
                vBoxContainer.addBox(new BoxTest(400,50));
            }
            HBoxContainer hBoxContainer = new HBoxContainer();
            for (int i = 0; i < 3; i++) {
                hBoxContainer.addBox(new BoxTest(50,50));
            }
            hBoxContainer.setInnerSpacing(3);
            vBoxContainer.addBox(hBoxContainer);
            rootContainer.addBox(vBoxContainer);



            BoxWindow window = new BoxWindow();
            window.create(rootContainer,anchor,name);
            window.open();
        }


    }

    protected void on_update(float delta) {
        GUI.render_to_gui_framebuffer(delta);
    }

    protected void on_render(float frame_time, float alpha) {
        GUI.render_to_screen_default(clear_color);
    }

    protected void on_exit() {
        GUI.dispose();
    }

    protected void resolution_request(Resolution resolution) throws Exception {
        GUI.on_app_resolution_update(resolution);
    }

    public static void main(String[] args) {
        Engine.get().run(new App2(),args);
    }
}
