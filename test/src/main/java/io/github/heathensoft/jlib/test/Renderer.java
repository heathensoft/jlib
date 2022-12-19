package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.hud.Hud;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class Renderer implements Disposable {

    private final Hud hud;
    private final ScreenQuad screenQuad;
    private final SpriteBatch spriteBatch;
    private final ShaderProgram defaultProgram;

    public Renderer(Hud hud) throws Exception {
        this.hud = hud;
        spriteBatch = new SpriteBatch(256);
        screenQuad = new ScreenQuad();
        String vs_shader = ScreenQuad.default_screen_vs_shader();
        String fs_shader = ScreenQuad.default_screen_fs_shader();
        defaultProgram = new ShaderProgram(vs_shader,fs_shader);
        defaultProgram.use();
        defaultProgram.createUniform("u_sampler");
    }

    public void render(float frame_time, float alpha, Vector2f mouseViewport) {
        hud.clearIdBuffer();
        hud.render(spriteBatch,mouseViewport);
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        defaultProgram.use();
        defaultProgram.setUniform1i("u_sampler",0);
        hud.FRAMEBUFFER.hud_texture().bindToSlot(0);
        screenQuad.render();
    }

    public void dispose() {
        Disposable.dispose(screenQuad,spriteBatch,defaultProgram);
    }
}
