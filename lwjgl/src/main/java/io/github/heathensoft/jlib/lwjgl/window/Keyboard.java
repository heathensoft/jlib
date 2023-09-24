package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;

import static org.lwjgl.glfw.GLFW.*;

/**
 * ASCII table:
 * https://learn.parallax.com/support/reference/ascii-table-0-127
 *
 * @author Frederik Dahl
 * 13/11/2022
 */


public class Keyboard {


    private final IntQueue queued_keys = new IntQueue(16);
    private final IntQueue queued_chars = new IntQueue(16);
    private final boolean[] c_keys = new boolean[GLFW_KEY_LAST];
    private final boolean[] p_keys = new boolean[GLFW_KEY_LAST];
    private TextProcessor textProcessor = tp_internal;
    private boolean update_required;
    
    public Keyboard() { }
    
    public void process_input() {
        if (update_required) {
            System.arraycopy(c_keys,0,
            p_keys,0, GLFW_KEY_LAST);
            update_required = false;
        } if (queued_keys.isEmpty()) {
            update_required = false;
        } else {
            while (!queued_keys.isEmpty()) {
                int key = queued_keys.dequeue();
                int mods = queued_keys.dequeue();
                if (key > 0) { c_keys[key] = true;
                    textProcessor.npcPress(key,mods);
                } else { key = Math.abs(key);
                    c_keys[key] = false;
                    textProcessor.npcRelease(key,mods);
                }
            } update_required = true;
        } while (!queued_chars.isEmpty()) {
            int character = queued_chars.dequeue();
            textProcessor.printable((byte) character);
        }
    }
    
    public void on_key_event(int key, int mods) {
        if (queued_keys.size() == 32) {
            queued_keys.dequeue();
            queued_keys.dequeue();
        } queued_keys.enqueue(key);
        queued_keys.enqueue(mods);
    }
    
    public void on_char_press(int codepoint) {
        if (queued_chars.size() == 16) {
            queued_chars.dequeue();
        } queued_chars.enqueue(codepoint);
    }


    public boolean pressed(int key) {
        if (key > GLFW_KEY_LAST) return false;
        return c_keys[key];
    }
    
    public boolean pressed(int key1, int key2) {
        return pressed(key1) && pressed(key2);
    }
    
    public boolean just_pressed(int key) {
        if (key >= GLFW_KEY_LAST) return false;
        return c_keys[key] && !p_keys[key];
    }
    
    /**
     * @param key key
     * @param mod mod (i.e. ctrl, alt, shift etc.)
     * @return mod is pressed, and key is just pressed
     */
    public boolean just_pressed(int key, int mod) {
        return pressed(mod) && just_pressed(key);
    }
    
    public boolean just_released(int key) {
        return p_keys[key] && !c_keys[key];
    }
    
    public void setTextProcessor(TextProcessor processor) {
        processor = processor == null ? tp_internal : processor;
        if (textProcessor != processor) {
            queued_chars.clear();
            queued_keys.clear();
            textProcessor = processor;
        }
    }
    
    private static final TextProcessor tp_internal = new TextProcessor() {
        public void npcPress(int key, int mods) {}
        public void npcRelease(int key, int mods) {}
        public void printable(byte character) {}
    };
}
