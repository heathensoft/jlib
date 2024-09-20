package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.gui.gfx.Fonts;
import io.github.heathensoft.jlib.gui.gfx.RendererOld;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;

/**
 * @author Frederik Dahl
 * 29/05/2024
 */


public class GUI {


    public static RendererOld renderer;
    public static Shaders shaders;
    public static Fonts fonts;


    public static Resolution resolution() {
        return Engine.get().window().appResolution();
    }

    public static final class Shaders {

        public int text_program;
        public int sprite_program;
        public int line_program;
        public int color_picker;
        public int icon_selection;
        public int icon_default_bg;
        public int bloom_threshold;
        public int bloom_ping_pong;
    }
}
