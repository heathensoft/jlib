package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;
import io.github.heathensoft.jlib.test.guinew.tt.*;
import io.github.heathensoft.jlib.test.ui.GridRoot;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.WindowAnchor;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.BackGround;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;

/**
 * @author Frederik Dahl
 * 23/02/2024
 */


public class App extends Application {

    private BubbleDemo bubbleDemo;
    private Texture bg_texture;

    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        config.settings_width = 1280;
        config.settings_height = 720;
        //config.settings_width = 1920;
        //config.settings_height = 1080;
        config.windowed_mode = true;
        config.auto_resolution = false;
        supported.add(Resolution.R_1280x720);
        //supported.add(Resolution.R_1920x1080);
        //config.limit_fps = false;
        //config.vsync_enabled = false;
    }

    protected void on_start(Resolution resolution) throws Exception {
        //String name = "icons";
        //String folder = "C:\\dump\\gui\\MaterialIcons\\All";
        //AtlasData atlas = TextureAtlas.pack(name,new WorkingDirectory(folder),1,GL_CLAMP_TO_EDGE,GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR,true,false);
        //atlas.export("");
        //atlas.dispose();
        //String name = "TradeWinds64";
        //ByteBuffer ttf = Resources.toBuffer("res/jlib/ui/fonts/ttf/TradeWinds-Regular.ttf",10 * 1024);
        //BitmapFont font = BitmapFont.create(name,ttf,64,2,0,0,0,GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR,true);
        //font.exportAsRepo("");
        //font.dispose();





        Framebuffer.setClearColor(new Vector4f(0.0f,0.0f,0.0f,1.0f));
        bubbleDemo = new BubbleDemo(resolution.width(),resolution.height());
        GUI.initialize(resolution);


        ColorTest colorTest = new ColorTest();
        GridRoot gridRoot = new GridRoot();


        if (true) {
            try {
                Bitmap bitmap = Resources.image("res/jlib/ui/img/bg_stars.png");
                bg_texture = bitmap.asTexture();
                bg_texture.bindToActiveSlot();
                bg_texture.textureRepeat();
                bg_texture.filterLinear();
                bitmap.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BackGround backGround = new BackGround(bg_texture,new Vector4f(0.3f,0.4f,0.4f,1f));
            backGround.velocityPixels().set(5,0);
            colorTest.setBackground(backGround);
            gridRoot.setBackground(backGround);

        }

        BoxWindow gridWindow = new BoxWindow(gridRoot,"GridWindow");
        GUI.windows.register(gridWindow);
        GUI.windows.openWindow(gridWindow);

        BoxWindow colorPicker = new BoxWindow(colorTest,"ColorTestWindow");
        GUI.windows.register(colorPicker);
        GUI.windows.openWindow(colorPicker);

        BoxWindow boxWindow = new BoxWindow(new TestRoot(), WindowAnchor.BOTTOM_LEFT,"BoxWindow");
        GUI.windows.register(boxWindow);
        GUI.windows.openWindow(boxWindow);
        boxWindow.maximize();

    }

    protected void on_update(float delta) {
        //GUI.out.write("FPS: " + Engine.get().time().fps());
        //if (GUI.keys.just_pressed(GLFW_KEY_B)) GUI.variables.bloom_enabled = !GUI.variables.bloom_enabled;
        if (GUI.keys.just_pressed(GLFW_KEY_ESCAPE)) Engine.get().exit();
        GUI.render_to_gui_framebuffer(delta);
        Framebuffer.bindDefault();
    }

    protected void on_render(float frame_time, float alpha) {
        //bubbleDemo.renderDemo();
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        //ShaderProgram.texturePass(bubbleDemo.texture());
        int buffer = 0;
        if (GUI.keys.pressed(GLFW_KEY_LEFT_ALT))buffer = 2;
        GUI.render_gui_to_screen(buffer);
    }

    protected void on_exit() { GUI.dispose(); Disposable.dispose(bubbleDemo,bg_texture); }
    protected void resolution_request(Resolution resolution) throws Exception { GUI.on_app_resolution_update(resolution); }
    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }
}
