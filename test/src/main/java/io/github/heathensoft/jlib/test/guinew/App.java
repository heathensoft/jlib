package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.gui.gfx.UIRenderer;
import io.github.heathensoft.jlib.gui.gfx.UISprite;
import io.github.heathensoft.jlib.gui.Interactables;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.DefaultInput;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */

public class App extends Application {


    private Cmd cmd = new Cmd();
    private UIRenderer uiRenderer;
    private ScreenQuad screenQuad;
    private UISprite sprite;
    private Texture texture;
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
        Bitmap bitmap = Resources.image("res/jlib/test/hud.png");
        texture = bitmap.asTexture();
        bitmap.dispose();
        texture.clampToEdge();
        texture.nearest();
        sprite = new UISprite(texture);
        Framebuffer.bindDefault();
        Framebuffer.setClearColor(0.18f,0.03f,0.11f,1);
        uiRenderer = new UIRenderer(resolution.width(),resolution.height());
        screenQuad = new ScreenQuad();
        Engine.get().input().keys().setTextProcessor(cmd);

        Bitmap cursor_image = Resources.image("res/jlib/lwjgl/cursors/cursor-x2.png");
        Optional<CursorObject> optional = Engine.get().window().createCursor("cursor",cursor_image,2,2);
        optional.ifPresent(CursorObject::use);

    }

    boolean resolution_change;

    Rectanglef quad = new Rectanglef(100,100,100+(256),100+(128));

    Element e1 = new Element(20,700,400,400, new Color32(0.2f,0.4f,0.3f,1f));
    Element e2 = new Element(20 + 420,700,400,400, new Color32(0.3f,0.1f,0.4f,1f));

    protected void on_update(float delta) {

        Keyboard keyboard = Engine.get().input().keys();
        Window window = Engine.get().window();

        //quad.translate(delta * 2, delta * 2);
        DefaultInput input = Engine.get().input();
        Vector2f mouse = input.mouse().position();

        int pixel_id = uiRenderer.pixelID();
        Interactables.refresh(pixel_id,delta);
        //Color32 color = new Color32(0.0f,0.0f,0.0f,0.4f);
        //Rectanglef rect = new Rectanglef(50,50,400,400);

        uiRenderer.begin(mouse);
        e1.draw(uiRenderer);
        e2.draw(uiRenderer);
        uiRenderer.drawElement(sprite,quad);
        uiRenderer.drawLine(cmd,(1280/2f),200,300,0,50,false);
        //uiRenderer.drawLine(cmd,(1280/2f),500,800,0,50,true);
        //uiRenderer.drawTextWrap(cmd,rect,0,40);
        //uiRenderer.drawElement(rect,color);
        //uiRenderer.drawTextWrap(cmd,(1280/2f),200,300,0,50);
        //uiRenderer.drawTextCenteredFit(cmd,(1280/2f),300,300,0,50);
        uiRenderer.end();

    }


    protected void on_render(float frame_time, float alpha) {
        Framebuffer.bindDefault();
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        Texture diffuse = uiRenderer.framebufferDiffuseTexture();
        screenQuad.render(diffuse);
    }

    protected void on_exit() {
        Disposable.dispose(screenQuad,uiRenderer, texture);
    }

    protected void resolution_request(Resolution resolution) throws Exception {
        uiRenderer.updateResolution(resolution.width(),resolution.height());
    }

    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }


}
