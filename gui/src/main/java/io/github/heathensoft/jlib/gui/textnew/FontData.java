package io.github.heathensoft.jlib.gui.textnew;

import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;

import java.util.List;
import java.util.Objects;

/**
 *
 * name size leading
 * char x y advance
 * ...
 * ...
 * char x y advance
 *
 * @author Frederik Dahl
 * 05/09/2023
 */


public class FontData {

    public static final int FIRST_CHARACTER = 32;
    public static final int FINAL_CHARACTER = 127;

    private final String name;
    private final Glyph[] glyphs;
    private final int height;
    private final int leading;
    private final int num_characters;
    private final boolean monoSpaced;


    public FontData(TextureRegion font_region, List<String> font_info) {
        String[] info_lines = font_info.get(0).split("\\s+");
        name = info_lines[0];
        height = Integer.parseInt(info_lines[1]);
        leading = Integer.parseInt(info_lines[2]);
        num_characters = font_info.size() - 1;
        glyphs = new Glyph[num_characters];
        for (int i = 1; i <= num_characters; i++) {
            String line = font_info.get(i);
            String[] info = line.substring(2).split("\\s+");
            int glyph_c = line.charAt(0);
            int glyph_x = Integer.parseInt(info[0]);
            int glyph_y = Integer.parseInt(info[1]);
            int glyph_a = Integer.parseInt(info[2]);
            TextureRegion region = font_region.subRegion(glyph_x,glyph_y,glyph_a,height);
            glyphs[i-1] = new Glyph(region,(byte) glyph_c);
        } int advance = glyphs[0].w();
        boolean sameAdvance = true;
        for (Glyph glyph : glyphs) {
            if (glyph.advance() != advance) {
                sameAdvance = false;
                break;
            }
        } monoSpaced = sameAdvance;
    }

    public FontData(String name, TextureRegion[] glyphs, int leading) {
        this.name = name == null ? "Unnamed Font" : name;
        this.num_characters = Math.min(glyphs.length,FINAL_CHARACTER);
        this.glyphs = new Glyph[num_characters];
        this.height = glyphs[0].h();
        this.leading = Math.max(0,leading);
        int advance = glyphs[0].w();
        boolean sameAdvance = true;
        for (int i = 0; i < num_characters; i++) {
            byte character = (byte) (FIRST_CHARACTER + i);
            this.glyphs[i] = new Glyph(glyphs[i],character);
            if (this.glyphs[i].advance() != advance)
                sameAdvance = false;
        } monoSpaced = sameAdvance;
    }

    public Glyph getChar(int c) {
        return getChar((byte) (c & 0xFF));
    }

    public Glyph getChar(char c) {
        return getChar((byte) (c & 0xFF));
    }

    public Glyph getChar(byte c) {
        return glyphs[Math.max(((c & 0x7F) - FIRST_CHARACTER),0) % num_characters];
    }

    public String name() {
        return name;
    }

    public int height() {
        return height;
    }

    public int leading() { return leading; }

    public int numCharacters() {
        return num_characters;
    }

    public boolean isMonoSpaced() {
        return monoSpaced;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FontData font = (FontData) o;
        return Objects.equals(name, font.name);
    }

    public int hashCode() {
        return Objects.hash(name);
    }
}
