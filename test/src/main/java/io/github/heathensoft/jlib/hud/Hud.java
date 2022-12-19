package io.github.heathensoft.jlib.hud;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.hud.ui.HudItem;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 17/12/2022
 */


public class Hud implements Disposable {

    public final int WIDTH;
    public final int HEIGHT;
    public final HudGraphics GRAPHICS;
    public final Matrix4f MATRIX;
    public final HudShader SHADER;
    public final IDBuffer ID_BUFFER;
    public final HudFramebuffer FRAMEBUFFER;

    public Hud(int width, int height) throws Exception {
        WIDTH = width; HEIGHT = height;
        Matrix4f view = new Matrix4f();
        view.identity().lookAt(0,0,1,
        0,0,-1, 0,1,0);
        MATRIX = new Matrix4f();
        MATRIX.ortho(0,WIDTH,0,HEIGHT,0.01f,1);
        MATRIX.mul(view);
        SHADER = new HudShader();
        GRAPHICS = new HudGraphics();
        ID_BUFFER = new IDBuffer(width, height);
        FRAMEBUFFER = new HudFramebuffer(ID_BUFFER,width,height);
    }

    public void render(SpriteBatch batch, Vector2f mouseViewport) {
        Framebuffer.bindDraw(FRAMEBUFFER);
        Framebuffer.viewport();
        FRAMEBUFFER.clearColorBuffer();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        SHADER.prepare(this);
        batch.begin();
        HudItem.renderAll(batch);
        batch.end();
        ID_BUFFER.readID(mouseViewport);
    }

    public void update(float dt) {
        HudItem.updateAll(dt);
    }

    public int interactableData() {
        return ID_BUFFER.pixelID();
    }

    public void clearIdBuffer() {
        // Might be better to do this before rendering "world"
        // Clear together with the "entity" framebuffer
        Framebuffer.bindDraw(ID_BUFFER);
        Framebuffer.viewport();
        Framebuffer.clear();
    }

    public void dispose() {
        HudItem.disposeAll();
        Disposable.dispose(SHADER);
        Disposable.dispose(GRAPHICS);
        Disposable.dispose(ID_BUFFER);
        Disposable.dispose(FRAMEBUFFER);
    }

}
