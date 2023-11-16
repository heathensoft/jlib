package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.gui.Interactable;
import io.github.heathensoft.jlib.gui.gfx.FontsGUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 23/10/2023
 */


public class Element implements Interactable {

    private static final float color_transition_duration = 0.27f;
    private static final int text_size = 32;
    private Rectanglef bounds;
    private Vector4f color1;
    private Vector4f color2;
    private int id;


    public Element(float x, float y, float w, float h, String color1, String color2) {
        bounds = new Rectanglef(x,y - h,x+w,y);
        this.color1 = Color.hex_to_rgb(color1,new Vector4f());
        this.color2 = Color.hex_to_rgb(color2,new Vector4f());
        this.id = iObtainID();
    }

    public void draw(RendererGUI renderer) {



        FontsGUI fonts = renderer.fonts();
        float scale = fonts.relativeScale(text_size);
        float line_height = (fonts.ascent() + fonts.descent() + fonts.lineGap()) * scale;
        float x0 = bounds.minX + 16;
        float y0 = bounds.maxY - 16;
        String duration_string;


        boolean hovered = iHovered();
        boolean pressed = iPressed(Mouse.LEFT);

        if (pressed) {

            renderer.drawElement(bounds,Color.rgb_to_intBits(color1),id);


        } else if (hovered) {

            if (iAnyInteractablePressed()) {
                renderer.drawElement(bounds,Color.rgb_to_intBits(color1),id);
            } else {
                float t = iHoveredDuration() / color_transition_duration;
                Vector4f rgb = Color.lerp(color1,color2,t);
                renderer.drawElement(bounds,Color.rgb_to_intBits(rgb),id);
            }


        } else {

            renderer.drawElement(bounds,Color.rgb_to_intBits(color1),id);

        }


        if (iHovered()) {

            duration_string = String.format("%.1f", iHoveredDuration());
            //renderer.drawParagraphDynamic(new Paragraph("Hovered Duration: " + duration_string),x0,y0,800,0,text_size,false,1);
            y0 -= line_height;
        }
        if (iPressed(Mouse.LEFT)) {

            duration_string = String.format("%.1f", iPressedDuration());
            //renderer.drawParagraphDynamic(new Paragraph("Pressed Duration: " + duration_string),x0,y0,800,0,text_size,false,1);
            y0 -= line_height;
            if (iGrabbed()) {
                //renderer.drawParagraphDynamic(new Paragraph("Grabbed"),x0,y0,800,0,text_size,false,1);
                y0 -= line_height;

            }
        }


    }

    public int interactableID() {
        return id;
    }
}
