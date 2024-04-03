package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.window.*;
import io.github.heathensoft.jlib.test.guinew.tt.BubbleDemo;
import io.github.heathensoft.jlib.test.guinew.tt.MipTest;
import io.github.heathensoft.jlib.test.guinew.tt.TextTest;
import io.github.heathensoft.jlib.test.guinew.tt.ToggleTest;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;

/**
 * @author Frederik Dahl
 * 23/02/2024
 */


public class App extends Application {

    private BubbleDemo bubbleDemo;

    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        config.settings_width = 1280;
        config.settings_height = 720;
        //config.settings_width = 1920;
        //config.settings_height = 1080;
        //config.windowed_mode = false;
        config.auto_resolution = false;
        supported.add(Resolution.R_1280x720);
        supported.add(Resolution.R_1920x1080);
    }

    protected void on_start(Resolution resolution) throws Exception {
        //String name = "pixeltier_icon_pack";
        //String folder = "C:\\dump\\gui\\MaterialIcons\\aseprite\\pixelTier";
        //AtlasData atlas = TextureAtlas.pack(name,new WorkingDirectory(folder),0,GL_CLAMP_TO_EDGE,GL_NEAREST,GL_NEAREST,false,false);
        //atlas.export("");
        //atlas.dispose();
        //String name = "TradeWinds64";
        //ByteBuffer ttf = Resources.toBuffer("res/jlib/ui/fonts/ttf/TradeWinds-Regular.ttf",10 * 1024);
        //BitmapFont font = BitmapFont.create(name,ttf,64,2,0,0,0,GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR,true);
        //font.exportAsRepo("");
        //font.dispose();
        Framebuffer.setClearColor(new Vector4f(0.3f,0.3f,0.3f,1.0f));
        bubbleDemo = new BubbleDemo(resolution.width(),resolution.height());
        GUI.initialize(resolution);
        BoxWindow textWindow = new BoxWindow(new TextTest(),"TextWindow");
        BoxWindow toggleWindow = new BoxWindow(new ToggleTest(),"ToggleWindow");
        BoxWindow mipmapTestWindow = new BoxWindow(new MipTest(),"MipmapTestWindow");
        GUI.windows.register(textWindow);
        GUI.windows.register(toggleWindow);
        GUI.windows.register(mipmapTestWindow);
        GUI.windows.openWindow(textWindow);
        GUI.windows.openWindow(toggleWindow);
        GUI.windows.openWindow(mipmapTestWindow);
    }

    protected void on_update(float delta) {
        if (GUI.keys.just_pressed(GLFW_KEY_ESCAPE)) Engine.get().exit();
        GUI.render_to_gui_framebuffer(delta);
    }

    protected void on_render(float frame_time, float alpha) {
        bubbleDemo.renderDemo();
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        ShaderProgram.texturePass(bubbleDemo.texture());
        GUI.render_gui_to_screen(0);
    }

    protected void on_exit() { GUI.dispose(); Disposable.dispose(bubbleDemo); }
    protected void resolution_request(Resolution resolution) throws Exception { GUI.on_app_resolution_update(resolution); }
    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }
}
