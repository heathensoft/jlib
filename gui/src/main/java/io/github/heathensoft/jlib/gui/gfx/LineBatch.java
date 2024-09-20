package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 28/07/2024
 */


public class LineBatch extends Batch {

    private int prev_abgr;
    private float color;

    LineBatch(int capacity) {
        int vertex_size = 3;
        int line_vertices = 2;
        int line_size = vertex_size * line_vertices;
        buffer_capacity = capacity;
        vertices = MemoryUtil.memAllocFloat(buffer_capacity * line_size);
        vertexAttribArray = new VertexAttributes().bind();
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vertexBuffer.bind().bufferData((long) line_size * Float.BYTES * buffer_capacity);
        glVertexAttribPointer(0,2,GL_FLOAT,false,vertex_size * Float.BYTES,0);
        glVertexAttribPointer(1,1,GL_FLOAT,false,vertex_size * Float.BYTES,2 * Float.BYTES);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        prev_abgr = Color.WHITE_BITS;
        color = Color.intBits_to_floatBits(prev_abgr);
    }

    void push(Vector2f p1, Vector2f p2, int abgr) {
        push(p1.x,p1.y,p2.x,p2.y, abgr);
    }

    void push(float x1, float y1, float x2, float y2, int abgr) {
        if (abgr != prev_abgr) {
            color = Color.intBits_to_floatBits(abgr);
            prev_abgr = abgr;
        } push(x1, y1, x2, y2);
    }

    void push(Vector2f p1, Vector2f p2) {
        push(p1.x,p1.y,p2.x,p2.y);
    }

    void push(float x1, float y1, float x2, float y2) {
        if (count == buffer_capacity) flush();
        vertices.put(x1).put(y1).put(x2).put(y2).put(color);
        count++;
    }

    void flush() {
        if (count > 0) {
            ShaderProgram.bindProgram(GUI.shaders.line_program);
            vertices.flip();
            vertexAttribArray.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices,0);
            glDrawArrays(GL_LINES,0,count * 2);
            draw_calls++;
            vertices.clear();
            count = 0;
        }
    }

    int shaderProgram() {
        return GUI.shaders.line_program;
    }

    public void setLineWidth(float width) {
        glLineWidth(width);
    }

    public float getLineWidth() {
        return glGetFloat(GL_LINE_WIDTH);
    }

    public void enableSmoothLines(boolean enable) {
        if (enable) glEnable(GL_LINE_SMOOTH);
        else glDisable(GL_LINE_SMOOTH);
    }

    public boolean smoothLinesEnabled() {
        return glIsEnabled(GL_LINE_SMOOTH);
    }
}
