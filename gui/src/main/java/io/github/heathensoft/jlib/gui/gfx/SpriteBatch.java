package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.system.MemoryUtil;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.ROT_270;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * Attempting to render disposed textures will color the output Pink (Color.ERROR_BITS)
 *
 * v2------v3
 * |        |
 * |        |
 * v0------v1
 *
 * // 00000000 00000000 00000000 00001111 texture_slot     Offset: 0
 * // 00000000 00000000 00000000 00010000 pixel_art        Offset: 4
 * // 00000000 00000000 00000000 00100000 ellipse          Offset: 5
 * // 00000000 00000000 00000000 01000000 transparent_id   Offset: 6
 * // 00000000 00000000 11111111 00000000 glow             Offset: 8
 * // 11111111 11111111 00000000 00000000 interactable_id  Offset: 16
 *
 *
 * @author Frederik Dahl
 * 30/05/2024
 */


public class SpriteBatch extends Batch {

    public static final int NUM_TEXTURE_SLOTS = 15;

    private final BufferObject indices;
    private final SamplerArray samplersDiffuse;
    private boolean pixel_art_antialiasing;     // reset these every frame ? false
    private boolean render_transparent_id;      // reset these every frame ? true
    private boolean render_ellipse;             // reset these every frame ? false


    SpriteBatch(int capacity) {
        int vertex_size = 6;
        int sprite_size = vertex_size * 4;
        int vertex_size_bytes = vertex_size * Float.BYTES;
        samplersDiffuse = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,0);
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
        pixel_art_antialiasing = false;
        render_transparent_id = true;
        render_ellipse = false;
    }

    public void enablePixelArtAntialiasing(boolean enable) { pixel_art_antialiasing = enable; }
    public void enableTransparentID(boolean enable) { render_transparent_id = enable; }
    public void enableRenderEllipse(boolean enable) { render_ellipse = enable; }
    public boolean isPixelArtAntialiasingEnabled() { return pixel_art_antialiasing; }
    public boolean isRenderTransparentIdEnabled() { return render_transparent_id; }
    public boolean isRenderEllipseEnabled() { return render_ellipse; }
    public void dispose() { super.dispose();
        Disposable.dispose(indices);
    }

    void push(Rectanglef quad, int abgr, int id, float glow) {
        if (count == buffer_capacity) flush();
        float color = Color.intBits_to_floatBits(abgr);
        float bits;
        {
            int int_bits = 0x0F; // NO TEXTURE
            int_bits = bitsApplySingletons(int_bits);
            int_bits = bitsApplyGlow(int_bits,glow);
            int_bits = bitsApplyID(int_bits,id);
            bits = Float.intBitsToFloat(int_bits);
        }
        vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(bits);
        vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(bits);
        vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(bits);
        vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(bits);
        count++;
    }

    void push(Rectanglef quad, float rotation, int abgr, int id, float glow) {
        if (count == buffer_capacity) flush();
        float color = Color.intBits_to_floatBits(abgr);
        float bits;
        {
            int int_bits = 0x0F; // NO TEXTURE
            int_bits = bitsApplySingletons(int_bits);
            int_bits = bitsApplyGlow(int_bits,glow);
            int_bits = bitsApplyID(int_bits,id);
            bits = Float.intBitsToFloat(int_bits);
        }
        if (rotation == ROT_0) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 2
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(bits); // 0
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(bits); // 1
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 3
        } else if (rotation == ROT_90) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 0
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(bits); // 1
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(bits); // 3
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 2
        } else if (rotation == ROT_180) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 1
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(bits); // 3
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(bits); // 2
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 0
        } else if (rotation == ROT_270) {
            vertices.put(quad.minX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 3
            vertices.put(quad.minX).put(quad.minY).put(0).put(0).put(color).put(bits); // 2
            vertices.put(quad.maxX).put(quad.minY).put(0).put(0).put(color).put(bits); // 0
            vertices.put(quad.maxX).put(quad.maxY).put(0).put(0).put(color).put(bits); // 1
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
            vertices.put(v2x).put(v2y).put(0).put(0).put(color).put(bits); // 2
            vertices.put(v0x).put(v0y).put(0).put(0).put(color).put(bits); // 0
            vertices.put(v1x).put(v1y).put(0).put(0).put(color).put(bits); // 1
            vertices.put(v3x).put(v3y).put(0).put(0).put(color).put(bits); // 3
        } count++;
    }

    void push(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, float glow) {
        if (count == buffer_capacity) flush();
        int diffuse_slot;
        if (diffuse == null) {
            diffuse_slot = 15;
        } else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        } else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        float color_bits = Color.intBits_to_floatBits(abgr);
        float float_bits;
        {
            int int_bits = diffuse_slot; // NO TEXTURE
            int_bits = bitsApplySingletons(int_bits);
            int_bits = bitsApplyGlow(int_bits,glow);
            int_bits = bitsApplyID(int_bits,id);
            float_bits = Float.intBitsToFloat(int_bits);
        }
        vertices.put(quad.minX).put(quad.maxY).put(region.u()).put(region.v()).put(color_bits).put(float_bits);
        vertices.put(quad.minX).put(quad.minY).put(region.u()).put(region.v2()).put(color_bits).put(float_bits);
        vertices.put(quad.maxX).put(quad.minY).put(region.u2()).put(region.v2()).put(color_bits).put(float_bits);
        vertices.put(quad.maxX).put(quad.maxY).put(region.u2()).put(region.v()).put(color_bits).put(float_bits);
        count++;
    }

    void push(Texture diffuse, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id, float glow) {
        if (count == buffer_capacity) flush();
        int diffuse_slot;
        if (diffuse == null) {
            diffuse_slot = 15;
        } else if (diffuse.hasBeenDisposed()) {
            abgr = Color.ERROR_BITS;
            diffuse_slot = 15;
        } else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        float color = Color.intBits_to_floatBits(abgr);
        float bits;
        {
            int int_bits = diffuse_slot; // NO TEXTURE
            int_bits = bitsApplySingletons(int_bits);
            int_bits = bitsApplyGlow(int_bits,glow);
            int_bits = bitsApplyID(int_bits,id);
            bits = Float.intBitsToFloat(int_bits);
        }
        final float u0 = region.u();
        final float v0 = region.v2();
        final float u2 = region.u();
        final float v2 = region.v();
        final float u3 = region.u2();
        final float v3 = region.v();
        final float u1 = region.u2();
        final float v1 = region.v2();
        if (rotation == ROT_0) {
            vertices.put(quad.minX).put(quad.maxY).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(quad.minX).put(quad.minY).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(quad.maxX).put(quad.minY).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(quad.maxX).put(quad.maxY).put(u3).put(v3).put(color).put(bits); // 3
        } else if (rotation == ROT_90) {
            vertices.put(quad.minX).put(quad.maxY).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(quad.minX).put(quad.minY).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(quad.maxX).put(quad.minY).put(u3).put(v3).put(color).put(bits); // 3
            vertices.put(quad.maxX).put(quad.maxY).put(u2).put(v2).put(color).put(bits); // 2
        } else if (rotation == ROT_180) {
            vertices.put(quad.minX).put(quad.maxY).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(quad.minX).put(quad.minY).put(u3).put(v3).put(color).put(bits); // 3
            vertices.put(quad.maxX).put(quad.minY).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(quad.maxX).put(quad.maxY).put(u0).put(v0).put(color).put(bits); // 0
        } else if (rotation == ROT_270) {
            vertices.put(quad.minX).put(quad.maxY).put(u3).put(v3).put(color).put(bits); // 3
            vertices.put(quad.minX).put(quad.minY).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(quad.maxX).put(quad.minY).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(quad.maxX).put(quad.maxY).put(u1).put(v1).put(color).put(bits); // 1
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
            vertices.put(v2x).put(v2y).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(v0x).put(v0y).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(v1x).put(v1y).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(v3x).put(v3y).put(u3).put(v3).put(color).put(bits); // 3
        } count++;
    }

    void push(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, float glow) {
        if (count == buffer_capacity) flush();
        int diffuse_slot;
        if (diffuse == null) {
            diffuse_slot = 15;
        } else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        } else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        float color = Color.intBits_to_floatBits(abgr);
        float bits;
        {
            int int_bits = diffuse_slot; // NO TEXTURE
            int_bits = bitsApplySingletons(int_bits);
            int_bits = bitsApplyGlow(int_bits,glow);
            int_bits = bitsApplyID(int_bits,id);
            bits = Float.intBitsToFloat(int_bits);
        }
        vertices.put(quad.minX).put(quad.maxY).put(region.x).put(region.y).put(color).put(bits);
        vertices.put(quad.minX).put(quad.minY).put(region.x).put(region.w).put(color).put(bits);
        vertices.put(quad.maxX).put(quad.minY).put(region.z).put(region.w).put(color).put(bits);
        vertices.put(quad.maxX).put(quad.maxY).put(region.z).put(region.y).put(color).put(bits);
        count++;
    }

    void push(Texture diffuse, Vector4f region, Rectanglef quad, float rotation, int abgr, int id, float glow) {
        if (count == buffer_capacity) flush();
        int diffuse_slot;
        if (diffuse == null) {
            diffuse_slot = 15;
        } else if (diffuse.hasBeenDisposed()) {
            diffuse_slot = 15;
            abgr = Color.ERROR_BITS;
        } else {
            diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            if (diffuse_slot == 15) { flush();
                diffuse_slot = samplersDiffuse.assignSlot(diffuse);
            }
        }
        float color = Color.intBits_to_floatBits(abgr);
        float bits;
        {
            int int_bits = diffuse_slot; // NO TEXTURE
            int_bits = bitsApplySingletons(int_bits);
            int_bits = bitsApplyGlow(int_bits,glow);
            int_bits = bitsApplyID(int_bits,id);
            bits = Float.intBitsToFloat(int_bits);
        }
        final float u0 = region.x;
        final float v0 = region.w;
        final float u2 = region.x;
        final float v2 = region.y;
        final float u3 = region.z;
        final float v3 = region.y;
        final float u1 = region.z;
        final float v1 = region.w;
        if (rotation == ROT_0) {
            vertices.put(quad.minX).put(quad.maxY).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(quad.minX).put(quad.minY).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(quad.maxX).put(quad.minY).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(quad.maxX).put(quad.maxY).put(u3).put(v3).put(color).put(bits); // 3
        } else if (rotation == ROT_90) {
            vertices.put(quad.minX).put(quad.maxY).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(quad.minX).put(quad.minY).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(quad.maxX).put(quad.minY).put(u3).put(v3).put(color).put(bits); // 3
            vertices.put(quad.maxX).put(quad.maxY).put(u2).put(v2).put(color).put(bits); // 2
        } else if (rotation == ROT_180) {
            vertices.put(quad.minX).put(quad.maxY).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(quad.minX).put(quad.minY).put(u3).put(v3).put(color).put(bits); // 3
            vertices.put(quad.maxX).put(quad.minY).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(quad.maxX).put(quad.maxY).put(u0).put(v0).put(color).put(bits); // 0
        } else if (rotation == ROT_270) {
            vertices.put(quad.minX).put(quad.maxY).put(u3).put(v3).put(color).put(bits); // 3
            vertices.put(quad.minX).put(quad.minY).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(quad.maxX).put(quad.minY).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(quad.maxX).put(quad.maxY).put(u1).put(v1).put(color).put(bits); // 1
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
            vertices.put(v2x).put(v2y).put(u2).put(v2).put(color).put(bits); // 2
            vertices.put(v0x).put(v0y).put(u0).put(v0).put(color).put(bits); // 0
            vertices.put(v1x).put(v1y).put(u1).put(v1).put(color).put(bits); // 1
            vertices.put(v3x).put(v3y).put(u3).put(v3).put(color).put(bits); // 3
        } count++;
    }

    void flush() {
        if (count > 0) {
            ShaderProgram.bindProgram(shaderProgram());
            samplersDiffuse.uploadUniform("u_diffuse_textures");
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

    int shaderProgram() {
        return GUI.shaders.sprite_program;
    }

    private int bitsApplyGlow(int bits, float glow) {
        int glow_bits = round(clamp(glow) * 255.0f);
        return U.bitsOr(bits,glow_bits,8,8);
    }

    private int bitsApplyID(int bits, int id) {
        return bits | ((id & 0xFFFF) << 16);
    }

    private int bitsApplySingletons(int bits) {
        if (pixel_art_antialiasing) bits |= 0x10;
        if (render_ellipse) bits |= 0x20;
        if (render_transparent_id) bits |= 0x40;
        return bits;
    }

    private static short[] generateIndices(int sprites) {
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
