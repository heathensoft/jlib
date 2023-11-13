package io.github.heathensoft.jlib.gui.dev;

import io.github.heathensoft.jlib.gui.gfx.Fonts;
import io.github.heathensoft.jlib.gui.gfx.TextBatchGUI;
import io.github.heathensoft.jlib.gui.text.Paragraph;
import io.github.heathensoft.jlib.gui.text.Text;
import io.github.heathensoft.jlib.gui.text.Word;
import io.github.heathensoft.jlib.gui.window.Box;
import io.github.heathensoft.jlib.gui.window.Size;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.Iterator;
import java.util.LinkedList;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.gui.text.TextUtils.desiredWidth;
import static io.github.heathensoft.jlib.gui.text.TextUtils.stringToParagraphs;

/**
 * @author Frederik Dahl
 * 12/11/2023
 */


public class TextBox extends Box implements Text, Iterable<Paragraph> {

    private final LinkedList<Paragraph> paragraphs;
    private final Vector4f background_color;
    private final Size text_bounds;
    private int font;
    private float padding;
    private float font_size;
    private float min_font_size;
    private float desired_font_size;
    private boolean dirty; // resize events etc.


    public TextBox(Vector4f background_color, int width, int height, int padding, int font, float font_size, float min_font_size) {
        this.background_color = background_color;
        this.paragraphs = new LinkedList<>();
        this.restingSize.set(width,height);
        this.currentSize.set(restingSize());
        this.padding = Math.max(0, padding);
        this.desired_font_size = clamp(font_size,1,256);
        this.min_font_size = Math.min(min_font_size, desired_font_size);
        this.font_size = desired_font_size;
        this.font = clamp(font,0,Fonts.FONT_SLOTS - 1);
        this.text_bounds = new Size();
        this.dirty = true;
    }


    public void draw(TextBatchGUI batch, Rectanglef bounds) {

        if (dirty) {
            recalculateField(batch.fonts());
            dirty = false;
        }

        float g = font_size * 3;


    }

    public Vector4f backgroundColor() {
        return background_color;
    }

    public void addFirst(Paragraph paragraph) {
        paragraphs.addFirst(paragraph);
        dirty = true;
    }

    public void addLast(Paragraph paragraph) {
        paragraphs.addLast(paragraph);
        dirty = true;
    }

    public Paragraph removeFirst() {
        if (paragraphs.isEmpty()) return null;
        Paragraph line = paragraphs.removeFirst();
        dirty = true;
        return line;
    }

    public Paragraph removeLast() {
        if (paragraphs.isEmpty()) return null;
        Paragraph line = paragraphs.removeLast();
        dirty = true;
        return line;
    }

    public void setText(String text) {
        paragraphs.clear();
        stringToParagraphs(text,paragraphs);
        dirty = true;
    }

    private void recalculateField(Fonts fonts) {
        text_bounds.set(currentSize);
        text_bounds.sub(2 * padding);
        if (!isBlank() && text_bounds.width() > 0f && text_bounds.height() > 0f) {
            boolean word_wrapping = false;
            float min_scale_ratio = min_font_size / desired_font_size;
            float width = text_bounds.width();
            float desired_width = desiredWidth(paragraphs,fonts,desired_font_size);
            float ratio = width / desired_width;
            if (ratio >= 1.0f) {
                font_size = desired_font_size;
            } else if (ratio >= min_scale_ratio) {
                font_size = desired_font_size * ratio;
            } else { word_wrapping = true;
                font_size = min_font_size;
            } float height = 0;
            float scale = fonts.relativeScale(font_size);
            float space = fonts.advance(' ') * scale;
            float gap = fonts.lineGap() * scale;
            float ascent = fonts.ascent() * scale;
            float descent = fonts.descent() * scale;
            float line_height = gap + ascent + descent;
            if (word_wrapping) {
                for (Paragraph line : paragraphs) {
                    float x = 0;
                    for (Word word : line) {
                        float word_width = word.widthPixels(fonts) * scale;
                        if ((x + word_width) > width && x > 0) { x = 0;
                            height += line_height;
                        } x += (word_width + space);
                    } height += line_height;
                }
            } else {
                int lines = paragraphs.size();
                for (int i = 0; i < lines; i++) {
                    height += line_height;
                }
            } height -= gap;
            if (height > text_bounds.height()) {
                text_bounds.setHeight(height);
            }
        }
    }

    public void setFont(int font) {
        font = clamp(font,0,Fonts.FONT_SLOTS - 1);
        if (this.font != font) {
            this.font = font;
            dirty = true;
        }
    }

    public void setFontSize(float size) {
        desired_font_size = clamp(size,1,256);
        min_font_size = Math.min(min_font_size, desired_font_size);
        dirty = true;
    }

    public void setMinimumFontSize(float size) {
        min_font_size = clamp(size,1,256);
        desired_font_size = Math.max(desired_font_size,min_font_size);
        dirty = true;
    }

    public void setTextFieldPadding(float padding) {
        if (this.padding != padding) {
            this.padding = padding;
            dirty = true;
        }
    }

    protected void resizeVertical(float dy) {
        super.resizeVertical(dy);
        dirty = true;
    }

    protected void resizeHorizontal(float dx) {
        super.resizeHorizontal(dx);
        dirty = true;
    }

    public boolean isBlank() {
        return paragraphs.isEmpty();
    }

    public Iterator<Paragraph> iterator() {
        return paragraphs.iterator();
    }
}
