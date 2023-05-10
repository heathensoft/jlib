package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Defined;
import io.github.heathensoft.jlib.common.utils.Rand;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

/**
 *
 * 32 bit precision ABGR color value
 * For vector calculations, use Vector3f rgb()
 *
 * @author Frederik Dahl
 * 14/03/2023
 */


public class Color32 implements Defined {

    private static final Vector3f CIE_1964_DAYLIGHT_sRGB = new Vector3f(95.047f,100.000f,108.883f);
    public static final Color32 WHITE = new Color32(0xFFFFFFFF);
    public static final Color32 BLACK = new Color32(0xFF000000);
    public static final Color32 EMPTY = new Color32(0x00000000);
    public static final Color32 ERROR = new Color32(0xFFB469FF);

    private int val;

    public Color32() {
        black();
    }

    public Color32(float r, float g, float b, float a) {
        val = abgr8(r, g, b, a);
    }

    public Color32(float r, float g, float b) {
        val = abgr8(r, g, b);
    }

    public Color32(Vector3f rgb) {
        val = abgr8(rgb);
    }

    public Color32(Vector4f rgba) {
        val = abgr8(rgba);
    }

    public Color32(String hex) {
        val = abgr8(hex);
    }

    public Color32(int abgr8) {
        val = abgr8;
    }

    public Color32(Color32 color) {
        set(color.val);
    }

    public Color32 set(float r, float g, float b, float a) {
        val = abgr8(r, g, b, a);
        return this;
    }

    public Color32 set(float r, float g, float b) {
        val = abgr8(r, g, b, a());
        return this;
    }

    public Color32 set(int h, float s, float v) {
        Vector3f rgb = hsvToRgb(new Vector3f(h,s,v));
        return set(rgb.x,rgb.y,rgb.z);
    }

    public Color32 set(Vector3f rgb) {
        return set(rgb.x,rgb.y,rgb.z);
    }

    public Color32 set(Vector4f rgba) {
        val = abgr8(rgba);
        return this;
    }

    public Color32 set(String hex) {
        val = abgr8(hex);
        return this;
    }

    public Color32 set(int abgr8) {
        val = abgr8;
        return this;
    }

    public Color32 set(Color32 color) {
        return set(color.val);
    }

    public Color32 setRed(float r) {
        return setRedBits((int)(r * 255));
    }

    public Color32 setGreen(float g) {
        return setGreenBits((int)(g * 255));
    }

    public Color32 setBlue(float b) {
        return setBlueBits((int)(b * 255));
    }

    public Color32 setAlpha(float a) {
        return setAlphaBits((int)(a * 255));
    }

    public Color32 setRedBits(int r) {
        val = ((val & 0xFFFFFF00) | (r & 0xFF));
        return this;
    }

    public Color32 setGreenBits(int g) {
        val = ((val & 0xFFFF00FF) | ((g & 0xFF) << 8));
        return this;
    }

    public Color32 setBlueBits(int b) {
        val = ((val & 0xFF00FFFF) | ((b & 0xFF) << 16));
        return this;
    }

    public Color32 setAlphaBits(int a) {
        val = ((val & 0x00FFFFFF) | ((a & 0xFF) << 24));
        return this;
    }

    public Color32 zero() {
        val = 0x00000000;
        return this;
    }

    public Color32 black() {
        val = 0xFF000000;
        return this;
    }

    public Color32 white() {
        val = 0xFFFFFFFF;
        return this;
    }

    public Color32 premultiplyAlpha() {
        val = premultiplyAlpha(val);
        return this;
    }

    public float r() {
        return redBits() / 255f;
    }

    public float g() {
        return greenBits() / 255f;
    }

    public float b() {
        return blueBits() / 255f;
    }

    public float a() {
        return alphaBits() / 255f;
    }

    public float floatBits() {
        return floatBits(val);
    }

    public int intBits() {
        return val;
    }

    public int redBits() {
        return rBits(val);
    }

    public int greenBits() {
        return gBits(val);
    }

    public int blueBits() {
        return bBits(val);
    }

    public int alphaBits() {
        return bBits(val);
    }

    public Vector3f rgb(Vector3f dest) {
        return rgb(val,dest);
    }

    public Vector3f rgb() {
        return rgb(val,new Vector3f());
    }

    public Vector4f rgba(Vector4f dest) {
        return rgba(val,dest);
    }

    public Vector4f rgba() {
        return rgba(val,new Vector4f());
    }

    public Vector3f hsv(Vector3f dest) {
        return hsv(val,dest);
    }

    public Vector3f hsv() {
        return hsv(val,new Vector3f());
    }

    public Vector3f lab(Vector3f dest) {
        return rgbToLab(rgb(dest));
    }

    public Vector3f lab() {
        return rgbToLab(rgb());
    }

    public Color32 cpy() {
        return new Color32(val);
    }

