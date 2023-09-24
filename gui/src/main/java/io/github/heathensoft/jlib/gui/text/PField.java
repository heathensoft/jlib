package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 17/09/2023
 */


public abstract class PField extends PColored implements TextProcessor {

    private int pointer;
    private int length;

    protected PField(Color32 color) {
        this(null,color);
    }

    protected PField(String text, Color32 color) {
        super(text, color);
        length = length() - 1;
        pointer = Math.max(0,length);
    }

    public void clear() {
        words = Paragraph.NEW_LINE;
        pointer = length = 0;
        onEdit("");
    }

    public void npcPress(int key) {
        if (!isBlank()) {
            StringBuilder sb;
            if (key == GLFW_KEY_ENTER) {
                onKeyEnter(toString());
            } else if (key == GLFW_KEY_BACKSPACE) {
                if (pointer > 0) {
                    pointer--;
                    sb = new StringBuilder(toString());
                    sb.deleteCharAt(pointer);
                    String string = sb.toString();
                    words = parse(string);
                    length = length() - 1;
                    pointer = Math.min(length,pointer);
                    onEdit(toStringTrimmed());
                }
            } else if (key == GLFW_KEY_DOWN) { onKeyDown();
            } else if (key == GLFW_KEY_UP) { onKeyUp();
            } else if (key == GLFW_KEY_LEFT) {
                if (pointer > 0) pointer--;
            } else if (key == GLFW_KEY_RIGHT) {
                if (pointer < length) pointer++;
            } else if (key == GLFW_KEY_DELETE) {
                words = Paragraph.NEW_LINE;
                pointer = length = 0;
                onEdit("");
            }
        }
    }

    public void npcRelease(int key) { }

    public void printable(byte character) {
        if (character == 32) {
            if (!isBlank() && pointer != 0) {
                byte prev = charAt(pointer - 1);
                if (prev != 32) {
                    if (pointer == length) {
                        byte[] w1 = words[words.length - 2].get();
                        byte[] w2 = Arrays.copyOf(w1,w1.length + 1);
                        w2[w2.length - 1] = 32;
                        words[words.length - 2] = new Word(w2);
                        pointer++;
                        length++;
                        onEdit(toStringTrimmed());
                    }  else if (charAt(pointer) != 32) {
                        String s = toString();
                        StringBuilder sb = new StringBuilder(s.length() + 4);
                        sb.append(s).insert(pointer,(char) 32);
                        words = parse(sb.toString());
                        length = length() - 1;
                        pointer++;
                        onEdit(toStringTrimmed());
                    }
                }
            }
        } else if (character != 10) {
            if (pointer == length) {
                if (isBlank()) {
                    words = new Word[2];
                    words[0] = new Word(Character.toString((char) character));
                    words[1] = Paragraph.EOL;
                } else {
                    byte[] w1 = words[words.length - 2].get();
                    byte[] w2 = Arrays.copyOf(w1,w1.length + 1);
                    w2[w2.length - 1] = character;
                    words[words.length - 2] = new Word(w2);
                }
            } else {
                String s = toString();
                StringBuilder sb = new StringBuilder(s.length() + 4);
                sb.append(s).insert(pointer,(char) character);
                words = parse(sb.toString());
            } onEdit(toStringTrimmed());
            pointer++;
            length++;
        }
    }

    protected abstract void onKeyEnter(String value);

    protected abstract void onEdit(String value);

    protected abstract void onKeyUp();

    protected abstract void onKeyDown();

}
