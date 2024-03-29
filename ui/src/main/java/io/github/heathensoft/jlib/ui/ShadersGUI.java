package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;

/**
 * @author Frederik Dahl
 * 28/03/2024
 */


public class ShadersGUI {


    public static final String path_vert_sprite_batch = "res/jlib/ui/glsl/ui_sprite.vert";
    public static final String path_frag_sprite_batch = "res/jlib/ui/glsl/ui_sprite.frag";
    public static final String path_vert_text_batch = "res/jlib/ui/glsl/ui_text.vert";
    public static final String path_geom_text_batch = "res/jlib/ui/glsl/ui_text.geom";
    public static final String path_frag_text_batch = "res/jlib/ui/glsl/ui_text.frag";

    public final int text_program;
    public final int sprite_program;


    public ShadersGUI() throws Exception {
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
