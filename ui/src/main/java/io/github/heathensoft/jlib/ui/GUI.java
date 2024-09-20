package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.TextAlignment;
import io.github.heathensoft.jlib.ui.text.TextEditor;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;

/**
 * @author Frederik Dahl
 * 19/02/2024
 */


public class GUI {

    public static WindowManager.Tooltips tooltips;
    public static GlobalVariables variables;
    public static WindowManager windows;
    public static RendererGUI renderer;
    public static Gadgets gadgets;
    public static Shaders shaders;
    public static FontsGUI fonts;
    public static Keyboard keys;
    public static DevDebug out;
    public static Icons icons;
    public static Mouse mouse;
    public static State state;

    private static boolean initialized;


    public static void initialize(Resolution resolution) throws Exception {
        if (initialized) throw new IllegalStateException("GUI already initialized");
        shaders = new Shaders();
        variables = new GlobalVariables();
        renderer = new RendererGUI(resolution.width(),resolution.height());
        gadgets = new Gadgets();
        icons = new Icons();
        mouse = Engine.get().input().mouse();
        keys = Engine.get().input().keys();
        windows = new WindowManager();
        tooltips = windows.helpText();
        fonts = renderer.fonts();
        state = new State();
        out = new DevDebug();
        initialized = true;
    }



    static TextEditor editor = new TextEditor();
    static Vector4f editor_color = new Vector4f(0.3f,0.8f,0.6f,1.0f);

    public static void render_to_gui_framebuffer(float dt) {
        Mouse mouse = Engine.get().input().mouse();
        state.update(renderer.pixelID(),dt);
        windows.update(dt);
        renderer.begin(mouse.position());
        windows.render(renderer,dt);
        editor.activateTextProcessor();
        Rectanglef rect = U.popSetRect(0,0,1280,720);
        renderer.drawAsciiEditor(editor,rect,editor_color,0,FontsGUI.SLOT_MONO,8,20,1.0f,false,true);
        U.pushRect();
        out.flush(renderer);
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
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        Texture texture = renderer.framebuffer().texture(color_buffer);
        if (color_buffer == 2) texture = renderer.bloomTexture();
        ShaderProgram.texturePass(texture);
        if (variables.bloom_enabled && color_buffer == 0) {
            //glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE);
            texture = renderer.bloomTexture();
            ShaderProgram.texturePass(texture);
        }
    }


    public static void on_app_resolution_update(Resolution resolution) throws Exception {
        renderer.updateResolution(resolution.width(),resolution.height());
    }

    public static void dispose() {
        Disposable.dispose(windows,icons,gadgets,renderer);
        icons = null;
        gadgets = null;
        renderer = null;
        windows = null;
        mouse = null;
        fonts = null;
        state = null;
        keys = null;
        initialized = false;
    }

    public static Resolution resolution() { return Engine.get().window().appResolution(); }


    public static Vector2f mousePosition(Vector2f dst) {
        Resolution resolution = resolution();
        return dst.set(mouse.position()).mul(resolution.width(),resolution.height());
    }

    public static Vector2f mouseDragVector(Vector2f dst, int button) {
        Resolution resolution = resolution();
        return dst.set(mouse.drag_vector(button)).mul(resolution.width(),resolution.height());
    }

    public static Vector2f mouseDragOrigin(Vector2f dst, int button) {
        Resolution resolution = resolution();
        return dst.set(mouse.drag_origin(button)).mul(resolution.width(),resolution.height());
    }

    public static Vector2f mouseToLocal(Rectanglef localBounds, Vector2f dst) {
        return mousePosition(dst).sub(localBounds.minX,localBounds.minY);
    }

    public static Vector2f mouseToLocalNormalized(Rectanglef localBounds, Vector2f dst) {
        return mouseToLocal(localBounds,dst).div(localBounds.lengthX(),localBounds.lengthY());
    }


    public static final class GlobalVariables {

        public int gui_default_font = 3;
        public float gui_default_font_size = 22;

        public float state_double_click_time_delay = 0.33f;

        public int scrollbar_default_width = 6;
        public float scrollbar_fade_mod = 2.8f;
        public float scrollbar_glow = 0.4f;
        public Vector4f scrollbar_default_color = new Vector4f(0.21f,0.94f,0.55f,1.0f);

        public int textfield_default_padding = 4;
        public Vector4f textfield_default_custom_color = Color.hex_to_rgb("A9B7C6FF");

