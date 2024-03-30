package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.text.TextUtils;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 21/11/2023
 */


public class GUI {


    public static final Vector4f color_slider_knob = Color.hex_to_rgb("C3E1E1FF",new Vector4f());
    public static final Vector4f color_slider_background = Color.hex_to_rgb("3A3D3DFF",new Vector4f());
    public static final Vector4f color_slider_inactive = Color.hex_to_rgb("838F8FFF",new Vector4f());
    public static final Vector4f color_slider_active = Color.hex_to_rgb("3EBF7CFF",new Vector4f());




    public static final String icon_default_circle = "baseline_circle_white_36dp";



    public static final String path_default_icons_atlas = "res/jlib/gui/atlas/default-icons.txt";
    public static final String path_default_icons_png = "res/jlib/gui/atlas/default-icons.png";

    public static Mouse mouse;
    public static Keyboard keys;
    public static State state;
    public static Windows windows;
    public static FontsGUI fonts;
    public static RendererGUI renderer;
    public static TextureAtlas default_icons;
    public static ScreenQuad screen_quad;
    private static boolean gui_initialized;

    public static void gui_initialize(Resolution resolution) throws Exception {
        if (gui_initialized) throw new IllegalStateException("GUI already initialized");
        renderer = new RendererGUI(resolution.width(),resolution.height());
        screen_quad = new ScreenQuad();
        default_icons = load_default_icons();
        mouse = Engine.get().input().mouse();
        keys = Engine.get().input().keys();
        windows = new Windows();
        state = new State();
        fonts = renderer.fonts();
        gui_initialized = true;
    }


    public static void gui_request_resolution(Resolution resolution) throws Exception {
        renderer.updateResolution(resolution.width(),resolution.height());
    }

    public static void gui_render_to_screen() {
        Framebuffer.bindDefault();
        //Framebuffer.setClearColor(0.18f,0.03f,0.11f,1);
        Framebuffer.setClearColor(0.8f,0.8f,0.8f,1);
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        Texture diffuse = renderer.framebufferDiffuseTexture();
        screen_quad.render(diffuse);
    }


    public static void gui_render_windows(float dt) {
        windows.prepare(dt);
        state.update(renderer.pixelID(),dt);
        renderer.begin(mouse.position());
        //windows.renderAll(renderer,dt);
        Rectanglef rect = U.rectf();
        float x0 = 50;
        float y0 = 80;
        String string = "Test String 1234567890\n\n Size...";
        float text_size = 64;
        float text_calculated_width = TextUtils.calculate_width(string,fonts,text_size);
        rect.setMin(x0,y0 - text_size);
        rect.setMax(x0 + text_calculated_width + 100,y0);

        //renderer.drawElement(rect,Color.BLACK_BITS);
        renderer.drawStringDynamicSize(string,0,Color.WHITE_BITS,x0,y0,text_calculated_width,text_size,0,false);



        //TextureRegion region = default_icons.getRegion(icon_default_circle);
        //if (region != null) {
        //    Rectanglef rect = MathLib.rectf(5,5,205,205);
        //    Texture diffuse = default_icons.texture(0);
        //    renderer.drawElement(diffuse,region,rect,Color.rgb_to_intBits(color_slider_active),0,false);
        //}


        renderer.end();
    }

    public static void gui_dispose() {
        if (!gui_initialized) throw new IllegalStateException("GUI not initialized");
        Disposable.dispose(windows, default_icons, renderer, screen_quad);
        gui_initialized = false;
    }

    private static TextureAtlas load_default_icons() throws Exception {
        String atlas_info = Resources.asString(path_default_icons_atlas);
        Bitmap atlas_diffuse = Resources.image(path_default_icons_png);
        TextureAtlas default_icons = new TextureAtlas(atlas_info,atlas_diffuse);
        atlas_diffuse.dispose();
        return default_icons;
    }
}
