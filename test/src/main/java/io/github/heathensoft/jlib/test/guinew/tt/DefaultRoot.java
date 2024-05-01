package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public abstract class DefaultRoot extends RootContainer {

    public static final Vector4f navbar_color = Color.hex_to_rgb("493737FF");
    public static final Vector4f border_color = Color.hex_to_rgb("7B5C5CFF");

    public DefaultRoot() throws Exception {
        VBoxContainer vBox = new VBoxContainer();
        NavBar nav = new NavBar(navbar_color,18);
        vBox.setInnerSpacing(3);
        setBorderPadding(6);
        vBox.addBoxes(nav, createContent());
        addBox(vBox);
        build();
        iID = iObtainID();
    }

    protected abstract Box createContent() throws Exception;

    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad =  bounds(U.popRect(),x,y);
        renderer.drawGadgetBorders(quad,border_padding,Color.rgb_to_intBits(border_color),iID);
        if (background == null) { renderer.drawElement(U.rectExpand(quad,-border_padding),0xFF000000, iID);
        } else background.render(renderer,U.rectExpand(quad,-border_padding),iID,dt);
        U.pushRect();
    }

    private static final class NavBar extends Box {


        private final Vector4f color;
        private final static int CLOSE_BTN_SIZE = 18;
        private final static int CLOSE_BTN_COLOR_REST = 0xFFffffff;
        private final static int CLOSE_BTN_COLOR_HOT = 0xFF2222EE;

        public NavBar(Vector4f color, float height) {
            this.color = color;
            this.desired_width = CLOSE_BTN_SIZE;
            this.desired_height = Math.max(height,CLOSE_BTN_SIZE);
            this.locked_vertical = true;
            this.iID = iObtainID();
        }

        protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef quad =  bounds(U.popRect(),x,y);
            renderer.drawElement(quad,Color.rgb_to_intBits(color),parent_id);
            quad.minX = quad.maxX - CLOSE_BTN_SIZE;
            quad.minY = quad.maxY - CLOSE_BTN_SIZE;
            int close_btn_color;
            float glow;
            if (iHovered() || iPressed(Mouse.LEFT)) {
                close_btn_color = CLOSE_BTN_COLOR_HOT;
                glow = 1.0f;
            } else {
                close_btn_color = CLOSE_BTN_COLOR_REST;
                glow = 0.0f;
            }
            if (iClicked(Mouse.LEFT)) {
                GUI.windows.closeWindow(window);
            } renderer.drawSprite(GUI.icons.close_window,quad,close_btn_color,iID,glow,true);
            U.pushRect();
        }


    }

}
