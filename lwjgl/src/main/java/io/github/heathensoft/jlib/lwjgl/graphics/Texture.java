package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.lwjgl.system.MemoryUtil;

import java.nio.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.*;

/**
 * @author Frederik Dahl
 * 12/01/2023
 */


public class Texture implements Disposable {

    private static int active_slot;

    private TextureFormat format;

    private int id;
    private final int target;
    private final int width;
    private final int height;
    private final int depth;

    private Texture(int target, int width, int height, int depth) {
        this.id = glGenTextures();
        this.target = target;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public static Texture generate1D(int width) {
        return new Texture(GL_TEXTURE_1D,width,1,1);
    }

    public static Texture generate1DArray(int width, int layers) {
        return new Texture(GL_TEXTURE_1D_ARRAY,width,layers,1);
    }

    public static Texture generate2D(int width, int height) {
        return new Texture(GL_TEXTURE_2D,width,height,1);
    }

    public static Texture generate2DArray(int width, int height, int layers) {
        return new Texture(GL_TEXTURE_2D_ARRAY,width,height,layers);
    }

    public static Texture generate3D(int width, int height, int depth) {
        return new Texture(GL_TEXTURE_3D,width,height,depth);
    }

    public static Texture image2D(Image image, boolean mipmap) {
        Texture texture = generate2D(image.width(),image.height());
        texture.allocate(image.format(),mipmap);
        texture.uploadData(image.data());
        return texture;
    }

    public void allocate(TextureFormat format) { allocate(format,false); }

    public void allocate(TextureFormat format, boolean mipmap) {
        if (isDisposed()) throw new IllegalStateException("cannot allocate storage for disposed textures");
        if (isAllocated()) throw new IllegalStateException("texture storage already allocated");
        int levels = mipmap ? calculateMipmapLevels() : 1;
        int i_format = format.sized_format;
        this.format = format;
        switch (target) {
            case GL_TEXTURE_1D -> glTexStorage1D(target,levels,i_format,width);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexStorage2D(target,levels,i_format,width,height);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexStorage3D(target,levels,i_format,width,height,depth);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(ByteBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (isDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!isAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(target,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(ShortBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (isDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!isAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(target,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }

    }

    public void uploadSubData(IntBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (isDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!isAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(target,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }

    }

    public void uploadSubData(FloatBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (isDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!isAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(target,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }

    }

    public void uploadSubData(ByteBuffer data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off, y_off,0);
    }

    public void uploadSubData(ShortBuffer data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off, y_off,0);
    }

    public void uploadSubData(IntBuffer data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off, y_off,0);
    }

    public void uploadSubData(FloatBuffer data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off, y_off,0);
    }

    public void uploadSubData(ByteBuffer data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height, x_off, 0);
    }

    public void uploadSubData(ShortBuffer data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height, x_off, 0);
    }

    public void uploadSubData(IntBuffer data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height, x_off, 0);
    }

    public void uploadSubData(FloatBuffer data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height, x_off, 0);
    }

    public void uploadSubData(ByteBuffer data, int level) {
        uploadSubData(data, level, width, 0);
    }

    public void uploadSubData(ShortBuffer data, int level) {
        uploadSubData(data, level, width, 0);
    }

    public void uploadSubData(IntBuffer data, int level) {
        uploadSubData(data, level, width, 0);
    }

    public void uploadSubData(FloatBuffer data, int level) {
        uploadSubData(data, level, width, 0);
    }

    public void uploadData(ByteBuffer data) {
        uploadSubData(data,0);
    }

    public void uploadData(ShortBuffer data) {
        uploadSubData(data,0);
    }

    public void uploadData(IntBuffer data) {
        uploadSubData(data,0);
    }

    public void uploadData(FloatBuffer data) {
        uploadSubData(data,0);
    }


    public void uploadSubData(byte[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        ByteBuffer buffer = toByteBuffer(data);
        uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off);
        MemoryUtil.memFree(buffer);
    }

    public void uploadSubData(short[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        ShortBuffer buffer = toShortBuffer(data);
        uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off);
        MemoryUtil.memFree(buffer);
    }

    public void uploadSubData(int[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        IntBuffer buffer = toIntBuffer(data);
        uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off);
        MemoryUtil.memFree(buffer);
    }

    public void uploadSubData(float[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        FloatBuffer buffer = toFloatBuffer(data);
        uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off);
        MemoryUtil.memFree(buffer);
    }

    public void uploadSubData(byte[] data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off,y_off,0);
    }

