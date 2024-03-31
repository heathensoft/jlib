package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgramOld;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * @author Frederik Dahl
 * 20/10/2023
 */


abstract class BatchGUI implements Disposable  {
    protected VertexAttributes vertexAttribArray;
    protected ShaderProgramOld shaderProgram; // disposed externally
    protected BufferObject vertexBuffer;
    protected FloatBuffer vertices;
    protected int buffer_capacity;
    protected int draw_calls;
    protected int count;
    abstract void flush();
    void setResolutionUniform(int width, int height) {
        shaderProgram.use().setUniform("u_resolution",width,height);
    } int resetDrawCalls() {
        int calls = draw_calls;
        draw_calls = 0;
        return calls;
    } public void dispose() {
        if (vertices != null) MemoryUtil.memFree(vertices);
        Disposable.dispose(vertexAttribArray,vertexBuffer,shaderProgram);
    }
}
