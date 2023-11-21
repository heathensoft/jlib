package io.github.heathensoft.jlib.gui.window;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author Frederik Dahl
 * 20/11/2023
 */


public class Slider extends Box {

    // todo: boolean rounded
    protected Vector4f color = new Vector4f();
    protected Vector4f color_bg = Color.intBits_to_rgb(0x66000000,new Vector4f());
    protected Vector4f color_max = Color.hex_to_rgb("35435FFF",new Vector4f());//Color.intBits_to_rgb(0xFF00C200,new Vector4f());
    protected Vector4f color_min = Color.hex_to_rgb("35435FFF",new Vector4f());//Color.intBits_to_rgb(0xFF2F00C2,new Vector4f());
    protected float current_value;
    protected float previous_value;
    protected int id;

    public Slider(float height) { this(0,height); }

    public Slider(float min_width, float height) {
        this.restingSize.set(max(height * 2,min_width), height);
        this.currentSize.set(restingSize());
        this.id = iObtainID();
        this.current_value = 0;
        this.previous_value = -1;
        this.lockedVertical = true;
    }



    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        float w = currentSize.width();
        float h = currentSize.height();
        if (iPressed(Mouse.LEFT)) { context.focus();
            Vector2f mouse = context.mouse_position(MathLib.vec2());
            float mouse_x = U.clamp(mouse.x,x+(h/2f),x+w-(h/2f));
            current_value = U.remap(mouse_x,x+(h/2f),x+w-(h/2f),0,1);
        }
        if (!float_equals(current_value,previous_value,1e-6)) {
            color = Color.lerp(color_min,color_max,smooth(current_value),color);
            previous_value = current_value;
        }


        Rectanglef quad = MathLib.rectf(x,y-h,x+w,y);
        renderer.drawElement(quad,Color.rgb_to_intBits(color_bg),id);
        quad.maxX = lerp(x + h, x + w,current_value);


        if (iPressed(Mouse.LEFT) || (iHovered() &! iAnyInteractablePressed())) {
            float t = 0f;
            if (iPressed(Mouse.LEFT)) {
                t = iPressedDuration();
            } if (iHovered()) {
                t = max(t,iHoveredDuration());
            } t = quadratic_erase_out(clamp(t * 4.0f));

            Vector4f hsv = Color.rgb_to_hsv(MathLib.vec4(color));
            hsv.x = lerp(hsv.x,max(0.0f,hsv.x - 0.05f),t);
            hsv.y = lerp(hsv.y,max(0.0f,hsv.y - 0.25f),t);
            hsv.z = lerp(hsv.z,min(1.0f,hsv.z + 0.25f),t);
            Vector4f color = Color.hsv_to_rgb(hsv);
            renderer.drawElement(quad,0xFFFFFFFF,id);
            quad.minX += 1;
            quad.minY += 1;
            quad.maxX -= 1;
            quad.maxY -= 1;
            renderer.drawElement(quad,Color.rgb_to_intBits(color),id);
            quad.minX = quad.maxX - (h - 1 );
            renderer.drawElement(quad,0xFFFFFFFF,id);
        } else {
            renderer.drawElement(quad,Color.rgb_to_intBits(color),id);
        }



        /*
        if (iPressed(Mouse.LEFT) || (iHovered() &! iAnyInteractablePressed())) {
            Vector4f color_min = Color.rgb_to_hsv(MathLib.vec4(this.color));
            Vector4f color_max = MathLib.vec4(color_min);
            color_max.y *= 0.5f;
            color_max.z = min(1.0f,color_max.z * 1.33f);
            float t = 0f;
            if (iPressed(Mouse.LEFT)) {
                t = iPressedDuration();
            } if (iHovered()) {
                t = max(t,iHoveredDuration());
            } t = smooth(clamp(t * 2.33f));

            Vector4f color = Color.lerp_hsv(color_min,color_max,t,MathLib.vec4());
            Color.hsv_to_rgb(color);

            renderer.drawElement(quad,Color.rgb_to_intBits(color),id);
            quad.minX = quad.maxX - h;
            renderer.drawElement(quad,0xFFFFFFFF,id);
        } else {
            renderer.drawElement(quad,Color.rgb_to_intBits(color),id);
        }

         */


        /*
        renderer.drawElement(quad,Color.rgb_to_intBits(color_bg),id);
        quad.maxX = lerp(x + h, x + w,current_value);
        renderer.drawElement(quad,Color.rgb_to_intBits(color),id);

        if (iPressed(Mouse.LEFT)) {
            quad.minX = quad.maxX - h;
            renderer.drawElement(quad,0xFFFFFFFF,id);
        }
        else if (iHovered() &! iAnyInteractablePressed()) {
            quad.minX = quad.maxX - h;
            renderer.drawElement(quad,0xFFFFFFFF,id);
        }

         */
    }

    public void setValue(float value) {
        if (!float_equals(current_value,value,1e-6)) {
            this.previous_value = current_value;
            this.current_value = clamp(value);
        }
    }

    public Vector4f color_background() { return color_bg; }

    public Vector4f color_min() { return color_min; }

    public Vector4f color_max() { return color_max; }

    public float value() { return current_value; }

    public int interactableID() { return id; }
}
