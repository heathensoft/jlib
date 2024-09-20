package io.github.heathensoft.jlib.test.ui;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Sprite;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.scroll.ItemList;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.TextAlignment;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.List;

/**
 * @author Frederik Dahl
 * 27/04/2024
 */


public class ColorList extends ItemList<Vector4f> {

    private final int button_color_pressed = Color.hex_to_intBits("B26E5D99");;
    private final int button_color_unpressed = Color.hex_to_intBits("BD896B99");
    private final int text_color_pressed = Color.hex_to_intBits("EBB53FFF");
    private final int text_color_unpressed = Color.hex_to_intBits("ADAD8CFF");

    public ColorList() {
        this.icon_text_spacing = 2f;
        this.item_border_thickness = 6f;
        this.desired_item_height = 26f;
        GUI.fonts.bindFontMetrics(font);
        float scale = GUI.fonts.relativeScale(desired_item_height);
        this.desired_height = 6 * (2 * item_border_thickness + desired_item_height);
        this.desired_width = (2 * item_border_thickness) + icon_text_spacing + desired_item_height + GUI.fonts.averageAdvance() * scale * 6;
        this.iID = iObtainIDAndRegisterAsConsumer();
        this.show_icons = true;
    }




    protected void renderItem(RendererGUI renderer, Rectanglef bounds, Item<Vector4f> item, float dt) {
        int id = !item.iGrabbed(Mouse.LEFT) && item.iAnyInteractableDragged() ? iID : item.interactableID();
        int color = item.isSelected() ? button_color_pressed : button_color_unpressed;
        renderer.drawGadgetButton(bounds, item_border_thickness,color,id,0.0f,item.isSelected());
        if (show_icons) {
            float glow = item.isSelected() || item.iHovered() ? 0.75f : 0.0f;
            Rectanglef icon_bounds = U.rectExpand(U.popSetRect(bounds),-item_border_thickness);
            icon_bounds.maxX = icon_bounds.minX + icon_bounds.lengthY();
            Sprite sprite = GUI.icons.color_palette;
            renderer.drawSprite(sprite,icon_bounds,color,id,glow,true);
            U.pushRect();
        }
    }

    protected void renderItemText(RendererGUI renderer, Rectanglef bounds, Item<Vector4f> item, int font, float dt) {
        String string = item.name;
        TextAlignment alignment = show_icons ? TextAlignment.LEFT : TextAlignment.CENTERED;
        int color = item.isSelected() ? text_color_pressed : text_color_unpressed;
        float glow = item.isSelected() || item.iHovered() ? 0.75f : 0.0f;
        renderer.drawStringFixedSize(string, alignment, bounds, font, color, 0, glow);
    }

    protected void onItemDraggedOverConsumer(Interactable consumer, Item<Vector4f> dragged_item) { }

    protected void onItemDroppedOnConsumer(Interactable consumer, Item<Vector4f> dropped_item) {
        if (consumer.iAcceptItemDrop(dropped_item)) removal_queue.addFirst(dropped_item);
    }

    protected void onSelectedItemsDroppedOnConsumer(Interactable consumer, List<Item<Vector4f>> dropped_items) {
        if (consumer.iAcceptItemDrop(dropped_items)) {
            for (Item<Vector4f> item : dropped_items) {
                removal_queue.addFirst(item);
            }
        }
    }

    public boolean iAcceptItemDrop(Interactable drop) {
        if (drop instanceof Item<?> item) {
            Object object = item.value;
            if (object instanceof Vector4f vector) {
                addItem(vector,item.name,item.type_mask);
                return true;
            }
        } return false;
    }

    public <T extends Interactable> boolean iAcceptItemDrop(List<T> drops) {
        if (drops != null && !drops.isEmpty()) {
            Object object = drops.get(0);
            if (object instanceof Item<?> item) {
                if (item.value instanceof Vector4f) {
                    for (T generic : drops) {
                        Item<?> generic_item = (Item<?>) generic;
                        Vector4f vector = (Vector4f) generic_item.value;
                        addItem(vector,generic_item.name,generic_item.type_mask);
                    } return true;
                }
            }
        } return false;
    }
}
