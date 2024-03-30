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
    
    protected int tw;
    protected int th;
    protected int x, y, w, h;
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
        this.th = texture_height;
        this.tw = texture_width;
        this.h = Math.max(1,height);
        this.w = Math.max(1,width);
        this.y = y % texture_height;
        this.x = x % texture_width;
        if (pixel_centered) {
            this.u = (float) ((this.x + 0.5d) / (double) this.tw);
            this.v = (float) ((this.y + 0.5d) / (double) this.th);
            this.u2 = (float) ((this.x + width - 0.5d) / (double) this.tw);
            this.v2 = (float) ((this.y + height - 0.5d) / (double) this.th);
        } else {
            this.u = (float) ((this.x) / (double) this.tw);
            this.v = (float) ((this.y) / (double) this.th);
            this.u2 = (float) ((this.x + width) / (double) this.tw);
            this.v2 = (float) ((this.y + height) / (double) this.th);
        } this.pixel_centered = pixel_centered;
    }

    public TextureRegion(TextureRegion region) {
        this.pixel_centered = region.pixel_centered;
        this.th = region.th;
        this.tw = region.tw;
        this.h = region.h;
        this.w = region.w;
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
    public int w() { return w; }
    public int h() { return h; }
    public int textureWidth() { return tw; }
    public int textureHeight() { return th; }
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
        return new TextureRegion(x+local_x,y+local_y,w,h, tw, th, pixel_centered);
    }

    public TextureRegion subRegion(int local_x, int local_y, int w, int h) {
        return new TextureRegion(x+local_x,y+local_y,w,h, tw, th);
    }

    public void scroll(int dx, int dy) { setPosition(this.x + dx, this.y + dy); }

    public void set(int tw, int th, int x, int y, int w, int h) {
        this.th = Math.max(1,h);
        this.tw = Math.max(1,w);
        set(x, y, w, h);
    }

    public void set(int x, int y, int w, int h) {
        this.y = y % th;
        this.x = x % tw;
        this.h = Math.max(1,h);
        this.w = Math.max(1,w);
        recalculateUV();
    }

    public void setPosition(int x, int y) {
        this.y = y % th;
        this.x = x % tw;
        recalculateUV();
    }

    public void setSize(int w, int h) {
        this.h = Math.max(1,h);
        this.w = Math.max(1,w);
        recalculateUV();
    }

    public void setTextureSize(int w, int h) {
        this.th = Math.max(1,h);
        this.tw = Math.max(1,w);
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
            this.u = (float) ((this.x + 0.5d) / (double) this.tw);
            this.v = (float) ((this.y + 0.5d) / (double) this.th);
            this.u2 = (float) ((this.x + w - 0.5d) / (double) this.tw);
            this.v2 = (float) ((this.y + h - 0.5d) / (double) this.th);
        } else if (pixel_centered &! on) {
            this.u = (float) ((this.x) / (double) this.tw);
            this.v = (float) ((this.y) / (double) this.th);
            this.u2 = (float) ((this.x + w) / (double) this.tw);
            this.v2 = (float) ((this.y + h) / (double) this.th);
        }
    }

    private void recalculateUV() {
        boolean was_flipped_x = isFlippedHorizontally();
        boolean was_flipped_y = isFlippedVertically();
        if (pixel_centered) {
            u = (float) ((x + 0.5d) / (double) tw);
            v = (float) ((y + 0.5d) / (double) th);
            u2 = (float) ((x + w - 0.5d) / (double) tw);
            v2 = (float) ((y + h - 0.5d) / (double) th);
        } else {
            u = (float) ((x) / (double) tw);
            v = (float) ((y) / (double) th);
            u2 = (float) ((x + w) / (double) tw);
            v2 = (float) ((y + h) / (double) th);
        } if (was_flipped_x) flipHorizontally();
        if (was_flipped_y) flipVertically();
    }
    
}
