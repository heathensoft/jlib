package io.github.heathensoft.jlib.lwjgl.gfx;

import org.joml.Vector4f;

import java.nio.FloatBuffer;

/**
 *
 * Texels have x,y = 0,0 in the top-left corner of the Texture
 * Width and Height is never < 1. (Clamped to 1) One pixel
 *
 * @author Frederik Dahl
 * 21/03/2023
 */


public class TextureRegion {
    
    protected final int texture_width;
    protected final int texture_height;
    protected int x, y, width, height;
    protected float u, v, u2, v2;
    protected boolean pixel_centered;
    
    public TextureRegion(int texture_size) {
        this(texture_size,texture_size);
    }
    
    public TextureRegion(int texture_width, int texture_height) {
        this(0,0, texture_width, texture_height, texture_width, texture_height);
    }
    
    public TextureRegion(int x, int y, int width, int height, int texture_width, int texture_height) {
        this(x,y,width,height,texture_width,texture_height,false);
    }

    public TextureRegion(int x, int y, int width, int height, int texture_width, int texture_height, boolean pixel_centered) {
        if (texture_width <= 0 || texture_height <= 0) throw new RuntimeException("Invalid TextureRegion Size");
        this.texture_height = texture_height;
        this.texture_width = texture_width;
        this.height = Math.max(1,height);
        this.width = Math.max(1,width);
        this.y = y % texture_height;
        this.x = x % texture_width;
        if (pixel_centered) {
            this.u = (float) ((this.x + 0.5d) / (double) this.texture_width);
            this.v = (float) ((this.y + 0.5d) / (double) this.texture_height);
            this.u2 = (float) ((this.x + width - 0.5d) / (double) this.texture_width);
            this.v2 = (float) ((this.y + height - 0.5d) / (double) this.texture_height);
        } else {
            this.u = (float) ((this.x) / (double) this.texture_width);
            this.v = (float) ((this.y) / (double) this.texture_height);
            this.u2 = (float) ((this.x + width) / (double) this.texture_width);
            this.v2 = (float) ((this.y + height) / (double) this.texture_height);
        } this.pixel_centered = pixel_centered;
    }

    public TextureRegion(TextureRegion region) {
        this.pixel_centered = region.pixel_centered;
        this.texture_height = region.texture_height;
        this.texture_width = region.texture_width;
        this.height = region.height;
        this.width = region.width;
        this.x = region.x;
        this.y = region.y;
        this.u = region.u;
        this.v = region.v;
        this.u2 = region.u2;
        this.v2 = region.v2;
    }

    public float u() { return u; }
    public float v() { return v; }
    public float u2() { return u2; }
    public float v2() { return v2; }
    public int x() { return x; }
    public int y() { return y; }
    public int w() { return width; }
    public int h() { return height; }
    public int textureWidth() { return texture_width; }
    public int textureHeight() { return texture_height; }
    public boolean isPixelCentered() { return pixel_centered; }
    public boolean isFlippedHorizontally() { return u > u2; }
    public boolean isFlippedVertically() { return v > v2; }
    public TextureRegion copy() { return new TextureRegion(this); }
    public FloatBuffer getUVs(FloatBuffer dst) { return dst.put(u).put(v).put(u2).put(v2); }
    public Vector4f getUVs(Vector4f dst) { return dst.set(u,v,u2,v2); }


    public TextureRegion[] subDivide(int rows, int cols, int reg_width, int reg_height, boolean pixel_centered) {
        TextureRegion[] array = new TextureRegion[rows * cols];
        for (int row = 0; row < rows; row++) {
            int local_y = row * reg_height;
            for (int col = 0; col < cols; col++) {
                int local_x = col * reg_width;
                array[row * cols + col] = subRegion(
                        local_x,local_y,reg_width,reg_height,pixel_centered);
            }
        } return array;
    }

    public TextureRegion[] subDivide(int rows, int cols, int reg_width, int reg_height) {
        return subDivide(rows,cols,reg_width,reg_height,false);
    }

    public TextureRegion[] subDivide(int rows, int cols, int reg_size) {
        return subDivide(rows,cols,reg_size,reg_size);
    }

    public TextureRegion subRegion(int local_x, int local_y, int w, int h, boolean pixel_centered) {
        return new TextureRegion(x+local_x,y+local_y,w,h, texture_width, texture_height, pixel_centered);
    }

    public TextureRegion subRegion(int local_x, int local_y, int w, int h) {
        return new TextureRegion(x+local_x,y+local_y,w,h, texture_width, texture_height);
    }

    public void scroll(int dx, int dy) { setPosition(this.x + dx, this.y + dy); }

    public void set(int x, int y, int w, int h) {
        this.y = y % texture_height;
        this.x = x % texture_width;
        this.height = Math.max(1,h);
        this.width = Math.max(1,w);
        recalculateUV();
    }

    public void setPosition(int x, int y) {
        this.x = x % texture_width;
        this.y = y % texture_height;
        recalculateUV();
    }

    public void setSize(int w, int h) {
        this.height = Math.max(1,h);
        this.width = Math.max(1,w);
        recalculateUV();
    }

    public void flipHorizontally() {
        float f = u;
        u = u2;
        u2 = f;
    }

    public void flipVertically() {
        float f = v;
        v = v2;
        v2 = f;
    }

    public void togglePixelCentered(boolean on) {
        if (on &! pixel_centered) {
            this.u = (float) ((this.x + 0.5d) / (double) this.texture_width);
            this.v = (float) ((this.y + 0.5d) / (double) this.texture_height);
            this.u2 = (float) ((this.x + width - 0.5d) / (double) this.texture_width);
            this.v2 = (float) ((this.y + height - 0.5d) / (double) this.texture_height);
        } else if (pixel_centered &! on) {
            this.u = (float) ((this.x) / (double) this.texture_width);
            this.v = (float) ((this.y) / (double) this.texture_height);
            this.u2 = (float) ((this.x + width) / (double) this.texture_width);
            this.v2 = (float) ((this.y + height) / (double) this.texture_height);
        }
    }

    private void recalculateUV() {
        boolean was_flipped_x = isFlippedHorizontally();
        boolean was_flipped_y = isFlippedVertically();
        if (pixel_centered) {
            u = (float) ((x + 0.5d) / (double) texture_width);
            v = (float) ((y + 0.5d) / (double) texture_height);
            u2 = (float) ((x + width - 0.5d) / (double) texture_width);
            v2 = (float) ((y + height - 0.5d) / (double) texture_height);
        } else {
            u = (float) ((x) / (double) texture_width);
            v = (float) ((y) / (double) texture_height);
            u2 = (float) ((x + width) / (double) texture_width);
            v2 = (float) ((y + height) / (double) texture_height);
        } if (was_flipped_x) flipHorizontally();
        if (was_flipped_y) flipVertically();
    }
    
}
