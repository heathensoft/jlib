package io.github.heathensoft.jlib.ui.box.scroll;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.gfx.BackGround;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

/**
 *
 * @author Frederik Dahl
 * 18/04/2024
 */


public abstract class ScrollBox extends Box {

    protected float content_offset;
    protected float scrollbar_grab;
    protected float scrollbar_fade;
    protected float scrollbar_width = GUI.variables.scrollbar_default_width;
    protected Vector4f scrollbar_color = GUI.variables.scrollbar_default_color;
    protected Interactable scrollbar = new Instance();
    protected BackGround background;

    protected abstract float boxPaddingTop();

    protected abstract float boxPaddingRight();

    protected abstract float boxPaddingBottom();

    protected abstract float boxPaddingLeft();

    protected float boxPaddingVertical() { return boxPaddingTop() + boxPaddingBottom(); }

    protected float boxPaddingHorizontal() { return boxPaddingLeft() + boxPaddingRight(); }

    protected float contentOffsetY() { return content_offset; }

    protected float boxHeightPadded() { return current_height - boxPaddingVertical(); }

    protected float boxWidthPadded() { return current_width - boxPaddingHorizontal(); }

    protected float scrollbarWidth() { return scrollbar_width; }

    protected float wiggleRoomScroll(float contentHeight, float windowHeight) { return Math.max(0,contentHeight - windowHeight); }

    protected float normalizedHeight(float contentHeight, float windowHeight) { return windowHeight / Math.max(windowHeight,contentHeight); }

    protected float normalizedPosition(float windowWiggleRoom) { return windowWiggleRoom <= 0 ? 0 : content_offset / windowWiggleRoom; }

    public BackGround background() { return background; }

    public void setBackground(BackGround background) { this.background = background; }

    public Vector4f scrollbarColor() { return scrollbar_color; }

    public void setScrollbarColor(Vector4f color) { this.scrollbar_color = color; }

    public void setScrollbarWidth(float width) { scrollbar_width = Math.max(0,width); }

    /**Process any scrollbar events and returns true if there was an event*/
    protected boolean processScrollBarEvents(float item_height, float content_height) {
        if (scrollbar.iHovered() && GUI.mouse.scrolled()) {
            windowScroll(GUI.mouse.get_scroll() * item_height,content_height);
            return true;
        } if (scrollbar.iGrabbed(Mouse.LEFT)) {
            float box_padded_height = boxHeightPadded();
            float content_wiggle = Math.max(0,content_height - box_padded_height);
            if (content_wiggle > 0) {
                float normalized_position;
                float normalized_height = box_padded_height / Math.max(box_padded_height,content_height);
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
            float box_padded_height = boxHeightPadded();
            float content_wiggle = Math.max(0,content_height - box_padded_height);
            if (content_wiggle > 0) {
                float normalized_height = box_padded_height / Math.max(box_padded_height,content_height);
                float normalized_position = content_offset / content_wiggle;
                float scrollbar_height_max = currentHeight() - scrollbar_width;
                float scrollbar_height = scrollbar_height_max * normalized_height;
                float scrollbar_wiggle = scrollbar_height_max - scrollbar_height;
                scrollbar_grab = scrollbar_wiggle * normalized_position;
                return true;
            }
        } return false;
    }

    protected void windowScroll(float dy, float content_height) {
        if (dy > 0 && content_offset > 0) {
            content_offset = Math.max(0,content_offset - dy);
        } else if (dy < 0) {
            float box_height_padded = boxHeightPadded();
            float wiggle_room = Math.max(0,content_height - box_height_padded);
            if (wiggle_room > 0) {
                wiggle_room -= content_offset;
                content_offset += Math.min(wiggle_room,-dy);
            }
        }
    }

    protected void renderScrollbar(RendererGUI renderer, float content_height, float x, float y, float dt) {
        float current_scrollbar_width = scrollbar_width;
        float box_height_padded = boxHeightPadded();
        float content_wiggle = Math.max(0,content_height - box_height_padded);
        boolean scrollbar_visible = content_wiggle > 0;
        scrollbar_fade = U.clamp(scrollbar_fade,0,1);
        if (scrollbar_visible) {
            current_scrollbar_width *= U.smooth(scrollbar_fade);
            if (scrollbar_fade < 1.0f) {
                dt *= GUI.variables.scrollbar_fade_mod;
                scrollbar_fade += dt;
            }
        } else { current_scrollbar_width *= 1 - (U.smooth(1 - scrollbar_fade));
            if (scrollbar_fade > 0.0f) {
                dt *= GUI.variables.scrollbar_fade_mod;
                scrollbar_fade -= dt;
            }
        } if (current_scrollbar_width > 0) {
            Rectanglef bounds = bounds(U.popRect(),x,y);
            bounds.minX = bounds.maxX - current_scrollbar_width;
            float alpha = scrollbar_color.w;
            scrollbar_color.w *= (current_scrollbar_width / scrollbar_width);
            int color = Color.rgb_to_intBits(scrollbar_color);
            scrollbar_color.w = alpha;
            float glow = scrollbar.iHovered() || scrollbar.iPressed(Mouse.LEFT) ? GUI.variables.scrollbar_glow : 0.0f;
            float normalized_height = box_height_padded / Math.max(box_height_padded,content_height);
            float scrollbar_height_max = bounds.lengthY() - current_scrollbar_width;
            float scrollbar_height = normalized_height * scrollbar_height_max;
            float scrollbar_wiggle = scrollbar_height_max - scrollbar_height;
            content_offset = U.clamp(content_offset,0,content_wiggle);
            float normalized_position = content_wiggle <= 0 ? 0 : content_offset / content_wiggle;
            float scrollbar_position = scrollbar_wiggle * normalized_position;
            bounds.maxY -= scrollbar_position;
            bounds.minY = bounds.maxY - (scrollbar_height + scrollbar_width);
            renderer.drawElement(bounds,color, scrollbar.interactableID(),glow);
            bounds.minX -= 2f;
            renderer.drawElement(bounds,0, scrollbar.interactableID(),0.0f);
            U.pushRect();
        }
    }

    protected void renderBackground(RendererGUI renderer, Rectanglef bounds, int id, float dt) {
        if (background == null) renderer.drawElement(bounds,0x00000000,id);
        else background.render(renderer,bounds,id,dt);
    }

    protected Rectanglef contentBounds(Rectanglef dst, float x, float y) {
        bounds(dst,x,y);
        dst.maxX -= boxPaddingRight();
        dst.minY += boxPaddingBottom();
        dst.minX += boxPaddingLeft();
        dst.maxY -= boxPaddingTop();
        return dst;
    }

    protected Rectanglef contentBounds(Rectanglef bounds, Rectanglef dst) {
        dst.set(bounds);
        dst.maxX -= boxPaddingRight();
        dst.minY += boxPaddingBottom();
        dst.minX += boxPaddingLeft();
        dst.maxY -= boxPaddingTop();
        return dst;
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
