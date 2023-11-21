package io.github.heathensoft.jlib.gui.window;

import io.github.heathensoft.jlib.gui.gfx.RendererGUI;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 29/10/2023
 */


public abstract class BoxContainer extends Box {

    protected final List<Box> contents = new LinkedList<>();

    public void add(Box ... boxes) {
        for (Box box : boxes) addBox(box);
    }

    public abstract void addBox(Box box);

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public int numChildren() {
        return contents.size();
    }

    public void onWindowFocus(WindowGUI context) { for (Box box : contents) box.onWindowFocus(context); }

    public void renderBackground(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {}

    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        renderBackground(context, renderer, x, y, dt, parent_id);
        if (iHasID()) { parent_id = interactableID(); }
        if (this instanceof HBoxContainer hBoxContainer) {
            int spacing = hBoxContainer.spacing();
            for (Box box : contents) {
                if (box.currentSize.area() > 0) {
                    box.render(context,renderer,x,y,dt,parent_id);
                } x += (box.currentSize().width() + spacing);
            }
        } else if (this instanceof VBoxContainer vBoxContainer) {
            int spacing = vBoxContainer.spacing();
            for (Box box : contents) {
                if (box.currentSize.area() > 0) {
                    box.render(context,renderer,x,y,dt,parent_id);
                } y -= (box.currentSize().height() + spacing);
            }
        } else if (this instanceof RootContainer root) {
            root.processRootInteraction(context,x,y);
            Box box = root.contents.get(0);
            if (box != null) {
                x = x + root.padding;
                y = y - root.padding;
                box.render(context, renderer, x, y, dt, parent_id);
            }
        }
    }

    public void renderText(RendererGUI renderer, float x, float y) {
        if (this instanceof HBoxContainer hBoxContainer) {
            int spacing = hBoxContainer.spacing();
            for (Box box : contents) {
                if (box.currentSize.area() > 0) {
                    box.renderText(renderer,x,y);
                } x += (box.currentSize().width() + spacing);
            }
        } else if (this instanceof VBoxContainer vBoxContainer) {
            int spacing = vBoxContainer.spacing();
            for (Box box : contents) {
                if (box.currentSize.area() > 0) {
                    box.renderText(renderer,x,y);
                } y -= (box.currentSize().height() + spacing);
            }
        } else if (this instanceof RootContainer root) {
            Box box = root.contents.get(0);
            if (box != null) {
                x = x + root.padding;
                y = y - root.padding;
                box.renderText(renderer, x, y);
            }
        }
    }

    protected void onWindowClose(WindowGUI context) {
        for (Box box : contents)
            box.onWindowClose(context);
    }

    protected void onWindowOpen(WindowGUI context) {
        for (Box box : contents)
            box.onWindowOpen(context);
    }

    protected void restore() {
        for (Box box : contents)
            box.restore();
        super.restore();
    }

    public void dispose() {
        super.dispose();
        for (Box box : contents) box.dispose();
    }
}
