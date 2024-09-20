package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.ui.GUI;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.system.MemoryUtil;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 *
 * v2------v3
 * |        |
 * |        |
 * v0------v1
 * 00000000 00000000 00000000 00001111 texture_slot_diffuse
 * 00000000 00000000 00000000 11110000 texture_slot_normals
 * 00000000 00000000 00011111 00000000 glow
 * 00000000 00000000 00100000 00000000 pixel AA
 * 00000000 00000000 01000000 00000000 rounded
 * 00000000 00000000 10000000 00000000 invisible_id (draw_alpha)
 * 11111111 11111111 00000000 00000000 pixel_id
 * Attempting to render disposed textures will color the output Pink (Color.ERROR_BITS)
 * @author Frederik Dahl
 * 20/10/2023
 */


public class SpriteBatchGUI extends BatchGUI {

    public static final int NUM_TEXTURE_SLOTS = 15;
    private final BufferObject indices;
    private final SamplerArray samplersDiffuse;
    private final SamplerArray samplersNormals;
    private boolean pixel_antialiasing;

    SpriteBatchGUI(int capacity, int width, int height) {
        int vertex_size = 6;
        int sprite_size = vertex_size * 4;
        int vertex_size_bytes = vertex_size * Float.BYTES;
        samplersDiffuse = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,0);
        samplersNormals = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,NUM_TEXTURE_SLOTS);
        buffer_capacity = capacity;
        vertices = MemoryUtil.memAllocFloat(buffer_capacity * sprite_size);
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
        updateResolution(width, height);
    }

    public void enablePixelArtAntialiasing(boolean enable) {
        this.pixel_antialiasing = enable;
    }

    void push(Rectanglef quad, int abgr, int id, float glow, boolean draw_alpha, boolean ellipse) {
        if (count == buffer_capacity) flush();
        //int bits = ((round(clamp(glow) * 63.0f) & 0x3F) << 8);
        //bits |= 0xFF;
        //if (ellipse) bits |= 0x4000;
        //bits |= draw_alpha ? 1 << 15 : 0;
        //bits |= (id & 0xFFFF) << 16;
        int bits = ((round(clamp(glow) * 31.0f) & 0x1F) << 8);
        bits |= 0xFF;
        if (ellipse) bits |= 0x4000;
        if (pixel_antialiasing) bits |= 0x2000;
        bits |= draw_alpha ? 1 << 15 : 0;
        bits |= (id & 0xFFFF) << 16;
        float shader_bits = Float.intBitsToFloat(bits);
        float color = Color.intBits_to_floatBits(abgr);
        vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits);
        vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(shader_bits);
        vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(shader_bits);
        vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits);
        count++;
    }

    void push(Rectanglef quad, float rotation, int abgr, int id, float glow, boolean draw_alpha) {
        if (count == buffer_capacity) flush();
        int bits = ((round(clamp(glow) * 31.0f) & 0x1F) << 8);
        bits |= 0xFF;
        bits |= draw_alpha ? 1 << 15 : 0;
        bits |= (id & 0xFFFF) << 16;
        float shader_bits = Float.intBitsToFloat(bits);
        float color = Color.intBits_to_floatBits(abgr);
        if (rotation == ROT_0) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 2
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 0
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 1
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 3
        } else if (rotation == ROT_90) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 0
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 1
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 3
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 2
        } else if (rotation == ROT_180) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 1
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 3
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 2
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 0
        } else if (rotation == ROT_270) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 3
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 2
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(shader_bits); // 0
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(shader_bits); // 1
        } else {
            final float cx = quad.minX + quad.lengthX() * 0.5f;
            final float cy = quad.minY + quad.lengthY() * 0.5f;
            final float sin = (float) Math.sin(rotation);
            final float cos = (float) Math.cos(rotation);
            quad.translate(-cx,-cy);
            final float sin_minX = sin * quad.minX;
            final float cos_minX = cos * quad.minX;
            final float sin_minY = sin * quad.minY;
            final float cos_minY = cos * quad.minY;
            final float sin_maxX = sin * quad.maxX;
            final float cos_maxX = cos * quad.maxX;
            final float sin_maxY = sin * quad.maxY;
            final float cos_maxY = cos * quad.maxY;
            final float v0x = cos_minX - sin_minY + cx;
            final float v0y = sin_minX + cos_minY + cy;
            final float v1x = cos_maxX - sin_minY + cx;
            final float v1y = sin_maxX + cos_minY + cy;
            final float v2x = cos_minX - sin_maxY + cx;
            final float v2y = sin_minX + cos_maxY + cy;
            final float v3x = cos_maxX - sin_maxY + cx;
            final float v3y = sin_maxX + cos_maxY + cy;
            quad.translate(cx,cy);
            vertices.put(v2x).put(v2y).put(0).put(0).put(color).put(shader_bits); // 2
            vertices.put(v0x).put(v0y).put(0).put(0).put(color).put(shader_bits); // 0
            vertices.put(v1x).put(v1y).put(0).put(0).put(color).put(shader_bits); // 1
            vertices.put(v3x).put(v3y).put(0).put(0).put(color).put(shader_bits); // 3
        } count++;
    }

    void push(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean draw_alpha, boolean ellipse) {
        if (count == buffer_capacity) flush();
        // split into diffuse and normals. 15 texture slots (0-14). 15 = NO TEXTURE
        int diffuse_slot, normals_slot;
        if (diffuse == null)
            diffuse_slot = 15;
        else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        }  else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        if (normals == null)
            normals_slot = 15;
        else if (normals.hasBeenDisposed()) {
            normals_slot = 15;
            abgr = Color.ERROR_BITS;
        }  else {
            normals_slot = samplersNormals.assignSlot(normals);
            if (normals_slot == 15) { flush();
                normals_slot = samplersNormals.assignSlot(normals);
            }
        }
        int bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
        bits |= ((round(clamp(glow) * 31.0f) & 0x1F) << 8);
        bits |= draw_alpha ? 1 << 15 : 0;
        bits |= (id & 0xFFFF) << 16;
        if (ellipse) bits |= 0x4000;
        if (pixel_antialiasing) bits |= 0x2000;
        float shader_bits = Float.intBitsToFloat(bits);
        float color = Color.intBits_to_floatBits(abgr);
        vertices.put(quad.minX).put(quad.maxY).put(region.u()).put(region.v()).put(color).put(shader_bits);
        vertices.put(quad.minX).put(quad.minY).put(region.u()).put(region.v2()).put(color).put(shader_bits);
        vertices.put(quad.maxX).put(quad.minY).put(region.u2()).put(region.v2()).put(color).put(shader_bits);
        vertices.put(quad.maxX).put(quad.maxY).put(region.u2()).put(region.v()).put(color).put(shader_bits);
        count++;


    }

    void push(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id, float glow, boolean draw_alpha) {
        if (count == buffer_capacity) flush();
        int diffuse_slot, normals_slot;
        if (diffuse == null)
            diffuse_slot = 15;
        else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        }  else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        if (normals == null)
            normals_slot = 15;
        else if (normals.hasBeenDisposed()) {
            normals_slot = 15;
            abgr = Color.ERROR_BITS;
        }  else {
            normals_slot = samplersNormals.assignSlot(normals);
            if (normals_slot == 15) { flush();
                normals_slot = samplersNormals.assignSlot(normals);
            }
        }
        int bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
        bits |= ((round(clamp(glow) * 31.0f) & 0x1F) << 8);
        bits |= draw_alpha ? 1 << 15 : 0;
        bits |= (id & 0xFFFF) << 16;
        if (pixel_antialiasing) bits |= 0x2000;
        final float shader_bits = Float.intBitsToFloat(bits);
        final float color = Color.intBits_to_floatBits(abgr);
        final float u0 = region.u();
        final float v0 = region.v2();
        final float u2 = region.u();
        final float v2 = region.v();
        final float u3 = region.u2();
        final float v3 = region.v();
        final float u1 = region.u2();
        final float v1 = region.v2();
        if (rotation == ROT_0) {
            vertices.put(quad.minX).put(quad.maxY).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(quad.minX).put(quad.minY).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(quad.maxX).put(quad.minY).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(quad.maxX).put(quad.maxY).put(u3).put(v3).put(color).put(shader_bits); // 3
        } else if (rotation == ROT_90) {
            vertices.put(quad.minX).put(quad.maxY).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(quad.minX).put(quad.minY).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(quad.maxX).put(quad.minY).put(u3).put(v3).put(color).put(shader_bits); // 3
            vertices.put(quad.maxX).put(quad.maxY).put(u2).put(v2).put(color).put(shader_bits); // 2
        } else if (rotation == ROT_180) {
            vertices.put(quad.minX).put(quad.maxY).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(quad.minX).put(quad.minY).put(u3).put(v3).put(color).put(shader_bits); // 3
            vertices.put(quad.maxX).put(quad.minY).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(quad.maxX).put(quad.maxY).put(u0).put(v0).put(color).put(shader_bits); // 0
        } else if (rotation == ROT_270) {
            vertices.put(quad.minX).put(quad.maxY).put(u3).put(v3).put(color).put(shader_bits); // 3
            vertices.put(quad.minX).put(quad.minY).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(quad.maxX).put(quad.minY).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(quad.maxX).put(quad.maxY).put(u1).put(v1).put(color).put(shader_bits); // 1
        } else {
            final float cx = quad.minX + quad.lengthX() * 0.5f;
            final float cy = quad.minY + quad.lengthY() * 0.5f;
            final float sin = (float) Math.sin(rotation);
            final float cos = (float) Math.cos(rotation);
            quad.translate(-cx,-cy);
            final float sin_minX = sin * quad.minX;
            final float cos_minX = cos * quad.minX;
            final float sin_minY = sin * quad.minY;
            final float cos_minY = cos * quad.minY;
            final float sin_maxX = sin * quad.maxX;
            final float cos_maxX = cos * quad.maxX;
            final float sin_maxY = sin * quad.maxY;
            final float cos_maxY = cos * quad.maxY;
            final float v0x = cos_minX - sin_minY + cx;
            final float v0y = sin_minX + cos_minY + cy;
            final float v1x = cos_maxX - sin_minY + cx;
            final float v1y = sin_maxX + cos_minY + cy;
            final float v2x = cos_minX - sin_maxY + cx;
            final float v2y = sin_minX + cos_maxY + cy;
            final float v3x = cos_maxX - sin_maxY + cx;
            final float v3y = sin_maxX + cos_maxY + cy;
            quad.translate(cx,cy);
            vertices.put(v2x).put(v2y).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(v0x).put(v0y).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(v1x).put(v1y).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(v3x).put(v3y).put(u3).put(v3).put(color).put(shader_bits); // 3
        } count++;
    }

    void push(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, float glow, boolean draw_alpha, boolean ellipse) {
        if (count == buffer_capacity) flush();
        // split into diffuse and normals. 15 texture slots (0-14). 15 = NO TEXTURE
        int diffuse_slot, normals_slot;
        if (diffuse == null)
            diffuse_slot = 15;
        else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        } else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        if (normals == null)
            normals_slot = 15;
        else if (normals.hasBeenDisposed()) {
            normals_slot = 15;
            abgr = Color.ERROR_BITS;
        } else {
            normals_slot = samplersNormals.assignSlot(normals);
            if (normals_slot == 15) { flush();
                normals_slot = samplersNormals.assignSlot(normals);
            }
        }
        int shader_bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
        shader_bits |= ((round(clamp(glow) * 31.0f) & 0x1F) << 8);
        shader_bits |= draw_alpha ? 1 << 15 : 0;
        shader_bits |= (id & 0xFFFF) << 16;
        if (pixel_antialiasing) shader_bits |= 0x2000;
        if (ellipse) shader_bits |= 0x4000;
        float shader_ = Float.intBitsToFloat(shader_bits);
        float color = Color.intBits_to_floatBits(abgr);
        vertices.put(quad.minX).put(quad.maxY).put(region.x).put(region.y).put(color).put(shader_);
        vertices.put(quad.minX).put(quad.minY).put(region.x).put(region.w).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.minY).put(region.z).put(region.w).put(color).put(shader_);
        vertices.put(quad.maxX).put(quad.maxY).put(region.z).put(region.y).put(color).put(shader_);
        count++;
    }

    void push(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, float rotation, int abgr, int id, float glow, boolean draw_alpha) {
        if (count == buffer_capacity) flush();
        int diffuse_slot, normals_slot;
        if (diffuse == null)
            diffuse_slot = 15;
        else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        } else { diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        } if (normals == null)
            normals_slot = 15;
        else if (normals.hasBeenDisposed()) {
            normals_slot = 15;
            abgr = Color.ERROR_BITS;
        } else { normals_slot = samplersNormals.assignSlot(normals);
            if (normals_slot == 15) { flush();
                normals_slot = samplersNormals.assignSlot(normals);
            }
        }
        int bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
        bits |= ((round(clamp(glow) * 31.0f) & 0x1F) << 8);
        bits |= draw_alpha ? 1 << 15 : 0;
        bits |= (id & 0xFFFF) << 16;
        if (pixel_antialiasing) bits |= 0x2000;
        final float shader_bits = Float.intBitsToFloat(bits);
        final float color = Color.intBits_to_floatBits(abgr);
        final float u0 = region.x;
        final float v0 = region.w;
        final float u2 = region.x;
        final float v2 = region.y;
        final float u3 = region.z;
        final float v3 = region.y;
        final float u1 = region.z;
        final float v1 = region.w;
        if (rotation == ROT_0) {
            vertices.put(quad.minX).put(quad.maxY).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(quad.minX).put(quad.minY).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(quad.maxX).put(quad.minY).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(quad.maxX).put(quad.maxY).put(u3).put(v3).put(color).put(shader_bits); // 3
        } else if (rotation == ROT_90) {
            vertices.put(quad.minX).put(quad.maxY).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(quad.minX).put(quad.minY).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(quad.maxX).put(quad.minY).put(u3).put(v3).put(color).put(shader_bits); // 3
            vertices.put(quad.maxX).put(quad.maxY).put(u2).put(v2).put(color).put(shader_bits); // 2
        } else if (rotation == ROT_180) {
            vertices.put(quad.minX).put(quad.maxY).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(quad.minX).put(quad.minY).put(u3).put(v3).put(color).put(shader_bits); // 3
            vertices.put(quad.maxX).put(quad.minY).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(quad.maxX).put(quad.maxY).put(u0).put(v0).put(color).put(shader_bits); // 0
        } else if (rotation == ROT_270) {
            vertices.put(quad.minX).put(quad.maxY).put(u3).put(v3).put(color).put(shader_bits); // 3
            vertices.put(quad.minX).put(quad.minY).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(quad.maxX).put(quad.minY).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(quad.maxX).put(quad.maxY).put(u1).put(v1).put(color).put(shader_bits); // 1
        } else {
            final float cx = quad.minX + quad.lengthX() * 0.5f;
            final float cy = quad.minY + quad.lengthY() * 0.5f;
            final float sin = (float) Math.sin(rotation);
            final float cos = (float) Math.cos(rotation);
            quad.translate(-cx,-cy);
            final float sin_minX = sin * quad.minX;
            final float cos_minX = cos * quad.minX;
            final float sin_minY = sin * quad.minY;
            final float cos_minY = cos * quad.minY;
            final float sin_maxX = sin * quad.maxX;
            final float cos_maxX = cos * quad.maxX;
            final float sin_maxY = sin * quad.maxY;
            final float cos_maxY = cos * quad.maxY;
            final float v0x = cos_minX - sin_minY + cx;
            final float v0y = sin_minX + cos_minY + cy;
            final float v1x = cos_maxX - sin_minY + cx;
            final float v1y = sin_maxX + cos_minY + cy;
            final float v2x = cos_minX - sin_maxY + cx;
            final float v2y = sin_minX + cos_maxY + cy;
            final float v3x = cos_maxX - sin_maxY + cx;
            final float v3y = sin_maxX + cos_maxY + cy;
            quad.translate(cx,cy);
            vertices.put(v2x).put(v2y).put(u2).put(v2).put(color).put(shader_bits); // 2
            vertices.put(v0x).put(v0y).put(u0).put(v0).put(color).put(shader_bits); // 0
            vertices.put(v1x).put(v1y).put(u1).put(v1).put(color).put(shader_bits); // 1
            vertices.put(v3x).put(v3y).put(u3).put(v3).put(color).put(shader_bits); // 3
        } count++;
    }

    void flush() {
        if (count > 0) {
            ShaderProgram.bindProgram(GUI.shaders.sprite_program);
            samplersDiffuse.uploadUniform("u_diffuse_textures");
            samplersNormals.uploadUniform("u_normals_textures");
            vertices.flip();
            vertexAttribArray.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices, 0);
            glDrawElements(GL_TRIANGLES,6 * count,GL_UNSIGNED_SHORT,0);
            draw_calls++;
            vertices.clear();
            count = 0;
        }
    }

    void updateResolution(int width, int height) {
        ShaderProgram.bindProgram(GUI.shaders.sprite_program);
        ShaderProgram.setUniform("u_resolution",(float) width,(float) height);
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
