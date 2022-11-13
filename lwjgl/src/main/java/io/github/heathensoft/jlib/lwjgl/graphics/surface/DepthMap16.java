package io.github.heathensoft.jlib.lwjgl.graphics.surface;

import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import org.joml.Math;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

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
        Texture texture = new Texture(GL_TEXTURE_2D);
        texture.bindToActiveSlot();
        texture.filter(GL_FILTER);
        texture.wrapST(GL_WRAP);
        texture.R16_2D(map,cols,rows);
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
