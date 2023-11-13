package io.github.heathensoft.jlib.gui.deprecated;

import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.lwjgl.window.Window;

import static org.lwjgl.glfw.GLFW.*;

/**
 *
 * This class has some useful default methods to override.
 *
 * @author Frederik Dahl
 * 20/09/2023
 */


public class TextField extends PlainParagraph implements TextProcessor {

    private final int character_capacity;
    private int cursor_character_index;
    private boolean valid_field_value;
    private double last_input_time;

    public TextField() {
        this(null);
    }

    public TextField(int capacity) {
        this(null,capacity);
    }

    public TextField(String string) {
        this(string,Integer.MAX_VALUE);
    }

    public TextField(String string, int capacity) {
        capacity = Math.max(1,capacity);
        if (string != null && string.length() > capacity) {
            string = string.substring(0,capacity);
        } this.character_capacity = capacity;
        this.words = stringToWords(string);
        this.length = calculateLength(words);
        this.cursor_character_index = Math.max(0,numPrintable());
        this.valid_field_value = validateField(toStringTrimmed());
        this.last_input_time = System.currentTimeMillis();
    }

    public void set(String string) {
        if (string != null) {
            if (string.length() > character_capacity) {
                string = string.substring(0,character_capacity);
            } this.words = stringToWords(string);
            this.length = calculateLength(words);
            this.cursor_character_index = Math.max(0,numPrintable());
            string = toStringTrimmed();
            this.valid_field_value = validateField(string);
            this.last_input_time = System.currentTimeMillis();
            onFieldEdit(string);
        }
    }

    public void clear() {
        if (!isBlank()) {
            words = new Word[] { Word.END_OF_LINE() };
            cursor_character_index = 0;
            length = 1;
            onFieldEdit("");
            valid_field_value = validateField("");
            last_input_time = System.currentTimeMillis();
        }
    }

    public void keyPress(int key, int mods) {
        if ((mods & GLFW_MOD_CONTROL) > 0) {
            if (key == GLFW_KEY_C) {
                String string = toStringTrimmed();
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
                if (cursor_character_index < numPrintable()) {
                    cursor_character_index++; }
            } else if (!isBlank()) {
                if (key == GLFW_KEY_DELETE) clear();
                else if (key == GLFW_KEY_ENTER) {
                    String value = toStringTrimmed();
                    valid_field_value = validateField(value);
                    onKeyEnter(value);
                } else if (key == GLFW_KEY_BACKSPACE) {
                    if (cursor_character_index > 0 &&
                        deleteChar(cursor_character_index -1)) {
                        String value = toStringTrimmed();
                        valid_field_value = validateField(value);
                        onFieldEdit(value);
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
                    String value = toStringTrimmed();
                    valid_field_value = validateField(value);
                    onFieldEdit(value);
                    cursor_character_index++;
                }
            }
        }
    }

    public void onTextProcessorActivated() {

    }

    public void onTextProcessorDeactivated() {

    }


    public void keyRelease(int key, int mods) { }

    public int characterCapacity() { return character_capacity; }

    public int cursorIndex() { return cursor_character_index; }

    public boolean fieldIsValid() { return valid_field_value; }

    public double lastInputMillis() {
        return System.currentTimeMillis() - last_input_time;
    }

    protected boolean validateField(String value) { return true; }

    protected boolean showCursor() { return false; }

    protected void onFieldEdit(String value) { }

    protected void onKeyEnter(String value) { }

    protected void onKeyEscape() { }

    protected void onKeyTab() { }

    protected void onKeyUp() { }

    protected void onKeyDown() { }
}
