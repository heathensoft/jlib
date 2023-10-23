package io.github.heathensoft.jlib.gui.box;

import io.github.heathensoft.jlib.gui.Interactable;
import io.github.heathensoft.jlib.gui.UIWindow;
import io.github.heathensoft.jlib.gui.gfx.UIAssets;
import io.github.heathensoft.jlib.gui.gfx.UIRenderer;

import java.util.LinkedList;

/**
 * @author Frederik Dahl
 * 23/10/2023
 */


public abstract class BoxContainer extends Box {

    protected LinkedList<Box> contents;
    protected final Size currentSize;
    protected final Size desiredSize;

    protected BoxContainer(Spacing margin, Spacing border, Spacing padding) {
        super(margin, border, padding);
        this.contents = new LinkedList<>();
        this.desiredSize = new Size(outerSize());
        this.currentSize = new Size(desiredSize);
    }

    public void render(UIRenderer renderer, UIAssets assets, UIWindow window, int parentID, float x, float y, float dt) {
        renderBackground(renderer, assets, window, parentID, x, y, dt);
        int content_offset_x = margin.left() + border.left() + padding.left();
        int content_offset_y = margin.top() + border.top() + padding.top();
        x += content_offset_x;
        y -= content_offset_y;
        if (this instanceof Interactable i && i.iHasID()) {
            parentID = i.interactableID();
        }
        if (this instanceof HBoxContainer) {
            for (Box content : contents) {
                content.render(renderer,assets,window,parentID,x,y,dt);
                x += content.currentSize().width();
            }
        }
        else if (this instanceof VBoxContainer) {
            for (Box content : contents) {
                content.render(renderer,assets,window,parentID,x,y,dt);
                y -= content.currentSize().height();
            }
        }
    }

    public void renderBackground(UIRenderer renderer, UIAssets assets, UIWindow window, int parentID, float x, float y, float dt) {}

    public void renderText(UIRenderer renderer, UIWindow window, float x, float y) {
        int content_offset_x = margin.left() + border.left() + padding.left();
        int content_offset_y = margin.top() + border.top() + padding.top();
        x += content_offset_x;
        y -= content_offset_y;
        if (this instanceof HBoxContainer) {
            for (Box content : contents) {
                content.renderText(renderer,window,x,y);
                x += content.currentSize().width();
            }
        }
        else if (this instanceof VBoxContainer) {
            for (Box content : contents) {
                content.renderText(renderer,window,x,y);
                y -= content.currentSize().height();
            }
        }
    }

    public abstract void addContent(Box content);

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public int numChildren() {
        return contents.size();
    }

    protected Size currentSize() {
        return currentSize;
    }

    protected Size desiredSize() {
        return desiredSize;
    }
}
