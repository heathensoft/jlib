package io.github.heathensoft.jlib.gui.deprecated;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.SpriteBatch;
import org.joml.Vector2f;

import java.util.HexFormat;

/**
 * @author Frederik Dahl
 * 19/09/2023
 */


public class TextUtils {


    private static final Size NO_SIZE_LIMIT = new Size(Float.MAX_VALUE);

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position) {
        draw(text, batch, scheme, font, position, 1.0f,0,0);
    }

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, int textureSlot) {
        draw(text, batch, scheme, font, position, 1.0f,0,textureSlot);
    }

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, int pixelID, int textureSlot) {
        draw(text, batch, scheme, font, position, 1.0f,pixelID,textureSlot);
    }

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float scale, int textureSlot) {
        draw(text, batch, scheme, font, position, Float.MAX_VALUE ,scale,0,textureSlot);
    }

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float scale, int pixelID, int textureSlot) {
        draw(text, batch, scheme, font, position, Float.MAX_VALUE ,scale,pixelID,textureSlot);
    }

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int textureSlot) {
        draw(text, batch, scheme, font, position, width, scale, 0,textureSlot);
    }

    public static void draw(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int pixelID, int textureSlot) {
        if (text.isBlank()) return;
        if (text instanceof ColoredParagraph) drawMonoColored(text,batch,scheme,font,position,width,scale,pixelID,textureSlot);
        else if (text instanceof PlainParagraph p) drawPlain(p,batch,scheme,font,position,width,scale,pixelID,textureSlot);
        else if (text instanceof ParsedParagraph p) drawParsed(p,batch,scheme,font,position,width,scale,pixelID,textureSlot);
    }

    private static void drawPlain(PlainParagraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int pixelID, int textureSlot) {
        if (text instanceof TextField p) drawTextField(p,batch,scheme,font,position,width,scale,pixelID,textureSlot);
        else drawMonoColored(text,batch,scheme,font,position,width,scale,pixelID,textureSlot);
    }

    private static void drawParsed(ParsedParagraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int pixelID, int textureSlot) {
        if (!text.isRegularParsed()) {
            if (text instanceof DebugLine p) {
                drawDebugLine(p,batch,scheme,font,position,width,scale,pixelID,textureSlot);
            } else drawMonoColored(text,batch,scheme,font,position,width,scale,pixelID,textureSlot);
        } else { // regular parsed: "Gameplay log"
            float pointerX = 0;
            float font_height = font.height() * scale;
            float y0 = position.y - font_height;
            float x0 = position.x;
            float space = font.getChar(' ').advance() * scale;
            int shaderCustom = (pixelID << 8) | (textureSlot & 0xFF);
            for (Word word : text) {
                if (word instanceof Word.EOL) break;
                float color = scheme.colorOfWord(word).floatBits();
                for (int i = 0; i < word.length(); i++) {
                    byte character = word.get(i);
                    Glyph glyph = font.getChar(character);
                    float advance = glyph.advance() * scale;
                    float next = pointerX + advance;
                    if (next > width) return;
                    batch.draw(glyph,x0 + pointerX,y0,advance,font_height,color,shaderCustom);
                    pointerX = next;
                } pointerX += space;
            }
        }
    }

    private static void drawDebugLine(DebugLine text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int pixelID, int textureSlot) {
        float pointerX = 0;
        float font_height = font.height() * scale;
        float y0 = position.y - font_height;
        float x0 = position.x;
        float space = font.getChar(' ').advance() * scale;
        float color_debug = scheme.text_debugging.floatBits();
        float color_value = scheme.text_numbers.floatBits();
        int shaderCustom = (pixelID << 8) | (textureSlot & 0xFF);
        for (Word word : text) {
            if (word instanceof Word.EOL) break;
            float color = word instanceof Keyword.Value ? color_value : color_debug;
            for (int i = 0; i < word.length(); i++) {
                byte character = word.get(i);
                Glyph glyph = font.getChar(character);
                float advance = glyph.advance() * scale;
                float next = pointerX + advance;
                if (next > width) return;
                batch.draw(glyph,x0 + pointerX,y0,advance,font_height,color,shaderCustom);
                pointerX = next;
            } pointerX += space;
        }
    }

    private static void drawTextField(TextField text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int pixelID, int textureSlot) {
        if (!text.showCursor()) drawMonoColored(text,batch,scheme,font,position,width,scale,pixelID,textureSlot);
        else {
            int shaderLetters = (pixelID << 8) | (textureSlot & 0xFF);
            int shaderCursor = (pixelID << 8) | (0xFF);
            int cursor_index = text.cursorIndex();
            float font_height = font.height() * scale;
            float y0 = position.y - font_height;
            float x0 = position.x;
            float space = font.getChar(' ').advance() * scale;
            float color = scheme.colorOfPlain(text).floatBits();
            float background_color = scheme.text_background.floatBits();
            float width_to_cursor = calculateWidthUpToCursor(text,font,scale,cursor_index);
            boolean cursor_visible; // cursor state
            {
                double time_since_last = text.lastInputMillis();
                if (time_since_last >= 500d) {
                    double time = (time_since_last) * 0.001d;
                    double sin = Math.sin(time * 1.5d * Math.PI);
                    cursor_visible = sin > 0.0d;
                } else cursor_visible = true;
            }
            int character_index = 0;
            float x_offset = width_to_cursor > width ? -(width_to_cursor - width) : 0;
            float pointer_x = 0;
            for (Word word : text) {
                if (word instanceof Word.EOL) break;
                for (int i = 0; i < word.length(); i++) {
                    byte character = word.get(i);
                    Glyph glyph = font.getChar(character);
                    float advance = glyph.advance() * scale;
                    if (x_offset < 0) { x_offset += advance;
                    } else {
                        float next = pointer_x + advance;
                        if (next > width) return;
                        if (cursor_index == character_index && cursor_visible) {
                            batch.draw(glyph,x0 + pointer_x,y0,advance,font_height,color,shaderCursor);
                            batch.draw(glyph,x0 + pointer_x,y0,advance,font_height,background_color,shaderLetters);
                        } else { batch.draw(glyph,x0 + pointer_x,y0,advance,font_height,color,shaderLetters);
                        } pointer_x = next;
                    } character_index++;
                } if (x_offset < 0) {
                    x_offset += space;
                } else {
                    float next = pointer_x + space;
                    if (next > width) return;;
                    if (cursor_index == character_index && cursor_visible) {
                        Glyph glyph = font.getChar(' ');
                        batch.draw(glyph,x0 + pointer_x,y0,space,font_height,color,shaderCursor);
                    } pointer_x = next;
                } character_index++;
            }
        }
    }



    private static void drawMonoColored(Paragraph text, SpriteBatch batch, ColorScheme scheme, FontData font, Vector2f position, float width, float scale, int pixelID, int textureSlot) {
        float pointerX = 0;
        float font_height = font.height() * scale;
        float y0 = position.y - font_height;
        float x0 = position.x;
        float space = font.getChar(' ').advance() * scale;
        float color = scheme.colorOfParagraph(text).floatBits();
        int shaderCustom = (pixelID << 8) | (textureSlot & 0xFF);
        for (Word word : text) {
            if (word instanceof Word.EOL) break;
            for (int i = 0; i < word.length(); i++) {
                byte character = word.get(i);
                Glyph glyph = font.getChar(character);
                float advance = glyph.advance() * scale;
                float next = pointerX + advance;
                if (next > width) return;
                batch.draw(glyph,x0 + pointerX,y0,advance,font_height,color,shaderCustom);
                pointerX = next;
            } pointerX += space;
        }
    }



    public static float calculateWidthUpToCursor(Paragraph paragraph, FontData font, float scale, int cursor) {
        if (scale <= 0) throw new IllegalArgumentException("scale <= 0");
        if (!paragraph.isBlank()) {
            cursor = U.clamp(cursor,0,paragraph.numPrintable());
            float space = font.getChar(' ').advance();
            float pointerX = 0;
            int current_char = 0;
            for (Word w : paragraph) {
                if (w instanceof Word.EOL) break;
                if (current_char == cursor) break;
                int word_length = w.length();
                if (word_length + current_char > cursor) {
                    byte[] word_bytes = w.get();
                    for (byte b : word_bytes) {
                        current_char++;
                        pointerX += font.getChar(b).advance();
                        if (current_char == cursor) {
                            return pointerX * scale;
                        }
                    }
                } else {
                    current_char += word_length;
                    pointerX += wordWidthPixels(w,font);
                    if (current_char == cursor) break;
                } pointerX += space;
                current_char++;
            } return pointerX * scale;
        } return 0;
    }


    public static float calculateWidth(Paragraph paragraph, FontData font, float scale) {
        if (scale <= 0) throw new IllegalArgumentException("scale <= 0");
        if (!paragraph.isBlank()) {
            float space = font.getChar(' ').advance();
            float pointerX = 0;
            for (Word w : paragraph) {
                if (w instanceof Word.EOL) break;
                pointerX += wordWidthPixels(w,font);
                pointerX += space;
            } pointerX -= space;
            return pointerX * scale;
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
                    } else if (word instanceof Keyword.Custom) {
                        if (word instanceof Keyword.Custom.C1) { builder.append("0c1");
                        } else if (word instanceof Keyword.Custom.C2) { builder.append("0c2");
                        } else if (word instanceof Keyword.Custom.C3) { builder.append("0c3");
                        } else if (word instanceof Keyword.Custom.C4) { builder.append("0c4");
                        } else if (word instanceof Keyword.Custom.C5) { builder.append("0c5");
                        } else if (word instanceof Keyword.Custom.C6) { builder.append("0c6");
                        } else if (word instanceof Keyword.Custom.C7) { builder.append("0c7");
                        } else if (word instanceof Keyword.Custom.C8) { builder.append("0c8");
                        } else if (word instanceof Keyword.Custom.C9) { builder.append("0c9");
                        } else builder.append("0c0");
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
