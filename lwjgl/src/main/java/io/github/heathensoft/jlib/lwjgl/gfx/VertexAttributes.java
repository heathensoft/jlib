package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;

import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL30.*;

/**
 * reference to a "vertex array object".
 * I think it's best to delete the vao before contained buffers.
 * deleting the vao first removes their references. Then calling glDeleteBuffers
 * would instead of queueing them for removal, free them immediately.
 * (If their not referenced by another container object elsewhere)
 *
 * @author Frederik Dahl
 * 29/10/2022
 */


public class VertexAttributes implements Disposable {
    private static int bound = GL_NONE;
    private final int id;
    public VertexAttributes() {
        id = glGenVertexArrays();
    }
    public VertexAttributes bind() {
        if (bound != id)  glBindVertexArray(bound = id);
        return this;
    } public void dispose() { glDeleteVertexArrays(id);
        if (bound == id) bound = GL_NONE;
    }
}
