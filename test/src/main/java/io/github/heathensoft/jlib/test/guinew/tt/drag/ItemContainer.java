package io.github.heathensoft.jlib.test.guinew.tt.drag;

import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.Box;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public abstract class ItemContainer<I extends Item> extends Box implements Iterable<I>{

    // if any interactable is dragging. draw items using container id for all consumers
    // highlight container when hovered with item
    // When an item is dragged draw it two times. Fist translucent in place. Then opaque where its dragged.
    // Of course use NO_ID
    // item view and list view / toggle box
    // list view has search bar.
    // use pixel tier icons for demo

    protected float item_width;
    protected float item_height;
    protected float inner_spacing;
    protected float outer_padding;
    protected final List<I> items;

    public ItemContainer(float item_width, float item_height, float inner_spacing, float padding, int num_items_x, int num_items_y) {
        this.desired_width = 2 * padding + item_width * num_items_x + inner_spacing * (num_items_x - 1);
        this.desired_height = 2 * padding + item_height * num_items_y + inner_spacing * (num_items_y - 1);
        this.inner_spacing = inner_spacing;
        this.outer_padding = padding;
        this.item_height = item_height;
        this.item_width = item_width;
        this.items = new ArrayList<I>(num_items_x * num_items_y);
        this.iID = iObtainIDAndRegisterAsConsumer();
    }

    @SuppressWarnings("unchecked")
    public boolean iIsValidDrop(Interactable drop) {
        I typedObject = null;
        try { typedObject = (I) drop;
        } catch (ClassCastException ignored) {}
        return typedObject != null;
    }



    public Iterator<I> iterator() { return items.iterator(); }


    public void dispose() {
        super.dispose();
        iUnRegisterAsConsumer();
    }
}
