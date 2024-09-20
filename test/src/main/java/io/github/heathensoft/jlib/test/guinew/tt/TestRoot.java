package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.box.HBoxContainer;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 23/05/2024
 */


public class TestRoot extends DefaultRoot {


    public TestRoot() throws Exception {
        super();
    }

    protected Box createContent() throws Exception {
        HBoxContainer container = new HBoxContainer();
        EmptyBox box1 = new EmptyBox(200,200,0xFFAAAAFF);
        EmptyBox box2 = new EmptyBox(Float.MIN_NORMAL,200,0xFFEE0077);
        box1.lockHorizontal();
        box1.lockVertical();
        container.addBoxes(box1,box2);
        return container;
    }

    private static final class EmptyBox extends Box {
        final int color;
        EmptyBox(float width, float height, int color) {
            this.desired_height = height;
            this.desired_width = width;
            this.color = color;
        }

        protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef bounds = bounds(U.popRect(),x,y);
            renderer.drawElement(bounds,color,parent_id,1.0f);
            U.pushRect();
        }
    }
}
