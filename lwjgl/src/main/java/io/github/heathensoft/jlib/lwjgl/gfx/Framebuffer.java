package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
import static org.lwjgl.opengl.GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

/**
 * Textures can be disposed with the framebuffer object (creation argument: dispose_with_fbo).
 * In other words you won't need to keep any references to it outside if the use for it
 * is limited to the framebuffer.
 * You can only reattach color textures. (not depth stencil)
 *
 * Always remember to call "check status"
 *
 * @author Frederik Dahl
 * 14/01/2023
 */


public class Framebuffer implements Disposable {

    private static final Vector4f DEFAULT_CLEAR_COLOR = new Vector4f(0f,0f,0f,0f);
    private static int DEFAULT_CLEAR_MASK = GL_COLOR_BUFFER_BIT;
    private static Framebuffer readBuffer = null;
    private static Framebuffer drawBuffer = null;

    protected int id;
    protected int width;
    protected int height;
    protected int clear_mask;
    protected Vector4f clear_color;
    protected ColorAttachment[] colorAttachments;
    protected NonColorAttachment depthAttachment;
    protected NonColorAttachment stencilAttachment;
    protected NonColorAttachment depthStencilAttachment;

    public Framebuffer(int width, int height) {
        this.id = glGenFramebuffers();
        this.width = width;
        this.height = height;
        this.clear_mask = GL_COLOR_BUFFER_BIT;
        this.clear_color = new Vector4f(DEFAULT_CLEAR_COLOR);
        this.colorAttachments = new ColorAttachment[16];
    }


    /**
     * Detaches the currently bound color texture from the framebuffer attachment slot.
     * You must use this before attaching a new texture to the same slot.
     * The only reason being, I want it to be done explicitly. So there are no
     * accidental overwrites where the old texture is not freed.
     * @param attachment_slot the attachment slot [0-15]
     * @return The previously bound color texture
     * @throws Exception If the current draw-buffer is the glfw default,
     * or if no textures are bound to the slot.
     */
    public static Texture detachColor(int attachment_slot) throws Exception {
        if (drawBuffer != null) {
            ColorAttachment colorAttachment = drawBuffer.colorAttachments[attachment_slot];
            if (colorAttachment != null) {
                int slot = GL_COLOR_ATTACHMENT0 + attachment_slot;
                glFramebufferTexture(GL_FRAMEBUFFER,slot,0,0);
                drawBuffer.colorAttachments[attachment_slot] = null;
                return colorAttachment.texture();
            } throw new Exception("no texture attached to slot: " + attachment_slot);
        } throw new Exception("cannot detach from default framebuffer");
    }

