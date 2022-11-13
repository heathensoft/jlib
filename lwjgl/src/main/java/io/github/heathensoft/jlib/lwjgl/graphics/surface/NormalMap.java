package io.github.heathensoft.jlib.lwjgl.graphics.surface;

import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * 8-bit (per-axis) Normal map
 * Calculates normals using the "finite difference method".
 *
 *
 * @author Frederik Dahl
 * 31/03/2022
 */


public class NormalMap {
 
    public static final int CHANNELS = 3;
    
    private final int rows;
    private final int cols;
    private final float amp;
    private final byte[] map;
    
    /**
     * @param dm 8-bit depth map
     * @param amp amplitude in "pixels" / "tile-size".
     */
    public NormalMap(DepthMap8 dm, float amp) {
        this.cols = dm.cols();
        this.rows = dm.rows();
        this.map = new byte[cols * rows * CHANNELS];
        this.amp = amp;
        float[][] depth = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float d = ((dm.data()[c + r * cols] & 0xff) / 255f);
                depth[r][c] = (2 * d - 1) * amp;
            }
        }
        int idx = 0;
        float hu, hr, hd, hl;
        final int cBounds = cols - 1;
        final int rBounds = rows - 1;
        Vector3f n = new Vector3f();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                hr = c < cBounds ? depth[r][c+1] : depth[r][c];
                hd = r < rBounds ? depth[r+1][c] : depth[r][c];
                hu = r > 0 ? depth[r-1][c] : depth[r][c];
                hl = c > 0 ? depth[r][c-1] : depth[r][c];
                n.set(hl - hr,hd - hu,2).normalize();
                map[idx++] = (byte) Math.round((n.x * 0.5f + 0.5f) * 255);
                map[idx++] = (byte) Math.round((n.y * 0.5f + 0.5f) * 255);
                map[idx++] = (byte) Math.round((n.z * 0.5f + 0.5f) * 255);
            }
        }
    }
    
    public NormalMap(DepthMap16 dm, float amp) {
        this.cols = dm.cols();
        this.rows = dm.rows();
        this.map = new byte[cols * rows * CHANNELS];
        this.amp = amp;
        float[][] depth = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float d = ((dm.data()[c + r * cols] & 0xffff) / (float)0xffff);
                depth[r][c] = (2 * d - 1) * amp;
            }
        }
        int idx = 0;
        float hu, hr, hd, hl;
        final int cBounds = cols - 1;
        final int rBounds = rows - 1;
        Vector3f n = new Vector3f();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                hr = c < cBounds ? depth[r][c+1] : depth[r][c];
                hd = r < rBounds ? depth[r+1][c] : depth[r][c];
                hu = r > 0 ? depth[r-1][c] : depth[r][c];
                hl = c > 0 ? depth[r][c-1] : depth[r][c];
                n.set(hl - hr,hd - hu,2).normalize();
                map[idx++] = (byte) Math.round((n.x * 0.5f + 0.5f) * 255);
                map[idx++] = (byte) Math.round((n.y * 0.5f + 0.5f) * 255);
                map[idx++] = (byte) Math.round((n.z * 0.5f + 0.5f) * 255);
            }
        }
    }
    
    public Texture toTexture(int GL_WRAP, int GL_FILTER) {
        Texture texture = new Texture(GL_TEXTURE_2D);
        texture.bindToActiveSlot();
        texture.filter(GL_FILTER);
        texture.wrapST(GL_WRAP);
        texture.RGB8_2D(map,cols,rows);
        return texture;
    }
    
    public void toPNG(String path) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(size());
        buffer.put(map).flip();
        stbi_write_png(path,cols,rows,CHANNELS,buffer,cols * CHANNELS);
    }
    
    public byte[] data() {
        return map;
    }
    
    public int width() {
        return cols;
    }
    
    public int height() {
        return rows;
    }
    
    public float amplitude() {
        return amp;
    }
    
    public int size() {
        return map.length;
    }
    
}
