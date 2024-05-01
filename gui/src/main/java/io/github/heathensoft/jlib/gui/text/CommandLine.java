package io.github.heathensoft.jlib.gui.text;



import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.gfx.TextBatchGUI;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.lwjgl.window.Window;
import org.joml.Vector4f;

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
    protected boolean active_processor;
    protected Vector4f color_valid;
    protected Vector4f color_invalid;

    public CommandLine() {
        this(Integer.MAX_VALUE);
    }

    public CommandLine(int capacity) {
        this(new Vector4f(Word.Type.REGULAR.color),new Vector4f(Word.Type.FAILURE.color),capacity);
    }

    public CommandLine(Vector4f color_valid, Vector4f color_invalid, int capacity) {
        this(null,color_valid,color_invalid,capacity);
    }

    public CommandLine(String string, Vector4f color_valid, Vector4f color_invalid, int capacity) {
        capacity = Math.max(1,capacity);
        if (string != null && string.length() > capacity) {
            string = string.substring(0,capacity);
        } this.character_capacity = capacity;
        this.type = Type.REGULAR;
        this.words = stringToWords(string,type);
        this.cursor_character_index = Math.max(0,length());
        this.valid_field_value = onFieldEdit(toString());
        this.last_input_time = System.currentTimeMillis();
        this.color_invalid = color_invalid;
        this.color_valid = color_valid;
    }

    public void drawDynamicSize(TextBatchGUI batch, float x, float y, float width, float size, boolean centered, float alpha) {
        FontsGUI fonts = batch.fonts();
        float scale = fonts.relativeScale(size);
        float width_pixels = widthPixels(fonts) + fonts.averageAdvance();
        float desired_width = width_pixels * scale;
        float ratio = width / desired_width;
        y -= fonts.ascent() * scale;
        if (ratio < 1) {
            size = size * ratio;
            scale = fonts.relativeScale(size);
            desired_width = width_pixels * scale;
        } if (centered) x += TextUtils.center_offset_x(fonts,desired_width,width);
        if (size > 1f) {
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
            int info_bits = (fonts.currentFont() << 29);
            info_bits |= (((round(size )- 1) & 0xFF) << 21);
            int inverted_mask = (1 << 20);
            int cursor_glow_mask = ((round(CURSOR_GLOW * 127.0f) & 0x7F) << 13);
            int glow_mask = ((round(glow() * 127.0f) & 0x7F) << 13);
            int character_index = 0;

            Vector4f color_vec4 = U.popSetVec4(color());
            color_vec4.w *= alpha;
            float color = Color.rgb_to_floatBits(color_vec4);
            U.pushVec4();

            for (Word word : words) {
                byte[] bytes = word.get();
                for (byte b : bytes) {
                    char character = (char) (b & 0xFF);
                    if (character_index == cursor_character_index && cursor_visible) {
                        batch.pushVertex(x, y, color,info_bits | cursor_glow_mask);
                        batch.pushVertex(x, y, color,info_bits | glow_mask | inverted_mask | character);
                    } else { batch.pushVertex(x, y, color, info_bits | glow_mask | character);
                    } x += fonts.advance(character) * scale;
                    character_index++;
                } if (character_index == cursor_character_index && cursor_visible) {
                    batch.pushVertex(x, y, color, info_bits | cursor_glow_mask);
                } character_index++;
                x += space;
            }
        }
    }

    public void set(String string) {
        if (string != null) {
            if (string.length() > character_capacity) {
                string = string.substring(0,character_capacity);
            }
            this.words = stringToWords(string,type);
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

    public void charPress(byte character) {
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

    public Vector4f validColor() { return color_valid; }

    public Vector4f invalidColor() { return color_invalid; }

    public boolean isActiveInputProcessor() { return active_processor; }

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

    protected void onKeyEscape() { deactivateTextProcessor(); }

    protected void onKeyTab() { }

    protected void onKeyUp() { }

    protected void onKeyDown() { }

    public Vector4f color() {
        return valid_field_value ? color_valid : color_invalid;
    }

    public float glow() { return 0; }

    public boolean transparent() { return !active_processor; }


}