    public static void attachColor(Texture texture, int attachment_slot, boolean dispose_with_fbo) throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.colorAttachments[attachment_slot] == null) {
                if (texture.width() == drawBuffer.width && texture.height() == drawBuffer.height) {
                    TextureFormat format = texture.format();
                    if (format.is_color_format) {
                        int slot = GL_COLOR_ATTACHMENT0 + attachment_slot;
                        glFramebufferTexture(GL_FRAMEBUFFER,slot,texture.id(),0);
                        drawBuffer.colorAttachments[attachment_slot] =
                        new ColorAttachment(texture,dispose_with_fbo);
                        return;
                    } throw new Exception("cannot attach non-color texture to color attachment ");
                } throw new Exception("texture dimensions does not match framebuffer");
            } throw new Exception("framebuffer already has color attachment in slot");
        } throw new Exception("cannot attach to default framebuffer");
    }

    public static void attachDepth(Texture texture, boolean dispose_with_fbo) throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.depthAttachment == null && drawBuffer.depthStencilAttachment == null) {
                if (texture.width() == drawBuffer.width && texture.height() == drawBuffer.height) {
                    TextureFormat format = texture.format();
                    if (!format.is_color_format) {
                        if (format.pixel_format == GL_DEPTH_COMPONENT) {
                            glFramebufferTexture(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,texture.id(),0);
                            drawBuffer.depthAttachment = new NonColorAttachment(texture,dispose_with_fbo);
                            return;
                        } throw new Exception("wrong format for depth attachment");
                    } throw new Exception("cannot attach color format texture to non-color attachment");
                } throw new Exception("texture dimensions does not match framebuffer");
            } throw new Exception("framebuffer already has non-color attachment of type");
        } throw new Exception("cannot attach to default framebuffer");
    }

    public static void attachStencil(Texture texture, boolean dispose_with_fbo) throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.stencilAttachment == null && drawBuffer.depthStencilAttachment == null) {
                if (texture.width() == drawBuffer.width && texture.height() == drawBuffer.height) {
                    TextureFormat format = texture.format();
                    if (!format.is_color_format) {
                        if (format.pixel_format == GL_STENCIL_INDEX) {
                            glFramebufferTexture(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT,texture.id(),0);
                            drawBuffer.stencilAttachment = new NonColorAttachment(texture,dispose_with_fbo);
                            return;
                        } throw new Exception("wrong format for stencil attachment");
                    } throw new Exception("cannot attach color format texture to non-color attachment");
                } throw new Exception("texture dimensions does not match framebuffer");
            } throw new Exception("framebuffer already has non-color attachment of type");
        } throw new Exception("cannot attach to default framebuffer");
    }

    public static void attachDepthStencil(Texture texture, boolean dispose_with_fbo) throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.stencilAttachment == null && drawBuffer.depthAttachment == null
                && drawBuffer.depthStencilAttachment == null) {
                if (texture.width() == drawBuffer.width && texture.height() == drawBuffer.height) {
                    TextureFormat format = texture.format();
                    if (!format.is_color_format) {
                        if (format.pixel_format == GL_DEPTH_STENCIL) {
                            glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,texture.id(),0);
                            drawBuffer.stencilAttachment = new NonColorAttachment(texture,dispose_with_fbo);
                            return;
                        } throw new Exception("wrong format for depth-stencil attachment");
                    } throw new Exception("cannot attach color format texture to non-color attachment");
                } throw new Exception("texture dimensions does not match framebuffer");
            } throw new Exception("framebuffer already has non-color attachment of type");
        } throw new Exception("cannot attach to default framebuffer");
    }

    public static void attachDepth16() throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.depthAttachment == null && drawBuffer.depthStencilAttachment == null) {
                TextureFormat format = TextureFormat.DEPTH16;
                drawBuffer.depthAttachment = new NonColorAttachment(drawBuffer,format,GL_DEPTH_ATTACHMENT);
                return;
            } throw new Exception("framebuffer already has non-color attachment of type");
        } throw new Exception("cannot attach to default framebuffer");
    }

    public static void attachStencil8() throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.stencilAttachment == null && drawBuffer.depthStencilAttachment == null) {
                TextureFormat format = TextureFormat.STENCIL8;
                drawBuffer.stencilAttachment = new NonColorAttachment(drawBuffer,format,GL_STENCIL_ATTACHMENT);
                return;
            } throw new Exception("framebuffer already has non-color attachment of type");
        } throw new Exception("cannot attach to default framebuffer");
    }

    public static void attachDepth24_Stencil8() throws Exception {
        if (drawBuffer != null) {
            if (drawBuffer.depthAttachment == null && drawBuffer.stencilAttachment == null
                && drawBuffer.depthStencilAttachment == null) {
                TextureFormat format = TextureFormat.DEPTH24_STENCIL8;
                drawBuffer.depthStencilAttachment = new NonColorAttachment(drawBuffer,format,GL_DEPTH_STENCIL_ATTACHMENT);
                return;
            } throw new Exception("framebuffer already has non-color attachment of type");
        } throw new Exception("cannot attach to default framebuffer");
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

    public static void readBuffer(int color_slot) {
        glReadBuffer(GL_COLOR_ATTACHMENT0 + color_slot);
    }

    public static void drawBuffer(int color_slot) {
        glDrawBuffer(GL_COLOR_ATTACHMENT0 + color_slot);
    }

    public static void drawBuffers(int ...color_slots) {
        if (color_slots != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer intBuff = stack.mallocInt(color_slots.length);
                for (int slot : color_slots) {
                    intBuff.put(GL_COLOR_ATTACHMENT0 + slot);
                } glDrawBuffers(intBuff.flip());
            }
        }
    }

    public static void setClearColor(float r, float g, float b, float a) {
        if (usingDefaultDrawBuffer()) {
            DEFAULT_CLEAR_COLOR.set(r, g, b, a);
        } else drawBuffer.clear_color.set(r, g, b, a);
    }

    public static void setClearMask(int mask) {
        if (usingDefaultDrawBuffer()) {
            DEFAULT_CLEAR_MASK = mask;
        } else { drawBuffer.clear_mask = mask;
        }
    }

    public static void viewport() {
        if (usingDefaultDrawBuffer()) {
            Engine.get().window().viewport().refresh();
        } else Engine.get().window().viewport().set(drawBuffer.width,drawBuffer.height);
    }

    public static void clear() {
        int clearMask; Vector4f c;
        if (usingDefaultDrawBuffer()) {
            c = DEFAULT_CLEAR_COLOR;
            clearMask = DEFAULT_CLEAR_MASK;
        } else { c = drawBuffer.clear_color;
            clearMask = drawBuffer.clear_mask;
        } glClearColor(c.x,c.y,c.z,c.w);
        glClear(clearMask);
    }

    /**
     * Clear single drawbuffer
     * @param index not GL_COLOR_ATTACHMENT, but the index of that attachment
     * @param value the color value
     */
    public static void clearColorBufferSignedInt(int index, IntBuffer value) {
        glClearBufferiv(GL_COLOR,index,value);
    }

    /**
     * Clear single drawbuffer
     * @param index not GL_COLOR_ATTACHMENT, but the index of that attachment
     * @param value the color value
     */
    public static void clearColorBufferUnsignedInt(int index, IntBuffer value) {
        glClearBufferuiv(GL_COLOR,index,value);
    }

    public static void clearColorBufferNormalized(int index, Vector4f clearColor) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer color = stack.mallocFloat(4);
            color.put(clearColor.x);
            color.put(clearColor.y);
            color.put(clearColor.z);
            color.put(clearColor.w);
            clearColorBufferNormalized(index,color.flip());
        }
    }

    public static void clearColorBufferNormalized(int index, FloatBuffer clearColor) {
        glClearBufferfv(GL_COLOR,index,clearColor);
    }

    public static void clearDepthBuffer(float value) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer v = stack.mallocFloat(1);
            v.put(value).flip();
            glClearBufferfv(GL_DEPTH,0,v);
        }
    }

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

    /**
     * Returns a color texture attached to the framebuffer in slot
     * @param attachment_slot attachment slot between 0-15
     * @return a color texture or null if the slot is empty or the
     * current draw-buffer is the default window framebuffer
     */
    public static Texture colorTexture(int attachment_slot) {
        if (!usingDefaultDrawBuffer()) {
            ColorAttachment attachment = drawBuffer.colorAttachments[attachment_slot];
            if (attachment != null) return attachment.texture();
        } return null;
    }

    /**
     * @return the depth texture if:
     * current draw-buffer is not the default
     * and the draw-buffer has a depth texture
     * else: null
     */
    public static Texture depthTexture() {
        if (!usingDefaultDrawBuffer()) {
            if (drawBuffer.depthAttachment != null)
                return drawBuffer.depthAttachment.texture();
        } return null;
    }

    /**
     * @return the stencil texture if:
     * current draw-buffer is not the default
     * and the draw-buffer has a stencil texture
     * else: null
     */
    public static Texture stencilTexture() {
        if (!usingDefaultDrawBuffer()) {
            if (drawBuffer.stencilAttachment != null)
                return drawBuffer.stencilAttachment.texture();
        } return null;
    }

    /**
     * @return the depthStencil texture if:
     * current draw-buffer is not the default
     * and the draw-buffer has a depthStencil texture
     * else: null
     */
    public static Texture depthStencilTexture() {
        if (!usingDefaultDrawBuffer()) {
            if (drawBuffer.depthStencilAttachment != null)
                return drawBuffer.depthStencilAttachment.texture();
        } return null;
    }

    public static Framebuffer boundDrawBuffer() {
        return usingDefaultDrawBuffer() ? null : drawBuffer;
    }

    public static Framebuffer boundReadBuffer() {
        return usingDefaultReadBuffer() ? null : readBuffer;
    }

    private static boolean usingDefaultDrawBuffer() {
        return drawBuffer == null;
    }

    private static boolean usingDefaultReadBuffer() {
        return readBuffer == null;
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

    public Texture texture(int slot) {
        ColorAttachment attachment = colorAttachments[slot];
        if (attachment != null) {
            return attachment.texture();
        } return null;
    }

    public void dispose() {
        if (this == readBuffer) bindRead(null);
        if (this == drawBuffer) bindDraw(null);
        for (int i = 0; i < colorAttachments.length; i++) {
            Disposable.dispose(colorAttachments[i]);
        } Disposable.dispose(depthStencilAttachment);
        Disposable.dispose(stencilAttachment);
        Disposable.dispose(depthAttachment);
        glDeleteFramebuffers(id);
    }



    private static abstract class Attachment implements Disposable {
        protected Texture texture;
        protected boolean dispose_with_fbo;
        public Texture texture() {
            return texture;
        }
    }

    private static final class ColorAttachment extends Attachment {
        ColorAttachment(Texture texture, boolean dispose_with_fbo) {
            this.dispose_with_fbo = dispose_with_fbo;
            this.texture = texture;
        } public void dispose() {
            if (dispose_with_fbo) {
                Disposable.dispose(texture);
            }
        }
    }

    private static final class NonColorAttachment extends Attachment {
        private int rbo;
        NonColorAttachment(Texture texture, boolean dispose_with_fbo) {
            this.dispose_with_fbo = dispose_with_fbo;
            this.texture = texture;
        } NonColorAttachment(Framebuffer fbo, TextureFormat format, int attachment) {
            this.dispose_with_fbo = true;
            this.rbo = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER,rbo);
            glRenderbufferStorage(GL_RENDERBUFFER,format.sized_format,fbo.width,fbo.height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER,attachment,GL_RENDERBUFFER,rbo);
        } public void dispose() {
            if (dispose_with_fbo) {
                if (texture == null) {
                    glDeleteRenderbuffers(rbo);
                } else { Disposable.dispose(texture); }
            }
        }
    }

}
