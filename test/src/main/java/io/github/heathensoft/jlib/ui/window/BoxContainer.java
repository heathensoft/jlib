package io.github.heathensoft.jlib.ui.window;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.ui.HudAssets;

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

    @Override
    protected Size currentSize() {
        return currentSize;
    }

    @Override
    protected Size desiredSize() {
        return desiredSize;
    }

    @Override
    public void update(float dt) {
        for (Box content : contents)
            content.update(dt);
    }


    @Override
    public void render(SpriteBatch batch, HudAssets assets, float posX, float posY) {
        drawBackground(batch,assets,posX,posY);
        drawBorder(batch,assets,posX,posY);
        float content_offset_x = margin.left() + border.left() + padding.left();
        float content_offset_y = margin.top() + border.top() + padding.top();
        posX += content_offset_x;
        posY -= content_offset_y;
        if (this instanceof HBoxContainer) {
            for (Box content : contents) {
                content.render(batch,assets,posX,posY);
                posX += content.contentSize().width();
            }
        }
        else if (this instanceof VBoxContainer) {
            for (Box content : contents) {
                content.render(batch,assets,posX,posY);
                posY -= content.currentSize().height();
            }
        }
    }

}
