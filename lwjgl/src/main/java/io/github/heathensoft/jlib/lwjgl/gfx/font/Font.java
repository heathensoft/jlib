package io.github.heathensoft.jlib.lwjgl.gfx.font;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBTruetype.*;

/**
 * @author Frederik Dahl
 * 01/10/2023
 */


public class Font implements Disposable {

    public static final char FIRST_CHARACTER_INCLUSIVE = 32;
    public static final char FINAL_CHARACTER_EXCLUSIVE = 127;

    // signed distance field
    private boolean sdf;
    int on_edge_value;
    float pixel_distance_scale;



    private boolean monospaced;
    private int size_pixels;
    private int num_characters;
    private float ascent;
    private float descent;
    private float lineGap;
    private Texture texture;
    private Glyph[] glyphs;
    private String name;

    public boolean isMonospaced() {
        return monospaced;
    }

    public boolean isSdf() { return sdf; }

    public int sizePixels() {
        return size_pixels;
    }

    public int numCharacters() {
        return num_characters;
    }

    public float ascent() {
        return ascent;
    }

    public float descent() {
        return descent;
    }

    public float lineGap() {
        return lineGap;
    }

    public String name() {
        return name;
    }

    public void dispose() {
        Disposable.dispose(texture);
    }


    public static void extractAndWrite(String name, ByteBuffer ttf, String directory, int size, int spacing) throws Exception {
        External dir = new External(directory);
        dir.createDirectories();
        StringBuilder builder = new StringBuilder();
        FontData fontData = extractFontData(name,ttf,size,spacing);
        String pngPathString = dir.path().resolve(name + ".png").toString();
        External txtFile = dir.resolve(name + ".txt");
        int image_width = fontData.bitmap_width;
        int image_height = fontData.bitmap_height;
        ByteBuffer image = fontData.bitmap;
        String fontInfo = fontData.info;
        stbi_write_png(pngPathString,image_width,image_height,1,image,image_width);
        txtFile.write(fontInfo);
        fontData.dispose();
    }

