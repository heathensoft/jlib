package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.noise.Noise;

/**
 * @author Frederik Dahl
 * 11/04/2023
 */


public class U {

    public static int[][] adj_8 = new int[][] {{-1,0},{ 0,1},{ 1,0},{ 0,-1},{-1,1},{1,1},{-1,1},{-1,-1}};

    public static float clamp(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    public static float map(float v, float min, float max) {
        return clamp((v - min) / (max - min));
    }

    public static float smooth(float v) {
        return v * v * (3.0f - 2.0f * v);
    }

    public static float fract(float v) {
        return Math.abs(v - (int)v);
    }

    public static int floor(float v) {
        return (int) Math.floor(v);
    }

    public static float lerp(float a, float b, float f) {
        return a+(b-a)*f;
    }

    public static float lerp(float a, float b, float c, float f) {
      if (f < 0.5f) return lerp(a,b,map(f,0,0.5f));
      if (f > 0.5f) return lerp(b,c,map(f,0.5f,1.0f));
      return b;
    }

    // lerp default value a between 0 - 1 , ((f = 0.5) == a)
    public static float lerp(float a, float f) {
        if (f < 0.5f) return map(f,0,0.5f) * a;
        if (f > 0.5f) return map(f,0.5f,1.0f) * (1 - a) + a;
        return a;
    }

    public static int round(float f) {
        return Math.round(f);
    }

    public static float abs(float f) {
        return Math.abs(f);
    }

    public static int nextPowerOfTwo(int value) {
        if (value-- == 0) return 1;
        value |= value >>> 1;
        value |= value >>> 2;
        value |= value >>> 4;
        value |= value >>> 8;
        value |= value >>> 16;
        return value + 1;
    }

    public static float[][] sharpen_array(float[][] src) {
        float[] kernel = {
                -0.250f, -1.000f, -0.250f,
                -1.000f,  6.0000f,-1.000f,
                -0.250f, -1.000f, -0.250f};
        int[][] adj = new int[][] {
                {-1, 1},{ 0, 1},{ 1, 1},
                {-1, 0},{ 0, 0},{ 1, 0},
                {-1,-1},{ 0,-1},{ 1,-1}};
        int rows = src.length;
        int cols = src[0].length;
        float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float original = src[r][c];
                float new_value = 0;
                for (int i = 0; i < adj.length; i++) {
                    float v;
                    int nx = c + adj[i][0];
                    int ny = r + adj[i][1];
                    if (nx < 0 || nx == cols || ny < 0 || ny == rows) {
                        v = original;
                    } else v = src[ny][nx];
                    new_value += (v * kernel[i]);
                } dst[r][c] = clamp(new_value);
            }
        } return dst;

    }


    public static float[][] smoothen_array(float[][] src) {
        float[] kernel = new float[] {
                0.0778f,0.1233f,0.0778f,
                0.1233f,0.1953f,0.1233f,
                0.0778f,0.1233f,0.0778f
        }; int[][] adj = new int[][] {
                {-1, 1},{ 0, 1},{ 1, 1},
                {-1, 0},{ 0, 0},{ 1, 0},
                {-1,-1},{ 0,-1},{ 1,-1}};
        int rows = src.length;
        int cols = src[0].length;
        float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float original = src[r][c];
                float new_value = 0;
                for (int i = 0; i < adj.length; i++) {
                    float v;
                    int nx = c + adj[i][0];
                    int ny = r + adj[i][1];
                    if (nx < 0 || nx == cols || ny < 0 || ny == rows) {
                        v = original;
                    } else v = src[ny][nx];
                    new_value += (v * kernel[i]);
                } dst[r][c] = new_value;
            }
        } return dst;
    }


    public static float[][] scale_array(float[][] src, int target_size) {
        return scale_array(src,target_size,target_size);
    }


    public static float[][] scale_array(float[][] src, int rows, int cols) {
        Noise.Sampler2D sampler = new Noise.Sampler2D(src);
        float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            float v = (float) r / rows;
            for (int c = 0; c < cols; c++) {
                float u = (float) c / cols;
                dst[r][c] = sampler.linear(u,v);
            }
        } return dst;
    }


    public static int[][] scale_array(int[][] src, int target_size) {
        return scale_array(src,target_size,target_size);
    }

    private static int[][] scale_array(int[][] src, int rows, int cols) {
        int src_rows = src.length;
        int src_cols = src[0].length;
        int[][] dst = new int[rows][cols];
        double x_ratio = (double) src_cols / cols;
        double y_ratio = (double) src_rows / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = (int)(x_ratio * c);
                int y = (int)(y_ratio * r);
                dst[r][c] = src[y][x];
            }
        } return dst;
    }

    public static int[][] smoothen_array(int[][] src) {
        return smoothen_array(src,1);
    }

    public static int[][] smoothen_array(int[][] src, int n) {
        int rows = src.length;
        int cols = src[0].length;
        int[][] tmp0 = new int[rows][cols];
        int[][] tmp1 = src;
        int[][] dst = tmp0;
        int[][] adj = new int[][] {
                {-1, 1},{ 0, 1},{ 1, 1},
                {-1, 0},{ 0, 0},{ 1, 0},
                {-1,-1},{ 0,-1},{ 1,-1}};
        int[] frequency = new int[9];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int acc = 0;
                    for (int[] j : adj) {
                        int nx = c + j[0];
                        int ny = r + j[1];
                        if (!(nx < 0 || nx == cols || ny < 0 || ny == rows))
                            frequency[acc++] = tmp1[ny][nx];
                    } tmp0[r][c] = most_frequent(frequency,acc);
                }
            }
            dst = tmp0;
            tmp0 = tmp1;
            tmp1 = dst;
        } return dst;
    }

    private static int most_frequent(int[] arr, int n)  {
        int max_count = 0;
        int most_frequent = 0;
        for (int i = 0; i < n; i++) {
            int count = 0;
            for (int j = 0; j < n; j++) {
                if (arr[i] == arr[j])
                    count++;
            } if (count > max_count) {
                max_count = count;
                most_frequent = arr[i]; }
        } return most_frequent;
    }
}
