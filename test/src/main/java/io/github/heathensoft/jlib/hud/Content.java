package io.github.heathensoft.jlib.hud;

import org.joml.Vector2f;

/**
 *
 * desired size can not be 0, and cannot be greater than current size
 *
 * @author Frederik Dahl
 * 23/11/2022
 */


public abstract class Content {

    /**
     * @return the desired total size of the element.
     * This includes margin, border and padding.
     * This is the minimum size of the element
     */
    protected abstract Size desiredSize();

    /**
     * @return the current total size of the element.
     * This includes margin, border and padding.
     * This will never be bigger than desired size.
     */
    protected abstract Size currentSize();

    /**
     * @return sum of padding, margin, border
     */
    protected abstract Size minimumSize();

    protected Size contentSize() {
        Size contentSize = new Size(currentSize());
        contentSize.sub(minimumSize());
        return contentSize;
    }

    protected abstract void resizeHorizontal(float dx);

    protected abstract void resizeVertical(float dy);

    /**
     * Should set current size to desired size
     */
    protected abstract void restore();

    protected void resize(Vector2f vector) {
        resize(vector.x,vector.y);
    }

    protected void resize(float dx, float dy) {
        resizeHorizontal(dx);
        resizeVertical(dy);
    }

    /**
     * @return current width / desired width. desired cannot be 0
     */
    protected float scaleX() {
        return currentSize().width() / desiredSize().width();
    }

    /**
     * @return current width / desired width. desired cannot be 0
     */
    protected float scaleY() {
        return currentSize().height() / desiredSize().height();
    }

    protected float contentScaleX() {
        Size minimumSize = minimumSize();
        float contentCurrent = currentSize().width() - minimumSize.width();
        float desiredCurrent = desiredSize().width() - minimumSize.width();
        return contentCurrent / desiredCurrent;
    }

    protected float contentScaleY() {
        Size minimumSize = minimumSize();
        float contentCurrent = currentSize().height() - minimumSize.height();
        float desiredCurrent = currentSize().height() - minimumSize.height();
        return contentCurrent / desiredCurrent;
    }
}
