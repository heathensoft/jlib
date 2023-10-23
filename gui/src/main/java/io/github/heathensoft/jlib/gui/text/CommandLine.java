package io.github.heathensoft.jlib.gui.text;



import io.github.heathensoft.jlib.gui.gfx.Fonts;
import io.github.heathensoft.jlib.gui.gfx.TextBatch;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.lwjgl.window.Window;

import static io.github.heathensoft.jlib.common.utils.U.round;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 19/10/2023
 */


public class CommandLine extends Paragraph implements TextProcessor, ColoredLine {

    protected static final float CURSOR_GLOW = 0.66f;
    protected static final int CURSOR_WAIT = 500;

    protected final int character_capacity;
    protected int cursor_character_index;
    protected boolean valid_field_value;
    protected double last_input_time;
    protected int color_index_valid;
    protected int color_index_invalid;
    protected boolean active_processor;

    public CommandLine() {
        this(Integer.MAX_VALUE);
    }

    public CommandLine(int capacity) {
        this(Word.Type.REGULAR.colorIndex, Word.Type.FAILURE.colorIndex,capacity);
    }

    public CommandLine(int colorIndexValid, int colorIndexInvalid, int capacity) {
        this(null,colorIndexValid,colorIndexInvalid,capacity);
    }

    public CommandLine(String string, int colorIndexValid, int colorIndexInvalid, int capacity) {
        capacity = Math.max(1,capacity);
        if (string != null && string.length() > capacity) {
            string = string.substring(0,capacity);
        } this.character_capacity = capacity;
        this.type = Type.REGULAR;
        this.words = stringToWords(string,type);
        this.cursor_character_index = Math.max(0,length());
        this.valid_field_value = onFieldEdit(toString());
        this.last_input_time = System.currentTimeMillis();
    }
    public void draw(TextBatch batch, float x, float y, float width, int size, boolean centered) {
        Fonts fonts = batch.fonts();
        float scale = fonts.relativeScale(size);
        float desired_width = widthPixels(fonts) * scale;
        float ratio = width / desired_width;
        y -= fonts.ascent() * scale;
        if (ratio < 1) {
            size = round(size * ratio);
            scale = fonts.relativeScale(size);
        } if (centered) x += centeredOffset(fonts,size);
        float space = scale * fonts.advance(' ');
        boolean cursor_visible;
        if (active_processor) {
            double time_since_last = lastInputMillis();
            if (time_since_last >= CURSOR_WAIT) {
                double time = (time_since_last) * 0.001d;
                double sin = Math.sin(time * 1.5d * Math.PI);
                cursor_visible = sin > 0.0d;
            } else cursor_visible = true;
        } else cursor_visible = false;
        int base_info = transparent() ? 0x8000_0000 : 0;
        base_info |= (fonts.currentFont() << 29);
        base_info |= (((size - 1) & 0xFF) << 21);
        int inverted_mask = (1 << 13);
        int cursor_glow_mask = ((round(CURSOR_GLOW * 127.0f) & 0x7F) << 12);
        int glow_mask = ((round(glow() * 127.0f) & 0x7F) << 12);
        int character_index = 0;
        for (Word word : words) {
            int word_color_mask = ((colorIndexOf(word) & 0x3F) << 7);
            byte[] bytes = word.get();
            for (byte b : bytes) {
                char c = (char) (b & 0xFF);
                int info = base_info | word_color_mask;
                if (character_index == cursor_character_index && cursor_visible) {
                    batch.pushVertex(x, y, info | cursor_glow_mask);
                    batch.pushVertex(x, y, info | glow_mask | inverted_mask | c);
                } else { batch.pushVertex(x, y, info | glow_mask | c);
                } x += fonts.advance(c) * scale;
                character_index++;
            } if (character_index == cursor_character_index && cursor_visible) {
                int info = base_info | word_color_mask;
                batch.pushVertex(x, y, info | cursor_glow_mask);
            } character_index++;
            x += space;
        }
    }

    public void set(String string) {
        if (string != null) {
            if (string.length() > character_capacity) {
                string = string.substring(0,character_capacity);
            } this.words = stringToWords(string,type);
            this.cursor_character_index = Math.max(0,length());
            this.valid_field_value = onFieldEdit(toString());
            this.last_input_time = System.currentTimeMillis();
        }
    }

    public void clear() {
        if (!isBlank()) {
            words.clear();
            cursor_character_index = 0;
            valid_field_value = onFieldEdit("");
            last_input_time = System.currentTimeMillis();
        }
    }

    public void keyPress(int key, int mods) {
        if ((mods & GLFW_MOD_CONTROL) > 0) {
            if (key == GLFW_KEY_C) {
                String string = toString();
                Window window = Engine.get().window();
                window.setClipboard(string);
            } else if (key == GLFW_KEY_V) {
                Window window = Engine.get().window();
                String string = window.getClipboard();
                set(string);
            }
        } else  {
            last_input_time = System.currentTimeMillis();
            if (key == GLFW_KEY_ESCAPE) { onKeyEscape(); }
            else if (key == GLFW_KEY_TAB) { onKeyTab();
            }else if (key == GLFW_KEY_DOWN) { onKeyDown();
            } else if (key == GLFW_KEY_UP) { onKeyUp();
            } else if (key == GLFW_KEY_LEFT) {
                if (cursor_character_index > 0) {
                    cursor_character_index--; }
            } else if (key == GLFW_KEY_RIGHT) {
                if (cursor_character_index < length()) {
                    cursor_character_index++; }
            } else if (!isBlank()) {
                if (key == GLFW_KEY_DELETE) clear();
                else if (key == GLFW_KEY_ENTER) {
                    String value = toString();
                    onKeyEnter(value, valid_field_value);
                } else if (key == GLFW_KEY_BACKSPACE) {
                    if (cursor_character_index > 0) {
                        delete(cursor_character_index - 1);
                        valid_field_value = onFieldEdit(toString());
                        cursor_character_index--;
                    }
                }
            }
        }
    }

    public void characterStream(byte character) {
        if (character != 10) {
            last_input_time = System.currentTimeMillis();
            if (cursor_character_index < character_capacity) {
                if (insert(character, cursor_character_index)) {
                    valid_field_value = onFieldEdit(toString());
                    cursor_character_index++;
                }
            }
        }
    }

    public void setColorIndexInvalid(int colorIndex) {
        this.color_index_invalid = colorIndex;
    }

    public void setColorIndexValid(int colorIndex) {
        this.color_index_valid = colorIndex;
    }

    public void onTextProcessorActivated() { active_processor = true; }

    public void onTextProcessorDeactivated() { active_processor = false; }

    public void keyRelease(int key, int mods) { }

    public int characterCapacity() { return character_capacity; }

    protected int cursorIndex() { return cursor_character_index; }

    public boolean fieldIsValid() { return valid_field_value; }

    protected double lastInputMillis() {
        return System.currentTimeMillis() - last_input_time;
    }

    protected boolean showCursor() { return active_processor; }

    /** return whether string is valid*/
    protected boolean onFieldEdit(String value) { return true; }

    protected void onKeyEnter(String value, boolean isValid) { }

    protected void onKeyEscape() { deactivateProcessor(); }

    protected void onKeyTab() { }

    protected void onKeyUp() { }

    protected void onKeyDown() { }

    public int colorIndex() {
        if (valid_field_value)
        return color_index_valid;
        else return color_index_invalid;
    }

    public float glow() { return 0; }

    public boolean transparent() { return !active_processor; }


}
