package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.system.MemoryUtil;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 20/10/2023
 */


public class SpriteBatchGUI extends BatchGUI {

    public static final String SHADER_VERT = "res/jlib/gui/glsl/ui_sprite.vert";
    public static final String SHADER_FRAG = "res/jlib/gui/glsl/ui_sprite.frag";
    public static final int NUM_TEXTURE_SLOTS = 15;
    private final BufferObject indices;
    private final SamplerArray samplersDiffuse;
    private final SamplerArray samplersNormals;

    SpriteBatchGUI(int capacity, int width, int height) throws Exception {
        int vertex_size = 6;
        int sprite_size = vertex_size * 4;
        int vertex_size_bytes = vertex_size * Float.BYTES;
        samplersDiffuse = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,0);
        samplersNormals = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,NUM_TEXTURE_SLOTS);
        buffer_capacity = capacity;
        vertices = MemoryUtil.memAllocFloat(buffer_capacity * vertex_size);
        indices = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vertexAttribArray = new VertexAttributes().bind();
        indices.bind().bufferData(generateIndices(buffer_capacity));
        vertexBuffer.bind().bufferData((long) sprite_size * buffer_capacity * Float.BYTES); int pointer = 0;
        glVertexAttribPointer(0,2,GL_FLOAT,false,vertex_size_bytes,pointer);
        glEnableVertexAttribArray(0); pointer += 2 * Float.BYTES;
        glVertexAttribPointer(1,2,GL_FLOAT,false,vertex_size_bytes,pointer);
        glEnableVertexAttribArray(1); pointer += 2 * Float.BYTES;
        glVertexAttribPointer(2,4,GL_UNSIGNED_BYTE,true,vertex_size_bytes,pointer);
        glEnableVertexAttribArray(2); pointer += Float.BYTES;
        glVertexAttribPointer(3,1,GL_FLOAT,false,vertex_size_bytes,pointer);
        glEnableVertexAttribArray(3);
        String vShader = Resources.asString(SHADER_VERT);
        String fShader = Resources.asString(SHADER_FRAG);
        shaderProgram = new ShaderProgramOld(vShader,fShader);
        shaderProgram.createUniform("u_resolution");
        shaderProgram.createUniform("u_diffuse_textures");
        shaderProgram.createUniform("u_normals_textures");
        setResolutionUniform(width, height);
    }


    void draw(Rectanglef quad, int abgr, int id, float glow, boolean drawAlpha) {
        if (count == buffer_capacity) flush();
        int shader_bits = 255 | (round(clamp(glow) * 127.0f) << 8);
        shader_bits |= drawAlpha ? 1 << 15 : 0;
        shader_bits |= (id & 0xFFFF) << 16;
        float shader_ = Float.intBitsToFloat(shader_bits);
        float color = Color.intBits_to_floatBits(abgr);
        vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(shader_);
        vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(shader_);
        count++;
    }

    void draw(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean drawAlpha) {
        if (count == buffer_capacity) flush();
        // split into diffuse and normals. 15 texture slots (0-14). 15 = NO TEXTURE
        int diffuse_slot = samplersDiffuse.assignSlot(diffuse);
        if (diffuse_slot == 15) { flush();
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
        } int normals_slot;
        if (normals != null) {
            normals_slot = samplersNormals.assignSlot(normals);
            if (normals_slot == 15) { flush();
                normals_slot = samplersNormals.assignSlot(normals);
            }
        } else normals_slot = 15;
        int shader_bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
        shader_bits |= (round(clamp(glow) * 127.0f) << 8);
        shader_bits |= drawAlpha ? 1 << 15 : 0;
        shader_bits |= (id & 0xFFFF) << 16;
        float shader_ = Float.intBitsToFloat(shader_bits);
        float color = Color.intBits_to_floatBits(abgr);
        vertices.put(quad.minX).put(quad.maxY).put(region.u()).put(region.v()).put(color).put(shader_);
        vertices.put(quad.minX).put(quad.minY).put(region.u()).put(region.v2()).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.minY).put(region.u2()).put(region.v2()).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.maxY).put(region.u2()).put(region.v()).put(color).put(shader_);
        count++;
    }

    void draw(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, float glow, boolean drawAlpha) {
        if (count == buffer_capacity) flush();
        // split into diffuse and normals. 15 texture slots (0-14). 15 = NO TEXTURE
        int diffuse_slot = samplersDiffuse.assignSlot(diffuse);
        if (diffuse_slot == 15) { flush();
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
        } int normals_slot;
        if (normals != null) {
            normals_slot = samplersNormals.assignSlot(normals);
            if (normals_slot == 15) { flush();
                normals_slot = samplersNormals.assignSlot(normals);
            }
        } else normals_slot = 15;
        int shader_bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
        shader_bits |= (round(clamp(glow) * 127.0f) << 8);
        shader_bits |= drawAlpha ? 1 << 15 : 0;
        shader_bits |= (id & 0xFFFF) << 16;
        float shader_ = Float.intBitsToFloat(shader_bits);
        float color = Color.intBits_to_floatBits(abgr);
        vertices.put(quad.minX).put(quad.maxY).put(region.x).put(region.y).put(color).put(shader_);
        vertices.put(quad.minX).put(quad.minY).put(region.x).put(region.w).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.minY).put(region.z).put(region.w).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.maxY).put(region.z).put(region.y).put(color).put(shader_);
        count++;
    }

    void flush() {
        if (count > 0) {
            vertices.flip();
            vertexAttribArray.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices, 0);
            shaderProgram.use();
            //samplersNormals.uploadUniform(shaderProgram,"u_normals_textures");
            glDrawElements(GL_TRIANGLES,6 * count,GL_UNSIGNED_SHORT,0);
            vertices.clear();
            draw_calls++;
            count = 0;
        }
    }


    public void dispose() {
        super.dispose();
        Disposable.dispose(indices);
    }

    private short[] generateIndices(int sprites) {
        int len = sprites * 6;
        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
        } return indices;
    }
}
