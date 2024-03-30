package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.gfx.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * The Image Size is bottom-clamped to one Pixel.
 *
 * @author Frederik Dahl
 * 24/03/2024
 */


public class SpriteGUI {

    private final Texture texture;
    private final Vector2f scale;
    private final TextureRegion region;
    private float rotation;

    public SpriteGUI(Texture texture, int x, int y, int w, int h) { // check and inc reference
        if (texture == null) throw new RuntimeException("ImageGUI Null Texture");
        this.region = new TextureRegion(x,y,w,h,texture.width(),texture.height());
        this.scale = new Vector2f(1,1);
        this.texture = texture;
    }

    public SpriteGUI(Texture texture) {
        if (texture == null) throw new RuntimeException("ImageGUI Null Texture");
        this.region = new TextureRegion(texture.width(),texture.height());
        this.scale = new Vector2f(1,1);
        this.texture = texture;
    }

    private SpriteGUI(Texture texture, TextureRegion region) {
        if (texture == null) throw new RuntimeException("ImageGUI Null Texture");
        this.scale = new Vector2f(1,1);
        this.texture = texture;
        this.region = region;
    }

    public void previewStretch(RendererGUI renderer, Rectanglef quad, float glow, int tint, int id) {
        renderer.drawElement(texture,region,quad,tint,id,glow);
    }

    public void previewFit(RendererGUI renderer, Rectanglef quad, float glow, int tint, int id) { // these should move out
        float box_width = quad.lengthX();
        float box_height = quad.lengthY();
        float aspect_ratio = width() / height();
        float aspect_width = box_width;
        float aspect_height = aspect_width / aspect_ratio;
        if (aspect_height > box_height) {
            aspect_height = box_height;
            aspect_width = aspect_height * aspect_ratio;
        } float x = (box_width / 2f) - (aspect_width / 2f) + quad.minX;
        float y = (box_height / 2f) - (aspect_height / 2f) + quad.minY;
        quad = U.rectf(x,y,x+aspect_width,y+aspect_height);
        renderer.drawElement(texture,region,quad,tint,id,glow);
    }

    public void previewFree(RendererGUI renderer, Rectanglef quad, Vector2f offset, float scale, float glow, int tint, int id) {
        float box_w = quad.lengthX();
        float box_h = quad.lengthY();
        float center_x = offset.x * scale + quad.minX + box_w / 2f;
        float center_y = offset.y * scale + quad.minY + box_h / 2f;
        float width = width() * scale;
        float height = height() * scale;
        Rectanglef rect = U.rectf();
        rect.minX = center_x - width * 0.5f;
        rect.minY = center_y - height * 0.5f;
        rect.maxX = rect.minX + width;
        rect.maxY = rect.minY + height;
        if (id != 0) {
            renderer.drawElement(quad,0,id,0,true);
        } if (rect.isValid()){
            renderer.flush();
            renderer.enableScissor(quad);
            renderer.drawRotated(texture,region,rect,rotation,tint,id,glow);
            renderer.flush();
            renderer.disableScissor();
        }
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

    public SpriteGUI copy(boolean copy_properties) {
        SpriteGUI copy = new SpriteGUI(texture,region.copy());
        if (copy_properties) {
            copy.rotation = rotation;
            copy.scale.set(scale);
        } return copy;
    }

    public TextureRegion regionCopy() { return region.copy(); }

    public Vector4f uvCoordinates(Vector4f dst) { return region.getUVs(dst); }

    public Vector2f scale(Vector2f dst) { return dst.set(scale); }

    public Rectanglef bounds(Rectanglef dst, boolean rotation) {

        return dst;
    }

    public Texture createTexture(boolean allocate_mipmaps) throws Exception {
        int width = round(width());
        int height = round(height());
        Framebuffer framebuffer = new Framebuffer(width,height);
        Framebuffer.bind(framebuffer);
        Texture texture = Texture.generate2D(width,height);
        texture.bindToActiveSlot();
        texture.allocate(this.texture.format(),allocate_mipmaps);
        texture.filterNearest(); texture.repeat();
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

    public void rotateDegrees(float degrees) {
        rotateRadians((float) Math.toRadians(degrees));
    }

    public void translateTexelArea(int dx, int dy) {
        region.scroll(dx,dy);
    }

    public void setRotation0() { rotation = ROT_0; }

    public void setRotation90() { rotation = ROT_90; }

    public void setRotation180() { rotation = ROT_180; }

    public void setRotation270() { rotation = ROT_270; }

    public void setTexelArea(int x, int y, int w, int h) {
        region.set(x,y,w,h);
        clampScale();
    }

    public void setTexelPosition(int x, int y) {
        region.setPosition(x,y);
    }

    public void flipImageHorizontally() {
        region.flipHorizontally();
    }

    public void flipImageVertically() {
        region.flipVertically();
    }

    public void togglePixelCenteredUV(boolean on) {
        region.togglePixelCentered(on);
    }

    private void clampScale() {
        float minX = 1f / widthTexels();
        float minY = 1f / heightTexels();
        scale.x = Math.max(scale.x,minX);
        scale.y = Math.max(scale.y,minY);
    }
}
