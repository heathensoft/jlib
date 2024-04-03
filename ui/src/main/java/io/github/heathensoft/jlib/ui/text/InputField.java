package io.github.heathensoft.jlib.ui.text;

import io.github.heathensoft.jlib.common.Executor;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.gfx.TextBatchGUI;
import org.joml.primitives.Rectanglef;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.heathensoft.jlib.ui.gfx.FontsGUI.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 03/04/2024
 */


public class InputField {

    private static final int CHARACTER_CAP_MAX = 128;
    private static final Predicate<String> always_valid = s -> true;
    private Executor navigate_down = Executor.voidExecutor;
    private Executor navigate_up = Executor.voidExecutor;
    private Predicate<String> validate;
    private Consumer<String> submit;
    private String value;
    private final StringBuilder builder;
    private boolean valid_value;
    private final int capacity;
    private int cursor_pointer;
    private double last_input_time;

    public InputField(Consumer<String> submit) { this(submit,CHARACTER_CAP_MAX); }
    public InputField(Consumer<String> submit, int capacity) { this(submit,always_valid,capacity); }
    public InputField(Consumer<String> submit, Predicate<String> validate, int capacity) {
        this.last_input_time = System.currentTimeMillis();
        this.builder = new StringBuilder(capacity);
        this.capacity = Math.max(1,capacity);
        this.submit = submit;
        this.validate = validate;
        this.value = "";
    }

    public void draw(RendererGUI renderer, Rectanglef bounds, int abgr, float glow, boolean show_cursor) {
        if (bounds.isValid()) {
            TextBatchGUI batch = renderer.textBatch();
            FontsGUI fonts = renderer.fonts();
            float color = Color.intBits_to_floatBits(abgr);
            float width = bounds.lengthX();
            float size = bounds.lengthY();
            float x = bounds.minX;
            float y = bounds.maxY;
            float scale = fonts.relativeScale(size);
            float width_unscaled = fonts.advanceSum(value);
            float desired_width = width_unscaled * scale;
            int num_characters = value.length();
            if (show_cursor && cursor_pointer == num_characters) {
                desired_width += fonts.averageAdvance() * scale;
            } y -= fonts.ascent() * scale;
            if (desired_width > width) {
                float ratio = width / desired_width;
                size = size * ratio;
                if (size < 1f) return;
                scale = fonts.relativeScale(size);
            } if (size > 2) {
                int info_bits = bits_font(fonts.currentFont());
                info_bits = bits_set_size(info_bits,size);
                info_bits = bits_set_glow(info_bits,glow);
                if (show_cursor) {
                    boolean cursor_visible;
                    double time_since_last = lastInputMillis();
                    if (time_since_last >= 500) {
                        double time = (time_since_last) * 0.001d;
                        double sin = Math.sin(time * 1.5d * Math.PI);
                        cursor_visible = sin > 0.0d;
                    } else cursor_visible = true;
                    if (num_characters == 0) {
                        if (cursor_visible) {
                            batch.pushVertex(x,y,color,info_bits);
                        }
                    } else {
                        if (cursor_visible) {
                            int character_index = 0;
                            for (int i = 0; i < num_characters; i++) {
                                char c = (char)(value.charAt(i) & 0x7F);
                                if (character_index == cursor_pointer) {
                                    int color_inv = FontsGUI.bits_invert_color(info_bits);
                                    batch.pushVertex(x,y,color,info_bits);
                                    batch.pushVertex(x,y,color,color_inv | c);
                                } else batch.pushVertex(x,y,color,info_bits | c);
                                x += fonts.advance(c) * scale;
                                character_index++;
                            } if (character_index == cursor_pointer) {
                                batch.pushVertex(x,y,color,info_bits);
                            }
                        } else {
                            for (int i = 0; i < num_characters; i++) {
                                char c = (char)(value.charAt(i) & 0x7F);
                                batch.pushVertex(x,y,color,info_bits | c);
                                x += fonts.advance(c) * scale;
                            }
                        }
                    }
                } else if (num_characters > 0){
                    for (int i = 0; i < num_characters; i++) {
                        char c = (char)(value.charAt(i) & 0x7F);
                        batch.pushVertex(x,y,color,info_bits | c);
                        x += fonts.advance(c) * scale;
                    }
                }
            }
        }
    }

    public void keyPress(int key, int mods) {
        if ((mods & GLFW_MOD_CONTROL) > 0) {
            if (key == GLFW_KEY_C) {
                Engine.get().window().setClipboard(value);
            } else if (key == GLFW_KEY_V) {
                set(Engine.get().window().getClipboard());}
        } else { last_input_time = System.currentTimeMillis();
            if (key == GLFW_KEY_DOWN) { navigate_down.execute();
            } else if (key == GLFW_KEY_UP) { navigate_up.execute();
            } else if (key == GLFW_KEY_LEFT) {
                if (cursor_pointer > 0) {
                    cursor_pointer--; }
            } else if (key == GLFW_KEY_RIGHT) {
                if (cursor_pointer < value.length()) {
                    cursor_pointer++; }
            } else if (!value.isEmpty()) {
                if (key == GLFW_KEY_DELETE) clear();
                else if (key == GLFW_KEY_ENTER) {
                    submit.accept(value);
                } else if (key == GLFW_KEY_BACKSPACE) {
                    if (cursor_pointer > 0) {
                        builder.deleteCharAt(cursor_pointer - 1);
                        value = builder.toString();
                        valid_value = validate.test(value);
                        cursor_pointer--;
                    }
                }
            }
        }
    }

    public void charPress(byte character) {
        if (character != 10) {
            last_input_time = System.currentTimeMillis();
            if (cursor_pointer < capacity) {
                builder.insert(cursor_pointer++,(char)character);
                value = builder.toString();
                valid_value = validate.test(value);
            }
        }
    }

    public void clear() {
        if (!value.isEmpty()) {
            cursor_pointer = 0;
            builder.delete(0, builder.length()-1);
            valid_value = validate.test(value = "");
            last_input_time = System.currentTimeMillis();
        }
    }

    public void set(String string) {
        if (string != null) {
            if (string.length() > capacity) {
                string = string.substring(0, capacity);
            } cursor_pointer = string.length();
            if (!builder.isEmpty()) {
                builder.delete(0, builder.length()-1);
            } builder.append(string);
            value = builder.toString();
            valid_value = validate.test(value);
            last_input_time = System.currentTimeMillis();
        }
    }

    public void setSubmit(Consumer<String> consumer) { this.submit = consumer; }
    public void setValidate(Predicate<String> predicate) { this.validate = predicate; }
    public void setNavigateDown(Executor executor) { this.navigate_down = executor; }
    public void setNavigateUp(Executor executor) { this.navigate_up = executor; }
    public String toString() { return value; }
    public int characterCapacity() { return capacity; }
    public int cursorIndex() { return cursor_pointer; }
    public boolean fieldIsValid() { return valid_value; }
    public double lastInputMillis() { return System.currentTimeMillis() - last_input_time; }
    public void resetCursorTimer() { last_input_time = System.currentTimeMillis(); }

}