    public void getRGB(FloatBuffer buffer) {
        buffer.put(r()).put(g()).put(b());
    }

    public void getRGBA(FloatBuffer buffer) {
        buffer.put(r()).put(g()).put(b()).put(a());
    }

    public void getRGB(ByteBuffer buffer) {
        buffer.put((byte) redBits());
        buffer.put((byte) greenBits());
        buffer.put((byte) blueBits());
    }

    public void getRGBA(ByteBuffer buffer) {
        buffer.putInt(val);
    }

    public void setProperties(ByteBuffer buffer) {
        val = buffer.getInt();
    }

    public void getProperties(ByteBuffer buffer) {
        buffer.putInt(val);
    }

    public int sizeOfProperties() {
        return sizeOf();
    }

    public String toString() {
        return toHex(intBits());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color32 color32 = (Color32) o;
        return val == color32.val;
    }

    public int hashCode() {
        return Objects.hash(val);
    }


    public static Vector4f rgba(int abgr8, Vector4f dest) {
        return dest.set(
                rBits(abgr8) / 255f,
                gBits(abgr8) / 255f,
                bBits(abgr8) / 255f,
                aBits(abgr8) / 255f);
    }

    public static Vector4f rgba(int abgr8) {
        return rgba(abgr8,new Vector4f());
    }

    public static Vector3f rgb(int abgr8, Vector3f dest) {
        return dest.set(
                rBits(abgr8) / 255f,
                gBits(abgr8) / 255f,
                bBits(abgr8) / 255f);
    }

    public static Vector3f rgb(int abgr8) {
        return rgb(abgr8,new Vector3f());
    }

    public static Vector3f hsv(int abgr8, Vector3f dest) {
        return rgbToHsv(rgb(abgr8,dest));
    }

    public static Vector3f hsv(int abgr8) {
        return rgbToHsv(rgb(abgr8));
    }

