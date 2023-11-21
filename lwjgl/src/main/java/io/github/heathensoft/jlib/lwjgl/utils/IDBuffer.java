package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureFormat;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL15.GL_STREAM_READ;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_MAP_READ_BIT;
import static org.lwjgl.opengl.GL30.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL30.glMapBufferRange;
import static org.lwjgl.opengl.GL32.*;

/**
 * Texture can be shared between multiple framebuffers.
 * I.e. Both HUD and "World" draws id's to this texture.
 * Using this texture as attachment in both framebuffers.
 * Just remember to clear the texture every frame.
 *
 * If used with interactables, the buffer bust be cleared with 0 value
 *
 * @author Frederik Dahl
 * 14/01/2023
 */


public class IDBuffer extends Framebuffer {

    private final Texture uid_texture;
    private final ByteBuffer pixelBuffer;
    private final IntBuffer syncBuffer;
    private final BufferObject pbo;
    private long syncObject;
    private int syncStatus;
    private int pixelID;

    public IDBuffer(int width, int height) throws Exception {
        super(width, height);
        bind(this);
        // initialize id-texture. read/write ops.
        uid_texture = Texture.generate2D(width, height);
        uid_texture.bindToActiveSlot();
        uid_texture.filter(GL_NEAREST,GL_NEAREST);
        uid_texture.clampToEdge();
        uid_texture.allocate(TextureFormat.R32_UNSIGNED_INTEGER,false);
        attachColor(uid_texture,0,true);
        drawBuffer(0);
        readBuffer(0);
        setClearColor(0,0,0,0);
        checkStatus();
        // prepare pixel read ops
        syncStatus = GL_UNSIGNALED;
        syncObject = 0L;
        pixelID = 0;
        pixelBuffer = MemoryUtil.memAlloc(Integer.BYTES);
        syncBuffer = MemoryUtil.memAllocInt(1);
        pbo = new BufferObject(GL_PIXEL_PACK_BUFFER, GL_STREAM_READ);
        pbo.bind();
        pbo.bufferData((long) width * height * Integer.BYTES);
        BufferObject.bindZERO(GL_PIXEL_PACK_BUFFER);
        Texture.unbindActiveSlot(uid_texture.target());
    }

    public void readID(Vector2f mouseViewport) {
        readID(mouseViewport.x,mouseViewport.y);
    }

    public void readID(float mouseViewportX, float mouseViewportY) {
        int x = (int)(mouseViewportX * width);
        int y = (int)(mouseViewportY * height);
        if (syncStatus == GL_SIGNALED) {
            syncStatus = GL_UNSIGNALED;
            glDeleteSync(syncObject);
            syncObject = 0L;
            pbo.bind();
            ByteBuffer pixel = glMapBufferRange(GL_PIXEL_PACK_BUFFER,0,Integer.BYTES,GL_MAP_READ_BIT,pixelBuffer);
            if (pixel != null) {
                pixelID = (pixel.get(0)) | (pixel.get(1) << 8) | (pixel.get(2) << 16) | (pixel.get(3) << 24);
                glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
            } bindRead(this);
            readBuffer(GL_COLOR_ATTACHMENT0); // bind uid buffer for read ops
            glReadPixels(x, y, 1,1, GL_RED_INTEGER, GL_UNSIGNED_INT,0);
            syncObject = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        } else {
            if (syncObject == 0L) {
                bindRead(this);
                readBuffer(GL_COLOR_ATTACHMENT0); // bind uid buffer for read ops
                pbo.bind();
                glReadPixels(x, y, 1,1, GL_RED_INTEGER, GL_UNSIGNED_INT,0);
                syncObject = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            } else {
                glGetSynciv(syncObject,GL_SYNC_STATUS,null,syncBuffer);
                syncStatus = syncBuffer.get(0);
            }
        }
    }

    public int pixelID() {
        return pixelID;
    }

    public Texture texture() {
        return uid_texture;
    }

    public Texture texture(int index) {
        return uid_texture;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (syncBuffer != null) MemoryUtil.memFree(syncBuffer);
        if (pixelBuffer != null) MemoryUtil.memFree(pixelBuffer);
        Disposable.dispose(pbo);
    }
}
