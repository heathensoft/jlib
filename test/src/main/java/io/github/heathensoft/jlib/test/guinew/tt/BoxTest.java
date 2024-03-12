package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 08/03/2024
 */


public class BoxTest extends Box {

    private final int color;

    public BoxTest(float width, float height, int abgr) {
        this.desired_width = width;
        this.desired_height = height;
        this.color = abgr;
    }


    protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
        renderer.drawElement(quad,color,parent_id);
    }
}
