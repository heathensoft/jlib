package io.github.heathensoft.jlib.tiles.neo.generation;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureFormat;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Frederik Dahl
 * 27/04/2023
 */


@Deprecated
public class DepthMap8 implements Disposable {

    private final int width;
    private final int height;
    private final ByteBuffer data;

    public DepthMap8(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = MemoryUtil.memAlloc(size());
    }

    public DepthMap8(float[][] heightmap) {
        this.width = heightmap[0].length;
        this.height = heightmap.length;
        int size = size();
        this.data = MemoryUtil.memAlloc(size);
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                float f = clamp(heightmap[r][c]);
                data.put(r * width + c,(byte) (round(f*255f) & 0xFF));
            }
        }
    }

    public DepthMap8(BitmapOld bitmap) {
        // todo: very wrong (data swapped by srs)
        this.width = bitmap.width();
        this.height = bitmap.height();
        int channels = bitmap.channels();
        if (channels == 1) {
            this.data = bitmap.data();
        } else {
            int size = size();
            this.data = MemoryUtil.memAlloc(size);
            ByteBuffer src = bitmap.data();
            Vector3f luma = new Vector3f(0.2126f,0.7152f,0.0722f);
            Vector3f color = new Vector3f();
            switch (bitmap.channels()) {
                case 2:
                    for (int i = 0; i < size; i++) {
                        int idx = i * 2;
                        float avg = (src.get(idx) & 0xFF);
                        avg += (src.get(idx + 1) & 0xFF);
                        avg /= 2.0f;
                        data.put(i,(byte) (round(avg) & 0xFF));
                    } break;
                case 3:
                    for (int i = 0; i < size; i++) {
                        float r = (data.get(i*channels) & 0xff) / 255.0f;
                        float g = (data.get(i*channels+1) & 0xff) / 255.0f;
                        float b = (data.get(i*channels+2) & 0xff) / 255.0f;
                        float v = color.set(r,g,b).dot(luma);
                        data.put(i,(byte) (round(v * 255f) & 0xFF));
                    } break;
                case 4:
                    for (int i = 0; i < size; i++) {
                        float r = (data.get(i*channels) & 0xff) / 255.0f;
                        float g = (data.get(i*channels+1) & 0xff) / 255.0f;
                        float b = (data.get(i*channels+2) & 0xff) / 255.0f;
                        float a = (data.get(i*channels+3) & 0xff) / 255.0f;
                        float v = color.set(r,g,b).dot(luma) * a;
                        data.put(i,(byte) (round(v * 255f) & 0xFF));
                    } break;
            }
        }
    }

    public Texture toTexture(int wrap, int min_filter, int max_filter, boolean mipmap) {
        Texture texture = Texture.generate2D(width,height);
        texture.bindToActiveSlot();
        texture.filter(min_filter,max_filter);
        texture.textureWrapST(wrap);
        texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,mipmap);
        if (mipmap) texture.generateMipmap();
        texture.uploadData(data);
        return texture;
    }

    public void toDisk(String path) {
        stbi_write_png(path, width, height,1, data, width);
    }

    public byte get(int x, int y) {
        return data.get(y * width + x);
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

    public int size() {
        return width * height;
    }

    public void dispose() {
        if (data != null) {
            MemoryUtil.memFree(data);
        }
    }

}
