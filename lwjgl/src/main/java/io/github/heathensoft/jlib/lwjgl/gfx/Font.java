package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;

/**
 * @author Frederik Dahl
 * 11/10/2023
 */


public class Font {

    public final ByteBuffer png;
    public final ByteBuffer metrics;

    public Font(ByteBuffer png, ByteBuffer metrics) {
        this.metrics = metrics;
        this.png = png;
    }

    public String metricsString() {
        return new String(metrics.array());
    }

    public Bitmap toBitmap() throws Exception { return new Bitmap(png); }

    public static Font create(String name, ByteBuffer ttf, int size, int spacing, float protrusion) throws Exception {
        return create(name, ttf, size, spacing, protrusion, 0);
    }

    public static Font create(String name, ByteBuffer ttf, int size, int spacing, float protrusion, float noise) throws Exception {
        return create(name, ttf, size, spacing, protrusion, noise, GL_LINEAR, GL_LINEAR);
    }

    public static Font create(String name, ByteBuffer ttf, int size, int spacing, float protrusion, float noise, int minFilter, int magFilter) throws Exception {
        return create(name, ttf, size, spacing, protrusion, noise,minFilter, magFilter, false);
    }

    public static Font create(String name, ByteBuffer ttf, int size, int spacing, float protrusion, float noise, int minFilter, int magFilter, boolean mipMap) throws Exception {
        name = name == null || name.isBlank() ? "UnnamedFont" : name;
        name = name.trim();
        size = Math.max(1,size);
        spacing = Math.max(0,spacing);
        noise = clamp(noise);
        protrusion = clamp(protrusion,-1.0f,1.0f);
        STBTTFontinfo info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info,ttf)) {
            throw new Exception("Failed to extract font information");
        } float scale = stbtt_ScaleForPixelHeight(info,size);
        float ascent, descent, lineGap;
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer Ascent  = stack.mallocInt(1);
            IntBuffer Descent = stack.mallocInt(1);
            IntBuffer LineGap = stack.mallocInt(1);
            stbtt_GetFontVMetrics(info, Ascent, Descent, LineGap);
            ascent = Ascent.get(0) * scale;
            descent = -Descent.get(0) * scale;
            lineGap = LineGap.get(0) * scale;
        } Map<Character, GlyphMetrics> map = extractGlyphs(info,scale,ascent);
        int num_glyphs = map.size();
        boolean monospaced = true;
        float maxAdvance = 0;
        float avgAdvance = 0;
        GlyphMetrics space = map.get(' ');
        for (var entry : map.entrySet()) {
            GlyphMetrics glyph = entry.getValue();
            avgAdvance += glyph.advance;
            maxAdvance = Math.max(glyph.advance,maxAdvance);
            if (monospaced) {
                if (glyph.advance != space.advance) {
                    monospaced = false;
                }
            }
        } avgAdvance /= map.size();
        IntBuffer rectangles = IntBuffer.allocate(map.size() * 5);
        IntBuffer bounds = IntBuffer.allocate(2);
        for (var entry : map.entrySet()) {
            GlyphMetrics glyph = entry.getValue();
            int id = glyph.character;
            int width = glyph.width + spacing;
            int height = glyph.height + spacing;
            rectangles.put(id).put(width).put(height);
        } RectPacker.pack(rectangles.flip(),bounds);
        int image_width = bounds.get(0);
        int image_height = bounds.get(1);
        for (int i = 0; i < num_glyphs; i++) {
            int id = rectangles.get();
            int w = rectangles.get();
            int h = rectangles.get();
            int x = rectangles.get();
            int y = rectangles.get();
            GlyphMetrics metrics = map.get((char)id);
            if (metrics != null) {
                metrics.x = x; // + spacing (unnecessary)
                metrics.y = y; // + spacing (unnecessary)
            }
        }
        StringBuilder builder = new StringBuilder(8 * 1024);
        builder.append("# https://github.com/heathensoft\n");
        builder.append("# Font: <name> <size> <characters> <width> <height> <protrusion> <noise> <maxAdvance> <avgAdvance> <ascent> <descent> <lineGap> <monospaced>\n");
        builder.append("# Texture: <minFilter> <magFilter> <textureWrap> <mipMap>\n");
        builder.append("# Glyph: <ascii_value> <x> <y> <width> <height> <xOff> <yOff> <advance>\n\n");
        builder.append("F ");
        builder.append(name).append(' ');
        builder.append(size).append(' ');
        builder.append(num_glyphs).append(' ');
        builder.append(image_width).append(' ');
        builder.append(image_height).append(' ');
        builder.append(protrusion).append(' ');
        builder.append(noise).append(' ');
        builder.append(maxAdvance).append(' ');
        builder.append(avgAdvance).append(' ');
        builder.append(ascent).append(' ');
        builder.append(descent).append(' ');
        builder.append(lineGap).append(' ');
        builder.append(monospaced ? 1 : 0).append('\n');
        builder.append("T ");
        builder.append(minFilter).append(' ');
        builder.append(magFilter).append(' ');
        builder.append(GL_CLAMP_TO_EDGE).append(' ');
        builder.append(mipMap ? 1 : 0).append("\n\n");

        ByteBuffer image = MemoryUtil.memCalloc(image_width * image_height);
        for (char c = 32; c < 127; c++) {
            GlyphMetrics glyphMetrics = map.get(c);
            if (glyphMetrics != null) {
                builder.append("G ");
                builder.append((int) glyphMetrics.character).append(' ');
                builder.append(glyphMetrics.x).append(' ');
                builder.append(glyphMetrics.y).append(' ');
                builder.append(glyphMetrics.width).append(' ');
                builder.append(glyphMetrics.height).append(' ');
                builder.append(glyphMetrics.xOff).append(' ');
                builder.append(glyphMetrics.yOff).append(' ');
                builder.append(glyphMetrics.advance).append('\n');
                ByteBuffer bm = glyphMetrics.bitmap;
                for (int ly = 0; ly < glyphMetrics.height; ly++) {
                    for (int lx = 0; lx < glyphMetrics.width; lx++) {
                        int x = glyphMetrics.x + lx;
                        int y = glyphMetrics.y + ly;
                        byte color = bm.get(ly * glyphMetrics.width + lx);
                        image.put(y * image_width + x,color);
                    }
                } glyphMetrics.dispose();
            }
        }
        Bitmap bitmap = new Bitmap(image,image_width,image_height,1);
        ByteBuffer png = bitmap.compress(); bitmap.dispose();
        byte[] string = builder.toString().getBytes();
        return new Font(png,ByteBuffer.wrap(string));
    }

    private static Map<Character, GlyphMetrics> extractGlyphs(STBTTFontinfo info, float scale, float fontAscent) throws Exception {
        int numCharacters = 127 - 32; // 95
        Map<Character, GlyphMetrics> map = new HashMap<>((int) (numCharacters * 1.75f));
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer stb_width  = stack.mallocInt(1);
            IntBuffer stb_height  = stack.mallocInt(1);
            IntBuffer stb_offset_x  = stack.mallocInt(1);
            IntBuffer stb_offset_y  = stack.mallocInt(1);
            IntBuffer stb_advance  = stack.mallocInt(1);
            IntBuffer stb_bearing  = stack.mallocInt(1);
            {
                // SPACE CHARACTER
                char space = 32;
                ByteBuffer bitmap = stbtt_GetCodepointBitmap(info,0,scale,space,stb_width,stb_height,stb_offset_x,stb_offset_y);
                stbtt_GetCodepointHMetrics(info,space,stb_advance,stb_bearing);
                GlyphMetrics glyph = new GlyphMetrics();
                glyph.character = space;
                if (bitmap == null) {
                    glyph.stb_allocated = false;
                    glyph.xOff = 0;
                    glyph.yOff = 0;
                    glyph.advance = stb_advance.get(0) * scale;
                    glyph.width = (int) glyph.advance;
                    glyph.height = (int) fontAscent;
                    if (glyph.width == 0 || glyph.height == 0) {
                        throw new Exception("Unable to extract the space character from font");
                    } glyph.bitmap = MemoryUtil.memCalloc(glyph.width * glyph.height);
                } else { glyph.stb_allocated = true;
                    glyph.height = stb_height.get(0);
                    glyph.width = stb_width.get(0);
                    glyph.xOff = stb_bearing.get(0) * scale;
                    glyph.yOff = - glyph.height - stb_offset_y.get(0);
                    glyph.advance = stb_advance.get(0) * scale;
                    glyph.bitmap = bitmap;
                } map.put(space,glyph);
            }
            for (char c = 32 + 1; c < 127; c++) {
                ByteBuffer bitmap = stbtt_GetCodepointBitmap(info,0,scale,c,stb_width,stb_height,stb_offset_x,stb_offset_y);
                // ByteBuffer bitmap = stbtt_GetCodepointSDF(info,scale,c,3,(byte) 128,64,stb_width,stb_height,stb_offset_x,stb_offset_y);
                if (bitmap != null) {
                    stbtt_GetCodepointHMetrics(info, c, stb_advance, stb_bearing);
                    GlyphMetrics glyph = new GlyphMetrics();
                    glyph.character = c;
                    glyph.stb_allocated = true;
                    glyph.height = stb_height.get(0);
                    glyph.width = stb_width.get(0);
                    glyph.xOff = stb_bearing.get(0) * scale;
                    glyph.yOff = -glyph.height - stb_offset_y.get(0);
                    glyph.advance = stb_advance.get(0) * scale;
                    glyph.bitmap = bitmap;
                    map.put(c,glyph);
                }
            }
        }
        return map;
    }

    private static final class GlyphMetrics implements Disposable {
        char character; int x, y, width, height;
        float xOff, yOff, advance; boolean stb_allocated;
        ByteBuffer bitmap;
        public void dispose() {
            if (bitmap != null) {
                if (stb_allocated)
                    stbtt_FreeBitmap(bitmap);
                else MemoryUtil.memFree(bitmap);
            }
        }
    }
}
