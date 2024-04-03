package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public class ToggleTest extends DefaultRoot {


    public ToggleTest() throws Exception { super(); }

    protected Box createContent() throws Exception {
        TBoxContainer tBox = new TBoxContainer();
        Button button_next = new Button(tBox::toggleNext);
        Button button_prev = new Button(tBox::togglePrevious);
        tBox.addBoxes(
                new BoxTest(200,100,0xFFFF0000),
                new BoxTest(200,100,0xFF00FF00),
                new BoxTest(200,100,0x00FF00FF)
        );


        HBoxContainer hBox = new HBoxContainer();
        hBox.addBoxes(button_prev,button_next);
        hBox.setInnerSpacing(3);
        VBoxContainer vBox = new VBoxContainer();
        vBox.addBoxes(hBox,tBox);
        vBox.setInnerSpacing(3);


        return vBox;
    }


    protected void onWindowInitContainer(BoxWindow boxWindow, BoxContainer parent) {

    }

    private static final class BoxTest extends Box {
        private final int color;
        public BoxTest(float width, float height, int color) {
            this.color = color;
            this.desired_width = width;
            this.desired_height = height;
        }

        protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef bounds = bounds(U.rectf(),x,y);
            renderer.drawElement(bounds,color,parent_id);
        }
    }

    private static final class Button extends Box {
        private final Click click;
        public Button(Click onClick) {
            this.click = onClick;
            this.iID = iObtainID();
            this.desired_height = 18;
            this.desired_width = 36;
            this.locked_vertical = true;
        }
        public interface Click {  void onClick(); }

        protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef bounds = bounds(U.rectf(),x,y);
            int color;
            final int color_inactive = 0xFF444444;
            final int color_pressed = 0xFFAAAAAA;
            if (iClicked(Mouse.LEFT)) {
                click.onClick();
                color = color_pressed;
            } else if (iPressed(Mouse.LEFT)) {
                color = color_pressed;
            } else color = color_inactive;
            renderer.drawElement(bounds,color,iID);
        }


    }
}
