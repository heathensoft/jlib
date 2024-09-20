package io.github.heathensoft.jlib.common.utils;

import org.joml.Vector3f;
import org.joml.Vector4f;


import static io.github.heathensoft.jlib.common.utils.U.*;
import static java.lang.Math.min;

/**
 * sRGB space color
 * interpolation happens in linear space (hsv)
 *
 * @author Frederik Dahl
 * 25/10/2023
 */


public class Color {

    public static final int ERROR_BITS = 0xFF5E2ADC;
    public static final int WHITE_BITS = 0xFFFFFFFF;
    public static final int BLACK_BITS = 0xFF000000;

    public static Vector4f rgb_lower_proportional(Vector4f dst, float amount) {
        amount = clamp(amount);
        dst.x = lowerProportional(dst.x,amount);
        dst.y = lowerProportional(dst.y,amount);
        dst.z = lowerProportional(dst.z,amount);
        return dst;
    }

    public static Vector4f rgb_raise_proportional(Vector4f dst, float amount) {
        amount = clamp(amount);
        dst.x = raiseProportional(dst.x,amount);
        dst.y = raiseProportional(dst.y,amount);
        dst.z = raiseProportional(dst.z,amount);
        return dst;
    }

    public static Vector4f invertHSV(Vector4f dst) {
        dst.x = (dst.x + 180) % 360;
        dst.y = 1 - dst.y;
        dst.z = 1 - dst.z;
        return dst;
    }

    public static Vector4f invertRGB(Vector4f dst) {
        dst.x = 1 - dst.x;
        dst.y = 1 - dst.y;
        dst.z = 1 - dst.z;
        return dst;
    }

