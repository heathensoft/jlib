package io.github.heathensoft.jlib.hud.ui;

import io.github.heathensoft.jlib.hud.HudGraphics;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


public abstract class BoxContainer extends Box {

    protected final List<Box> contents;
    protected final Size currentSize;
    protected final Size desiredSize;

    protected BoxContainer(Spacing margin, Spacing border, Spacing padding, Color backgroundColor, Color borderColor) {
        super(margin, border, padding, backgroundColor, borderColor);
        this.contents = new ArrayList<>(2);
        this.desiredSize = new Size(outerSize());
        this.currentSize = new Size(desiredSize);
    }

    protected BoxContainer(Spacing margin, Spacing border, Spacing padding) {
        this(margin, border, padding,Color.EMPTY.cpy(),Color.EMPTY.cpy());
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

    public void update(float dt) {
        for (Box content : contents) content.update(dt);
    }

    public void render(SpriteBatch batch, HudGraphics assets, int x0, int y0) {
        drawBackground(batch,assets,x0,y0);
        drawBorder(batch,assets,x0,y0);
        int content_offset_x = margin.left() + border.left() + padding.left();
        int content_offset_y = margin.top() + border.top() + padding.top();
        x0 += content_offset_x;
        y0 -= content_offset_y;
        if (this instanceof HBoxContainer) {
            for (Box content : contents) {
                content.render(batch,assets,x0,y0);
                x0 += content.currentSize().width();
            }
        }
        else if (this instanceof VBoxContainer) {
            for (Box content : contents) {
                content.render(batch,assets,x0,y0);
                y0 -= content.currentSize().height();
            }
        }
    }

}
