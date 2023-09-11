package io.github.heathensoft.jlib.test.gui;

import io.github.heathensoft.jlib.test.graphicsOld.ShaderProgram;
import io.github.heathensoft.jlib.test.graphicsOld.Texture;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class GUIShader extends ShaderProgram {
    
    private final static String U_SAMPLER_2D_ARRAY = "u_textures";
    private final static String U_MATRIX = "u_combined";
    
    public GUIShader() throws Exception {
        super(vs_shader(),fs_shader()); use();
        createUniform(U_SAMPLER_2D_ARRAY);
        createUniform(U_MATRIX);
    }
    
    public void prepare(GUI GUI) { use();
        int num_textures = GUI.GRAPHICS.NUM_TEXTURES;
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(num_textures);
            for (int i = 0; i < num_textures; i++) buffer.put(i);
            setUniform1iv(U_SAMPLER_2D_ARRAY,buffer.flip());
        } for (int i = 0; i < num_textures; i++) {
            Texture texture = GUI.GRAPHICS.texture(i);
            if (texture != null) texture.bindToSlot(i);
        } setUniform(U_MATRIX, GUI.MATRIX);
    }
    
    private static String vs_shader() {
        return """
                #version 440
                layout (location=0) in vec4 a_pos;
                layout (location=1) in vec2 a_uv;
                layout (location=2) in vec4 a_color;
                layout (location=3) in float a_id;
                uniform mat4 u_combined;
                out flat int tex_slot;
                out flat uint id;
                out vec2 uv;
                out vec4 color;
                void main() {
                    id = uint(a_id);
                    tex_slot = int(id) & (0xFF);
                    uv = a_uv;
                    color = a_color;
                    color.a *= (255.0/254.0);
                    gl_Position = u_combined * a_pos;
                }""";
    }


    private static String fs_shader() {
        return """
                #version 440
                #define NUM_TEXTURES 6
                layout (location=0) out vec4 f_color;
                layout (location=1) out uint f_id;
                in flat int tex_slot;
                in flat uint id;
                in vec2 uv;
                in vec4 color;
                uniform sampler2D[NUM_TEXTURES] u_textures;
                void main() {
                    f_color = (texture(u_textures[tex_slot],uv).rgba * color);
                    f_id = id + 1;
                }""";
    }
}
