package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.gui.gfx.Fonts;

import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 12/11/2023
 */


public class TextUtils {


    /** Eventually add parsing of types */
    public static void stringToParagraphs(String string, List<Paragraph> dst) {
        if (string == null || string.isBlank()) return;
        List<String> lines = string.trim().lines().collect(Collectors.toList());
        for (String line : lines) dst.add(new Paragraph(line));
    }

    public static float desiredWidth(List<Paragraph> text, Fonts fonts, float size) {
        float width_pixels = 0;
        for (Paragraph line : text) {
            width_pixels = Math.max(width_pixels,line.widthPixels(fonts));
        } return width_pixels * fonts.relativeScale(size);
    }

    public static float centeredOffsetX(Fonts fonts, float desired_width, float bounds_width) {
        return (bounds_width - desired_width) / 2f;
    }

    public static float desiredWidth(String s, Fonts fonts, float size) {
        return widthPixels(s,fonts) * fonts.relativeScale(size);
    }

    public static float widthPixels(String s, Fonts fonts) {
        if (s == null || s.isBlank()) return 0;
        float width = 0;
        for (int i = 0; i < s.length(); i++) {
             width += fonts.advance(s.charAt(i));
        } return width;
    }

    public static boolean isHexadecimal(String s) {
        if (s == null) return false;
        try { int h = HexFormat.fromHexDigits(s);
        } catch (IllegalArgumentException i) {
            return false;
        } return true;
    }

    public static boolean isNumeric(String s) {
        if (s == null) return false;
        try { double d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        } return true;
    }
}
