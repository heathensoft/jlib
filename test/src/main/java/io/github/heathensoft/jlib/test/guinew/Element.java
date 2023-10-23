package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.gui.Interactable;
import io.github.heathensoft.jlib.gui.gfx.Fonts;
import io.github.heathensoft.jlib.gui.gfx.UIRenderer;
import io.github.heathensoft.jlib.gui.text.Paragraph;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 23/10/2023
 */


public class Element implements Interactable {

    private static final int text_size = 32;
    private Rectanglef bounds;
    private Color32 color;
    private int id;


    public Element(float x, float y, float w, float h, Color32 color) {
        bounds = new Rectanglef(x,y - h,x+w,y);
        this.color = color;
        this.id = iObtainID();
    }

    public void draw(UIRenderer renderer) {

        renderer.drawElement(bounds,color,id);
        Fonts fonts = renderer.fonts();
        float scale = fonts.relativeScale(text_size);
        float line_height = (fonts.ascent() + fonts.descent() + fonts.lineGap()) * scale;
        float x0 = bounds.minX + 16;
        float y0 = bounds.maxY - 16;
        String duration;

        if (iIsHovered()) {
            duration = String.format("%.2f", iHoveredDuration());
            renderer.drawLine(new Paragraph("Hovered Duration: " + duration),x0,y0,800,0,text_size,false);
            y0 -= line_height;
        }
        if (iIsPressed(Mouse.LEFT)) {
            duration = String.format("%.2f", iPressedDuration());
            renderer.drawLine(new Paragraph("Pressed Duration:" + duration),x0,y0,800,0,text_size,false);
            y0 -= line_height;
            if (iIsGrabbed()) {
                duration = String.format("%.2f", iPressedDuration());
                renderer.drawLine(new Paragraph("Grabbed: :" + duration),x0,y0,800,0,text_size,false);
                y0 -= line_height;

            }
        }



    }

    public int interactableID() {
        return id;
    }
}
