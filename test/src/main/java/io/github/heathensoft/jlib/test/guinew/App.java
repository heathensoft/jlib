package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.io.ExternalFile;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.dev.TextBox;
import io.github.heathensoft.jlib.gui.text.Text;
import io.github.heathensoft.jlib.gui.window.*;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector4f;

import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */

public class App extends Application {

    private Text text;

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

        if (true == true) {
           text = new Text(20);
           text.useWordWrap(true);
           text.set("Nunc hendrerit ornare rhoncus. Mauris eget diam placerat, " +
                   "venenatis augue vitae, mollis nibh. Aenean blandit dolor ac lectus tincidunt, " +
                   "id malesuada turpis volutpat. Pellentesque pulvinar iaculis arcu, eu varius sapien fermentum quis. " +
                   "Etiam at tortor lectus. Sed sollicitudin metus vel tortor consequat fermentum. Morbi viverra dapibus lobortis. " +
                   "Mauris tellus metus, porta id faucibus id, imperdiet aliquam massa. In libero augue, commodo vitae risus at");
           RootContainer rootContainer = new RootTest(5);
           EmptyBox emptyBox = new EmptyBox(200,32);
           emptyBox.lockVertical();
           VBoxContainer vBoxContainer = new VBoxContainer(0);
           vBoxContainer.addBox(emptyBox);
           TextBox textBox = new TextBox(new Vector4f(0,0,0,0.0f),200,200,5);
           textBox.setText(text);
           textBox.useParentID(true);
           vBoxContainer.addBox(textBox);
           rootContainer.add(vBoxContainer);
           WindowGUI textWindow = new WindowGUI("TextBox", Anchor.NONE);
           textWindow.create(rootContainer);
           textWindow.open();
       }


    }


    protected void on_update(float delta) {

        GUI.gui_pre_render_update(delta);
        GUI.gui_start_rendering();
        GUI.gui_render_windows(delta);
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
