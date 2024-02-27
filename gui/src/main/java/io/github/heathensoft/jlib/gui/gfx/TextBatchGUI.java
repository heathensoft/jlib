package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.gui.text.TextUtils;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.Vao;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.lwjgl.system.MemoryUtil;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static io.github.heathensoft.jlib.gui.text.TextUtils.width_pixels;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 20/10/2023
 */


public class TextBatchGUI extends BatchGUI {

    public static final String SHADER_VERT = "res/jlib/gui/glsl/ui_text.vert";
    public static final String SHADER_GEOM = "res/jlib/gui/glsl/ui_text.geom";
    public static final String SHADER_FRAG = "res/jlib/gui/glsl/ui_text.frag";
    private final FontsGUI fonts;

    TextBatchGUI(FontsGUI fonts, int capacity, int width, int height) throws Exception {
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
        String vShader = Resources.asString(SHADER_VERT);
        String gShader = Resources.asString(SHADER_GEOM);
        String fShader = Resources.asString(SHADER_FRAG);
        shaderProgram = new ShaderProgram(vShader,gShader,fShader);
        shaderProgram.createUniform("u_resolution");
        shaderProgram.createUniform("u_font_textures");
        setResolutionUniform(width, height);
    }

    public FontsGUI fonts() {
        return fonts;
    }

    public void drawDynamicSize(String string, float x, float y, float width, float size, int abgr, float glow, boolean centered) {
        float color = Color.intBits_to_floatBits(abgr);
        float scale = fonts.relativeScale(size);
        float width_pixels = width_pixels(string,fonts);
        float desired_width = width_pixels * scale;
        float ratio = width / desired_width;
        y -= fonts.ascent() * scale;
        if (ratio < 1) { size = size * ratio;
            scale = fonts.relativeScale(size);
            desired_width = width_pixels * scale;
        } if (centered) x += TextUtils.center_offset_x(fonts,desired_width,width);
        if (size >= 1f) { int info_bits = (fonts.currentFont() << 29);
            info_bits |= (((round(size) - 1) & 0xFF) << 21);
            info_bits |= ((round(clamp(glow) * 127.0f) & 0x7F) << 13);
            for (int i = 0; i < string.length(); i++) {
                char c = (char)(string.charAt(i) & 0x7F);
                pushVertex(x,y,color,info_bits | c);
                x += fonts.advance(c) * scale;
            }
        }
    }

    public void drawFixedSize(String string, float x, float y, float width, float size, int abgr, float glow) {
        float color = Color.intBits_to_floatBits(abgr);
        float scale = fonts.relativeScale(size);
        float width_pixels = width_pixels(string,fonts);
        float desired_width = width_pixels * scale;
        float bounds_x = (x + width) + 0.5f;
        int info_bits = (fonts.currentFont() << 29);
        info_bits |= (((round(size) - 1) & 0xFF) << 21);
        info_bits |= ((round(clamp(glow) * 127.0f) & 0x7F) << 13);
        y -= fonts.ascent() * scale;
        for (int i = 0; i < string.length(); i++) {
            char c = (char)(string.charAt(i) & 0x7F);
            float next_x = x + fonts.advance(c) * scale;
            if (next_x > (bounds_x)) return;
            pushVertex(x,y,color,info_bits | c);
            x = next_x;
        }
    }

    public void pushVertex(float x, float y, float color, int char_info) {
        if (count == buffer_capacity) flush();
        vertices.put(x).put(y).put(Float.intBitsToFloat(char_info)).put(color);
        count++;
    }

    void flush() {
        if (count > 0) {
            shaderProgram.use();
            fonts.bindUploadTextures(shaderProgram,"u_font_textures");
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

}
