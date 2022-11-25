package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.IDBuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class HudFramebuffer extends Framebuffer {
    
    private final Texture color_texture;
    
    public HudFramebuffer(IDBuffer idBuffer, int width, int height) throws Exception {
        super(width, height);
        // create attachments
        bind(this);
        color_texture = new Texture(GL_TEXTURE_2D);
        color_texture.bindToActiveSlot();
        color_texture.nearest();
        color_texture.wrapST(GL_CLAMP_TO_BORDER);
        color_texture.RGBA8_2D((ByteBuffer)null,width,height);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,
        color_texture.target(), color_texture.id(),0);
        // uid_texture is disposed externally
        Texture uid_texture = idBuffer.texture();
        uid_texture.bindToActiveSlot();
        // It's possible I only need to set target to DRAW_BUFFER
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT1,
        uid_texture.target(), uid_texture.id(),0);
        drawBuffers(GL_COLOR_ATTACHMENT0,GL_COLOR_ATTACHMENT1);
        //readBuffer(GL_COLOR_ATTACHMENT1);
        setClearMask(GL_COLOR_BUFFER_BIT);
        setClearColor(0.0f,0,0.0f,0);
        checkStatus();
        
    }

    public void clearColorBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer color = stack.mallocFloat(4);
            color.put(clearColor.r);
            color.put(clearColor.g);
            color.put(clearColor.b);
            color.put(clearColor.a);
            clearColorBufferFloat(GL_COLOR_ATTACHMENT0,color);
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
    public void resize(Resolution resolution) {
    
    }
}
