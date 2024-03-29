package io.github.heathensoft.jlib.lwjgl.utils;


import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.Vao;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 21/04/2022
 */


public class ScreenQuad implements Disposable {
    
    private final Vao vao;
    private final BufferObject indexBuffer;
    private final BufferObject vertexBuffer;
    private final ShaderProgram shaderProgram;
    
    public ScreenQuad() throws Exception {
        shaderProgram = new ShaderProgram(default_screen_vs_shader(),default_screen_fs_shader());
        shaderProgram.createUniform("u_sampler");
        indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
        float[] vertices = {
                 1.0f,-1.0f,1.0f, 0.0f, // Bottom right 0
                -1.0f, 1.0f,0.0f, 1.0f, // Top left     1
                 1.0f, 1.0f,1.0f, 1.0f, // Top right    2
                -1.0f,-1.0f,0.0f, 0.0f, // Bottom left  3
        }; short[] indices = { 2, 1, 0, 0, 1, 3};
        vao = new Vao().bind();
        indexBuffer.bind();
        indexBuffer.bufferData(indices);
        vertexBuffer.bind();
        vertexBuffer.bufferData(vertices);
        int posPointer = 0;
        int texPointer = 2 * Float.BYTES;
        int stride = 4 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, posPointer);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, texPointer);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }
    
    public void render(Texture texture) {
        shaderProgram.use();
        shaderProgram.setUniform1i("u_sampler",0);
        texture.bindToSlot(0);
        vao.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }

    public void dispose() {
        Disposable.dispose(vao);
        Disposable.dispose(indexBuffer);
        Disposable.dispose(vertexBuffer);
        Disposable.dispose(shaderProgram);
    }

    public static String default_screen_vs_shader() {
        return """
                #version 440
                layout (location=0) in vec4 a_pos;
                layout (location=1) in vec2 a_uv;
                out vec2 uv;
                void main() {
                    uv = a_uv;
                    gl_Position = a_pos;
                }""";
    }

    public static String default_screen_fs_shader() {
        return """
                #version 440
                layout (location=0) out vec4 f_color;
                in vec2 uv;
                uniform sampler2D u_sampler;
                void main() {
                    f_color = vec4(texture(u_sampler,uv));
                }""";
    }
}
