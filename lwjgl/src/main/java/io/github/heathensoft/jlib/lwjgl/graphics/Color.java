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

    /**
     * The color in Cie-L*ab format (color space)
     * Used for calculations to do with how "far apart" two colors are visually.
     * @param dest the destination for lab-color
     * @return the destination converted to lab-color
     */
    public Vector3f toLAB(Vector3f dest) {
        return toCIE_LAB(toXYZ(dest));
    }

    /**
     * D65/2° standard illuminant
     * @param rgb the (rgb normalized) destination vec3
     * @return the destination vec3
     */
    private Vector3f toXYZ(Vector3f rgb) {
        float r = this.r;
        float g = this.g;
        float b = this.b;
        if (r > 0.04045) r = (float) Math.pow( ((r + 0.055f) / 1.055f ), 2.4f);
        else r /= 12.92f;
        if (g > 0.04045) g = (float) Math.pow( ((g + 0.055f) / 1.055f ), 2.4f);
        else g /= 12.92f;
        if (b > 0.04045) b = (float) Math.pow( ((b + 0.055f) / 1.055f ), 2.4f);
        else b /= 12.92f;
        r *= 100;
        g *= 100;
        b *= 100;
        return rgb.set(
                r * 0.4124 + g * 0.3576 + b * 0.1805,
                r * 0.2126 + g * 0.7152 + b * 0.0722,
                r * 0.0193 + g * 0.1192 + b * 0.9505
        );
    }

    // reference below http://www.easyrgb.com/en/math.php#text2
    private static final Vector3f CIE_1964_DAYLIGHT_sRGB = new Vector3f(95.047f,100.000f,108.883f);

    private Vector3f toCIE_LAB(Vector3f xyz) {
        xyz.div(CIE_1964_DAYLIGHT_sRGB);
        float x = xyz.x;
        float y = xyz.y;
        float z = xyz.z;
        float a = 1/3f;
        float b = 16/116f;
        if(x > 0.008856) x = (float) Math.pow(x,a);
        else x = ( 7.787f * x ) + b;
        if(y > 0.008856) y = (float) Math.pow(y,a);
        else y = ( 7.787f * y ) + b;
        if(z > 0.008856) z = (float) Math.pow(z,a);
        else z = ( 7.787f * z ) + b;
        return xyz.set(
                ( 116f * y ) - 16,
                500f * ( x - y ),
                200f * ( y - z )
        );
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

    /**
     * Creates a 3D RGB-8 texture of size: texture_size ^3 where rgb values
     * are set to the visually most similar palette color.
     * The size of the texture should be in the power of 2.
     * You can look at the size as sampler precision.
     * A size of 64 should be good for a 256 color palette.
     * @param paletteColors colors in the palette
     * @param texture_size the dimensions you want for the sampler texture
     * @return sampler texture used to convert any rgb value to a visually similar palette color
     */
    public static Texture paletteSampler3D(List<Color> paletteColors, int texture_size) { // texture size 64
        int num_palette_colors = paletteColors.size();
        List<Vector3f> paletteLAB = new ArrayList<>(num_palette_colors);
        for (Color color : paletteColors) {
            paletteLAB.add(color.toLAB(new Vector3f()));
        } Color sampleColor = new Color();
        Color closestColor = new Color();
        Vector3f sampleRGB = new Vector3f();
        Vector3f sampleLAB = new Vector3f();
        Vector3f size = new Vector3f(texture_size);
        int bytes = texture_size * texture_size * texture_size * 3;
        ByteBuffer pixels = MemoryUtil.memAlloc(bytes);
        for (int r = 0; r < texture_size; r++) {
            for (int g = 0; g < texture_size; g++) {
                for (int b = 0; b < texture_size; b++) {
                    //int idx = 3 * ((r * S * S) + (g * S) + b);
                    sampleRGB.set(b,g,r).div(size); // invert (rgb -> bgr)
                    sampleColor.set(sampleRGB.x,sampleRGB.y,sampleRGB.z,1f);
                    sampleLAB = sampleColor.toLAB(sampleLAB);
                    float d_min = Float.MAX_VALUE;
                    for (int i = 0; i < num_palette_colors; i++) {
                        float d = paletteLAB.get(i).distance(sampleLAB);
                        if (d < d_min) {
                            closestColor.set(paletteColors.get(i));
                            d_min = d;
                        }
                    } closestColor.getRGB(pixels);
                }
            }
        }
        Texture texture = Texture.generate3D(texture_size, texture_size, texture_size);
        texture.bindToActiveSlot();
        texture.filter(GL_NEAREST,GL_NEAREST);
        texture.clampToEdge();
        texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        texture.uploadData(pixels.flip());
        MemoryUtil.memFree(pixels);
        return texture;
    }


    /*
Observer	2° (CIE 1931)	10° (CIE 1964)	Note
Illuminant	X2	Y2	Z2	X10	Y10	Z10
A	109.850	100.000	35.585	111.144	100.000	35.200	Incandescent/tungsten
B	99.0927	100.000	85.313	99.178;	100.000	84.3493	Old direct sunlight at noon
C	98.074	100.000	118.232	97.285	100.000	116.145	Old daylight
D50	96.422	100.000	82.521	96.720	100.000	81.427	ICC profile PCS
D55	95.682	100.000	92.149	95.799	100.000	90.926	Mid-morning daylight
D65	95.047	100.000	108.883	94.811	100.000	107.304	Daylight, sRGB, Adobe-RGB
D75	94.972	100.000	122.638	94.416	100.000	120.641	North sky daylight
E	100.000	100.000	100.000	100.000	100.000	100.000	Equal energy
F1	92.834	100.000	103.665	94.791	100.000	103.191	Daylight Fluorescent
F2	99.187	100.000	67.395	103.280	100.000	69.026	Cool fluorescent
F3	103.754	100.000	49.861	108.968	100.000	51.965	White Fluorescent
F4	109.147	100.000	38.813	114.961	100.000	40.963	Warm White Fluorescent
F5	90.872	100.000	98.723	93.369	100.000	98.636	Daylight Fluorescent
F6	97.309	100.000	60.191	102.148	100.000	62.074	Lite White Fluorescent
F7	95.044	100.000	108.755	95.792	100.000	107.687	Daylight fluorescent, D65 simulator
F8	96.413	100.000	82.333	97.115	100.000	81.135	Sylvania F40, D50 simulator
F9	100.365	100.000	67.868	102.116	100.000	67.826	Cool White Fluorescent
F10	96.174	100.000	81.712	99.001	100.000	83.134	Ultralume 50, Philips TL85
F11	100.966	100.000	64.370	103.866	100.000	65.627	Ultralume 40, Philips TL84
F12	108.046	100.000	39.228	111.428	100.000	40.353	Ultralume 30, Philips TL83
     */
}
