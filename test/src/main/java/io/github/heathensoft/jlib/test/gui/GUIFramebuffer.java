package io.github.heathensoft.jlib.test.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.test.graphicsOld.Framebuffer;
import io.github.heathensoft.jlib.test.graphicsOld.IDBuffer;
import io.github.heathensoft.jlib.test.graphicsOld.Texture;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class GUIFramebuffer extends Framebuffer {
    
    private final Texture color_texture;
    
    public GUIFramebuffer(IDBuffer idBuffer, int width, int height) throws Exception {
        super(width, height);
        bind(this);
        color_texture = new Texture(GL_TEXTURE_2D);
        color_texture.bindToActiveSlot();
        color_texture.nearest();
        color_texture.wrapST(GL_CLAMP_TO_BORDER);
        color_texture.RGBA8_2D((ByteBuffer)null,width,height);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,
        color_texture.target(), color_texture.id(),0);
        Texture uid_texture = idBuffer.texture();
        uid_texture.bindToActiveSlot();
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT1,
        uid_texture.target(), uid_texture.id(),0);
        drawBuffers(GL_COLOR_ATTACHMENT0,GL_COLOR_ATTACHMENT1);
        setClearMask(GL_COLOR_BUFFER_BIT);
        setClearColor(0.0f,0.0f,0.0f,0.0f);
        checkStatus();
    }

    public void clearColorBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer color = stack.mallocFloat(4);
            color.put(clearColor.r);
            color.put(clearColor.g);
            color.put(clearColor.b);
            color.put(clearColor.a);
            clearColorBufferFloat(0,color.flip());
        }
    }
    
    public Texture hud_texture() {
        return color_texture;
    }
    
    @Override
    public void disposeInternal() {
        Disposable.dispose(color_texture);
    }
    
    @Override
    public void resize(Resolution resolution) { }
}
