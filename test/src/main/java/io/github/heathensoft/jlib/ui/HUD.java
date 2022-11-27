package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.IDBuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 17/11/2022
 */


public class HUD {

    public final int WIDTH;
    public final int HEIGHT;
    public final Matrix4f MATRIX;
    public final HudAssets ASSETS;
    public final HudShader SHADER;
    public final IDBuffer ID_BUFFER;
    public final HudFramebuffer FRAMEBUFFER;


    public HUD(int width, int height) throws Exception {
        WIDTH = width;
        HEIGHT = height;
        Matrix4f view = new Matrix4f();
        view.identity().lookAt(0,0,1,
        0,0,-1, 0,1,0);
        MATRIX = new Matrix4f();
        MATRIX.ortho(0,WIDTH,0,HEIGHT,0.01f,1);
        MATRIX.mul(view);
        ASSETS = new HudAssets();
        SHADER = new HudShader();
        ID_BUFFER = new IDBuffer(WIDTH,HEIGHT);
        FRAMEBUFFER = new HudFramebuffer(ID_BUFFER,WIDTH,HEIGHT);
    }

    /**
     * Render HUD AFTER rendering the "world".
     * This clears the HUD color texture, renders the HUD
     * and reads from the ID_BUFFER.
     * @param batch sprite batch
     * @param mouseViewport mouse position in viewport space
     */
    public void render(SpriteBatch batch,Vector2f mouseViewport) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        // blending does not affect the id buffer
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        Framebuffer.bindDraw(FRAMEBUFFER);
        Framebuffer.viewport();
        FRAMEBUFFER.clearColorBuffer();
        SHADER.use();
        SHADER.setUniform("u_combined", MATRIX);
        int numTextures = ASSETS.NUM_TEXTURES;
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(numTextures);
            for (int i = 0; i < numTextures; i++) buffer.put(i);
            SHADER.setUniform1iv("u_texture",buffer.flip());
        } for (int i = 0; i < numTextures; i++)
            ASSETS.texture(i).bindToSlot(i);
        batch.begin();
        // draw elements
        batch.end();
        ID_BUFFER.readID(mouseViewport.x,mouseViewport.y);
        // Might be better to do this before rendering "world"
        // Clear together with the "entity" framebuffer
        //Framebuffer.bindDraw(ID_BUFFER);
        //Framebuffer.clear();
    }


    public void update(float dt) {

        // update elements
    }

    public void dispose() {
        Disposable.dispose(ASSETS);
        Disposable.dispose(SHADER);
        Disposable.dispose(ID_BUFFER);
        Disposable.dispose(FRAMEBUFFER);
    }
}
