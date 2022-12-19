package io.github.heathensoft.jlib.hud.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.hud.HudGraphics;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import org.joml.Vector2f;

/**
 * If you don't want to draw background, set background alpha to 0.
 * If you don't want to draw border, set border alpha to 0 or border spacing 0.
 *
 * @author Frederik Dahl
 * 27/11/2022
 */


public abstract class Box implements Disposable {

    protected final Size outerSize;
    protected final Spacing margin;
    protected final Spacing border;
    protected final Spacing padding;
    protected final Color borderColor;
    protected final Color backgroundColor;

    protected Box(Spacing margin, Spacing border, Spacing padding,
        Color backgroundColor, Color borderColor) {
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
        ); this.borderColor = borderColor;
        this.backgroundColor = backgroundColor;
    }

    protected Box(Spacing margin, Spacing border, Spacing padding) {
        this(margin,border,padding,Color.EMPTY.cpy(),Color.EMPTY.cpy());
    }

    public abstract void update(float dt);

    public abstract void render(SpriteBatch batch, HudGraphics graphics, int x0, int y0);

    protected void drawBorder(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        if (borderColor.a > 0) {
            float color = borderColor.toFloatBits();
            int width = discreteWidth();
            int height = discreteHeight();
            TextureRegion blank = graphics.blank();
            int x, y, w, h;
            if (border.top() > 0) {
                x = x0 + margin.left();
                y = y0 - margin.top() - border.top();
                w = width - margin.horizontal();
                h = border.top();
                batch.draw(blank,x,y,w,h,color,0);
            } if (border.bottom() > 0) {
                x = x0 + margin.left();
                y = y0 - height + margin.bottom();
                w = width - margin.horizontal();
                h = border.bottom();
                batch.draw(blank,x,y,w,h,color,0);
            } if (border.right() > 0) {
                x = x0 + width - margin.right() - border.right();
                y = y0 - height + margin.bottom();
                w = border.right();
                h = height - margin.vertical();
                batch.draw(blank,x,y,w,h,color,0);
            } if (border.left() > 0) {
                x = x0 + margin.left();
                y = y0 - height + margin.bottom();
                w = border.left();
                h = height - margin.vertical();
                batch.draw(blank,x,y,w,h,color,0);
            }
        }
    }

    protected void drawBackground(SpriteBatch batch, HudGraphics graphics, int x0, int y0, int interactable) {
        if (backgroundColor.a > 0) {
            int width = discreteWidth();
            int height = discreteHeight();
            Size content = contentSize();
            TextureRegion blank = graphics.blank();
            float color = backgroundColor.toFloatBits();
            int x = x0 + margin.left() + border.left();
            int y = y0 - height + margin.bottom() + border.bottom();
            int w = (int)content.width() + padding.horizontal();
            int h = (int)content.height() + padding.vertical();
            batch.draw(blank,x,y,w,h,color,interactable);
        }
    }

    protected void drawBackground(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        drawBackground(batch,graphics,x0,y0,0);
    }

    public int discreteWidth() {
        return (int) currentSize().width();
    }

    public int discreteHeight() {
        return (int) currentSize().height();
    }

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

    protected abstract Size desiredSize();

    protected abstract Size currentSize();

    protected Size outerSize() {
        return outerSize;
    }

    protected Size contentSize() {
        return new Size(currentSize()).sub(outerSize);
    }

    public void resize(Vector2f vector) {
        resize(vector.x,vector.y);
    }

    public void resize(float dx, float dy) {
        resizeHorizontal(dx);
        resizeVertical(dy);
    }

    public abstract void resizeHorizontal(float dx);

    public abstract void resizeVertical(float dy);

    /**
     * must set current size to desired size
     */
    public abstract void restore();

    /**
     * @return current width / desired width. desired cannot be 0
     */
    public float totalScaleX() {
        return currentSize().width() / desiredSize().width();
    }

    /**
     * @return current height / desired height. desired cannot be 0
     */
    public float totalScaleY() {
        return currentSize().height() / desiredSize().height();
    }

    public float wiggleRoomX() {
        return currentSize().width() - desiredSize().width();
    }

    public float wiggleRoomY() {
        return currentSize().height() - desiredSize().height();
    }

    /**
     * @return scale of the actual content x
     */
    public float contentScaleX() {
        float contentCurrent = currentSize().width() - outerSize.width();
        float desiredCurrent = desiredSize().width() - outerSize.width();
        return contentCurrent / desiredCurrent;
    }

    /**
     * @return scale of the actual content y
     */
    public float contentScaleY() {
        float contentCurrent = currentSize().height() - outerSize.height();
        float desiredCurrent = currentSize().height() - outerSize.height();
        return contentCurrent / desiredCurrent;
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

    public Color borderColor() {
        return borderColor;
    }

    public Color backgroundColor() {
        return backgroundColor;
    }

    public void dispose() {

    }

}
