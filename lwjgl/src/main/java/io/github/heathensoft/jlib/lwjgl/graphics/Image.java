package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.File;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * @author Frederik Dahl
 * 20/10/2022
 */


public class Image implements Disposable {
    
    private final int width;
    private final int height;
    private final int components;
    
    private ByteBuffer data;
    
    public Image(ByteBuffer buffer) throws Exception {
        this(buffer,false);
    }
    
    public Image(ByteBuffer buffer, boolean flip) throws Exception {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            if (!stbi_info_from_memory(buffer, w, h, c)) {
                throw new Exception("unable to read image info: " + stbi_failure_reason());
            } stbi_set_flip_vertically_on_load(flip);
            data = stbi_load_from_memory(buffer, w, h, c, 0);
            if (data == null) {
                throw new Exception("unable to load image: " + stbi_failure_reason());
            } width = w.get(0);
            height = h.get(0);
            components = c.get(0);
        }
    }
    
    public void premultiplyAlpha() {
        if (notDisposed()) {
            final ByteBuffer b = data;
            final int w = width;
            final int h = height;
            final int c = components;
            if (c == 4) {
                int stride = w * 4;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int i = y * stride + x * 4;
                        float alpha = (b.get(i + 3) & 0xFF) / 255.0f;
                        b.put(i, (byte) Math.round(((b.get(i) & 0xFF) * alpha)));
                        b.put(i + 1, (byte)Math.round(((b.get(i + 1) & 0xFF) * alpha)));
                        b.put(i + 2, (byte)Math.round(((b.get(i + 2) & 0xFF) * alpha)));
                    }
                }
            }
        }
    }
    
    public void flattenNormalmap(String path, float maxAngleDeg) {
        if (notDisposed()) {
            final int w = width;
            final int h = height;
            final int c = components;
            if (c >= 3) {
                final float n = 1 / 255f;
                maxAngleDeg = Math.max(0,Math.min(90, maxAngleDeg));
                final float maxAngle = Math.toRadians(maxAngleDeg);
                Vector3f flatSurface = new Vector3f(0,0,1);
                int stride = w * c;
                for (int row = 0; row < h; row++) {
                    for (int col = 0; col < w; col++) {
                        int i = row * stride + col * c;
                        int r = (data.get(i) & 0xFF);
                        int g = (data.get(i + 1) & 0xFF);
                        int b = (data.get(i + 2) & 0xFF);
                        Vector3f color = MathLib.vec3();
                        color.set(r * n,g * n,b * n);
                        Vector3f normal = MathLib.vec3();
                        normal.x = (color.x - 0.5f) * 2f;
                        normal.y = (color.y - 0.5f) * 2f;
                        normal.z = (color.z - 0.5f) * 2f;
                        float angle = flatSurface.angle(normal);
                        if (angle > maxAngle) {
                            float rotation = maxAngle - angle;
                            Vector3f cross = MathLib.vec3();
                            flatSurface.cross(normal,cross);
                            normal.rotateAxis(rotation,cross.x,cross.y,cross.z);
                            normal.normalize();
                            color.x = normal.x * 0.5f + 0.5f;
                            color.y = normal.y * 0.5f + 0.5f;
                            color.z = normal.z * 0.5f + 0.5f;
                            r = Math.round(color.x * 255);
                            g = Math.round(color.y * 255);
                            b = Math.round(color.z * 255);
                            data.put(i, (byte) r);
                            data.put(i + 1, (byte) g);
                            data.put(i + 2, (byte) b);
                        }
                    }
                }
                toPNG(path);
            }
        }
        
    }
    
    public boolean notDisposed() {
        return data != null;
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public int components() {
        return components;
    }
    
    public ByteBuffer data() {
        return data;
    }
    
    public void toPNG(File file) {
        toPNG(file.path().toString());
    }
    
    public void toPNG(String path) {
        final int stride = width * components;
        if (!stbi_write_png(path,width,height, components,data,stride))
            Logger.warn("unable to write image to png: " + stbi_failure_reason());
    }
    
    @Override
    public void dispose() {
        stbi_image_free(data);
        data = null;
    }
}
