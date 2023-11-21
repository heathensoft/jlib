package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.gui.dev.ColorPickerWindow;
import io.github.heathensoft.jlib.gui.dev.TextBox;
import io.github.heathensoft.jlib.gui.state.GUI;
import io.github.heathensoft.jlib.gui.text.Text;
import io.github.heathensoft.jlib.gui.window.*;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector4f;

import java.util.List;

/**
 * @author Frederik Dahl
 * 22/11/2023
 */


public class App2 extends Application  {


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
        GUI.gui_initialize(resolution);
        new ColorPickerWindow().open();
        if (true == true) {
            Text text = new Text(20);
            text.useWordWrap(true);
            text.set("Text messaging, or texting, is the act of composing and sending electronic messages, " +
                    "typically consisting of alphabetic and numeric characters, " +
                    "between two or more users of mobile devices, desktops/laptops, " +
                    "or another type of compatible computer. " +
                    "Text messages may be sent over a cellular network or may also be sent via satellite or Internet connection.\n" +
                    "\n" +
                    "The term originally referred to messages sent using the Short Message Service (SMS)." +
                    " It has grown beyond alphanumeric text to include multimedia messages " +
                    "using the Multimedia Messaging Service (MMS) containing digital images, videos, and sound content, " +
                    "as well as ideograms known as emoji (happy faces, sad faces, and other icons), " +
                    "and instant messenger applications (usually the term is used when on mobile devices).");
            RootContainer rootContainer = new RootTest(5);
            EmptyBox emptyBox = new EmptyBox(200,32);
            emptyBox.lockVertical();
            VBoxContainer vBoxContainer = new VBoxContainer(2);
            vBoxContainer.addBox(emptyBox);
            TextBox textBox = new TextBox(new Vector4f(0,0,0,0.0f),200,100,5);
            textBox.setText(text);
            textBox.useParentID(true);
            vBoxContainer.addBox(textBox);
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            vBoxContainer.addBox(new Slider(14));
            rootContainer.add(vBoxContainer);
            WindowGUI textWindow = new WindowGUI("TextBox", Anchor.NONE);
            textWindow.create(rootContainer);
            textWindow.open();
        }
    }

    protected void on_update(float delta) {
        GUI.gui_render_windows(delta);
    }

    protected void on_render(float frame_time, float alpha) {
        GUI.gui_render_to_screen();
    }

    protected void on_exit() {
        GUI.gui_dispose();
    }

    protected void resolution_request(Resolution resolution) throws Exception {
        GUI.gui_request_resolution(resolution);
    }

    public static void main(String[] args) {
        Engine.get().run(new App2(),args);
    }
}
