package io.github.heathensoft.jlib.lwjgl.gfx.font;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.Vao;
import io.github.heathensoft.jlib.lwjgl.gfx.Font;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Vector2f;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;

/**
 * @author Frederik Dahl
 * 11/10/2023
 */


public abstract class TextRenderer implements Disposable {

    private static final String DEFAULT_FONT = "Balla";
    private static final String U_FONT_TEXTURES = "u_font_textures";
    private static final String U_RESOLUTION = "u_resolution";

    private boolean render_state;

    private int draw_calls;
    private int draw_calls_max;
    private int draw_calls_total;
    private int char_count;
    private int buffer_capacity;

    private ShaderProgram shaderProgram;
    private BufferObject vertexBuffer;
    private FloatBuffer vertices;

    private Vao vertexAttribArray;

    public TextRenderer() {

    }

    public void startRendering(int res_width, int res_height) {
        if (render_state) throw new IllegalStateException();
        draw_calls_max = Math.max(draw_calls,draw_calls_max);
        draw_calls = 0;
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        shaderProgram.use();
        Vector2f resolution = MathLib.vec2(res_width,res_height);
        shaderProgram.setUniform(U_RESOLUTION,resolution);
        render_state = true;
    }

    public void endRendering() {
        if (!render_state) throw new IllegalStateException();
        flush();
        glDisable(GL_BLEND);
        render_state = false;
    }

    public abstract void drawString(String string, float x, float y, int size, int color, float highlight);

    public abstract void addFont(ByteBuffer png, ByteBuffer info, int slot);

    public abstract void bindFont(int font);

    public int drawCalls() { return draw_calls; }

    public int drawCallsTotal() { return draw_calls_total; }



    protected void loadDefaultFont() throws Exception {
        String repo_name = DEFAULT_FONT + ".repo";
        String repo_location = "res/jlib/lwjgl/font/";
        Logger.debug("TextRenderer: loading default font: " + repo_name);
        Repository font_repo = Repository.loadFromResources(repo_location + repo_name);
        Font font = font_repo.getFont(DEFAULT_FONT);
    }

    protected ShaderProgram loadShader() throws Exception {
        Logger.debug("TextRenderer: loading shaders");
        String vShader = Resources.asString("res/jlib/lwjgl/glsl/ui_text.vert");
        String gShader = Resources.asString("res/jlib/lwjgl/glsl/ui_text.geom");
        String fShader = Resources.asString("res/jlib/lwjgl/glsl/ui_text.frag");
        ShaderProgram shaderProgram = new ShaderProgram(vShader,gShader,fShader).use();
        shaderProgram.createUniform(U_RESOLUTION);
        shaderProgram.createUniform(U_FONT_TEXTURES);
        return shaderProgram;
    }

    protected void push(float x, float y, int i) {
        if (char_count == buffer_capacity) flush();
        float f = Float.intBitsToFloat(i);
        vertices.put(x).put(y).put(f);
        char_count++;
    }

    protected void flush() {
        if (char_count > 0) {
            vertices.flip();
            vertexAttribArray.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices, 0);
            glDrawArrays(GL_POINTS, 0, char_count);
            vertices.clear();
            char_count = 0;
            draw_calls++;
            draw_calls_total++;
        }
    }

}