        public int boxwindow_fadedisplay_font = 3;
        public int boxwindow_fadedisplay_padding = 5;
        public int boxwindow_fadedisplay_desired_height = 36;
        public Vector4f boxwindow_fadedisplay_text_color = new Vector4f(0.21f,0.94f,0.55f,1.0f);

        public int tooltips_fontsize = 20;
        public int tooltips_font = 3;
        public int tooltips_padding = 4;
        public Vector4f tooltips_default_text_color = Color.intBits_to_rgb(0xFF99EE88);

        public boolean bloom_enabled = true;
        public float bloom_threshold = 0.20f;
        public int bloom_ping_pong_iterations = 10;
    }

    public static final class Shaders {
        public static final String path_vert_sprite_batch = "res/jlib/ui/glsl/ui_sprite.vert";
        public static final String path_frag_sprite_batch = "res/jlib/ui/glsl/ui_sprite.frag";
        public static final String path_vert_text_batch = "res/jlib/ui/glsl/ui_text.vert";
        public static final String path_geom_text_batch = "res/jlib/ui/glsl/ui_text.geom";
        public static final String path_frag_text_batch = "res/jlib/ui/glsl/ui_text.frag";
        public static final String path_vert_color_picker = "res/jlib/ui/glsl/ui_color_picker.vert";
        public static final String path_frag_color_picker = "res/jlib/ui/glsl/ui_color_picker.frag";
        public static final String path_vert_icon_select = "res/jlib/ui/glsl/ui_icon_select.vert";
        public static final String path_frag_icon_select = "res/jlib/ui/glsl/ui_icon_select.frag";
        public static final String path_vert_bloom_threshold = "res/jlib/ui/glsl/bloom/ui_bloom_threshold.vert";
        public static final String path_frag_bloom_threshold = "res/jlib/ui/glsl/bloom/ui_bloom_threshold.frag";
        public static final String path_vert_bloom_ping_pong = "res/jlib/ui/glsl/bloom/ui_bloom_ping_pong.vert";
        public static final String path_frag_bloom_ping_pong = "res/jlib/ui/glsl/bloom/ui_bloom_ping_pong.frag";
        public static final String path_vert_icon_bg_default = "res/jlib/ui/glsl/ui_icon_bg_default.vert";
        public static final String path_frag_icon_bg_default = "res/jlib/ui/glsl/ui_icon_bg_default.frag";
        public final int text_program;
        public final int sprite_program;
        public final int color_picker;
        public final int icon_selection;
        public final int icon_default_bg;
        public final int bloom_threshold;
        public final int bloom_ping_pong;
        Shaders() throws Exception {
            String v_source,g_source,f_source;
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
            v_source = Resources.asString(path_vert_color_picker);
            f_source = Resources.asString(path_frag_color_picker);
            program = new ShaderProgram("ui_color_picker_program",v_source,f_source);
            color_picker = program.glHandle();
            v_source = Resources.asString(path_vert_icon_select);
            f_source = Resources.asString(path_frag_icon_select);
            program = new ShaderProgram("ui_icon_select_program",v_source,f_source);
            icon_selection = program.glHandle();
            v_source = Resources.asString(path_vert_bloom_threshold);
            f_source = Resources.asString(path_frag_bloom_threshold);
            program = new ShaderProgram("ui_bloom_threshold_program",v_source,f_source);
            bloom_threshold = program.glHandle();
            v_source = Resources.asString(path_vert_bloom_ping_pong);
            f_source = Resources.asString(path_frag_bloom_ping_pong);
            program = new ShaderProgram("ui_bloom_ping_pong_program",v_source,f_source);
            bloom_ping_pong = program.glHandle();
            v_source = Resources.asString(path_vert_icon_bg_default);
            f_source = Resources.asString(path_frag_icon_bg_default);
            program = new ShaderProgram("ui_icon_default_bg",v_source,f_source);
            icon_default_bg = program.glHandle();
        }
    }

