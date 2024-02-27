package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.gui.ny.box.Box;
import io.github.heathensoft.jlib.gui.ny.box.BoxWindow;
import io.github.heathensoft.jlib.gui.ny.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 23/02/2024
 */


public class BoxTest extends Box {

    private int color;

    public BoxTest(float width, float height) {
        this.desired_width = width;
        this.desired_height = height;
        this.color = Color.rgb_to_intBits(Color.random_opaque(MathLib.vec4()));
        this.interactable_id = iObtainID();
    }


    protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef bounds = bounds(MathLib.rectf(),x,y);
        renderer.drawElement(bounds,Color.WHITE_BITS,parent_id);
        //renderer.drawOutline(bounds,2,Color.BLACK_BITS,parent_id);
    }
}
