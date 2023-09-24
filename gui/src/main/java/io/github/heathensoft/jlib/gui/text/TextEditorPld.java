package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;


/**
 * @author Frederik Dahl
 * 17/09/2023
 */


public class TextEditorPld implements TextProcessor {


    private StringBuilder builder; // ONE global builder instead. TextLine extends paragraph. This extends text
    private final List<TextLine> lines;
    private int line_index = 0;
    private int char_index = 0;


    public TextEditorPld() {
        lines = new LinkedList<>();
        lines.add(new TextLine());
    }

    public Text getText() {
        Text text = new ParagraphList();
        for (TextLine line : lines) {
            text.add(line.paragraph);
        } return text;
    }

    public void npcPress(int key, int mods) {
        if (!lines.isEmpty()) {

            if (key == GLFW_KEY_UP) {
                if (line_index > 0) {
                    line_index--;
                    int max_index = current_char_index_max();
                    char_index = Math.min(max_index,char_index);
                }
            } else if (key == GLFW_KEY_RIGHT) {
                if (char_index < current_char_index_max()) {
                    char_index++;
                } else {
                    if (line_index != current_line_index_max()) {
                        line_index++;
                        char_index = 0;
                    }
                }
            } else if (key == GLFW_KEY_DOWN) {
                if (line_index != current_line_index_max()) {
                    line_index++;
                    int max_index = current_char_index_max();
                    char_index = Math.min(max_index,char_index);
                }
            } else if (key == GLFW_KEY_LEFT) {
                if (char_index > 0) {
                    char_index--;
                } else {
                    if (line_index > 0) {
                        line_index--;
                        char_index = current_char_index_max();
                    }
                }
            }
        }


    }


    public void npcRelease(int key, int mods) { }


    public void printable(byte character) {

        TextLine line = lines.get(line_index);
        if (character == 32) { // Space
            if (!line.isEmpty() && char_index != 0) {
                char prev = line.get(char_index - 1);
                if (prev != 32) {
                    if (current_char_index_max() == char_index) {
                        line.append(character);
                    } else line.insert(character,char_index);
                    char_index++;
                }
            }
        } else {
            if (character < 39 && character != 34 ) {
                System.out.println("!#$%&");
            } else {
                if (current_char_index_max() == char_index) {
                    line.append(character);
                } else line.insert(character,char_index);
                char_index++;
            }
        }
    }

    private int current_char_index_max() {
        return lines.get(line_index).length();
    }

    private int current_line_index_max() {
        return Math.max(0,lines.size() - 1);
    }

    private static final class TextLine  { // Extend paragraph

        Paragraph paragraph;
        StringBuilder line;

        TextLine() {
            line = new StringBuilder(128);
            paragraph = Paragraph.EMPTY_PARAGRAPH;
        }

        void append(TextLine line) {
            this.line.append(line.line);
            paragraph = new Paragraph(line.toString());
        }

        void append(byte c) {
            line.append((char) c);
            paragraph = new Paragraph(line.toString());
        }

        void insert(byte c, int index) {
            line.insert(index,(char) c);
            paragraph = new Paragraph(line.toString());
        }

        char get(int index) {
            char[] dst = new char[1];
            line.getChars(index,index + 1,dst,0);
            return dst[0];
        }

        int length() {
            return line.length();
        }

        boolean isEmpty() {
            return line.isEmpty();
        }

        Paragraph paragraph() {
            return paragraph;
        }

    }
}