    public static final class Icons implements Disposable {
        public final Texture faded_background; // quadratic
        public final TextureAtlas atlas;
        public Sprite refresh, cut, paste, search, build, play, pause, statistics, fast_forward, fast_rewind, close_window
        ,color_palette, color_sample, color_fill, icon_view, list_view, settings, show_more, edit_text, upload, download
        ,arrow_north,arrow_east,arrow_south,arrow_west,arrow_north_east,arrow_north_west,arrow_south_east,arrow_south_west
        ,question_mark, garbage_bin, image_file, audio_file, text_file, folder, zoom_inn, zoom_out;
        Icons() throws Exception {
            atlas = load_default_icons();
            cut = atlas.createSprite(0,"sharp_content_cut_white_36dp");
            search = atlas.createSprite(0,"sharp_search_white_36dp");
            play = atlas.createSprite(0, "sharp_play_arrow_white_36dp");
            fast_forward = atlas.createSprite(0,"sharp_fast_forward_white_36dp");
            pause = atlas.createSprite(0,"sharp_pause_white_36dp");
            close_window = atlas.createSprite(0,"sharp_close_white_36dp");
            icon_view = atlas.createSprite(0,"sharp_apps_white_36dp");
            list_view = atlas.createSprite(0,"sharp_reorder_white_36dp");
            settings = atlas.createSprite(0,"sharp_settings_white_36dp");
            show_more = atlas.createSprite(0,"sharp_more_horiz_white_36dp");
            edit_text = atlas.createSprite(0,"sharp_edit_note_white_36dp");
            download = atlas.createSprite(0,"sharp_download_white_36dp");
            refresh = atlas.createSprite(0,"sharp_cached_white_36dp");
            color_palette = atlas.createSprite(0,"sharp_color_lens_white_36dp");
            color_sample = atlas.createSprite(0,"sharp_color_lens_white_36dp");
            color_fill = atlas.createSprite(0,"sharp_format_color_fill_white_36dp");
            statistics = atlas.createSprite(0,"sharp_equalizer_white_36dp");
            fast_rewind = atlas.createSprite(0,"sharp_fast_rewind_white_36dp");
            build = atlas.createSprite(0,"sharp_construction_white_36dp");
            paste = atlas.createSprite(0,"sharp_content_paste_go_white_36dp");
            upload = atlas.createSprite(0,"sharp_upload_white_36dp");
            garbage_bin = atlas.createSprite(0,"sharp_delete_forever_white_36dp");
            question_mark = atlas.createSprite(0,"sharp_question_mark_white_36dp");
            arrow_south = atlas.createSprite(0,"sharp_south_white_36dp");
            arrow_north = atlas.createSprite(0,"sharp_north_white_36dp");
            arrow_east = atlas.createSprite(0,"sharp_east_white_36dp");
            arrow_west = atlas.createSprite(0,"sharp_west_white_36dp");
            arrow_north_east = atlas.createSprite(0,"sharp_north_east_white_36dp");
            arrow_south_east = atlas.createSprite(0,"sharp_south_east_white_36dp");
            arrow_south_west = atlas.createSprite(0,"sharp_south_west_white_36dp");
            arrow_north_west = atlas.createSprite(0,"sharp_north_west_white_36dp");
            image_file = atlas.createSprite(0,"sharp_image_white_36dp");
            text_file = atlas.createSprite(0,"sharp_description_white_36dp");
            audio_file = atlas.createSprite(0,"sharp_audio_file_white_36dp");
            folder = atlas.createSprite(0,"sharp_folder_white_36dp");
            zoom_inn = atlas.createSprite(0,"sharp_zoom_in_white_36dp");
            zoom_out = atlas.createSprite(0,"sharp_zoom_out_white_36dp");

            faded_background = render_faded_background();
        }

        private Texture render_faded_background() throws Exception {
            Texture texture = Texture.generate2D(128,128);
            texture.bindToActiveSlot();
            texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
            texture.filterLinear();
            texture.clampToEdge();
            Framebuffer framebuffer = new Framebuffer(texture.width(),texture.height());
            Framebuffer.bind(framebuffer);
            Framebuffer.attachColor(texture,0,false);
            Framebuffer.drawBuffer(0);
            Framebuffer.checkStatus();
            Framebuffer.viewport();
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            ShaderProgram.bindProgram(shaders.icon_default_bg);
            ShaderProgram.shaderPass().draw();
            Disposable.dispose(framebuffer);
            return texture;
        }

