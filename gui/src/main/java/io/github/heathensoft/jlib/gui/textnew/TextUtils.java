package io.github.heathensoft.jlib.gui.textnew;

import java.util.HexFormat;

/**
 * @author Frederik Dahl
 * 19/09/2023
 */


public class TextUtils {


    public static float calculateWidth(Paragraph paragraph, FontData font, float scale) {
        if (scale <= 0) throw new IllegalArgumentException("scale <= 0");
        if (!paragraph.isBlank()) {
            float space = font.getChar(' ').advance() * scale;
            float pointerX = 0;
            for (Word w : paragraph) {
                if (w instanceof Word.EOL) break;
                pointerX += wordWidthPixels(w,font) * scale;
                pointerX += space;
            } pointerX -= space;
            return pointerX;
        } return 0;

    }

    public static float calculateHeight(Text<?> text, FontData font, float scale, int leading) {
        if (scale <= 0) throw new IllegalArgumentException("scale <= 0");
        if (!text.isBlank()) {
            float fontHeight = (font.height() + leading) * scale;
            float pointerY = 0;
            for (Paragraph p : text) {
                pointerY += fontHeight;
            } return pointerY;
        } return 0;
    }

    public static float calculateHeightWrapped(Text<?> text, FontData font, float scale, float desiredWidth, int leading) {
        if (scale <= 0 || desiredWidth <= 0) throw new IllegalArgumentException("scale or desired width <= 0");
        if (!text.isBlank()) {
            float pointerX;
            float pointerY = 0;
            float fontHeight = (font.height() + leading) * scale;
            float space = font.getChar(' ').advance() * scale;
            for (Paragraph p : text) {
                pointerX = 0;
                pointerY += fontHeight;
                for (Word w : p) {
                    if (w instanceof Word.EOL) break;
                    float wordWidth = wordWidthPixels(w,font) * scale;
                    float nextX = pointerX + wordWidth;
                    if (nextX > desiredWidth && pointerX > 0) {
                        pointerY += fontHeight;
                        pointerX = 0;
                    } pointerX += (wordWidth + space);
                }
            } return pointerY;
        } return 0;
    }

    public static float wordWidthPixels(Word word, FontData font) {
        if (font.isMonoSpaced()) {
            int advance = font.getChar('x').advance();
            return word.length() * advance;
        } int width = 0;
        for (int i = 0; i < word.length(); i++) {
            width += font.getChar(word.get(i)).advance();
        } return width;
    }


    public static PlainParagraph toPlain(ParsedParagraph paragraph) {
        if (paragraph == null || paragraph.isBlank()) return new PlainParagraph();
        String prefix;
        if (paragraph.isRegularParsed()) prefix = "";
        else  if (paragraph instanceof Highlighted p) prefix = "%% ";
        else if (paragraph instanceof Comment p)  prefix = "## ";
        else if (paragraph instanceof DebugLine p) prefix = "** ";
        else if (paragraph instanceof Warning p) prefix = "!! ";
        else throw new IllegalStateException();
        StringBuilder builder = new StringBuilder(4 + 8 * paragraph.wordCount());
        builder.append(prefix);
        Word[] words = paragraph.words();
        for (Word word : paragraph) {
            if (!word.isRegularWord()) {
                if (word.isEndOfLine()) break;
                if (word instanceof Keyword) {
                    if (word instanceof Keyword.InlineComment) builder.append("#");
                    else if (word instanceof Keyword.Value) {
                        if (word instanceof Keyword.Value.Hexadecimal) builder.append("0x");
                        else builder.append("0v");
                    } else if (word instanceof Keyword.Action) {
                        if (word instanceof Keyword.Action.Failure) builder.append("&&&");
                        else if (word instanceof Keyword.Action.Success) builder.append("&&");
                        else builder.append("&");
                    } else if (word instanceof Keyword.Entity) {
                        if (word instanceof Keyword.Entity.Hostile) builder.append("$$$");
                        else if (word instanceof Keyword.Entity.Friendly) builder.append("$$");
                        else builder.append("$");
                    } else builder.append("%");
                } else throw new IllegalStateException("Should never occur");
            } builder.append(word).append(" ");
        } builder.deleteCharAt(builder.length()-1);
        return new PlainParagraph(builder.toString());
    }

    public static ParsedParagraph toParsed(PlainParagraph paragraph) {
        if (paragraph == null || paragraph.isBlank()) return ParsedParagraph.EMPTY_PARAGRAPH;
        return stringToParsed(paragraph.toStringTrimmed());
    }

    public static ParsedParagraph stringToParsed(String string) {
        if (string == null || string.isBlank()) {
            return ParsedParagraph.EMPTY_PARAGRAPH;
        } string = string.trim();
        if (string.length() > 2) {
            char first_char = string.charAt(0);
            if (first_char > 32 && first_char < 38) {
                if (string.startsWith("%%")) {
                    string = string.replaceFirst("%%\\s*","");
                    return new Highlighted(string);
                } else if (string.startsWith("##")) {
                    string = string.replaceFirst("##\\s*","");
                    return new Comment(string);
                } else if (string.startsWith("**")) {
                    string = string.replaceFirst("\\*\\*\\s*","");
                    return new DebugLine(string);
                } else if (string.startsWith("!!")) {
                    string = string.replaceFirst("!!\\s*","");
                    return new Warning(string);
                }
            }
        } return new ParsedParagraph(string);
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
