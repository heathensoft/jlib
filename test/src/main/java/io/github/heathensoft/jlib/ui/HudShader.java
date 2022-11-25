package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.lwjgl.graphics.ShaderProgram;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class HudShader extends ShaderProgram {
    
    
    public HudShader() throws Exception {
        super(vs_shader(),fs_shader());
        createUniform("u_combined");
        createUniform("u_sampler");
    }
    
    
    private static String vs_shader() {
        return """
                #version 440
                layout (location=0) in vec4 a_pos;
                layout (location=1) in vec2 a_uv;
                layout (location=2) in vec4 a_color;
                layout (location=3) in float a_id;
                uniform mat4 u_combined;
                out flat uint id;
                out vec2 uv;
                out vec4 color;
                void main() {
                    id = uint(a_id);
                    uv = a_uv;
                    color = a_color;
                    color.a *= (255.0/254.0);
                    gl_Position = u_combined * a_pos;
                }""";
    }
    
    private static String fs_shader() {
        return """
                #version 440
                layout (location=0) out vec4 f_color;
                layout (location=1) out uint f_id;
                in flat uint id;
                in vec2 uv;
                in vec4 color;
                uniform sampler2D u_sampler;
                void main() {
                    f_color = texture(u_sampler,uv).rgba * color;
                    f_id = id;
                }""";
    }
}
