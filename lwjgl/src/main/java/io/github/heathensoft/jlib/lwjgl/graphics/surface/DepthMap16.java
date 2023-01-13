package io.github.heathensoft.jlib.lwjgl.graphics.surface;

import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureFormat;
import org.joml.Math;

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
        float[][] m = nm.map().get();
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
    
    public Texture toTexture(int GL_WRAP, int GL_FILTER) {
        Texture texture = Texture.generate2D(cols,rows);
        texture.bindToActiveSlot();
        texture.filter(GL_FILTER,GL_FILTER);
        texture.wrapST(GL_WRAP);
        texture.allocate(TextureFormat.R16_UNSIGNED_NORMALIZED,false);
        texture.uploadData(data());
        return texture;
    }
    
    public short[] data() {
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
