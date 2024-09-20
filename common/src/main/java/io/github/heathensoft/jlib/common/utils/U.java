package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.noise.Noise;
import io.github.heathensoft.jlib.common.storage.generic.Pool;
import io.github.heathensoft.jlib.common.storage.generic.Stack;
import org.joml.*;
import org.joml.Math;
import org.joml.primitives.Rectanglef;


import java.nio.*;
import java.util.function.Consumer;


/**
 * @author Frederik Dahl
 * 11/04/2023
 */


public class U {

    private static final int[] logTable = new int[256];

    private static final byte v2Count = 32;
    private static final byte v3Count = 16;
    private static final byte v4Count = 16;
    private static final byte m3Count = 4;
    private static final byte m4Count = 8;
    private static final byte rfCount = 32;
    private static int rfIdx = rfCount - 1;
    private static int v4Idx = v4Count - 1;
    private static int v3Idx = v3Count - 1;
    private static int v2Idx = v2Count - 1;
    private static int m4Idx = m4Count - 1;
    private static int m3Idx = m3Count - 1;
    private static final Vector2f[] vec2_stack = new Vector2f[v2Count];
    private static final Vector3f[] vec3_stack = new Vector3f[v3Count];
    private static final Vector4f[] vec4_stack = new Vector4f[v4Count];
    private static final Matrix3f[] mat3_stack = new Matrix3f[m3Count];
    private static final Matrix4f[] mat4_stack = new Matrix4f[m4Count];
    private static final Rectanglef[] rect_stack = new Rectanglef[rfCount];

    public static void pushVec2() { v2Idx++; }
    public static void pushVec3() { v3Idx++; }
    public static void pushVec4() { v4Idx++; }
    public static void pushMat3() { m3Idx++; }
    public static void pushMat4() { m4Idx++; }
    public static void pushRect() { rfIdx++; }
    public static void pushVec2(int count) { v2Idx += count; }
    public static void pushVec3(int count) { v3Idx += count; }
    public static void pushVec4(int count) { v4Idx += count; }
    public static void pushMat3(int count) { m3Idx += count; }
    public static void pushMat4(int count) { m4Idx += count; }
    public static void pushRect(int count) { rfIdx += count; }

    public static Vector2f popVec2() { return vec2_stack[v2Idx--]; }
    public static Vector3f popVec3() { return vec3_stack[v3Idx--]; }
    public static Vector4f popVec4() { return vec4_stack[v4Idx--]; }
    public static Matrix3f popMat3() { return mat3_stack[m3Idx--]; }
    public static Matrix4f popMat4() { return mat4_stack[m4Idx--]; }
    public static Rectanglef popRect() { return rect_stack[rfIdx--]; }
    public static Vector2f popSetVec2(Vector2f vec) { return popVec2().set(vec); }
    public static Vector3f popSetVec3(Vector3f vec) { return popVec3().set(vec); }
    public static Vector4f popSetVec4(Vector4f vec) { return popVec4().set(vec); }
    public static Rectanglef popSetRect(Rectanglef rect) { return popRect().set(rect); }
    public static Vector2f popSetVec2(float x, float y) { return popVec2().set(x,y); }
    public static Vector3f popSetVec3(float x, float y, float z) { return popVec3().set(x,y,z); }
    public static Vector4f popSetVec4(float x, float y, float z, float w) { return popVec4().set(x,y,z,w); }
    public static Rectanglef popSetRect(float minX, float minY, float maxX, float maxY) {
        Rectanglef rect = popRect();
        rect.minX = minX;
        rect.minY = minY;
        rect.maxX = maxX;
        rect.maxY = maxY;
        return rect;
    }

    static {
        for (int i = 0; i < vec2_stack.length; i++) vec2_stack[i] = new Vector2f();
        for (int i = 0; i < vec3_stack.length; i++) vec3_stack[i] = new Vector3f();
        for (int i = 0; i < vec4_stack.length; i++) vec4_stack[i] = new Vector4f();
        for (int i = 0; i < mat3_stack.length; i++) mat3_stack[i] = new Matrix3f();
        for (int i = 0; i < mat4_stack.length; i++) mat4_stack[i] = new Matrix4f();
        for (int i = 0; i < rect_stack.length; i++) rect_stack[i] = new Rectanglef();
        {
            logTable[0] = logTable[1] = 0;
            for (int i=2; i<256; i++) logTable[i] = 1 + logTable[i/2];
            logTable[0] = -1;
        }
    }

