package io.github.heathensoft.jlib.gui.state;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 21/11/2023
 */


public class GUI {

    public static final String path_default_icons_atlas = "";
    public static final String path_default_icons_png = "";

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
        //default_icons = load_default_icons();
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
        Framebuffer.setClearColor(0.18f,0.03f,0.11f,1);
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
        windows.renderAll(renderer,dt);
        renderer.end();
    }

    public static void gui_dispose() {
        if (!gui_initialized) throw new IllegalStateException("GUI not initialized");
        Disposable.dispose(
                windows,
                default_icons,
                renderer);
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
