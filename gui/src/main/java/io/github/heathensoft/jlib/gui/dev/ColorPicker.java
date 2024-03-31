package io.github.heathensoft.jlib.gui.dev;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 27/10/2023
 */


public class ColorPicker implements Disposable {

    public static final int HSV_WINDOW_RES_WIDTH = 512;
    public static final int HSV_WINDOW_RES_HEIGHT = 512;
    public static final int HUE_SLIDER_RES_WIDTH = 512;
    public static final int HUE_SLIDER_RES_HEIGHT = 32;

    private static final String SHADER_VERT_PATH = "res/jlib/gui/glsl/color_picker.vert";
    private static final String SHADER_FRAG_PATH = "res/jlib/gui/glsl/color_picker.frag";
    private static final String U_DRAW_SLIDER = "u_draw_slider";
    private static final String U_HUE = "u_hue";

    private final ShaderProgramOld shader;
    private final Framebuffer hsv_window_buffer;
    private final Framebuffer hue_slider_buffer;
    private final BufferObject vertexBuffer;
    private final BufferObject indexBuffer;
    private final VertexAttributes vertexArrayObject;
    private final Vector4f hsv;
    private Vector4f rgb;
    private boolean dirty;


    public ColorPicker() throws Exception {
        indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
        float[] vertices = new float[] {1.0f,-1.0f,-1.0f,1.0f,1.0f,1.0f,-1.0f,-1.0f};
        short[] indices = {2, 1, 0, 0, 1, 3};
        vertexArrayObject = new VertexAttributes().bind();
        indexBuffer.bind().bufferData(indices);
        vertexBuffer.bind().bufferData(vertices);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        shader = create_shader_program();
        hue_slider_buffer = create_hue_slider_framebuffer();
        hsv_window_buffer = create_hsv_window_framebuffer();
        hsv = new Vector4f(180.0f,0.5f,0.5f,1.0f);
        rgb = Color.hsv_to_rgb(new Vector4f(hsv));
        render_hue_slider();
        render_hsv_window();
        dirty = false;

    }


    public void refreshHsvWindowTexture() {
        if (dirty) {
            render_hsv_window();
            dirty = false;
        }
    }

    public void pick(Vector2f pickerPos) {
        hsv.y = clamp(pickerPos.x);
        hsv.z = clamp(pickerPos.y);
        Color.hsv_to_rgb(rgb.set(hsv));
    }

    public void selectHue(float slider_position) {
        float hue = clamp(slider_position) * 360.0f;
        if (hue != hsv.x) {
            hsv.x = hue;
            dirty = true;
            Color.hsv_to_rgb(rgb.set(hsv));
        }
    }

    public void editColor(Vector4f rgb) {
        this.rgb = Color.clamp_rgb(rgb);
        Color.rgb_to_hsv(hsv.set(rgb));
        dirty = true;
    }

    public void setRGB(Vector4f rgb) {
        Color.clamp_rgb(this.rgb.set(rgb));
        Color.rgb_to_hsv(hsv.set(this.rgb));
        dirty = true;
    }

    public void setHSV(Vector4f hsv) {
        Color.clamp_hsv(this.hsv.set(hsv));
        Color.hsv_to_rgb(this.rgb.set(this.hsv));
        dirty = true;
    }

    public Texture hsv_window_texture() { return hsv_window_buffer.texture(0); }

    public Texture hue_slider_texture() { return hue_slider_buffer.texture(0); }

    @SuppressWarnings("SuspiciousNameCombination")
    public float pickerPositionX() { return hsv.y; }

    public float pickerPositionY() { return hsv.z; }

    public float sliderPosition() { return hsv.x / 360.0f; }

    public Vector4f getRgb() { return rgb; }

    public Vector4f getHsv() { return hsv; }

    public void dispose() {
        Disposable.dispose(shader,vertexArrayObject);
        Disposable.dispose(vertexBuffer,indexBuffer);
        Disposable.dispose(hsv_window_buffer,hue_slider_buffer);
    }

    private void render_hsv_window() {
        Framebuffer.bindDraw(hsv_window_buffer);
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        shader.use();
        shader.setUniform1i(U_DRAW_SLIDER,0);
        shader.setUniform1f(U_HUE,hsv.x);
        vertexArrayObject.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }

    private void render_hue_slider() {
        Framebuffer.bindDraw(hue_slider_buffer);
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        shader.use();
        shader.setUniform1i(U_DRAW_SLIDER,1);
        shader.setUniform1f(U_HUE,0);
        vertexArrayObject.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }

    private ShaderProgramOld create_shader_program() throws Exception {
        String vert_shader = Resources.asString(SHADER_VERT_PATH);
        String frag_shader = Resources.asString(SHADER_FRAG_PATH);
        ShaderProgramOld shader = new ShaderProgramOld(vert_shader,frag_shader);
        shader.createUniform(U_DRAW_SLIDER);
        shader.createUniform(U_HUE);
        return shader;
    }

    private Framebuffer create_hsv_window_framebuffer() throws Exception {
        Framebuffer hsv_window_buffer = new Framebuffer(HSV_WINDOW_RES_WIDTH,HSV_WINDOW_RES_HEIGHT);
        Framebuffer.bind(hsv_window_buffer);
        Framebuffer.setClearColor(0,0,0,0);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Texture hsw_window_texture = Texture.generate2D(HSV_WINDOW_RES_WIDTH,HSV_WINDOW_RES_HEIGHT);
        hsw_window_texture.bindToActiveSlot();
        hsw_window_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        hsw_window_texture.textureFilter(GL_LINEAR,GL_LINEAR);
        hsw_window_texture.clampToEdge();
        Framebuffer.attachColor(hsw_window_texture,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
        return hsv_window_buffer;
    }

    private Framebuffer create_hue_slider_framebuffer() throws Exception {
        Framebuffer hue_slider_buffer = new Framebuffer(HUE_SLIDER_RES_WIDTH,HUE_SLIDER_RES_HEIGHT);
        Framebuffer.bind(hue_slider_buffer);
        Framebuffer.setClearColor(0,0,0,0);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Texture hue_slider_texture = Texture.generate2D(HUE_SLIDER_RES_WIDTH,HUE_SLIDER_RES_HEIGHT);
        hue_slider_texture.bindToActiveSlot();
        hue_slider_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        hue_slider_texture.textureFilter(GL_LINEAR,GL_LINEAR);
        hue_slider_texture.clampToEdge();
        Framebuffer.attachColor(hue_slider_texture,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
        return hue_slider_buffer;
    }





}
