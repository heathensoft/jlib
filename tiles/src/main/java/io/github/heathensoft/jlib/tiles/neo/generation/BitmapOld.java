package io.github.heathensoft.jlib.tiles.neo.generation;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureFormat;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.tiles.graphics.Color32;
import org.joml.Math;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.floor;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * @author Frederik Dahl
 * 11/04/2023
 */


@Deprecated
public class BitmapOld implements Disposable {

    private final ByteBuffer data;
    private final int width;
    private final int height;
    private final int channels;

    public BitmapOld(int[][] abgr8) {
        width = abgr8[0].length;
        height = abgr8.length;
        channels = 4;
        data = MemoryUtil.memAlloc(width * height * channels);
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int idx = (r * width + c) * channels;
                data.putInt(idx,abgr8[r][c]);
            }
        }
    }

    public BitmapOld(int width, int height, int channels) {
        if (channels < 0 || channels > 4) throw new RuntimeException("invalid channels: " + channels);
        this.data = MemoryUtil.memCalloc(width * height * channels);
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public BitmapOld(ByteBuffer png) throws Exception {
        this(png,false);
    }

    public BitmapOld(ByteBuffer png, boolean flip_v) throws Exception {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            if (!stbi_info_from_memory(png, w, h, c))
                throw new Exception("unable to read image info: " + stbi_failure_reason());
            stbi_set_flip_vertically_on_load(flip_v);
            this.data = stbi_load_from_memory(png, w, h, c, 0);
            if (data == null) throw new Exception("unable to load image: " + stbi_failure_reason());
            this.width = w.get(0);
            this.height = h.get(0);
            this.channels = c.get(0);
        }
    }

    public void draw_nearest_sampling(BitmapOld source, TextureRegion region, int x0, int y0, int w, int h) {
        draw_nearest_sampling(source,x0,y0,w,h,region.u(),region.v(),region.u2(),region.v2());
    }

    public void draw_linear_sampling(BitmapOld source, TextureRegion region, int x0, int y0, int w, int h) {
        draw_linear_sampling(source,x0,y0,w,h,region.u(),region.v(),region.u2(),region.v2());
    }

    public void draw_nearest_sampling(BitmapOld source, int x0, int y0, int w, int h, float u1, float v1, float u2, float v2) {
        if (w > 1 && h > 1) {
            if (source.channels == 4 && channels == 4) {
                for (int r = 0; r < h; r++) {
                    int texel_y = r + y0;
                    for (int c = 0; c < w; c++) {
                        int texel_x = c + x0;
                        if (texel_y >= 0 && texel_x >= 0 && texel_y < height && texel_x < width) {
                            float u = U.lerp(u1,u2, (float) (c) / (float) (w - 1));
                            float v = U.lerp(v1,v2, (float) (r) / (float) (h - 1));
                            int dst_color = get_unchecked(texel_x,texel_y);
                            int src_color = source.sampleNearest(u,v);
                            set_unchecked(texel_x,texel_y,alpha_blend(src_color,dst_color));
                        }
                    }
                }
            } else if (source.channels == channels){
                for (int r = 0; r < h; r++) {
                    int texel_y = r + y0;
                    for (int c = 0; c < w; c++) {
                        int texel_x = c + x0;
                        if (texel_y >= 0 && texel_x >= 0 && texel_y < height && texel_x < width) {
                            float u = U.lerp(u1,u2, (float) (c) / (float) (w - 1));
                            float v = U.lerp(v1,v2, (float) (r) / (float) (h - 1));
                            set_unchecked(texel_x,texel_y,source.sampleNearest(u,v));
                        }
                    }
                }
            }
        }
    }

    public void draw_linear_sampling(BitmapOld source, int x0, int y0, int w, int h, float u1, float v1, float u2, float v2) {
        if (w > 1 && h > 1) {
            if (source.channels == 4 && channels == 4) {
                for (int r = 0; r < h; r++) {
                    int texel_y = r + y0;
                    for (int c = 0; c < w; c++) {
                        int texel_x = c + x0;
                        if (texel_y >= 0 && texel_x >= 0 && texel_y < height && texel_x < width) {
                            // TODO: Linear sampling is not correct ()
                            float u = U.lerp(u1,u2, (float) (c) / (float) (w - 1));
                            float v = U.lerp(v1,v2, (float) (r) / (float) (h - 1));
                            int dst_color = get_unchecked(texel_x,texel_y);
                            int src_color = source.sampleLinear(u,v);
                            set_unchecked(texel_x,texel_y,alpha_blend(src_color,dst_color));
                        }
                    }
                }
            }else if (source.channels == channels){
                for (int r = 0; r < h; r++) {
                    int texel_y = r + y0;
                    for (int c = 0; c < w; c++) {
                        int texel_x = c + x0;
                        if (texel_y >= 0 && texel_x >= 0 && texel_y < height && texel_x < width) {
                            float u = U.lerp(u1,u2, (float) (c) / (float) (w - 1));
                            float v = U.lerp(v1,v2, (float) (r) / (float) (h - 1));
                            set_unchecked(texel_x,texel_y,source.sampleLinear(u,v));
                        }
                    }
                }
            }
        }
    }

    public void draw_pixel(int x, int y, Color32 color) {
        draw_pixel(x, y, color.intBits());
    }

    public void draw_pixel(int x, int y, int color) {
        set(x,y,alpha_blend(color,get(x,y)));
    }

    public void draw_pixel_unchecked(int x, int y, int color) {
        set_unchecked(x,y,alpha_blend(color,get_unchecked(x,y)));
    }

    public void draw_pixel_unchecked(int x, int y, Color32 color) {
        draw_pixel_unchecked(x, y, color.intBits());
    }

    // When using the bitmap as source for drawing ops, it's useful to premultiply before alpha blending

    public void premultiplyAlpha() {
        if (channels == 4) {
            final ByteBuffer b = data;
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
            final ByteBuffer b = data;
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

    public void set(int x, int y, Color32 color) {
        set(x, y, color.intBits());
    }

    public void set(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            set_unchecked(x, y, color);
        }
    }

    public int[][] array() {
        int[][] result = new int[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                result[r][c] = get_unchecked(c,r);
            }
        } return result;
    }

    public int get(int x, int y) {
        x = Math.max(0,Math.min(x,width-1));
        y = Math.max(0,Math.min(y,height-1));
        return get_unchecked(x,y);
    }

    public void set_unchecked(int x, int y, int color) {
        int idx = y * width * channels + x * channels;
        if (channels == 4) data.putInt(idx,color);
        else { for (int i = 0; i < channels; i++) {
            data.put(idx + i, (byte) ((color >> (i * 8)) & 0xFF)); }
        }
    }

    public int get_unchecked(int x, int y) {
        int idx = y * width * channels + x * channels;
        if (channels == 4) {
            return data.getInt(idx);
        } int color = 0;
        for (int i = 0; i < channels; i++) {
            color |= (data.get(idx + i) & 0xFF) << (i * 8);
        } return color;
    }

    public int sampleNearest(float u, float v) {
        int x = (int) (clamp(u) * width);
        int y = (int) (clamp(v) * height);
        int color = 0;
        int idx = y_clamp(y) * width * channels + x_clamp(x) * channels;
        for (int i = 0; i < channels; i++) {
            color |= (data.get(idx + i) & 0xFF) << (i * 8);
        } return color;
    }

    public int sampleLinear(float u, float v) {
        float x = (clamp(u) * width);
        float y = (clamp(v) * height);
        float fx = fract(clamp(x-0.49f)); // TODO: previous: fract(x)
        float fy = fract(clamp(y-0.49f));
        int ix = floor(x);
        int iy = floor(y);
        int color = 0;
        for (int i = 0; i < channels; i++) {
            int n0 = color_component(ix,iy,i);
            int n1 = color_component(ix+1,iy,i);
            int n2 = color_component(ix,iy+1,i);
            int n3 = color_component(ix+1,iy+1,i);
            int c = interpolate_color_component(
                    interpolate_color_component(n0,n1,fx),
                    interpolate_color_component(n2,n3,fx),fy);
            color |= ((c & 0xFF) << (i * 8));
        } return color;
    }

    // Do not use this anonymously. Bitmap must be disposed
    public Texture asTexture() {
        return asTexture(false);
    }

    public Texture asTexture(boolean allocate_mipmap) {
        return asTexture(allocate_mipmap,false);
    }

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
        texture.uploadData(data);
        return texture;
    }

    public void toDisk(String path) {
        stbi_write_png(path,width,height,channels, data,width * channels);
    }

    public ByteBuffer data() {
        return data;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int channels() {
        return channels;
    }

    public int pixels() {
        return width * height;
    }

    public int sizeOf() {
        return pixels() * channels;
    }

    public void clear() {
        int p = data.position();
        int l = data.limit();
        for (int i = p; i < l; i++) {
            data.put(i,(byte)0);
        }
    }

    public void dispose() {
        if (data.isDirect()) {
            MemoryUtil.memFree(data);
        }
    }

    private int x_clamp(int x) {
        return Math.min(width - 1, x);
    }

    private int y_clamp(int y) {
        return Math.min(height - 1, y);
    }

    private static int interpolate_color_component(int c0, int c1, float v) {
        return (int)(c0+(c1-c0)*v);
    }

    private int color_component(int x, int y, int c) {
        return data.get(y_clamp(y) * width * channels + x_clamp(x) * channels + c) & 0xFF;
    }

    private int alpha_blend(int src, int dst) {
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



}






















