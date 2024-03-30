package io.github.heathensoft.jlib.gui.dev;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.text.TextOld;
import io.github.heathensoft.jlib.gui.window.Box;
import io.github.heathensoft.jlib.gui.window.WindowGUI;
import io.github.heathensoft.jlib.common.utils.Color;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;


/**
 * @author Frederik Dahl
 * 15/11/2023
 */


public class TextBox extends Box {

    private TextOld text;
    private final Vector4f background_color;
    private float padding;
    private boolean use_parent_id;

    public TextBox(Vector4f background_color, int width, int height, int padding) {
        this.background_color = background_color;
        this.restingSize.set(width,height);
        this.currentSize.set(restingSize());
        this.padding = Math.max(0, padding);
        this.use_parent_id = true;
    }

    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        int id = use_parent_id ? parent_id : 0;
        Rectanglef bounds = bounds(U.rectf(),x,y);
        renderer.drawElement(bounds, Color.rgb_to_intBits(background_color),id);
    }

    public void renderText(RendererGUI renderer, float x, float y) {
        if (text != null && !text.isBlank()) {
            Rectanglef bounds = bounds(U.rectf(),x,y);
            if (padding > 0) {
                bounds.minX += padding;
                bounds.minY += padding;
                bounds.maxX -= padding;
                bounds.maxY -= padding;
            } renderer.drawText(text,bounds);
        }
    }

    public void setText(TextOld text) { this.text = text; }

    public TextOld text() { return text; }

    public Vector4f backgroundColor() {
        return background_color;
    }

    public void setTextFieldPadding(float padding) { this.padding = Math.max(0,padding); }

    public void useParentID(boolean yes) {
        use_parent_id = yes;
    }
}
