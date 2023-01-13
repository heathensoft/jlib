package io.github.heathensoft.jlib.lwjgl.graphics.surface;

import io.github.heathensoft.jlib.common.Assert;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureFormat;
import org.joml.Math;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * 8-bit depth map
 *
 * @author Frederik Dahl
 * 31/03/2022
 */


public class DepthMap8 {
    
    private final int cols;
    private final int rows;
    private final byte[] map;
    
    public DepthMap8(NoiseMap nm) {
        Assert.notNull(nm);
        this.cols = nm.cols();
        this.rows = nm.rows();
        this.map = new byte[cols * rows];
        float[][] m = nm.map().get();
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
    
    public DepthMap8(Image img) {
        this.cols = img.width();
        this.rows = img.height();
        this.map = new byte[cols * rows];
        int c = img.format().channels;
        float avg = 0;
        float alpha;
        int length = size();
        ByteBuffer data = img.data();
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
                for (int i = 0; i < length; i++) {
                    alpha = (data.get(i*c+3) & 0xff) / 255.0f;
                    avg += (data.get(i*c) & 0xff);
                    avg += (data.get(i*c+1) & 0xff);
                    avg += (data.get(i*c+2) & 0xff);
                    avg = avg/3;
                    avg *= alpha;
                    map[i] = (byte) (Math.round(avg) & 0xff);
                    avg = 0;
                }
            break;
        }
    }
    
    public Texture toTexture(int GL_WRAP, int GL_FILTER) {
        Texture texture = Texture.generate2D(cols,rows);
        texture.bindToActiveSlot();
        texture.filter(GL_FILTER,GL_FILTER);
        texture.wrapST(GL_WRAP);
        texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        texture.uploadData(data());
        return texture;
    }
    
    public void toPNG(String path) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(size());
        buffer.put(map).flip();
        stbi_write_png(path, cols, rows,1,buffer, cols);
    }
    
    public byte[] data() {
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
