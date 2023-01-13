package io.github.heathensoft.jlib.graphicsOld;


import io.github.heathensoft.jlib.common.Disposable;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

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
    
    
    SpriteVertexData(int sprites) {
        indices = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexData = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vao = new Vao().bind();
        indices.bind();
        indices.bufferData(Sprite.generateIndices(sprites));
        vertexData.bind();
        vertexData.bufferData((long) Sprite.SIZE * sprites * Float.BYTES);
        int pointer = 0;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, Sprite.VERTEX_SIZE * Float.BYTES, pointer);
        glEnableVertexAttribArray(0);
        pointer += Sprite.POS_SIZE * Float.BYTES;
        glVertexAttribPointer(1, 2, GL_FLOAT, false, Sprite.VERTEX_SIZE * Float.BYTES, pointer);
        glEnableVertexAttribArray(1);
        pointer += Sprite.UV_SIZE * Float.BYTES;
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, Sprite.VERTEX_SIZE * Float.BYTES, pointer);
        glEnableVertexAttribArray(2);
        pointer += Sprite.COLOR_SIZE * Float.BYTES;
        glVertexAttribPointer(3, 1, GL_FLOAT, false, Sprite.VERTEX_SIZE * Float.BYTES, pointer);
        glEnableVertexAttribArray(3);
    }
    
    void render(FloatBuffer vertexBuffer, int sprites) {
        vertexBuffer.flip();
        vao.bind();
        vertexData.bind();
        vertexData.bufferSubData(vertexBuffer,0);
        glDrawElements(GL_TRIANGLES, Sprite.NUM_INDICES * sprites,GL_UNSIGNED_SHORT,0);
        vertexBuffer.clear();
    }
    
    @Override
    public void dispose() {
        Disposable.dispose(vao,indices,vertexData);
    }
}
