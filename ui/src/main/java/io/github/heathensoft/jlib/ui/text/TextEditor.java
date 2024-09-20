package io.github.heathensoft.jlib.ui.text;

import io.github.heathensoft.jlib.common.text.Ascii;
import io.github.heathensoft.jlib.common.text.code.CodeColor;
import io.github.heathensoft.jlib.common.text.code.CodeHighlightParser;
import io.github.heathensoft.jlib.common.text.code.CodeHighlights;
import io.github.heathensoft.jlib.common.text.code.Language;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.lwjgl.window.Window;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.TextBatchGUI;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 08/05/2024
 */


public class TextEditor implements TextProcessor {

    private static final int indent = 4;
    private final Ascii.Editor editor;
    private CodeHighlightParser parser;
    private CodeHighlights highlights;
    private Vector2f cursor_previous;
    private Vector2f cursor_position;
    private Vector2f desired_offset;
    private Vector2f current_offset;
    private Vector2f desired_size;
    private float cursor_lerp_timer;

    public TextEditor() { this(null); }
    public TextEditor(String str) {
        editor = new Ascii.Editor(str,0);
        parser = new CodeHighlightParser(Language.JAVA);
        highlights = parser.parseCode(editor.bufferView());
        editor.setTokenize(false);
        cursor_position = new Vector2f();
        desired_size = new Vector2f();
    }

    private void refresh(Rectanglef bounds, float font_size) {

        if (editor.isEmpty()) {
            GUI.out.write("Empty Editor");
            cursor_position.zero();
            desired_size.zero();
        } else {
            FontsGUI fonts = GUI.fonts;
            Ascii.Buffer buffer = editor.buffer();
            final int TAB = 9;
            final int SPACE = 32;
            final int LINE_FEED = 10;
            float x = 0;
            float y = 0;
            float w = 0;
            int x_index = 0;
            int count = buffer.length();
            int cursor_index = editor.cursor();
            float scale = fonts.relativeScale(font_size);
            float space = fonts.advance(' ') * scale;
            for (int i = 0; i < count; i++) {
                if (i == cursor_index) {
                    cursor_position.set(x,y);
                } byte c = buffer.get(i);
                if (c == LINE_FEED) {
                    y += font_size;
                    x = x_index = 0;
                } else if (c == SPACE) {
                    x += space;
                    x_index++;
                } else if (c == TAB) {
                    int spaces = indent - (x_index % indent);
                    x_index += spaces;
                    x += spaces * space;
                } else { x_index++;
                    x += (fonts.advance(c) * scale);
                    w = Math.max(w,x);
                }
            } if (cursor_index == count) {
                cursor_position.set(x,y);
            } desired_size.set(w,y);
        }

    }

