package io.github.heathensoft.jlib.ui.window;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.ui.HudAssets;
import org.joml.Vector2f;

/**
 * If you don't want to draw background, set background alpha to 0.
 * If you don't want to draw border, set border alpha to 0 or border spacing 0.
 *
 * @author Frederik Dahl
 * 27/11/2022
 */


public abstract class Box implements UiComponent {

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

    protected void drawBorder(SpriteBatch batch, HudAssets assets, float posX, float posY) {
        if (borderColor.a > 0) {
            float color = borderColor.toFloatBits();
            Size size = currentSize();
            TextureRegion blank = assets.blank();
            float x, y, w, h;
            if (border.top() > 0) {
                x = posX + margin.left();
                y = posY - margin.top() - border.top();
                w = size.width() - margin.horizontal();
                h = border.top();
                batch.draw(blank,x,y,w,h,color,0);
            } if (border.bottom() > 0) {
                x = posX + margin.left();
                y = posY - size.height() + margin.bottom();
                w = size.width() - margin.horizontal();
                h = border.bottom();
                batch.draw(blank,x,y,w,h,color,0);
            } if (border.right() > 0) {
                x = posX + size.width() - margin.right() - border.right();
                y = posY - size.height() + margin.bottom();
                w = border.right();
                h = size.height() - margin.vertical();
                batch.draw(blank,x,y,w,h,color,0);
            } if (border.left() > 0) {
                x = posX + margin.left();
                y = posY - size.height() + margin.bottom();
                w = border.left();
                h = size.height() - margin.vertical();
                batch.draw(blank,x,y,w,h,color,0);
            }
        }
    }

    protected void drawBackground(SpriteBatch batch, HudAssets assets, float posX, float posY) {
        if (backgroundColor.a > 0) {
            Size size = currentSize();
            Size content = contentSize();
            TextureRegion blank = assets.blank();
            float color = backgroundColor.toFloatBits();
            float x0 = posX + margin.left() + border.left();
            float y0 = posY - size.height() + margin.bottom() + border.bottom();
            float w = content.width() + padding.horizontal();
            float h = content.height() + padding.vertical();
            batch.draw(blank,x0,y0,w,h,color,0);
            /*else {
                Texture background = assets.texture(backgroundTexture);
                float u2 = w * backgroundScaling / (float) background.width();
                float v2 = h * backgroundScaling / (float) background.height();
                batch.draw(0,0,u2,v2,x0,y0,w,h,backgroundColor,backgroundTexture);
            }*/
        }
    }

    protected abstract Size desiredSize();

    protected abstract Size currentSize();

    protected Size outerSize() {
        return outerSize;
    }

    protected Size contentSize() {
        return new Size(currentSize()).sub(outerSize);
    }

    protected void resize(Vector2f vector) {
        resize(vector.x,vector.y);
    }

    protected void resize(float dx, float dy) {
        resizeHorizontal(dx);
        resizeVertical(dy);
    }

    protected abstract void resizeHorizontal(float dx);

    protected abstract void resizeVertical(float dy);

    /**
     * must set current size to desired size
     */
    protected abstract void restore();

    /**
     * @return current width / desired width. desired cannot be 0
     */
    protected float totalScaleX() {
        return currentSize().width() / desiredSize().width();
    }

    /**
     * @return current height / desired height. desired cannot be 0
     */
    protected float totalScaleY() {
        return currentSize().height() / desiredSize().height();
    }

    /**
     * @return scale of the actual content x
     */
    protected float contentScaleX() {
        float contentCurrent = currentSize().width() - outerSize.width();
        float desiredCurrent = desiredSize().width() - outerSize.width();
        return contentCurrent / desiredCurrent;
    }

    /**
     * @return scale of the actual content y
     */
    protected float contentScaleY() {
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

}
