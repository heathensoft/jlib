package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.Window;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

/**
 * For displaying Textures
 *
 * @author Frederik Dahl
 * 21/03/2024
 */


public class ImageDisplay implements Interactable {

    private Texture texture;
    private Vector4f color;
    private ImageAlignment alignment;
    private final Vector2f scale;
    private final Vector2f position; // pixels
    private final Vector2f velocity; // pixels / second
    private final Vector2f grab_origin;
    private final int interactable_id;
    private float zoom;

    public ImageDisplay(Vector4f color) { this(null,ImageAlignment.STRETCH,color); }

    public ImageDisplay(Texture texture, ImageAlignment alignment, Vector4f color) {
        this.scale = new Vector2f(1,1);
        this.grab_origin = new Vector2f();
        this.position = new Vector2f();
        this.velocity = new Vector2f();
        this.alignment = alignment;
        this.texture = texture;
        this.color = color;
        this.interactable_id = iObtainID();
    }

    public void render(Window window, RendererGUI renderer, Rectanglef quad, int id, float glow, float dt) {
        if (quad.isValid()) {
            int color = Color.rgb_to_intBits(this.color);
            id = id == 0 ? interactableID() : id;
            if (texture == null) {
                renderer.drawElement(quad,color,id,glow);
            } else {
                switch (alignment) {
                    case REPEAT -> {
                        if (iHovered() && GUI.mouse.scrolled()) {
                            float amount = GUI.mouse.get_scroll();
                            if (amount != 0) {
                                zoom += amount;
                                zoom = U.clamp(zoom,-3,3);
                                scale.set((float) Math.pow(2,(int)zoom));
                                if (window instanceof BoxWindow boxWindow) {
                                    boxWindow.displayFading("x",scale.x,null,quad);
                                }
                            }
                        }
                        float texture_width = texture.width();
                        float texture_height = texture.height();
                        if (scale.x > 0 && scale.y > 0) {
                            if (iGrabbed(Mouse.LEFT)) {
                                Vector2f drag_vector = MathLib.vec2();
                                GUI.mouse_drag_vector(drag_vector,Mouse.LEFT).div(scale);
                                position.set(grab_origin).sub(drag_vector.x,-drag_vector.y);
                            } else if (iJustPressed(Mouse.LEFT)) {
                                grab_origin.set(position);
                            }

                            else if (!U.float_equals(velocity.lengthSquared(),0,0.01f)) {
                                position.x += (velocity.x * dt);
                                position.y += (velocity.y * dt);
                            } position.x = position.x % texture_width;
                            position.y = position.y % texture_height;
                            float u1 = position.x / texture_width; // round?
                            float v1 = position.y / texture_height;
                            float u2 = u1 + (quad.lengthX() / texture_width) * (1f/scale.x);
                            float v2 = v1 + (quad.lengthY() / texture_height) * (1f/scale.y);
                            Vector4f region = MathLib.vec4(u1,v1,u2,v2);
                            renderer.drawElement(texture,region,quad,color,id,glow);
                        }
                    }
                    case CENTERED -> {
                        if (iHovered() && GUI.mouse.scrolled()) {
                            float amount = GUI.mouse.get_scroll();
                            if (amount != 0) {
                                zoom += amount;
                                zoom = U.clamp(zoom,-3,3);
                                scale.set((float) Math.pow(2,(int)zoom));
                                if (window instanceof BoxWindow boxWindow) {
                                    boxWindow.displayFading("x",scale.x,null,quad);
                                }
                            }
                        }
                        if (scale.x > 0 && scale.y > 0) {
                            if (iGrabbed(Mouse.LEFT)) {
                                Vector2f drag_vector = MathLib.vec2();
                                GUI.mouse_drag_vector(drag_vector,Mouse.LEFT);
                                position.set(grab_origin).add(drag_vector.x,drag_vector.y);
                            } else if (iJustPressed(Mouse.LEFT)) {
                                grab_origin.set(position);
                            } else if (position.lengthSquared() <= 1f) {
                                position.zero();
                            } else { // pull spring
                                Vector2f v = MathLib.vec2(-position.x,-position.y).normalize();
                                float speed = Math.max(10f,position.length());
                                position.add(v.mul(speed * dt));
                            } position.x = position.x % texture.width();
                            position.y = position.y % texture.height();
                            float box_width = quad.lengthX();
                            float box_height = quad.lengthY();
                            float box_center_x = quad.minX + box_width / 2f;
                            float box_center_y = quad.minY + box_height / 2f;
                            float virtual_width = texture.width() * scale.x;
                            float virtual_height = texture.height() * scale.y;
                            float virtual_center_x = position.x + box_center_x;
                            float virtual_center_y = position.y + box_center_y;
                            float virtual_x1 = virtual_center_x - virtual_width / 2f;
                            float virtual_y1 = virtual_center_y - virtual_height / 2f;
                            float virtual_x2 = virtual_x1 + virtual_width;
                            float virtual_y2 = virtual_y1 + virtual_height;
                            Rectanglef virtual_quad = MathLib.rectf(virtual_x1,virtual_y1,virtual_x2,virtual_y2);
                            if (id == interactableID() && quad.containsRectangle(virtual_quad)) {
                                renderer.drawElement(quad,0,id,0,true);
                            } if (virtual_quad.intersectsRectangle(quad)) {
                                renderer.flush();
                                renderer.enableScissor(
                                        Math.max(quad.minX,virtual_quad.minX),
                                        Math.max(quad.minY,virtual_quad.minY),
                                        Math.min(quad.maxX,virtual_quad.maxX),
                                        Math.min(quad.maxY,virtual_quad.maxY)
                                );
                                renderer.drawElement(texture,virtual_quad,color,id,glow);
                                renderer.flush();
                                renderer.disableScissor();
                            }
                        }
                    }
                    case STRETCH -> { renderer.drawElement(texture,quad,color,id,glow); }
                    case FIT -> {
                        float box_width = quad.lengthX();
                        float box_height = quad.lengthY();
                        float aspect_ratio = (float) texture.width() / texture.height();
                        float aspect_width = box_width;
                        float aspect_height = aspect_width / aspect_ratio;
                        if (aspect_height > box_height) {
                            aspect_height = box_height;
                            aspect_width = aspect_height * aspect_ratio;
                        }
                        float x = (box_width / 2f) - (aspect_width / 2f) + quad.minX;
                        float y = (box_height / 2f) - (aspect_height / 2f) + quad.minY;
                        Rectanglef fit_quad = MathLib.rectf(x,y,x+aspect_width,y+aspect_height);
                        renderer.drawElement(texture,fit_quad,color,id,glow);
                    }
                }
            }
        }
    }

    public ImageAlignment getAlignment() { return alignment; }

    public void setAlignment(ImageAlignment alignment) { this.alignment = alignment; }

    public Texture getTexture() { return texture; }

    public void setTexture(Texture texture) { this.texture = texture; }

    public Vector4f getColor() { return color; }

    public void setColor(Vector4f color) { if (color != null) this.color = color; }

    public Vector2f getScale() { return scale; }

    public void setScale(float x, float y) { scale.set(x, y); }

    public Vector2f getVelocity() { return velocity; }

    public void setVelocity(float x, float y) { velocity.set(x,y); }

    public int interactableID() { return interactable_id; }
}
