package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.GLContext;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.*;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.glTextureView;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;

/**
 *
 * @see <a href="https://www.khronos.org/opengl/wiki/Texture">Texture</a>
 * @see <a href="https://www.khronos.org/opengl/wiki/Pixel_Transfer">Pixel Transfer Operations</a>
 * @see <a href="https://stackoverflow.com/questions/8866904/differences-and-relationship-between-glactivetexture-and-glbindtexture">Bind/Actives</a>
 *
 * Generate -> allocate -> upload -> generate mipmap
 *
 * On Packing / unpacking:
 * Pixel transfers can either go from user memory to OpenGL memory,
 * or from OpenGL memory to user memory (the user memory can be client memory or buffer objects).
 * Pixel data in user memory is said to be packed.
 * Therefore, transfers to OpenGL memory are called unpack operations,
 * and transfers from OpenGL memory are called pack operations.
 *
 * Valid targets:
 *
 *  0 - GL_TEXTURE_1D,
 *  1 - GL_TEXTURE_2D,
 *  2 - GL_TEXTURE_3D,
 *  3 - GL_TEXTURE_1D_ARRAY
 *  4 - GL_TEXTURE_2D_ARRAY
 *  5 - GL_TEXTURE_CUBE_MAP
 *
 * @author Frederik Dahl
 * 12/01/2023
 */


public class Texture implements Disposable {

    private static final int NO_SLOT = -1;
    private static final int NUM_SLOTS = 32;
    private static final int UNIQUE_TARGETS = 6; // Add more later
    private static final int[][] SLOTS = new int[NUM_SLOTS][UNIQUE_TARGETS];
    private static int active_slot;

    private TextureFormat format;
    private boolean allocated;
    private int id, mip_levels;
    private final int target;
    private final int width, height, depth;