    public static int rgbaToAbgr(int rgba8) {
        int r = aBits(rgba8);
        int g = bBits(rgba8);
        int b = gBits(rgba8);
        int a = rBits(rgba8);
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    public static int abgrToRgba(int abgr8) {
        int r = rBits(abgr8);
        int g = gBits(abgr8);
        int b = bBits(abgr8);
        int a = aBits(abgr8);
        return a | (b << 8) | (g << 16) | (r << 24);
    }

    public static int abgr8(float r, float g, float b, float a) {
        int ri = fromNormalized(r);
        int gi = fromNormalized(g);
        int bi = fromNormalized(b);
        int ai = fromNormalized(a);
        return ri | (gi << 8) | (bi << 16) | (ai << 24);
    }

    public static int abgr8(float r, float g, float b) {
        int ri = fromNormalized(r);
        int gi = fromNormalized(g);
        int bi = fromNormalized(b);
        return ri | (gi << 8) | (bi << 16) | (0xFF << 24);
    }

    public static int abgr8(Vector4f color) {
        return abgr8(color.x, color.y, color.z, color.w);
    }

    public static int abgr8(Vector3f color) {
        return abgr8(color.x, color.y, color.z);
    }

    public static int abgr8(float floatBits) {
        int intBits = Float.floatToRawIntBits(floatBits);
        intBits |= (int)((intBits >>> 24) * (255f / 254f)) << 24;
        return intBits;
    }

    public static int abgr8(String hex) {
        if (hex == null || hex.length() == 0) return ERROR.val;
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        if (hex.length() < 6) return ERROR.val;
        try { int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            int a = hex.length() != 8 ? 0xFF : Integer.parseInt(hex.substring(6, 8), 16);
            return r | (g << 8) | (b << 16) | (a << 24);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ERROR.val;
        }
    }

    public static float floatBits(int abgr8) {
        return Float.intBitsToFloat(abgr8 & 0xfeffffff);
    }

    public static String toHex(int abgr8) {
        int r = rBits(abgr8);
        int g = gBits(abgr8);
        int b = bBits(abgr8);
        int a = aBits(abgr8);
        return (Integer.toHexString((r << 24) | (g << 16) | (b << 8) | a)).toUpperCase();
    }

    /**
     * Hue in degrees (0 to 360)
     * Saturation (0 to 1)
     * Value / Brightness (0 to 1)
     * @param dest converted from hsv to rgb
     */
    public static Vector3f hsvToRgb(Vector3f dest) {
        float h = dest.x;
        float s = dest.y;
        float v = dest.z;
        float x = (h / 60f + 6) % 6;
        int i = (int)x;
        float f = x - i;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        switch (i) {
            case 0: dest.x = v; dest.y = t; dest.z = p; break;
            case 1: dest.x = q; dest.y = v; dest.z = p; break;
            case 2: dest.x = p; dest.y = v; dest.z = t; break;
            case 3: dest.x = p; dest.y = q; dest.z = v; break;
            case 4: dest.x = t; dest.y = p; dest.z = v; break;
            default:dest.x = v; dest.y = p; dest.z = q; break;
        } return clamp(dest);
    }

    /**
     * Hue in degrees (0 to 360)
     * Saturation (0 to 1)
     * Value / Brightness (0 to 1)
     * @param dest converted from rgb to hsv
     */
    public static Vector3f rgbToHsv(Vector3f dest) {
        clamp(dest);
        float r = dest.x;
        float g = dest.y;
        float b = dest.z;
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float range = max - min;
        if (range == 0) dest.x = 0;
        else if (max == r) dest.x = (60 * (g - b) / range + 360) % 360;
        else if (max == g) dest.x = 60 * (b - r) / range + 120;
        else dest.x = 60 * (r - g) / range + 240;
        if (max > 0) dest.y = 1 - min / max;
        else dest.y = 0;
        dest.z = max;
        return dest;
    }

    public static Vector3f rgbToXyz(Vector3f dest) {
        float r = dest.x; float g = dest.y; float b = dest.z;
        if (r > 0.04045) r = (float) Math.pow( ((r + 0.055f) / 1.055f ), 2.4f); else r /= 12.92f;
        if (g > 0.04045) g = (float) Math.pow( ((g + 0.055f) / 1.055f ), 2.4f); else g /= 12.92f;
        if (b > 0.04045) b = (float) Math.pow( ((b + 0.055f) / 1.055f ), 2.4f); else b /= 12.92f;
        r *= 100; g *= 100; b *= 100;
        return dest.set(
                r * 0.4124 + g * 0.3576 + b * 0.1805,
                r * 0.2126 + g * 0.7152 + b * 0.0722,
                r * 0.0193 + g * 0.1192 + b * 0.9505
        );
    }

    public static Vector3f xyzToLab(Vector3f dest) {
        dest.div(CIE_1964_DAYLIGHT_sRGB);
        float x = dest.x; float y = dest.y; float z = dest.z;
        float a = 1/3f; float b = 16/116f;
        if(x > 0.008856) x = (float) Math.pow(x,a); else x = ( 7.787f * x ) + b;
        if(y > 0.008856) y = (float) Math.pow(y,a); else y = ( 7.787f * y ) + b;
        if(z > 0.008856) z = (float) Math.pow(z,a); else z = ( 7.787f * z ) + b;
        return dest.set(( 116f * y ) - 16,500f * ( x - y ),200f * ( y - z ));
    }

    public static Vector3f rgbToLab(Vector3f dest) {
        return xyzToLab(rgbToXyz(dest));
    }

    public static int rBits(int abgr8) {
        return abgr8 & 0xFF;
    }

    public static int gBits(int abgr8) {
        return (abgr8 >> 8) & 0xFF;
    }

    public static int bBits(int abgr8) {
        return (abgr8 >> 16) & 0xFF;
    }

    public static int aBits(int abgr8) {
        return (abgr8 >> 24) & 0xFF;
    }

    public static int premultiplyAlpha(int abgr8) {
        float r = rBits(abgr8) / 255f;
        float g = gBits(abgr8) / 255f;
        float b = bBits(abgr8) / 255f;
        float a = aBits(abgr8) / 255f;
        return abgr8(r*a,g*a,b*a,a);
    }

    public static Vector4f premultiplyAlpha(Vector4f color) {
        color.x *= color.w;
        color.y *= color.w;
        color.z *= color.w;
        return color;
    }

    public static Vector3f clamp(Vector3f color) {
        if (color.x < 0) color.x = 0;
        else if (color.x > 1) color.x = 1;
        if (color.y < 0) color.y = 0;
        else if (color.y > 1) color.y = 1;
        if (color.z < 0) color.z = 0;
        else if (color.z > 1) color.z = 1;
        return color;
    }

    public static Vector4f clamp(Vector4f color) {
        if (color.x < 0) color.x = 0;
        else if (color.x > 1) color.x = 1;
        if (color.y < 0) color.y = 0;
        else if (color.y > 1) color.y = 1;
        if (color.z < 0) color.z = 0;
        else if (color.z > 1) color.z = 1;
        if (color.w < 0) color.w = 0;
        else if (color.w > 1) color.w = 1;
        return color;
    }


    public static Color32 randomColor() {
        return new Color32(
                Rand.nextFloat(),
                Rand.nextFloat(),
                Rand.nextFloat()
        );
    }

    public static Color32[] randomColors(int n) {
        Color32[] result = new Color32[n];
        for (int i = 0; i < n; i++) {
            result[i] = randomColor();
        } return result;
    }

    public static int sizeOf() {
        return 4;
    }

    private static int fromNormalized(float component) {
        return ((int)(component * 255) & 0xFF );
    }


}