    public static final float ROT_0 = 0f;
    public static final float ROT_90 =  (float)(    (Math.PI / 2d));
    public static final float ROT_180 = (float)(2 * (Math.PI / 2d));
    public static final float ROT_270 = (float)(3 * (Math.PI / 2d));
    public static final float ROT_360 = (float)(4 * (Math.PI / 2d));
    public static final int[][] adj_8 = new int[][] {
            {-1, 0},{ 0, 1},{ 1, 0},
            { 0,-1},        {-1, 1},
            { 1, 1},{-1, 1},{-1,-1}};


    public static Rectanglef rectSet(Rectanglef dst, float minX, float minY, float maxX, float maxY) {
        dst.minX = minX;
        dst.minY = minY;
        dst.maxX = maxX;
        dst.maxY = maxY;
        return dst;
    }
    public static Rectanglef rectExpand(Rectanglef dst, float amount) {
        dst.minX -= amount;
        dst.minY -= amount;
        dst.maxX += amount;
        dst.maxY += amount;
        return dst;
    }
    public static Vector2f clamp(Vector2f dst) {
        dst.x = clamp(dst.x);
        dst.y = clamp(dst.y);
        return dst;
    }

    public static Vector2f lerp(Vector2f a, Vector2f b, float t, Vector2f dst) {
        if (t <= 0) dst.set(a);
        else if (t >= 1) dst.set(b);
        else {
            dst.x = lerp(a.x,b.x,t);
            dst.y = lerp(a.y,b.y,t);
        } return dst;
    }

    public static Vector2f smoothLerp(Vector2f a, Vector2f b, float t, Vector2f dst) {
        if (t <= 0) dst.set(a);
        else if (t >= 1) dst.set(b);
        else {
            t = smooth(t);
            dst.x = lerp(a.x,b.x,t);
            dst.y = lerp(a.y,b.y,t);
        } return dst;
    }


    public static float angle2D(float x, float y) { return Math.atan2(y,x); }
    public static float angle2D(Vector2f v) { return Math.atan2(v.y,v.x); }
    public static Vector2f rotate2D(Vector2f dst, float rad) {
        float a = angle2D(dst);
        float l = dst.length();
        float x = l * Math.cos(a + rad);
        float y = l * Math.sin(a + rad);
        return dst.set(x,y);
    }


