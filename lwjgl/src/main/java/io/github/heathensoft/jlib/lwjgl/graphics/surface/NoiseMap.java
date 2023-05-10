package io.github.heathensoft.jlib.lwjgl.graphics.surface;


import io.github.heathensoft.jlib.common.storage.primitive.FloatArray2D;
import io.github.heathensoft.jlib.common.noise.NoiseFunction;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.Bitmap;

/**
 * @author Frederik Dahl
 * 23/06/2022
 */


public class NoiseMap {

    
    private FloatArray2D map;
    private float amplitude;
    private float baseline;
    
    
    public NoiseMap(NoiseFunction function, int rows, int cols, float amplitude, float baseline) {
        this.map = new FloatArray2D(rows, cols);
        this.amplitude = amplitude;
        this.baseline = baseline;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = function.get(c,r);
                n = n * amplitude + baseline;
                map.set(n,c,r);
            }
        }
    }
    
    public NoiseMap(DepthMap8 depthmap, float amplitude, float baseline) {
        this.amplitude = amplitude;
        this.baseline = baseline;
        int rows = depthmap.rows();
        int cols = depthmap.cols();
        this.map = new FloatArray2D(rows,cols);
        float[][] map = get().get();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = ((depthmap.get()[c + r * cols] & 0xff) / 255f);
                map[r][c] = (2 * n - 1) * amplitude + baseline;
            }
        }
    }
    
    public NoiseMap(Bitmap image, float amplitude, float baseline) {
        this(new DepthMap8(image),amplitude,baseline);
    }
    
    public NoiseMap(NoiseFunction function, int rows, int cols, float amplitude) {
        this(function,rows,cols,amplitude,0);
    }
    
    public NoiseMap(NoiseFunction function, int rows, int cols) {
        this(function,rows,cols,1);
    }
    
    public NoiseMap(DepthMap8 depthmap, float amplitude) {
        this(depthmap,amplitude,0);
    }
    
    public NoiseMap(DepthMap8 depthmap) {
        this(depthmap,1);
    }

    public void sharpen() {
        int rows = map.rows();
        int cols = map.cols();
        float[][] m0 = this.map.get();
        float[][] m1 = new float[rows][cols];
        int[][] adj = new int[][] {
                {-1, 1},{ 0, 1},{ 1, 1},
                {-1, 0},{ 0, 0},{ 1, 0},
                {-1,-1},{ 0,-1},{ 1,-1}};
        float[] kernel = {
                -0.250f, -1.000f, -0.250f,
                -1.000f,  6.0000f,-1.000f,
                -0.250f, -1.000f, -0.250f};
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float original = m0[r][c];
                float new_value = 0;
                for (int i = 0; i < adj.length; i++) {
                    float v;
                    int nx = c + adj[i][0];
                    int ny = r + adj[i][1];
                    if (nx < 0 || nx == cols || ny < 0 || ny == rows) {
                        v = original;
                    } else v = m0[ny][nx];
                    new_value += (v * kernel[i]);
                } m1[r][c] = new_value;
            }
        } this.map = new FloatArray2D(m1);

    }
    
    public void blend_add(NoiseMap h1) {
        float[][] m0 = this.map.get();
        float[][] m1 = h1.map.get();
        // recalculate wave properties
        float max = this.max() + h1.max();
        float min = this.min() + h1.min();
        amplitude = (max - min) / 2f;
        baseline = max - amplitude;
        if (this.rows() == h1.rows() && this.cols() == h1.cols()) {
            for (int r = 0; r < rows(); r++) {
                for (int c = 0; c < cols(); c++) {
                    m0[r][c] = m0[r][c] + m1[r][c];
                }
            }
        }
    }
    
    public void smoothen() {
        /* **** */
    }

    public Texture toTexture(int GL_WRAP, int GL_FILTER) {
        // todo:
        return null;
    }
    
    public void toPNG(String path) {
        depthMap8().toPNG(path);
    }
    
    public DepthMap8 depthMap8() {
        return new DepthMap8(this);
    }
    
    public DepthMap16 depthMap16() {
        return new DepthMap16(this);
    }
    
    public NormalMap normalMap() {
        return new NormalMap(new DepthMap8(this), amplitude);
    }
    
    public float baseline() {
        return baseline;
    }
    
    public float amplitude() {
        return amplitude;
    }
    
    float max() {
        return baseline + amplitude;
    }
    
    float min() {
        return baseline - amplitude;
    }
    
    public int rows() {
        return map.rows();
    }
    
    public int cols() {
        return map.cols();
    }
    
    public FloatArray2D get() {
        return map;
    }
}
