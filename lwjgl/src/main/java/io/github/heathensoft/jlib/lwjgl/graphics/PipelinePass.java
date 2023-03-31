package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 16/03/2023
 */


public class PipelinePass implements Disposable {

    private final Vao vao;
    private final BufferObject indexBuffer;
    private final BufferObject vertexBuffer;
    private final FloatBuffer vertices;

    private static PipelinePass instance;

    public static PipelinePass get() {
        if (instance == null) {
            instance = new PipelinePass();
        } return instance;
    }

    public static void terminate() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }

    private PipelinePass() {
        vertices = MemoryUtil.memAllocFloat(16);
        indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
        vao = new Vao().bind();
        indexBuffer.bind().bufferData(new byte[] { 2, 1, 0, 0, 1, 3});
        vertexBuffer.bind().bufferData(16 * Float.BYTES);
        int posPointer = 0;
        int texPointer = 2 * Float.BYTES;
        int stride = 4 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, posPointer);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, texPointer);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }

    public void drawViewportNDC() {
        drawViewportNDC(0.0f,0.0f,1.0f,1.0f);
    }

    public void drawViewportNDC(float u, float v, float u2, float v2) {
        draw(-1.0f,-1.0f,1.0f,1.0f,u,v,u2,v2);
    }

    public void draw(float x, float y, float x2, float y2, float u, float v, float u2, float v2) {
        vertices.put(x2).put(y).put(u2).put(v);     // bottom_right
        vertices.put(x).put(y2).put(u).put(v2);     // top_left
        vertices.put(x2).put(y2).put(u2).put(v2);   // top_right
        vertices.put(x).put(y).put(u).put(v);       // bottom_left
        vertices.flip();
        vertexBuffer.bind();
        vertexBuffer.bufferSubData(vertices,0);
        vertices.clear();
        vao.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_BYTE,0);
    }

    public void dispose() {
        if (vertices != null) MemoryUtil.memFree(vertices);
        Disposable.dispose(vao);
        Disposable.dispose(indexBuffer);
        Disposable.dispose(vertexBuffer);
    }
}
