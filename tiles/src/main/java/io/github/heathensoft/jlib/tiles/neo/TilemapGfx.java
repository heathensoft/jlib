package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;

/**
 * @author Frederik Dahl
 * 27/06/2023
 */


public class TilemapGfx implements Disposable {

    private final VertexAttributes gfx_tiles_vao;
    private final BufferObject gfx_tiles_vbo;

    public TilemapGfx() {
        gfx_tiles_vao = new VertexAttributes().bind();
        gfx_tiles_vbo = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW).bind();
        gfx_tiles_vbo.bufferData((long) 256 * Integer.BYTES);
        glVertexAttribIPointer(0,1,GL_UNSIGNED_INT,Integer.BYTES,0);
        glEnableVertexAttribArray(0);
    }


    public void upload_terrain(Tilemap tilemap, int chunk_x, int chunk_y) {

    }

    public void upload_blocks(Tilemap tilemap, int chunk_x, int chunk_y) {

    }

    public void dispose() {
        Disposable.dispose(
                gfx_tiles_vao,
                gfx_tiles_vbo
        );
    }
}
