package io.github.heathensoft.jlib.gui.dev;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.window.Box;
import io.github.heathensoft.jlib.gui.window.WindowGUI;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.quadraticEraseOut;
import static java.lang.Math.max;

/**
 * @author Frederik Dahl
 * 25/11/2023
 */


public class Slider extends Box {

    protected float current_value;
    protected float slider_height;
    protected int id;

    public Slider(float height) {
        this(height,height);
    }
    public Slider(float box_height, float slider_height) {
        this(0,box_height,slider_height);
    }

    public Slider(float min_box_width, float box_height, float slider_height) {
        box_height = max(3,box_height);
        this.slider_height = clamp(slider_height,1,box_height);
        this.restingSize.set(max(min_box_width,2 * box_height),box_height);
        this.currentSize.set(restingSize());
        this.id = iObtainID();
        this.lockedVertical = true;
        this.current_value = 0;
    }


    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        float bounds_width = currentSize.width();
        float bounds_height = currentSize.height();
        float slider_width = bounds_width - slider_height;
        float slider_circle_radius = slider_height / 2f;
        float slider_rect_x = x + slider_circle_radius;
        float slider_rect_y = y - (bounds_height / 2f) + (slider_height / 2f);
        Rectanglef rect = U.rectf();
        TextureAtlas default_icons = GUI.default_icons;
        Texture icons_diffuse_texture = default_icons.texture(0);
        TextureRegion circle_region = default_icons.getRegion(GUI.icon_default_circle);
        if (iPressed(Mouse.LEFT)) { context.focus();
            Vector2f mouse = context.mouse_position(U.vec2());
            float max = slider_rect_x + slider_width;
            float mouse_x = U.clamp(mouse.x, slider_rect_x,max);
            current_value = U.remap(mouse_x, slider_rect_x,max,0,1);
        }

        if (iPressed(Mouse.LEFT) || (iHovered() &! iAnyInteractablePressed())) { // Hot
            int color_slider_knob = Color.rgb_to_intBits(GUI.color_slider_knob);
            rect.minY = slider_rect_y - slider_height;
            rect.maxY = slider_rect_y;
            if (current_value <= 0) {
                int color_slider_background = Color.rgb_to_intBits(GUI.color_slider_background);
                rect.minX = slider_rect_x;
                rect.maxX = slider_rect_x + slider_width;
                renderer.drawElement(rect,color_slider_background,id);
                rect.minX = rect.maxX - slider_circle_radius;
                rect.maxX = rect.maxX + slider_circle_radius;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
                rect.minX = x;
                rect.maxX = x + slider_height;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_knob,id);

            } else { float t = 0f;
                if (iPressed(Mouse.LEFT)) { t = iPressedDuration();
                } if (iHovered()) { t = max(t,iHoveredDuration());
                } t = quadraticEraseOut(clamp(t * 4.0f));
                int color_slider_active = Color.rgb_to_intBits(Color.lerp(
                        GUI.color_slider_inactive,
                        GUI.color_slider_active,
                        t, U.vec4()
                ));

                if (current_value >= 1) {
                    rect.minX = x;
                    rect.maxX = x + slider_height;
                    renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_active,id);
                    rect.minX = slider_rect_x;
                    rect.maxX = slider_rect_x + slider_width;
                    renderer.drawElement(rect,color_slider_active,id);
                    rect.minX = rect.maxX - slider_circle_radius;
                    rect.maxX = rect.maxX + slider_circle_radius;
                } else {
                    int color_slider_background = Color.rgb_to_intBits(GUI.color_slider_background);
                    float slider_pos = slider_rect_x + slider_width * current_value;
                    rect.minX = slider_pos;
                    rect.maxX = slider_rect_x + slider_width;
                    renderer.drawElement(rect,color_slider_background,id);
                    rect.minX = rect.maxX - slider_circle_radius;
                    rect.maxX = rect.maxX + slider_circle_radius;
                    renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
                    rect.minX = slider_rect_x;
                    rect.maxX = slider_pos;
                    renderer.drawElement(rect,color_slider_active,id);
                    rect.minX = slider_rect_x - slider_circle_radius;
                    rect.maxX = slider_rect_x + slider_circle_radius;
                    renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_active,id);
                    rect.minX = slider_pos - slider_circle_radius;
                    rect.maxX = slider_pos + slider_circle_radius;
                } renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_knob,id);
            }
        } else {
            int color_slider_inactive = Color.rgb_to_intBits(GUI.color_slider_inactive);
            int color_slider_background = Color.rgb_to_intBits(GUI.color_slider_background);
            rect.minY = slider_rect_y - slider_height;
            rect.maxY = slider_rect_y;
            if (current_value <= 0) {
                rect.minX = x;
                rect.maxX = x + slider_height;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
                rect.minX = slider_rect_x;
                rect.maxX = slider_rect_x + slider_width;
                renderer.drawElement(rect,color_slider_background,id);
                rect.minX = rect.maxX - slider_circle_radius;
                rect.maxX = rect.maxX + slider_circle_radius;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
            } else if (current_value >= 1) {
                rect.minX = x;
                rect.maxX = x + slider_height;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_inactive,id);
                rect.minX = slider_rect_x;
                rect.maxX = slider_rect_x + slider_width;
                renderer.drawElement(rect,color_slider_inactive,id);
                rect.minX = rect.maxX - slider_circle_radius;
                rect.maxX = rect.maxX + slider_circle_radius;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_inactive,id);
            } else {
                float slider_pos = slider_rect_x + slider_width * current_value;
                rect.minX = slider_pos;
                rect.maxX = slider_rect_x + slider_width;
                renderer.drawElement(rect,color_slider_background,id);
                rect.minX = rect.maxX - slider_circle_radius;
                rect.maxX = rect.maxX + slider_circle_radius;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_background,id);
                rect.minX = slider_rect_x;
                rect.maxX = slider_pos;
                renderer.drawElement(rect,color_slider_inactive,id);
                rect.minX = slider_pos - slider_circle_radius;
                rect.maxX = slider_pos + slider_circle_radius;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_inactive,id);
                rect.minX = slider_rect_x - slider_circle_radius;
                rect.maxX = slider_rect_x + slider_circle_radius;
                renderer.drawElement(icons_diffuse_texture,circle_region,rect,color_slider_inactive,id);
            }
        }
    }

    public float value() { return current_value; }

    public void setValue(float value) { this.current_value = clamp(value); }

    public int interactableID() { return id; }
}
