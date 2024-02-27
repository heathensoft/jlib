package io.github.heathensoft.jlib.gui.ny;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.ny.box.BoxWindow;
import io.github.heathensoft.jlib.gui.ny.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.ny.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.ny.text.Text;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public class GUI {


    public static final String path_default_icons_atlas = "res/jlib/gui/atlas/default-icons.txt";
    public static final String path_default_icons_png = "res/jlib/gui/atlas/default-icons.png";


    private static Text text;
    static {
        text = new Text("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
                "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,\n" +
                "when an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
                "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
                "remaining essentially unchanged.\n" +
                "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages,\n" +
                "and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum");

    }
    public static TextureAtlas default_icons;
    public static RendererGUI renderer;
    public static WindowsGUI windows;
    public static FontsGUI fonts;
    public static StateGUI state;
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
        windows = new WindowsGUI();
        fonts = renderer.fonts();
        state = new StateGUI();
        initialized = true;

        text.activateProcessor(); //*****************************************
    }

    public static void render_to_gui_framebuffer(float dt) {
        Mouse mouse = Engine.get().input().mouse();
        windows.update(dt);
        state.update(renderer.pixelID(),dt);
        renderer.begin(mouse.position());
        windows.render(renderer,dt);


        Resolution res = resolution();



        Rectanglef bound = MathLib.rectf(20,20,(res.width() - 20),res.height() - 20);

        renderer.drawElement(bound,Color.BLACK_BITS);

        final float font_size = 28;
        final boolean wrap = false;


        renderer.drawText(text, bound,0, 0, font_size,0,wrap);

        Vector2f cursor_position = text.cursorPosition(MathLib.vec2(),bound.lengthX(),font_size,wrap);

        float cursor_x = cursor_position.x + bound.minX;
        float cursor_y = cursor_position.y + bound.maxY;

        Rectanglef cursor_bounds = fonts.cursorBounds(cursor_x,cursor_y,font_size,MathLib.rectf());

        renderer.drawElement(cursor_bounds,Color.rgb_to_intBits(MathLib.vec4(0f,0f,1f,1f)));


        Optional<BoxWindow> windowOptional = windows.getByName("Window Test0",BoxWindow.class);
        windowOptional.ifPresent(GUI::render_window_info_debug);
        renderer.end();
    }



    private static void render_window_info_debug(BoxWindow window) {


        /*
        String False = " False";
        String True = " True";
        float text_size = 32;
        float scale = fonts.relativeScale(text_size);
        float line_height = scale * (fonts.lineGap() + fonts.ascent() + fonts.descent());
        float x = 20;
        float y = resolution().height() - 10;
        float text_width = 500;
        boolean b = window.isMaximizedX();

        renderer.drawStringFixedSize(
                "Window Width: " + U.round(window.width()),
                TextAlignment.RIGHT, 0,Color.BLACK_BITS,
                resolution().width() - text_width - 20, y,
                text_width, text_size,0
        );

         */



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

    private static TextureAtlas load_default_icons() throws Exception {
        String atlas_info = Resources.asString(path_default_icons_atlas);
        Bitmap atlas_diffuse = Resources.image(path_default_icons_png);
        TextureAtlas default_icons = new TextureAtlas(atlas_info,atlas_diffuse);
        atlas_diffuse.dispose();
        return default_icons;
    }
}