    public static boolean floatEquals(double a, double b) { return floatEquals(a, b,(1e-9)); }
    public static boolean floatEquals(double a, double b, double epsilon) { return Math.abs(a - b) < epsilon; }
    public static float clamp(float v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    public static float clamp(float v, float min, float max) { return Math.max(min,Math.min(v,max)); }
    public static int clamp(int v, int min, int max) { return Math.max(min,Math.min(v,max)); }
    public static float remap(float v, float v_min, float v_max, float out_min, float out_max) { return lerp(out_min,out_max, unLerp(v_min,v_max,v)); }
    public static float unLerp(float a, float b, float t) { return clamp((t - a) / (b - a)); }
    public static float smooth(float v) { return v * v * (3.0f - 2.0f * v); }
    public static float quadraticEraseOut(float v) { return 1.0f - (1.0f - v) * (1.0f - v); }
    public static float fract(float v) { return Math.abs(v - (int)v); }
    public static int floor(float v) { return (int) Math.floor(v); }
    public static int ceil(float v) { return (int) Math.ceil(v); }
    public static float square(float v) { return v * v; }
    public static float abs(float f) { return Math.abs(f); }
    public static float sqrt(float v) { return Math.sqrt(v); }
    public static float pow(float v, float e) { return (float) java.lang.Math.pow(v,e); }
    /** Modulo for repeating patterns / maps etc. */
    public static int modRepeat(int v, int range) { return v < 0 ? (range + (v % range)) % range : v % range; }
    public static int round(float f) { return Math.round(f); }
    public static int round(double d) { return (int) Math.round(d); }
    public static float round(double d, int digits) {
        if (digits <= 0) return round(d);
        double e = 10 * digits;
        return (float) (Math.round(d * e) / e);
    }
    public static float lerp(float a, float b, float t) { return a * (1-t) + b * t; }
    public static float lerp(float a, float b, float c, float t) {
        if (t < 0.5f) return lerp(a,b, unLerp(0,0.5f,t));
        if (t > 0.5f) return lerp(b,c, unLerp(0.5f,1.0f,t));
        return b;
    }
    // lerp default value a between 0 - 1 , ((t = 0.5) == a)
    public static float lerp(float a, float t) {
        if (t < 0.5f) return unLerp(0,0.5f,t) * a;
        if (t > 0.5f) return unLerp(0.5f,1.0f,t) * (1 - a) + a;
        return a;
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

    // *********************************************************************
    // Generalized Bit-operations.
    // The following bit-operations assumes you are within the integer range.
    // You are responsible for checking overflow
    public static int bitsOr(int dst, int src, int dst_off, int src_len) {
        return dst | ((src & ((1 << src_len) - 1)) << dst_off);
    }

    public static int bitsClear(int dst, int off, int len) {
        return dst & ~(((1 << len) - 1) << off);
    }

    public static int bitsSet(int dst, int src, int dst_off, int src_len) {
        int mask = (1 << src_len) - 1;
        dst &= ~(mask << dst_off);
        dst |= ((src & mask) << dst_off);
        return dst;
    }

    public static int bitsSet(int dst, int n) {
        return dst | (1 << n);
    }

    public static boolean bitIsSet(int bits, int n) {
        return (bits & (1 << n)) > 0;
    }

    // *********************************************************************

    public static float brighten(float v, float amount) {
        return (amount == 0) ? v :  v * (1.f + amount);
    }


    // both arguments must be clamped
    public static float raiseProportional(float v, float amount) {
        return v + (1-v) * amount;
    }

    public static float lowerProportional(float v, float amount) {
        return v - v * amount;
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

    public static byte[] arraySplice(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length + src2.length];
        ByteBuffer buffer = ByteBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static char[] arraySplice(char[] src1, char[] src2) {
        char[] dst = new char[src1.length + src2.length];
        CharBuffer buffer = CharBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static short[] arraySplice(short[] src1, short[] src2) {
        short[] dst = new short[src1.length + src2.length];
        ShortBuffer buffer = ShortBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static int[] arraySplice(int[] src1, int[] src2) {
        int[] dst = new int[src1.length + src2.length];
        IntBuffer buffer = IntBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static float[] arraySplice(float[] src1, float[] src2) {
        float[] dst = new float[src1.length + src2.length];
        FloatBuffer buffer = FloatBuffer.wrap(dst);
        return buffer.put(src1).put(src2).array();
    }

    public static float[][] arrayCopy(float[][] src) {
        final int rows = src.length;
        final int cols = src[0].length;
        final float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            float[] from = src[r];
            float[] to = dst[r];
            System.arraycopy(from, 0, to, 0, from.length);
        } return dst;
    }

    public static int[][] arrayCopy(int[][] src) {
        final int rows = src.length;
        final int cols = src[0].length;
        final int[][] dst = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            int[] from = src[r];
            int[] to = dst[r];
            System.arraycopy(from, 0, to, 0, from.length);
        } return dst;
    }

    public static float[][] arraySharpen(float[][] src) {
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

    public static int[][] arraySmoothen(int[][] src) {
        return arraySmoothen(src,1);
    }

    public static int[][] arraySmoothen(int[][] src, int n) {
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
                    } tmp0[r][c] = mostFrequent(frequency,acc);
                }
            }
            dst = tmp0;
            tmp0 = tmp1;
            tmp1 = dst;
        } return dst;
    }

    public static float[][] arraySmoothen(float[][] src, int n) {
        float[][] dst = src;
        for (int i = 0; i < n; i++) {
            dst = arraySmoothen(dst);
        } return dst;
    }

    public static float[][] arraySmoothen(float[][] src) {
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


    public static float[][] arrayScale(float[][] src, int target_size) {
        return arrayScale(src,target_size,target_size);
    }


    public static float[][] arrayScale(float[][] src, int rows, int cols) {
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


    public static int[][] arrayScale(int[][] src, int target_size) {
        return arrayScale(src,target_size,target_size);
    }

    public static int[][] arrayScale(int[][] src, int rows, int cols) {
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

    private static int mostFrequent(int[] arr, int n)  {
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
    public static int[][] growBiomes(int[][] src, int target_size, Rand rng) {
        if (src[0].length != src.length) {
            throw new IllegalArgumentException("argument array must be of size: n * n");
        } int[][] dst = arrayScale(src, next_valid_growing_size(src.length));
        while(dst.length < target_size) {
            dst = growBiomes(dst,rng);
        } return arrayScale(dst,target_size);
    }

    private static int[][] growBiomes(int[][] src, Rand rng) {
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
