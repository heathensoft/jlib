package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;

/**
 * @author Frederik Dahl
 * 30/10/2022
 */


public abstract class Framebuffer implements Disposable {
    
    private static final Color DEFAULT_COLOR = new Color(0,0,0,0);
    private static int DEFAULT_CLEAR_MASK = GL_COLOR_BUFFER_BIT;
    private static Framebuffer readBuffer = null;
    private static Framebuffer drawBuffer = null;
    
    protected int id;
    protected int width;
    protected int height;
    protected int clearMask = GL_COLOR_BUFFER_BIT;
    public final Color clearColor = new Color(0,0,0,0);
    
    
    public Framebuffer(int width, int height) {
        this.id = glGenFramebuffers();
        this.width = width;
        this.height = height;
    }
    
    public int id() {
        return id;
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public abstract void disposeInternal();
    
    public abstract void resize(Resolution resolution);
    
    public final void dispose() {
        onDispose(this);
        glDeleteFramebuffers(id);
        disposeInternal();
    }
    
    
    public static Framebuffer drawBuffer() {
        return drawBuffer;
    }
    
    public static Framebuffer readBuffer() {
        return readBuffer;
    }
    
    public static void bindDefault() {
        bind(null);
    }
    
    public static void bind(Framebuffer buffer) {
        int bufferID = buffer == null ? 0 : buffer.id;
        int readBufferID = readBuffer == null ? 0 : readBuffer.id;
        int drawBufferID = drawBuffer == null ? 0 : drawBuffer.id;
        if (bufferID != readBufferID || bufferID != drawBufferID) {
            readBuffer = buffer; drawBuffer = buffer;
            glBindFramebuffer(GL_FRAMEBUFFER, bufferID);
        }
    }
    
    public static void bindRead(Framebuffer buffer) {
        int bufferID = buffer == null ? 0 : buffer.id;
        int readBufferID = readBuffer == null ? 0 : readBuffer.id;
        if (bufferID != readBufferID) {
            readBuffer = buffer;
            glBindFramebuffer(GL_READ_FRAMEBUFFER, bufferID);
        }
    }
    
    public static void bindDraw(Framebuffer buffer) {
        int bufferID = buffer == null ? 0 : buffer.id;
        int drawBufferID = drawBuffer == null ? 0 : drawBuffer.id;
        if (bufferID != drawBufferID) {
            drawBuffer = buffer;
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, bufferID);
        }
    }
    
    public static void drawBuffers(int ...drawBuffers) {
        if (drawBuffers != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer intBuff = stack.mallocInt(drawBuffers.length);
                for (int buffer : drawBuffers) {
                    intBuff.put(buffer);
                } glDrawBuffers(intBuff.flip());
            }
        }
    }
    
    public static void viewport() {
        if (drawBuffer == null) {
            Engine.get().window().viewport().refresh();
        } else Engine.get().window().viewport().set(drawBuffer.width,drawBuffer.height);
    }
    
    public static void readBuffer(int readBuffer) {
        glReadBuffer(readBuffer);
    }
    
    public static void drawBuffer(int drawBuffer) {
        glDrawBuffer(drawBuffer);
    }
    
    /**
     * Set Default framebuffer clear-mask
     * (Framebuffer provided by GLFW)
     * @param mask the mask. i.e. color, depth, stencil.
     */
    public static void setDefaultClearMask(int mask) {
        DEFAULT_CLEAR_MASK = mask;
    }
    
    /**
     * Clear current draw-buffer
     * with draw-buffer's clear - color/mask
     */
    public static void clear() {
        int clearMask; Color c;
        if (drawBuffer == null) {
            c = DEFAULT_COLOR;
            clearMask = DEFAULT_CLEAR_MASK;
        } else { c = drawBuffer.clearColor;
            clearMask = drawBuffer.clearMask;
        } glClearColor(c.r,c.g,c.b,c.a);
        glClear(clearMask);
    }
    
    /**
     * Clear single drawbuffer
     * @param index 1 - 15
     * @param value the color value
     */
    public static void clearColorBufferInt(int index, IntBuffer value) {
        glClearBufferiv(GL_COLOR,index,value);
    }
    
    /**
     * Clear single drawbuffer
     * @param index 1 - 15
     * @param value the color value
     */
    public static void clearColorBufferUInt(int index, IntBuffer value) {
        glClearBufferuiv(GL_COLOR,index,value);
    }
    
    /**
     * Clear single drawbuffer
     * @param index 1 - 15
     * @param value the color value
     */
    public static void clearColorBufferFloat(int index, FloatBuffer value) {
        glClearBufferfv(GL_COLOR,index,value);
    }
    
    /**
     * Clear depth buffer
     * @param value the color value
     */
    public static void clearDepthBuffer(float value) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer v = stack.mallocFloat(1);
            v.put(value).flip();
            glClearBufferfv(GL_DEPTH,0,v);
        }
    }
    
    /**
     * Clear stencil buffer
     * @param value the color value
     */
    public static void clearStencilBuffer(int value) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer v = stack.mallocInt(1);
            v.put(value).flip();
            glClearBufferiv(GL_STENCIL,0,v);
        }
    }
    
    
    public static void checkStatus() throws Exception {
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            String message = switch (status) {
                case GL_FRAMEBUFFER_UNDEFINED                       -> ": Framebuffer undefined";
                case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT           -> ": Incomplete attachment";
                case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT   -> ": Missing attachment";
                case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER          -> ": Incomplete draw buffer";
                case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER          -> ": Incomplete read buffer";
                case GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE          -> ": Attachment object type";
                case GL_FRAMEBUFFER_UNSUPPORTED                     -> ": Framebuffer unsupported";
                case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE          -> ": Incomplete multi-sample";
                case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS        -> ": Incomplete layer targets";
                default                                             -> ": Unknown error";
            };
            throw new Exception("Incomplete framebuffer " + status + message);
        }
    }
    
    private static void onDispose(Framebuffer frameBuffer) {
        if (frameBuffer == readBuffer) bindRead(null);
        if (frameBuffer == drawBuffer) bindDraw(null);
    }
}
