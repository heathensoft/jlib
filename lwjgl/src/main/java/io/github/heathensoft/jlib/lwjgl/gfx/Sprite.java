package io.github.heathensoft.jlib.lwjgl.gfx;

import org.joml.Math;
import org.joml.primitives.Rectanglef;

/**
 *
 * Borrowed functionality from the libgdx class with the same name, but the data structure is very different.
 * This Sprite's texture region is shared between sprites. So you would have some external mapping to the regions.
 * Textures are also managed and disposed externally. Instead of 4 bytes of color for each vertex, this use 4 bytes per instance.
 * Each Sprite also has a 4 byte id per instance. This could be used for pixel perfect mouse picking,
 * or something else entirely.
 *
 * Tips: Try to do translation / set position /  set bounds after other operation: rotation and scaling
 * as it's slightly better for performance.
 *
 * @author Frederik Dahl
 * 29/06/2022
 */

//  v1------v4
//  |        |
//  |        |
//  v2------v3

public class Sprite {
    
    public static final int DEFAULT_ID = 0;
    public static final int UV_SIZE = 2;
    public static final int POS_SIZE = 2;
    public static final int COLOR_SIZE = 1;
    public static final int ID_SIZE = 1;
    public static final int VERTICES = 4;
    public static final int NUM_INDICES = 6;
    public static final int VERTEX_SIZE = POS_SIZE + UV_SIZE + COLOR_SIZE + ID_SIZE;
    public static final int SIZE = VERTICES * VERTEX_SIZE;
    

    public static final Color32 DEFAULT_COLOR = Color32.WHITE;
    
    protected static final int X1 = 0;     // x
    protected static final int Y1 = 1;     // y + h
    protected static final int U1 = 2;     // u
    protected static final int V1 = 3;     // v
    protected static final int C1 = 4;
    protected static final int I1 = 5;
    protected static final int X2 = 6;     // x
    protected static final int Y2 = 7;     // y
    protected static final int U2 = 8;     // u
    protected static final int V2 = 9;     // v2
    protected static final int C2 = 10;
    protected static final int I2 = 11;
    protected static final int X3 = 12;    // x + w
    protected static final int Y3 = 13;    // y
    protected static final int U3 = 14;    // u2
    protected static final int V3 = 15;    // v2
    protected static final int C3 = 16;
    protected static final int I3 = 17;
    protected static final int X4 = 18;    // x + h
    protected static final int Y4 = 19;    // y + h
    protected static final int U4 = 20;    // u2
    protected static final int V4 = 21;    // v
    protected static final int C4 = 22;
    protected static final int I4 = 23;
    
    private final float[] data = new float[SIZE];
    private final Color32 color = Color32.WHITE.cpy();
    private TextureRegion region;
    private float x;
    private float y;
    private float width;
    private float height;
    private float originX;
    private float originY;
    private float scale;
    private float rotation;
    private boolean dirty;

    public Sprite() { }
    
    public Sprite(TextureRegion region, float x, float y, float w, float h) {
        this.region = region;
        final float x2 = x + w;
        final float y2 = y + h;
        final float u = region.u();
        final float v = region.v();
        final float u2 = region.u2();
        final float v2 = region.v2();
        setData(x,y,x2,y2,w,h,u,v,u2,v2);
    }

    public void set(TextureRegion region, float x, float y, float w, float h) {
        this.region = region;
        final float x2 = x + w;
        final float y2 = y + h;
        final float u = region.u();
        final float v = region.v();
        final float u2 = region.u2();
        final float v2 = region.v2();
        setData(x,y,x2,y2,w,h,u,v,u2,v2);
    }

