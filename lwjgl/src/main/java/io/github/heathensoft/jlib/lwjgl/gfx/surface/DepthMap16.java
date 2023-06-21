package io.github.heathensoft.jlib.lwjgl.gfx.surface;

import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureFormat;
import org.joml.Math;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * 16-bit depth map
 *
 * @author Frederik Dahl
 * 10/04/2022
 */


public class DepthMap16 {
    
    private final int cols;
    private final int rows;
    private final short[] map;
    
    public DepthMap16(NoiseMap nm) {
        this.cols = nm.cols();
        this.rows = nm.cols();
        this.map = new short[cols * rows];
        float[][] m = nm.get().get();
        float amp = nm.amplitude();
        float bsl = nm.baseline();
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = ((m[r][c] - bsl) / amp + 1) / 2f;
                map[idx++] = (short)(Math.round(n * 0xffff) & 0xffff);
            }
        }
    }

    public void toPNG(String path) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(sizeBytes());
        for (int i = 0; i < size(); i++) {
            int b1 = map[i] & 0xFF;
            int b2 = (map[i] >> 8) & 0xFF;
            buffer.put((byte)b1).put((byte)b2);
        } stbi_write_png(path, cols, rows,1,buffer.flip(),cols * 2);
    }

    public Texture toTexture(int wrap, int min_filter, int max_filter, boolean mipmap) {
        Texture texture = Texture.generate2D(cols,rows);
        texture.bindToActiveSlot();
        texture.filter(min_filter,max_filter);
        texture.wrapST(wrap);
        texture.allocate(TextureFormat.R16_UNSIGNED_NORMALIZED,mipmap);
        if (mipmap) texture.generateMipmap();
        texture.uploadData(get());
        Texture.unbindActiveSlot(texture.target());
        return texture;
    }
    
    public short[] get() {
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
    
    public int sizeBytes() {
        return map.length * Short.BYTES;
    }
}
