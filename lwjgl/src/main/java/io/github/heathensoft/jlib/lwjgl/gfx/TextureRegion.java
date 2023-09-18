package io.github.heathensoft.jlib.lwjgl.gfx;

import java.nio.FloatBuffer;

/**
 *
 * The UV-coordinates of a texture-region are always pixel-centered
 *
 * @author Frederik Dahl
 * 21/03/2023
 */


public class TextureRegion {
    
    private final int texture_width;
    private final int texture_height;
    private int x, y, width, height;
    private float u, v, u2, v2;
    
    public TextureRegion(int texture_size) {
        this(texture_size,texture_size);
    }
    
    public TextureRegion(int texture_width, int texture_height) {
        this(0,0, texture_width, texture_height, texture_width, texture_height);
    }
    
    public TextureRegion(int x, int y, int width, int height, int texture_width, int texture_height) {
        this(x,y,width,height,texture_width,texture_height,false);
    }

    public TextureRegion(int x, int y, int width, int height, int texture_width, int texture_height, boolean pixel_centered_uvs) {
        this.width = width; this.texture_width = texture_width; this.x = x % texture_width;
        this.height = height; this.texture_height = texture_height; this.y = y % texture_height;
        if (pixel_centered_uvs) {
            this.u = (float) ((this.x + 0.5d) / (double) this.texture_width);
            this.v = (float) ((this.y + 0.5d) / (double) this.texture_height);
            this.u2 = (float) ((this.x + width - 0.5d) / (double) this.texture_width);
            this.v2 = (float) ((this.y + height - 0.5d) / (double) this.texture_height);
        } else { this.u = (float) ((this.x) / (double) this.texture_width);
            this.v = (float) ((this.y) / (double) this.texture_height);
            this.u2 = (float) ((this.x + width) / (double) this.texture_width);
            this.v2 = (float) ((this.y + height) / (double) this.texture_height);
        }
    }

    public TextureRegion(TextureRegion region) {
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

    public void pixelCenterUVs() {
        this.u = (float) ((this.x + 0.5d) / (double) this.texture_width);
        this.v = (float) ((this.y + 0.5d) / (double) this.texture_height);
        this.u2 = (float) ((this.x + width - 0.5d) / (double) this.texture_width);
        this.v2 = (float) ((this.y + height - 0.5d) / (double) this.texture_height);
    }

    public TextureRegion[] subDivide(int rows, int cols, int element_width, int element_height, boolean pixel_centered_uvs) {
        TextureRegion[] array = new TextureRegion[rows * cols];
        for (int row = 0; row < rows; row++) {
            int local_y = row * element_height;
            for (int col = 0; col < cols; col++) {
                int local_x = col * element_width;
                array[row * cols + col] = subRegion(
                        local_x,local_y,element_width,element_height,pixel_centered_uvs);
            }
        } return array;
    }
    
    public TextureRegion[] subDivide(int rows, int cols, int element_width, int element_height) {
        return subDivide(rows,cols,element_width,element_height,false);
    }
    
    public TextureRegion[] subDivide(int rows, int cols, int element_size) {
        return subDivide(rows,cols,element_size,element_size);
    }

    public TextureRegion subRegion(int local_x, int local_y, int w, int h, boolean pixel_centered_uvs) {
        return new TextureRegion(x+local_x,y+local_y,w,h, texture_width, texture_height, pixel_centered_uvs);
    }
    
    public TextureRegion subRegion(int local_x, int local_y, int w, int h) {
        return new TextureRegion(x+local_x,y+local_y,w,h, texture_width, texture_height);
    }
    
    public void scroll(int amount_x, int amount_y) {
        setPosition(this.x + amount_x, this.y + amount_y);
    }
    
    public void setPosition(int x, int y) {
        boolean pixel_centered = isPixelCentered();
        this.x = x % texture_width;
        this.y = y % texture_height;
        recalculateUV(pixel_centered);
    }
    
    public void setSize(int w, int h) {
        boolean pixel_centered = isPixelCentered();
        this.width = w;
        this.height = h;
        recalculateUV(pixel_centered);
    }
    
    public void resize(int amount_x, int amount_y) {
        setSize(this.width + amount_x, this.height + amount_y);
    }
    
    public void flipX() {
        float f = u;
        u = u2;
        u2 = f;
    }
    
    public void flipY() {
        float f = v;
        v = v2;
        v2 = f;
    }

    public float u() {
        return u;
    }
    
    public float u2() {
        return u2;
    }
    
    public float v() {
        return v;
    }
    
    public float v2() {
        return v2;
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }
    
    public int w() {
        return width;
    }
    
    public int h() {
        return height;
    }
    
    public int textureWidth() {
        return texture_width;
    }
    
    public int textureHeight() {
        return texture_height;
    }
    
    public void getUVs(FloatBuffer buffer) {
        buffer.put(u).put(v).put(u2).put(v2);
    }

    public boolean isPixelCentered() {
        return !(u == (this.x / (double) texture_width));
    }
    
    public boolean isFlippedX() {
        return u > u2;
    }
    
    public boolean isFlippedY() {
        return v > v2;
    }

    public TextureRegion cpy() {
        return new TextureRegion(this);
    }
    
    private void recalculateUV(boolean pixel_centered) {
        boolean was_flipped_x = isFlippedX();
        boolean was_flipped_y = isFlippedY();
        if (pixel_centered) {
            u = (float) ((x + 0.5d) / (double) texture_width);
            v = (float) ((y + 0.5d) / (double) texture_height);
            u2 = (float) ((x + width - 0.5d) / (double) texture_width);
            v2 = (float) ((y + height - 0.5d) / (double) texture_height);
        } else { u = (float) ((x) / (double) texture_width);
            v = (float) ((y) / (double) texture_height);
            u2 = (float) ((x + width) / (double) texture_width);
            v2 = (float) ((y + height) / (double) texture_height);
        } if (was_flipped_x) flipX();
        if (was_flipped_y) flipY();
    }
    
}
