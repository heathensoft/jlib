package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.ROT_270;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 29/03/2024
 */


public class Sprite {

    private final Texture texture;
    private final Vector2f scale;
    private final TextureRegion region;
    private float rotation;

    public Sprite(Texture texture, int x, int y, int w, int h) {
        if (texture == null) throw new RuntimeException("Sprite Null Texture");
        this.region = new TextureRegion(x,y,w,h,texture.width(),texture.height());
        this.scale = new Vector2f(1,1);
        this.texture = texture;
    }

    public Sprite(Texture texture, TextureRegion region) {
        if (texture == null) throw new RuntimeException("Sprite Null Texture");
        this.scale = new Vector2f(1,1);
        this.region = region.copy();
        this.texture = texture;
    }

    public Sprite(Texture texture) {
        if (texture == null) throw new RuntimeException("Sprite Null Texture");
        this.region = new TextureRegion(texture.width(),texture.height());
        this.scale = new Vector2f(1,1);
        this.texture = texture;
    }

    public boolean isFlippedVertically() { return region.isFlippedVertically(); }

    public boolean isFlippedHorizontally() { return region.isFlippedHorizontally(); }

    public int widthTexels() { return region.w(); }

    public int heightTexels() { return region.h(); }

    public float width() { return widthTexels() * scale.x; }

    public float height() { return heightTexels() * scale.y; }

    public float rotationRadians() { return rotation; }

    public float rotationDegrees() { return (float) Math.toDegrees(rotation); }

    public Texture texture() { return texture; }

    public TextureRegion regionCopy() { return region.copy(); }

    public Vector4f uvCoordinates(Vector4f dst) { return region.getUVs(dst); }

    public Vector2f scale(Vector2f dst) { return dst.set(scale); }

    public Rectanglef bounds(Rectanglef dst, boolean centered, boolean rotated) {
        if (rotated) { float w, h;
            if (rotation == ROT_0) { w = width(); h = height();
            } else if (rotation == ROT_90) { w = height(); h = width();
            } else if (rotation == ROT_180) { w = width(); h = height();
            } else if (rotation == ROT_270) { w = height(); h = width();
            } else { w = width(); h = height();
                final float cx = w / 2f; final float cy = h / 2f;
                Vector2f p0 = U.rotate2D(U.vec2(-cx,-cy),rotation);
                Vector2f p1 = U.rotate2D(U.vec2( cx,-cy),rotation);
                Vector2f p2 = U.rotate2D(U.vec2(-cx, cy),rotation);
                Vector2f p3 = U.rotate2D(U.vec2( cx, cy),rotation);
                dst.minX = Math.min(p0.x,Math.min(p1.x,Math.min(p2.x,p3.x)));
                dst.minY = Math.min(p0.y,Math.min(p1.y,Math.min(p2.y,p3.y)));
                dst.maxX = Math.max(p0.x,Math.max(p1.x,Math.max(p2.x,p3.x)));
                dst.maxY = Math.max(p0.y,Math.max(p1.y,Math.max(p2.y,p3.y)));
                if (!centered) dst.translate(-dst.minX,-dst.minY);
                return dst;
            } if (centered) {
                final float cx = w / 2f;
                final float cy = h / 2f;
                dst.minX = -cx; dst.minY = -cy;
                dst.maxX =  cx; dst.maxY =  cy;
            } else {
                dst.minX = 0f; dst.minY = 0f;
                dst.maxX = w;  dst.maxY = h;
            }
        } else {
            final float w = width();
            final float h = height();
            if (centered) {
                final float cx = w / 2f;
                final float cy = h / 2f;
                dst.minX = -cx; dst.minY = -cy;
                dst.maxX =  cx; dst.maxY =  cy;
            } else {
                dst.minX = 0f; dst.minY = 0f;
                dst.maxX = w; dst.maxY = h;
            }
        }
        return dst;
    }

    public Bitmap createBitmap() throws Exception {
        //texture.get();
        return null;
    }

    public Texture createTexture(boolean allocate_mipmaps) throws Exception {
        int width = round(width());
        int height = round(height());
        Framebuffer framebuffer = new Framebuffer(width,height);
        Framebuffer.bind(framebuffer);
        Texture texture = Texture.generate2D(width,height);
        texture.bindToActiveSlot();
        texture.allocate(this.texture.format(),allocate_mipmaps);
        texture.nearest(); texture.repeat();
        Framebuffer.attachColor(texture,0,false);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        Framebuffer.viewport();
        Vector4f uv = region.getUVs(U.vec4());
        ShaderProgram.texturePass(this.texture,uv);
        Disposable.dispose(framebuffer);
        return texture;
    }

    public void setScale(float x, float y) {
        if (x > 0) {
            if (x < 1) {
                float min = 1f / widthTexels();
                scale.x = Math.max(x, min);
            } else scale.x = x;
        } if (y > 0) {
            if (y < 1) {
                float min = 1f / heightTexels();
                scale.y = Math.max(y,min);
            } else scale.y = y;
        }
    }

    public void setSize(float w, float h) {
        float width = width();
        float height = height();
        resize(w - width,h - height);
    }

    public void resize(float dx, float dy) {
        if (dx > 0) {
            float width_texels = widthTexels();
            float width = width_texels * scale.x;
            scale.x = (width + dx) / width_texels;
        } else if (dx < 0) {
            float width_texels = widthTexels();
            float width = width_texels * scale.x;
            width = Math.max(1f,width + dx);
            scale.x = width / width_texels;
        } if (dy > 0) {
            float height_texels = heightTexels();
            float height = height_texels * scale.y;
            scale.y = (height + dy) / height_texels;
        } else if (dy < 0) {
            float height_texels = heightTexels();
            float height = height_texels * scale.y;
            height = Math.max(1f,height + dy);
            scale.y = height / height_texels;
        }
    }

    public void setRotationRadians(float radians) {
        rotation = radians % ROT_360;
        if (rotation < 0) rotation += ROT_360;
    }

    public void setRotationDegrees(float degrees) {
        setRotationRadians((float)Math.toRadians(degrees));
    }

    public void rotateRadians(float radians) {
        rotation = (rotation + radians) % ROT_360;
        if (rotation < 0) rotation += ROT_360;
    }

    public void rotateDegrees(float degrees) { rotateRadians((float) Math.toRadians(degrees)); }

    public void translateTexelArea(int dx, int dy) { region.scroll(dx,dy); }

    public void setRotation0() { rotation = ROT_0; }

    public void setRotation90() { rotation = ROT_90; }

    public void setRotation180() { rotation = ROT_180; }

    public void setRotation270() { rotation = ROT_270; }

    public void setTexelArea(int x, int y, int w, int h) {
        region.set(x,y,w,h);
        clampScale();
    }

    public void setTexelPosition(int x, int y) { region.setPosition(x,y); }

    public void flipImageHorizontally() { region.flipHorizontally(); }

    public void flipImageVertically() { region.flipVertically(); }

    public void togglePixelCenteredUV(boolean on) { region.togglePixelCentered(on); }

    private void clampScale() {
        float minX = 1f / widthTexels();
        float minY = 1f / heightTexels();
        scale.x = Math.max(scale.x,minX);
        scale.y = Math.max(scale.y,minY);
    }
}
