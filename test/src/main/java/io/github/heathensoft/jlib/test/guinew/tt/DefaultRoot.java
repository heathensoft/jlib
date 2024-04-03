package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public abstract class DefaultRoot extends RootContainer {


    // Do test of Array VS. Map. lookup times. Make Texture Atlas store Array instead of map if num regions is low?

    private final static String BORDER_EDGE_TOP = "box_window_border_edge_top_marble_10px";
    private final static String BORDER_EDGE_RIGHT = "box_window_border_edge_right_marble_10px";
    private final static String BORDER_EDGE_BOTTOM = "box_window_border_edge_bottom_marble_10px";
    private final static String BORDER_EDGE_LEFT = "box_window_border_edge_left_marble_10px";
    private final static String BORDER_CORNER_TOP_RIGHT = "box_window_border_corner_tr_marble_10px";
    private final static String BORDER_CORNER_TOP_LEFT = "box_window_border_corner_tl_marble_10px";
    private final static String BORDER_CORNER_BOTTOM_RIGHT = "box_window_border_corner_br_marble_10px";
    private final static String BORDER_CORNER_BOTTOM_LEFT = "box_window_border_corner_bl_marble_10px";

    public DefaultRoot() throws Exception {
        VBoxContainer vBox = new VBoxContainer();
        NavBar nav = new NavBar(0xFF232323,18);
        vBox.setInnerSpacing(3);
        setBorderPadding(5);
        vBox.addBoxes(nav, createContent());
        addBox(vBox);
        build();
        iID = iObtainID();
    }

    protected abstract Box createContent() throws Exception;


    protected void onWindowInitContainer(BoxWindow boxWindow, BoxContainer parent) {

    }

    protected void onWindowOpenContainer(BoxWindow boxWindow) {

    }

    protected void onWindowCloseContainer(BoxWindow boxWindow) {

    }

    protected void onWindowPrepare(BoxWindow window, float dt) {

    }

    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(U.rectf(),x,y);
        quad.minX += border_padding;
        quad.maxX -= border_padding;
        quad.minY += border_padding;
        quad.maxY -= border_padding;
        renderer.drawElement(quad,0xFF000000, iID);
        bounds(quad,x,y);
        renderWindowBorder(quad,renderer);
        processRootInteraction(window,x,y);
    }

    private void renderWindowBorder(Rectanglef bounds, RendererGUI renderer) {
        if (border_padding > 0) {
            int color = 0xFF666666;
            float window_width = bounds.lengthX();
            float window_height = bounds.lengthY();
            float padding_sum = border_padding * 2;
            if (window_width > padding_sum && window_height > padding_sum) {
                TextureAtlas atlas = GUI.default_gadgets;
                Texture texture = atlas.texture(0);
                TextureRegion edge_top = atlas.getRegion(BORDER_EDGE_TOP);
                TextureRegion edge_left = atlas.getRegion(BORDER_EDGE_LEFT);
                TextureRegion edge_right = atlas.getRegion(BORDER_EDGE_RIGHT);
                TextureRegion edge_bottom = atlas.getRegion(BORDER_EDGE_BOTTOM);
                TextureRegion corner_top_left = atlas.getRegion(BORDER_CORNER_TOP_LEFT);
                TextureRegion corner_top_right = atlas.getRegion(BORDER_CORNER_TOP_RIGHT);
                TextureRegion corner_bottom_left = atlas.getRegion(BORDER_CORNER_BOTTOM_LEFT);
                TextureRegion corner_bottom_right = atlas.getRegion(BORDER_CORNER_BOTTOM_RIGHT);
                Rectanglef rect = U.rectf();
                rect.maxY = bounds.maxY;
                rect.minY = rect.maxY - border_padding;
                rect.minX = bounds.minX;
                rect.maxX = rect.minX + border_padding;
                renderer.drawElement(texture,corner_top_left,rect,color, iID);
                rect.translate(window_width - border_padding,0f);
                renderer.drawElement(texture,corner_top_right,rect,color,iID);
                rect.translate(0,- (window_height - border_padding));
                renderer.drawElement(texture,corner_bottom_right,rect,color,iID);
                rect.translate(- (window_width - border_padding),0f);
                renderer.drawElement(texture,corner_bottom_left,rect,color,iID);
                rect.translate(0,border_padding);
                rect.maxY = rect.minY + (window_height - padding_sum);
                renderer.drawElement(texture,edge_left,rect,color,iID);
                rect.translate(window_width - border_padding,0);
                renderer.drawElement(texture,edge_right,rect,color,iID);
                rect.maxY += border_padding;
                rect.minY = rect.maxY - border_padding;
                rect.minX = bounds.minX + border_padding;
                rect.maxX = bounds.maxX - border_padding;
                renderer.drawElement(texture,edge_top,rect,color, iID);
                rect.translate(0,-(window_height - border_padding));
                renderer.drawElement(texture,edge_bottom,rect,color,iID);
            }
        }
    }

    private static final class NavBar extends Box {

        private final int color;
        private final static int CLOSE_BTN_SIZE = 18;
        private final static String CLOSE_BTN = "sharp_close_white_36dp";
        private final static int CLOSE_BTN_COLOR_REST = 0xFFffffff;
        private final static int CLOSE_BTN_COLOR_HOT = 0xFF2222EE;

        public NavBar(int color, float height) {
            this.color = color;
            this.desired_width = CLOSE_BTN_SIZE;
            this.desired_height = Math.max(height,CLOSE_BTN_SIZE);
            this.locked_vertical = true;
            this.iID = iObtainID();
        }

        protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {


            Rectanglef quad = bounds(U.rectf(),x,y);
            renderer.drawElement(quad,color,parent_id);
            TextureRegion region = GUI.default_icons.getRegion(CLOSE_BTN);
            Texture texture = GUI.default_icons.texture(0);
            quad.minX = quad.maxX - CLOSE_BTN_SIZE;
            quad.minY = quad.maxY - CLOSE_BTN_SIZE;

            int close_btn_color;
            if (iHovered() || iPressed(Mouse.LEFT)) {
                close_btn_color = CLOSE_BTN_COLOR_HOT;
            } else { close_btn_color = CLOSE_BTN_COLOR_REST; }
            if (iClicked(Mouse.LEFT)) {

                GUI.windows.closeWindow(window);
            }
            renderer.drawElement(texture,region,quad,close_btn_color, iID,true);
        }


    }

}