    public float[] data() {
        if (dirty) {
            dirty = false;
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
            } if (rotation != 0) {
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
                final float x1 = localXCos - localY2Sin + worldOriginX;
                final float y1 = localY2Cos + localXSin + worldOriginY;
                final float x2 = localXCos - localYSin + worldOriginX;
                final float y2 = localYCos + localXSin + worldOriginY;
                final float x3 = localX2Cos - localYSin + worldOriginX;
                final float y3 = localYCos + localX2Sin + worldOriginY;
                final float x4 = x1 + (x3 - x2);
                final float y4 = y3 - (y2 - y1);
                data[X1] = x1; data[Y1] = y1;
                data[X2] = x2; data[Y2] = y2;
                data[X3] = x3; data[Y3] = y3;
                data[X4] = x4; data[Y4] = y4;
            } else {
                final float x1 = localX + worldOriginX;
                final float y1 = localY + worldOriginY;
                final float x2 = localX2 + worldOriginX;
                final float y2 = localY2 + worldOriginY;
                data[X1] = x1; data[Y1] = y2;
                data[X2] = x1; data[Y2] = y1;
                data[X3] = x2; data[Y3] = y1;
                data[X4] = x2; data[Y4] = y2;
            }
        }
        return data;
    }
    
    public void setPosition (float x, float y) {
        this.x = x;
        this.y = y;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        float x2 = x + width;
        float y2 = y + height;
        data[X1] = x;
        data[Y1] = y2;
        data[X2] = x;
        data[Y2] = y;
        data[X3] = x2;
        data[Y3] = y;
        data[X4] = x2;
        data[Y4] = y2;
    }
    
    public void setSize (float width, float height) {
        this.width = width;
        this.height = height;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        float x2 = x + width;
        float y2 = y + height;
        data[Y1] = y2;
        data[X3] = x2;
        data[X4] = x2;
        data[Y4] = y2;
    }
    
    public void setBounds (float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        float x2 = x + width;
        float y2 = y + height;
        data[X1] = x;
        data[Y1] = y2;
        data[X2] = x;
        data[Y2] = y;
        data[X3] = x2;
        data[Y3] = y;
        data[X4] = x2;
        data[Y4] = y2;
    }
    
    public void setX (float x) {
        this.x = x;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        float x2 = x + width;
        data[X1] = x;
        data[X2] = x;
        data[X3] = x2;
        data[X4] = x2;
    }
    
    public void setY (float y) {
        this.y = y;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        float y2 = y + height;
        data[Y1] = y2;
        data[Y2] = y;
        data[Y3] = y;
        data[Y4] = y2;
    }
    
    public void setOriginBasedPosition (float x, float y) {
        setPosition(x - originX, y - originY);
    }
    
    public void translateX (float amount) {
        this.x += amount;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        data[X1] += amount;
        data[X2] += amount;
        data[X3] += amount;
        data[X4] += amount;
    }
    
    public void translateY (float amount) {
        this.y += amount;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        data[Y1] += amount;
        data[Y2] += amount;
        data[Y3] += amount;
        data[Y4] += amount;
    }
    
    public void translate(float xAmount, float yAmount) {
        x += xAmount;
        y += yAmount;
        if (dirty) return;
        if (rotation != 0 || scale != 1) {
            dirty = true;
            return;
        }
        data[X1] += xAmount;
        data[X2] += xAmount;
        data[X3] += xAmount;
        data[X4] += xAmount;
        data[Y1] += yAmount;
        data[Y2] += yAmount;
        data[Y3] += yAmount;
        data[Y4] += yAmount;
    }

    public void setRegion(TextureRegion tr) {
        if (region != tr) {
            float u = tr.u();
            float v = tr.v();
            float u2 = tr.u2();
            float v2 = tr.v2();
            data[U1] = u;
            data[V1] = v;
            data[U2] = u;
            data[V2] = v2;
            data[U3] = u2;
            data[V3] = v2;
            data[U4] = u2;
            data[V4] = v;
            region = tr;
        }
    }
    
    public void setID(int id) {
        data[I1] = id;
        data[I2] = id;
        data[I3] = id;
        data[I4] = id;
    }
    
    public void setColor(Color32 color) {
        this.color.set(color);
        float floatBits = color.floatBits();
        data[C1] = floatBits;
        data[C2] = floatBits;
        data[C3] = floatBits;
        data[C4] = floatBits;
    }
    
    public void setColorDefault() {
        this.color.set(DEFAULT_COLOR);
        float floatBits = color.floatBits();
        data[C1] = floatBits;
        data[C2] = floatBits;
        data[C3] = floatBits;
        data[C4] = floatBits;
    }
    
    public void setAlpha (float a) {
        color.setAlpha(a);
        float floatBits = color.floatBits();
        data[C1] = floatBits;
        data[C2] = floatBits;
        data[C3] = floatBits;
        data[C4] = floatBits;
    }
    
    public void flipX() {
        float tmp = data[U1];
        data[U1] = data[U4];
        data[U4] = tmp;
        tmp = data[U2];
        data[U2] = data[U3];
        data[U3] = tmp;
    }
    
    public void flipY() {
        float tmp = data[V1];
        data[V1] = data[V2];
        data[V2] = tmp;
        tmp = data[V4];
        data[V4] = data[V3];
        data[V3] = tmp;
    }
    
    public void rotateUVs180() {
        float temp = data[V4];
        data[V4] = data[V2];
        data[V2] = temp;
        temp = data[V1];
        data[V1] = data[V3];
        data[V3] = temp;
        temp = data[U4];
        data[U4] = data[U2];
        data[U2] = temp;
        temp = data[U1];
        data[U1] = data[U3];
        data[U3] = temp;
    }
    
    public void rotateUVs90(boolean clockwise) {
        float temp = data[V4];
        if (clockwise) {
            data[V4] = data[V1];
            data[V1] = data[V2];
            data[V2] = data[V3];
            data[V3] = temp;
            temp = data[U4];
            data[U4] = data[U1];
            data[U1] = data[U2];
            data[U2] = data[U3];
            data[U3] = temp;
        } else {
            data[V4] = data[V3];
            data[V3] = data[V2];
            data[V2] = data[V1];
            data[V1] = temp;
            temp = data[U4];
            data[U4] = data[U3];
            data[U3] = data[U2];
            data[U2] = data[U1];
            data[U1] = temp;
        }
    }
    
    public void rotateBy(float deg) {
        if (deg == 0) return;
        rotation += deg;
        dirty = true;
    }
    
    public void setRotation(float deg) {
        this.rotation = deg;
        dirty = true;
    }
    
    public void scaleBy(float amount) {
        scale += amount;
        dirty = true;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
        this.dirty = true;
    }
    
    public void setOrigin (float originX, float originY) {
        this.originX = originX;
        this.originY = originY;
        dirty = true;
    }
    
    public void setOriginCenter () {
        this.originX = width / 2;
        this.originY = height / 2;
        dirty = true;
    }
    
    public float x() {
        return x;
    }
    
    public float y() {
        return y;
    }
    
    public float width() {
        return width;
    }
    
    public float height() {
        return height;
    }
    
    public float scale() {
        return scale;
    }
    
    public float originX() {
        return originX;
    }
    
    public float originY() {
        return originY;
    }
    
    public int id() {
        return (int) data[I1];
    }
    
    public float rotationDeg() {
        return rotation;
    }
    
    public Color32 colorReadOnly() {
        return color;
    }
    
    public TextureRegion region() {
        return region;
    }
    
    public void boundingBox(Rectanglef bounds) { // todo: rotation
        bounds.minX = data[X2];
        bounds.minY = data[Y2];
        bounds.maxX = data[X4];
        bounds.maxY = data[Y4];
    }
    
    private void setData(float x, float y, float x2, float y2, float w, float h,
                         float u, float v, float u2, float v2) {
        float floatBits = color.floatBits();
        data[X1] = x;
        data[Y1] = y2;
        data[U1] = u;
        data[V1] = v;
        data[C1] = floatBits;
        data[I1] = DEFAULT_ID;
        data[X2] = x;
        data[Y2] = y;
        data[U2] = u;
        data[V2] = v2;
        data[C2] = floatBits;
        data[I2] = DEFAULT_ID;
        data[X3] = x2;
        data[Y3] = y;
        data[U3] = u2;
        data[V3] = v2;
        data[C3] = floatBits;
        data[I3] = DEFAULT_ID;
        data[X4] = x2;
        data[Y4] = y2;
        data[U4] = u2;
        data[V4] = v;
        data[C4] = floatBits;
        data[I4] = DEFAULT_ID;
        width = w;
        height = h;
        originX = w / 2f;
        originY = h / 2f;
        scale = 1;
        this.x = x;
        this.y = y;
    }
    
    public static short[] generateIndices(int sprites) {
        int len = sprites * NUM_INDICES;
        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
        } return indices;
    }
    
}
