package io.github.heathensoft.jlib.ui.box.scroll;

import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;

import java.util.*;

/**
 * @author Frederik Dahl
 * 18/04/2024
 */


public abstract class ItemContainer<T> extends ScrollBox {

    public static final class Item<T> implements Interactable, Comparable<Item<?>> {
        public final String name;
        public final T value;
        public final int iID;
        public final int type_mask;
        boolean selected;
        Item(T value, String name, int type_mask) {
            this.iID = iObtainID();
            this.value = value;
            this.type_mask = type_mask;
            this.name = name;
        } public int interactableID() {
            return iID;
        }
        public int compareTo(Item<?> o) {
            return this.name.compareTo(o.name);
        } public boolean isSelected() {
            return selected;
        }
        public int typeMask() { return type_mask; }
    }

    protected Item<T> hovered_item;
    protected Item<T> grabbed_item;
    protected Item<T> double_clicked_item;
    protected List<Item<T>> all_items = new ArrayList<>();
    protected List<Item<T>> filtered_items = new ArrayList<>();
    protected LinkedList<Item<T>> added_queue = new LinkedList<>();
    protected LinkedList<Item<T>> removal_queue = new LinkedList<>();
    protected LinkedList<Item<T>> selected_items = new LinkedList<>();
    protected Comparator<Item<T>> sort_request;
    protected String name_filter_uppercase = "";
    protected int type_filter = Integer.MAX_VALUE;
    protected int max_selected_items = Integer.MAX_VALUE;
    protected boolean grabbing_enabled;
    protected boolean refresh_item_filter;
    protected boolean double_click_enabled;
    protected boolean multi_selection_enabled;
    protected boolean recalculate_max_advance;
    protected float box_padding_bottom;
    protected float box_padding_right;
    protected float box_padding_left;
    protected float box_padding_top;

    /**An item in this container is dragged over a consumer who might accept the Item as a "Drop"*/
    protected abstract void onItemDraggedOverConsumer(Interactable consumer, Item<T> dragged_item);
    /**An item is this container has been "Dropped" on a consumer who might accept the Item as a "Drop"*/
    protected abstract void onItemDroppedOnConsumer(Interactable consumer, Item<T> dropped_item);
    /**An item from another container is "Dropped" in this container. Return true if you accept the "Drop"*/
    protected abstract void onSelectedItemsDroppedOnConsumer(Interactable consumer,List<Item<T>> dropped_items);
    /**An item from another container is "Dropped" in this container. Return true if you accept the "Drop"*/
    public abstract boolean iAcceptItemDrop(Interactable drop);

    public void setBoxPaddingBottom(float padding) { this.box_padding_bottom = padding; }
    public void setBoxPaddingRight(float padding) { this.box_padding_right = padding; }
    public void setBoxPaddingLeft(float padding) { this.box_padding_left = padding; }
    public void setBoxPaddingTop(float padding) { this.box_padding_top = padding; }
    protected float boxPaddingBottom() { return box_padding_bottom; }
    protected float boxPaddingRight() { return box_padding_right; }
    protected float boxPaddingLeft() { return box_padding_left; }
    protected float boxPaddingTop() { return box_padding_top; }
    public int mumItemsSelected() { return selected_items.size(); }
    public int numItemsFiltered() { return filtered_items.size(); }
    public int numItemsAll() { return all_items.size(); }
    public void clear() {
        removal_queue.clear();
        for (Item<T> item : all_items) {
            removal_queue.addFirst(item);
        }
    }
    public void clearSelected() {
        while (!selected_items.isEmpty()) {
            selected_items.removeLast().selected = false;
        }
    }



    protected void refreshItemContainer() {
        // handle queues
        if (!removal_queue.isEmpty()) {
            while (!removal_queue.isEmpty()) {
                Item<T> item = removal_queue.removeLast();
                all_items.remove(item);
                filtered_items.remove(item);
                selected_items.remove(item);
                if (double_clicked_item == item) {
                    double_clicked_item = null;
                } if (hovered_item == item) {
                    hovered_item = null;
                } if (grabbed_item == item) {
                    grabbed_item = null;
                } item.dispose();
            } recalculate_max_advance = true;
        }if (!added_queue.isEmpty()) {
            while (!added_queue.isEmpty()) {
                Item<T> item = added_queue.removeLast();
                if (isMatchingFilter(item)) filtered_items.add(item);
                all_items.add(item);
            } recalculate_max_advance = true;
        }
        // refresh Filters
        if (refresh_item_filter) {
            filtered_items.clear();
            for (Item<T> item : all_items) {
                if (isMatchingFilter(item)) {
                    filtered_items.add(item);
                } else if (item.selected) {
                    removeFromSelected(item);}
            } recalculate_max_advance = true;
            refresh_item_filter = false;
        }
        // handle sort requests
        if (sort_request != null) {
            if (!filtered_items.isEmpty()) {
                filtered_items.sort(sort_request);
            } sort_request = null;
        }

        if (hovered_item != null) {
            if (!hovered_item.iHovered()) {
                hovered_item = null;
            }
        } if (grabbed_item != null) {
            if (!grabbed_item.iGrabbed(Mouse.LEFT)) {
                grabbed_item = null;
            }
        }
    }

    public void addItem(T value, String name) { addItem(value,name,Integer.MAX_VALUE,Integer.MAX_VALUE); }
    public void addItem(T value, String name, int types_minima, int types_maxima) {
        int type_mask = (types_minima & 0xFFFF) | ((types_minima & 0xFFFF) << 16);
        addItem(value,name,type_mask);
    } public void addItem(T value, String name, int type_mask) {
        added_queue.addFirst(new Item<>(value,name,type_mask));
    }

