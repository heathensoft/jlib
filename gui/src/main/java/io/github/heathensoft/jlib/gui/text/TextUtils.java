package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import org.joml.Vector4f;

import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 12/11/2023
 */


public class TextUtils {

    public static float calculate_width(List<Paragraph> text, FontsGUI fonts, float size) {
        float width_pixels = 0;
        for (Paragraph line : text) {
            width_pixels = Math.max(width_pixels,line.widthPixels(fonts));
        } return width_pixels * fonts.relativeScale(size);
    }

    public static float calculate_height(List<Paragraph> paragraphs, FontsGUI fonts, float size, float width, boolean word_wrap) {
        if (paragraphs.isEmpty() || size < 1f) return 0;
        float height = 0;
        float scale = fonts.relativeScale(size);
        float gap = fonts.lineGap() * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_height = gap + ascent + descent;
        if (word_wrap) {
            for (Paragraph line : paragraphs) { float x = 0;
                float space = fonts.advance(' ') * scale;
                for (Word word : line) {
                    float word_width = word.widthPixels(fonts) * scale;
                    if ((x + word_width) > width && x > 0) { x = 0;
                        height += line_height;
                    } x += (word_width + space);
                } height += line_height;
            }
        } else { for (Paragraph line : paragraphs)
                height += line_height;
        } return height - gap;
    }

    public static Vector4f colorOf(Paragraph paragraph, Word word) {
        return paragraph.colorOf(word);
    }

    /** Eventually add parsing of types */
    public static void string_to_paragraphs(String string, List<Paragraph> dst) {
        if (string == null || string.isBlank()) return;
        List<String> lines = string.trim().lines().collect(Collectors.toList());
        for (String line : lines) dst.add(new Paragraph(line));
    }


    public static float center_offset_x(FontsGUI fonts, float desired_width, float bounds_width) {
        return (bounds_width - desired_width) / 2f;
    }

    public static float calculate_width(String s, FontsGUI fonts, float size) {
        return width_pixels(s,fonts) * fonts.relativeScale(size);
    }

    public static float width_pixels(String s, FontsGUI fonts) {
        if (s == null || s.isBlank()) return 0;
        float width = 0;
        for (int i = 0; i < s.length(); i++) {
             width += fonts.advance(s.charAt(i));
        } return width;
    }

    public static boolean is_hexadecimal(String s) {
        if (s == null) return false;
        try { int h = HexFormat.fromHexDigits(s);
        } catch (IllegalArgumentException i) {
            return false;
        } return true;
    }

    public static boolean is_numeric(String s) {
        if (s == null) return false;
        try { double d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        } return true;
    }
}
