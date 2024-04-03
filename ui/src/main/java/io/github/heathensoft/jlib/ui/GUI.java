package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;
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


    private static final String path_default_icons_atlas = "res/jlib/ui/atlas/default-icons.txt";
    private static final String path_default_icons_png = "res/jlib/ui/atlas/default-icons.png";
    private static final String path_pixel_icons_atlas = "res/jlib/ui/atlas/default-gadgets.txt";
    private static final String path_pixel_icons_png = "res/jlib/ui/atlas/default-gadgets.png";

    public static GlobalVariables variables;
    public static TextureAtlas default_gadgets;
    public static TextureAtlas default_icons;
    public static WindowManager windows;
    public static RendererGUI renderer;
    public static Shaders shaders;
    public static FontsGUI fonts;
    public static Keyboard keys;
    public static Mouse mouse;
    public static State state;

    private static boolean initialized;

    public static void initialize(Resolution resolution) throws Exception {
        if (initialized) throw new IllegalStateException("GUI already initialized");
        variables = new GlobalVariables();
        shaders = new Shaders();
        renderer = new RendererGUI(resolution.width(),resolution.height());
        default_icons = load_default_icons();
        default_gadgets = load_default_gadgets();
        mouse = Engine.get().input().mouse();
        keys = Engine.get().input().keys();
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


    /**
     * Draw GUI to screen using a simple passthrough shader.
     * This should be called during the applications render phase.
     * Meant to be used during development / debugging. Simple setup.
     * It renders the content of the GUI renderer's framebuffer directly onto the screen.
     * @param color_buffer The GUI Renderers color buffer. One of 0,1,2. Where 0 is diffuse color.
     */
    public static void render_gui_to_screen(int color_buffer) {
        color_buffer = U.clamp(color_buffer,0,2);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        Texture texture = renderer.framebuffer().texture(color_buffer);
        ShaderProgram.texturePass(texture);
        ShaderProgram.shaderPass().draw();
    }


    public static void on_app_resolution_update(Resolution resolution) throws Exception {
        renderer.updateResolution(resolution.width(),resolution.height());
    }

    public static void dispose() {
        Disposable.dispose(windows,default_icons, default_gadgets,renderer);
        default_icons = null;
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

    private static TextureAtlas load_default_gadgets() throws Exception {
        String atlas_info = Resources.asString(path_pixel_icons_atlas);
        Bitmap atlas_diffuse = Resources.image(path_pixel_icons_png);
        TextureAtlas default_icons = new TextureAtlas(atlas_info,atlas_diffuse);
        atlas_diffuse.dispose();
        return default_icons;
    }

    public static final class GlobalVariables {

        public int boxWindow_fadeDisplay_font = 3;
        public int boxWindow_fadDisplay_padding = 5;
        public int boxWindow_fadDisplay_desiredHeight = 36;
        public Vector4f boxWindow_fadeDisplay_textColor = new Vector4f(0.21f,0.94f,0.55f,1.0f);


    }

    public static final class Shaders {
        public static final String path_vert_sprite_batch = "res/jlib/ui/glsl/ui_sprite.vert";
        public static final String path_frag_sprite_batch = "res/jlib/ui/glsl/ui_sprite.frag";
        public static final String path_vert_text_batch = "res/jlib/ui/glsl/ui_text.vert";
        public static final String path_geom_text_batch = "res/jlib/ui/glsl/ui_text.geom";
        public static final String path_frag_text_batch = "res/jlib/ui/glsl/ui_text.frag";
        public final int text_program;
        public final int sprite_program;
        Shaders() throws Exception {
            String v_source;
            String g_source;
            String f_source;
            ShaderProgram program;
            v_source = Resources.asString(path_vert_text_batch);
            g_source = Resources.asString(path_geom_text_batch);
            f_source = Resources.asString(path_frag_text_batch);
            program = new ShaderProgram("ui_text_program",v_source,g_source,f_source);
            text_program = program.glHandle();
            v_source = Resources.asString(path_vert_sprite_batch);
            f_source = Resources.asString(path_frag_sprite_batch);
            program = new ShaderProgram("ui_sprite_program",v_source,f_source);
            sprite_program = program.glHandle();
        }
    }
}
