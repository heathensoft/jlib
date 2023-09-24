package io.github.heathensoft.jlib.gui.textnew;

import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 20/09/2023
 */


public class CommandLine extends PlainParagraph implements TextProcessor {

    private int pointer;

    public CommandLine() { this(null); }

    public CommandLine(String string) {
        super(string);
        pointer = Math.max(0,numPrintable());
    }

    public void clear() {
        if (!isBlank()) {
            words = new Word[] { Word.END_OF_LINE() };
        } pointer = 0;
        length = 1;
        onEdit("");
    }

    public void npcPress(int key, int mods) {
        if (!isBlank()) {
            if (key == GLFW_KEY_ENTER) {
                onKeyEnter(toStringTrimmed());
            } else if (key == GLFW_KEY_BACKSPACE) {
                if (pointer > 0 && deleteChar(pointer-1)) {
                    onEdit(toStringTrimmed());
                    pointer--;
                }
            } else if (key == GLFW_KEY_DOWN) { onKeyDown();
            } else if (key == GLFW_KEY_UP) { onKeyUp();
            } else if (key == GLFW_KEY_LEFT) {
                if (pointer > 0) pointer--;
            } else if (key == GLFW_KEY_RIGHT) {
                if (pointer < numPrintable()) {
                    pointer++;
                }
            } else if (key == GLFW_KEY_DELETE) {
                clear();
            }
        }
    }

    public void printable(byte character) {
        if (character != 10 && insert(character,pointer)) {
            onEdit(toStringTrimmed());
            pointer++;
        }
    }

    public void npcRelease(int key, int mods) { }

    protected void onKeyEnter(String value) { }

    protected void onEdit(String value) { }

    protected void onKeyUp() { }

    protected void onKeyDown() { }
}
