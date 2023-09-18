package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.gui.text.*;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.gfx.SpriteBatch;
import org.joml.Vector2f;

import static io.github.heathensoft.jlib.common.utils.U.ceil;
import static io.github.heathensoft.jlib.common.utils.U.floor;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Frederik Dahl
 * 15/09/2023
 */


public class TextRenderer {

    private FontData font;
    private ColorScheme scheme;
    private final Size area;
    private final SpriteBatch batch;
    private final Vector2f position;
    private final Vector2f local_pointer;
    private float scale;
    private int leading;
    private int shader_custom;
    private boolean wrapping_enabled;
    private boolean scissoring_enabled;

    public TextRenderer(SpriteBatch spriteBatch, ColorScheme colorScheme, FontData font) {
        this.font = font;
        this.batch = spriteBatch;
        this.scheme = colorScheme;
        this.area = new Size();
        this.position = new Vector2f();
        this.local_pointer = new Vector2f();
        this.scale = 1;
        this.leading = 0;
        this.shader_custom = 0;
        this.wrapping_enabled = false;
        this.scissoring_enabled = false;
    }

    public void draw(String string, Color32 color) {
        draw(Paragraph.colored(string,color));
    }

    public void draw(Paragraph text) {
        if (batch.isRendering() && text != null && !text.isBlank() && area.isValid()) {
            if (scissoring_enabled) {
                batch.flush();
                int w = ceil(area.width());
                int h = ceil(area.height());
                int x0 = floor(position.x);
                int y0 = floor(position.y) - h;
                glEnable(GL_SCISSOR_TEST);
                glScissor(x0,y0,w,h);
                local_pointer.y = font.height() * scale;
                if (wrapping_enabled) drawWrapped(text);
                else drawUnwrapped(text);
                batch.flush();
                glDisable(GL_SCISSOR_TEST);
            } else {
                local_pointer.y = font.height() * scale;
                if (local_pointer.y > area.height()) return;
                if (wrapping_enabled) drawWrapped(text);
                else drawUnwrapped(text);
            }
        }
    }

    public void draw(Text text) {
        if (batch.isRendering() && text != null && !text.isBlank() && area.isValid()) {
            float font_height = font.height() * scale;
            float leading = this.leading * scale;
            local_pointer.zero();
            if (scissoring_enabled) {
                batch.flush();
                int w = ceil(area.width());
                int h = ceil(area.height());
                int x0 = floor(position.x);
                int y0 = floor(position.y) - h;
                glEnable(GL_SCISSOR_TEST);
                glScissor(x0,y0,w,h);
                for (Paragraph paragraph : text) {
                    if (local_pointer.y > area.height()) break;
                    local_pointer.y += font_height;
                    local_pointer.x = 0;
                    if (wrapping_enabled) drawWrapped(paragraph);
                    else drawUnwrapped(paragraph);
                    local_pointer.y += leading;
                } batch.flush();
                glDisable(GL_SCISSOR_TEST);
            } else {
                for (Paragraph paragraph : text) {
                    local_pointer.y += font_height;
                    if (local_pointer.y > area.height()) break;
                    local_pointer.x = 0;
                    if (wrapping_enabled) drawWrapped(paragraph);
                    else drawUnwrapped(paragraph);
                    local_pointer.y += leading;
                }
            }
        }
    }


