package io.github.heathensoft.jlib.gui.window;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
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


public class SliderOld extends Box {

    // todo: boolean rounded
    protected Vector4f color = new Vector4f();
    protected Vector4f color_bg = Color.intBits_to_rgb(0x66000000,new Vector4f());
    protected Vector4f color_max = Color.hex_to_rgb("35435FFF",new Vector4f());//Color.intBits_to_rgb(0xFF00C200,new Vector4f());
    protected Vector4f color_min = Color.hex_to_rgb("35435FFF",new Vector4f());//Color.intBits_to_rgb(0xFF2F00C2,new Vector4f());


    protected float current_value;
    protected float previous_value;
    protected int id;

    public SliderOld(float height) { this(0,height); }

    public SliderOld(float min_width, float height) {
        this.restingSize.set(max(height * 2,min_width), height);
        this.currentSize.set(restingSize());
        this.id = iObtainID();
        this.current_value = 0;
        this.previous_value = -1;
        this.lockedVertical = true;
    }

    public void render3(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        float bounds_width = currentSize.width();
        float bounds_height = currentSize.height();
        float slider_rect_height = bounds_height / 2f;
        float slider_rect_width = bounds_width - slider_rect_height;
        float slider_circle_radius = slider_rect_height / 2f;
        float slider_rect_x = x + slider_circle_radius;
        float slider_rect_y = y - slider_circle_radius;
        Rectanglef rect = MathLib.rectf();
        TextureAtlas default_icons = GUI.default_icons;
        Texture icons_diffuse_texture = default_icons.texture(0);
        TextureRegion circle_region = default_icons.getRegion(GUI.icon_default_circle);
        int color_slider_default = 0xFF777777;
        int color_slider_background = 0xFF333333;
        int color_slider_active = 0xFF33AA33;
        if (iPressed(Mouse.LEFT)) { context.focus();
            Vector2f mouse = context.mouse_position(MathLib.vec2());
            float min = slider_rect_x;
            float max = slider_rect_x + slider_rect_width;
            float mouse_x = U.clamp(mouse.x,min,max);
            current_value = U.remap(mouse_x,min,max,0,1);
        }

        rect.minY = slider_rect_y - slider_rect_height;
        rect.maxY = slider_rect_y;
        if (current_value <= 0) {
            rect.minX = x;
            rect.maxX = x + slider_rect_height;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
            rect.minX = slider_rect_x;
            rect.maxX = slider_rect_x + slider_rect_width;
            renderer.drawElement(rect,color_slider_background,id);
            rect.minX = rect.maxX - slider_circle_radius;
            rect.maxX = rect.maxX + slider_circle_radius;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
        } else if (current_value >= 1) {
            rect.minX = x;
            rect.maxX = x + slider_rect_height;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_default,id);
            rect.minX = slider_rect_x;
            rect.maxX = slider_rect_x + slider_rect_width;
            renderer.drawElement(rect,color_slider_default,id);
            rect.minX = rect.maxX - slider_circle_radius;
            rect.maxX = rect.maxX + slider_circle_radius;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_default,id);
        } else {
            float slider_pos = slider_rect_x + slider_rect_width * current_value;
            rect.minX = slider_pos;
            rect.maxX = slider_rect_x + slider_rect_width;
            renderer.drawElement(rect,color_slider_background,id);
            rect.minX = rect.maxX - slider_circle_radius;
            rect.maxX = rect.maxX + slider_circle_radius;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
            rect.minX = slider_rect_x;
            rect.maxX = slider_pos;
            renderer.drawElement(rect,color_slider_default,id);
            rect.minX = slider_pos - slider_circle_radius;
            rect.maxX = slider_pos + slider_circle_radius;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_default,id);
            rect.minX = slider_rect_x - slider_circle_radius;
            rect.maxX = slider_rect_x + slider_circle_radius;
            renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_default,id);
        }
    }

    public void render2(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        float w = currentSize.width() - 2;
        float h = currentSize.height() - 2;
        if (iPressed(Mouse.LEFT)) { context.focus();
            Vector2f mouse = context.mouse_position(MathLib.vec2());
            float min = x + (h/2f) + 1;
            float max = x + w -(h/2f) - 1;
            float mouse_x = U.clamp(mouse.x,min,max);
            current_value = U.remap(mouse_x,min,max,0,1);
        }

        Rectanglef quad = MathLib.rectf(x,y - currentSize.height(),x + currentSize.width(),y);
        float slider_pos = x + 1 + current_value * (w - h);



        if (iPressed(Mouse.LEFT) || (iHovered() &! iAnyInteractablePressed())) {

            renderer.drawOutline(quad,1,Color.rgb_to_intBits(GUI.color_slider_active),id);

            quad.minY += 1;
            quad.maxY -= 1;
            quad.minX = x + 1;
            quad.maxX = slider_pos + (h/2f);

            renderer.drawElement(quad,Color.rgb_to_intBits(GUI.color_slider_inactive),id);

            quad.minX = quad.maxX;
            quad.maxX = x + currentSize.width() - 1;

            renderer.drawElement(quad,Color.rgb_to_intBits(GUI.color_slider_background),id);

            quad.minX = slider_pos;
            quad.maxX = slider_pos + h;

            TextureAtlas default_icons = GUI.default_icons;
            Texture icons_diffuse_texture = default_icons.texture(0);
            TextureRegion circle_region = default_icons.getRegion(GUI.icon_default_circle);
            renderer.drawElement(icons_diffuse_texture,circle_region,quad,Color.rgb_to_intBits(GUI.color_slider_active),id);


        } else {


            renderer.drawOutline(quad,1,0x00000000,id,true);
            quad.minY += 1;
            quad.maxY -= 1;
            quad.minX = x + 1;
            quad.maxX = slider_pos + (h/2f);

            renderer.drawElement(quad,Color.rgb_to_intBits(GUI.color_slider_inactive),id);

            quad.minX = quad.maxX;
            quad.maxX = x + currentSize.width() - 1;

            renderer.drawElement(quad,Color.rgb_to_intBits(GUI.color_slider_background),id);

            quad.minX = slider_pos;
            quad.maxX = slider_pos + h;

            TextureAtlas default_icons = GUI.default_icons;
            Texture icons_diffuse_texture = default_icons.texture(0);
            TextureRegion circle_region = default_icons.getRegion(GUI.icon_default_circle);
            renderer.drawElement(icons_diffuse_texture,circle_region,quad,Color.rgb_to_intBits(GUI.color_slider_knob),id);

        }



    }


    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        render3(context, renderer, x, y, dt, parent_id);

        /*
        float padding = 1f;
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



        if (iPressed(Mouse.LEFT) || (iHovered() &! iAnyInteractablePressed())) {
            float t = 0f;
            if (iPressed(Mouse.LEFT)) {
                t = iPressedDuration();
            } if (iHovered()) {
                t = max(t,iHoveredDuration());
            } t = quadratic_erase_out(clamp(t * 4.0f));

            renderer.drawElement(quad,Color.rgb_to_intBits(color_bg),id);
            quad.maxX = lerp(x + h, x + w,current_value);

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
            TextureAtlas default_icons = GUI.default_icons;
            Texture icons_diffuse_texture = default_icons.texture(0);
            TextureRegion circle_region = default_icons.getRegion(GUI.icon_default_circle);
            renderer.drawElement(icons_diffuse_texture,circle_region,quad,0xFFFFFFFF,id);
            //renderer.drawElement(quad,0xFFFFFFFF,id);
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
