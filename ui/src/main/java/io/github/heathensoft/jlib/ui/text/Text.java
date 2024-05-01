package io.github.heathensoft.jlib.ui.text;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.TextBatchGUI;
import io.github.heathensoft.jlib.ui.gfx.TextColors;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;

/**
 * @author Frederik Dahl
 * 19/03/2024
 */


public class Text implements Iterable<Paragraph> {

    protected final LinkedList<Paragraph> lines;
    protected int cursor_desired_char_index;
    protected int cursor_char_index;
    protected int cursor_line_index;
    protected int line_capacity;
    protected boolean insert_bottom;


    public Text() { this((String) null); }

    public Text(boolean insert_bottom) {
        this((String) null, insert_bottom);
    }

    public Text(String string) { this(string,true); }

    public Text(int capacity, boolean insert_bottom) {
        this((String)null,capacity, insert_bottom);
    }

    public Text(String string, boolean insert_bottom) {
        this(string,Integer.MAX_VALUE, insert_bottom);
    }

    public Text(String string, int capacity, boolean insert_bottom) {
        this.line_capacity = Math.max(1,capacity);
        this.insert_bottom = insert_bottom;
        this.lines = new LinkedList<>();
        set(string);
    }

    public Text(List<Paragraph> list) {
        this(list,Integer.MAX_VALUE,true);
    }

    public Text(List<Paragraph> list, boolean insert_bottom) {
        this(list,Integer.MAX_VALUE, insert_bottom);
    }

    public Text(List<Paragraph> list, int capacity, boolean insert_bottom) {
        this.line_capacity = Math.max(1,capacity);
        this.insert_bottom = insert_bottom;
        this.lines = new LinkedList<>();
        set(list);
    }