    private void drawWrapped(Paragraph paragraph) {
        float leading = this.leading * scale;
        float font_height = font.height() * scale;
        float space = font.getChar(' ').advance() * scale;
        float color;
        if (paragraph.getClass().equals(Paragraph.class)) {
            if (scissoring_enabled) {
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    float word_width = word_width(word);
                    float next_x = local_pointer.x + word_width;
                    // Single words not fitting the area width will be drawn anyway. (Clipped)
                    if (next_x > area.width() && local_pointer.x > 0) {
                        if (local_pointer.y >= area.height()) return; // already partially outside y-bounds.
                        local_pointer.y += font_height;
                        local_pointer.y += leading;
                        local_pointer.x = 0;
                    }
                    float y0 = position.y - local_pointer.y;
                    if (word.getClass().equals(Word.class)) { // Regular Word
                        color = scheme.text_letters.floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    else { // Special Word
                        color = scheme.colorOf(word).floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    local_pointer.x += space;
                }
            } else {
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    float word_width = word_width(word);
                    float next_x = local_pointer.x + word_width;
                    if (next_x > area.width()) {
                        if (word_width > area.width()) return; // No room for single word
                        local_pointer.y += font_height;
                        local_pointer.y += leading;
                        if (local_pointer.y > area.height()) return; // partially outside y-bounds.
                        local_pointer.x = 0;
                    }
                    float y0 = position.y - local_pointer.y;
                    if (word.getClass().equals(Word.class)) { // Regular Word
                        color = scheme.text_letters.floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    else { // Special Word
                        color = scheme.colorOf(word).floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    local_pointer.x += space;
                }
            }
        } else {
            float default_color = scheme.colorOf(paragraph).floatBits();
            boolean debug = paragraph instanceof PDebug;
            if (scissoring_enabled) {
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    float word_width = word_width(word);
                    float next_x = local_pointer.x + word_width;
                    // Single words not fitting the area width will be drawn anyway. (Clipped)
                    if (next_x > area.width() && local_pointer.x > 0) {
                        if (local_pointer.y >= area.height()) return; // already partially outside y-bounds.
                        local_pointer.y += font_height;
                        local_pointer.y += leading;
                        local_pointer.x = 0;
                    }
                    float y0 = position.y - local_pointer.y;
                    if (word.getClass().equals(Word.class)) { // Regular Word
                        color = default_color;
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    else { // Special Word
                        if (debug && word instanceof Keyword.Value)
                            color = scheme.text_numbers.floatBits();
                        else color = default_color;
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    local_pointer.x += space;
                }
            } else {
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    float word_width = word_width(word);
                    float next_x = local_pointer.x + word_width;
                    if (next_x > area.width()) {
                        if (word_width > area.width()) return; // No room for single word
                        local_pointer.y += font_height;
                        local_pointer.y += leading;
                        if (local_pointer.y > area.height()) return; // partially outside y-bounds.
                        local_pointer.x = 0;
                    }
                    float y0 = position.y - local_pointer.y;
                    if (word.getClass().equals(Word.class)) { // Regular Word
                        color = scheme.text_letters.floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    else { // Special Word
                        color = scheme.colorOf(word).floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    local_pointer.x += space;
                }
            }
        }
    }

    private void drawUnwrapped(Paragraph paragraph) {
        float font_height = font.height() * scale;
        float space = font.getChar(' ').advance() * scale;
        float color;
        if (paragraph.getClass().equals(Paragraph.class)) { // Regular paragraph
            if (scissoring_enabled) { // Scissoring enabled
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    if (local_pointer.x >= area.width()) break;
                    float y0 = position.y - local_pointer.y;
                    if (word.getClass().equals(Word.class)) { // Regular Word
                        color = scheme.text_letters.floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            if (local_pointer.x >= area.width()) break;
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    else { // Special Word
                        color = scheme.colorOf(word).floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            if (local_pointer.x >= area.width()) break;
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                            local_pointer.x += advance;
                        }
                    }
                    local_pointer.x += space;
                }
            }
            else { // Scissoring disabled
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    if (local_pointer.x >= area.width()) break;
                    float y0 = position.y - local_pointer.y;
                    if (word.getClass().equals(Word.class)) { // Regular Word
                        color = scheme.text_letters.floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            local_pointer.x += advance;
                            if (local_pointer.x >= area.width()) break;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                        }
                    } else { // Special Word
                        color = scheme.colorOf(word).floatBits();
                        for (int i = 0; i < word.length(); i++) {
                            byte character = word.get(i);
                            Glyph glyph = font.getChar(character);
                            float advance = glyph.advance() * scale;
                            float x0 = position.x + local_pointer.x;
                            local_pointer.x += advance;
                            if (local_pointer.x >= area.width()) break;
                            batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                        }
                    }
                    local_pointer.x += space;
                }
            }
        }
        else { // Special Paragraph
            float default_color = scheme.colorOf(paragraph).floatBits();
            boolean debug = paragraph instanceof PDebug;
            if (scissoring_enabled) { // Scissoring enabled
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    if (local_pointer.x >= area.width()) break;
                    float y0 = position.y - local_pointer.y;
                    if (debug && word instanceof Keyword.Value) {
                        color = scheme.text_numbers.floatBits();
                    } else color = default_color;
                    for (int i = 0; i < word.length(); i++) {
                        byte character = word.get(i);
                        if (local_pointer.x >= area.width()) break;
                        Glyph glyph = font.getChar(character);
                        float advance = glyph.advance() * scale;
                        float x0 = position.x + local_pointer.x;
                        batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                        local_pointer.x += advance;
                    } local_pointer.x += space;
                }
            }
            else { // Scissoring disabled
                for (Word word : paragraph) {
                    if (word instanceof Word.EOL) break;
                    if (local_pointer.x >= area.width()) break;
                    float y0 = position.y - local_pointer.y;
                    if (debug && word instanceof Keyword.Value) {
                        color = scheme.text_numbers.floatBits();
                    } else color = default_color;
                    for (int i = 0; i < word.length(); i++) {
                        byte character = word.get(i);
                        Glyph glyph = font.getChar(character);
                        float advance = glyph.advance() * scale;
                        float x0 = position.x + local_pointer.x;
                        local_pointer.x += advance;
                        if (local_pointer.x >= area.width()) break;
                        batch.draw(glyph,x0,y0,advance,font_height,color,shader_custom);
                    } local_pointer.x += space;
                }
            }
        }
    }

