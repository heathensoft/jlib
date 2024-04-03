package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public class BackGround { // use background for item containers. if you know the dim. spacing etc

    private boolean fill;
    private Texture bg_texture;
    private Vector4f bg_color;
    private final Vector2f bg_texture_scale;
    private final Vector2f bg_position_pixels;
    private final Vector2f bg_velocity_pixels;


    public BackGround(Texture texture, Vector4f color) {
        if (texture != null && texture.target() == GL_TEXTURE_2D) {
            this.bg_texture = texture;
        } else this.bg_texture = null;
        this.bg_color = color == null ? new Vector4f() : color;
        this.bg_texture_scale = new Vector2f(1,1);
        this.bg_position_pixels = new Vector2f(0,0);
        this.bg_velocity_pixels = new Vector2f(0,0);
    }

    public void render(RendererGUI renderer, Rectanglef quad, int id, float dt) {
        int color = Color.rgb_to_intBits(bg_color);
        if (bg_texture == null) renderer.drawElement(quad,color,id);
        else if (fill) renderer.drawElement(bg_texture,quad,color,id);
        else { float texture_width = bg_texture.width();
            float texture_height = bg_texture.height();
            if (!U.floatEquals(bg_velocity_pixels.lengthSquared(),0,0.01f)) {
                bg_position_pixels.x += (bg_velocity_pixels.x * dt);
                bg_position_pixels.y += (bg_velocity_pixels.y * dt);
            } bg_position_pixels.x = bg_position_pixels.x % texture_width;
            bg_position_pixels.y = bg_position_pixels.y % texture_height;
            float u1 = bg_position_pixels.x / texture_width; // round?
            float v1 = bg_position_pixels.y / texture_height;
            float u2 = u1 + (quad.lengthX() / texture_width) * (1f/bg_texture_scale.x);
            float v2 = v1 + (quad.lengthY() / texture_height) * (1f/bg_texture_scale.y);
            Vector4f region = U.vec4(u1,v1,u2,v2);
            renderer.drawElement(bg_texture,region,quad,color,id);
        }
    }

    public Vector2f positionPixels() { return bg_position_pixels; }
    public Vector2f velocityPixels() { return bg_velocity_pixels; }
    public Vector2f scale() { return bg_texture_scale; }
    public Texture texture() { return bg_texture; }
    public Vector4f color() { return bg_color; }
    public void setTexture(Texture texture) { bg_texture = texture; }
    public void toggleFill(boolean on) { fill = on; }
    public void setColor(Vector4f color) {
        if (color == null) { bg_color.zero();}
        else bg_color = color;
    }


}
