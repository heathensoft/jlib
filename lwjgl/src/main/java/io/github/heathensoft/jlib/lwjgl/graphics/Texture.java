package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.primitive.ByteArray2D;
import io.github.heathensoft.jlib.common.storage.primitive.FloatArray2D;
import io.github.heathensoft.jlib.common.storage.primitive.IntArray2D;
import io.github.heathensoft.jlib.common.storage.primitive.ShortArray2D;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_R32F;
import static org.lwjgl.opengl.GL31.*;

/**
 * Pain to write :D
 * @author Frederik Dahl
 * 30/10/2022
 */


public class Texture implements Disposable {
    
    private static int active_slot;
    
    public static int activeSlot() {
        return active_slot;
    }
    
    public static void unBind(int target) {
        glBindTexture(target,0);
    }
    
    private int id;
    private int target;
    private int width = 0;
    private int height = 0;
    private int depth = 0;
    
    
    public Texture(int target) {
        this.target = target;
        id = glGenTextures();
    }
    
    public void bindToActiveSlot() {
        glBindTexture(target, id);
    }
    
    public void bindToSlot(int slot) {
        if (slot != active_slot) {
            glActiveTexture(slot + GL_TEXTURE0);
            active_slot = slot;
        } bindToActiveSlot();
    }
    
