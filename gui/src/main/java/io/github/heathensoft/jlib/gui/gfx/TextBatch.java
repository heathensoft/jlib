package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.text.TextAlignment;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * // 00000000 00000000 00000000 01111111 character     // Offset: 0
 * // 00000000 00000000 00000000 10000000 negate_color  // Offset: 7
 * // 00000000 00000000 11111111 00000000 font_size     // Offset: 8
 * // 00000000 11111111 00000000 00000000 glow          // Offset: 16
 * // 00000001 00000000 00000000 00000000 marked        // Offset: 24
 * // 00001110 00000000 00000000 00000000 font (5 max)  // Offset: 25
 *
 * @author Frederik Dahl
 * 30/05/2024
 */


public class TextBatch extends Batch {


    TextBatch(int capacity) {
        int vertex_size = 4;
        int vertex_size_bytes = vertex_size * Float.BYTES;
        buffer_capacity = capacity;
        vertexAttribArray = new VertexAttributes().bind();
        vertices = MemoryUtil.memAllocFloat(capacity * vertex_size);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        vertexBuffer.bind().bufferData((long) vertex_size_bytes * capacity);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertex_size_bytes, 0);
        glVertexAttribPointer(1,4,GL_UNSIGNED_BYTE,true,vertex_size_bytes,3 * Float.BYTES);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }

    // TODO: Renderer should have: renderer.drawCharacter
    void push(float x, float y, float color, int bits) {
        if (count == buffer_capacity) flush();
        vertices.put(x).put(y).put(Float.intBitsToFloat(bits)).put(color);
        count++;
    }

    void drawFixedSize(String string, TextAlignment alignment, int abgr, float x, float y, float width, float size, float glow) {
        int num_characters = string.length();
        if (num_characters > 0 && size >= 1) {
            Fonts fonts = GUI.fonts;
            float color = Color.intBits_to_floatBits(abgr);
            float scale = fonts.relativeScale(size);
            float width_unscaled = fonts.advanceSum(string);
            float desired_width = width_unscaled * scale;

            int bits = Fonts.bitsSetFont(0,fonts.currentFont());
            bits = Fonts.bitsSetFontSize(bits,size);
            bits = Fonts.bitsSetGlow(bits,glow);

            y -= fonts.ascent() * scale;
            if (desired_width > width) {
                // Will be out of bounds. Must check each character
                switch (alignment) {
                    case LEFT, CENTERED -> { float pointer_x = x;
                        float bounds = (x + width) + 0.5f;
                        for (int i = 0; i < num_characters; i++) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            float next_x = pointer_x + fonts.advanceUnchecked(c) * scale;
                            if (next_x > bounds) return;
                            if (c != 32) push(pointer_x,y,color,bits | c);
                            pointer_x = next_x;
                        }
                    }
                    case RIGHT -> { float pointer_x = x + width;
                        float bounds = x - 0.5f;
                        for (int i = (num_characters - 1); i >= 0; i--) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            float next_x = pointer_x - fonts.advanceUnchecked(c) * scale;
                            if (next_x < bounds) return;
                            if (c != 32) push(next_x,y,color,bits | c);
                            pointer_x = next_x;
                        }
                    }
                }
            } else switch (alignment) {
                // ATP The line fits inside the rect. No need to check bounds
                case LEFT -> {
                    for (int i = 0; i < num_characters; i++) {
                        byte c = (byte) (string.charAt(i) & 0x7F);
                        if (c != 32) push(x,y,color,bits | c);
                        x += fonts.advanceUnchecked(c) * scale;
                    }
                }
                case RIGHT -> { x += width;
                    for (int i = (num_characters - 1); i >= 0; i--) {
                        byte c = (byte) (string.charAt(i) & 0x7F);
                        x -= fonts.advanceUnchecked(c) * scale;
                        if (c != 32) push(x,y,color,bits | c);
                    }
                }
                case CENTERED -> { x += center_offset_x(desired_width,width);
                    for (int i = 0; i < num_characters; i++) {
                        byte c = (byte) (string.charAt(i) & 0x7F);
                        if (c != 32) push(x,y,color,bits | c);
                        x += fonts.advanceUnchecked(c) * scale;
                    }
                }
            }
        }
    }


    /** Draw the string dynamic sized and vertically centered */
    void drawDynamicVerticalCentered(String string, TextAlignment alignment, int abgr, float x, float y, float width, float size, float glow) {
        int num_characters = string.length();
        if (num_characters > 0 && size >= 1f) {
            Fonts fonts = GUI.fonts;
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
                int bits = Fonts.bitsSetFont(0,fonts.currentFont());
                bits = Fonts.bitsSetFontSize(bits,size);
                bits = Fonts.bitsSetGlow(bits,glow);
                for (int i = 0; i < num_characters; i++) {
                    byte c = (byte) (string.charAt(i) & 0x7F);
                    if (c != 32) push(x,y,color,bits | c);
                    x += fonts.advanceUnchecked(c) * scale;
                }
            } else { // ATP The line fits inside the rect without scale adjustments.
                float ascent = scale * fonts.ascent();
                float descent = scale * fonts.descent();
                float letter_height = ascent + descent;
                y = y - (letter_height / 2f) + descent;
                int bits = Fonts.bitsSetFont(0,fonts.currentFont());
                bits = Fonts.bitsSetFontSize(bits,size);
                bits = Fonts.bitsSetGlow(bits,glow);
                switch (alignment) {
                    case LEFT -> {
                        for (int i = 0; i < num_characters; i++) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            if (c != 32) push(x,y,color,bits | c);
                            x += fonts.advanceUnchecked(c) * scale;
                        }
                    } case RIGHT -> { x += width;
                        for (int i = (num_characters - 1); i >= 0; i--) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            x -= fonts.advanceUnchecked(c) * scale;
                            if (c != 32) push(x,y,color,bits | c);
                        }
                    } case CENTERED -> { x += center_offset_x(desired_width,width);
                        for (int i = 0; i < num_characters; i++) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            if (c != 32) push(x,y,color,bits | c);
                            x += fonts.advanceUnchecked(c) * scale;
                        }
                    }
                }
            }
        }
    }

    void drawDynamicSize(String string, TextAlignment alignment, int abgr, float x, float y, float width, float size, float glow) {
        int num_characters = string.length();
        if (num_characters > 0 && size >= 1f) {
            Fonts fonts = GUI.fonts;
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
                int bits = Fonts.bitsSetFont(0,fonts.currentFont());
                bits = Fonts.bitsSetFontSize(bits,size);
                bits = Fonts.bitsSetGlow(bits,glow);

                for (int i = 0; i < num_characters; i++) {
                    byte c = (byte) (string.charAt(i) & 0x7F);
                    if (c != 32) push(x,y,color,bits | c);
                    x += fonts.advanceUnchecked(c) * scale;
                }
            } else { // ATP The line fits inside the rect without scale adjustments.
                int bits = Fonts.bitsSetFont(0,fonts.currentFont());
                bits = Fonts.bitsSetFontSize(bits,size);
                bits = Fonts.bitsSetGlow(bits,glow);
                switch (alignment) {
                    case LEFT -> {
                        for (int i = 0; i < num_characters; i++) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            if (c != 32) push(x,y,color,bits | c);
                            x += fonts.advanceUnchecked(c) * scale;
                        }
                    } case RIGHT -> { x += width;
                        for (int i = (num_characters - 1); i >= 0; i--) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            x -= fonts.advanceUnchecked(c) * scale;
                            if (c != 32) push(x,y,color,bits | c);
                        }
                    } case CENTERED -> { x += center_offset_x(desired_width,width);
                        for (int i = 0; i < num_characters; i++) {
                            byte c = (byte) (string.charAt(i) & 0x7F);
                            if (c != 32) push(x,y,color,bits | c);
                            x += fonts.advanceUnchecked(c) * scale;
                        }
                    }
                }
            }
        }
    }

    void flush() {
        if (count > 0) {
            ShaderProgram.bindProgram(shaderProgram());
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

    int shaderProgram() {
        return GUI.shaders.text_program;
    }

    private float center_offset_x(float desired_width, float bounds_width) {
        return (bounds_width - desired_width) / 2f;
    }
}
