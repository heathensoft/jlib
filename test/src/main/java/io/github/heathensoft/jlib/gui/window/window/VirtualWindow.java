package io.github.heathensoft.jlib.gui.window.window;

import io.github.heathensoft.jlib.gui.window.Size;

/**
 * @author Frederik Dahl
 * 22/12/2022
 */


public abstract class VirtualWindow implements BoxContent {

    protected ScrollableBox<?> container;
    protected Size currentSize;

    /**
     * Call this when the contents of this window has been altered
     * in some way. I.e. after adding, removing or altering content.
     */
    protected void refresh() {
        Size visibleWindow = container.windowSize();
        float W = visibleWindow.width();
        float h = visibleWindow.height();
        float H = calculateHeight(W);
        H = Math.max(H,h);
        currentSize.set(W,H);
        container.onSizeAltered();
    }

    /**
     * @return pixels scrolled for each scroll-wheel dy
     */
    public abstract int scrollDelta();

    /**
     * Calculate the height of the virtual area needed
     * to contain all items / text / whatever.
     * Height should be calculated in constructor
     * @param width the width of the virtual window
     * @return height in pixels
     */
    protected abstract float calculateHeight(float width);

    /**
     * The desired size of the visible window.
     * The virtual window will never be resized to a smaller size.
     * The height of the desired size must be greater than 12
     * (the minimum size of the scroll bar)
     * @return desired size of the visible window
     */
    protected abstract Size desiredSize();

    /**
     * @return The size required to contain all content.
     */
    protected Size currentSize() {
        return currentSize;
    }

    protected void setContainer(ScrollableBox<?> container) {
        this.container = container;
        refresh();
    }


}
