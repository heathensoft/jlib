package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.InputField;
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


    protected void initContainer(BoxWindow boxWindow, BoxContainer parent) {

    }

    private static final class BoxTest extends Box implements TextProcessor {
        private final int color;
        private final InputField inputField;
        public BoxTest(float width, float height, int color) {
            this.color = color;
            this.desired_width = width;
            this.desired_height = height;
            this.inputField = new InputField(System.out::println);
            this.iID = iObtainID();
        }

        protected void closeBox() { iYieldFocus(); }


        protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef bounds =  bounds(U.popRect(),x,y);
            renderer.drawElement(bounds,color,iID);
            if (iClicked(Mouse.LEFT)) iFocus();
            if (iHasFocus()) {
                if (!isActiveTextProcessor()) {
                    inputField.resetCursorTimer();
                    activateTextProcessor();
                }
            } else {
                if (isActiveTextProcessor()) {
                    deactivateTextProcessor();
                }
            } U.pushRect();
        }

        protected void renderBoxText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
            Rectanglef bounds = bounds(U.popRect(),x,y);
            float text_size = 32;
            float padding = 4;
            bounds.minX += padding;
            bounds.maxX -= padding;
            bounds.maxY -= padding;
            bounds.minY = bounds.maxY - text_size;
            renderer.fonts().bindFontMetrics(FontsGUI.SLOT_MONO);
            inputField.draw(renderer,bounds,0xFFFFFFFF,0,iHasFocus());
            U.pushRect();
        }

        public void keyPress(int key, int mods, boolean repeat) {
            inputField.keyPress(key, mods);
        }

        public void charPress(byte character) {
            inputField.charPress(character);
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

        protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef bounds =  bounds(U.popRect(),x,y);
            int color;
            final int color_inactive = 0xFF444444;
            //final int color_pressed = 0xFFAAAAAA;
            final int color_pressed = 0xFF444444;

            boolean pressed = false;

            if (iClicked(Mouse.LEFT)) {
                click.onClick();
                pressed = true;
                color = color_pressed;
            } else if (iPressed(Mouse.LEFT)) {
                pressed = true;
                color = color_pressed;
            } else color = color_inactive;

            renderer.drawGadgetButton(bounds,5,color,iID,0,pressed);

            if (iHovered()) {
                if (iHoveredDuration() >0.5) {
                    GUI.tooltips.display("Toggle Next Window",GUI.mousePosition(U.popVec2()));
                    U.pushVec2();
                }

            }
            U.pushRect();
        }


    }
}
