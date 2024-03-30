package io.github.heathensoft.jlib.ui.gfx;


import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.text.TextAlignment;
import org.lwjgl.system.MemoryUtil;

import static io.github.heathensoft.jlib.ui.gfx.FontsGUI.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 *
 * @author Frederik Dahl
 * 20/10/2023
 */


public class TextBatchGUI extends BatchGUI {

    private final FontsGUI fonts;

    TextBatchGUI(FontsGUI fonts, int capacity, int width, int height) {
        int vertex_size = 4;
        int vertex_size_bytes = vertex_size * Float.BYTES;
        this.fonts = fonts;
        buffer_capacity = capacity;
        vertexAttribArray = new Vao().bind();
        vertices = MemoryUtil.memAllocFloat(capacity * vertex_size);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vertexBuffer.bind().bufferData((long) vertex_size_bytes * capacity);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertex_size_bytes, 0);
        glVertexAttribPointer(1,4,GL_UNSIGNED_BYTE,true,vertex_size_bytes,3 * Float.BYTES);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        updateResolution(width, height);
    }

    public FontsGUI fonts() {
        return fonts;
    }

    public void drawDynamicVerticalCentered(String string, TextAlignment alignment, int abgr, float x, float y, float width, float size, float glow) {
        int num_characters = string.length();
        if (num_characters > 0 && size >= 1f) {
            float color = Color.intBits_to_floatBits(abgr);
            float scale = fonts.relativeScale(size);
            float width_unscaled = fonts.advanceSum(string);
            float desired_width = width_unscaled * scale;
            y -= (size / 2f);
            if (desired_width >= width) { // Alignment does not matter here.
                if (desired_width > width) { // Adjusting scale to fit
                    float ratio = width / desired_width;
                    size = size * ratio;
                    if (size < 1f) return;
                    scale = fonts.relativeScale(size);
                }
                float ascent = scale * fonts.ascent();
                float descent = scale * fonts.descent();
                float letter_height = ascent + descent;
                y = y - (letter_height / 2f) + descent;
                int info_bits = bits_font(fonts.currentFont());
                info_bits = bits_set_size(info_bits,size);
                info_bits = bits_set_glow(info_bits,glow);
                for (int i = 0; i < num_characters; i++) {
                    char c = (char)(string.charAt(i) & 0x7F);
                    pushVertex(x,y,color,info_bits | c);
                    x += fonts.advance(c) * scale;
                }
            } else { // ATP The line fits inside the rect without scale adjustments.
                float ascent = scale * fonts.ascent();
                float descent = scale * fonts.descent();
                float letter_height = ascent + descent;
                y = y - (letter_height / 2f) + descent;
                int info_bits = bits_font(fonts.currentFont());
                info_bits = bits_set_size(info_bits,size);
                info_bits = bits_set_glow(info_bits,glow);
                switch (alignment) {
                    case LEFT -> {
                        for (int i = 0; i < num_characters; i++) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            pushVertex(x,y,color,info_bits | c);
                            x += fonts.advance(c) * scale;
                        }
                    } case RIGHT -> { x += width;
                        for (int i = (num_characters - 1); i >= 0; i--) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            x -= fonts.advance(c) * scale;
                            pushVertex(x,y,color,info_bits | c);
                        }
                    } case CENTERED -> { x += center_offset_x(desired_width,width);
                        for (int i = 0; i < num_characters; i++) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            pushVertex(x,y,color,info_bits | c);
                            x += fonts.advance(c) * scale;
                        }
                    }
                }
            }
        }
    }


    public void drawDynamicSize(String string, TextAlignment alignment, int abgr, float x, float y, float width, float size, float glow) {
        int num_characters = string.length();
        if (num_characters > 0 && size >= 1f) {
            float color = Color.intBits_to_floatBits(abgr);
            float scale = fonts.relativeScale(size);
            float width_unscaled = fonts.advanceSum(string);
            float desired_width = width_unscaled * scale;
            y -= fonts.ascent() * scale;
            if (desired_width >= width) { // Alignment does not matter here.
                if (desired_width > width) { // Adjusting scale to fit
                    float ratio = width / desired_width;
                    size = size * ratio;
                    if (size < 1f) return;
                    scale = fonts.relativeScale(size);
                }
                int info_bits = bits_font(fonts.currentFont());
                info_bits = bits_set_size(info_bits,size);
                info_bits = bits_set_glow(info_bits,glow);

                for (int i = 0; i < num_characters; i++) {
                    char c = (char)(string.charAt(i) & 0x7F);
                    pushVertex(x,y,color,info_bits | c);
                    x += fonts.advance(c) * scale;
                }
            } else { // ATP The line fits inside the rect without scale adjustments.

                int info_bits = bits_font(fonts.currentFont());
                info_bits = bits_set_size(info_bits,size);
                info_bits = bits_set_glow(info_bits,glow);
                switch (alignment) {
                    case LEFT -> {
                        for (int i = 0; i < num_characters; i++) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            pushVertex(x,y,color,info_bits | c);
                            x += fonts.advance(c) * scale;
                        }
                    } case RIGHT -> { x += width;
                        for (int i = (num_characters - 1); i >= 0; i--) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            x -= fonts.advance(c) * scale;
                            pushVertex(x,y,color,info_bits | c);
                        }
                    } case CENTERED -> { x += center_offset_x(desired_width,width);
                        for (int i = 0; i < num_characters; i++) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            pushVertex(x,y,color,info_bits | c);
                            x += fonts.advance(c) * scale;
                        }
                    }
                }
            }
        }
    }

    public void drawFixedSize(String string, TextAlignment alignment, int abgr, float x, float y, float width, float size, float glow) {
        int num_characters = string.length();
        if (num_characters > 0 && size >= 1) {
            float color = Color.intBits_to_floatBits(abgr);
            float scale = fonts.relativeScale(size);
            float width_unscaled = fonts.advanceSum(string);
            float desired_width = width_unscaled * scale;

            int info_bits = bits_font(fonts.currentFont());
            info_bits = bits_set_size(info_bits,size);
            info_bits = bits_set_glow(info_bits,glow);

            y -= fonts.ascent() * scale;
            if (desired_width > width) {
                // Will be out of bounds. Must check each character
                switch (alignment) {
                    case LEFT -> { float pointer_x = x;
                        float bounds = (x + width) + 0.5f;
                        for (int i = 0; i < num_characters; i++) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            float next_x = pointer_x + fonts.advance(c) * scale;
                            if (next_x > bounds) return;
                            pushVertex(pointer_x,y,color,info_bits | c);
                            pointer_x = next_x;
                        }
                    }
                    case RIGHT -> { float pointer_x = x + width;
                        float bounds = x - 0.5f;
                        for (int i = (num_characters - 1); i >= 0; i--) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            float next_x = pointer_x - fonts.advance(c) * scale;
                            if (next_x < bounds) return;
                            pushVertex(next_x,y,color,info_bits | c);
                            pointer_x = next_x;
                        }
                    }
                    case CENTERED -> { float pointer_x = x + center_offset_x(desired_width,width);
                        float bounds = x + width + 0.5f;
                        for (int i = 0; i < num_characters; i++) {
                            char c = (char)(string.charAt(i) & 0x7F);
                            float next_x = pointer_x + fonts.advance(c) * scale;
                            if (pointer_x >= x) {
                                if (next_x > bounds) return;
                                pushVertex(pointer_x,y,color,info_bits | c);
                            } pointer_x = next_x;
                        }
                    }
                }
            } else switch (alignment) {
                // ATP The line fits inside the rect. No need to check bounds
                case LEFT -> {
                    for (int i = 0; i < num_characters; i++) {
                        char c = (char)(string.charAt(i) & 0x7F);
                        pushVertex(x,y,color,info_bits | c);
                        x += fonts.advance(c) * scale;
                    }
                }
                case RIGHT -> { x += width;
                    for (int i = (num_characters - 1); i >= 0; i--) {
                        char c = (char)(string.charAt(i) & 0x7F);
                        x -= fonts.advance(c) * scale;
                        pushVertex(x,y,color,info_bits | c);
                    }
                }
                case CENTERED -> { x += center_offset_x(desired_width,width);
                    for (int i = 0; i < num_characters; i++) {
                        char c = (char)(string.charAt(i) & 0x7F);
                        pushVertex(x,y,color,info_bits | c);
                        x += fonts.advance(c) * scale;
                    }
                }
            }
        }
    }

    public void pushVertex(float x, float y, float color, int char_info) {
        if (count == buffer_capacity) flush();
        vertices.put(x).put(y).put(Float.intBitsToFloat(char_info)).put(color);
        count++;
    }

    void flush() {
        if (count > 0) {
            ShaderProgram.bindProgram(GUI.shaders.text_program);
            fonts.bindUploadTextures("u_font_textures");
            vertices.flip();
            vertexAttribArray.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices, 0);
            glDrawArrays(GL_POINTS, 0, count);
            vertices.clear();
            draw_calls++;
            count = 0;
        }
    }

    void updateResolution(int width, int height) {
        ShaderProgram.bindProgram(GUI.shaders.text_program);
        ShaderProgram.setUniform("u_resolution",(float) width,(float) height);
    }

    private float center_offset_x(float desired_width, float bounds_width) {
        return (bounds_width - desired_width) / 2f;
    }

}
