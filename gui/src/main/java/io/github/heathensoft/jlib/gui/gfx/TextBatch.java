package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.Vao;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 20/10/2023
 */


public class TextBatch extends UIBatch {

    public static final String SHADER_VERT = "res/jlib/lwjgl/glsl/ui_text.vert";
    public static final String SHADER_GEOM = "res/jlib/lwjgl/glsl/ui_text.geom";
    public static final String SHADER_FRAG = "res/jlib/lwjgl/glsl/ui_text.frag";
    private final Fonts fonts;

    TextBatch(Fonts fonts, int capacity, int width, int height) throws Exception {
        int vertex_size = 3;
        int vertex_size_bytes = vertex_size * Float.BYTES;
        this.fonts = fonts;
        buffer_capacity = capacity;
        vertexAttribArray = new Vao().bind();
        vertices = MemoryUtil.memAllocFloat(capacity * vertex_size);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vertexBuffer.bind().bufferData((long) vertex_size_bytes * capacity);
        glVertexAttribPointer(0, vertex_size, GL_FLOAT, false, vertex_size_bytes, 0);
        glEnableVertexAttribArray(0);
        String vShader = Resources.asString(SHADER_VERT);
        String gShader = Resources.asString(SHADER_GEOM);
        String fShader = Resources.asString(SHADER_FRAG);
        shaderProgram = new ShaderProgram(vShader,gShader,fShader);
        shaderProgram.createUniform("u_resolution");
        shaderProgram.createUniform("u_font_textures");
        setResolutionUniform(width, height);
    }

    public Fonts fonts() {
        return fonts;
    }

    public void pushVertex(float x, float y, int i) {
        if (count == buffer_capacity) flush();
        vertices.put(x).put(y).put(Float.intBitsToFloat(i));
        count++;
    }

    void flush() {
        if (count > 0) {
            shaderProgram.use();
            fonts.bindUploadTextures(shaderProgram,"u_font_textures");
            vertices.flip();
            vertexAttribArray.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices, 0);
            glDrawArrays(GL_POINTS, 0, count);
            vertices.clear();
            draw_calls++;
            count = 0;
        }
    }

}
