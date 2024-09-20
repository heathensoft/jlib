package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.SamplerArray;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

/**
 * @author Frederik Dahl
 * 09/08/2024
 */


public class Renderer {


    private static final int SPRITE_BATCH = 0;
    private static final int LINE_BATCH = 1;
    private static final int CHAR_BATCH = 2;

    private VertexAttributes sprites_vao;
    private BufferObject sprites_vbo;
    private BufferObject sprites_ebo;
    private SamplerArray sprites_sampler_array;
    private boolean sprites_pixel_art_antialiasing;
    private boolean sprites_render_transparent_id;
    private boolean sprites_render_ellipse;
    private final int sprites_num_texture_slots = 15;
    private final int sprite_vertex_size = 6;
    private final int sprite_num_vertices = 4;
    private final int sprite_size_float = sprite_vertex_size * sprite_num_vertices;

    private VertexAttributes lines_vao;
    private BufferObject lines_vbo;
    private int lines_prev_abgr;
    private float lines_color;
    private final int line_vertex_size = 3;
    private final int line_num_vertices = 2;
    private final int line_size_float = line_vertex_size * line_num_vertices;

    private VertexAttributes char_vao;
    private BufferObject char_vbo;
    private final int char_vertex_size = 4;
    private final int char_num_vertices = 1;
    private final int char_size_float = char_vertex_size * char_num_vertices;

    private FloatBuffer vertex_buffer;
    private int vertex_buffer_pushed;
    private int vertex_buffer_capacity;
    private int element_size_float;
    private int active_shader;
    private int active_batch;
    private int draw_calls;
    private int count;


    private void flush() {
        if (count > 0) {
            vertex_buffer.flip();
            switch (active_batch) {
                case SPRITE_BATCH -> {
                    ShaderProgram.bindProgram(GUI.shaders.sprite_program);
                }
                case CHAR_BATCH -> {
                    ShaderProgram.bindProgram(GUI.shaders.text_program);
                }
                case LINE_BATCH -> {
                    ShaderProgram.bindProgram(GUI.shaders.line_program);
                    lines_vao.bind();
                    lines_vbo.bind();
                    lines_vbo.bufferSubData(vertex_buffer,0);
                    glDrawArrays(GL_LINES,0,count * 2);
                }
            }
            vertex_buffer.clear();
            draw_calls++;
            count = 0;
        }
    }



}
