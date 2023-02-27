package io.github.heathensoft.jlib.lwjgl.graphics.surface;

import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureFormat;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * 8-bit depth map
 *
 * proper grayscale: https://developer.apple.com/documentation/accelerate/converting_color_images_to_grayscale
 *
 * @author Frederik Dahl
 * 31/03/2022
 */


public class DepthMap8 {
    
    private final int cols;
    private final int rows;
    private final byte[] map;
    
    public DepthMap8(NoiseMap nm) {
        this.cols = nm.cols();
        this.rows = nm.rows();
        this.map = new byte[cols * rows];
        float[][] m = nm.get().get();
        float amp = nm.amplitude();
        float bsl = nm.baseline();
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = ((m[r][c] - bsl) / amp + 1) / 2f;
                map[idx++] = (byte)(Math.round(n * 0xff) & 0xff);
            }
        }
    }

    public DepthMap8(int width, int height, int channels, ByteBuffer data) {
        this.cols = width;
        this.rows = height;
        this.map = new byte[cols * rows];
        int c = channels;
        float avg = 0;
        float alpha;
        int length = size();
        switch (c) {
            case 1: case 2: case 3:
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < c; j++) {
                        avg += (data.get(i*c+j) & 0xff);
                    } avg = avg/c;
                    map[i] = (byte) (Math.round(avg) & 0xff);
                    avg = 0;
                }
                break;
            case 4:
                Vector3f luma = new Vector3f(0.2126f,0.7152f,0.0722f);
                Vector3f color = new Vector3f();
                for (int i = 0; i < length; i++) {
                    float r = (data.get(i*c) & 0xff) / 255.0f;;
                    float g = (data.get(i*c+1) & 0xff) / 255.0f;;
                    float b = (data.get(i*c+2) & 0xff) / 255.0f;;
                    float a = (data.get(i*c+3) & 0xff) / 255.0f;
                    float v = color.set(r,g,b).dot(luma) * a;
                    map[i] = (byte) (Math.round(v * 255.0f) & 0xff);
                }
                break;
        }
    }
    
    public DepthMap8(Image img) {
        this(img.width(),img.height(),img.format().channels,img.data());
    }
    
    public Texture toTexture(int wrap, int min_filter, int max_filter, boolean mipmap) {
        Texture texture = Texture.generate2D(cols,rows);
        texture.bindToActiveSlot();
        texture.filter(min_filter,max_filter);
        texture.wrapST(wrap);
        texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,mipmap);
        if (mipmap) texture.generateMipmap();
        texture.uploadData(get());
        Texture.unbindActiveSlot(texture.target());
        return texture;
    }
    
    public void toPNG(String path) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(size());
        buffer.put(map).flip();
        stbi_write_png(path, cols, rows,1,buffer, cols);
    }
    
    public byte[] get() {
        return map;
    }
    
    public int cols() {
        return cols;
    }
    
    public int rows() {
        return rows;
    }
    
    public int size() {
        return map.length;
    }
}
