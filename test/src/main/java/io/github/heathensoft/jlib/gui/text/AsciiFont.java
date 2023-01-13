package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.graphicsOld.TextureRegion;
import org.joml.Vector4f;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class AsciiFont {

    public static final int FIRST_CHARACTER = 32;

    private final String name;
    private final Glyph[] glyphs;
    private final int descent;
    private final int ascent;
    private final int height;
    private final int num_characters;
    private final boolean isMonoSpaced;


    public AsciiFont(TextureRegion fontRegion, List<String> layout) {
        String[] font_info = layout.get(0).split(" ");
        name = font_info[0];
        height = Integer.parseInt(font_info[1]);
        ascent = Integer.parseInt(font_info[2]);
        descent = Integer.parseInt(font_info[3]);
        num_characters = layout.size() - 1;
        glyphs = new Glyph[num_characters];
        int rw = fontRegion.w();
        int rh = fontRegion.h();
        float rw_inv = 1f / rw;
        float rh_inv = 1f / rh;
        for (int i = 1; i <= num_characters; i++) {
            String line = layout.get(i);
            String[] info = line.substring(2).split(" ");
            int glyph_c = line.charAt(0);
            int glyph_x = Integer.parseInt(info[0]);
            int glyph_y = Integer.parseInt(info[1]);
            int glyph_a = Integer.parseInt(info[2]);
            float u = glyph_x * rw_inv;
            float v2 = glyph_y * rh_inv;
            float u2 = (glyph_x + glyph_a) * rw_inv;
            float v = (glyph_y - height) * rh_inv;
            Vector4f glyph_uv = new Vector4f(u,v,u2,v2);
            fontRegion.toRegionUVs(glyph_uv);
            TextureRegion glyph_region = new TextureRegion(
                    fontRegion.texture(),
                    glyph_uv.x,
                    glyph_uv.y,
                    glyph_uv.z,
                    glyph_uv.w
            );
            glyphs[i-1] = new Glyph(
                    glyph_region,
                    this,
                    glyph_a
            );
        }
        int advance = glyphs[0].advance();
        boolean sameAdvance = true;
        for (Glyph glyph : glyphs) {
            if (glyph.advance() != advance) {
                sameAdvance = false;
                break;
            }
        }
        this.isMonoSpaced = sameAdvance;
    }

    /**
     * MONO FONT
     * @param name name of font
     * @param regions character regions (in order)
     * @param lineHeight font line height
     * @param advance common advance for all chars
     */
    public AsciiFont(String name, TextureRegion[] regions, int lineHeight, int advance) {
        this.num_characters = regions.length;
        this.height = lineHeight;
        this.isMonoSpaced = true;
        this.descent = 0;
        this.ascent = 0;
        this.name = name;
        this.glyphs = new Glyph[num_characters];
        for (int i = 0; i < num_characters; i++) {
            glyphs[i] = new Glyph(regions[i],this,advance);
        }
    }

    public Glyph getChar(char c) {
        int idx = ((c & 0x7F) - AsciiFont.FIRST_CHARACTER) % num_characters;
        return glyphs[Math.max(0,idx)];
    }

    public Glyph getChar(byte c) {
        int idx = ((c & 0x7F) - AsciiFont.FIRST_CHARACTER) % num_characters;
        return glyphs[Math.max(0,idx)];
    }

    public int width(String string, int scale) {
        return width(string.getBytes(StandardCharsets.US_ASCII),scale);
    }

    public int width(byte[] string, int scale) {
        int width = 0;
        if (isMonoSpaced) {
            int x_width = getChar('x').advance();
            width = string.length * x_width;
        } else { for (byte c : string)
                width += getChar(c).advance();
        } width *= scale;
        return width;
    }

    public String name() {
        return name;
    }

    public int numCharacters() {
        return num_characters;
    }

    public int height() {
        return height;
    }

    public int ascent() {
        return ascent;
    }

    public int descent() {
        return descent;
    }

    public boolean isMonoSpaced() {
        return isMonoSpaced;
    }
}