    public static int rgb_to_intBits(Vector4f rgb) {
        int r = (int)(rgb.x * 255) & 0xFF;
        int g = (int)(rgb.y * 255) & 0xFF;
        int b = (int)(rgb.z * 255) & 0xFF;
        int a = (int)(rgb.w * 255) & 0xFF;
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    public static int rgb_to_intBits(int r, int g, int b, int a) {
        int abgr = a & 0xFF;
        abgr = (abgr << 8) | (b & 0xFF);
        abgr = (abgr << 8) | (g & 0xFF);
        abgr = (abgr << 8) | (r & 0xFF);
        return abgr;
    }

    public static float rgb_to_floatBits(Vector4f rgb) {
        return intBits_to_floatBits(rgb_to_intBits(rgb));
    }

    public static Vector4f rgb_to_hsl(Vector4f dst) {
        float r = dst.x;
        float g = dst.y;
        float b = dst.z;
        float max = Math.max(Math.max(r, g), b);
        float min = min(min(r, g), b);
        float range = max - min;
        if (range == 0) dst.x = 0;
        else if (max == r) dst.x = (60 * (g - b) / range + 360) % 360;
        else if (max == g) dst. x = 60 * (b - r) / range + 120;
        else dst.x = 60 * (r - g) / range + 240;
        float minMax = (max + min);
        dst.z = 0.5f * minMax;
        if (dst.z < 1f) dst.y = range / (1 - abs(2 * dst.z - 1));
        else dst.y = 0;
        return dst;
    }

    public static Vector4f rgb_to_hsv(Vector4f dst) {
        float r = dst.x;
        float g = dst.y;
        float b = dst.z;
        float max = Math.max(Math.max(r, g), b);
        float min = min(min(r, g), b);
        float range = max - min;
        if (range == 0) dst.x = 0;
        else if (max == r) dst.x = (60 * (g - b) / range + 360) % 360;
        else if (max == g) dst. x = 60 * (b - r) / range + 120;
        else dst.x = 60 * (r - g) / range + 240;
        if (max > 0) dst.y = 1 - min / max;
        else dst.y = 0;
        dst.z = max;
        return dst;
    }

    public static String rgb_to_hex(Vector4f rgb) { return intBits_to_hex(rgb_to_intBits(rgb)); }

    public static Vector4f rgb_to_xyz(Vector4f dst) {
        float r = dst.x; float g = dst.y; float b = dst.z;
        if (r > 0.04045) r = (float) Math.pow( ((r + 0.055f) / 1.055f ), 2.4f); else r /= 12.92f;
        if (g > 0.04045) g = (float) Math.pow( ((g + 0.055f) / 1.055f ), 2.4f); else g /= 12.92f;
        if (b > 0.04045) b = (float) Math.pow( ((b + 0.055f) / 1.055f ), 2.4f); else b /= 12.92f;
        r *= 100; g *= 100; b *= 100;
        return dst.set(
                r * 0.4124 + g * 0.3576 + b * 0.1805,
                r * 0.2126 + g * 0.7152 + b * 0.0722,
                r * 0.0193 + g * 0.1192 + b * 0.9505,
                dst.w
        );
    }

    public static Vector4f rgb_to_lab(Vector4f dst) { return xyz_to_lab(rgb_to_xyz(dst)); }

    public static int hex_to_intBits(String hex) {
        if (hex == null || hex.isBlank()) { return ERROR_BITS; }
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        if (hex.length() < 6) { return ERROR_BITS; }
        try { int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            int a = hex.length() != 8 ? 0xFF : Integer.parseInt(hex.substring(6, 8), 16);
            return (r | (g << 8) | (b << 16) | (a << 24));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ERROR_BITS;
        }
    }

    public static Vector4f hex_to_rgb(String hex) { return hex_to_rgb(hex,new Vector4f()); }

    public static Vector4f hex_to_rgb(String hex, Vector4f dst) { return intBits_to_rgb(hex_to_intBits(hex),dst); }

    public static Vector4f hex_to_hsv(String hex, Vector4f dst) {
        return intBits_to_hsv(hex_to_intBits(hex),dst);
    }

    public static Vector4f hex_to_hsl(String hex, Vector4f dst) { return intBits_to_hsl(hex_to_intBits(hex),dst); }

    public static Vector4f intBits_to_rgb(int abgr) { return intBits_to_rgb(abgr,new Vector4f()); }
    public static Vector4f intBits_to_rgb(int abgr, Vector4f dst) {
        dst.x = rBits(abgr) / 255f;
        dst.y = gBits(abgr) / 255f;
        dst.z = bBits(abgr) / 255f;
        dst.w = aBits(abgr) / 255f;
        return dst;
    }

    public static Vector4f intBits_to_hsv(int abgr, Vector4f dst) { return rgb_to_hsv(intBits_to_rgb(abgr,dst)); }

    public static Vector4f intBits_to_hsl(int abgr, Vector4f dst) { return rgb_to_hsl(intBits_to_rgb(abgr,dst)); }

    public static Vector4f intBits_to_xyz(int abgr, Vector4f dst) { return rgb_to_xyz(intBits_to_rgb(abgr,dst)); }

    public static Vector4f intBits_to_lab(int abgr, Vector4f dst) { return xyz_to_lab(intBits_to_xyz(abgr,dst)); }

    public static float intBits_to_floatBits(int abgr) { return Float.intBitsToFloat(abgr & 0xfeffffff); }

    public static String intBits_to_hex(int abgr) {
        int r = rBits(abgr);
        int g = gBits(abgr);
        int b = bBits(abgr);
        int a = aBits(abgr);
        StringBuilder sb = new StringBuilder(8);
        String string = (Integer.toHexString((r << 24) | (g << 16) | (b << 8) | a)).toUpperCase();
        sb.append(string);
        while (sb.length() < 8) sb.insert(0,'0');
        return sb.toString();
    }

    public static Vector4f hsv_to_rgb(Vector4f dst) {
        float h = dst.x;
        float s = dst.y;
        float v = dst.z;
        float x = (h / 60f + 6) % 6;
        int i = (int)x;
        float f = x - i;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        switch (i) {
            case 0: dst.x = v; dst.y = t; dst.z = p; break;
            case 1: dst.x = q; dst.y = v; dst.z = p; break;
            case 2: dst.x = p; dst.y = v; dst.z = t; break;
            case 3: dst.x = p; dst.y = q; dst.z = v; break;
            case 4: dst.x = t; dst.y = p; dst.z = v; break;
            default:dst.x = v; dst.y = p; dst.z = q; break;
        } return dst;
    }

    public static Vector4f hsv_to_hsl(Vector4f dst) {
        float l = dst.z - dst.z * dst.y * 0.5f;
        if (l <= 0 || l >= 1) {
            dst.y = 0;
            dst.z = clamp(l);
        } else {
            dst.y = (dst.z - l) / min(l,1-l);
            dst.z = l;
        } return dst;
    }

    public static String hsv_to_hex(Vector4f hsv) {
        Vector4f tmp = U.popSetVec4(hsv);
        String hex = rgb_to_hex(hsv_to_rgb(tmp));
        U.pushVec4();
        return hex;
    }

    public static int hsv_to_intBits(Vector4f hsv) {
        Vector4f tmp = U.popSetVec4(hsv);
        int intBits = rgb_to_intBits(hsv_to_rgb(tmp));
        U.pushVec4();
        return intBits;
    }

    public static float hsv_to_floatBits(Vector4f hsv) { return intBits_to_floatBits(hsv_to_intBits(hsv)); }

    public static Vector4f hsl_to_hsv(Vector4f dst) {
        float v = dst.z + dst.y * min(dst.z,1-dst.z);
        if (v > 0) {
            dst.y = 2 - (2 * dst.z) / v;
        } else dst.y = 0;
        dst.z = v;
        return dst;
    }

    public static Vector4f hsl_to_rgb(Vector4f dst) { return hsv_to_rgb(hsl_to_hsv(dst)); }

    public static int rBits(int abgr) { return abgr & 0xFF; }

    public static int gBits(int abgr) { return (abgr >> 8) & 0xFF; }

    public static int bBits(int abgr) { return (abgr >> 16) & 0xFF; }

    public static int aBits(int abgr) { return (abgr >> 24) & 0xFF; }

    // todo: probably not correct. clear alpha first?
    public static int floatBits_to_intBits(float floatBits) {
        int abgr = Float.floatToRawIntBits(floatBits);
        abgr |= (int)((abgr >>> 24) * (255f / 254f)) << 24;
        return abgr;
    }

    public static Vector4f clamp_rgb(Vector4f dst) {
        dst.x = U.clamp(dst.x);
        dst.y = U.clamp(dst.y);
        dst.z = U.clamp(dst.z);
        dst.w = U.clamp(dst.w);
        return dst;
    }

    public static Vector4f clamp_hsl(Vector4f dst) { return clamp_hsv(dst); }

    public static Vector4f clamp_hsv(Vector4f dst) {
        dst.x = dst.x % 360;
        dst.y = U.clamp(dst.y);
        dst.z = U.clamp(dst.z);
        dst.w = U.clamp(dst.w);
        return dst;
    }

    private static final Vector4f CIE_1964_DAYLIGHT_sRGB = new Vector4f(95.047f,100.000f,108.883f,1.0f);

    public static Vector4f xyz_to_lab(Vector4f dst) {
        dst.div(CIE_1964_DAYLIGHT_sRGB);
        float x = dst.x; float y = dst.y; float z = dst.z;
        float a = 1/3f; float b = 16/116f;
        if(x > 0.008856) x = (float) Math.pow(x,a); else x = ( 7.787f * x ) + b;
        if(y > 0.008856) y = (float) Math.pow(y,a); else y = ( 7.787f * y ) + b;
        if(z > 0.008856) z = (float) Math.pow(z,a); else z = ( 7.787f * z ) + b;
        return dst.set(( 116f * y ) - 16,500f * ( x - y ),200f * ( y - z ));
    }

    public static float lab_distance(Vector4f lab1, Vector4f lab2) {
        Vector3f v1 = U.popSetVec3(lab1.x,lab1.y,lab1.z);
        Vector3f v2 = U.popSetVec3(lab2.x,lab2.y,lab2.z);
        float distance = v1.distance(v2);
        U.pushVec3(2);
        return distance;
    }

    public static Vector4f lerp(Vector4f a, Vector4f b, float t) { // Creates new object
        return lerp(a,b,t, new Vector4f());
    }

    public static Vector4f lerp(Vector4f a, Vector4f b, float t, Vector4f dst) {
        if (t <= 0) return dst.set(a);
        else if (t >= 1) return dst.set(b);
        Vector4f hsv1 = Color.rgb_to_hsv(Color.sRGB_to_linear(U.popSetVec4(a)));
        Vector4f hsv2 = Color.rgb_to_hsv(Color.sRGB_to_linear(U.popSetVec4(b)));
        Color.linear_to_sRGB(hsv_to_rgb(lerp_hsv(hsv1,hsv2,t,dst)));
        U.pushVec4(2);
        return dst;
    }

    public static Vector4f lerp_hsv(Vector4f hsv1, Vector4f hsv2, float t,  Vector4f dst) {
        if (t <= 0) { dst.set(hsv1);
        } else if (t > 1) { dst.set(hsv2);
        } else {
            dst.x = hue_lerp(hsv1.x,hsv2.x,t);
            dst.y = U.lerp(hsv1.y,hsv2.y,t);
            dst.z = U.lerp(hsv1.z,hsv2.z,t);
            dst.w = U.lerp(hsv1.w,hsv2.w,t);
        } return dst;
    }

    private static float hue_lerp(float a, float b, float t) {
        float dt = hue_repeat(b - a);
        return U.lerp(a,a + (dt > 180 ? dt - 360 : dt), t);
    }

    private static float hue_repeat(float t) { return U.clamp(t - floor(t / 360f) * 360f,0,360f); }

    public static Vector4f sRGB_to_linear(Vector4f dst) {
        dst.x = pow(dst.x,2.2f);
        dst.y = pow(dst.y,2.2f);
        dst.z = pow(dst.z,2.2f);
        return dst;
    }

    public static Vector4f linear_to_sRGB(Vector4f dst) {
        float e = 1f / 2.2f;
        dst.x = pow(dst.x,e);
        dst.y = pow(dst.y,e);
        dst.z = pow(dst.z,e);
        return dst;
    }

    public static Vector4f premultiply_alpha(Vector4f dst) {
        if (dst.w < 1) {
            dst.x *= dst.w;
            dst.y *= dst.w;
            dst.z *= dst.w;
        } return dst;
    }

    public static Vector4f unMultiply_alpha(Vector4f dst) {
        if (dst.w > 0) { float a_inv = 1f / dst.w;
            dst.x *= a_inv;
            dst.y *= a_inv;
            dst.z *= a_inv;
        } return dst;
    }

    public static Vector4f random_opaque(Vector4f dst) {
        dst.x = Rand.nextFloat();
        dst.y = Rand.nextFloat();
        dst.z = Rand.nextFloat();
        dst.w = 1.0f;
        return dst;
    }


}
