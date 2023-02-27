package io.github.heathensoft.jlib.lwjgl.graphics;


import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

/**
 * @author Frederik Dahl
 * 30/06/2022
 */


public class Color {


    public static final Color ERROR = Color.valueOf("FF69B4FF");
    public static final Color EMPTY = new Color(0x00000000);
    public static final Color BLACK = new Color(0x000000FF);
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color RED = new Color(0xFF0000FF);
    public static final Color GREEN = new Color(0x00FF00FF);
    public static final Color BLUE = new Color(0x0000FFFF);
    public static final Color TMP = new Color();

    public float r, g, b, a;
    
    public Color() { }
    
    public Color(int rgba8888) {
        rgba8888ToColor(this, rgba8888);
    }
    
    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        clamp();
    }
    
    public Color(Color color) {
        set(color);
    }
    
    public Color set (Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        return this;
    }
    
    public Color set (float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return clamp();
    }
    
    public Color set (int rgba) {
        rgba8888ToColor(this, rgba);
        return this;
    }
    
    public Color mul (Color color) {
        this.r *= color.r;
        this.g *= color.g;
        this.b *= color.b;
        this.a *= color.a;
        return clamp();
    }
    
    public Color mul (float value) {
        this.r *= value;
        this.g *= value;
        this.b *= value;
        this.a *= value;
        return clamp();
    }
    
    public Color add (Color color) {
        this.r += color.r;
        this.g += color.g;
        this.b += color.b;
        this.a += color.a;
        return clamp();
    }
    
    public Color sub (Color color) {
        this.r -= color.r;
        this.g -= color.g;
        this.b -= color.b;
        this.a -= color.a;
        return clamp();
    }
    
    public Color clamp () {
        if (r < 0) r = 0;
        else if (r > 1) r = 1;
        if (g < 0) g = 0;
        else if (g > 1) g = 1;
        if (b < 0) b = 0;
        else if (b > 1) b = 1;
        if (a < 0) a = 0;
        else if (a > 1) a = 1;
        return this;
    }
    
    public Color lerp (final Color target, final float t) {
        this.r += t * (target.r - this.r);
        this.g += t * (target.g - this.g);
        this.b += t * (target.b - this.b);
        this.a += t * (target.a - this.a);
        return clamp();
    }
    
    public Color premultiplyAlpha () {
        r *= a;
        g *= a;
        b *= a;
        return this;
    }

    public void getRGBA(FloatBuffer buffer) {
        buffer.put(r).put(g).put(b).put(a);
    }

    public void getRGB(FloatBuffer buffer) {
        buffer.put(r).put(g).put(b);
    }

    public void getRGBA(ByteBuffer buffer) {
        int r = (int) (255 * this.r);
        int g = (int) (255 * this.g);
        int b = (int) (255 * this.b);
        int a = (int) (255 * this.a);
        buffer.put((byte)(r & 0xFF));
        buffer.put((byte)(g & 0xFF));
        buffer.put((byte)(b & 0xFF));
        buffer.put((byte)(a & 0xFF));
    }

    public void getRGB(ByteBuffer buffer) {
        int r = (int) (255 * this.r);
        int g = (int) (255 * this.g);
        int b = (int) (255 * this.b);
        buffer.put((byte)(r & 0xFF));
        buffer.put((byte)(g & 0xFF));
        buffer.put((byte)(b & 0xFF));
    }
    
    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color)o;
        return toIntBits() == color.toIntBits();
    }
    
    @Override
    public int hashCode () {
        int result = (r != +0.0f ? Float.floatToIntBits(r) : 0);
        result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
        result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
        result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
        return result;
    }
    
    /** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float. Alpha is compressed
     * from 0-255 to use only even numbers between 0-254 to avoid using float bits in the NaN range (see
     * {@link #intToFloatColor(int)}). Converting a color to a float and back can be lossy for alpha.
     * @return the packed color as a 32-bit float */
    public float toFloatBits () {
        return intToFloatColor(toIntBits());
    }
    
    /** Packs the color components into a 32-bit integer with the format ABGR.
     * @return the packed color as a 32-bit int. */
    public int toIntBits () {
        return ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
    }

    /** Returns the color encoded as hex string with the format RRGGBBAA. */
    public String toString () {
        String value = Integer
                .toHexString(((int)(255 * r) << 24) | ((int)(255 * g) << 16) | ((int)(255 * b) << 8) | ((int)(255 * a)));
        while (value.length() < 8)
            value = "0" + value;
        return value;
    }
    
    /** Returns a new color from a hex string with the format RRGGBBAA.
     * @see #toString() */
    public static Color valueOf (String hex) {
        return valueOf(hex, new Color());
    }
    
    /** Sets the specified color from a hex string with the format RRGGBBAA.
     * @see #toString() */
    public static Color valueOf (String hex, Color color) {
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        if (hex.length() < 6) return color.set(Color.WHITE);
        color.r = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
        color.g = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
        color.b = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
        color.a = hex.length() != 8 ? 1 : Integer.parseInt(hex.substring(6, 8), 16) / 255f;
        return color;
    }
    
    /** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float. Note that no range
     * checking is performed for higher performance.
     * @param r the red component, 0 - 255
     * @param g the green component, 0 - 255
     * @param b the blue component, 0 - 255
     * @param a the alpha component, 0 - 255
     * @return the packed color as a float
     * @see #intToFloatColor(int) */
    public static float toFloatBits (int r, int g, int b, int a) {
        int color = (a << 24) | (b << 16) | (g << 8) | r;
        return intToFloatColor(color);
    }
    
    /** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
     * @return the packed color as a 32-bit float
     * @see #intToFloatColor(int) */
    public static float toFloatBits (float r, float g, float b, float a) {
        int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
        return intToFloatColor(color);
    }
    
    /** Packs the color components into a 32-bit integer with the format ABGR. Note that no range checking is performed for higher
     * performance.
     * @param r the red component, 0 - 255
     * @param g the green component, 0 - 255
     * @param b the blue component, 0 - 255
     * @param a the alpha component, 0 - 255
     * @return the packed color as a 32-bit int */
    public static int toIntBits (int r, int g, int b, int a) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
    
    public static int alpha (float alpha) {
        return (int)(alpha * 255.0f);
    }
    
    public static int luminanceAlpha (float luminance, float alpha) {
        return ((int)(luminance * 255.0f) << 8) | (int)(alpha * 255);
    }
    
    public static int rgb565 (float r, float g, float b) {
        return ((int)(r * 31) << 11) | ((int)(g * 63) << 5) | (int)(b * 31);
    }
    
    public static int rgba4444 (float r, float g, float b, float a) {
        return ((int)(r * 15) << 12) | ((int)(g * 15) << 8) | ((int)(b * 15) << 4) | (int)(a * 15);
    }
    
    public static int rgb888 (float r, float g, float b) {
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
    
    public static int rgba8888 (float r, float g, float b, float a) {
        return ((int)(r * 255) << 24) | ((int)(g * 255) << 16) | ((int)(b * 255) << 8) | (int)(a * 255);
    }
    
    public static int argb8888 (float a, float r, float g, float b) {
        return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
    
    public static int rgb565 (Color color) {
        return ((int)(color.r * 31) << 11) | ((int)(color.g * 63) << 5) | (int)(color.b * 31);
    }
    
    public static int rgba4444 (Color color) {
        return ((int)(color.r * 15) << 12) | ((int)(color.g * 15) << 8) | ((int)(color.b * 15) << 4) | (int)(color.a * 15);
    }
    
    public static int rgb888 (Color color) {
        return ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
    }
    
    public static int rgba8888 (Color color) {
        return ((int)(color.r * 255) << 24) | ((int)(color.g * 255) << 16) | ((int)(color.b * 255) << 8) | (int)(color.a * 255);
    }
    
    public static int argb8888 (Color color) {
        return ((int)(color.a * 255) << 24) | ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
    }
    
    /** Sets the Color components using the specified integer value in the format RGB565. This is inverse to the rgb565(r, g, b)
     * method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGB565 format. */
    public static void rgb565ToColor (Color color, int value) {
        color.r = ((value & 0x0000F800) >>> 11) / 31f;
        color.g = ((value & 0x000007E0) >>> 5) / 63f;
        color.b = ((value & 0x0000001F)) / 31f;
    }
    
    /** Sets the Color components using the specified integer value in the format RGBA4444. This is inverse to the rgba4444(r, g,
     * b, a) method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGBA4444 format. */
    public static void rgba4444ToColor (Color color, int value) {
        color.r = ((value & 0x0000f000) >>> 12) / 15f;
        color.g = ((value & 0x00000f00) >>> 8) / 15f;
        color.b = ((value & 0x000000f0) >>> 4) / 15f;
        color.a = ((value & 0x0000000f)) / 15f;
    }
    
    /** Sets the Color components using the specified integer value in the format RGB888. This is inverse to the rgb888(r, g, b)
     * method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGB888 format. */
    public static void rgb888ToColor (Color color, int value) {
        color.r = ((value & 0x00ff0000) >>> 16) / 255f;
        color.g = ((value & 0x0000ff00) >>> 8) / 255f;
        color.b = ((value & 0x000000ff)) / 255f;
    }
    
    /** Sets the Color components using the specified integer value in the format RGBA8888. This is inverse to the rgba8888(r, g,
     * b, a) method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGBA8888 format. */
    public static void rgba8888ToColor (Color color, int value) {
        color.r = ((value & 0xff000000) >>> 24) / 255f;
        color.g = ((value & 0x00ff0000) >>> 16) / 255f;
        color.b = ((value & 0x0000ff00) >>> 8) / 255f;
        color.a = ((value & 0x000000ff)) / 255f;
    }
    
    /** Sets the Color components using the specified integer value in the format ARGB8888. This is the inverse to the argb8888(a,
     * r, g, b) method
     *
     * @param color The Color to be modified.
     * @param value An integer color value in ARGB8888 format. */
    public static void argb8888ToColor (Color color, int value) {
        color.a = ((value & 0xff000000) >>> 24) / 255f;
        color.r = ((value & 0x00ff0000) >>> 16) / 255f;
        color.g = ((value & 0x0000ff00) >>> 8) / 255f;
        color.b = ((value & 0x000000ff)) / 255f;
    }
    
    /** Sets the Color components using the specified integer value in the format ABGR8888.
     * @param color The Color to be modified. */
    public static void abgr8888ToColor (Color color, int value) {
        color.a = ((value & 0xff000000) >>> 24) / 255f;
        color.b = ((value & 0x00ff0000) >>> 16) / 255f;
        color.g = ((value & 0x0000ff00) >>> 8) / 255f;
        color.r = ((value & 0x000000ff)) / 255f;
    }
    
    /** Sets the Color components using the specified float value in the format ABGR8888.
     * @param color The Color to be modified. */
    public static void abgr8888ToColor (Color color, float value) {
        int c = floatToIntColor(value);
        color.a = ((c & 0xff000000) >>> 24) / 255f;
        color.b = ((c & 0x00ff0000) >>> 16) / 255f;
        color.g = ((c & 0x0000ff00) >>> 8) / 255f;
        color.r = ((c & 0x000000ff)) / 255f;
    }
    
    /** Sets the RGB Color components using the specified Hue-Saturation-Value. Note that HSV components are voluntary not clamped
     * to preserve high range color and can range beyond typical values.
     * @param h The Hue in degree from 0 to 360
     * @param s The Saturation from 0 to 1
     * @param v The Value (brightness) from 0 to 1
     * @return The modified Color for chaining. */
    public Color fromHsv (float h, float s, float v) {
        float x = (h / 60f + 6) % 6;
        int i = (int)x;
        float f = x - i;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        switch (i) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default:r = v; g = p; b = q;
        } return clamp();
    }
    
    /** Sets RGB components using the specified Hue-Saturation-Value. This is a convenient method for
     * {@link #fromHsv(float, float, float)}. This is the inverse of {@link #toHsv(float[])}.
     * @param hsv The Hue, Saturation and Value components in that order.
     * @return The modified Color for chaining. */
    public Color fromHsv (float[] hsv) {
        return fromHsv(hsv[0], hsv[1], hsv[2]);
    }
    
    /** Extract Hue-Saturation-Value. This is the inverse of {@link #fromHsv(float[])}.
     * @param hsv The HSV array to be modified.
     * @return HSV components for chaining. */
    public float[] toHsv (float[] hsv) {
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float range = max - min;
        if (range == 0) hsv[0] = 0;
        else if (max == r) hsv[0] = (60 * (g - b) / range + 360) % 360;
        else if (max == g) hsv[0] = 60 * (b - r) / range + 120;
        else hsv[0] = 60 * (r - g) / range + 240;
        if (max > 0) hsv[1] = 1 - min / max;
        else hsv[1] = 0;
        hsv[2] = max;
        return hsv;
    }
    
    /** Converts the color from a float ABGR encoding to an int ABGR encoding. The alpha is expanded from 0-254 in the float
     * encoding (see intToFloatColor) to 0-255, which means converting from int to float and back to int can be
     * lossy. */
    public static int floatToIntColor (float value) {
        int intBits = Float.floatToRawIntBits(value);
        intBits |= (int)((intBits >>> 24) * (255f / 254f)) << 24;
        return intBits;
    }
    
    /** Encodes the ABGR int color as a float. The alpha is compressed to use only even numbers between 0-254 to avoid using bits
     * in the NaN range (see {@link Float#intBitsToFloat(int)} javadocs). Rendering which uses colors encoded as floats should
     * expand the 0-254 back to 0-255, else colors cannot be fully opaque. */
    public static float intToFloatColor (int value) {
        return Float.intBitsToFloat(value & 0xfeffffff);
    }
    
    public Color cpy () {
        return new Color(this);
    }

    
}
