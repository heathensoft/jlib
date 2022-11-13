package io.github.heathensoft.jlib.lwjgl.graphics;


import io.github.heathensoft.jlib.common.Disposable;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 * Vertex data: position | uv coordinate
 * Instance data: id (normalized 4 * unsigned byte) | color
 *
 * @author Frederik Dahl
 * 01/07/2022
 */


public class SpriteVertexData implements Disposable {
    
    private final Vao vao;
    private final BufferObject indices;
    private final BufferObject vertexData;
    private final BufferObject instanceData;
    
    
    SpriteVertexData(int sprites) {
        indices = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexData = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        instanceData = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vao = new Vao().bind();
        indices.bind();
        indices.bufferData(Sprite.generateIndices(sprites));
        vertexData.bind();
        vertexData.bufferData((long) Sprite.VERTICES_SIZE * sprites * Float.BYTES);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, Sprite.VERTEX_SIZE * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, Sprite.VERTEX_SIZE * Float.BYTES, Sprite.POS_SIZE * Float.BYTES);
        glEnableVertexAttribArray(1);
        instanceData.bind();
        instanceData.bufferData((long) Sprite.INSTANCE_DATA_SIZE * sprites * Float.BYTES);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, Sprite.INSTANCE_DATA_SIZE * Float.BYTES, 0);
        glVertexAttribDivisor(2, 1);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, Sprite.INSTANCE_DATA_SIZE * Float.BYTES, Float.BYTES);
        glVertexAttribDivisor(3, 1);
        glEnableVertexAttribArray(3);
    }
    
    void render(FloatBuffer vertexBuffer, FloatBuffer instanceBuffer, int sprites) {
        vertexBuffer.flip();
        instanceBuffer.flip();
        vao.bind();
        vertexData.bind();
        vertexData.bufferSubData(vertexBuffer,0);
        instanceData.bind();
        instanceData.bufferSubData(instanceBuffer,0);
        glDrawElementsInstanced(GL_TRIANGLES,Sprite.NUM_INDICES * sprites,GL_UNSIGNED_SHORT,0,sprites);
        vertexBuffer.clear();
        instanceBuffer.clear();
    }
    
    @Override
    public void dispose() {
        Disposable.dispose(vao,indices,vertexData,instanceData);
    }
}
