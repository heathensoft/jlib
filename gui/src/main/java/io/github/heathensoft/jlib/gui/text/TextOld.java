package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.gfx.TextBatchGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static io.github.heathensoft.jlib.gui.text.TextUtils.colorOf;
import static io.github.heathensoft.jlib.gui.text.TextUtils.string_to_paragraphs;

/**
 * @author Frederik Dahl
 * 13/11/2023
 */


public class TextOld implements RenderableText, Iterable<Paragraph> {

    protected int font;
    protected float font_size;
    protected boolean word_wrap;
    protected LinkedList<Paragraph> lines;

    public TextOld(int font_size) {
        this(font_size,false);
    }

    public TextOld(int font_size, boolean word_wrap) {
        this(0,font_size,word_wrap);
    }

    public TextOld(int font, int font_size, boolean word_wrap) {
        this.font = clamp(font,0, FontsGUI.FONT_SLOTS - 1);
        this.lines = new LinkedList<>();
        this.word_wrap = word_wrap;
        this.font_size = font_size;
    }

    public LinkedList<Paragraph> get() { return lines; }

    public void set(String text) {
        lines.clear();
        string_to_paragraphs(text,lines);
    }

    public void set(List<Paragraph> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
    }

    public void add(Paragraph paragraph) { addLast(paragraph); }

    public void addFirst(Paragraph paragraph) { lines.addFirst(paragraph); }

    public void addLast(Paragraph paragraph) { lines.addLast(paragraph); }

    public boolean remove(Paragraph paragraph) { return lines.remove(paragraph); }

    public Paragraph removeFirst() { return lines.removeFirst(); }

    public Paragraph removeLast() { return lines.removeLast(); }

    public void copy(List<Paragraph> dst) { for (Paragraph line : lines) { dst.add(line.copy()); } }

    public void draw(TextBatchGUI batch, Rectanglef bounds) {
        FontsGUI fonts = batch.fonts();
        fonts.bindFontMetrics(font);
        float width = bounds.lengthX();
        float scale = fonts.relativeScale(font_size);
        float space = fonts.advance(' ') * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_gap = fonts.lineGap() * scale;
        float line_height = line_gap + ascent + descent;
        int shared_bits = (fonts.currentFont() << 29);
        shared_bits |= (((round(font_size) - 1) & 0xFF) << 21);
        Vector4f rgb = MathLib.vec4();
        float color_alpha;
        float color_float_bits;
        float y = bounds.maxY;
        for (Paragraph line : lines) {
            if (y <= bounds.minY) return;
            y -= ascent;
            if ((y - descent) <= bounds.minY) {
                color_alpha = 0.5f;
            } else color_alpha = 1.0f;
            int line_bits = shared_bits;
            if (line instanceof ColoredLine custom) {
                float glow = clamp(custom.glow());
                line_bits |= ((round(glow * 127.0f) & 0x7F) << 13);
            }
            if (word_wrap) { float x_offset = 0;
                for (Word word : line) {
                    float word_width = word.widthPixels(fonts) * scale;
                    if ((x_offset + word_width) > width && x_offset > 0) {
                        if (y <= bounds.minY) { return; }
                        y -= line_height;
                        if ((y - descent) <= bounds.minY) {
                            color_alpha = 0.5f;
                        } else color_alpha = 1.0f;
                        x_offset = 0;
                    }
                    rgb.set(colorOf(line,word));
                    rgb.w *= color_alpha;
                    color_float_bits = Color.rgb_to_floatBits(rgb);
                    int word_length = word.length();
                    float x = x_offset + bounds.minX;
                    for (int i = 0; i < word_length; i++) {
                        if (x >= bounds.maxX) { break; }
                        char c = (char) (word.get(i) & 0x7F);
                        float advance = fonts.advance(c) * scale;
                        batch.pushVertex(x,y,color_float_bits,line_bits | c);
                        x += advance;
                    } x_offset += (word_width + space);
                }
            }
            else { float x = bounds.minX;
                next_line:
                for (Word word : line) {
                    rgb.set(colorOf(line,word));
                    rgb.w *= color_alpha;
                    color_float_bits = Color.rgb_to_floatBits(rgb);
                    int word_length = word.length();
                    for (int i = 0; i < word_length; i++) {
                        if (x >= bounds.maxX) { break next_line; }
                        char c = (char) (word.get(i) & 0x7F);
                        float advance = fonts.advance(c) * scale;
                        batch.pushVertex(x,y,color_float_bits,line_bits | c);
                        x += advance;
                    } x += space;
                }
            } y -= (descent + line_gap);
        }
    }

    public float calculateHeight(FontsGUI fonts, float width) {
        if (isBlank() || font_size < 1f) return 0;
        fonts.bindFontMetrics(font);
        float height = 0;
        float scale = fonts.relativeScale(font_size);
        float gap = fonts.lineGap() * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_height = gap + ascent + descent;
        if (word_wrap) {
            for (Paragraph line : lines) { float x = 0;
                float space = fonts.advance(' ') * scale;
                for (Word word : line) {
                    float word_width = word.widthPixels(fonts) * scale;
                    if ((x + word_width) > width && x > 0) {x = 0;
                        height += line_height;
                    }x += (word_width + space);
                }height += line_height;
            }
        } else { for (Paragraph line : this)
            height += line_height;
        } return height - gap;
    }

    public float desiredWidth(FontsGUI fonts) {
        float width_pixels = 0;
        fonts.bindFontMetrics(font);
        for (Paragraph line : this) {
            width_pixels = Math.max(width_pixels,line.widthPixels(fonts));
        } return width_pixels * fonts.relativeScale(font_size);
    }

    public void setFontSize(float size) { font_size = clamp(size,1,256); }

    public float fontSize() { return font_size; }

    public void setFont(int font) { this.font = clamp(font,0, FontsGUI.FONT_SLOTS - 1); }

    public int font() { return font; }

    public int mumLines() { return lines.size(); }

    public boolean isBlank() { return lines.isEmpty(); }

    public void useWordWrap(boolean yes) { this.word_wrap = yes; }

    public boolean isWordWrapped() { return word_wrap; }

    public Iterator<Paragraph> iterator() { return lines.iterator(); }

    public String string() {
        if (isBlank()) return "";
        StringBuilder stringBuilder = new StringBuilder(256);
        for (Paragraph line : lines) {
            line.toString(stringBuilder);
            stringBuilder.append("\n");
        } int length = stringBuilder.length();
        stringBuilder.deleteCharAt(length - 1);
        return stringBuilder.toString();
    }

    public void clear() { lines.clear(); }
}
