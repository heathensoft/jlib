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
 * 28/04/2024
 */


public abstract class ItemGrid<T> extends ItemContainer<T> {

    protected float item_width; // width of item without borders
    protected float item_height; // height of item without borders
    protected float item_border_thickness; // border for each individual item
    protected float item_desired_spacing; // minimum spacing between items



    public void setItemWidth(float width) { this.item_width = width; }
    public void setItemHeight(float height) { this.item_height = height; }
    public void setItemBorder(float thickness) { this.item_border_thickness = thickness; }
    public void setItemSpacing(float spacing) { this.item_desired_spacing = spacing; }



    protected abstract void renderItem(RendererGUI renderer, Rectanglef bounds, Item<T> item, float dt);


    protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef bounds = bounds(U.popRect(),x,y);
        renderBackground(renderer,bounds,iID,dt);
        refreshItemContainer();
        int num_items = numItemsFiltered();
        if (num_items > 0) {
            final float item_width = this.item_width + 2 * item_border_thickness;
            final float item_height = this.item_height + 2 * item_border_thickness;
            final int cols = calculateCols(item_width);
            final int rows = calculateRows(num_items,cols);
            final float item_spacing = calculateSpacing(rows,cols,item_width);
            final float content_height = contentHeight(rows,item_height,item_spacing);
            if (processScrollBarEvents(item_height + item_spacing,content_height)) {
                GUI.windows.focusRequest(window);
                scrollbar.iFocus();
            }
            Rectanglef content_bounds = contentBounds(bounds,U.popRect());
            Rectanglef row_bounds = U.popSetRect(content_bounds);
            Rectanglef item_bounds = U.popRect();
            row_bounds.translate(itemOffsetX(cols,item_width),content_offset);
            row_bounds.minY = row_bounds.maxY - item_height;
            renderer.pushScissor(content_bounds);

            int index = 0;
            out:
            for (int r = 0; r < rows; r++) {
                if (row_bounds.minY < content_bounds.maxY) {
                    if (row_bounds.maxY < content_bounds.minY) break;
                    item_bounds.minY = row_bounds.minY;
                    item_bounds.maxY = row_bounds.maxY;
                    item_bounds.minX = row_bounds.minX;
                    item_bounds.maxX = item_bounds.minX + item_width;
                    for (int c = 0; c < cols; c++) {
                        if (index >= num_items) break out;
                        Item<T> item = filtered_items.get(index);


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
                        } renderItem(renderer,item_bounds,item,dt);
                        item_bounds.translate(item_width + item_spacing,0f);
                        index++;
                    }
                } else index += cols;
                row_bounds.translate(0,-(item_height + item_spacing));
            } U.pushRect(3);
            renderer.popScissor();
            renderScrollbar(renderer,content_height,x,y,dt);
        } U.pushRect();
    }

    protected float itemOffsetX(int cols, float item_width) {
        if (cols == 1) {
            float content_width = boxWidthPadded();
            if (content_width > item_width)  return (content_width - item_width) / 2f;
        } return 0;
    }

    protected float contentHeight(int rows, float item_height, float item_spacing) {
        if (rows > 0) { float height = rows * item_height;
            if (rows > 1) { height += item_spacing * (rows - 1);
            } return height;
        } return 0;
    }

    protected float calculateSpacing(int rows, int cols, float item_width) {
        // If you calculate cols with desired spacing,
        // then this spacing will be >= desired spacing
        float content_width = boxWidthPadded();
        float spacing = 0;
        int num_gaps = cols - 1;
        if (num_gaps > 0) {
            float desired_width = cols * item_width;
            float sum_spacing = content_width - desired_width;
            return sum_spacing / num_gaps;
        } return spacing;
    }

    protected int calculateRows(int num_items, int columns) {
        if (columns > 0) {
            return U.ceil(num_items / (float) columns);
        } return 0;
    }

    protected int calculateCols(float item_width) {
        float content_width = boxWidthPadded();
        float width = 0;
        int i = 0;
        while (true) {
            width += item_width;
            if (width > content_width) break;
            i++; width += item_desired_spacing;
        } return Math.max(i,1);
    }
}