    public void drawCode(TextBatchGUI batch, Rectanglef bounds, float y_offset, float size, float glow, boolean show_cursor) {

        //refresh(bounds,size);


        // (NOTE): It should be the rendered objects' responsibility to set the viewport. Not the renderer.

        if (editor.shouldTokenize()) {
            highlights = parser.parseCode(editor.bufferView());
            editor.setTokenize(false);
        }

        if (editor.isEmpty() && !show_cursor) return;
        highlights.rewind();

        FontsGUI fonts = batch.fonts();
        float width = bounds.lengthX();
        float scale = fonts.relativeScale(size);
        float space = fonts.advance(' ') * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_gap = fonts.lineGap() * scale;
        float line_height = ascent + descent + line_gap;

        //GUI.out.write("Space Advance: " + space);
        //GUI.out.write("Bounds: " + bounds);
        //GUI.out.write("Size: " + desired_size);
        //GUI.out.write("Cursor: " + cursor_position);
        //GUI.out.write("Cursor Index: " + editor.cursor());

        float y = bounds.maxY + y_offset - ascent;
        float x = bounds.minX;
        int bits = FontsGUI.bits_font(fonts.currentFont());
        bits = FontsGUI.bits_set_size(bits,size);
        int glow_bits = FontsGUI.bits_set_glow(bits,glow);
        int mark_start = editor.markStart();
        int mark_end = editor.markEnd();
        boolean marking = editor.isMarking() && mark_start != mark_end;
        int cursor = editor.cursor();
        Ascii.Buffer buffer = editor.buffer();
        float top = y + ascent;
        float bot = y - descent;
        int num_chars = buffer.length();

        final float[] colors_unmarked = new float[CodeColor.array.length];
        {
            for (int i = 0; i < colors_unmarked.length; i++) {
                Vector4f rgb = CodeColor.typeByOrdinal(i).color;
                colors_unmarked[i] = Color.rgb_to_floatBits(rgb);
            }
        }

        if (show_cursor) {

            final int TAB = 9;
            final int SPACE = 32;
            final int LINE_FEED = 10;

            int cursor_x_index = 0;

            if (marking) {

                final float[] colors_marked = new float[CodeColor.array.length];
                {
                    Vector4f tmp = U.popVec4();
                    for (int i = 0; i < colors_marked.length; i++) {
                        Vector4f rgb = CodeColor.typeByOrdinal(i).color;
                        Color.rgb_lower_proportional(tmp.set(rgb),0.25f).w *= 0.8;
                        colors_marked[i] = Color.rgb_to_floatBits(tmp);
                    } U.pushVec4();
                }

                CodeColor type = highlights.sequenceHighlight();
                int seq_len = highlights.sequenceLength();
                int seq_idx = 0;
                float color_marked = colors_marked[type.ordinal()];
                float color_unmarked = colors_unmarked[type.ordinal()];

                for (int index = 0; index < num_chars; index++) {
                    boolean marked = index >= mark_start && index < mark_end;
                    byte c = buffer.get(index);
                    if (top < bounds.minY) break;
                    if (seq_idx == seq_len) {
                        seq_idx = 0;
                        if (highlights.hasNext()) {
                            highlights.nextSequence();
                            type = highlights.sequenceHighlight();
                            seq_len = highlights.sequenceLength();
                            color_marked = colors_marked[type.ordinal()];
                            color_unmarked = colors_unmarked[type.ordinal()];
                        }
                    } seq_idx++;

                    if (c == LINE_FEED) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_marked,glow_bits);
                        } x = bounds.minX;
                        y -= line_height;
                        top = y + ascent;
                        bot = y - descent;
                        cursor_x_index = 0;
                    } else if (c == SPACE) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_marked,glow_bits);
                        } x += space;
                        cursor_x_index++;
                    } else if (c == TAB) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_marked,glow_bits);
                        } int spaces = indent - (cursor_x_index % indent);
                        cursor_x_index += spaces;
                        x += spaces * space;
                    } else {
                        float advance = fonts.advance(c) * scale;
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_unmarked,bits);
                            batch.pushVertex(x,y,color_marked,FontsGUI.bits_invert_color(glow_bits | c));
                        } else {
                            if (bot < bounds.maxY && x < bounds.maxX) {
                                if (marked) batch.pushVertex(x, y, color_marked, bits | c);
                                else batch.pushVertex(x, y, color_unmarked, glow_bits | c);
                            }
                        } x += advance;
                        cursor_x_index++;
                    }
                } if (cursor == num_chars) {
                    batch.pushVertex(x,y,color_marked,glow_bits);
                }


            } else {

                CodeColor type = highlights.sequenceHighlight();
                int seq_len = highlights.sequenceLength();
                int seq_idx = 0;

                float color = colors_unmarked[type.ordinal()];

                for (int index = 0; index < num_chars; index++) {
                    byte c = buffer.get(index);
                    if (top < bounds.minY) break;
                    if (seq_idx == seq_len) {
                        seq_idx = 0;
                        if (highlights.hasNext()) {
                            highlights.nextSequence();
                            type = highlights.sequenceHighlight();
                            seq_len = highlights.sequenceLength();
                            color = colors_unmarked[type.ordinal()];
                        }
                    } seq_idx++;
                    if (c == LINE_FEED) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,glow_bits);
                        } x = bounds.minX;
                        y -= line_height;
                        top = y + ascent;
                        bot = y - descent;
                        cursor_x_index = 0;
                    } else if (c == SPACE) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,glow_bits);
                        } x += space;
                        cursor_x_index++;
                    } else if (c == TAB) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,glow_bits);
                        } int spaces = indent - (cursor_x_index % indent);
                        cursor_x_index += spaces;
                        x += spaces * space;
                    } else {
                        float advance = fonts.advance(c) * scale;
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,bits);
                            batch.pushVertex(x,y,color,FontsGUI.bits_invert_color(glow_bits | c));
                        } else {
                            if (bot < bounds.maxY && x < bounds.maxX) {
                                batch.pushVertex(x, y, color, glow_bits | c);
                            }
                        } x += advance;
                        cursor_x_index++;
                    }
                } if (cursor == num_chars) {
                    batch.pushVertex(x,y,color,glow_bits);
                }
            }


        } else {


        }

    }


    public void draw(TextBatchGUI batch, Rectanglef bounds, Vector4f rgb, float y_offset, float size, float glow, boolean wrap, boolean show_cursor) {

        drawCode(batch,bounds,y_offset,size,glow,show_cursor);
        /*
        FontsGUI fonts = batch.fonts();
        float width = bounds.lengthX();
        float scale = fonts.relativeScale(size);
        float space = fonts.advance(' ') * scale;
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float line_gap = fonts.lineGap() * scale;
        float line_height = ascent + descent + line_gap;
        float y = bounds.maxY + y_offset - ascent;
        float x = bounds.minX;
        int bits = FontsGUI.bits_font(fonts.currentFont());
        bits = FontsGUI.bits_set_size(bits,size);
        int glow_bits = FontsGUI.bits_set_glow(bits,glow);
        int mark_start = editor.markStart();
        int mark_end = editor.markEnd();
        boolean marking = editor.isMarking() && mark_start != mark_end;
        int cursor = editor.cursor();
        Ascii.Buffer buffer = editor.buffer();
        float top = y + ascent;
        float bot = y - descent;
        int num_chars = editor.length();
        if (show_cursor) {
            final int TAB = 9;
            final int SPACE = 32;
            final int LINE_FEED = 10;
            final int CARRIAGE_RETURN = 13;
            int cursor_x_index = 0;
            if (marking) {
                // marked glows and raised by 0.1
                // unmarked lowered by 0.1
                // cursor does not glow

                Vector4f vec = U.popSetVec4(rgb);
                Color.rgb_lower_proportional(vec,0.25f);
                vec.w *= 0.80;
                float color_marked = Color.rgb_to_floatBits(vec);
                //Color.rgb_raise_proportional(vec.set(rgb),0.25f);
                float color_unmarked = Color.rgb_to_floatBits(rgb);
                U.pushVec4();
                for (int index = 0; index < num_chars; index++) {
                    boolean marked = index >= mark_start && index < mark_end;
                    byte c = buffer.get(index);
                    if (top < bounds.minY) break;
                    if (c == LINE_FEED) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_marked,glow_bits);
                        } x = bounds.minX;
                        y -= line_height;
                        top = y + ascent;
                        bot = y - descent;
                        cursor_x_index = 0;
                    } else if (c == SPACE) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_marked,glow_bits);
                        } x += space;
                        cursor_x_index++;
                    } else if (c == TAB) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_marked,glow_bits);
                        } int spaces = tab - (cursor_x_index % tab);
                        cursor_x_index += spaces;
                        x += spaces * space;
                    } else {
                        float advance = fonts.advance(c) * scale;
                        if (index == cursor) {
                            batch.pushVertex(x,y,color_unmarked,bits);
                            batch.pushVertex(x,y,color_marked,FontsGUI.bits_invert_color(glow_bits | c));
                        } else {
                            if (bot < bounds.maxY && x < bounds.maxX) {
                                if (marked) batch.pushVertex(x, y, color_marked, bits | c);
                                else batch.pushVertex(x, y, color_unmarked, glow_bits | c);
                            }
                        } x += advance;
                        cursor_x_index++;
                    }
                } if (cursor == num_chars) {
                    batch.pushVertex(x,y,color_marked,glow_bits);
                }



            } else {

                float color = Color.rgb_to_floatBits(rgb);
                for (int index = 0; index < num_chars; index++) {
                    byte c = buffer.get(index);
                    if (top < bounds.minY) break;
                    if (c == LINE_FEED) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,glow_bits);
                        } x = bounds.minX;
                        y -= line_height;
                        top = y + ascent;
                        bot = y - descent;
                        cursor_x_index = 0;
                    } else if (c == SPACE || c == CARRIAGE_RETURN) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,glow_bits);
                        } x += space;
                        cursor_x_index++;
                    } else if (c == TAB) {
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,glow_bits);
                        } int spaces = tab - (cursor_x_index % tab);
                        cursor_x_index += spaces;
                        x += spaces * space;
                    } else {
                        float advance = fonts.advance(c) * scale;
                        if (index == cursor) {
                            batch.pushVertex(x,y,color,bits);
                            batch.pushVertex(x,y,color,FontsGUI.bits_invert_color(glow_bits | c));
                        } else {
                            if (bot < bounds.maxY && x < bounds.maxX) {
                                batch.pushVertex(x, y, color, glow_bits | c);
                            }
                        } x += advance;
                        cursor_x_index++;
                    }
                } if (cursor == num_chars) {
                    batch.pushVertex(x,y,color,glow_bits);
                }
            }
        }

         */

    }

    public void keyPress(int key, int mods, boolean repeat) {
        boolean cntr =  (mods & GLFW_MOD_CONTROL) > 0;
        boolean shift = (mods & GLFW_MOD_SHIFT) > 0;
        boolean marking = editor.isMarking();
        if (key == GLFW_KEY_BACKSPACE) {
            editor.backspace();
        } else if (key == GLFW_KEY_ENTER) {
            editor.enterChar(Ascii.LINE_FEED);
        } else if (key == GLFW_KEY_TAB) {
            editor.tabulator();
        } else if (key == GLFW_KEY_INSERT) {
            boolean insert = editor.insertModeEnabled();
            editor.enableInsertMode(!insert);
        } else if (key == GLFW_KEY_UP) {
            if (marking &! shift) {
                editor.releaseMark();
            } else if (shift &! marking) {
                editor.setMark();
            } editor.moveCursorUp();
        } else if (key == GLFW_KEY_RIGHT) {
            if (marking &! shift) {
                editor.releaseMark();
            } else if (shift &! marking) {
                editor.setMark();
            } if (cntr) editor.moveCursorEOL();
            else editor.moveCursorRight();
        } else if (key == GLFW_KEY_DOWN) {
            if (marking &! shift) {
                editor.releaseMark();
            } else if (shift &! marking) {
                editor.setMark();
            } editor.moveCursorDown();
        } else if (key == GLFW_KEY_LEFT) {
            if (marking &! shift) {
                editor.releaseMark();
            } else if (shift &! marking) {
                editor.setMark();
            } if (cntr) editor.moveCursorSOL();
            else editor.moveCursorLeft();
        } else if (key == GLFW_KEY_C) {
            if (cntr &! repeat) {
                String str = editor.copy(false);
                Window window = Engine.get().window();
                window.setClipboard(str); }
        } else if (key == GLFW_KEY_X) {
            if (cntr &! repeat) {
                String str = editor.cut(false);
                Window window = Engine.get().window();
                window.setClipboard(str); }
        } else if (key == GLFW_KEY_V) {
            if (cntr &! repeat) {
                Window window = Engine.get().window();
                String str = window.getClipboard();
                editor.paste(str); }
        } else if (key == GLFW_KEY_Z) {
            if (cntr &! repeat) {
                if (shift) editor.redo();
                else editor.undo();
            }
        }
    }


    public void charPress(byte character) {
        editor.enterChar(character);
    }
}
