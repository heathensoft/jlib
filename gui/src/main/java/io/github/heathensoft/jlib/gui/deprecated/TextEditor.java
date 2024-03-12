package io.github.heathensoft.jlib.gui.deprecated;

import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The Text is not copied! Editing the text elsewhere will cause errors.
 *
 * @author Frederik Dahl
 * 23/09/2023
 */


public abstract class TextEditor implements TextProcessor {

    private final PlainText text;
    private int lineIndex;
    private int charIndex;
    private int desiredIndex;


    public TextEditor() {
        this(new PlainText());
    }

    public TextEditor(PlainText text) {
        if (text.isBlank()) text.add("");
        this.text = text;
        this.lineIndex = 0;
        this.charIndex = 0;
        this.desiredIndex = 0;
    }

    public void keyPress(int key, int mods) {

        if ((mods & GLFW_MOD_CONTROL) > 0) {
            if (key == GLFW_KEY_DELETE) {
                clear();
            }
        } else {
            if (key == GLFW_KEY_UP) {
                if (lineIndex > 0) {
                    lineIndex--;
                    int max = charIndexMax(lineIndex);
                    charIndex = Math.min(desiredIndex, max);
                    onMoveUp(lineIndex);
                }
            } else if (key == GLFW_KEY_DOWN) {
                if (lineIndex < lineIndexMax()) {
                    lineIndex++;
                    int max = charIndexMax(lineIndex);
                    charIndex = Math.min(desiredIndex, max);
                    onMoveDown(lineIndex);
                }
            } else if (key == GLFW_KEY_RIGHT) {
                if (charIndex < charIndexMax(lineIndex)) {
                    charIndex++;
                    desiredIndex = charIndex;
                } else {
                    if (lineIndex < lineIndexMax()) {
                        lineIndex++;
                        charIndex = 0;
                        desiredIndex = charIndex;
                        onMoveDown(lineIndex);
                    }
                }
            } else if (key == GLFW_KEY_LEFT) {
                if (charIndex > 0) {
                    charIndex--;
                    desiredIndex = charIndex;
                } else {
                    if (lineIndex > 0) {
                        lineIndex--;
                        charIndex = lineIndexMax();
                        desiredIndex = charIndex;
                        onMoveDown(lineIndex);
                    }
                }
            } else if (key == GLFW_KEY_ENTER) {
                PlainParagraph p = text.get(lineIndex).split(charIndex);
                lineIndex++;
                text.add(p,lineIndex);
                charIndex = 0;
                desiredIndex = charIndex;
                onMoveDown(lineIndex);
                onEdit();
            } else if (key == GLFW_KEY_BACKSPACE) {
                if (charIndex > 0) {
                    PlainParagraph p = text.get(lineIndex);
                    if (p.deleteChar(charIndex - 1)) {
                        charIndex--;
                        desiredIndex = charIndex;
                        onEdit();
                    }
                } else if (lineIndex > 0) {
                    PlainParagraph removed = text.remove(lineIndex);
                    lineIndex--;
                    PlainParagraph p = text.get(lineIndex);
                    p.trim();
                    charIndex = charIndexMax(lineIndex);
                    p.append(removed);
                    if (p.charAt(charIndex) == 32)
                        p.deleteChar(charIndex);
                    onMoveUp(lineIndex);
                    desiredIndex = charIndex;
                    onEdit();
                }
            }  else if (key == GLFW_KEY_PAGE_DOWN) {
                if (lineIndex < lineIndexMax()) {
                    lineIndex = lineIndexMax();
                    charIndex = charIndexMax(lineIndex);
                    desiredIndex = charIndex;
                    onMoveDown(lineIndex);
                }
            } else if (key == GLFW_KEY_PAGE_UP) {
                if (lineIndex > 0) {
                    lineIndex = 0;
                    charIndex = 0;
                    desiredIndex = 0;
                    onMoveUp(lineIndex);
                }
            }

        }




    }

    public void keyRelease(int key, int mods) { }

    public void charPress(byte character) {
        if (character != 10) {
            PlainParagraph p = text.get(lineIndex);
            if (p.insert(character,charIndex)) {
                charIndex++;
                desiredIndex = charIndex;
                onEdit();
            }
        }
    }

    protected abstract void onMoveUp(int lineIndex);

    protected abstract void onMoveDown(int lineIndex);

    protected abstract void onEdit();

    public boolean isBlank() {
        if (text.size() == 1) {
            return text.get(0).isBlank();
        } return false;
    }

    public void clear() {
        if (!isBlank()) {
            text.clear();
            text.add("");
            lineIndex = 0;
            charIndex = 0;
            desiredIndex = 0;
            onEdit();
        }
    }

    public int currentLineIndex() {
        return lineIndex;
    }

    public int currentCharIndex() {
        return charIndex;
    }

    public PlainText text() {
        return text;
    }


    private int charIndexMax(int line) {
        return text.get(line).numPrintable();
    }

    private int lineIndexMax() {
        int index = text.size() - 1;
        if (index < 0) throw new IllegalStateException("index < 0");
        return index;
    }

}
