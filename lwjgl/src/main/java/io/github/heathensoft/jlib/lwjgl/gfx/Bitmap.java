package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 *
 * 4 channels: | a | b | g | r |
 * 3 channels: | b | g | r |
 * 2 channels: | g | r |
 * 1 channels: | r |
 *
 * @author Frederik Dahl
 * 03/10/2023
 */


public class Bitmap implements Disposable {

    private final ByteBuffer pixels;
    private final int width;
    private final int height;
    private final int channels;

    // todo: support drawing from larger format to a smaller


    public Bitmap(int width, int height, int channels) {
        this.pixels = MemoryUtil.memCalloc(width * height * channels);
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public Bitmap(ByteBuffer pixels, int width, int height, int channels) {
        if (!pixels.isDirect()) {
            int size = width * height * channels;
            this.pixels = MemoryUtil.memAlloc(size);
            for (int i = 0; i < size; i++)
                this.pixels.put(i,pixels.get(i));
        } else this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public Bitmap(ByteBuffer png, boolean vFlip) throws Exception {
        if (png == null) throw new Exception("null argument png");
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            if (!stbi_info_from_memory(png, w, h, c))
                throw new Exception("unable to read image info: " + stbi_failure_reason());
            stbi_set_flip_vertically_on_load(vFlip);
            this.pixels = stbi_load_from_memory(png, w, h, c, 0);
            if (pixels == null) throw new Exception("unable to load image: " + stbi_failure_reason());
            this.width = w.get(0);
            this.height = h.get(0);
            this.channels = c.get(0);
        }
    }

    public Bitmap(float[][] heightmap) {
        this.width = heightmap[0].length;
        this.height = heightmap.length;
        this.channels = 1;
        this.pixels = MemoryUtil.memAlloc(sizeOf());
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                float f = clamp(heightmap[r][c]);
                pixels.put(r * width + c,(byte) (round(f*255f) & 0xFF));
            }
        }
    }

    public Bitmap(int[][] abgr) {
        width = abgr[0].length;
        height = abgr.length;
        channels = 4;
        pixels = MemoryUtil.memAlloc(sizeOf());
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int idx = (r * width + c) * channels;
                pixels.putInt(idx,abgr[r][c]);
            }
        }
    }

    public Bitmap(byte[][] red) {
        width = red[0].length;
        height = red.length;
        channels = 1;
        pixels = MemoryUtil.memAlloc(sizeOf());
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int idx = (r * width + c);
                pixels.put(idx,red[r][c]);
            }
        }
    }

    public Bitmap(ByteBuffer png) throws Exception { this(png,false); }

    public ByteBuffer pixels() { return pixels; }

    public int channels() { return channels; }

    public int stride() { return width * channels; }

    public int sizeOf() { return width * height * channels; }

    public int surfaceArea() { return width * height; }

    public int width() { return width; }

    public int height() { return height; }

    /** src channels must <= dst channels.
     * if src == 2 channel and dst == 4, we assume the src is 16-bit grayscale. where the red channel is value and green channel is alpha */
    public void drawNearest(Bitmap source, float x, float y, float w, float h, float u1, float v1, float u2, float v2) {
        float x2 = x + w;
        float y2 = y + h;
        int ix1 = Math.max(floor(x),0);
        int iy1 = Math.max(floor(y),0);
        @SuppressWarnings("SuspiciousNameCombination")
        int ix2 = Math.min(ceil(x2),width);
        int iy2 = Math.min(ceil(y2),height);
        if (source.channels == 4 && channels == 4) {
            for (int r = iy1; r < iy2; r++) {
                float v = remap(r,y,y2,v1,v2);
                for (int c = ix1; c < ix2; c++) {
                    float u = remap(c,x,x2,u1,u2);
                    int dst = getPixelUnchecked(c,r);
                    int src = source.sampleNearest(u,v);
                    setPixelUnchecked(c,r,alphaBlend(src,dst));
                }
            }
        } else if (source.channels == channels) {
            for (int r = iy1; r < iy2; r++) {
                float v = remap(r,y,y2,v1,v2);
                for (int c = ix1; c < ix2; c++) {
                    float u = remap(c,x,x2,u1,u2);
                    int src = source.sampleNearest(u,v);
                    setPixelUnchecked(c,r,src);
                }
            }
        } else if (source.channels < channels) {
            if (channels == 4) {
                if (source.channels == 2) {
                    for (int r = iy1; r < iy2; r++) {
                        float v = remap(r,y,y2,v1,v2);
                        for (int c = ix1; c < ix2; c++) {
                            float u = remap(c,x,x2,u1,u2);
                            int dst = getPixelUnchecked(c,r);
                            int src = source.sampleNearest(u,v);
                            int src_red = Color.rBits(src);
                            int src_gre = Color.gBits(src);
                            src = Color.rgb_to_intBits(src_red,src_red,src_red,src_gre);
                            setPixelUnchecked(c,r,alphaBlend(src,dst));
                        }
                    }
                } else {
                    for (int r = iy1; r < iy2; r++) {
                        float v = remap(r,y,y2,v1,v2);
                        for (int c = ix1; c < ix2; c++) {
                            float u = remap(c,x,x2,u1,u2);
                            int dst = getPixelUnchecked(c,r);
                            int src = source.sampleNearest(u,v) | 0xFF000000;
                            setPixelUnchecked(c,r,alphaBlend(src,dst));
                        }
                    }
                }
            } else {
                for (int r = iy1; r < iy2; r++) {
                    float v = remap(r,y,y2,v1,v2);
                    for (int c = ix1; c < ix2; c++) {
                        float u = remap(c,x,x2,u1,u2);
                        int src = source.sampleNearest(u,v);
                        setPixelUnchecked(c,r,src);
                    }
                }
            }
        }
    }

    /** src channels must <= dst channels.
     * if src == 2 channel and dst == 4, we assume the src is 16-bit grayscale. where the red channel is value and green channel is alpha */
    public void drawLinear(Bitmap source, float x, float y, float w, float h, float u1, float v1, float u2, float v2) {
        float x2 = x + w;
        float y2 = y + h;
        int ix1 = Math.max(floor(x),0);
        int iy1 = Math.max(floor(y),0);
        @SuppressWarnings("SuspiciousNameCombination")
        int ix2 = Math.min(ceil(x2),width);
        int iy2 = Math.min(ceil(y2),height);
        if (source.channels == 4 && channels == 4) {
            for (int r = iy1; r < iy2; r++) {
                float v = remap(r,y,y2,v1,v2);
                for (int c = ix1; c < ix2; c++) {
                    float u = remap(c,x,x2,u1,u2);
                    int dst = getPixelUnchecked(c,r);
                    int src = source.sampleLinear(u,v);
                    setPixelUnchecked(c,r,alphaBlend(src,dst));
                }
            }
        } else if (source.channels == channels) {
            for (int r = iy1; r < iy2; r++) {
                float v = remap(r,y,y2,v1,v2);
                for (int c = ix1; c < ix2; c++) {
                    float u = remap(c,x,x2,u1,u2);
                    int src = source.sampleLinear(u,v);
                    setPixelUnchecked(c,r,src);
                }
            }
        } else if (source.channels < channels) {
            if (channels == 4) {
                if (source.channels == 2) {
                    for (int r = iy1; r < iy2; r++) {
                        float v = remap(r,y,y2,v1,v2);
                        for (int c = ix1; c < ix2; c++) {
                            float u = remap(c,x,x2,u1,u2);
                            int dst = getPixelUnchecked(c,r);
                            int src = source.sampleLinear(u,v);
                            int src_red = Color.rBits(src);
                            int src_gre = Color.gBits(src);
                            src = Color.rgb_to_intBits(src_red,src_red,src_red,src_gre);
                            setPixelUnchecked(c,r,alphaBlend(src,dst));
                        }
                    }
                } else {
                    for (int r = iy1; r < iy2; r++) {
                        float v = remap(r,y,y2,v1,v2);
                        for (int c = ix1; c < ix2; c++) {
                            float u = remap(c,x,x2,u1,u2);
                            int dst = getPixelUnchecked(c,r);
                            int src = source.sampleLinear(u,v) | 0xFF000000;
                            setPixelUnchecked(c,r,alphaBlend(src,dst));
                        }
                    }
                }
            } else {
                for (int r = iy1; r < iy2; r++) {
                    float v = remap(r,y,y2,v1,v2);
                    for (int c = ix1; c < ix2; c++) {
                        float u = remap(c,x,x2,u1,u2);
                        int src = source.sampleLinear(u,v);
                        setPixelUnchecked(c,r,src);
                    }
                }
            }

        }
    }

    /** src channels must == dst channels */
    public void drawNearest(Bitmap source, TextureRegion region, float x, float y, float w, float h) {
        drawNearest(source,x,y,w,h,region.u(),region.v(),region.u2(),region.v2());
    }
    /** src channels must == dst channels */
    public void drawLinear(Bitmap source, TextureRegion region, float x, float y, float w, float h) {
        drawLinear(source,x,y,w,h,region.u(),region.v(),region.u2(),region.v2());
    }
    /** src channels must == dst channels */
    public void drawNearest(Bitmap source, float x, float y, float w, float h) {
        drawNearest(source,x,y,w,h,0,0,1,1);
    }
    /** src channels must == dst channels */
    public void drawLinear(Bitmap source, float x, float y, float w, float h) {
        drawLinear(source,x,y,w,h,0,0,1,1);
    }

    public void drawPixel(int x, int y, int value) {
        if (boundsCheck(x,y)) drawPixelUnchecked(x,y,value);
    }

    public void drawPixelUnchecked(int x, int y, int value) {
        if (channels == 4) {
            value = alphaBlend(value,getPixelUnchecked(x,y));
        } setPixelUnchecked(x,y,value);
    }

    public void setPixel(int x, int y, int value) {
        if (boundsCheck(x,y)) setPixelUnchecked(x,y,value);
    }

    public void setPixelUnchecked(int x, int y, int value) {
        int idx = (y * width + x) * channels;
        if (channels == 4) pixels.putInt(idx,value);
        else { for (int i = 0; i < channels; i++) {
            pixels.put(idx + i, (byte) ((value >> (i * 8)) & 0xFF)); }
        }
    }

    public int getPixel(int x, int y) {
        return getPixelUnchecked(clampToEdgeX(x), clampToEdgeY(y));
    }

    public int getPixelUnchecked(int x, int y) {
        int idx = (y * width + x) * channels;
        if (channels == 4) {
            return pixels.getInt(idx);
        } int return_value = 0;
        for (int i = 0; i < channels; i++) {
            return_value |= (pixels.get(idx + i) & 0xFF) << (i * 8);
        } return return_value;
    }

    public int getColorComponent(int x, int y, int channel) {
        int idx = ((clampToEdgeY(y) * width + clampToEdgeX(x)) * channels + channel);
        return pixels.get(idx) & 0xFF;
    }

    public int sampleNearest(float u, float v) {
        int return_value = 0;
        int x = clampToEdgeX((int) (u * width));
        int y = clampToEdgeY((int) (v * height));
        int idx = (y * width + x) * channels;
        for (int i = 0; i < channels; i++) {
            return_value |= (pixels.get(idx + i) & 0xFF) << (i * 8);
        } return return_value;
    }

    public int sampleLinear(float u, float v) {
        int return_value = 0;
        float px = (u * width  - 0.5f); // half-pixel offset
        float py = (v * height - 0.5f); // half-pixel offset
        int floorX = floor(px); float fractX = px - floorX;
        int floorY = floor(py); float fractY = py - floorY;
        for (int i = 0; i < channels; i++) {
            int bl = getColorComponent(floorX, floorY,i);
            int br = getColorComponent(floorX + 1,floorY + 0,i);
            int tl = getColorComponent(floorX + 0,floorY + 1,i);
            int tr = getColorComponent(floorX + 1,floorY + 1,i);
            float tx = lerp(tl,tr,fractX);
            float bx = lerp(bl,br,fractX);
            int c =  round(lerp(bx,tx,fractY));
            return_value |= ((c & 0xFF) << (i * 8));
        } return return_value;
    }

    public void clear(int value) {
        byte[] rgba = new byte[channels];
        for (int c = 0; c < channels; c++) {
            rgba[c] = (byte) ((value >> (8 * c)) & 0xFF);
        } for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * channels;
                for (int c = 0; c < channels; c++) {
                    pixels.put(i + c, rgba[c]);
                }
            }
        }
    }

    /**  (4 channels only) When using the bitmap as source for drawing ops,
     * it's useful to premultiply before alpha blending */
    public void premultiplyAlpha() {
        if (channels == 4) {
            final ByteBuffer b = pixels;
            int stride = width * 4;
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    int i = r * stride + c * 4;
                    float alpha = (b.get(i + 3) & 0xFF) / 255.0f;
                    b.put(i, (byte) Math.round(((b.get(i) & 0xFF) * alpha)));
                    b.put(i + 1, (byte)round(((b.get(i + 1) & 0xFF) * alpha)));
                    b.put(i + 2, (byte)round(((b.get(i + 2) & 0xFF) * alpha)));
                }
            }
        }
    }

    public void unMultiplyAlpha() {
        if (channels == 4) {
            final ByteBuffer b = pixels;
            int stride = width * 4;
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    int i = r * stride + c * 4;
                    float alpha_inv = 1f / (b.get(i + 3) & 0xFF) / 255.0f;
                    b.put(i, (byte) round(((b.get(i) & 0xFF) * alpha_inv)));
                    b.put(i + 1, (byte)round(((b.get(i + 1) & 0xFF) * alpha_inv)));
                    b.put(i + 2, (byte)round(((b.get(i + 2) & 0xFF) * alpha_inv)));
                }
            }
        }
    }

    public void linearToSrgb() {
        float inv255 = 1 / 255f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = (y * width + x) * channels;
                float fSRGB = sqrt((pixels.get(idx) & 0xFF) * inv255);
                int value = ((int)(fSRGB * 255f) & 0xFF);
                pixels.put(idx,(byte) value);
                if (channels > 1) {
                    fSRGB = sqrt((pixels.get(idx + 1) & 0xFF)  * inv255);
                    value = ((int)(fSRGB * 255f) & 0xFF);
                    pixels.put(idx + 1,(byte) value);
                    if (channels > 2) {
                        fSRGB = sqrt((pixels.get(idx + 2) & 0xFF)  * inv255);
                        value = ((int)(fSRGB * 255f) & 0xFF);
                        pixels.put(idx + 2,(byte) value);
                    }
                }
            }
        }
    }

    public void srgbToLinear() {
        float inv255 = 1 / 255f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = (y * width + x) * channels;
                int iSRGB = (pixels.get(idx) & 0xFF);
                float fLinear = square(inv255 * iSRGB);
                int value = ((int) (fLinear * 255f) & 0xFF);
                pixels.put(idx,(byte) value);
                if (channels > 1) {
                    iSRGB = (pixels.get(idx + 1) & 0xFF) ;
                    fLinear = square(inv255 * iSRGB);
                    value = ((int) (fLinear * 255f) & 0xFF);
                    pixels.put(idx + 1,(byte) value);
                    if (channels > 2) {
                        iSRGB = (pixels.get(idx + 2) & 0xFF);
                        fLinear = square(inv255 * iSRGB);
                        value = ((int) (fLinear * 255f) & 0xFF);
                        pixels.put(idx + 2,(byte) value);
                    }
                }
            }
        }
    }

    /** write as png to disk */
    public void compressToDisk(String path) {
        stbi_flip_vertically_on_write(false);
        stbi_write_png(path,width,height,channels,pixels,stride());
    }

    public void compressToDiskFlipped(String path) {
        stbi_flip_vertically_on_write(true);
        stbi_write_png(path,width,height,channels,pixels,stride());
    }

    /**
     * Allocated direct with BufferUtils (No need to free the buffer)
     * @return compressed png or empty buffer if compressed > (uncompressed * 1.25)
     * Tested with every pixel as random colors, And x1.25 should be more than enough
     * to store the size of worst case scenario.
     */
    public ByteBuffer compress() {
        ByteBuffer buffer = BufferUtils.createByteBuffer((int) (sizeOf() * 1.25f));
        long window = Engine.get().window().handle();
        stbi_write_png_to_func(new STBIWriteCallback() {
            public void invoke(long context, long data, int size) {
                if (size <= buffer.capacity()) {
                    ByteBuffer stb_buffer = STBIWriteCallback.getData(data, size);
                    for (int i = 0; i < size; i++)
                        buffer.put(stb_buffer.get(i));
                } buffer.flip();
            } }, window, width, height, channels, pixels, stride());
        return MemoryUtil.memSlice(buffer);
    }

    public Bitmap copy() {
        ByteBuffer copy = MemoryUtil.memAlloc(sizeOf());
        MemoryUtil.memCopy(pixels,copy);
        return new Bitmap(copy,width,height,channels);
    }

    public Bitmap greyScale() {
        if (channels == 1) { return copy();
        } else { int size = width * height;
            ByteBuffer src = pixels;
            ByteBuffer dst = MemoryUtil.memAlloc(size);
            Vector3f luma = new Vector3f(0.2126f,0.7152f,0.0722f);
            Vector3f color = new Vector3f();
            if (channels == 2) {
                for (int i = 0; i < size; i++) {
                    int idx = i * 2;
                    float avg = (src.get(idx) & 0xFF);
                    avg += (src.get(idx + 1)  & 0xFF);
                    dst.put(i,(byte) (round(avg / 2f) & 0xFF)); }
            } else if (channels == 3) {
                for (int i = 0; i < size; i++) {
                    float r = (src.get(i*channels+0) & 0xFF) / 255.0f;
                    float g = (src.get(i*channels+1) & 0xFF) / 255.0f;
                    float b = (src.get(i*channels+2) & 0xFF) / 255.0f;
                    float v = color.set(r,g,b).dot(luma);
                    dst.put(i,(byte) (round(v * 255f) & 0xFF)); }
            } else if (channels == 4) {
                for (int i = 0; i < size; i++) {
                    float r = (src.get(i*channels+0) & 0xFF) / 255.0f;
                    float g = (src.get(i*channels+1) & 0xFF) / 255.0f;
                    float b = (src.get(i*channels+2) & 0xFF) / 255.0f;
                    float a = (src.get(i*channels+3) & 0xFF) / 255.0f;
                    float v = color.set(r,g,b).dot(luma) * a;
                    dst.put(i,(byte) (round(v * 255f) & 0xFF)); }
            } else throw new IllegalStateException("color channels: " + channels);
            return new Bitmap(dst,width,height,1);
        }
    }

    public Bitmap normalMap(float amp) {
        Bitmap bm = channels == 1 ? this : greyScale();
        float[][] hm = new float[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                float d = ((bm.pixels.get(r*width+c)&0xff)/255f);
                hm[r][c] = (2 * d - 1) * amp; }
        } if (bm != this) bm.dispose();
        ByteBuffer dst = MemoryUtil.memAlloc(width * height * 3);
        float hu, hr, hd, hl;
        final int cBounds = width - 1;
        final int rBounds = height - 1;
        Vector3f n = new Vector3f();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                hr = c < cBounds ? hm[r][c+1] : hm[r][c];
                hd = r < rBounds ? hm[r+1][c] : hm[r][c];
                hu = r > 0 ? hm[r-1][c] : hm[r][c];
                hl = c > 0 ? hm[r][c-1] : hm[r][c];
                n.set(hl - hr,hd - hu,2).normalize();
                dst.put((byte) Math.round((n.x * 0.5f + 0.5f) * 255));
                dst.put((byte) Math.round((n.y * 0.5f + 0.5f) * 255));
                dst.put((byte) Math.round((n.z * 0.5f + 0.5f) * 255));}
        } return new Bitmap(dst.flip(),width,height,3);
    }

    public Texture asTexture() { return asTexture(false); }

    public Texture asTexture(boolean allocate_mipmap) { return asTexture(allocate_mipmap,false); }

    public Texture asTexture(boolean allocate_mipmap, boolean srgb) {
        Texture texture = Texture.generate2D(width,height);
        TextureFormat format;
        switch (channels) {
            case 1  -> format = TextureFormat.R8_UNSIGNED_NORMALIZED;
            case 2  -> format = TextureFormat.RG8_UNSIGNED_NORMALIZED;
            case 3  -> format = srgb ? TextureFormat.SRGB8_UNSIGNED_NORMALIZED : TextureFormat.RGB8_UNSIGNED_NORMALIZED;
            case 4  -> format = srgb ? TextureFormat.SRGBA8_UNSIGNED_NORMALIZED : TextureFormat.RGBA8_UNSIGNED_NORMALIZED;
            default -> format = TextureFormat.INVALID;
        } texture.bindToActiveSlot();
        texture.allocate(format,allocate_mipmap);
        texture.uploadSubData(pixels);
        return texture;
    }

    public ColorPalette palette() {
        ArrayList<Vector4f> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>((int)(128 * 1.75f));
        if (channels == 1) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = (y * width + x) * channels;
                    int r = pixels.get(idx) & 0xFF;
                    int c = Color.rgb_to_intBits(r,0,0,255);
                    if (set.add(c)) list.add(Color.intBits_to_rgb(c,new Vector4f()));
                }
            }
        } else if (channels == 2) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = (y * width + x) * channels;
                    int r = pixels.get(idx + 0) & 0xFF;
                    int g = pixels.get(idx + 1) & 0xFF;
                    int c = Color.rgb_to_intBits(r,g,0,255);
                    if (set.add(c)) list.add(Color.intBits_to_rgb(c,new Vector4f()));
                }
            }
        } else if (channels == 3) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = (y * width + x) * channels;
                    int r = pixels.get(idx + 0) & 0xFF;
                    int g = pixels.get(idx + 1) & 0xFF;
                    int b = pixels.get(idx + 2) & 0xFF;
                    int c = Color.rgb_to_intBits(r,g,b,255);
                    if (set.add(c)) list.add(Color.intBits_to_rgb(c,new Vector4f()));
                }
            }
        } else if (channels == 4) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = (y * width + x) * channels;
                    int c = pixels.getInt(idx);
                    if (set.add(c)) list.add(Color.intBits_to_rgb(c,new Vector4f()));
                }
            }
        } else throw new IllegalStateException("color channels: " + channels);
        list.trimToSize();
        return new ColorPalette(list);
    }

    protected int clampToEdgeX(int x) {
        //noinspection SuspiciousNameCombination
        return Math.max(0,Math.min(width - 1, x));
    }

    protected int clampToEdgeY(int y) {
        return Math.max(0,Math.min(height - 1, y));
    }

    protected boolean boundsCheck(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    protected int alphaBlend(int src, int dst) {
        final float src_alpha = ((src >> 24) & 0xFF) / 255f;
        final float dst_alpha = ((dst >> 24) & 0xFF) / 255f;
        float rf = ((src & 0xFF) / 255f) + ((dst & 0xFF) / 255f) * (1 - src_alpha);
        float gf = (((src >> 8) & 0xFF) / 255f) + (((dst >> 8) & 0xFF) / 255f) * (1 - src_alpha);
        float bf = (((src >> 16) & 0xFF) / 255f) + (((dst >> 16) & 0xFF) / 255f) * (1 - src_alpha);
        float af = src_alpha + dst_alpha * (1 - src_alpha);
        int color = ((int) (rf * 255) & 0xFF);
        color |= (((int)(gf * 255) & 0xFF) << 8 );
        color |= (((int)(bf * 255) & 0xFF) << 16);
        color |= (((int)(af * 255) & 0xFF) << 24);
        return color;
    }

    /** Will cause undefined behaviour if the buffer is not allocated through lwjgl */
    public void dispose() {
        MemoryUtil.memFree(pixels);
    }

    /** combine bitmaps in order and return new. (Exception if channel overflow or wrong dim )*/
    public static Bitmap combine(Bitmap b0, Bitmap b1) throws Exception {
        if ((b0.channels + b1.channels) <= 4) {
            if (b0.width == b1.width && b0.height == b1.height) {
                int channels = b0.channels + b1.channels;
                int num_pixels = b0.height * b0.width;
                int size = num_pixels * channels;
                ByteBuffer dst = MemoryUtil.memAlloc(size);
                int b0_idx = 0, b1_idx = 0, dst_idx = 0;
                for (int i = 0; i < num_pixels; i++) {
                    for (int j = 0; j < b0.channels; j++) {
                        dst.put(dst_idx++,b0.pixels.get(b0_idx++));
                    } for (int j = 0; j < b1.channels; j++) {
                        dst.put(dst_idx++,b1.pixels.get(b1_idx++)); }
                } return new Bitmap(dst,b0.width,b0.height,channels);
            } else throw new Exception("Combining bitmaps != dimensions");
        } else throw new Exception("Combining bitmaps with sum channels > 4");
    }

    /** combine bitmaps in order and return new. (Exception if channel overflow or wrong dim )*/
    public static Bitmap combine(Bitmap b0, Bitmap b1, Bitmap b2) throws Exception {
        if ((b0.channels + b1.channels + b2.channels) <= 4) {
            if (b0.width == b1.width && b0.height == b1.height && b0.width == b2.width && b0.height == b2.height) {
                int channels = b0.channels + b1.channels + b2.channels;
                int num_pixels = b0.height * b0.width;
                int size = num_pixels * channels;
                ByteBuffer dst = MemoryUtil.memAlloc(size);
                int b0_idx = 0, b1_idx = 0, b2_idx = 0, dst_idx = 0;
                for (int i = 0; i < num_pixels; i++) {
                    for (int j = 0; j < b0.channels; j++) {
                        dst.put(dst_idx++,b0.pixels.get(b0_idx++));
                    } for (int j = 0; j < b1.channels; j++) {
                        dst.put(dst_idx++,b1.pixels.get(b1_idx++));
                    } for (int j = 0; j < b2.channels; j++) {
                        dst.put(dst_idx++,b2.pixels.get(b2_idx++)); }
                } return new Bitmap(dst,b0.width,b0.height,channels);
            } else throw new Exception("Combining bitmaps != dimensions");
        } else throw new Exception("Combining bitmaps with sum channels > 4");
    }


}


