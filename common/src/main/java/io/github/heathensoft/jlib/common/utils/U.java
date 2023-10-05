package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.noise.Noise;

import java.nio.*;


/**
 * @author Frederik Dahl
 * 11/04/2023
 */


public class U {

    private static final int[] logTable;

    static {
        logTable = new int[256];
        logTable[0] = logTable[1] = 0;
        for (int i=2; i<256; i++) logTable[i] = 1 + logTable[i/2];
        logTable[0] = -1;
    }

    public static int[][] adj_8 = new int[][] {{-1,0},{ 0,1},{ 1,0},{ 0,-1},{-1,1},{1,1},{-1,1},{-1,-1}};

    public static float clamp(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    public static float remap(float v, float v_min, float v_max, float out_min, float out_max) {
        return lerp(out_min,out_max,unlerp(v_min,v_max,v));
    }

    public static float unlerp(float a, float b, float t) {
        return clamp((t - a) / (b - a));
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

    public static int ceil(float v) {
        return (int) Math.ceil(v);
    }

    public static float square(float v) {
        return v * v;
    }

    public static float sqrt(float v) {
        return (float) Math.sqrt(v);
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min,Math.min(v,max));
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min,Math.min(v,max));
    }

    public static float pow(float v, float e) {
        return (float)Math.pow(v,e);
    }

    /** Modulo for repeating patterns / maps etc. */
    public static int mod_repeat(int v, int range) {
        return v < 0 ? (range + (v % range)) % range : v % range;
    }

    public static float lerp(float a, float b, float t) {
        //return a+(b-a)*t;
        return a * (1-t) + b * t;
    }

    public static float lerp(float a, float b, float c, float t) {
        if (t < 0.5f) return lerp(a,b, unlerp(0,0.5f,t));
        if (t > 0.5f) return lerp(b,c, unlerp(0.5f,1.0f,t));
        return b;
    }

    // lerp default value a between 0 - 1 , ((t = 0.5) == a)
    public static float lerp(float a, float t) {
        if (t < 0.5f) return unlerp(0,0.5f,t) * a;
        if (t > 0.5f) return unlerp(0.5f,1.0f,t) * (1 - a) + a;
        return a;
    }

    public static float brighten(float v, float b) {
        return (b == 0) ? v :  v * (1.f + b);
    }

    public static float raise(float v, float b) {
        if (b == 0) return v;
        if (b > 0) return lerp(clamp(v),1.0f,clamp(abs(b)));
        else return lerp(clamp(v),0.0f,clamp(abs(b)));
    }