    public FontData getFont() {
        return font;
    }

    public void setFont(FontData font) {
        this.font = font;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ColorScheme getColorScheme() {
        return scheme;
    }

    public void setScheme(ColorScheme scheme) {
        this.scheme = scheme;
    }

    public Size getTextAreaSize() {
        return area;
    }

    public void setTextArea(float x, float y, float w, float h) {
        setTextPosition(x,y);
        setTextAreaSize(w,h);
    }

    public void setTextAreaSize(Size area) {
        this.area.set(area);
    }

    public void setTextAreaSize(float w, float h) {
        this.area.set(w,h);
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position.set(position);
    }

    public void setTextPosition(float x, float y) {
        this.position.set(x,y);
    }

    public float getTextScale() {
        return scale;
    }

    public void setTextScale(float scale) {
        this.scale = scale;
    }

    public int getTextLeading() {
        return leading;
    }

    public void setTextLeading(int leading) {
        this.leading = leading;
    }

    public int getTextureSlot() {
        return shader_custom & 0xFF;
    }

    public void setTextureSlot(int slot) {
        this.shader_custom = (shader_custom &~ 0x000000FF) | (slot & 0xFF);
    }

    public int getPixelID() {
        return (shader_custom >> 8) & 0x00FFFFFF;
    }

    public void setPixelID(int id) {
        shader_custom = (shader_custom &~ 0xFFFFFF00) | ((id & 0xFFFFFF) << 8);
    }

    public boolean isWrappingEnabled() {
        return wrapping_enabled;
    }

    public void enableWrapping(boolean wrapping) {
        this.wrapping_enabled = wrapping;
    }

    public boolean isScissoringEnabled() {
        return scissoring_enabled;
    }

    public void enableScissoring(boolean scissoring) {
        this.scissoring_enabled = scissoring;
    }

    private float word_width(Word word) {
        if (font.isMonoSpaced()) {
            int advance = font.getChar('x').advance();
            return word.length() * advance * scale;
        } int width = 0;
        for (int i = 0; i < word.length(); i++) {
            width += font.getChar(word.get(i)).advance();
        } return width * scale;
    }

    private boolean isNumber(byte character) {
        return character > 47 && character < 58;
    }

    private boolean isLetter(byte character) {
        return (character > 64 && character < 91) || (character > 96 && character < 123);
    }

}