    public void draw(TextBatchGUI batch, Rectanglef bounds, Vector4f rgb, float y_offset, float size, float glow, boolean wrap, boolean show_cursor) {
        FontsGUI fonts = batch.fonts();
        float width = bounds.lengthX();
        float scale = fonts.relativeScale(size);
        float space = fonts.advance(' ') * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_gap = fonts.lineGap() * scale;
        float line_height = ascent + descent;
        float y = bounds.maxY + y_offset, x;
        float color_float = Color.rgb_to_floatBits(rgb);
        float color_alpha;

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
                    {
                        if ((y - descent) < bounds.minY ) {
                            float hidden_height = bounds.minY - (y - descent);
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else if ((y + ascent > bounds.maxY)) {
                            float hidden_height = (y + ascent) - bounds.maxY;
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else color_alpha = 1.0f;
                    }
                    if (line.isBlank() && line_index == cursor_line_index) {
                        if ((y - descent) < bounds.maxY) {
                            batch.pushVertex(x,y,color_float,bits); }
                    } else for (Word word : line) {
                        float word_width = word.width() * scale;
                        if ((x + word_width) > bounds.maxX && x > bounds.minX) {
                            if (y < bounds.minY) return;
                            x = bounds.minX; y -= size;
                            {
                                if ((y - descent) < bounds.minY ) {
                                    float hidden_height = bounds.minY - (y - descent);
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else if ((y + ascent > bounds.maxY)) {
                                    float hidden_height = (y + ascent) - bounds.maxY;
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else color_alpha = 1.0f;
                            }
                        } int word_length = word.length();
                        if ((y - descent) < bounds.maxY) {
                            float color = color_alpha == 1.0f ? color_float : TextColors.floatBits(line,word,color_alpha);
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break;
                                char c = (char) (word.get(i) & 0x7F);
                                if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                    batch.pushVertex(x,y,color,bits);
                                    batch.pushVertex(x,y,color,FontsGUI.bits_invert_color(bits | c));
                                } else batch.pushVertex(x,y,color,bits | c);
                                x += (fonts.advanceUnchecked(c) * scale);
                                char_index++;
                            } if (line_index == cursor_line_index && char_index == cursor_char_index)
                                batch.pushVertex(x,y,color,bits);
                        } else for (int i = 0; i < word_length; i++) {
                            if (x >= bounds.maxX) break;
                            char c = (char) (word.get(i) & 0x7F);
                            x += (fonts.advanceUnchecked(c) * scale);
                            char_index++;
                        } char_index++;
                        x += space;
                    }
                } else {
                    {
                        if ((y - descent) < bounds.minY ) {
                            float hidden_height = bounds.minY - (y - descent);
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else if ((y + ascent > bounds.maxY)) {
                            float hidden_height = (y + ascent) - bounds.maxY;
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else color_alpha = 1.0f;
                    }
                    if (line.isBlank() && line_index == cursor_line_index) {
                        if ((y - descent) < bounds.maxY) {
                            batch.pushVertex(x,y,color_float,bits);
                        }
                    } else {
                        if ((y - descent) < bounds.maxY) {
                            next_line:
                            for (Word word : line) {
                                float color = color_alpha == 1.0f ? color_float : TextColors.floatBits(line,word,color_alpha);
                                int word_length = word.length();
                                for (int i = 0; i < word_length; i++) {
                                    if (x >= bounds.maxX) break next_line;
                                    char c = (char) (word.get(i) & 0x7F);
                                    if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                        batch.pushVertex(x,y,color,bits);
                                        batch.pushVertex(x,y,color,FontsGUI.bits_invert_color(bits | c));
                                    } else batch.pushVertex(x,y,color,bits | c);
                                    x += (fonts.advanceUnchecked(c) * scale);
                                    char_index++;
                                } if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                    batch.pushVertex(x,y,color,bits);
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
                    {
                        if ((y - descent) < bounds.minY ) {
                            float hidden_height = bounds.minY - (y - descent);
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else if ((y + ascent > bounds.maxY)) {
                            float hidden_height = (y + ascent) - bounds.maxY;
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else color_alpha = 1.0f;
                    }
                    for (Word word : line) {
                        int word_length = word.length();
                        float word_width = word.width() * scale;
                        if ((x + word_width) > bounds.maxX && x > bounds.minX) {
                            if (y < bounds.minY) return;
                            x = bounds.minX; y -= size;
                            {
                                if ((y - descent) < bounds.minY ) {
                                    float hidden_height = bounds.minY - (y - descent);
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else if ((y + ascent > bounds.maxY)) {
                                    float hidden_height = (y + ascent) - bounds.maxY;
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else color_alpha = 1.0f;
                            }
                        } if ((y - descent) < bounds.maxY) {
                            float color = color_alpha == 1.0f ? color_float : TextColors.floatBits(line,word,color_alpha);
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break;
                                char c = (char) (word.get(i) & 0x7F);
                                batch.pushVertex(x,y,color,bits | c );
                                x += (fonts.advanceUnchecked(c) * scale); }
                        } else for (int i = 0; i < word_length; i++) {
                            if (x >= bounds.maxX) break;
                            char c = (char) (word.get(i) & 0x7F);
                            x += (fonts.advanceUnchecked(c) * scale);
                        } x += space;
                    }
                } else {
                    if ((y - descent) < bounds.maxY) {
                        {
                            if ((y - descent) < bounds.minY ) {
                                float hidden_height = bounds.minY - (y - descent);
                                float lerp_factor = hidden_height / line_height;
                                color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                            } else if ((y + ascent > bounds.maxY)) {
                                float hidden_height = (y + ascent) - bounds.maxY;
                                float lerp_factor = hidden_height / line_height;
                                color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                            } else color_alpha = 1.0f;
                        }
                        next_line:
                        for (Word word : line) {
                            float color = color_alpha == 1.0f ? color_float : TextColors.floatBits(line,word,color_alpha);
                            int word_length = word.length();
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break next_line;
                                char c = (char) (word.get(i) & 0x7F);
                                batch.pushVertex(x,y,color,bits | c);
                                x += (fonts.advanceUnchecked(c) * scale);
                            } x += space;
                        }
                    }
                } y -= (descent + line_gap);
            }
        }
    }

    public void draw(TextBatchGUI batch, Rectanglef bounds, float y_offset, float size, float glow, boolean wrap, boolean show_cursor) {
        FontsGUI fonts = batch.fonts();
        float width = bounds.lengthX();
        float scale = fonts.relativeScale(size);
        float space = fonts.advance(' ') * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_gap = fonts.lineGap() * scale;
        float line_height = ascent + descent;
        float y = bounds.maxY + y_offset, x;
        float color_alpha;

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
                    {
                        if ((y - descent) < bounds.minY ) {
                            float hidden_height = bounds.minY - (y - descent);
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else if ((y + ascent > bounds.maxY)) {
                            float hidden_height = (y + ascent) - bounds.maxY;
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else color_alpha = 1.0f;
                    }
                    if (line.isBlank() && line_index == cursor_line_index) {
                        if ((y - descent) < bounds.maxY) {
                            float color = TextColors.floatBits(Paragraph.Type.DEFAULT);
                            batch.pushVertex(x,y,color,bits); }
                    } else for (Word word : line) {
                        float word_width = word.width() * scale;
                        if ((x + word_width) > bounds.maxX && x > bounds.minX) {
                            if (y < bounds.minY) return;
                            x = bounds.minX; y -= size;
                            {
                                if ((y - descent) < bounds.minY ) {
                                    float hidden_height = bounds.minY - (y - descent);
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else if ((y + ascent > bounds.maxY)) {
                                    float hidden_height = (y + ascent) - bounds.maxY;
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else color_alpha = 1.0f;
                            }
                        } int word_length = word.length();
                        if ((y - descent) < bounds.maxY) {
                            float color = TextColors.floatBits(line,word,color_alpha);
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break;
                                char c = (char) (word.get(i) & 0x7F);
                                if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                    batch.pushVertex(x,y,color,bits);
                                    batch.pushVertex(x,y,color,FontsGUI.bits_invert_color(bits | c));
                                } else batch.pushVertex(x,y,color,bits | c);
                                x += (fonts.advanceUnchecked(c) * scale);
                                char_index++;
                            } if (line_index == cursor_line_index && char_index == cursor_char_index)
                                batch.pushVertex(x,y,color,bits);
                        } else for (int i = 0; i < word_length; i++) {
                            if (x >= bounds.maxX) break;
                            char c = (char) (word.get(i) & 0x7F);
                            x += (fonts.advanceUnchecked(c) * scale);
                            char_index++;
                        } char_index++;
                        x += space;
                    }
                } else {
                    {
                        if ((y - descent) < bounds.minY ) {
                            float hidden_height = bounds.minY - (y - descent);
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else if ((y + ascent > bounds.maxY)) {
                            float hidden_height = (y + ascent) - bounds.maxY;
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else color_alpha = 1.0f;
                    }
                    if (line.isBlank() && line_index == cursor_line_index) {
                        if ((y - descent) < bounds.maxY) {
                            float color = TextColors.floatBits(Paragraph.Type.DEFAULT);
                            batch.pushVertex(x,y,color,bits);
                        }
                    } else {
                        if ((y - descent) < bounds.maxY) {
                            next_line:
                            for (Word word : line) {
                                float color = TextColors.floatBits(line,word,color_alpha);
                                int word_length = word.length();
                                for (int i = 0; i < word_length; i++) {
                                    if (x >= bounds.maxX) break next_line;
                                    char c = (char) (word.get(i) & 0x7F);
                                    if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                        batch.pushVertex(x,y,color,bits);
                                        batch.pushVertex(x,y,color,FontsGUI.bits_invert_color(bits | c));
                                    } else batch.pushVertex(x,y,color,bits | c);
                                    x += (fonts.advanceUnchecked(c) * scale);
                                    char_index++;
                                } if (line_index == cursor_line_index && char_index == cursor_char_index) {
                                    batch.pushVertex(x,y,color,bits);
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
                    {
                        if ((y - descent) < bounds.minY ) {
                            float hidden_height = bounds.minY - (y - descent);
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else if ((y + ascent > bounds.maxY)) {
                            float hidden_height = (y + ascent) - bounds.maxY;
                            float lerp_factor = hidden_height / line_height;
                            color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                        } else color_alpha = 1.0f;
                    }
                    for (Word word : line) {
                        int word_length = word.length();
                        float word_width = word.width() * scale;
                        if ((x + word_width) > bounds.maxX && x > bounds.minX) {
                            if (y < bounds.minY) return;
                            x = bounds.minX; y -= size;
                            {
                                if ((y - descent) < bounds.minY ) {
                                    float hidden_height = bounds.minY - (y - descent);
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else if ((y + ascent > bounds.maxY)) {
                                    float hidden_height = (y + ascent) - bounds.maxY;
                                    float lerp_factor = hidden_height / line_height;
                                    color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                                } else color_alpha = 1.0f;
                            }
                        } if ((y - descent) < bounds.maxY) {
                            float color = TextColors.floatBits(line,word,color_alpha);
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break;
                                char c = (char) (word.get(i) & 0x7F);
                                batch.pushVertex(x,y,color,bits | c );
                                x += (fonts.advanceUnchecked(c) * scale); }
                        } else for (int i = 0; i < word_length; i++) {
                            if (x >= bounds.maxX) break;
                            char c = (char) (word.get(i) & 0x7F);
                            x += (fonts.advanceUnchecked(c) * scale);
                        } x += space;
                    }
                } else {
                    if ((y - descent) < bounds.maxY) {
                        {
                            if ((y - descent) < bounds.minY ) {
                                float hidden_height = bounds.minY - (y - descent);
                                float lerp_factor = hidden_height / line_height;
                                color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                            } else if ((y + ascent > bounds.maxY)) {
                                float hidden_height = (y + ascent) - bounds.maxY;
                                float lerp_factor = hidden_height / line_height;
                                color_alpha = 1 - U.lerp(0,0.5f,lerp_factor);
                            } else color_alpha = 1.0f;
                        }
                        next_line:
                        for (Word word : line) {
                            float color = TextColors.floatBits(line,word,color_alpha);
                            int word_length = word.length();
                            for (int i = 0; i < word_length; i++) {
                                if (x >= bounds.maxX) break next_line;
                                char c = (char) (word.get(i) & 0x7F);
                                batch.pushVertex(x,y,color,bits | c);
                                x += (fonts.advanceUnchecked(c) * scale);
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
                            cursor_desired_char_index = 0;
                        }
                    } else  {
                        int line_length = current_line.length();
                        if (cursor_char_index == line_length) {
                            if ((lines.size() - 1) > cursor_line_index) {
                                cursor_line_index++;
                                cursor_char_index = 0;
                                cursor_desired_char_index = 0;
                            }
                        } else {
                            cursor_char_index = line_length;
                            cursor_desired_char_index = cursor_char_index;
                        }
                    }
                }
            } else if (key == GLFW_KEY_LEFT) {
                if (!isBlank()) {
                    if (cursor_char_index == 0) {
                        if (cursor_line_index > 0) {
                            cursor_line_index--;
                            cursor_char_index = lines.get(cursor_line_index).length();
                            cursor_desired_char_index = cursor_char_index;
                        }
                    } else {
                        cursor_char_index = 0;
                        cursor_desired_char_index = 0;
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
                if (lines.size() < line_capacity) {
                    lines.add(cursor_line_index,new Paragraph());
                    cursor_line_index++;
                }
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
                if (key == GLFW_KEY_ENTER) {
                    lines.add(new Paragraph());
                }

            } else {

                int num_lines = lines.size();

                if (key == GLFW_KEY_ENTER) {
                    if (num_lines < line_capacity) {
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
                        } cursor_desired_char_index = cursor_char_index;
                    }
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
                    } cursor_desired_char_index = cursor_char_index;
                }
                else if (key == GLFW_KEY_UP) {
                    if (cursor_line_index > 0) {
                        cursor_line_index--;
                        Paragraph current_line = lines.get(cursor_line_index);
                        int line_length = current_line.length();
                        cursor_char_index = Math.min(cursor_desired_char_index, line_length);
                    }
                } else if (key == GLFW_KEY_DOWN) {
                    if (cursor_line_index < (num_lines - 1)) {
                        cursor_line_index++;
                        Paragraph current_line = lines.get(cursor_line_index);
                        int line_length = current_line.length();
                        cursor_char_index = Math.min(cursor_desired_char_index, line_length);
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
                    } cursor_desired_char_index = cursor_char_index;
                } else if (key == GLFW_KEY_LEFT) {
                    if (cursor_char_index > 0) {
                        cursor_char_index--;
                    } else {
                        if (cursor_line_index > 0) {
                            cursor_line_index--;
                            Paragraph current_line = lines.get(cursor_line_index);
                            cursor_char_index = current_line.length();
                        }
                    } cursor_desired_char_index = cursor_char_index;
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
                    cursor_desired_char_index = 0;
                }
            }
        }
    }

    public void charPress(byte character) {
        if (character != 10) {
            if (isBlank()) {
                add(new Paragraph(Character.toString((char)character)));
                cursor_char_index++;
                cursor_desired_char_index = cursor_char_index;
            }
            else {
                Paragraph current_line = lines.get(cursor_line_index);
                if (current_line.insert(character,cursor_char_index)) {
                    cursor_char_index++;
                    cursor_desired_char_index = cursor_char_index;
                }
            }
        }
    }

    public void addMultiple(List<Paragraph> list) {
        if (!list.isEmpty()) {
            if (insert_bottom) {
                for (Paragraph paragraph : list) add(paragraph);
            } else { int num_lines = list.size();
                for (int i = num_lines - 1; i >= 0; i--) {
                    add(list.get(i));
                }
            }
        }
    }

    /** Added last or first depending on the order */
    public void add(Paragraph paragraph) {
        if (paragraph != null) {
            capLinesToAdd();
            if (insert_bottom) lines.addLast(paragraph);
            else lines.addFirst(paragraph);
            restrictCursor();
        }
    }

    public void insert(Paragraph paragraph, int index) {
        if (paragraph != null) {
            capLinesToAdd();
            index = Math.min(index,lines.size());
            lines.add(index,paragraph);
            restrictCursor();
        }
    }

    public boolean remove(Paragraph paragraph) {
        if (lines.remove(paragraph)) {
            restrictCursor();
            return true;
        } return false;
    }

    public Paragraph remove(int index) {
        Paragraph paragraph = lines.remove(index);
        restrictCursor();
        return paragraph;
    }

    public Paragraph removeFirst() {
        Paragraph paragraph = lines.removeFirst();
        restrictCursor();
        return paragraph;
    }

    public Paragraph removeLast() {
        Paragraph paragraph = lines.removeLast();
        restrictCursor();
        return paragraph;
    }

    public void set(String string) {
        lines.clear();
        stringToText(string,lines);
        capLinesAtMax();
        restrictCursor();
    }

    public void set(List<Paragraph> list) {
        lines.clear();
        lines.addAll(list);
        capLinesAtMax();
        restrictCursor();
    }

    public void clear() {
        lines.clear();
        restrictCursor();
    }

    public int mumLines() { return lines.size(); }

    public int cursorCharIndex() { return cursor_char_index; }

    public int cursorLineIndex() { return cursor_line_index; }

    public int capacity() { return line_capacity; }

    public boolean isBlank() { return lines.isEmpty(); }

    /** @return true if default order (insert at bottom, remove from top) */
    public boolean isOrderedDefault() { return insert_bottom; }

    /** @return Desired width of text. Set correct font before calling this */
    public float width() {
        if (isBlank()) return 0;
        float width = 0;
        for (Paragraph line : lines) {
            width = Math.max(width,line.width());
        } return width;
    }

    public float length() {
        if (isBlank()) return 0;
        float length = 0;
        for (Paragraph line : lines) {
            length += line.length();
        } return length;
    }

    /**
     * @param width width of the container
     * @param font_size size of the current font
     * @param wrap word wrapping enabled
     * @return the height of the text */
    public float height(float width, float font_size, boolean wrap) {
        if (isBlank() || font_size < 1f || width < 1f ) return 0;
        FontsGUI fonts = GUI.fonts;
        float height = 0f;
        float scale = fonts.relativeScale(font_size);
        if (wrap) { float space = fonts.advance(' ') * scale;
            for (Paragraph line : lines) {
                float x = 0f;
                for (Word word : line) {
                    float word_width = word.width() * scale;
                    if ((x + word_width) > width && x > 0) { x = 0;
                        height += font_size;
                    } x += (word_width + space);
                } height += font_size; }
        } else for (Paragraph line : lines) {
            height += font_size;
        } return height - (fonts.lineGap() * scale);
    }

    /**
     * Cursors position relative to the top right corner of Text
     * @param dst destination of position
     * @param width width of the container
     * @param font_size size of the current font */
    public void cursorPosition(Vector2f dst, float width, float font_size, boolean wrap) {
        if (isBlank() || font_size < 1f || width < 1f ) dst.zero();
        else { FontsGUI fonts = GUI.fonts;
            float scale = fonts.relativeScale(font_size);
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
                                y -= font_size; x = 0;
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
                                y -= font_size; x = 0; }
                            byte[] bytes = word.get();
                            for (byte c : bytes) {
                                x += fonts.advance((char) c) * scale;
                            } x += space;
                        }
                    } line_index++;
                    y -= font_size;
                }
            } else {
                y = - (font_size * cursor_line_index);
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
        }
    }

    public List<Paragraph> findLinesMatching(String string, boolean case_sensitive) {
        List<Paragraph> list = new LinkedList<>();
        if (!isBlank() && string != null) {
            for (Paragraph line : lines) {
                if (line.matching(string,case_sensitive)) {
                    if (!line.isBlank()) list.add(line);
                }
            }
        } return list;
    }

    public List<Paragraph> findLinesMatching(String keyword, Word.Type type, boolean case_sensitive) {
        List<Paragraph> list = new LinkedList<>();
        if (!isBlank() && keyword != null) {
            Set<String> set = new HashSet<>();
            for (Paragraph line : lines) {
                set.clear();
                line.findKeywords(set,type); {
                    if (!set.isEmpty()) {
                        for (String str : set) {
                            if (str.length() >= keyword.length()) {
                                String kwo = keyword;
                                if (!case_sensitive) {
                                    kwo = keyword.toUpperCase();
                                    str = str.toUpperCase();
                                } if (str.contains(kwo)) {
                                    list.add(line);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } return list;
    }

    public Set<String> findAllKeywords(Word.Type type) {
        Set<String> set = new HashSet<>();
        for (Paragraph line : lines) {
            line.findKeywords(set,type);
        } return set;
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

    public Iterator<Paragraph> iterator() { return lines.iterator(); }

    public Text copy() {
        Text text = new Text();
        for (Paragraph line : lines) {
            text.lines.add(line.copy());
        } text.insert_bottom = insert_bottom;
        text.line_capacity = line_capacity;
        text.cursor_line_index = cursor_line_index;
        text.cursor_char_index = cursor_char_index;
        text.cursor_desired_char_index = cursor_desired_char_index;
        return text;
    }

    public void setListOrder(boolean insert_bottom) {
        this.insert_bottom = insert_bottom;
    }

    public void setCapacity(int cap) {
        cap = Math.max(1,cap);
        if (line_capacity > cap) {
            line_capacity = cap;
            if (lines.size() > cap) {
                capLinesAtMax();
                restrictCursor();
            }
        } else line_capacity = cap;
    }

    protected void restrictCursor() {
        if (isBlank()) {
            cursor_line_index = 0;
            cursor_char_index = 0;
            cursor_desired_char_index = 0;
        } else {
            int line_max = lines.size() - 1; // not empty atp
            cursor_line_index = Math.min(cursor_line_index,line_max);
            Paragraph current_line = lines.get(cursor_line_index);
            int char_max = current_line.length();
            cursor_char_index = Math.min(cursor_char_index,char_max);
            cursor_desired_char_index = cursor_char_index;
        }
    }

    protected void capLinesToAdd() { // Remove lines until size < cap
        if (insert_bottom) {
            while (lines.size() >= line_capacity &! lines.isEmpty()) {
                lines.removeFirst();
            }
        } else {
            while (lines.size() >= line_capacity &! lines.isEmpty()) {
                lines.removeLast();
            }
        }
    }

    protected void capLinesAtMax() { // Remove lines until size <= cap
        if (insert_bottom) {
            while (lines.size() > line_capacity &! lines.isEmpty()) {
                lines.removeFirst();
            }
        } else {
            while (lines.size() > line_capacity &! lines.isEmpty()) {
                lines.removeLast();
            }
        }
    }

    protected void stringToText(String string, LinkedList<Paragraph> dst) {
        if (string == null || string.isBlank()) return;
        List<String> lines = string.trim().lines().collect(Collectors.toList());
        for (String line : lines) dst.add(new Paragraph(line));
    }
}
