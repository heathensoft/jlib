package io.github.heathensoft.jlib.ui.text;

import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.TextBatchGUI;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;


/**
 * Text is also a working text editor.
 * Add more functionality when needed.
 *
 *
 * @author Frederik Dahl
 * 25/02/2024
 */


public class Text implements Iterable<Paragraph> {

    protected LinkedList<Paragraph> lines;
    protected int cursor_char_index;
    protected int cursor_line_index;
    protected int desired_char_index;

    public Text() { lines = new LinkedList<>(); }

    public Text(String string) {
        lines = new LinkedList<>();
        stringToText(string,lines);
    }

    public void draw(TextBatchGUI batch, Rectanglef bounds, float y_offset,
        float size, float glow, boolean wrap, boolean show_cursor) {
        FontsGUI fonts = batch.fonts();
        float width = bounds.lengthX();
        float scale = fonts.relativeScale(size);
        float space = fonts.advance(' ') * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_gap = fonts.lineGap() * scale;
        float y = bounds.maxY + y_offset, x;

        int bits = FontsGUI.bits_font(fonts.currentFont());
        bits = FontsGUI.bits_set_size(bits,size);
        bits = FontsGUI.bits_set_glow(bits,glow);

        if (show_cursor) {
            int char_index, line_index = 0;
            for (Paragraph line : lines) {
                if (y <= bounds.minY) return;
                char_index = 0;
                x = bounds.minX;
                y -= ascent;
                if (wrap) {
                    if (line.isBlank() && line_index == cursor_line_index) {
                        if ((y - descent) < bounds.maxY) {
                            float color_float_bits = Color.rgb_to_floatBits(Paragraph.Type.REGULAR.color);
                            batch.pushVertex(x,y,color_float_bits,bits); }
                    } else for (Word word : line) {
                        float word_width = word.width() * scale;
                        if ((x + word_width) > bounds.maxX && x > bounds.minX) {
                            if (y < bounds.minY) return;
                            x = bounds.minX; y -= size;
                        } int word_length = word.length();
                        if ((y - descent) < bounds.maxY) {
                            Vector4f rgb = Paragraph.color_of(line,word);
                            float color_float_bits = Color.rgb_to_floatBits(rgb);
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break;
                                char c = (char) (word.get(i) & 0x7F);
                                if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                    batch.pushVertex(x,y,color_float_bits,bits);
                                    batch.pushVertex(x,y,color_float_bits,FontsGUI.bits_invert_color(bits | c));
                                } else batch.pushVertex(x,y,color_float_bits,bits | c);
                                x += (fonts.advance(c) * scale);
                                char_index++;
                            } if (line_index == cursor_line_index && char_index == cursor_char_index)
                                batch.pushVertex(x,y,color_float_bits,bits);
                        } else for (int i = 0; i < word_length; i++) {
                            if (x >= bounds.maxX) break;
                            char c = (char) (word.get(i) & 0x7F);
                            x += (fonts.advance(c) * scale);
                            char_index++;
                        } char_index++;
                        x += space;
                    }
                } else {
                    if (line.isBlank() && line_index == cursor_line_index) {
                        if ((y - descent) < bounds.maxY) {
                            float color_float_bits = Color.rgb_to_floatBits(Paragraph.Type.REGULAR.color);
                            batch.pushVertex(x,y,color_float_bits,bits);
                        }
                    } else {
                        if ((y - descent) < bounds.maxY) {
                            next_line:
                            for (Word word : line) {
                                Vector4f rgb = Paragraph.color_of(line,word);
                                float color_float_bits = Color.rgb_to_floatBits(rgb);
                                int word_length = word.length();
                                for (int i = 0; i < word_length; i++) {
                                    if (x >= bounds.maxX) break next_line;
                                    char c = (char) (word.get(i) & 0x7F);
                                    if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                        batch.pushVertex(x,y,color_float_bits,bits);
                                        batch.pushVertex(x,y,color_float_bits,FontsGUI.bits_invert_color(bits | c));
                                    } else batch.pushVertex(x,y,color_float_bits,bits | c);
                                    x += (fonts.advance(c) * scale);
                                    char_index++;
                                } if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                    batch.pushVertex(x,y,color_float_bits,bits);
                                } char_index++;
                                x += space;
                            }
                        }
                    }
                } line_index++;
                y -= (descent + line_gap);
            }
        } else {
            for (Paragraph line : lines) {
                if (y <= bounds.minY) return;
                x = bounds.minX; y -= ascent;
                if (wrap) {
                    for (Word word : line) {
                        int word_length = word.length();
                        float word_width = word.width() * scale;
                        if ((x + word_width) > bounds.maxX && x > bounds.minX) {
                            if (y < bounds.minY) return;
                            x = bounds.minX; y -= size;
                        } if ((y - descent) < bounds.maxY) {
                            Vector4f rgb = Paragraph.color_of(line,word);
                            float color_float_bits = Color.rgb_to_floatBits(rgb);
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break;
                                char c = (char) (word.get(i) & 0x7F);
                                batch.pushVertex(x,y,color_float_bits,bits | c );
                                x += (fonts.advance(c) * scale); }
                        } else for (int i = 0; i < word_length; i++) {
                            if (x >= bounds.maxX) break;
                            char c = (char) (word.get(i) & 0x7F);
                            x += (fonts.advance(c) * scale);
                        } x += space;
                    }
                } else {
                    if ((y - descent) < bounds.maxY) {
                        next_line:
                        for (Word word : line) {
                            Vector4f rgb = Paragraph.color_of(line,word);
                            float color_float_bits = Color.rgb_to_floatBits(rgb);
                            int word_length = word.length();
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break next_line;
                                char c = (char) (word.get(i) & 0x7F);
                                batch.pushVertex(x,y,color_float_bits,bits | c);
                                x += (fonts.advance(c) * scale);
                            } x += space;
                        }
                    }
                } y -= (descent + line_gap);
            }
        }
    }

    public void keyPress(int key, int mods) {

        if ((mods & GLFW_MOD_ALT) > 0) {


            if ((mods & GLFW_MOD_CONTROL) > 0) {


            } else if ((mods & GLFW_MOD_SHIFT) > 0) {

                if (key == GLFW_KEY_UP) {
                    if (cursor_line_index > 0) {
                        Paragraph line = lines.remove(cursor_line_index);
                        cursor_line_index--;
                        lines.add(cursor_line_index,line);
                    }
                } else if (key == GLFW_KEY_DOWN) {
                    if (cursor_line_index < (mumLines() - 1)) {
                        Paragraph line = lines.remove(cursor_line_index);
                        cursor_line_index++;
                        lines.add(cursor_line_index,line);
                    }
                }

            }

        } else if ((mods & GLFW_MOD_CONTROL) > 0) {

            if (key == GLFW_KEY_RIGHT) {
                if (!isBlank()) {
                    Paragraph current_line = lines.get(cursor_line_index);
                    if (current_line.isBlank()) {
                        if ((lines.size() - 1) > cursor_line_index) {
                            cursor_line_index++;
                            cursor_char_index = 0;
                            desired_char_index = 0;
                        }
                    } else  {
                        int line_length = current_line.length();
                        if (cursor_char_index == line_length) {
                            if ((lines.size() - 1) > cursor_line_index) {
                                cursor_line_index++;
                                cursor_char_index = 0;
                                desired_char_index = 0;
                            }
                        } else {
                            cursor_char_index = line_length;
                            desired_char_index = cursor_char_index;
                        }
                    }
                }
            } else if (key == GLFW_KEY_LEFT) {
                if (!isBlank()) {
                    if (cursor_char_index == 0) {
                        if (cursor_line_index > 0) {
                            cursor_line_index--;
                            cursor_char_index = lines.get(cursor_line_index).length();
                            desired_char_index = cursor_char_index;
                        }
                    } else {
                        cursor_char_index = 0;
                        desired_char_index = 0;
                    }
                }
            } else if (key == GLFW_KEY_UP) {
                if (!isBlank()) {
                    cursor_char_index = 0;
                    if (cursor_line_index > 0) {
                        Paragraph current_line = lines.get(cursor_line_index);
                        if (current_line.isBlank()) {
                            while (current_line.isBlank()) {
                                if (cursor_line_index == 0) break;
                                cursor_line_index--;
                                current_line = lines.get(cursor_line_index);
                            }
                        }
                        while (!current_line.isBlank()) {
                            if (cursor_line_index == 0) break;
                            cursor_line_index--;
                            current_line = lines.get(cursor_line_index);
                        }
                    }
                }
            } else if (key == GLFW_KEY_DOWN) {
                if (!isBlank()) {
                    cursor_char_index = 0;
                    int cursor_max_index = lines.size() - 1;
                    if (cursor_line_index < cursor_max_index) {
                        Paragraph current_line = lines.get(cursor_line_index);
                        if (current_line.isBlank()) {
                            while (current_line.isBlank()) {
                                if (cursor_line_index == cursor_max_index) break;
                                cursor_line_index++;
                                current_line = lines.get(cursor_line_index);
                            }
                        }
                        while (!current_line.isBlank()) {
                            if (cursor_line_index == cursor_max_index) break;
                            cursor_line_index++;
                            current_line = lines.get(cursor_line_index);
                        }
                    }
                }
            }

        } else if ((mods & GLFW_MOD_SHIFT) > 0) {
            if (key == GLFW_KEY_DOWN) {
                lines.add(cursor_line_index,new Paragraph());
                cursor_line_index++;
            } else if (key == GLFW_KEY_UP) {
                if (cursor_line_index > 0) {
                    int index = cursor_line_index;
                    while (index > 0) {
                        index--;
                        if (lines.get(index).isBlank()) {
                            lines.remove(index);
                            cursor_line_index--;
                            break;
                        }
                    }
                }
            }
        }

        else {

            if (key == GLFW_KEY_ESCAPE) {
                /*       */
                return;
            }

            if (isBlank()) {
                cursor_char_index = 0;
                if (key == GLFW_KEY_ENTER) {
                    lines.add(new Paragraph());
                }

            } else {

                int num_lines = lines.size();

                if (key == GLFW_KEY_ENTER) {
                    Paragraph current_line = lines.get(cursor_line_index);
                    int line_length = current_line.length();
                    if (cursor_char_index == 0){
                        lines.add(cursor_line_index,new Paragraph());
                        cursor_line_index++;
                    } else if (cursor_char_index == line_length) {
                        cursor_line_index++;
                        cursor_char_index = 0;
                        lines.add(cursor_line_index,new Paragraph());
                    } else {
                        Paragraph[] split = current_line.split(cursor_char_index);
                        lines.remove(cursor_line_index);
                        lines.add(cursor_line_index,split[1]);
                        lines.add(cursor_line_index,split[0]);
                        cursor_line_index++;
                        cursor_char_index = 0;
                    } desired_char_index = cursor_char_index;
                } else if (key == GLFW_KEY_BACKSPACE) {
                    Paragraph current_line = lines.get(cursor_line_index);
                    if (cursor_char_index > 0) {
                        current_line.delete(cursor_char_index - 1);
                        cursor_char_index--;
                    } else {
                        if (cursor_line_index > 0) {
                            if (current_line.isBlank()) {
                                lines.remove(cursor_line_index);
                                cursor_line_index--;
                                current_line = lines.get(cursor_line_index);
                                cursor_char_index = current_line.length();
                            } else {
                                Paragraph removed = lines.remove(cursor_line_index);
                                cursor_line_index--;
                                current_line = lines.get(cursor_line_index);
                                cursor_char_index = current_line.length();
                                if (cursor_char_index > 0) cursor_char_index++;
                                current_line.append(removed);
                            }
                        }
                    } desired_char_index = cursor_char_index;
                }
                else if (key == GLFW_KEY_UP) {
                    if (cursor_line_index > 0) {
                        cursor_line_index--;
                        Paragraph current_line = lines.get(cursor_line_index);
                        int line_length = current_line.length();
                        cursor_char_index = Math.min(desired_char_index, line_length);
                    }
                } else if (key == GLFW_KEY_DOWN) {
                    if (cursor_line_index < (num_lines - 1)) {
                        cursor_line_index++;
                        Paragraph current_line = lines.get(cursor_line_index);
                        int line_length = current_line.length();
                        cursor_char_index = Math.min(desired_char_index, line_length);
                    }
                } else if (key == GLFW_KEY_RIGHT) {
                    Paragraph current_line = lines.get(cursor_line_index);
                    int line_length = current_line.length();
                    if (cursor_char_index < line_length) {
                        cursor_char_index++;
                    } else if (cursor_char_index == line_length) {
                        if (cursor_line_index < (num_lines - 1)) {
                            cursor_line_index++;
                            cursor_char_index = 0;
                        }
                    } desired_char_index = cursor_char_index;
                } else if (key == GLFW_KEY_LEFT) {
                    if (cursor_char_index > 0) {
                        cursor_char_index--;
                    } else {
                        if (cursor_line_index > 0) {
                            cursor_line_index--;
                            Paragraph current_line = lines.get(cursor_line_index);
                            cursor_char_index = current_line.length();
                        }
                    } desired_char_index = cursor_char_index;
                } else if (key == GLFW_KEY_DELETE) {
                    if (cursor_line_index == 0) {
                        Paragraph current_line = lines.getFirst();
                        if (num_lines == 1) {
                            if (!current_line.isBlank()) {
                                current_line.clear();
                            }
                        }
                        else {
                            lines.removeFirst();
                        }
                    } else {
                        if (cursor_line_index == (num_lines - 1)) {
                            Paragraph current_line = lines.get(cursor_line_index);
                            if (!current_line.isBlank()) {
                                current_line.clear();
                            }
                        } else {
                            lines.remove(cursor_line_index);
                        }
                    }
                    cursor_char_index = 0;
                    desired_char_index = 0;
                }
            }
        }
    }

    public void keyRelease(int key, int mods) { }

    public void charPress(byte character) {
        if (character != 10) {
            if (isBlank()) {
                add(new Paragraph(Character.toString((char)character)));
                cursor_char_index++;
                desired_char_index = cursor_char_index;
            }
            else {
                Paragraph current_line = lines.get(cursor_line_index);
                if (current_line.insert(character,cursor_char_index)) {
                    cursor_char_index++;
                    desired_char_index = cursor_char_index;
                }
            }
        }
    }

    public void add(Paragraph paragraph) { addLast(paragraph); }

    public void addFirst(Paragraph paragraph) {
        lines.addFirst(paragraph);
        resetCursorPosition();
    }

    public void addLast(Paragraph paragraph) {
        lines.addLast(paragraph);
        resetCursorPosition();
    }

    public void add(Paragraph paragraph, int index) {
        lines.add(index,paragraph);
        resetCursorPosition();
    }

    public boolean remove(Paragraph paragraph) {
        resetCursorPosition();
        return lines.remove(paragraph);
    }

    public Paragraph remove(int index) {
        resetCursorPosition();
        return lines.remove(index);
    }

    public Paragraph removeFirst() {
        resetCursorPosition();
        return lines.removeFirst();
    }

    public Paragraph removeLast() {
        resetCursorPosition();
        return lines.removeLast();
    }

    public Iterator<Paragraph> iterator() { return lines.iterator(); }

    public Text copy() {
        Text text = new Text();
        for (Paragraph line : lines) {
            text.addLast(line.copy());
        } return text;
    }

    public String toString() {
        if (isBlank()) return "";
        StringBuilder stringBuilder = new StringBuilder(256);
        for (Paragraph line : lines) {
            line.toString(stringBuilder);
            stringBuilder.append("\n");
        } int length = stringBuilder.length();
        stringBuilder.deleteCharAt(length - 1);
        return stringBuilder.toString();
    }

    /** Desired width of text. Set correct font before calling this */
    public float width() {
        if (isBlank()) return 0;
        float width = 0;
        for (Paragraph line : lines) {
            width = Math.max(width,line.width());
        } return width;
    }

    /** Cursors position relative to the top right corner of Text */
    public Vector2f cursorPosition(Vector2f dst, float width, float size, boolean wrap) {
        if (isBlank() || size < 1f || width < 1f ) dst.zero();
        else { FontsGUI fonts = GUI.fonts;
            float scale = fonts.relativeScale(size);
            float space = fonts.advance(' ') * scale;
            float x = 0; float y = 0;
            if (wrap) {
                int line_index = 0;
                out:
                for (Paragraph line : lines) { x = 0;
                    if (line_index == cursor_line_index) {
                        int char_index = 0;
                        if (char_index == cursor_char_index) break;
                        for (Word word : line) {
                            float word_width = word.width() * scale;
                            if ((x + word_width) > width && x > 0) {
                                y -= size; x = 0;
                            } if (char_index == cursor_char_index) break out;
                            byte[] bytes = word.get();
                            for (byte c : bytes) {
                                if (char_index == cursor_char_index) break out;
                                x += fonts.advance((char) c) * scale;
                                char_index++;
                            } if (char_index == cursor_char_index) break out;
                            x += space;
                            char_index++;
                        }
                    } else {
                        for (Word word : line) {
                            float word_width = word.width() * scale;
                            if ((x + word_width) > width && x > 0) {
                                y -= size; x = 0; }
                            byte[] bytes = word.get();
                            for (byte c : bytes) {
                                x += fonts.advance((char) c) * scale;
                            } x += space;
                        }
                    } line_index++;
                    y -= size;
                }
            } else {
                y = - (size * cursor_line_index);
                Paragraph line = lines.get(cursor_line_index);
                int char_index = 0;
                out:
                for (Word word : line) {
                    byte[] bytes = word.get();
                    for (byte c : bytes) {
                        if (char_index == cursor_char_index) break out;
                        x += fonts.advance((char) c) * scale;
                        char_index++;
                    } if (char_index == cursor_char_index) break;
                    x += space;
                    char_index++;
                }
            } dst.set(x,y);
        } return dst;
    }

    public float height(float width, float size, boolean wrap) {
        if (isBlank() || size < 1f || width < 1f ) return 0;
        FontsGUI fonts = GUI.fonts;
        float height = 0f;
        float scale = fonts.relativeScale(size);
        if (wrap) { float space = fonts.advance(' ') * scale;
            for (Paragraph line : lines) {
                float x = 0f;
                for (Word word : line) {
                    float word_width = word.width() * scale;
                    if ((x + word_width) > width && x > 0) { x = 0;
                        height += size;
                    } x += (word_width + space);
                } height += size; }
        } else for (Paragraph line : lines) {
            height += size;
        } return height - (fonts.lineGap() * scale);
    }

    public float length() {
        if (isBlank()) return 0;
        float length = 0;
        for (Paragraph line : lines) {
            length += line.length();
        } return length;
    }

    public void set(String text) {
        resetCursorPosition();
        lines.clear();
        stringToText(text,lines);
    }

    public void clear() {
        if (!isBlank()) {
            resetCursorPosition();
            lines.clear();
        }
    }

    private void resetCursorPosition() {
        cursor_line_index = 0;
        cursor_char_index = 0;
        desired_char_index = 0;
    }

    public int mumLines() { return lines.size(); }

    public int cursorCharIndex() { return cursor_char_index; }

    public int cursorLineIndex() { return cursor_line_index; }

    public boolean isBlank() { return lines.isEmpty(); }

    private void stringToText(String string, LinkedList<Paragraph> dst) {
        if (string == null || string.isBlank()) return;
        List<String> lines = string.trim().lines().collect(Collectors.toList());
        for (String line : lines) dst.add(new Paragraph(line));
    }



}