        private TextureAtlas load_default_icons() throws Exception {
            String path_default_icons_atlas = "res/jlib/ui/atlas/icons.txt";
            String path_default_icons_png = "res/jlib/ui/atlas/icons.png";
            String atlas_info = Resources.asString(path_default_icons_atlas);
            Bitmap atlas_diffuse = Resources.image(path_default_icons_png);
            TextureAtlas default_icons = new TextureAtlas(atlas_info,atlas_diffuse);
            atlas_diffuse.dispose();
            return default_icons;
        } public void dispose() {
            Disposable.dispose(atlas);
        }
    }

    public static final class Gadgets implements Disposable {

        public final TextureAtlas atlas;
        public final TextureRegion window_border_top;
        public final TextureRegion window_border_top_right;
        public final TextureRegion window_border_right;
        public final TextureRegion window_border_bottom_right;
        public final TextureRegion window_border_bottom;
        public final TextureRegion window_border_bottom_left;
        public final TextureRegion window_border_left;
        public final TextureRegion window_border_top_left;
        public final TextureRegion button_top;
        public final TextureRegion button_top_right;
        public final TextureRegion button_right;
        public final TextureRegion button_bottom_right;
        public final TextureRegion button_bottom;
        public final TextureRegion button_bottom_left;
        public final TextureRegion button_left;
        public final TextureRegion button_top_left;
        public final TextureRegion button_center;
        public final TextureRegion button_pressed_top;
        public final TextureRegion button_pressed_top_right;
        public final TextureRegion button_pressed_right;
        public final TextureRegion button_pressed_bottom_right;
        public final TextureRegion button_pressed_bottom;
        public final TextureRegion button_pressed_bottom_left;
        public final TextureRegion button_pressed_left;
        public final TextureRegion button_pressed_top_left;
        public final TextureRegion button_pressed_center;

        Gadgets() throws Exception {
            atlas = load_default_gadgets();
            window_border_top = atlas.getRegion("border_t");
            window_border_top_right = atlas.getRegion("border_tr");
            window_border_right = atlas.getRegion("border_r");
            window_border_bottom_right = atlas.getRegion("border_br");
            window_border_bottom = atlas.getRegion("border_b");
            window_border_bottom_left = atlas.getRegion("border_bl");
            window_border_left = atlas.getRegion("border_l");
            window_border_top_left = atlas.getRegion("border_tl");
            button_top = atlas.getRegion("button_t");
            button_top_right = atlas.getRegion("button_tr");
            button_right = atlas.getRegion("button_r");
            button_bottom_right = atlas.getRegion("button_br");
            button_bottom = atlas.getRegion("button_b");
            button_bottom_left = atlas.getRegion("button_bl");
            button_left = atlas.getRegion("button_l");
            button_top_left = atlas.getRegion("button_tl");
            button_center = atlas.getRegion("button_c");
            button_pressed_top = atlas.getRegion("button_pressed_t");
            button_pressed_top_right = atlas.getRegion("button_pressed_tr");
            button_pressed_right = atlas.getRegion("button_pressed_r");
            button_pressed_bottom_right = atlas.getRegion("button_pressed_br");
            button_pressed_bottom = atlas.getRegion("button_pressed_b");
            button_pressed_bottom_left = atlas.getRegion("button_pressed_bl");
            button_pressed_left = atlas.getRegion("button_pressed_l");
            button_pressed_top_left = atlas.getRegion("button_pressed_tl");
            button_pressed_center = atlas.getRegion("button_pressed_c");
        }
        private TextureAtlas load_default_gadgets() throws Exception {
            String path_default_gadgets_atlas = "res/jlib/ui/atlas/gadgets.txt";
            String path_default_gadgets_png = "res/jlib/ui/atlas/gadgets.png";
            String atlas_info = Resources.asString(path_default_gadgets_atlas);
            Bitmap atlas_diffuse = Resources.image(path_default_gadgets_png);
            TextureAtlas default_icons = new TextureAtlas(atlas_info,atlas_diffuse);
            atlas_diffuse.dispose();
            return default_icons;
        } public void dispose() { Disposable.dispose(atlas); }
    }

    public static final class DevDebug {
        private final LinkedList<String> outputList = new LinkedList<>();
        public void write(String string) { outputList.add(string); }
        void flush(RendererGUI renderer) {
            float y = resolution().height() - 4;
            while (!outputList.isEmpty()) {
                String string = outputList.removeFirst();
                renderer.drawStringFixedSize(string, TextAlignment.LEFT,3,0xFF44FF66,4,y,Float.MAX_VALUE,32,1.0f);
                y -= 32;
            }
        }
    }
}
