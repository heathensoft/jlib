package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.window.RootContainer;
import io.github.heathensoft.jlib.gui.window.WindowGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 13/11/2023
 */


public class RootTest extends RootContainer {
    public RootTest() {
        super();
    }

    public RootTest(int padding) {
        super(padding);
    }

    public RootTest(int max_content_width, int max_content_height, int padding) {
        super(max_content_width, max_content_height, padding);
    }

    public void renderBackground(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(U.rectf(),x,y);
        renderer.drawElement(quad,0x7F000000,id,0,true);
    }
}
