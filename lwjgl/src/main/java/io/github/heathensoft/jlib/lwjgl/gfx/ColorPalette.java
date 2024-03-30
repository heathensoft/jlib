package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.lwjgl.window.GLContext;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static io.github.heathensoft.jlib.common.utils.Color.*;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 * @author Frederik Dahl
 * 28/10/2023
 */


public class ColorPalette implements Iterable<Vector4f>{


    private final List<Vector4f> colors;

    public ColorPalette(List<Vector4f> colors) { this.colors = colors; }

    public int[] intBits() {
        int[] array = new int[colors.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = rgb_to_intBits(colors.get(i));
        } return array;
    }

    public Bitmap bitmap() {
        if (colors.isEmpty()) {
            Vector4f error = intBits_to_rgb(ERROR_BITS,new Vector4f());
            colors.add(error);
        } int width = colors.size();
        int size = Integer.BYTES * width;
        ByteBuffer buffer;
        buffer = MemoryUtil.memAlloc(size);
        for (Vector4f color : colors) {
            buffer.putInt(rgb_to_intBits(color));
        } buffer.flip();
        return new Bitmap(buffer,width,1,4);
    }

    public Texture texture1D(int min_filter, int mag_filter, boolean repeat) {
        Bitmap bitmap = bitmap();
        Texture texture = Texture.generate1D(bitmap.width());
        texture.bindToActiveSlot();
        texture.filter(min_filter, mag_filter);
        if (repeat) texture.textureWrapS(GL_REPEAT);
        else texture.clampToEdge();
        texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        texture.uploadData(bitmap.pixels());
        bitmap.dispose();
        GLContext.checkError();
        return texture;
    }

    public Texture texture3D(int texture_size, int min_filter, int mag_filter) {
        Texture texture = Texture.generate3D(texture_size, texture_size, texture_size);
        texture.bindToActiveSlot();
        texture.filter(min_filter, mag_filter);
        texture.clampToEdge();
        texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        int num_pixels = texture_size * texture_size * texture_size;
        int bytes = num_pixels * Integer.BYTES;
        ByteBuffer buffer = MemoryUtil.memAlloc(bytes);
        if (colors.isEmpty()) {
            Logger.warn("Creating Texture of empty palette");
            for (int i = 0; i < num_pixels; i++) { buffer.putInt(ERROR_BITS); }
        } else {
            int num_palette_colors = colors.size();
            List<Vector4f> paletteLAB = new ArrayList<>(num_palette_colors);
            for (Vector4f color : colors) {
                Vector4f lab = rgb_to_lab(new Vector4f(color));
                paletteLAB.add(lab);
            }
            Vector4f closestColor = new Vector4f();
            Vector4f size = new Vector4f();
            size.set(texture_size,texture_size,texture_size,1.0f);
            Vector4f sampleLAB = new Vector4f(0,0,0,1);
            for (int r = 0; r < texture_size; r++) {
                for (int g = 0; g < texture_size; g++) {
                    for (int b = 0; b < texture_size; b++) {
                        sampleLAB.set(b, g, r).div(size);
                        rgb_to_lab(sampleLAB);
                        float d_min = Float.MAX_VALUE;
                        for (int i = 0; i < num_palette_colors; i++) {
                            float d = lab_distance(paletteLAB.get(i),sampleLAB);
                            if (d < d_min) {
                                closestColor.set(colors.get(i));
                                d_min = d;
                            }
                        }
                        buffer.putInt(rgb_to_intBits(closestColor));
                    }
                }
            }
        }
        texture.uploadData(buffer.flip());
        MemoryUtil.memFree(buffer);
        GLContext.checkError();
        return texture;
    }


    public List<Vector4f> list() { return colors; }

    public Iterator<Vector4f> iterator() { return colors.iterator(); }

    public static ColorPalette ramp(String hex1, String hex2, int samples) {
        samples = Math.max(2,samples);
        Vector4f a = hex_to_rgb(hex1,new Vector4f());
        Vector4f b = hex_to_rgb(hex2,new Vector4f());
        List<Vector4f> colors = new ArrayList<>(samples);
        for (int i = 0; i < samples; i++) {
            float t = (float) i / (samples - 1);
            colors.add(lerp(a,b,t,new Vector4f()));
        } return new ColorPalette(colors);
    }

    public static ColorPalette ramp(int abgr1, int abgr2, int samples) {
        samples = Math.max(2,samples);
        Vector4f a = intBits_to_rgb(abgr1,new Vector4f());
        Vector4f b = intBits_to_rgb(abgr2,new Vector4f());
        List<Vector4f> colors = new ArrayList<>(samples);
        for (int i = 0; i < samples; i++) {
            float t = (float) i / (samples - 1);
            colors.add(lerp(a,b,t,new Vector4f()));
        } return new ColorPalette(colors);
    }

    public static ColorPalette ramp(Vector4f rgb1, Vector4f rgb2, int samples) {
        samples = Math.max(2,samples);
        Vector4f a = new Vector4f(rgb1);
        Vector4f b = new Vector4f(rgb2);
        List<Vector4f> colors = new ArrayList<>(samples);
        for (int i = 0; i < samples; i++) {
            float t = (float) i / (samples - 1);
            colors.add(lerp(a,b,t,new Vector4f()));
        } return new ColorPalette(colors);
    }
}
