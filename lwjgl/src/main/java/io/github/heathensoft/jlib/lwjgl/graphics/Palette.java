package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

/**
 *
 * // http://www.easyrgb.com/en/math.php#text2
 *
 * @author Frederik Dahl
 * 04/02/2023
 */


public class Palette implements Disposable {

    private static final int DEFAULT_TEXTURE_SIZE = 64;
    private static final String DEFAULT_NAME = "Untitled Palette";
    private static final Vector3f CIE_1964_DAYLIGHT_sRGB = new Vector3f(95.047f,100.000f,108.883f);

    private final String name;
    private final Texture texture;

    public Palette(List<Color> colors, String name, int textureSize) {
        if (colors.isEmpty()) colors.add(Color.ERROR.cpy());
        this.texture = createPalette(colors,Math.max(1,textureSize));
        this.name = name == null || name.isBlank() ? DEFAULT_NAME : name;
    }

    public Palette(List<Color> colors, String name) {
        this(colors,name,DEFAULT_TEXTURE_SIZE);
    }

    public Palette(List<Color> colors) {
        this(colors,DEFAULT_NAME);
    }

    public Texture texture() {
        return texture;
    }

    public String name() {
        return name;
    }

    public void dispose() {
        Disposable.dispose(texture);
    }

    private Texture createPalette(List<Color> paletteColors, int texture_size) {
        int num_palette_colors = paletteColors.size();
        List<Vector3f> paletteLAB = new ArrayList<>(num_palette_colors);
        for (Color color : paletteColors) {
            paletteLAB.add(toLAB(color,new Vector3f()));
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
                    sampleRGB.set(b,g,r).div(size); // invert (rgb -> bgr)
                    sampleColor.set(sampleRGB.x,sampleRGB.y,sampleRGB.z,1f);
                    toLAB(sampleColor,sampleLAB);
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
        Texture.unbindActiveSlot(texture.target());
        return texture;
    }

    private Vector3f toLAB(Color color, Vector3f dest) {
        return toCIE_LAB(toXYZ(color, dest));
    }

    private Vector3f toXYZ(Color color, Vector3f dest) {
        float r = color.r; float g = color.g; float b = color.b;
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

    private Vector3f toCIE_LAB(Vector3f xyz) {
        xyz.div(CIE_1964_DAYLIGHT_sRGB);
        float x = xyz.x; float y = xyz.y; float z = xyz.z;
        float a = 1/3f; float b = 16/116f;
        if(x > 0.008856) x = (float) Math.pow(x,a); else x = ( 7.787f * x ) + b;
        if(y > 0.008856) y = (float) Math.pow(y,a); else y = ( 7.787f * y ) + b;
        if(z > 0.008856) z = (float) Math.pow(z,a); else z = ( 7.787f * z ) + b;
        return xyz.set(( 116f * y ) - 16,500f * ( x - y ),200f * ( y - z ));
    }

    public static Palette brightFuture() {
        String[] lines = brightFutureHex().split("\\R");
        List<Color> colors = new ArrayList<>(lines.length);
        for (String s : lines) colors.add(Color.valueOf(s));
        return new Palette(colors,"Bright Future");
    }

    private static String brightFutureHex() {
        return """
                210226
                59044d
                8c0e54
                a62144
                ba3c38
                cc7c52
                d9b882
                e5e0ac
                e9f2ce
                f7fff2
                170e19
                3b2137
                603a4f
                774f59
                8c6665
                a1897c
                bab0a0
                d1cfc0
                e2e5da
                f8faf6
                260215
                59041a
                8c130e
                a64e21
                ba8a38
                ccc552
                c6d982
                c8e5ac
                d3f2ce
                f2fff5
                190e14
                3b2128
                603b3a
                775c4f
                8c7e65
                a19f7c
                b4baa0
                c8d1c0
                dce5da
                f6faf7
                260502
                592104
                8c5e0e
                a69d21
                9cba38
                8acc52
                92d982
                ace5b2
                cef2df
                f2fffd
                190f0e
                3b2a21
                60523a
                77744f
                838c65
                8da17c
                a5baa0
                c0d1c2
                dae5df
                f6faf9
                261a02
                595404
                6e8c0e
                5fa621
                4eba38
                52cc63
                82d9a6
                ace5d4
                ceeff2
                f2f9ff
                19150e
                3b3921
                57603a
                62774f
                6c8c65
                7ca181
                a0baab
                c0d1cc
                dae4e5
                f6f8fa
                1c2602
                2b5904
                228c0e
                21a633
                38ba70
                52ccac
                82d7d9
                acd4e5
                ced9f2
                f3f2ff
                16190e
                2d3b21
                40603a
                4f7754
                658c76
                7ca197
                a0b9ba
                c0ccd1
                dadde5
                f6f6fa
                082602
                045910
                0e8c45
                21a682
                38b6ba
                52a3cc
                82a2d9
                acb0e5
                d6cef2
                f9f2ff
                10190e
                213b25
                3a604a
                4f776c
                658b8c
                7c94a1
                a0aaba
                c0c1d1
                dcdae5
                f8f6fa
                022611
                045943
                0e878c
                217aa6
                3868ba
                525acc
                9682d9
                caace5
                eccef2
                fff2fd
                0e1913
                213b34
                3a5e60
                4f6a77
                65738c
                7c7ea1
                a6a0ba
                c9c0d1
                e3dae5
                faf6f9
                022526
                043c59
                0e3b8c
                212aa6
                5638ba
                9352cc
                ca82d9
                e5acde
                f2cee2
                fff2f5
                0e1919
                21323b
                3a4860
                4f5277
                6e658c
                907ca1
                b5a0ba
                d1c0cf
                e5dae0
                faf6f7
                020f26
                040959
                2d0e8c
                6821a6
                a438ba
                cc52bb
                d982b4
                e5acbc
                f2d0ce
                fff7f2
                0e1219
                21233b
                433a60
                644f77
                85658c
                a17c9c
                baa0af
                d1c0c5
                e5dbda
                faf8f6
                0b0226
                320459
                780e8c
                a62194
                ba3882
                cc5271
                d98482
                e5beac
                f2e5ce
                fffff2
                110e19
                2f213b
                5a3a60
                774f71
                8c657b
                a17c85
                baa1a0
                d1c5c0
                e5e1da
                fafaf6""";
    }
}
