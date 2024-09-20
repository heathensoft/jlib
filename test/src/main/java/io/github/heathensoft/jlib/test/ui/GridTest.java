package io.github.heathensoft.jlib.test.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.Executor;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.box.scroll.ItemGrid;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 28/04/2024
 */


public class GridTest extends ItemGrid<Sprite> {

    private final int bg_color_pressed = Color.hex_to_intBits("B26E5DFF");
    private final int bg_color_unpressed = Color.hex_to_intBits("444444FF");
    private final Framebuffer selectBuffer;


    public GridTest(int rows, int cols, float item_size) throws Exception {
        rows = Math.max(1,rows);
        cols = Math.max(1,cols);
        this.item_width = item_size;
        this.item_height = item_size;
        this.item_border_thickness = 2;
        this.item_desired_spacing = 0;
        this.desired_width = (item_width + 2 * item_border_thickness) * cols;
        this.desired_height = (item_height + 2 * item_border_thickness) * rows;
        if (cols > 1) desired_width += ((cols - 1) * item_desired_spacing);
        if (rows > 1) desired_height += ((rows - 1) * item_desired_spacing);
        this.iID = iObtainIDAndRegisterAsConsumer();
        selectBuffer = new Framebuffer((int) (item_size * 2),(int) (item_size * 2));
        Framebuffer.bind(selectBuffer);
        Texture texture = Texture.generate2D(selectBuffer.width(),selectBuffer.height());
        texture.bindToActiveSlot();
        texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        texture.clampToEdge();
        texture.filterLinear();
        Framebuffer.attachColor(texture,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
    }


    protected void prepareBox(BoxWindow window, float dt) {
        super.prepareBox(window, dt);
        if (!selected_items.isEmpty()) {
            Framebuffer.bind(selectBuffer);
            Framebuffer.viewport();
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            ShaderProgram.bindProgram(GUI.shaders.icon_selection);
            ShaderProgram.setUniform(ShaderProgram.UNIFORM_RESOLUTION,U.popSetVec2(
                    selectBuffer.width(),selectBuffer.height()
            )); ShaderProgram.setUniform(ShaderProgram.UNIFORM_TIME, (float) Engine.get().time().timeSeconds());
            ShaderProgram.setUniform("u_border_pixels",5f);
            ShaderProgram.setUniform("u_radius_pixels",22.5f);
            ShaderProgram.shaderPass().draw();
            U.pushVec2();
        }
    }

    protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {

        super.renderBox(window, renderer, x, y, dt, parent_id);

    }

    protected void renderItem(RendererGUI renderer, Rectanglef bounds, Item<Sprite> item, float dt) {
        int id = item.iAnyInteractableDragged() ? iID : item.interactableID();
        int color = item.isSelected() ? bg_color_pressed : bg_color_unpressed;
        renderer.drawElement(GUI.icons.faded_background,bounds,color,id);
        float glow = item.iHovered() ? 0.5f : 0.0f;
        if (item.iHovered()) {
            if (iHoveredDuration() > 0.5) {
                GUI.tooltips.display(item.name,GUI.mousePosition(U.popVec2()));
                U.pushVec2();
            }
        }
        color = item.iGrabbed(Mouse.LEFT) ? 0x55FFFFFF : 0xFFFFFFFF;
        Rectanglef sprite_rect = U.popSetRect(bounds);
        U.rectExpand(sprite_rect,-item_border_thickness);
        renderer.spriteBatch().enablePixelArtAntialiasing(true);
        renderer.drawSprite(item.value,sprite_rect,color,id,glow,true);
        renderer.spriteBatch().enablePixelArtAntialiasing(false);
        U.pushRect();
        if (item.isSelected()) {
            Texture texture = selectBuffer.texture(0);
            Rectanglef rect = U.popSetRect(bounds);
            U.rectExpand(rect,0f);
            renderer.drawElement(texture,rect,0xFF77EE55,id,1.0f);
            U.pushRect();
        }
        if (item.iGrabbed(Mouse.LEFT)) {
            renderer.drawDelayed(new Executor() {
                @Override
                public void apply() {
                    float wh = item_width / 2f;
                    float hh = item_height / 2f;
                    Vector2f mouse = GUI.mousePosition(U.popVec2());
                    Rectanglef rect = U.popSetRect(mouse.x - wh, mouse.y - hh, mouse.x + wh, mouse.y + hh);
                    renderer.drawSprite(item.value,rect,0xFFFFFFFF,RendererGUI.SKIP_ID);
                    U.pushVec2();
                    U.pushRect();
                }
            });
        }
    }

    protected void onItemDraggedOverConsumer(Interactable consumer, Item<Sprite> dragged_item) { }

    protected void onItemDroppedOnConsumer(Interactable consumer, Item<Sprite> dropped_item) {
        if (consumer.iAcceptItemDrop(dropped_item)) removal_queue.addFirst(dropped_item);
    }

    protected void onSelectedItemsDroppedOnConsumer(Interactable consumer, List<Item<Sprite>> dropped_items) {
        if (consumer.iAcceptItemDrop(dropped_items)) {
            for (Item<Sprite> item : dropped_items) {
                removal_queue.addFirst(item);
            }
        }
    }

    public boolean iAcceptItemDrop(Interactable drop) {
        if (drop instanceof Item<?> item) {
            Object object = item.value;
            if (object instanceof Sprite sprite) {
                addItem(sprite,item.name,item.type_mask);
                return true;
            }
        } return false;
    }


    public <T extends Interactable> boolean iAcceptItemDrop(List<T> drops) {
        if (drops != null && !drops.isEmpty()) {
            Object object = drops.get(0);
            if (object instanceof Item<?> item) {
                if (item.value instanceof Sprite) {
                    for (T generic : drops) {
                        Item<?> generic_item = (Item<?>) generic;
                        Sprite sprite = (Sprite) generic_item.value;
                        addItem(sprite,generic_item.name,generic_item.type_mask);
                    } return true;
                }
            }
        } return false;
    }

    public void dispose() {
        super.dispose();
        Disposable.dispose(selectBuffer);
    }
}