    public List<T> getAllSelected(List<T> dst, boolean remove) {
        if (remove) {
            while (!selected_items.isEmpty()) {
                Item<T> item = selected_items.removeFirst();
                removal_queue.addFirst(item);
                dst.add(item.value); }
        } else for (Item<T> item : selected_items) {
            dst.add(item.value);
        } return dst;
    }

    public T getLastSelected(boolean remove) {
        if (!selected_items.isEmpty()) {
            if (remove) {
                Item<T> item = selected_items.removeLast();
                removal_queue.addFirst(item);
                return item.value;
            } return selected_items.getLast().value;
        } return null;
    }

    public T getDoubleClicked(boolean remove) {
        if (double_clicked_item == null) return null;
        T value = double_clicked_item.value;
        if (remove) { removal_queue.addFirst(double_clicked_item);
        } return value;
    }

    public T getHovered() {
        if (hovered_item != null) {
            return hovered_item.value;
        } return null;
    }

    public T getGrabbed() {
        if (grabbed_item != null) {
            return grabbed_item.value;
        } return null;
    }

    public boolean grabbingEnabled() { return grabbing_enabled; }
    public void enableGrabbing(boolean enable) {
        if (!enable && grabbed_item != null) {
            grabbed_item = null;
        } grabbing_enabled = enable;
    }

    public boolean multiSelectionEnabled() { return multi_selection_enabled; }
    public void enableMultiSelect(boolean enable) {
        if (multi_selection_enabled &! enable) {
            int num_selected = selected_items.size();
            if (num_selected > 1) {
                Item<T> last_selected = selected_items.removeLast();
                while (!selected_items.isEmpty()) {
                    selected_items.removeFirst().selected = false;
                } selected_items.add(last_selected); }
        } multi_selection_enabled = enable;
    }

    public boolean doubleClickEnabled() { return double_click_enabled; }
    public void enableDoubleClick(boolean enable) {
        if (!enable) double_clicked_item = null;
        double_click_enabled = enable;
    }

    public int maxSelectedItems() { return max_selected_items; }
    public void setMaxSelectedItems(int value) {
        if (value > 0 && value != max_selected_items) {
            max_selected_items = value;
            while (selected_items.size() > max_selected_items) {
                Item<T> first = selected_items.removeFirst();
                first.selected = false;
            }
        }
    }

    public String getNameFilterUpperCase() {
        return name_filter_uppercase;
    }
    public void filterByName(String name) {
        if (name == null) name = "";
        name = name.toUpperCase();
        if (!name_filter_uppercase.equals(name)) {
            name_filter_uppercase = name;
            refresh_item_filter = true;
        }
    }

    public int getTypeFilterMinima() { return type_filter & 0x0000FFFF; }
    public void setTypeFilterMinima(int mask) {
        int new_filter = (type_filter &~ 0x0000FFFF) | (mask & 0xFFFF);
        if (type_filter != new_filter) {
            type_filter = new_filter;
            refresh_item_filter = true;
        }
    }

    public int getTypeFilterMaxima() { return (type_filter >> 16)  & 0xFFFF; }
    public void setTypeFilterMaxima(int mask) {
        int new_filter = (type_filter &~ 0xFFFF0000) | ((mask & 0xFFFF) << 16);
        if (type_filter != new_filter) {
            type_filter = new_filter;
            refresh_item_filter = true;
        }
    }

    public void sortByNames() { this.sort_request = Comparator.naturalOrder(); }
    public void sort(Comparator<T> comparator) { this.sort_request = (o1, o2) -> comparator.compare(o1.value,o2.value); }

    protected void removeFromSelected(Item<T> item) {
        item.selected = false;
        selected_items.remove(item);
    }

    protected void addToSelectedIfPossible(Item<T> item) {
        if (multi_selection_enabled) {
            if (selected_items.size() < max_selected_items) {
                selected_items.add(item);
                item.selected = true;
            }
        } else {
            while (!selected_items.isEmpty()) {
                selected_items.remove().selected = false;
            } selected_items.add(item);
            item.selected = true;
        }
    }

    protected boolean isMatchingFilter(Item<T> item) {
        int filter_minima = type_filter  & 0x0000FFFF;
        int filter_maxima = type_filter  & 0xFFFF0000;
        int item_minima = item.type_mask & 0x0000FFFF;
        int item_maxima = item.type_mask & 0xFFFF0000;
        if ((item_minima & filter_minima) > 0 & (item_maxima & filter_maxima) > 0) {
            if (name_filter_uppercase == null || name_filter_uppercase.isBlank()) return true;
            if (item.name == null || item.name.isBlank()) return true;
            return item.name.toUpperCase().contains(name_filter_uppercase);
        } return false;
    }

    protected float maxAdvanceFilteredItems(int font) {
        if (filtered_items.isEmpty()) return 0;
        float max_advance = 0;
        GUI.fonts.bindFontMetrics(font);
        for (Item<T> item : filtered_items) {
            float advance = GUI.fonts.advanceSum(item.name);
            max_advance = Math.max(max_advance,advance);
        } return max_advance;
    }

    public void dispose() {
        super.dispose();
        sort_request = null;
        hovered_item = null;
        grabbed_item = null;
        double_clicked_item = null;
        added_queue.clear();
        removal_queue.clear();
        selected_items.clear();
        filtered_items.clear();
        Iterator<Item<T>> iterator = all_items.iterator();
        while (iterator.hasNext()) {
            iterator.next().dispose();
            iterator.remove();
        }
    }
}
