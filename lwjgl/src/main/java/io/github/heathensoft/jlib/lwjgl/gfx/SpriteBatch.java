package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import org.joml.Math;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.FloatBuffer;

import static io.github.heathensoft.jlib.lwjgl.gfx.Sprite.*;


/**
 * @author Frederik Dahl
 * 01/07/2022
 */


public class SpriteBatch implements Disposable {

    public static final float DEFAULT_COLOR_BITS = DEFAULT_COLOR.floatBits();
    public static final int MAX_CAPACITY = Short.MAX_VALUE / 4;
    
    private final SpriteVertexData vertexData;
    private final FloatBuffer vertexBuffer;
    private final int capacity;
    
    private int count;
    private int drawCalls;
    private int drawCallsTotal;
    private boolean rendering;
    
    
    public SpriteBatch(int capacity) {
        this.capacity = capacity = Math.min(MAX_CAPACITY,capacity);
        vertexBuffer = MemoryUtil.memAllocFloat(capacity * SIZE);
        vertexData = new SpriteVertexData(capacity);
    }
    
    public void begin() {
        if (rendering) {
            Logger.warn("spriteBatch: already in rendering state");
            return;
        }
        drawCalls = 0;
        rendering = true;
    }
    
    public void end() {
        if (!rendering) {
            Logger.warn("spriteBatch: calling end when not in rendering state");
            return;
        }
        if (count > 0) flush();
        rendering = false;
    }
    
    private void flush() {
        vertexData.render(vertexBuffer,count);
        count = 0;
        drawCalls++;
        drawCallsTotal++;
    }
    
    public void draw(Sprite sprite) {
        if (rendering) {
            if (count == capacity) flush();
            vertexBuffer.put(sprite.data(),0, SIZE);
            count++;
        } else {
            Logger.warn("spriteBatch: call begin before draw");
        }
    }
    
    /*
    public void draw(SpriteBuffer buffer) {
        if (rendering && buffer.notEmpty()) {
            int sprites = buffer.size();
            if (count + sprites < capacity) {
                vertexBuffer.put(buffer.vertexData());
                instanceBuffer.put(buffer.instanceData());
                count += sprites;
            } else {
                final float[] v_data = buffer.vertexData();;
                final float[] i_data = buffer.instanceData();
                for (int i = 0; i < sprites; i++) {
                    if (count == capacity) flush();
                    vertexBuffer.put(v_data,i * VERTICES_SIZE, VERTICES_SIZE);
                    instanceBuffer.put(i_data,i * INSTANCE_DATA_SIZE, INSTANCE_DATA_SIZE);
                    count++;
                }
            }
        }
    }
    
     */

    public void draw() {
        draw(0,0,1,1);
    }

    public void draw(float u, float v, float u2, float v2) {
        draw(u,v,u2,v2,-1,-1,2,2,DEFAULT_COLOR_BITS,DEFAULT_ID);
    }
    
    public void draw(float x, float y, float width, float height, float color, float id) {
        draw(0,0,1,1,x,y,width,height,color,id);
    }
    
