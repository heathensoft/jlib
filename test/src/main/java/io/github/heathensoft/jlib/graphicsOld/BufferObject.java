package io.github.heathensoft.jlib.graphicsOld;

import io.github.heathensoft.jlib.common.Disposable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glBindBufferRange;

/**
 * A buffer object are all opengl buffers created by glGenerateBuffers.
 * This is a utility wrapper around an opengl buffer. And can be useful when the
 * buffer you are creating have a single purpose. (The target and usage for the buffer never change)
 * i.e. target: ELEMENT ARRAY BUFFER and usage: STATIC DRAW
 * It contains the most common functionality.
 *
 * @author Frederik Dahl
 * 30/10/2022
 */


public class BufferObject implements Disposable {
    
    protected final int target;
    protected final int usage;
    protected final int name;
    
    public BufferObject(int target, int usage) {
        this.name = glGenBuffers();
        this.target = target;
        this.usage = usage;
    }
    
    public BufferObject bind() {
        glBindBuffer(target, name);
        return this;
    }
    
    public void bufferData(byte[] data) {
        ByteBuffer buffer = null;
        try { buffer = MemoryUtil.memAlloc(data.length);
            buffer.put(data).flip();
            bufferData(buffer);
        }finally {
            if (buffer != null)
                MemoryUtil.memFree(buffer);
        }
    }
    
    public void bufferData(short[] data) {
        ShortBuffer buffer = null;
        try { buffer = MemoryUtil.memAllocShort(data.length);
            buffer.put(data).flip();
            bufferData(buffer);
        }finally {
            if (buffer != null)
                MemoryUtil.memFree(buffer);
        }
    }
    
    public void bufferData(int[] data) {
        IntBuffer buffer = null;
        try { buffer = MemoryUtil.memAllocInt(data.length);
            buffer.put(data).flip();
            bufferData(buffer);
        }finally {
            if (buffer != null){
                MemoryUtil.memFree(buffer);
            }
        }
    }
    
    public void bufferData(float[] data) {
        FloatBuffer buffer = null;
        try { buffer = MemoryUtil.memAllocFloat(data.length);
            buffer.put(data).flip();
            bufferData(buffer);
        }finally {
            if (buffer != null)
                MemoryUtil.memFree(buffer);
        }
    }
    
    public void bufferData(long bytes) {
        glBufferData(target,bytes,usage);
    }
    
    public void bufferData(FloatBuffer data) {
        glBufferData(target,data,usage);
    }
    
    public void bufferData(IntBuffer data) {
        glBufferData(target,data,usage);
    }
    
    public void bufferData(ShortBuffer data) {
        glBufferData(target,data,usage);
    }
    
    public void bufferData(ByteBuffer data) {
        glBufferData(target,data,usage);
    }
    
    public void bufferSubData(FloatBuffer data, int offset) {
        glBufferSubData(target,offset,data);
    }
    
    public void bufferSubData(IntBuffer data, int offset) {
        glBufferSubData(target,offset,data);
    }
    
    public void bufferSubData(ShortBuffer data, int offset) {
        glBufferSubData(target,offset,data);
    }
    
    public void bufferSubData(ByteBuffer data, int offset) {
        glBufferSubData(target,offset,data);
    }
    
    public void bindBufferBase(int bindingPoint) {
        glBindBufferBase(target, bindingPoint, name);
    }
    
    public void bindBufferRange(int bindingPoint, long offset, long size) {
        glBindBufferRange(target, bindingPoint, name,offset,size);
    }
    
    public int name() {
        return name;
    }
    
    public int target() {
        return target;
    }
    
    public int usage() {
        return usage;
    }
    
    public static void bindZERO(int target) {
        glBindBuffer(target,0);
    }
    
    @Override
    public void dispose() {
        glDeleteBuffers(name);
    }

}
