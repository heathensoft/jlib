package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.font.TextRenderer1;
import io.github.heathensoft.jlib.lwjgl.gfx.gui.UIRenderer;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.DefaultInput;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */

public class App extends Application {

    private Cmd commandLine = new Cmd(128);
    private Renderer renderer;
    private TextRenderer1 textRenderer;
    private UIRenderer uiRenderer;
    private ScreenQuad screenQuad;
    boolean dragging;

    @Override
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

        Framebuffer.bindDefault();
        Framebuffer.setClearColor(0,0.6f,0.3f,1);
        uiRenderer = new UIRenderer(resolution.width(),resolution.height());
        screenQuad = new ScreenQuad();
    }


    protected void on_update(float delta) { }

    boolean first = true;

    protected void on_render(float frame_time, float alpha) {
        DefaultInput input = Engine.get().input();
        Resolution resolution = Engine.get().window().appResolution();
        Vector2f mouse = input.mouse().position();
        uiRenderer.begin(mouse);
        uiRenderer.drawElement(new Vector4f(50,90,800,400), new Color32(0.1f,0.1f,0.2f,1),4,1f,false);
        uiRenderer.drawText("Testing",0,200,0,100,50,1,false);
        uiRenderer.end();

        if (first) {
            Texture t = uiRenderer.framebuffer().texture(1);
            Framebuffer.bindRead(uiRenderer.framebuffer());
            Framebuffer.readBuffer(1);
            ByteBuffer buffer = MemoryUtil.memAlloc(t.width()*t.height()*t.format().channels);
            glPixelStorei(GL_PACK_ALIGNMENT,t.format().pack_alignment);
            glReadPixels(0,0,t.width(),t.height(),t.format().pixel_format,GL_UNSIGNED_BYTE, buffer);
            Bitmap bitmap = new Bitmap(buffer,t.width(),t.height(),t.format().channels);
            bitmap.compressToDisk("ff.png");
            bitmap.dispose();
            first = false;
        }

        Framebuffer.bindDefault();
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        //glDisable(GL_BLEND);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        //glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        Texture diffuse = uiRenderer.framebufferDiffuseTexture();
        screenQuad.render(diffuse);
    }

    @Override
    protected void on_exit() {
        Disposable.dispose(renderer,textRenderer,screenQuad,uiRenderer);
    }

    @Override
    protected void resolution_request(Resolution resolution) throws Exception {
        uiRenderer.updateResolution(resolution.width(),resolution.height());
    }

    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }


}
