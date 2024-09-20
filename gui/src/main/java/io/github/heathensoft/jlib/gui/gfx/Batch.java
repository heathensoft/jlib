package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * @author Frederik Dahl
 * 30/05/2024
 */


abstract class Batch implements Disposable {
    protected VertexAttributes vertexAttribArray;
    protected BufferObject vertexBuffer;
    protected FloatBuffer vertices;
    protected int buffer_capacity;
    protected int draw_calls;
    protected int count;
    abstract void flush();
    abstract int shaderProgram();
    int resetDrawCalls() {
        int calls = draw_calls;
        draw_calls = 0;
        return calls;
    } public void dispose() {
        if (vertices != null) MemoryUtil.memFree(vertices);
        Disposable.dispose(vertexAttribArray,vertexBuffer);
    }
}