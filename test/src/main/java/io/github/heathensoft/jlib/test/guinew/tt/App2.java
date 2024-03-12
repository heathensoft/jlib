package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.ui.Window;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;

/**
 * @author Frederik Dahl
 * 23/02/2024
 */


public class App2 extends Application {

    private Vector4f clear_color;


    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        config.settings_width = 1280;
        config.settings_height = 720;
        //config.settings_width = 1920;
        //config.settings_height = 1080;
        config.windowed_mode = true;
        config.auto_resolution = false;
        supported.add(Resolution.R_1280x720);
        //supported.add(Resolution.R_1920x1080);
    }

    protected void on_start(Resolution resolution) throws Exception {
        //String name = "LiberationMono64";
        //ByteBuffer ttf = Resources.toBuffer("res/jlib/gui/ttf/LiberationMono-Regular.ttf",10 * 1024);
        //BitmapFont font = BitmapFont.create(name,ttf,64,2,0,0,GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR,true);
        //font.saveAsRepo(name,"");

        GUI.initialize(resolution);
        clear_color = new Vector4f(0.3f,0.3f,0.3f,1.0f);
        BoxWindow window = new BoxWindow(new Root(),"Window");
        //window.autoRestoreOnClose(true);
        GUI.windows.register(window);
        GUI.windows.openWindow(window);
        /*
        for (int i = 0; i < 2; i++) {

            TextField textField = new TextField(400,200,16,0,19);
            textField.enableEditing(true);
            Box nav_bar = new NavBar(Color.hex_to_rgb("202020FF",new Vector4f()),22);

            VBoxContainer v_box = new VBoxContainer();
            v_box.addBoxes(nav_bar, textField);
            //v_box.setInnerSpacing(3);

            RootTest rootContainer = new RootTest(true);
            rootContainer.setBorderPadding(6);
            rootContainer.addBox(v_box);

            String window_name = "TextField" + i;
            WindowAnchor anchor = WindowAnchor.NONE;
            BoxWindow window = new BoxWindow();
            window.create(rootContainer,anchor,window_name);
            window.open();
        }

         */


    }

    protected void on_update(float delta) {

        Optional<Window> optionalWindow = GUI.windows.getByName("Window");
        if (optionalWindow.isPresent()) {
            Window window = optionalWindow.get();
            if (!window.isOpen()) {
                if (GUI.keys.just_pressed(GLFW_KEY_O)) {
                    GUI.windows.openWindow(window);
                }
            }
        }
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