    public void uploadSubData(short[] data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off,y_off,0);
    }

    public void uploadSubData(int[] data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off,y_off,0);
    }

    public void uploadSubData(float[] data, int level, int width, int height, int x_off, int y_off) {
        uploadSubData(data, level, width, height, depth, x_off,y_off,0);
    }

    public void uploadSubData(byte[] data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height,x_off,0);
    }

    public void uploadSubData(short[] data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height,x_off,0);
    }

    public void uploadSubData(int[] data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height,x_off,0);
    }

    public void uploadSubData(float[] data, int level, int width, int x_off) {
        uploadSubData(data, level, width, height,x_off,0);
    }

    public void uploadSubData(byte[] data, int level) {
        uploadSubData(data, level, width,0);
    }

    public void uploadSubData(short[] data, int level) {
        uploadSubData(data, level, width,0);
    }

    public void uploadSubData(int[] data, int level) {
        uploadSubData(data, level, width,0);
    }

    public void uploadSubData(float[] data, int level) {
        uploadSubData(data, level, width,0);
    }

    public void uploadData(byte[] data) {
        uploadSubData(data,0);
    }

    public void uploadData(short[] data) {
        uploadSubData(data,0);
    }

    public void uploadData(int[] data) {
        uploadSubData(data,0);
    }

    public void uploadData(float[] data) {
        uploadSubData(data,0);
    }

    public void generateMipmap() {
        glGenerateMipmap(target);
    }

    public void bindToActiveSlot() {
        glBindTexture(target, id);
    }

    public void bindToSlot(int slot) {
        if (slot != active_slot) {
            glActiveTexture(slot + GL_TEXTURE0);
            active_slot = slot;
        } bindToActiveSlot();
    }

    public void dispose() {
        if (!isDisposed()) {
            glDeleteTextures(id);
            format = null;
            id = -1;
        }
    }

    public boolean isAllocated() {
        return format != null;
    }

    public boolean isDisposed() {
        return id == -1;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }

    public int id() {
        return id;
    }

    public int target() {
        return target;
    }

    public void wrapS(int wrapS) {
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
    }

    public void wrapT(int wrapT) {
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
    }

    public void wrapR(int wrapR) {
        glTexParameteri(target, GL_TEXTURE_WRAP_R, wrapR);
    }

    public void wrapST(int wrapS, int wrapT) {
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
    }

    public void wrapSTR(int wrapS, int wrapT, int wrapR) {
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
        glTexParameteri(target, GL_TEXTURE_WRAP_R, wrapR);
    }

    public void wrapST(int wrap) {
        wrapST(wrap,wrap);
    }

    public void wrapSTR(int wrap) {
        wrapSTR(wrap,wrap,wrap);
    }

    public void repeat() {
        switch (target) {
            case GL_TEXTURE_1D -> wrapS(GL_REPEAT);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> wrapST(GL_REPEAT);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> wrapSTR(GL_REPEAT);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void clampToBorder() {
        switch (target) {
            case GL_TEXTURE_1D -> wrapS(GL_CLAMP_TO_BORDER);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> wrapST(GL_CLAMP_TO_BORDER);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> wrapSTR(GL_CLAMP_TO_BORDER);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void clampToEdge() {
        switch (target) {
            case GL_TEXTURE_1D -> wrapS(GL_CLAMP_TO_EDGE);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> wrapST(GL_CLAMP_TO_EDGE);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> wrapSTR(GL_CLAMP_TO_EDGE);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void nearest() {
        filter(GL_NEAREST,GL_NEAREST);
    }

    public void filter(int min, int mag) {
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag);
    }


    private ByteBuffer toByteBuffer(byte[] array) {
        ByteBuffer buffer = MemoryUtil.memAlloc(array.length);
        return buffer.put(array).flip();
    }

    private ByteBuffer toByteBuffer(byte[][] array) {
        int w = array[0].length;
        int h = array.length;
        ByteBuffer buffer = MemoryUtil.memAlloc(w*h);
        for (int r = 0; r < height; r++) {
            buffer.put(array[r]);
        } return buffer.flip();
    }

    private ShortBuffer toShortBuffer(short[] array) {
        ShortBuffer buffer = MemoryUtil.memAllocShort(array.length);
        return buffer.put(array).flip();
    }

    private ShortBuffer toShortBuffer(short[][] array) {
        int w = array[0].length;
        int h = array.length;
        ShortBuffer buffer = MemoryUtil.memAllocShort(w*h);
        for (int r = 0; r < height; r++) {
            buffer.put(array[r]);
        } return buffer.flip();
    }

    private IntBuffer toIntBuffer(int[] array) {
        IntBuffer buffer = MemoryUtil.memAllocInt(array.length);
        return buffer.put(array).flip();
    }

    private IntBuffer toIntBuffer(int[][] array) {
        int w = array[0].length;
        int h = array.length;
        IntBuffer buffer = MemoryUtil.memAllocInt(w*h);
        for (int r = 0; r < height; r++) {
            buffer.put(array[r]);
        } return buffer.flip();
    }

    private FloatBuffer toFloatBuffer(float[] array) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(array.length);
        return buffer.put(array).flip();
    }

    private FloatBuffer toFloatBuffer(float[][] array) {
        int w = array[0].length;
        int h = array.length;
        FloatBuffer buffer = MemoryUtil.memAllocFloat(w*h);
        for (int r = 0; r < height; r++) {
            buffer.put(array[r]);
        } return buffer.flip();
    }

    private int calculateMipmapLevels() {
        return MathLib.log2(Math.min(width,height)) + 1;
    }
}