    public static float contrast(float v, float c) {
        if (c == 0) return v;
        float start = clamp(v);
        float adjustment = clamp(pow(abs(c),3.0f));
        float target;
        if (c > 0) {
            if (start < 0.5f) target = 0.0f;
            else target = 1.0f;
        } else { target = 0.5f;
        } return lerp(start,target,adjustment);
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

    public static int log2(float f) {
        int x = Float.floatToIntBits(f);
        int c = x >> 23;
        if (c != 0) return c - 127; //Compute directly from exponent.
        else { //Subnormal, must compute from mantissa.
            int t = x >> 16;
            if (t != 0) return logTable[t] - 133;
            else return (x >> 8 != 0) ? logTable[t] - 141 : logTable[x] - 149;
        }
    }

    public static byte[] splice(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length + src2.length];
        ByteBuffer buffer = ByteBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static char[] splice(char[] src1, char[] src2) {
        char[] dst = new char[src1.length + src2.length];
        CharBuffer buffer = CharBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static short[] splice(short[] src1, short[] src2) {
        short[] dst = new short[src1.length + src2.length];
        ShortBuffer buffer = ShortBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static int[] splice(int[] src1, int[] src2) {
        int[] dst = new int[src1.length + src2.length];
        IntBuffer buffer = IntBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static float[] splice(float[] src1, float[] src2) {
        float[] dst = new float[src1.length + src2.length];
        FloatBuffer buffer = FloatBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static float[][] copy_array(float[][] src) {
        final int rows = src.length;
        final int cols = src[0].length;
        final float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            float[] from = src[r];
            float[] to = dst[r];
            System.arraycopy(from, 0, to, 0, from.length);
        } return dst;
    }

    public static int[][] copy_array(int[][] src) {
        final int rows = src.length;
        final int cols = src[0].length;
        final int[][] dst = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            int[] from = src[r];
            int[] to = dst[r];
            System.arraycopy(from, 0, to, 0, from.length);
        } return dst;
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

    public static float[][] smoothen_array(float[][] src, int n) {
        float[][] dst = src;
        for (int i = 0; i < n; i++) {
            dst = smoothen_array(dst);
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

    public static int[][] scale_array(int[][] src, int rows, int cols) {
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

    public static void interleaveBuffers(ByteBuffer src1, int channels1, ByteBuffer src2, int channels2, ByteBuffer dst, int dst_size) {
        int src1_index = 0, src2_index = 0, dst_index = 0;
        for (int i = 0; i < dst_size; i++) {
            for (int j = 0; j < channels1; j++) {
                dst.put(dst_index++,src1.get(src1_index++));
            } for (int j = 0; j < channels2; j++) {
                dst.put(dst_index++,src2.get(src2_index++));
            }
        }
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

    /**
     * "Growing biomes" (Procedural Generation)
     * "http://mc-server.xoft.cz/docs/Generator.html"
     * @param src array to grow. Must be of size: n * n
     * @param target_size size of fully grown array
     * @param rng random generator
     * @return grown array
     */
    public static int[][] grow_biomes(int[][] src, int target_size, Rand rng) {
        if (src[0].length != src.length) {
            throw new IllegalArgumentException("argument array must be of size: n * n");
        } int[][] dst = scale_array(src, next_valid_growing_size(src.length));
        while(dst.length < target_size) {
            dst = grow_biomes(dst,rng);
        } return scale_array(dst,target_size);
    }

    private static int[][] grow_biomes(int[][] src, Rand rng) {
        int[][] dst = prepare_for_growth(src);
        int rows = dst.length;
        int cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r % 2 == 0) {
                    if (c % 2 == 1) {
                        float n = rng.white_noise();
                        int tl = dst[r][c-1];
                        int tr = dst[r][c+1];
                        dst[r][c] = n < 0.5f ? tl : tr;
                    }
                } else {
                    if (c % 2 == 0) {
                        float n = rng.white_noise();
                        int tb = dst[r-1][c];
                        int tt = dst[r+1][c];
                        dst[r][c] = n < 0.5f ? tb : tt;
                    }
                }
            }
        }
        for (int r = 1; r < rows; r += 2) {
            for (int c = 1; c < cols; c += 2) {
                int t1;
                int t2;
                {
                    float n = rng.white_noise();
                    int tl = dst[r][c-1];
                    int tr = dst[r][c+1];
                    t1 = n < 0.5f ? tl : tr;
                }
                {
                    float n = rng.white_noise();
                    int tb = dst[r-1][c];
                    int tt = dst[r+1][c];
                    t2 = n < 0.5f ? tb : tt;
                }
                float n = rng.white_noise();
                dst[r][c] = n < 0.5f ? t1 : t2;
            }
        }
        return dst;
    }

    private static int next_valid_growing_size(int size) {
        int valid_size = 2;
        while (valid_size < size) {
            valid_size = valid_size * 2 - 1;
        } return valid_size;
    }

    private static int[][] prepare_for_growth(int[][] region_map) {
        int rows = region_map.length;
        int cols = region_map[0].length;
        int[][] result = new int[rows * 2 - 1][cols * 2 - 1];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result[r*2][c*2] = region_map[r][c];
        return result;
    }

}
