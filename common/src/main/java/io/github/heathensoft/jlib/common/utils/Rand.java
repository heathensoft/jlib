package io.github.heathensoft.jlib.common.utils;

/**
 * @author Frederik Dahl
 * 31/03/2023
 */


public class Rand {


    private long instance_last;
    private long instance_inc;

    public Rand() {
        this(System.currentTimeMillis());
    }

    public Rand(long seed) {
        instance_last = seed | 1;
        instance_inc = seed;
    }

    public float nextFloat() {
        return (float) nextInt(0x2B2A_B5E9) / 0x2B2A_B5E9;
    }

    public int nextInt(int max) {
        instance_last ^= (instance_last << 21);
        instance_last ^= (instance_last >>> 35);
        instance_last ^= (instance_last << 4);
        instance_inc += 123456789123456789L;
        int out = (int) ((instance_last + instance_inc) % max);
        return (out < 0) ? -out : out;
    }

    public static float noise(float x, float y, int seed, float fq) {
        final float fx = smooth(fract(x * fq));
        final float fy = smooth(fract(y * fq));
        final int ix = floor(x * fq);
        final int iy = floor(y * fq);
        final float b = mix(white_noise(ix,iy,seed), white_noise(ix + 1,iy,seed),fx);
        final float t = mix(white_noise(ix,iy + 1,seed), white_noise(ix+1,iy + 1,seed),fx);
        return mix(b,t,fy);
    }

    public static float noise_layered(float x, float y, int seed, float fq, int octaves) {
        float n = 0.0f, amp = 1.0f, acc = 0.0f;
        for (int i = 0; i < octaves; i++) {
            n += noise(x,y,seed++,fq) * amp;
            acc += amp; amp *= 0.5f; fq *= 2.0f;
        } return acc == 0 ? 0 : n / acc;
    }

    public static float noise(float x, int seed, float fq) {
        final float fx = smooth(fract(x * fq));
        final int ix = floor(x * fq);
        return mix(white_noise(ix,seed),white_noise(ix+1,seed),fx);
    }

    public static float noise_layered(float x, int seed, float fq, int octaves) {
        float n = 0.0f, amp = 1.0f, acc = 0.0f;
        for (int i = 0; i < octaves; i++) {
            n += noise(x,seed++,fq) * amp;
            acc += amp; amp *= 0.5f; fq *= 2.0f;
        } return acc == 0 ? 0 : n / acc;
    }

    public static float white_noise(int x, int y, int seed) {
        return (hash(x + (0x0BD4BCB5 * y), seed) & 0x7FFF_FFFF) / (float) 0x7FFF_FFFF;
    }

    public static float white_noise(int x, int seed) {
        return (hash(x,seed) & 0x7FFF_FFFF) / (float) 0x7FFF_FFFF;
    }

    public static int hash(int value, int seed) {
        long m = (long) value & 0xFFFFFFFFL;
        m *= 0xB5297AAD;
        m += seed;
        m ^= (m >> 8);
        m += 0x68E31DA4;
        m ^= (m << 8);
        m *= 0x1B56C4E9;
        m ^= (m >> 8);
        return (int) m;
    }

    private static float fract(double d) {
        return (float) (d - (long) d);
    }

    private static int floor(double d) {
        return (int) Math.floor(d);
    }

    private static float smooth(float f) {
        return (f * f * (3.0f - 2.0f * f));
    }

    private static float mix(float f1, float f2, float a) {
        return (float) (f1 * (1.0 - a) + f2 * a);
    }
}
