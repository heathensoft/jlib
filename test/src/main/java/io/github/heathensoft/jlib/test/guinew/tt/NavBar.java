package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;

/**
 * @author Frederik Dahl
 * 08/03/2024
 */


public class NavBar extends Box {

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
        this.interactable_id = iObtainID();
    }

    protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
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

        renderer.drawElement(texture,region,quad,close_btn_color,interactable_id,true);
    }


}
