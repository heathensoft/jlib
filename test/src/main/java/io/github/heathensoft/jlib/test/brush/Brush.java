package io.github.heathensoft.jlib.test.brush;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.test.graphicsOld.Texture;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Frederik Dahl
 * 04/01/2023
 */


public class Brush implements Disposable {

    public static final int TEXTURE_SIZE = 32; // this is absolute
    public static final int SIZE_MAX = TEXTURE_SIZE;
    public static final int SIZE_MIN = 1;


    private final Texture texture;
    private BrushShape shape;
    private int size;
    private int color;


    public Brush(BrushShape shape, int size) {
        size = clampSize(size);
        this.shape = shape;
        this.size = size;
        this.color = 255;
        this.texture = new Texture(GL_TEXTURE_2D);
        try (MemoryStack stack = MemoryStack.stackPush()){
            ByteBuffer pixels = stack.malloc(SIZE_MAX * SIZE_MAX);
            if (shape == BrushShape.CIRCLE) {
                boolean oddSize = (size & 1) == 1;
                float offset = oddSize ? 0.5f : 0.0f;
                float radius2 = (size / 2.f) * (size / 2.f);
                float center_x = (SIZE_MAX / 2f) - offset;
                float center_y = (SIZE_MAX / 2f) - offset;
                for (int r = 0; r < TEXTURE_SIZE; r++) {
                    float y = r + 0.5f;
                    float dy = center_y - y;
                    for (int c = 0; c < TEXTURE_SIZE; c++) {
                        float x = c + 0.5f;
                        float dx = center_x - x;
                        float d2 = dx * dx + dy * dy;
                        if (radius2 >= d2) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            } else {
                Area area = new Area(0,0,size-1,size-1);
                int translationX = ((TEXTURE_SIZE - size) / 2);
                int translationY = ((TEXTURE_SIZE - size) / 2);
                area.translate(translationX,translationY);
                for (int r = 0; r < TEXTURE_SIZE; r++) {
                    for (int c = 0; c < TEXTURE_SIZE; c++) {
                        if (area.contains(c,r)) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }

            }
            texture.bindToActiveSlot();
            texture.wrapST(GL_CLAMP_TO_EDGE);
            texture.filter(GL_NEAREST);
            texture.R8_2D(pixels.flip(),TEXTURE_SIZE,TEXTURE_SIZE);
            stbi_write_png("brush.png",TEXTURE_SIZE,TEXTURE_SIZE,1,pixels,TEXTURE_SIZE);
        }
    }


    private void refresh() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            ByteBuffer pixels = stack.malloc(SIZE_MAX * SIZE_MAX);
            if (shape == BrushShape.CIRCLE) {
                boolean oddSize = (size & 1) == 1;
                float offset = oddSize ? 0.5f : 0.0f;
                float radius2 = (size / 2.f) * (size / 2.f);
                float center_x = (SIZE_MAX / 2f) - offset;
                float center_y = (SIZE_MAX / 2f) - offset;
                for (int r = 0; r < TEXTURE_SIZE; r++) {
                    float y = r + 0.5f;
                    float dy = center_y - y;
                    for (int c = 0; c < TEXTURE_SIZE; c++) {
                        float x = c + 0.5f;
                        float dx = center_x - x;
                        float d2 = dx * dx + dy * dy;
                        if (radius2 >= d2) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            } else {
                Area area = new Area(0,0,size-1,size-1);
                int translationX = ((TEXTURE_SIZE - size) / 2);
                int translationY = ((TEXTURE_SIZE - size) / 2);
                area.translate(translationX,translationY);
                for (int r = 0; r < TEXTURE_SIZE; r++) {
                    for (int c = 0; c < TEXTURE_SIZE; c++) {
                        if (area.contains(c,r)) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            }
            texture.bindToActiveSlot();
            glTexSubImage2D(GL_TEXTURE_2D,0,0,0,
            TEXTURE_SIZE,TEXTURE_SIZE,GL_RED,GL_UNSIGNED_BYTE,pixels.flip());

        }
    }


    public void dispose() {
        Disposable.dispose(texture);
    }

    public void setDepthColor(int value) {
        color = value & 0xff;
    }

    public void setShape(BrushShape shape) {
        if (this.shape != shape) {
            this.shape = shape;
            refresh();
        }
    }

    public void setSize(int size) {
        size = clampSize(size);
        if (this.size != size) {
            this.size = size;
            refresh();
        }
    }

    private int clampSize(int size) {
        size = Math.min(size,SIZE_MAX);
        size = Math.max(size,SIZE_MIN);
        return size;
    }

    public Texture texture() {
        return texture;
    }

    public BrushShape shape() {
        return shape;
    }

    public int depthColor() {
        return color;
    }

    public int size() {
        return size;
    }
}
