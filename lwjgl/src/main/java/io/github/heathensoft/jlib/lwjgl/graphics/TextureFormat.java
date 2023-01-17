package io.github.heathensoft.jlib.lwjgl.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

/**
 * @author Frederik Dahl
 * 12/01/2023
 */


public enum TextureFormat {

    /* Details below */

    INVALID(0,0,0,0,0,false),
    STENCIL8(GL_STENCIL_INDEX8,GL_STENCIL_INDEX,GL_UNSIGNED_BYTE,0,1,false), // OpenGL 4.4
    DEPTH16(GL_DEPTH_COMPONENT16,GL_DEPTH_COMPONENT,GL_UNSIGNED_SHORT,1,2,false),
    DEPTH32(GL_DEPTH_COMPONENT32,GL_DEPTH_COMPONENT,GL_UNSIGNED_INT,1,4,false),
    DEPTH32F(GL_DEPTH_COMPONENT32F,GL_DEPTH_COMPONENT,GL_FLOAT,1,4,false),
    DEPTH24_STENCIL8(GL_DEPTH24_STENCIL8,GL_DEPTH_STENCIL,GL_UNSIGNED_INT_24_8,2,4,false),
    R32_FLOAT(GL_R32F,GL_R,GL_FLOAT,1,4,true),
    RG32_FLOAT(GL_RG32F,GL_RG,GL_FLOAT,2,8,true),
    RGB32_FLOAT(GL_RGB32F,GL_RGB,GL_FLOAT,3,4,true),
    RGBA32_FLOAT(GL_RGBA32F,GL_RGBA,GL_FLOAT,4,8,true),
    R8_SIGNED_NORMALIZED(GL_R8_SNORM,GL_RED,GL_BYTE,1,1,true),
    R8_UNSIGNED_NORMALIZED(GL_R8,GL_RED,GL_UNSIGNED_BYTE,1,1,true),
    R16_SIGNED_NORMALIZED(GL_R16_SNORM,GL_RED,GL_SHORT,1,2,true),
    R16_UNSIGNED_NORMALIZED(GL_R16,GL_RED,GL_UNSIGNED_SHORT,1,2,true),
    R32_SIGNED_INTEGER(GL_R32I,GL_RED_INTEGER,GL_INT,1,4,true),
    R32_UNSIGNED_INTEGER(GL_R32UI,GL_RED_INTEGER,GL_UNSIGNED_INT,1,4,true),
    RG8_SIGNED_NORMALIZED(GL_RG8_SNORM,GL_RG,GL_BYTE,2,2,true),
    RG8_UNSIGNED_NORMALIZED(GL_RG8,GL_RG,GL_UNSIGNED_BYTE,2,2,true),
    RG16_SIGNED_NORMALIZED(GL_RG16_SNORM,GL_RG,GL_SHORT,2,4,true),
    RG16_UNSIGNED_NORMALIZED(GL_RG16,GL_RG,GL_UNSIGNED_SHORT,2,4,true),
    RGB8_SIGNED_NORMALIZED(GL_RGB8_SNORM,GL_RGB,GL_BYTE,3,1,true),
    RGB8_UNSIGNED_NORMALIZED(GL_RGB8,GL_RGB,GL_UNSIGNED_BYTE,3,1,true),
    RGB16_SIGNED_NORMALIZED(GL_RGB16_SNORM,GL_RGB,GL_SHORT,3,2,true),
    RGB16_UNSIGNED_NORMALIZED(GL_RGB16,GL_RGB,GL_UNSIGNED_SHORT,3,2,true),
    RGBA4_UNSIGNED_NORMALIZED(GL_RGBA4,GL_RGBA,GL_UNSIGNED_SHORT_4_4_4_4,4,2,true),
    RGBA8_SIGNED_NORMALIZED(GL_RGBA8_SNORM,GL_RGBA,GL_BYTE,4,4,true),
    RGBA8_UNSIGNED_NORMALIZED(GL_RGBA8,GL_RGBA,GL_UNSIGNED_BYTE,4,4,true);

    public final int sized_format;
    public final int pixel_format;
    public final int pixel_data_type;
    public final int channels;
    public final int pack_alignment;
    public final boolean is_color_format;

    TextureFormat(int sized_format, int pixel_format, int pixel_data_type, int channels, int pack_alignment, boolean is_color_format) {
        this.sized_format = sized_format;
        this.pixel_format = pixel_format;
        this.pixel_data_type = pixel_data_type;
        this.channels = channels;
        this.pack_alignment = pack_alignment;
        this.is_color_format = is_color_format;
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
}