    public void draw(float u, float v, float u2, float v2, float x, float y, float width, float height, float color, float id) {
        if (rendering) {
            if (count == capacity) flush();
            final float x2 = x + width;
            final float y2 = y + height;
            vertexBuffer
            .put(x).put(y2).put(u).put(v).put(color).put(id)
            .put(x).put(y).put(u).put(v2).put(color).put(id)
            .put(x2).put(y).put(u2).put(v2).put(color).put(id)
            .put(x2).put(y2).put(u2).put(v).put(color).put(id);
            count++;
        }
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        draw(region, x, y, width, height, DEFAULT_ID);
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height, int id) {
        draw(region, x, y, width, height, DEFAULT_COLOR_BITS, id);
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height, float color, int id) {
        if (rendering) {
            if (count == capacity) flush();
            final float x2 = x + width;
            final float y2 = y + height;
            final float u = region.u();
            final float v = region.v();
            final float u2 = region.u2();
            final float v2 = region.v2();
            vertexBuffer
                    .put(x).put(y2).put(u).put(v).put(color).put(id)
                    .put(x).put(y).put(u).put(v2).put(color).put(id)
                    .put(x2).put(y).put(u2).put(v2).put(color).put(id)
                    .put(x2).put(y2).put(u2).put(v).put(color).put(id);
            count++;
        }
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height,
                     float originX, float originY, float scale) {
        draw(region, x, y, width, height, originX, originY, scale,
                DEFAULT_ID, DEFAULT_COLOR_BITS);
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height,
                     float originX, float originY, float scale, int id, float color) {
        if (rendering) {
            if (count == capacity) flush();
            float localX = -originX;
            float localY = -originY;
            float localX2 = localX + width;
            float localY2 = localY + height;
            float worldOriginX = x - localX;
            float worldOriginY = y - localY;
            if (scale != 1) {
                localX *= scale;
                localY *= scale;
                localX2 *= scale;
                localY2 *= scale;
            }
            final float x1 = localX + worldOriginX;
            final float y1 = localY + worldOriginY;
            final float x2 = localX2 + worldOriginX;
            final float y2 = localY2 + worldOriginY;
            final float u = region.u();
            final float v = region.v();
            final float u2 = region.u2();
            final float v2 = region.v2();
            vertexBuffer
                    .put(x).put(y2).put(u).put(v).put(color).put(id)
                    .put(x).put(y).put(u).put(v2).put(color).put(id)
                    .put(x2).put(y).put(u2).put(v2).put(color).put(id)
                    .put(x2).put(y2).put(u2).put(v).put(color).put(id);
            count++;
        }
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height,
                     float originX, float originY, float scale, float rotation) {
        draw(region,x,y,width,height, originX,originY,scale,rotation, DEFAULT_ID);
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height,
                     float originX, float originY, float scale, float rotation, int id) {
        draw(region,x,y,width,height,originX,originY,scale,rotation, DEFAULT_COLOR_BITS,id);
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height,
                     float originX, float originY, float scale, float rotation, float color, int id) {
        draw(region.u(),region.v(),region.u2(),region.v2(),x,y,
                width,height,originX,originY,scale,rotation,color,id);
    }
    
    public void draw(float u, float v, float u2, float v2, float x, float y, float width, float height,
                     float originX, float originY, float scale, float rotation, float color, int id) {
        if (rendering) {
            if (count == capacity) flush();
            float localX = -originX;
            float localY = -originY;
            float localX2 = localX + width;
            float localY2 = localY + height;
            float worldOriginX = x - localX;
            float worldOriginY = y - localY;
            if (scale != 1) {
                localX *= scale;
                localY *= scale;
                localX2 *= scale;
                localY2 *= scale;
            }
            float x1, x2, x3, x4;
            float y1, y2, y3, y4;
            if (rotation != 0) {
                final float radians = Math.toRadians(rotation);
                final float cos = Math.cos(radians);
                final float sin = Math.sin(radians);
                final float localXCos = localX * cos;
                final float localXSin = localX * sin;
                final float localYCos = localY * cos;
                final float localYSin = localY * sin;
                final float localX2Cos = localX2 * cos;
                final float localX2Sin = localX2 * sin;
                final float localY2Cos = localY2 * cos;
                final float localY2Sin = localY2 * sin;
                x1 = localXCos - localY2Sin + worldOriginX;
                y1 = localY2Cos + localXSin + worldOriginY;
                x2 = localXCos - localYSin + worldOriginX;
                y2 = localYCos + localXSin + worldOriginY;
                x3 = localX2Cos - localYSin + worldOriginX;
                y3 = localYCos + localX2Sin + worldOriginY;
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = localX + worldOriginX;
                y1 = localY + worldOriginY;
                x2 = localX2 + worldOriginX;
                y2 = localY2 + worldOriginY;

                // TODO: !!!!!!!!!!!!!!!!!

                x3 = x2;
                y3 = y1;
                x4 = x2;
                y4 = y2;
            }
            vertexBuffer
                    .put(x).put(y2).put(u).put(v).put(color).put(id)
                    .put(x).put(y).put(u).put(v2).put(color).put(id)
                    .put(x2).put(y).put(u2).put(v2).put(color).put(id)
                    .put(x2).put(y2).put(u2).put(v).put(color).put(id);
            count++;
        }
    }
    
    public boolean isRendering() {
        return rendering;
    }
    
    @Override
    public void dispose() {
        if (vertexBuffer != null) MemoryUtil.memFree(vertexBuffer);
        Disposable.dispose(vertexData);
    }
    
}