    @Override
    public void dispose() {
        if (!isDisposed()) {
            glDeleteTextures(id);
            id = GL_NONE;
            target = GL_NONE;
            width = 0;
            height = 0;
            depth = 0;
        }
    }
    
    

/*
 OpenGL image formats along with their un-sized variants and preferred formats for pixel transfer (Written by hand,
 needs verification) Pixel store for compressed textures not provided because there are glCompressedTexImage and family for them.
 EXT_texture_compression_s3tc formats not included.
 
|          Image format (sized)         |      Unsized       | Compr |    Pixel format    |             Pixel type            |
|---------------------------------------|--------------------|-------|--------------------|-----------------------------------|
| GL_R8                                 | GL_RED             | False | GL_RED             | GL_UNSIGNED_BYTE                  |
| GL_R8_SNORM                           | GL_RED             | False | GL_RED             | GL_BYTE                           |
| GL_R16                                | GL_RED             | False | GL_RED             | GL_UNSIGNED_SHORT                 |
| GL_R16_SNORM                          | GL_RED             | False | GL_RED             | GL_SHORT                          |
| GL_R32F                               | GL_RED             | False | GL_RED             | GL_FLOAT                          |
| GL_R8I                                | GL_RED             | False | GL_RED_INTEGER     | GL_INT                            |
| GL_R8UI                               | GL_RED             | False | GL_RED_INTEGER     | GL_UNSIGNED_INT                   |
| GL_R16I                               | GL_RED             | False | GL_RED_INTEGER     | GL_INT                            |
| GL_R16UI                              | GL_RED             | False | GL_RED_INTEGER     | GL_UNSIGNED_INT                   |
| GL_R32I                               | GL_RED             | False | GL_RED_INTEGER     | GL_INT                            |
| GL_R32UI                              | GL_RED             | False | GL_RED_INTEGER     | GL_UNSIGNED_INT                   |
| GL_R16F                               | GL_RED             | False | GL_RED             | GL_HALF_FLOAT                     |
| GL_RG8                                | GL_RG              | False | GL_RG              | GL_UNSIGNED_BYTE                  |
| GL_RG8_SNORM                          | GL_RG              | False | GL_RG              | GL_BYTE                           |
| GL_RG16                               | GL_RG              | False | GL_RG              | GL_UNSIGNED_SHORT                 |
| GL_RG16_SNORM                         | GL_RG              | False | GL_RG              | GL_SHORT                          |
| GL_RG16F                              | GL_RG              | False | GL_RG              | GL_HALF_FLOAT                     |
| GL_RG32F                              | GL_RG              | False | GL_RG              | GL_FLOAT                          |
| GL_RG8I                               | GL_RG              | False | GL_RG_INTEGER      | GL_INT                            |
| GL_RG8UI                              | GL_RG              | False | GL_RG_INTEGER      | GL_UNSIGNED_INT                   |
| GL_RG16I                              | GL_RG              | False | GL_RG_INTEGER      | GL_INT                            |
| GL_RG16UI                             | GL_RG              | False | GL_RG_INTEGER      | GL_UNSIGNED_INT                   |
| GL_RG32I                              | GL_RG              | False | GL_RG_INTEGER      | GL_INT                            |
| GL_RG32UI                             | GL_RG              | False | GL_RG_INTEGER      | GL_UNSIGNED_INT                   |
| GL_R3_G3_B2                           | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_BYTE_3_3_2            |
| GL_RGB4                               | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_BYTE                  |
| GL_RGB5                               | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_BYTE                  |
| GL_RGB8                               | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_BYTE                  |
| GL_RGB8_SNORM                         | GL_RGB             | False | GL_RGB             | GL_BYTE                           |
| GL_RGB10                              | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_SHORT                 |
| GL_RGB12                              | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_SHORT                 |
| GL_RGB16_SNORM                        | GL_RGB             | False | GL_RGB             | GL_SHORT                          |
| GL_RGBA2                              | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_SHORT_4_4_4_4         |
| GL_RGBA4                              | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_SHORT_4_4_4_4         |
| GL_SRGB8                              | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_BYTE                  |
| GL_RGB16F                             | GL_RGB             | False | GL_RGB             | GL_HALF_FLOAT                     |
| GL_RGB32F                             | GL_RGB             | False | GL_RGB             | GL_FLOAT                          |
| GL_R11F_G11F_B10F                     | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_INT_10F_11F_11F_REV   |
| GL_RGB9_E5                            | GL_RGB             | False | GL_RGB             | GL_UNSIGNED_INT_5_9_9_9_REV       |
| GL_RGB8I                              | GL_RGB             | False | GL_RGB_INTEGER     | GL_INT                            |
| GL_RGB8UI                             | GL_RGB             | False | GL_RGB_INTEGER     | GL_UNSIGNED_INT                   |
| GL_RGB16I                             | GL_RGB             | False | GL_RGB_INTEGER     | GL_INT                            |
| GL_RGB16UI                            | GL_RGB             | False | GL_RGB_INTEGER     | GL_UNSIGNED_INT                   |
| GL_RGB32I                             | GL_RGB             | False | GL_RGB_INTEGER     | GL_INT                            |
| GL_RGB32UI                            | GL_RGB             | False | GL_RGB_INTEGER     | GL_UNSIGNED_INT                   |
| GL_RGB5_A1                            | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_SHORT_5_5_5_1         |
| GL_RGBA8                              | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_BYTE                  |
| GL_RGBA8_SNORM                        | GL_RGBA            | False | GL_RGBA            | GL_BYTE                           |
| GL_RGB10_A2                           | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_INT_10_10_10_2        |
| GL_RGB10_A2UI                         | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_UNSIGNED_INT_10_10_10_2        |
| GL_RGBA12                             | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_SHORT                 |
| GL_RGBA16                             | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_SHORT                 |
| GL_SRGB8_ALPHA8                       | GL_RGBA            | False | GL_RGBA            | GL_UNSIGNED_BYTE                  |
| GL_RGBA16F                            | GL_RGBA            | False | GL_RGBA            | GL_HALF_FLOAT                     |
| GL_RGBA32F                            | GL_RGBA            | False | GL_RGBA            | GL_FLOAT                          |
| GL_RGBA8I                             | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_INT                            |
| GL_RGBA8UI                            | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_UNSIGNED_INT                   |
| GL_RGBA16I                            | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_INT                            |
| GL_RGBA16UI                           | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_UNSIGNED_INT                   |
| GL_RGBA32I                            | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_INT                            |
| GL_RGBA32UI                           | GL_RGBA            | False | GL_RGBA_INTEGER    | GL_UNSIGNED_INT                   |
| GL_DEPTH_COMPONENT16                  | GL_DEPTH_COMPONENT | False | GL_DEPTH_COMPONENT | GL_UNSIGNED_SHORT                 |
| GL_DEPTH_COMPONENT24                  | GL_DEPTH_COMPONENT | False | GL_DEPTH_COMPONENT | GL_UNSIGNED_INT                   |
| GL_DEPTH_COMPONENT32                  | GL_DEPTH_COMPONENT | False | GL_DEPTH_COMPONENT | GL_UNSIGNED_INT                   |
| GL_DEPTH_COMPONENT32F                 | GL_DEPTH_COMPONENT | False | GL_DEPTH_COMPONENT | GL_FLOAT                          |
| GL_DEPTH24_STENCIL8                   | GL_DEPTH_STENCIL   | False | GL_DEPTH_STENCIL   | GL_UNSIGNED_INT_24_8              |
| GL_DEPTH32F_STENCIL8                  | GL_DEPTH_STENCIL   | False | GL_DEPTH_STENCIL   | GL_FLOAT_32_UNSIGNED_INT_24_8_REV |
| GL_COMPRESSED_RED                     | GL_RED             | True  | -                  | -                                 |
| GL_COMPRESSED_RED_RGTC1               | GL_RED             | True  | -                  | -                                 |
| GL_COMPRESSED_SIGNED_RED_RGTC1        | GL_RED             | True  | -                  | -                                 |
| GL_COMPRESSED_RG                      | GL_RG              | True  | -                  | -                                 |
| GL_COMPRESSED_RG_RGTC2                | GL_RG              | True  | -                  | -                                 |
| GL_COMPRESSED_SIGNED_RG_RGTC2         | GL_RG              | True  | -                  | -                                 |
| GL_COMPRESSED_RGB                     | GL_RGB             | True  | -                  | -                                 |
| GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT   | GL_RGB             | True  | -                  | -                                 |
| GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT | GL_RGB             | True  | -                  | -                                 |
| GL_COMPRESSED_SRGB                    | GL_RGB             | True  | -                  | -                                 |
| GL_COMPRESSED_RGBA                    | GL_RGBA            | True  | -                  | -                                 |
| GL_COMPRESSED_RGBA_BPTC_UNORM         | GL_RGBA            | True  | -                  | -                                 |
| GL_COMPRESSED_SRGB_ALPHA              | GL_RGBA            | True  | -                  | -                                 |
| GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM   | GL_RGBA            | True  | -                  | -                                 |
 */
    
    
    
    
    public void image_2D(Image image) throws Exception {
        this.width = image.width();
        this.height = image.height();
        this.depth = 1;
        int i_format; // GPU side
        int format; // client memory
        int stride = 4;
        switch (image.components()) {
            case 1 -> {
                format = GL_RED;
                i_format = GL_R8;
                stride = 1;
            }case 2 -> {
                format = GL_RG;
                i_format = GL_RG8;
                stride = 2;
            }case 3 -> {
                i_format = format = GL_RGB;
                if ((width & 3) != 0) {
                    stride = 2 - (width & 1);}
            }case 4 -> {
                format = GL_RGBA;
                i_format = GL_RGBA8;
            } default -> throw new Exception("unsupported format");
        } glPixelStorei(GL_UNPACK_ALIGNMENT,stride);
        glTexImage2D(target, 0, i_format, width, height,
                0, format, GL_UNSIGNED_BYTE, image.data());
        Disposable.dispose(image);
    }
    
    
    // ---------------------------------R8 Unsigned Normalized
    
