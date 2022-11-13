package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Frederik Dahl
 * 13/11/2022
 */


public class SpriteFBO extends Framebuffer {
    
    private final Texture diffuse;
    private final Texture ids;
    
    public SpriteFBO(int width, int height) throws Exception {
        super(width, height);
        bind(this);
        diffuse = new Texture(GL_TEXTURE_2D);
        diffuse.bindToActiveSlot();
        diffuse.nearest();
        diffuse.wrapST(GL_CLAMP_TO_BORDER);
        diffuse.RGBA8_2D((ByteBuffer)null,width,height);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,diffuse.target(),diffuse.id(),0);
        ids = new Texture(GL_TEXTURE_2D);
        ids.bindToActiveSlot();
        ids.nearest();
        ids.wrapST(GL_CLAMP_TO_BORDER);
        ids.R32UI_2D((IntBuffer)null,width,height);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT1,ids.target(),ids.id(),0);
        drawBuffers(GL_COLOR_ATTACHMENT0,GL_COLOR_ATTACHMENT1);
        readBuffer(GL_COLOR_ATTACHMENT1);
        setClearMask(GL_COLOR_BUFFER_BIT);
        setClearColor(0.2f,0,0.2f,0);
        checkStatus();
        bindDefault();
    }
    
    public Texture diffuseTexture() {
        return diffuse;
    }
    
    public Texture idTexture() {
        return ids;
    }
    
    @Override
    public void disposeInternal() {
        Disposable.dispose(diffuse,ids);
    }
    
    @Override
    public void resize(Resolution resolution) {
    
    }
}
