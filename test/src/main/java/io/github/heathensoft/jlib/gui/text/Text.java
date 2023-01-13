package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.graphicsOld.TextureRegion;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Frederik Dahl
 * 01/12/2022
 */


public class Text {

    private final List<Paragraph> paragraphs;

    public Text(String string, boolean searchKeywords) {
        string = string == null ? "" : string;
        List<String> strings = string.lines().collect(Collectors.toList());
        paragraphs = new ArrayList<>(strings.size());
        for (String s : strings) paragraphs.add(new Paragraph(s,searchKeywords));
    }

    public List<Paragraph> paragraphs() {
        return paragraphs;
    }


    public void render(SpriteBatch batch, AsciiFont font, int x0, int y0, float color, int id, int scale, int desired_width, int leading) {
        if (scale > 0 && !paragraphs.isEmpty()) {
            TextureRegion region;
            Glyph glyph;
            leading *= scale;
            int font_height = font.height() * scale;
            int space = font.getChar(' ').advance() * scale;
            int pointer_x = 0;
            int pointer_y = font_height;
            int word_id;
            float word_color;
            if (font.isMonoSpaced()) {
                for (Paragraph paragraph : paragraphs) {
                    for (Word word : paragraph.words()) {
                        int word_width = word.length() * space;
                        int next = pointer_x + word_width;
                        if (next > desired_width) {
                            if (word_width > desired_width) {
                                return;
                            } pointer_x = 0;
                            pointer_y += (font_height + leading);
                        }
                        if (word instanceof Keyword) {
                            InteractableWord iWord = ((Keyword) word).interactable();
                            word_color = iWord.color();
                            word_id = iWord.iData();
                        } else {
                            word_color = color;
                            word_id = id;
                        }
                        byte[] characters = word.get();
                        int x = x0 + pointer_x;
                        int y = y0 - pointer_y;
                        int word_advance = 0;
                        for (int c = 0; c < characters.length; c++) {
                            glyph = font.getChar(characters[c]);
                            region = glyph.region();
                            batch.draw(region, x + word_advance, y, space,
                            font_height, word_color, word_id);
                            word_advance += space;
                        } pointer_x += (word_width + space);
                    } pointer_x = 0;
                    pointer_y += (font_height + leading);
                }
            } else {
                for (Paragraph paragraph : paragraphs) {
                    for (Word word : paragraph.words()) {
                        int word_width = word.widthPixels(font,scale);
                        int next = pointer_x + word_width;
                        if (next > desired_width) {
                            if (word_width > desired_width) {
                                return;
                            } pointer_x = 0;
                            pointer_y += (font_height + leading);
                        }
                        if (word instanceof Keyword) {
                            InteractableWord iWord = ((Keyword) word).interactable();
                            word_color = iWord.color();
                            word_id = iWord.iData();
                        } else {
                            word_color = color;
                            word_id = id;
                        }
                        byte[] characters = word.get();
                        int x = x0 + pointer_x;
                        int y = y0 - pointer_y;
                        int glyph_advance;
                        int word_advance = 0;
                        for (int c = 0; c < characters.length; c++) {
                            glyph = font.getChar(characters[c]);
                            region = glyph.region();
                            glyph_advance = glyph.advance() * scale;
                            batch.draw(region, x + word_advance, y,
                            glyph_advance, font_height, word_color, word_id);
                            word_advance += glyph_advance;
                        } pointer_x += (word_width + space);
                    } pointer_x = 0;
                    pointer_y += (font_height + leading);
                }
            }
        }
    }

    public Vector2i calculateBounds(AsciiFont font, int desired_width, int scale, int leading) {
        if (scale > 0 && !paragraphs.isEmpty()) {
            leading *= scale;
            int font_height = font.height() * scale;
            int space = font.getChar(' ').advance() * scale;
            int pointer_x = 0;
            int pointer_y = font_height;
            int bounds_width = 0;
            if (font.isMonoSpaced()) {
                for (Paragraph paragraph : paragraphs) {
                    for (Word word : paragraph.words()) {
                        int word_width = word.length() * space;
                        int next = pointer_x + word_width;
                        if (next > desired_width) {
                            if (word_width > desired_width) {
                                return new Vector2i();
                            } bounds_width = Math.max(bounds_width,pointer_x - space);
                            pointer_x = 0;
                            pointer_y += (font_height + leading);
                        } pointer_x += (word_width + space);
                    } bounds_width = Math.max(bounds_width,pointer_x - space);
                    pointer_x = 0;
                    pointer_y += (font_height + leading);
                }
            } else {
                for (Paragraph paragraph : paragraphs) {
                    for (Word word : paragraph.words()) {
                        int word_width = word.widthPixels(font,scale);
                        int next = pointer_x + word_width;
                        if (next > desired_width) {
                            if (word_width > desired_width) {
                                return new Vector2i();
                            } bounds_width = Math.max(bounds_width,pointer_x - space);
                            pointer_x = 0;
                            pointer_y += (font_height + leading);
                        } pointer_x += (word_width + space);
                    } bounds_width = Math.max(bounds_width,pointer_x - space);
                    pointer_x = 0;
                    pointer_y += (font_height + leading);
                }
            } pointer_y -= (font_height + leading);
            return new Vector2i(bounds_width,pointer_y);
        } return new Vector2i();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        for (Paragraph p : paragraphs) {
            for (Word w : p.words()) {
                sb.append(w.toString()).append(' ');
            } sb.deleteCharAt(sb.length()-1);
            sb.append('\n');
        } sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

}
