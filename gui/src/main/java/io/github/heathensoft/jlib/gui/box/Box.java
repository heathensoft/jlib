package io.github.heathensoft.jlib.gui.box;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.UIWindow;
import io.github.heathensoft.jlib.gui.gfx.UIAssets;
import io.github.heathensoft.jlib.gui.gfx.UIRenderer;

/**
 * @author Frederik Dahl
 * 23/10/2023
 */


public abstract class Box implements Disposable  {

    protected Size outerSize;
    protected Spacing margin;
    protected Spacing border;
    protected Spacing padding;

    protected Box(Spacing margin, Spacing border, Spacing padding) {
        this.margin = margin;
        this.border = border;
        this.padding = padding;
        this.outerSize = new Size(
                margin.horizontal()+
                        border.horizontal()+
                        padding.horizontal(),
                margin.vertical()+
                        border.vertical()+
                        padding.vertical()
        );
    }

    public abstract void render(UIRenderer renderer, UIAssets assets, UIWindow window, int parentID, float x, float y, float dt);

    public abstract void renderText(UIRenderer renderer, UIWindow window, float x, float y);

    public float desiredWidth() {
        return desiredSize().width();
    }

    public float desiredHeight() {
        return desiredSize().height();
    }

    public float currentWidth() {
        return currentSize().width();
    }

    public float currentHeight() {
        return currentSize().height();
    }

    public float contentWidth() {
        return contentSize().width();
    }

    public float contentHeight() {
        return contentSize().height();
    }

    public float outerWidth() {
        return outerSize().width();
    }

    public float outerHeight() {
        return outerSize().height();
    }

    /** @return current width / desired width. desired cannot be 0 */
    public float totalScaleX() {
        return currentSize().width() / desiredSize().width();
    }

    /** @return current height / desired height. desired cannot be 0 */
    public float totalScaleY() {
        return currentSize().height() / desiredSize().height();
    }

    public float wiggleRoomX() {
        return currentSize().width() - desiredSize().width();
    }

    public float wiggleRoomY() {
        return currentSize().height() - desiredSize().height();
    }

    /** @return scale of the actual content x */
    public float contentScaleX() {
        float contentCurrent = currentSize().width() - outerSize.width();
        float desiredCurrent = desiredSize().width() - outerSize.width();
        return contentCurrent / desiredCurrent;
    }

    /** @return scale of the actual content y */
    public float contentScaleY() {
        float contentCurrent = currentSize().height() - outerSize.height();
        float desiredCurrent = currentSize().height() - outerSize.height();
        return contentCurrent / desiredCurrent;
    }

    protected Size outerSize() {
        return outerSize;
    }

    protected Size contentSize() {
        return new Size(currentSize()).sub(outerSize);
    }

    protected Spacing getMargin() {
        return margin;
    }

    protected Spacing getBorder() {
        return border;
    }

    protected Spacing getPadding() {
        return padding;
    }

    public void dispose() { }

    /** must set current size to desired size */
    public abstract void restore();

    public abstract void resizeHorizontal(float dx);

    public abstract void resizeVertical(float dy);

    protected abstract Size desiredSize();

    protected abstract Size currentSize();
}
