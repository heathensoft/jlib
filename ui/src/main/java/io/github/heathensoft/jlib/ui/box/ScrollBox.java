package io.github.heathensoft.jlib.ui.box;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.gfx.BackGround;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 09/04/2024
 */


public abstract class ScrollBox extends Box {


    protected float content_offset;
    protected float scrollbar_width;
    protected float scrollbar_grab;
    protected float scrollbar_fade;
    protected Interactable scrollbar;
    protected BackGround background;
    protected Vector4f scrollbar_color;


    /**The desired height of the content*/
    protected abstract float contentHeight();

    protected abstract float contentPaddingTop();

    protected abstract float contentPaddingRight();

    protected abstract float contentPaddingBottom();

    protected abstract float contentPaddingLeft();

    protected float contentPaddingVertical() { return contentPaddingTop() + contentPaddingBottom(); }

    protected float contentPaddingHorizontal() { return contentPaddingLeft() + contentPaddingRight(); }

    protected float contentOffsetY() { return content_offset; }

    protected float virtualWindowHeight() { return current_height - contentPaddingVertical(); }

    protected float virtualWindowWidth() { return current_width - contentPaddingHorizontal(); }

    protected float scrollbarWidth() { return scrollbar_width; }

    public void setScrollbarWidth(float width) { scrollbar_width = Math.max(4,width); }

    protected float windowWiggleRoom(float contentHeight, float windowHeight) { return Math.max(0,contentHeight - windowHeight); }

    protected float normalizedHeight(float contentHeight, float windowHeight) { return windowHeight / Math.max(windowHeight,contentHeight); }

    protected float normalizedPosition(float windowWiggleRoom) { return windowWiggleRoom <= 0 ? 0 : content_offset / windowWiggleRoom; }

    public BackGround background() { return background; }

    public void setBackground(BackGround background) { this.background = background; }

    public Vector4f scrollbarColor() { return scrollbar_color; }

    public void setScrollbarColor(Vector4f color) { this.scrollbar_color = color; }

    protected void clampContentOffset(float windowWiggleRoom) { content_offset = U.clamp(content_offset,0,windowWiggleRoom); }

    protected void renderBackground(RendererGUI renderer, Rectanglef bounds, int id, float dt) {
        if (background == null) renderer.drawElement(bounds,0x00000000,id);
        else background.render(renderer,bounds,id,dt);
    }

    /**Can add more of these to allow for custom scrollbars*/
    protected void renderScrollbar(RendererGUI renderer, Rectanglef bounds, float dt) {
        float width = scrollbar_width;
        float window_height = bounds.lengthY() - contentPaddingVertical();
        float content_height = contentHeight();
        float content_wiggle = Math.max(0,content_height - window_height);
        boolean scrollbar_visible = content_wiggle > 0;
        scrollbar_fade = U.clamp(scrollbar_fade,0,1);
        if (scrollbar_visible) {
            width *= U.smooth(scrollbar_fade);
            if (scrollbar_fade < 1.0f) {
                dt *= GUI.variables.scrollbar_fade_mod;
                scrollbar_fade += dt;
            }
        } else {
            width *= 1 - (U.smooth(1 - scrollbar_fade));
            if (scrollbar_fade > 0.0f) {
                dt *= GUI.variables.scrollbar_fade_mod;
                scrollbar_fade -= dt;
            }
        } if (width > 0) {
            bounds.minX = bounds.maxX - width;
            float alpha = width / scrollbar_width;
            Vector4f rgb = U.popVec4();
            if (alpha < 1.0f) {
                rgb.set(scrollbar_color);
                rgb.w *= alpha;
            } else rgb = scrollbar_color;
            int color = Color.rgb_to_intBits(rgb);
            U.pushVec4();

            float normalized_height = window_height / Math.max(window_height,content_height);
            float scrollbar_height_max = bounds.lengthY() - width;
            float scrollbar_height = normalized_height * scrollbar_height_max;
            float scrollbar_wiggle = scrollbar_height_max - scrollbar_height;
            content_offset = U.clamp(content_offset,0,content_wiggle);
            float normalized_position = content_wiggle <= 0 ? 0 : content_offset / content_wiggle;
            float scrollbar_position = scrollbar_wiggle * normalized_position;
            bounds.maxY -= scrollbar_position;
            bounds.minY = bounds.maxY - (scrollbar_height + scrollbar_width);
            renderer.drawElement(bounds,color, scrollbar.interactableID(),0.0f);
        }
    }

    /**Process any scrollbar events and returns true if there was an event*/
    protected boolean processScrollBarEvents(float item_height) {
        if (scrollbar.iHovered() && GUI.mouse.scrolled()) {
            windowScroll(GUI.mouse.get_scroll() * item_height);
            return true;
        } if (scrollbar.iGrabbed(Mouse.LEFT)) {
            float window_height = virtualWindowHeight();
            float content_height = contentHeight();
            float content_wiggle = Math.max(0,content_height - window_height);
            if (content_wiggle > 0) {
                float normalized_position;
                float normalized_height = window_height / Math.max(window_height,content_height);
                float scrollbar_height_max = currentHeight() - scrollbar_width;
                float scrollbar_height = scrollbar_height_max * normalized_height;
                float scrollbar_wiggle = scrollbar_height_max - scrollbar_height;
                float mouse_drag_y = GUI.mouseDragVector(U.popVec2(),Mouse.LEFT).y;
                U.pushVec2();
                float scrollbar_position = scrollbar_grab - mouse_drag_y;
                scrollbar_position = U.clamp(scrollbar_position,0,scrollbar_wiggle);
                normalized_position = scrollbar_position / scrollbar_wiggle;
                content_offset = content_wiggle * normalized_position;
                return true;
            }
        } else if (scrollbar.iJustPressed(Mouse.LEFT)) {
            float window_height = virtualWindowHeight();
            float content_height = contentHeight();
            float content_wiggle = Math.max(0,content_height - window_height);
            if (content_wiggle > 0) {
                float normalized_height = window_height / Math.max(window_height,content_height);
                float normalized_position = content_offset / content_wiggle;
                float scrollbar_height_max = currentHeight() - scrollbar_width;
                float scrollbar_height = scrollbar_height_max * normalized_height;
                float scrollbar_wiggle = scrollbar_height_max - scrollbar_height;
                scrollbar_grab = scrollbar_wiggle * normalized_position;
                return true;
            }
        } return false;
    }

    protected void windowScroll(float dy) {
        if (dy > 0 && content_offset > 0) {
            content_offset = Math.max(0,content_offset - dy);
        } else if (dy < 0) {
            float windowHeight = virtualWindowHeight();
            float contentHeight = contentHeight();
            float wiggleRoom = Math.max(0,contentHeight - windowHeight);
            if (wiggleRoom > 0) {
                wiggleRoom -= content_offset;
                content_offset += Math.min(wiggleRoom,-dy);
            }
        }
    }


    protected void closeBox() {
        scrollbar.iYieldFocus();
        iYieldFocus();
    }

    public void dispose() {
        super.dispose();
        scrollbar.dispose();
    }
}
