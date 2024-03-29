package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public class GUI {


    public static final String path_default_icons_atlas = "res/jlib/ui/atlas/default-icons.txt";
    public static final String path_default_icons_png = "res/jlib/ui/atlas/default-icons.png";

    public static TextureAtlas default_icons;
    public static RendererGUI renderer;
    public static WindowManager windows; // TODO: ****************
    public static FontsGUI fonts;
    public static State state;
    public static Keyboard keys;
    public static Mouse mouse;

    private static ScreenQuad screen_quad;
    private static boolean initialized;


    public static void initialize(Resolution resolution) throws Exception {
        if (initialized) throw new IllegalStateException("GUI already initialized");
        renderer = new RendererGUI(resolution.width(),resolution.height());
        default_icons = load_default_icons();
        mouse = Engine.get().input().mouse();
        keys = Engine.get().input().keys();
        screen_quad = new ScreenQuad();
        windows = new WindowManager();
        fonts = renderer.fonts();
        state = new State();
        initialized = true;
    }

    public static void render_to_gui_framebuffer(float dt) {
        Mouse mouse = Engine.get().input().mouse();
        windows.update(dt);
        state.update(renderer.pixelID(),dt);
        renderer.begin(mouse.position());
        windows.render(renderer,dt);
        renderer.end();
    }


    public static void render_to_screen_default(Vector4f clear_color) {
        render_to_screen_default(0,clear_color);
    }

    /**
     * Draw GUI to screen using a simple passthrough shader.
     * This should be called during the applications render phase.
     * Meant to be used during development / debugging. Simple setup.
     * It renders the content of the GUI renderer's framebuffer directly onto the screen.
     * @param color_buffer The GUI Renderers color buffer. One of 0,1,2. Where 0 is diffuse color.
     */
    public static void render_to_screen_default(int color_buffer, Vector4f clear_color) {
        color_buffer = U.clamp(color_buffer,0,2);
        Framebuffer.bindDefault();
        Framebuffer.setClearColor(clear_color);
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        Texture diffuse = renderer.framebuffer().texture(color_buffer);
        screen_quad.render(diffuse);
    }


    public static void on_app_resolution_update(Resolution resolution) throws Exception {
        renderer.updateResolution(resolution.width(),resolution.height());
    }

    public static void dispose() {
        Disposable.dispose(windows,default_icons,screen_quad,renderer);
        default_icons = null;
        screen_quad = null;
        renderer = null;
        windows = null;
        mouse = null;
        fonts = null;
        state = null;
        keys = null;
        initialized = false;
    }

    public static Resolution resolution() { return Engine.get().window().appResolution(); }

    public static Vector2f mouse_position(Vector2f dst) {
        Resolution resolution = resolution();
        return dst.set(mouse.position()).mul(resolution.width(),resolution.height());
    }

    public static Vector2f mouse_drag_vector(Vector2f dst, int button) {
        Resolution resolution = resolution();
        return dst.set(mouse.drag_vector(button)).mul(resolution.width(),resolution.height());
    }

    public static Vector2f mouse_drag_origin(Vector2f dst, int button) {
        Resolution resolution = resolution();
        return dst.set(mouse.drag_origin(button)).mul(resolution.width(),resolution.height());
    }

    private static TextureAtlas load_default_icons() throws Exception {
        String atlas_info = Resources.asString(path_default_icons_atlas);
        Bitmap atlas_diffuse = Resources.image(path_default_icons_png);
        TextureAtlas default_icons = new TextureAtlas(atlas_info,atlas_diffuse);
        atlas_diffuse.dispose();
        return default_icons;
    }
}