    public void R8_2D(byte[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ByteBuffer buffer = MemoryUtil.memAlloc(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glTexImage2D(target, 0, GL_R8, width, height,
        0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R8_2D(ByteArray2D data) {
        R8_2D(data.get());
    }
    
    public void R8_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glTexImage2D(target, 0, GL_R8, w, h,
        0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R8_2D(ByteBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glTexImage2D(target, 0, GL_R8, w, h,
        0, GL_RED, GL_UNSIGNED_BYTE, data);
        GLError.check();
    }
    
    // ---------------------------------R8 Signed Normalized
    
    
    
    public void R8_SNORM_2D(ByteArray2D byteArray) {
        R8_SNORM_2D(byteArray.get());
    }
    
    public void R8_SNORM_2D(byte[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ByteBuffer buffer = MemoryUtil.memAlloc(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glTexImage2D(target, 0, GL_R8_SNORM, width, height,
        0, GL_RED, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R8_SNORM_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glTexImage2D(target, 0, GL_R8_SNORM, w, h,
        0, GL_RED, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R8_SNORM_2D(ByteBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);
        glTexImage2D(target, 0, GL_R8_SNORM, w, h,
        0, GL_RED, GL_BYTE, data);
        GLError.check();
    }
    
    // ---------------------------------R16 Unsigned Normalized
    
    
    
    public void R16_2D(ShortArray2D data) {
        R16_2D(data.get());
    }
    
    public void R16_2D(short[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ShortBuffer buffer = MemoryUtil.memAllocShort(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_R16, width, height,
        0, GL_RED, GL_UNSIGNED_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
        
    }
    
    public void R16_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        ShortBuffer buffer = MemoryUtil.memAllocShort(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_R16, w, h,
        0, GL_RED, GL_UNSIGNED_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R16_2D(ShortBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_R16, w, h,
        0, GL_RED, GL_UNSIGNED_SHORT, data);
        GLError.check();
    }
    
    
    
    // ---------------------------------R16 Signed Normalized
    
    
    
    public void R16_SNORM_2D(ShortArray2D data) {
        R16_2D(data.get());
    }
    
    public void R16_SNORM_2D(short[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ShortBuffer buffer = MemoryUtil.memAllocShort(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_R16_SNORM, width, height,
        0, GL_RED, GL_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R16_SNORM_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        ShortBuffer buffer = MemoryUtil.memAllocShort(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_R16_SNORM, width, height,
        0, GL_RED, GL_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R16_SNORM_2D(ShortBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_R16_SNORM, width, height,
        0, GL_RED, GL_SHORT, data);
        GLError.check();
    }
    
    
    // ---------------------------------R32F Float
    
    
    
    public void R32F_2D(FloatArray2D data) {
        R32F_2D(data.get());
    }
    
    public void R32F_2D(float[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        FloatBuffer buffer = MemoryUtil.memAllocFloat(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32F, width, height,
        0, GL_RED, GL_FLOAT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R32F_2D(float[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32F, w, h,
        0, GL_RED, GL_FLOAT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R32F_2D(FloatBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32F, w, h,
        0, GL_RED, GL_FLOAT, data);
        GLError.check();
    }
    
    
    // ---------------------------------R32I Signed Integer
    
    
    
    public void R32I_2D(IntArray2D data) {
        R32I_2D(data.get());
    }
    
    public void R32I_2D(int[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        IntBuffer buffer = MemoryUtil.memAllocInt(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32I, width, height,
        0, GL_RED_INTEGER, GL_INT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R32I_2D(int[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32I, w, h,
                0, GL_RED_INTEGER, GL_INT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R32I_2D(IntBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32I, w, h,
                0, GL_RED_INTEGER, GL_INT, data);
        GLError.check();
    }
    
    
    // ---------------------------------R32UI Unsigned Integer
    
    
    public void R32UI_2D(IntArray2D data) {
        R32I_2D(data.get());
    }
    
    public void R32UI_2D(int[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        IntBuffer buffer = MemoryUtil.memAllocInt(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32I, width, height,
        0, GL_RED_INTEGER, GL_UNSIGNED_INT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R32UI_2D(int[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32I, width, height,
        0, GL_RED_INTEGER, GL_UNSIGNED_INT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void R32UI_2D(IntBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_R32I, width, height,
        0, GL_RED_INTEGER, GL_UNSIGNED_INT, data);
        MemoryUtil.memFree(data);
        GLError.check("specify R32UI");
    }
    
    
    // ---------------------------------RG8 Unsigned Normalized
    
    
    
    public void RG8_2D(ShortArray2D data) {
        RG8_2D(data.get());
    }
    
    public void RG8_2D(short[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ShortBuffer buffer = MemoryUtil.memAllocShort(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8, width, height,
        0, GL_RG, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG8_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        ShortBuffer buffer = MemoryUtil.memAllocShort(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8, w, h,
        0, GL_RG, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG8_2D(ShortBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8, w, h,
        0, GL_RG, GL_UNSIGNED_BYTE, data);
        GLError.check();
    }
    
    public void RG8_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int length = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(length);
        for (int i = 0; i < length; i += 2) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8, w, h,
        0, GL_RG, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG8_2D(ByteBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8, w, h,
        0, GL_RG, GL_UNSIGNED_BYTE, data);
        GLError.check();
    }
    
    
    
    // ---------------------------------RG8 Signed Normalized
    
    
    
    public void RG8_SNORM_2D(ShortArray2D data) {
        RG8_2D(data.get());
    }
    
    public void RG8_SNORM_2D(short[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ShortBuffer buffer = MemoryUtil.memAllocShort(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8_SNORM, width, height,
        0, GL_RG, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG8_SNORM_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        ShortBuffer buffer = MemoryUtil.memAllocShort(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8_SNORM, width, height,
        0, GL_RG, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG8_SNORM_2D(ShortBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8_SNORM, width, height,
        0, GL_RG, GL_BYTE, data);
        GLError.check();
    }
    
    public void RG8_SNORM_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int length = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(length);
        for (int i = 0; i < length; i += 2) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8_SNORM, width, height,
        0, GL_RG, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG8_SNORM_2D(ByteBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RG8_SNORM, width, height,
        0, GL_RG, GL_BYTE, data);
        GLError.check();
    }
    
    
    
    // ---------------------------------RG16 Unsigned Normalized
    
    
    
    
    public void RG16_2D(IntArray2D data) {
        RG16_2D(data.get());
    }
    
    public void RG16_2D(int[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        IntBuffer buffer = MemoryUtil.memAllocInt(width * height);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, width, height,
        0, GL_RG, GL_UNSIGNED_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_2D(int[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, w, h,
        0, GL_RG, GL_UNSIGNED_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_2D(IntBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, w, h,
        0, GL_RG, GL_UNSIGNED_SHORT, data);
        GLError.check();
    }
    
    
    public void RG16_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ShortBuffer buffer = MemoryUtil.memAllocShort(l);
        for (int i = 0; i < l; i += 2) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, w, h,
        0, GL_RG, GL_UNSIGNED_SHORT, data);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_2D(ShortBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, w, h,
        0, GL_RG, GL_UNSIGNED_SHORT, buffer);
        GLError.check();
    }
    
    public void RG16_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(l);
        for (int i = 0; i < l; i += 4) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
            buffer.put(data[i + 2]);
            buffer.put(data[i + 3]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, w, h,
        0, GL_RG, GL_UNSIGNED_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_2D(ByteBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16, w, h,
        0, GL_RG, GL_UNSIGNED_SHORT, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RG16 Signed Normalized
    
    
    
    public void RG16_SNORM_2D(IntArray2D data) {
        RG16_SNORM_2D(data.get());
    }
    
    public void RG16_SNORM_2D(int[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        IntBuffer buffer = MemoryUtil.memAllocInt(width * height);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, width, height,
        0, GL_RG, GL_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_SNORM_2D(int[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++)
                buffer.put(data[c + (h * r)]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, w, h,
        0, GL_RG, GL_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_SNORM_2D(IntBuffer data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, w, h,
        0, GL_RG, GL_SHORT, data);
        GLError.check();
    }
    
    
    public void RG16_SNORM_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ShortBuffer buffer = MemoryUtil.memAllocShort(l);
        for (int i = 0; i < l; i += 2) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, w, h,
        0, GL_RG, GL_SHORT, data);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_SNORM_2D(ShortBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, w, h,
        0, GL_RG, GL_SHORT, buffer);
        GLError.check();
    }
    
    public void RG16_SNORM_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(l);
        for (int i = 0; i < l; i += 4) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
            buffer.put(data[i + 2]);
            buffer.put(data[i + 3]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, w, h,
        0, GL_RG, GL_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RG16_SNORM_2D(ByteBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RG16_SNORM, w, h,
        0, GL_RG, GL_SHORT, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RGB8 Unsigned Normalized
    
    
    
    public void RGB8_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(l);
        for (int i = 0; i < l; i += 3) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
            buffer.put(data[i + 2]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB8, w, h,
        0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGB8_2D(ByteBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB8, w, h,
        0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RGB8 Signed Normalized
    
    
    public void RGB8_SNORM_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(l);
        for (int i = 0; i < l; i += 3) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
            buffer.put(data[i + 2]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB8_SNORM, w, h,
        0, GL_RGB, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGB8_SNORM_2D(ByteBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB8_SNORM, w, h,
        0, GL_RGB, GL_BYTE, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RGB16 Unsigned Normalized
    
    
    
    public void RGB16_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ShortBuffer buffer = MemoryUtil.memAllocShort(l);
        for (int i = 0; i < l; i += 3) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
            buffer.put(data[i + 2]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB16, w, h,
        0, GL_RGB, GL_UNSIGNED_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGB16_2D(ShortBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB16, w, h,
        0, GL_RGB, GL_UNSIGNED_SHORT, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RGB16 Signed Normalized
    
    
    
    public void RGB16_SNORM_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ShortBuffer buffer = MemoryUtil.memAllocShort(l);
        for (int i = 0; i < l; i += 3) {
            buffer.put(data[i]);
            buffer.put(data[i + 1]);
            buffer.put(data[i + 2]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB16_SNORM, w, h,
        0, GL_RGB, GL_SHORT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGB16_SNORM_2D(ShortBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,3);
        glTexImage2D(target, 0, GL_RGB16_SNORM, w, h,
        0, GL_RGB, GL_SHORT, buffer);
        GLError.check();
    }
    
    
    // ---------------------------------RGBA4 Unsigned Normalized
    // todo: include bytes
    
    
    
    public void RGBA4_2D(ShortArray2D data) {
        RGBA4_2D(data.get());
    }
    
    public void RGBA4_2D(short[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        ShortBuffer buffer = MemoryUtil.memAllocShort(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RGBA4, width, height,
        0, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA4_2D(short[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ShortBuffer buffer = MemoryUtil.memAllocShort(l);
        for (short datum : data) {
            buffer.put(datum);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RGBA4, w, h,
        0, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA4_2D(ShortBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_RGBA4, w, h,
        0, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RGB32F Float
    
    
    
    public void RGB32F_2D(FloatArray2D data) {
        RGB32F_2D(data.get());
    }
    
    public void RGB32F_2D(float[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        FloatBuffer buffer = MemoryUtil.memAllocFloat(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGB32F, width, height,
        0, GL_RGB, GL_FLOAT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGB32F_2D(float[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        FloatBuffer buffer = MemoryUtil.memAllocFloat(l);
        for (float datum : data) {
            buffer.put(datum);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGB32F, w, h,
        0, GL_RGB, GL_FLOAT, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGB32F_2D(FloatBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGB32F, w, h,
        0, GL_RGB, GL_FLOAT, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------RGBA8 Unsigned Normalized
    
    
    
    public void RGBA8_2D(IntArray2D data) {
        RGBA8_2D(data.get());
    }
    
    public void RGBA8_2D(int[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        IntBuffer buffer = MemoryUtil.memAllocInt(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        System.out.println(GL_NONE);
        System.out.println(target);
        glTexImage2D(target, 0, GL_RGBA8, width, height,
        0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check("specify rgba8");
    }
    
    public void RGBA8_2D(int[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        IntBuffer buffer = MemoryUtil.memAllocInt(l);
        for (int datum : data) {
            buffer.put(datum);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8, w, h,
        0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA8_2D(IntBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8, w, h,
        0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        GLError.check();
    }
    
    public void RGBA8_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(l);
        for (byte datum : data) {
            buffer.put(datum);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8, w, h,
        0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA8_2D(ByteBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8, w, h,
        0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        GLError.check("specify RGBA8");
    }
    
    
    
    // ---------------------------------RGBA8 Signed Normalized
    
    
    
    public void RGBA8_SNORM_2D(IntArray2D data) {
        RGBA8_SNORM_2D(data.get());
    }
    
    public void RGBA8_SNORM_2D(int[][] data) {
        width = data[0].length;
        height = data.length;
        depth = 1;
        int size = width * height;
        IntBuffer buffer = MemoryUtil.memAllocInt(size);
        for (int row = 0; row < height; row++) {
            buffer.put(data[row]);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8_SNORM, width, height,
        0, GL_RGBA, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA8_SNORM_2D(int[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        IntBuffer buffer = MemoryUtil.memAllocInt(l);
        for (int datum : data) {
            buffer.put(datum);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8_SNORM, width, height,
        0, GL_RGBA, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA8_SNORM_2D(IntBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8_SNORM, width, height,
        0, GL_RGBA, GL_BYTE, buffer);
        GLError.check();
    }
    
    public void RGBA8_SNORM_2D(byte[] data, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        int l = data.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(l);
        for (byte datum : data) {
            buffer.put(datum);
        } buffer.flip();
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8_SNORM, w, h,
        0, GL_RGBA, GL_BYTE, buffer);
        MemoryUtil.memFree(buffer);
        GLError.check();
    }
    
    public void RGBA8_SNORM_2D(ByteBuffer buffer, int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_RGBA8_SNORM, w, h,
        0, GL_RGBA, GL_BYTE, buffer);
        GLError.check();
    }
    
    
    
    // ---------------------------------DEPTH / STENCIL
    
    
    
    
    public void DEPTH16(int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,2);
        glTexImage2D(target, 0, GL_DEPTH_COMPONENT16, w, h,
        0, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, (ShortBuffer) null);
        GLError.check();
    }
    
    public void DEPTH32(int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_DEPTH_COMPONENT32, w, h,
        0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (IntBuffer) null);
        GLError.check();
    }
    
    public void DEPTH32F(int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_DEPTH_COMPONENT32F, w, h,
        0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
        GLError.check();
    }
    
    public void DEPTH24_STENCIL8(int w, int h) {
        width = w;
        height = h;
        depth = 1;
        glPixelStorei(GL_UNPACK_ALIGNMENT,4);
        glTexImage2D(target, 0, GL_DEPTH24_STENCIL8, w, h,
        0, GL_UNSIGNED_INT_24_8 , GL_FLOAT, (IntBuffer) null);
        GLError.check();
    }
    
    
    
    
    
    
    public void generateMipMap(float lodBias, float min, float max) {
        glTexParameterf(target,GL_TEXTURE_MIN_LOD,min);
        glTexParameterf(target,GL_TEXTURE_MAX_LOD,max);
        glTexParameterf(target,GL_TEXTURE_LOD_BIAS,lodBias);
    }
    
    public void generateMipMap(float lodBias) {
        generateMipMap(lodBias, -1000, 1000);
    }
    
    public void generateMipMap() {
        generateMipMap(0.0f);
    }
    
    public void filter(int filter) {
        filter(filter,filter);
    }
    
    public void filter(int min, int mag) {
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag);
    }
    
    public void nearest() {
        filter(GL_NEAREST);
    }
    
    public void linear() {
        filter(GL_LINEAR);
    }
    
    public void wrapS(int wrapS) {
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
    }
    
    public void wrapT(int wrapT) {
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
    }
    
    public void wrapR(int wrapR) {
        glTexParameteri(target, GL_TEXTURE_WRAP_R, wrapR);
    }
    
    public void wrapST(int wrapS, int wrapT) {
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
    }
    
    public void wrapSTR(int wrapS, int wrapT, int wrapR) {
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
        glTexParameteri(target, GL_TEXTURE_WRAP_R, wrapR);
    }
    
    public void wrapST(int wrap) {
        wrapST(wrap,wrap);
    }
    
    public void wrapSTR(int wrap) {
        wrapSTR(wrap,wrap,wrap);
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public int depth() {
        return depth;
    }
    
    public int id() {
        return id;
    }
    
    public int target() {
        return target;
    }
    
    public boolean isDisposed() {
        return id == GL_NONE;
    }
    
    public boolean isSpecified() {
        return target != GL_NONE;
    }
    
    
}
