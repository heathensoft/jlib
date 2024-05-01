package io.github.heathensoft.jlib.ui.box.scroll;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 18/04/2024
 */


public abstract class ItemList<T> extends ItemContainer<T> {

    protected boolean show_icons;
    protected float icon_text_spacing; // horizontal spacing between th icon and the text;
    protected float item_border_thickness; // thickness top/right/bottom/left for each item rectangle
    protected float max_advance; // maximum unscaled advance of all the items
    protected float desired_item_height = GUI.variables.gui_default_font_size;
    protected int font = GUI.variables.gui_default_font;

    public void setItemBorder(float thickness) { this.item_border_thickness = thickness; }
    public void setIconTextSpacing(float spacing) { this.icon_text_spacing = spacing; }
    public void setItemDesiredHeight(float height) { desired_item_height = height; }
    public void setFont(int font) { this.font = font; }
    public void showIcons(boolean show) { this.show_icons = show; }
    public float itemHeight() { return desired_item_height; }
    public int font() { return font; }



    protected abstract void renderItem(RendererGUI renderer, Rectanglef bounds, Item<T> item, float dt);

    protected abstract void renderItemText(RendererGUI renderer, Rectanglef bounds, Item<T> item, int font, float dt);


    protected void renderBoxText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
        refreshItemContainer();
        int num_items = numItemsFiltered();
        if (num_items > 0) {
            renderer.fonts().bindFontMetrics(font);
            float item_height = calculateItemHeight();
            float row_height = itemHeightPadded(item_height);
            float content_height = num_items * row_height;
            Rectanglef content_bounds, item_bounds;
            content_bounds = contentBounds(U.popRect(),x,y);
            item_bounds = U.popSetRect(content_bounds);
            item_bounds.translate(0,content_offset);
            item_bounds.minY = item_bounds.maxY - row_height;
            U.rectExpand(item_bounds,-item_border_thickness);
            if (show_icons) { item_bounds.minX += (item_height + icon_text_spacing);
            } renderer.pushScissor(content_bounds);
            for (Item<T> item : filtered_items) {
                if (item_bounds.maxY > content_bounds.minY && item_bounds.minY < content_bounds.maxY) {
                    renderItemText(renderer,item_bounds,item,font,dt);
                } item_bounds.translate(0,-row_height);
            } renderer.popScissor();
            U.pushRect(2);
        }
    }

    protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef bounds = bounds(U.popRect(),x,y);
        renderBackground(renderer,bounds,iID,dt);
        refreshItemContainer();
        int num_items = numItemsFiltered();
        if (num_items > 0) {
            float item_height = calculateItemHeight();
            float row_height = itemHeightPadded(item_height);
            float content_height = num_items * row_height;
            if (processScrollBarEvents(row_height,content_height)) {
                GUI.windows.focusRequest(window);
                scrollbar.iFocus();
            } Rectanglef content_bounds = contentBounds(bounds,U.popRect());
            Rectanglef item_bounds = U.popSetRect(content_bounds);
            item_bounds.translate(0,content_offset);
            item_bounds.minY = item_bounds.maxY - row_height;
            renderer.pushScissor(content_bounds);
            for (Item<T> item : filtered_items) {
                if (item_bounds.maxY > content_bounds.minY && item_bounds.minY < content_bounds.maxY) {


                    if (item.iHovered()) hovered_item = item;
                    if (item.iPressed(Mouse.LEFT)) {
                        if (item.iGrabbed(Mouse.LEFT) && grabbing_enabled) {
                            if (item.iJustGrabbed(Mouse.LEFT)) grabbed_item = item;
                            else { Interactable consumer = item.iSearchForItemConsumer();
                                if (consumer != null && consumer != this) {
                                    onItemDraggedOverConsumer(consumer,item);
                                }
                            } if (!item.selected) addToSelectedIfPossible(item);
                        } else if (item.iJustPressed(Mouse.LEFT)) {
                            if (double_click_enabled && item.iDoubleClicked(Mouse.LEFT)) {
                                double_clicked_item = item;
                                if (!item.selected) addToSelectedIfPossible(item);
                            } else { double_clicked_item = null;
                                if (item.selected) removeFromSelected(item);
                                else addToSelectedIfPossible(item);
                            }
                        }
                    } else if (item.iJustReleased(Mouse.LEFT) && grabbing_enabled) {
                        Interactable consumer = item.iSearchForItemConsumer();
                        if (consumer != null && consumer != this) {
                            if (multi_selection_enabled && selected_items.size() > 1) {
                                onSelectedItemsDroppedOnConsumer(consumer,selected_items);
                            } else onItemDroppedOnConsumer(consumer,item);
                        }
                    }
                    renderItem(renderer,item_bounds,item,dt);
                } item_bounds.translate(0,-row_height);
            } renderer.popScissor();
            U.pushRect(2);
            renderScrollbar(renderer,content_height,x,y,dt);
        } U.pushRect();
    }

    protected float calculateItemHeight() {
        if (recalculate_max_advance) {
            max_advance = maxAdvanceFilteredItems(font);
            recalculate_max_advance = false;
        } if (max_advance == 0) return desired_item_height;
        float width = boxWidthPadded();
        width -= item_border_thickness * 2f;
        GUI.fonts.bindFontMetrics(font);
        float scale = GUI.fonts.relativeScale(desired_item_height);
        float desired_width = max_advance * scale;
        if (show_icons) desired_width += icon_text_spacing + desired_item_height;
        if (desired_width > width) {
            float ratio = width / desired_width;
            return Math.max(desired_item_height / 1.25f, desired_item_height * ratio);
        } return desired_item_height;
    }

    protected float itemHeightPadded(float item_height) {
        return item_height + 2 * item_border_thickness;
    }



}
