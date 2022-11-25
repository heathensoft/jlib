package io.github.heathensoft.jlib.lwjgl.window;

import io.github.heathensoft.jlib.common.Assert;
import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.storage.primitive.iterators.IntReader;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;

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
    private final boolean[] keys = new boolean[GLFW_KEY_LAST];
    private final boolean[] pkeys = new boolean[GLFW_KEY_LAST];
    
    private TextProcessor textProcessor = tp_internal;
    private boolean update_required;
    
    public Keyboard() { }
    
    public void process_input() {
        if (update_required) {
            System.arraycopy(keys,0,
            pkeys,0, GLFW_KEY_LAST);
            update_required = false;
        } if (queued_keys.isEmpty()) {
            update_required = false;
        } else { queued_keys.dequeueAll(keyCollector);
            update_required = true;
        } if (!queued_chars.isEmpty()) {
            queued_chars.dequeueAll(charCollector);
        }
    }
    
    public void on_key_event(int key) {
        if (queued_keys.size() == 16) {
            queued_keys.dequeue();
        } queued_keys.enqueue(key);
    }
    
    public void on_char_press(int codepoint) {
        if (queued_chars.size() == 16) {
            queued_chars.dequeue();
        } queued_chars.enqueue(codepoint);
    }
    
    private final IntReader keyCollector = key -> {
        if (key > 0) { keys[key] = true;
            if (key < 0x20 || key == 0x7F)
                textProcessor.npcPress(key);
        } else { key = Math.abs(key);
            keys[key] = false;
            if (key < 0x20 || key == 0x7F) {
                textProcessor.npcRelease(key);
            }
        }
    };
    
    private final IntReader charCollector = key
    -> textProcessor.printable((byte)key);
    
    
    public boolean pressed(int key) {
        if (key > GLFW_KEY_LAST) return false;
        return keys[key];
    }
    
    public boolean pressed(int key1, int key2) {
        return pressed(key1) && pressed(key2);
    }
    
    public boolean just_pressed(int key) {
        if (key >= GLFW_KEY_LAST) return false;
        return keys[key] && !pkeys[key];
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
        return pkeys[key] && !keys[key];
    }
    
    public void setTextProcessor(TextProcessor processor) {
        Assert.notNull("null argument text processor",processor);
        if (textProcessor != processor) {
            queued_chars.dequeueAll(charCollector);
            textProcessor = processor;
        }
    }
    
    private static final TextProcessor tp_internal = new TextProcessor() {
        public void npcPress(int character) {}
        public void npcRelease(int character) {}
        public void printable(byte character) {}
    };
}