    public static FontData extractFontData(String name, ByteBuffer ttf, int size, int spacing) throws Exception {
        size = Math.max(1,size);
        spacing = Math.max(0,spacing);
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
        } Map<Character,GlyphMetrics> map = extractGlyphs(info,scale,ascent);
        int num_glyphs = map.size();
        boolean monospaced = true;
        GlyphMetrics space = map.get(' ');
        for (var entry : map.entrySet()) {
            GlyphMetrics glyph = entry.getValue();
            if (glyph.advance != space.advance) {
                monospaced = false;
                break;
            }
        }
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
                metrics.x0 = x + spacing;
                metrics.y0 = y + spacing;
            }
        }
        StringBuilder builder = new StringBuilder(8 * 1024);
        builder.append("#\n");
        builder.append("#\n");
        builder.append("#f <name> <size> <monospaced> <width> <height> <ascent> <descent> <gap> <characters>").append('\n');
        builder.append("#g <ascii> <char> <x0> <y0< <width> <height> <xOff> <yOff> <advance>").append('\n');
        builder.append("#\n");
        builder.append("#\n");
        builder.append("f ");
        builder.append(name).append(' ');
        builder.append(size).append(' ');
        builder.append(monospaced ? 1 : 0).append(' ');
        builder.append(image_width).append(' ');
        builder.append(image_height).append(' ');
        builder.append(ascent).append(' ');
        builder.append(descent).append(' ');
        builder.append(lineGap).append(' ');
        builder.append(num_glyphs).append("\n\n");
        ByteBuffer image = MemoryUtil.memCalloc(image_width * image_height);
        for (char c = FIRST_CHARACTER_INCLUSIVE; c < FINAL_CHARACTER_EXCLUSIVE; c++) {
            GlyphMetrics glyphMetrics = map.get(c);
            if (glyphMetrics != null) {
                builder.append("g ");
                builder.append((int) glyphMetrics.character).append(' ');
                builder.append(c).append(' ');
                builder.append(glyphMetrics.x0).append(' ');
                builder.append(glyphMetrics.y0).append(' ');
                builder.append(glyphMetrics.width).append(' ');
                builder.append(glyphMetrics.height).append(' ');
                builder.append(glyphMetrics.xOffset).append(' ');
                builder.append(glyphMetrics.yOffset).append(' ');
                builder.append(glyphMetrics.advance).append('\n');
                ByteBuffer bm = glyphMetrics.bitmap;
                for (int ly = 0; ly < glyphMetrics.height; ly++) {
                    for (int lx = 0; lx < glyphMetrics.width; lx++) {
                        int x = glyphMetrics.x0 + lx;
                        int y = glyphMetrics.y0 + ly;
                        byte color = bm.get(ly * glyphMetrics.width + lx);
                        image.put(y * image_width + x,color);
                    }
                } glyphMetrics.dispose();
            }
        } return new FontData(image,builder.toString(),image_width,image_height);
    }

    private static Map<Character,GlyphMetrics> extractGlyphs(STBTTFontinfo info, float scale, float fontAscent) throws Exception {
        int numCharacters = FINAL_CHARACTER_EXCLUSIVE - FIRST_CHARACTER_INCLUSIVE; // 95 [32 -> 127)
        Map<Character,GlyphMetrics> map = new HashMap<>((int) (numCharacters * 1.75f));
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer stb_width  = stack.mallocInt(1);
            IntBuffer stb_height  = stack.mallocInt(1);
            IntBuffer stb_offset_x  = stack.mallocInt(1);
            IntBuffer stb_offset_y  = stack.mallocInt(1);
            IntBuffer stb_advance  = stack.mallocInt(1);
            IntBuffer stb_bearing  = stack.mallocInt(1);
            {
                // SPACE CHARACTER
                char space = FIRST_CHARACTER_INCLUSIVE;
                ByteBuffer bitmap = stbtt_GetCodepointBitmap(info,0,scale,space,stb_width,stb_height,stb_offset_x,stb_offset_y);
                stbtt_GetCodepointHMetrics(info,space,stb_advance,stb_bearing);
                GlyphMetrics glyph = new GlyphMetrics();
                glyph.character = space;
                if (bitmap == null) {
                    glyph.stb_allocated = false;
                    glyph.xOffset = 0;
                    glyph.yOffset = 0;
                    glyph.advance = stb_advance.get(0) * scale;
                    glyph.width = (int) glyph.advance;
                    glyph.height = (int) fontAscent;
                    if (glyph.width == 0 || glyph.height == 0) {
                        throw new Exception("Unable to extract the space character from font");
                    } glyph.bitmap = MemoryUtil.memCalloc(glyph.width * glyph.height);
                } else { glyph.stb_allocated = true;
                    glyph.height = stb_height.get(0);
                    glyph.width = stb_width.get(0);
                    glyph.xOffset = stb_bearing.get(0) * scale;
                    glyph.yOffset = - glyph.height - stb_offset_y.get(0);
                    glyph.advance = stb_advance.get(0) * scale;
                    glyph.bitmap = bitmap;
                } map.put(space,glyph);
            }
            for (char c = FIRST_CHARACTER_INCLUSIVE + 1; c < FINAL_CHARACTER_EXCLUSIVE; c++) {
                ByteBuffer bitmap = stbtt_GetCodepointBitmap(info,0,scale,c,stb_width,stb_height,stb_offset_x,stb_offset_y);
                // ByteBuffer bitmap = stbtt_GetCodepointSDF(info,scale,c,3,(byte) 128,64,stb_width,stb_height,stb_offset_x,stb_offset_y);
                if (bitmap != null) {
                    stbtt_GetCodepointHMetrics(info, c, stb_advance, stb_bearing);
                    GlyphMetrics glyph = new GlyphMetrics();
                    glyph.character = c;
                    glyph.stb_allocated = true;
                    glyph.height = stb_height.get(0);
                    glyph.width = stb_width.get(0);
                    glyph.xOffset = stb_bearing.get(0) * scale;
                    glyph.yOffset = -glyph.height - stb_offset_y.get(0);
                    glyph.advance = stb_advance.get(0) * scale;
                    glyph.bitmap = bitmap;
                    map.put(c,glyph);
                }
            }
        }
        return map;
    }

    public record FontData(ByteBuffer bitmap, String info, int bitmap_width,
                           int bitmap_height) implements Disposable {
        public void dispose() { MemoryUtil.memFree(bitmap); }
    }

    private static final class GlyphMetrics implements Disposable {
        char character;
        int x0, y0;
        int width, height;
        float xOffset, yOffset, advance;
        boolean stb_allocated;
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
