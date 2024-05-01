package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.ui.GUI;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 07/04/2024
 */


public class ColorPicker implements Disposable {

    private final Framebuffer hsv_window_buffer;
    private final Texture alpha_slider_texture;
    private final Texture hue_slider_texture;
    private final Texture alpha_slider_background;
    private final Vector4f hsv;
    private Vector4f rgb;
    private boolean dirty;

    public ColorPicker() throws Exception {
        Framebuffer framebuffer = new Framebuffer(256,16);
        Framebuffer.bind(framebuffer);
        alpha_slider_texture = Texture.generate2D(256,16);
        alpha_slider_texture.bindToActiveSlot();
        alpha_slider_texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        alpha_slider_texture.clampToEdge();
        alpha_slider_texture.filterLinear();
        Framebuffer.attachColor(alpha_slider_texture,0,false);
        hue_slider_texture = Texture.generate2D(256,16);
        hue_slider_texture.bindToActiveSlot();
        hue_slider_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        hue_slider_texture.clampToEdge();
        hue_slider_texture.filterLinear();
        Framebuffer.attachColor(hue_slider_texture,1,false);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
        Framebuffer.viewport();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        ShaderProgram.bindProgram(GUI.shaders.color_picker);
        ShaderProgram.setUniform("u_mode",0);
        ShaderProgram.shaderPass().draw();
        Framebuffer.drawBuffer(1);
        Framebuffer.checkStatus();
        ShaderProgram.setUniform("u_mode",2);
        ShaderProgram.shaderPass().drawRepeat();
        Disposable.dispose(framebuffer);
        hsv_window_buffer = new Framebuffer(256,256);
        Framebuffer.bind(hsv_window_buffer);
        Framebuffer.setClearColor(0,0,0,0);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Texture hsw_window_texture = Texture.generate2D(256,256);
        hsw_window_texture.bindToActiveSlot();
        hsw_window_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        hsw_window_texture.filterLinear();
        hsw_window_texture.clampToEdge();
        Framebuffer.attachColor(hsw_window_texture,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
        Framebuffer.viewport();
        hsv = new Vector4f(180.0f,0.5f,0.5f,1.0f);
        rgb = Color.hsv_to_rgb(new Vector4f(hsv));
        ShaderProgram.setUniform("u_mode",1);
        ShaderProgram.setUniform("u_hue",hsv.x);
        ShaderProgram.shaderPass().drawRepeat();
        Bitmap bitmap = Bitmap.checkeredPattern(0xFFAAAAAA,0xFF6E6E6E,4,4,4);
        alpha_slider_background = bitmap.asTexture();
        alpha_slider_background.bindToActiveSlot();
        alpha_slider_background.filterNearest();
        alpha_slider_background.textureRepeat();
        bitmap.dispose();
    }

    public void refresh() {
        if (dirty) {
            Framebuffer.bind(hsv_window_buffer);
            Framebuffer.viewport();
            glDisable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            ShaderProgram.bindProgram(GUI.shaders.color_picker);
            ShaderProgram.setUniform("u_mode",1);
            ShaderProgram.setUniform("u_hue",hsv.x);
            ShaderProgram.shaderPass().draw();
            dirty = false;
        }
    }

    public void pick(Vector2f picker_position) {
        hsv.y = clamp(picker_position.x);
        hsv.z = clamp(picker_position.y);
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

    public void selectAlpha(float slider_position) {
        this.rgb.w = U.clamp(1 - slider_position);
    }

    public void editColor(Vector4f rgb) {
        if (this.rgb != rgb) {
            this.rgb = Color.clamp_rgb(rgb);
            Color.rgb_to_hsv(hsv.set(rgb));
            dirty = true;
        }

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

    public Vector4f getAlphaBackgroundUV(Rectanglef quad, Vector4f dst) {
        dst.x = 0; dst.y = 0;
        dst.z = quad.lengthX() / alpha_slider_background.width();
        dst.w = quad.lengthY() / alpha_slider_background.height();
        return dst;
    }

    public Texture alphaSliderBackground() { return alpha_slider_background; }

    public Texture alphaSliderTexture() { return alpha_slider_texture; }

    public Texture husSliderTexture() { return hue_slider_texture; }

    public Texture hsvWindowTexture() { return hsv_window_buffer.texture(0); }

    @SuppressWarnings("SuspiciousNameCombination")
    public float pickerPositionX() { return hsv.y; }

    public float pickerPositionY() { return hsv.z; }

    public float hueSliderPosition() { return hsv.x / 360.0f; }

    public float alphaSliderPosition() { return (1 - rgb.w); }

    public Vector4f getRgb() { return rgb; }

    public Vector4f getHsv() { return hsv; }

    public void dispose() {
        Disposable.dispose(
                hsv_window_buffer,
                alpha_slider_texture,
                hue_slider_texture,
                alpha_slider_background);
    }
}