    private Texture(int target, int width, int height, int depth) {
        this.id = glGenTextures();
        this.target = target;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public static Texture generate1D(int width) { return new Texture(GL_TEXTURE_1D,width,1,1); }
    public static Texture generate1DArray(int width, int layers) { return new Texture(GL_TEXTURE_1D_ARRAY,width,layers,1); }
    public static Texture generate2D(int width, int height) { return new Texture(GL_TEXTURE_2D,width,height,1); }
    public static Texture generate2D(int size) { return generate2D(size,size); }
    public static Texture generate2DArray(int width, int height, int layers) { return new Texture(GL_TEXTURE_2D_ARRAY,width,height,layers); }
    public static Texture generate2DArray(int size, int layers) { return generate2DArray(size,size,layers); }
    public static Texture generate3D(int width, int height, int depth) { return new Texture(GL_TEXTURE_3D,width,height,depth); }

    public void bindToSlot(int slot) { bindToSlot(slot,target, id); }
    public void bindToActiveSlot() { bindToActiveSlot(target, id); }
    public int bindTooAnySlot() { return bindToAny(target, id); }
    public int id() { return id; }
    public int width() { return width; }
    public int height() { return height; }
    public int depth() { return depth; }
    public int target() { return target; }
    public int mipLevels() { return mip_levels; }
    public boolean hasBeenDisposed() { return id == -1; }
    public boolean hasBeenAllocated() { return allocated; }
    public TextureFormat format() { return format; }
    public void dispose() {
        if (!hasBeenDisposed()) {
            removeFromSlots(target, id);
            glDeleteTextures(id);
            allocated = false;
            id = -1;
        }
    }

    public void texParameteri(int pname, int param) { glTexParameteri(target, pname, param); }
    public void texParameterf(int pname, float param) { glTexParameterf(target, pname, param); }
    public void filterNearest() { textureFilter(GL_NEAREST,GL_NEAREST); }
    public void filterLinear() { textureFilter(GL_LINEAR,GL_LINEAR); }
    public void textureFilter(int min, int mag) {
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag);
    }
    public void textureRepeat() { textureWrap(GL_REPEAT); }
    public void clampToBorder() { textureWrap(GL_CLAMP_TO_BORDER); }
    public void clampToEdge() { textureWrap(GL_CLAMP_TO_EDGE); }
    public void textureWrap(int param) {
        switch (target) {
            case GL_TEXTURE_1D -> texParameteri(GL_TEXTURE_WRAP_S, param);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> {
                texParameteri(GL_TEXTURE_WRAP_S, param);
                texParameteri(GL_TEXTURE_WRAP_T, param);
            } case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> {
                texParameteri(GL_TEXTURE_WRAP_S, param);
                texParameteri(GL_TEXTURE_WRAP_T, param);
                texParameteri(GL_TEXTURE_WRAP_R, param);
            } default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public Bitmap bitmap() { return bitmap(0); }
    public Bitmap bitmap(int level) {
        if (target == GL_TEXTURE_1D || target == GL_TEXTURE_2D) {
            level = Math.min(calculateMipmapLevels() - 1,Math.max(0,level));
            int w = this.width / (int)(java.lang.Math.pow(2,level));
            int h = this.height / (int)(java.lang.Math.pow(2,level));
            int c = this.format.channels;
            ByteBuffer pixels = MemoryUtil.memAlloc(w*h*c);
            downloadData(pixels,level); return new Bitmap(pixels,w,h,c);
        } throw new RuntimeException("texture must be 1D or 2D to create Bitmap");
    }

    public Texture viewOfLayer(int layer) {
        if (allocated &! hasBeenDisposed()) {
            Texture texture;
            switch (target) {
                case GL_TEXTURE_1D -> throw new RuntimeException("Unsupported view of 1D texture");
                case GL_TEXTURE_2D -> texture = generate1D(width);
                case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> texture = generate2D(width,height);
                default -> throw new RuntimeException("Unsupported format for texture view");
            } texture.format = format;
            texture.mip_levels = mip_levels;
            texture.allocated = true;
            glTextureView(texture.id,texture.target, id, format.sized_format, 0, mip_levels, layer, layer);
            return texture;
        } throw new RuntimeException("texture storage not allocated");
    }

    public Vector2i mipmapSize(Vector2i dst, int level) {
        int max = U.log2(Math.max(width,height));
        level = U.clamp(level,0,max);
        dst.x = Math.max(width / (int) U.pow(2,level),1);
        dst.y = Math.max(height / (int) U.pow(2,level),1);
        return dst;
    }
    public Vector3i mipmapSize(Vector3i dst, int level) {
        int max = U.log2(Math.max(Math.max(width,height),depth));
        level = U.clamp(level,0,max);
        dst.x = Math.max(width / (int) U.pow(2,level),1);
        dst.y = Math.max(height / (int) U.pow(2,level),1);
        dst.z = Math.max(depth / (int) U.pow(2,level),1);
        return dst;
    }

    public void generateMipmap() {
        if (mip_levels > 1) glGenerateMipmap(target);
        else Logger.warn("attempted to generate mipmaps, but storage not allocated");
    }




    public void allocate(TextureFormat format) { allocate(format,false); }
    public void allocate(TextureFormat format, boolean mipmap) {
        if (hasBeenDisposed()) throw new IllegalStateException("cannot allocate storage for disposed textures");
        if (hasBeenAllocated()) throw new IllegalStateException("texture storage already allocated");
        int i_format = format.sized_format;
        this.mip_levels = mipmap ? calculateMipmapLevels() : 1;
        this.format = format;
        this.allocated = true;
        switch (target) {
            case GL_TEXTURE_1D -> glTexStorage1D(target,mip_levels,i_format,width);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexStorage2D(target,mip_levels,i_format,width,height);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexStorage3D(target,mip_levels,i_format,width,height,depth);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(ByteBuffer data) { uploadSubData(data,0); }
    public void uploadSubData(ByteBuffer data, int level) { uploadSubData(data, level, width, 0); }
    public void uploadSubData(ByteBuffer data, int level, int width, int x_off) { uploadSubData(data, level, width, height, x_off, 0); }
    public void uploadSubData(ByteBuffer data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off, y_off,0); }
    public void uploadSubData(ByteBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (hasBeenDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!hasBeenAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(GL_UNPACK_ALIGNMENT,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(ShortBuffer data) { uploadSubData(data,0); }
    public void uploadSubData(ShortBuffer data, int level) { uploadSubData(data, level, width, 0); }
    public void uploadSubData(ShortBuffer data, int level, int width, int x_off) { uploadSubData(data, level, width, height, x_off, 0); }
    public void uploadSubData(ShortBuffer data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off, y_off,0); }
    public void uploadSubData(ShortBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (hasBeenDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!hasBeenAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(GL_UNPACK_ALIGNMENT,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(IntBuffer data) { uploadSubData(data,0); }
    public void uploadSubData(IntBuffer data, int level) { uploadSubData(data, level, width, 0); }
    public void uploadSubData(IntBuffer data, int level, int width, int x_off) { uploadSubData(data, level, width, height, x_off, 0); }
    public void uploadSubData(IntBuffer data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off, y_off,0); }
    public void uploadSubData(IntBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (hasBeenDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!hasBeenAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(GL_UNPACK_ALIGNMENT,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(FloatBuffer data) { uploadSubData(data,0); }
    public void uploadSubData(FloatBuffer data, int level) { uploadSubData(data, level, width, 0); }
    public void uploadSubData(FloatBuffer data, int level, int width, int x_off) { uploadSubData(data, level, width, height, x_off, 0); }
    public void uploadSubData(FloatBuffer data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off, y_off,0); }
    public void uploadSubData(FloatBuffer data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        if (hasBeenDisposed()) throw new IllegalStateException("cannot transfer data to disposed textures");
        if (!hasBeenAllocated()) throw new IllegalStateException("texture storage not allocated");
        glPixelStorei(GL_UNPACK_ALIGNMENT,format.pack_alignment);
        int transfer_format = format.pixel_format;
        int data_type = format.pixel_data_type;
        switch (target) {
            case GL_TEXTURE_1D -> glTexSubImage1D(target,level,x_off,width,transfer_format,data_type,data);
            case GL_TEXTURE_2D, GL_TEXTURE_1D_ARRAY -> glTexSubImage2D(target,level,x_off,y_off,width,height,transfer_format,data_type,data);
            case GL_TEXTURE_3D, GL_TEXTURE_2D_ARRAY -> glTexSubImage3D(target,level,x_off,y_off,z_off,width,height,depth,transfer_format,data_type,data);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        }
    }

    public void uploadSubData(byte[] data) { uploadSubData(data,0); }
    public void uploadSubData(byte[] data, int level) { uploadSubData(data, level, width,0); }
    public void uploadSubData(byte[] data, int level, int width, int x_off) { uploadSubData(data, level, width, height,x_off,0); }
    public void uploadSubData(byte[] data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off,y_off,0); }
    public void uploadSubData(byte[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        ByteBuffer buffer = toByteBuffer(data); uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off); MemoryUtil.memFree(buffer);
    }
    public void uploadSubData(short[] data) { uploadSubData(data,0); }
    public void uploadSubData(short[] data, int level) { uploadSubData(data, level, width,0); }
    public void uploadSubData(short[] data, int level, int width, int x_off) { uploadSubData(data, level, width, height,x_off,0); }
    public void uploadSubData(short[] data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off,y_off,0); }
    public void uploadSubData(short[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        ShortBuffer buffer = toShortBuffer(data); uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off); MemoryUtil.memFree(buffer);
    }
    public void uploadSubData(int[] data) { uploadSubData(data,0); }
    public void uploadSubData(int[] data, int level) { uploadSubData(data, level, width,0); }
    public void uploadSubData(int[] data, int level, int width, int x_off) { uploadSubData(data, level, width, height,x_off,0); }
    public void uploadSubData(int[] data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off,y_off,0); }
    public void uploadSubData(int[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        IntBuffer buffer = toIntBuffer(data); uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off); MemoryUtil.memFree(buffer);
    }
    public void uploadSubData(float[] data) { uploadSubData(data,0); }
    public void uploadSubData(float[] data, int level) { uploadSubData(data, level, width,0); }
    public void uploadSubData(float[] data, int level, int width, int x_off) { uploadSubData(data, level, width, height,x_off,0); }
    public void uploadSubData(float[] data, int level, int width, int height, int x_off, int y_off) { uploadSubData(data, level, width, height, depth, x_off,y_off,0); }
    public void uploadSubData(float[] data, int level, int width, int height, int depth, int x_off, int y_off, int z_off) {
        FloatBuffer buffer = toFloatBuffer(data); uploadSubData(buffer,level,width,height,depth,x_off,y_off,z_off); MemoryUtil.memFree(buffer);
    }

    public void downloadData(ByteBuffer pixels) { downloadData(pixels,0); }
    public void downloadData(ByteBuffer pixels, int level) {
        glPixelStorei(GL_PACK_ALIGNMENT,format.pack_alignment);
        glGetTexImage(target,level,format.pixel_format,format.pixel_data_type,pixels);
    }
    public void downloadData(ShortBuffer pixels) { downloadData(pixels,0); }
    public void downloadData(ShortBuffer pixels, int level) {
        glPixelStorei(GL_PACK_ALIGNMENT,format.pack_alignment);
        glGetTexImage(target,level,format.pixel_format,format.pixel_data_type,pixels);
    }
    public void downloadData(IntBuffer pixels) { downloadData(pixels,0); }
    public void downloadData(IntBuffer pixels, int level) {
        glPixelStorei(GL_PACK_ALIGNMENT,format.pack_alignment);
        glGetTexImage(target,level,format.pixel_format,format.pixel_data_type,pixels);
    }
    public void downloadData(FloatBuffer pixels) { downloadData(pixels,0); }
    public void downloadData(FloatBuffer pixels, int level) {
        glPixelStorei(GL_PACK_ALIGNMENT,format.pack_alignment);
        glGetTexImage(target,level,format.pixel_format,format.pixel_data_type,pixels);
    }












    public static String glWrapEnumToString(int glEnum) {
        return switch (glEnum) {
            case GL_REPEAT -> "GL_REPEAT";
            case GL_MIRRORED_REPEAT -> "GL_MIRRORED_REPEAT";
            case GL_CLAMP_TO_BORDER -> "GL_CLAMP_TO_BORDER";
            case GL_CLAMP_TO_EDGE -> "GL_CLAMP_TO_EDGE";
            default -> "INVALID_ENUM";
        };
    }

    public static String glFilterEnumToString(int glEnum) {
        return switch (glEnum) {
            case GL_LINEAR -> "GL_LINEAR";
            case GL_NEAREST -> "GL_NEAREST";
            case GL_NEAREST_MIPMAP_NEAREST -> "GL_NEAREST_MIPMAP_NEAREST";
            case GL_NEAREST_MIPMAP_LINEAR -> "GL_NEAREST_MIPMAP_LINEAR";
            case GL_LINEAR_MIPMAP_NEAREST -> "GL_LINEAR_MIPMAP_NEAREST";
            case GL_LINEAR_MIPMAP_LINEAR -> "GL_LINEAR_MIPMAP_LINEAR";
            default -> "INVALID_ENUM";
        };
    }

    public static boolean glWrapEnumIsValid(int glEnum) {
        return glEnum == GL_REPEAT ||
                glEnum == GL_MIRRORED_REPEAT ||
                glEnum == GL_CLAMP_TO_EDGE ||
                glEnum == GL_CLAMP_TO_BORDER;
    }

    public static boolean glFilterEnumIsValid(int glEnum) {
        return glEnum == GL_LINEAR ||
                glEnum == GL_NEAREST ||
                glEnum == GL_NEAREST_MIPMAP_NEAREST ||
                glEnum == GL_NEAREST_MIPMAP_LINEAR ||
                glEnum == GL_LINEAR_MIPMAP_NEAREST ||
                glEnum == GL_LINEAR_MIPMAP_LINEAR;
    }


    private static int targetIndex(int target) {
        switch (target) {
            case GL_TEXTURE_2D -> { return 1; }
            case GL_TEXTURE_1D -> { return 0; }
            case GL_TEXTURE_3D -> { return 2; }
            case GL_TEXTURE_2D_ARRAY -> { return 4; }
            case GL_TEXTURE_1D_ARRAY -> { return 3; }
            case GL_TEXTURE_CUBE_MAP -> { return 5; }
            default -> throw new IllegalStateException("Unsupported texture target: " + target);
        }
    }

    private static void bindToSlot(int slot, int target, int texture) {
        if (slot != active_slot) {
            glActiveTexture(slot + GL_TEXTURE0);
            active_slot = slot;
        } bindToActiveSlot(target,texture);
    }


    private static void bindToActiveSlot(int target, int texture) {
        int target_index = targetIndex(target);
        if (!(SLOTS[active_slot][target_index] == texture)) {
            SLOTS[active_slot][target_index] = texture;
            glBindTexture(target,texture);
        }
    }

    private static int bindToAny(int target, int texture) {
        // Using this will take advantage of a wider array of slots
        // instead of keeping rotating the first indices.
        int slot = findSlotOrAvailable(target,texture);
        if (slot == active_slot) return slot;
        if (slot != NO_SLOT) {
            glActiveTexture(slot + GL_TEXTURE0);
            active_slot = slot;
        } bindToActiveSlot(target,texture);
        return active_slot;
    }

    private static int findSlotOrAvailable(int target, int texture) {
        // First check if bound, if not find available if possible
        final int target_index = targetIndex(target);
        int available = NO_SLOT;
        for (int slot = 0; slot < NUM_SLOTS; slot++) {
            final int texture_in_slot = SLOTS[slot][target_index];
            if (texture_in_slot == texture) return slot;
            if (texture_in_slot == GL_NONE && available == NO_SLOT)
                available = slot;
        } return available;
    }

    private static int findSlotIfBound(int target, int texture) {
        int target_index = targetIndex(target);
        for (int slot = 0; slot < NUM_SLOTS; slot++) {
            if (SLOTS[slot][target_index] == texture) return slot;
        } return NO_SLOT;
    }

    private static void removeFromSlots(int target, int texture) {
        int target_index = targetIndex(target);
        for (int slot = 0; slot < NUM_SLOTS; slot++) {
            if (SLOTS[slot][target_index] == texture) {
                SLOTS[slot][target_index] = 0;
            }
        }
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
        return U.log2(Math.max(Math.max(width,height),depth)) + 1;
    }











    // various (Testing, Utility ETC)

    public void generateMipmapCustom() throws Exception {
        if (mip_levels > 1) {
            if (target == GL_TEXTURE_2D) {
                int mipmap_levels = calculateMipmapLevels();
                if (mipmap_levels > 1) {
                    bindToSlot(0);
                    Framebuffer framebuffer = new Framebuffer(width,height);
                    Framebuffer.bind(framebuffer);
                    Framebuffer.attachColor(this,0,false);
                    Framebuffer.drawBuffer(0);
                    Framebuffer.checkStatus();
                    int prev_min_filter = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER);
                    int prev_mag_filter = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER);
                    int prev_tex_wrap_u = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S);
                    int prev_tex_wrap_v = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T);
                    clampToEdge();
                    filterLinear();
                    glDisable(GL_DEPTH_TEST);
                    glDisable(GL_BLEND);
                    ShaderProgram.bindProgram(ShaderProgram.commonPrograms().mipmap_gen_program);
                    ShaderProgram.setUniform(ShaderProgram.UNIFORM_SAMPLER_2D,0);
                    for (int sample_level = 0; sample_level < mipmap_levels - 1; sample_level++) {
                        int target_level = sample_level + 1;
                        ShaderProgram.setUniform(ShaderProgram.UNIFORM_LOD,sample_level);
                        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, sample_level);
                        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, sample_level);
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, id, target_level);
                        Framebuffer.viewport(target_level);
                        if (sample_level == 0) ShaderProgram.shaderPass().draw();
                        else ShaderProgram.shaderPass().drawRepeat();
                    }
                    framebuffer.dispose();
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1000);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, prev_min_filter);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, prev_mag_filter);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, prev_tex_wrap_u);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, prev_tex_wrap_v);
                } else Logger.warn("cannot generate mipmap, single level");
            } else Logger.warn("cannot generate custom mipmap for target != TEXTURE_2D");
        }  else Logger.warn("attempted to generate mipmap, but storage not allocated");
    }

    public ByteBuffer toPNG(int depth_layer, int mip_layer) {

        return null;
    }



   

    public static Texture generateTilemapBlendTexture(int size, boolean allocate_mipmap) throws Exception {
        Texture blend_map = generate2D(size);
        blend_map.bindToActiveSlot();
        blend_map.textureWrap(GL_REPEAT);
        blend_map.textureFilter(GL_LINEAR,GL_LINEAR);
        blend_map.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED,allocate_mipmap);
        GLContext.checkError();
        Framebuffer framebuffer = new Framebuffer(size,size);
        Framebuffer.bind(framebuffer);
        Framebuffer.attachColor(blend_map,0,false);
        Framebuffer.checkStatus();
        Framebuffer.viewport();
        String name = "gfx_tile_blendmap_program";
        Optional<ShaderProgram> optionalProgram = ShaderProgram.optionalProgramByName(name);
        ShaderProgram program = optionalProgram.orElseGet(() -> {
            try { String vSource = Resources.asString("res/jlib/lwjgl/glsl/gfx_tile_blendmap.vert");
                String fSource = Resources.asString("res/jlib/lwjgl/glsl/gfx_tile_blendmap.frag");
                return new ShaderProgram(name,vSource, fSource);
            } catch (Exception e) {
                Logger.error(e);
                return null;
            }
        });
        if (program == null) throw new Exception("Unable to create Texture");
        ShaderProgram.bindProgram(program);
        ShaderProgram.shaderPass().draw();
        Disposable.dispose(framebuffer);
        return blend_map;
    }



}
