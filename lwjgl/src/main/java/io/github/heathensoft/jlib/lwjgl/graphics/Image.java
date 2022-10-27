package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.File;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Frederik Dahl
 * 20/10/2022
 */


public class Image implements Disposable {
    
    private int width;
    private int height;
    private int components;
    
    private ByteBuffer data;
    
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
        stbi_write_png(path,width,height, components,data,stride);
    }
    
    @Override
    public void dispose() {
        stbi_image_free(data);
        data = null;
    }
}
