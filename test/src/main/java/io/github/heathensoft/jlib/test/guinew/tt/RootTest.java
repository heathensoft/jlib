package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.gui.ny.box.BoxWindow;
import io.github.heathensoft.jlib.gui.ny.box.RootContainer;
import io.github.heathensoft.jlib.gui.ny.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 24/02/2024
 */


public class RootTest extends RootContainer {


    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
        renderer.drawElement(quad,0x7F000000,interactable_id,0,true);
    }
}
