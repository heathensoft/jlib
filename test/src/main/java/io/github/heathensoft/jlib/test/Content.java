package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.graphicsOld.Color;
import io.github.heathensoft.jlib.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.gui.GUIGraphics;
import io.github.heathensoft.jlib.gui.window.Box;
import io.github.heathensoft.jlib.gui.window.Size;
import io.github.heathensoft.jlib.gui.window.Spacing;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class Content extends Box {

    private final Size desiredSize;
    private final Size currentSize;

    protected Content(Size contentSize, Spacing margin, Spacing border, Spacing padding, Color backgroundColor, Color borderColor) {
        super(margin, border, padding,backgroundColor,borderColor);
        this.desiredSize = new Size(outerSize()).add(contentSize);
        this.currentSize = new Size(desiredSize);
    }


    @Override
    public void update(float dt) {

    }

    @Override
    public void render(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        drawBackground(batch, graphics, x0, y0);
        drawBorder(batch, graphics, x0, y0);
    }

    @Override
    protected Size desiredSize() {
        return desiredSize;
    }

    @Override
    protected Size currentSize() {
        return currentSize;
    }

    @Override
    public void resizeHorizontal(float dx) {
        currentSize.addWidth(dx);
    }

    @Override
    public void resizeVertical(float dy) {
        currentSize.addHeight(dy);
    }

    @Override
    public void restore() {
        currentSize.set(desiredSize);
    }
}
