package io.github.heathensoft.jlib.gui.window;


import io.github.heathensoft.jlib.graphicsOld.Color;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


public class HBoxContainer extends BoxContainer {

    public HBoxContainer(Spacing margin, Spacing border, Spacing padding, Color backgroundColor, Color borderColor) {
        super(margin, border, padding, backgroundColor, borderColor);
    }

    public HBoxContainer(Spacing margin, Spacing border, Spacing padding) {
        super(margin, border, padding);
    }

    public void resizeHorizontal(float dx) {
        if (dx == 0) return;
        float currentContentWidth = currentSize.width() - outerSize.width();
        float desiredContentWidth = desiredSize.width() - outerSize.width();
        if (dx < 0) {
            float sumWiggle = currentContentWidth - desiredContentWidth;
            if (sumWiggle <= 0) return;
            dx = -(Math.min(sumWiggle,-dx));
            for (Box content : contents) {
                float wiggle = content.wiggleRoomX();
                float weight = wiggle / sumWiggle;
                float resize = dx * weight;
                content.resizeHorizontal(resize);
            }
        } else {
            for (Box content : contents) {
                float weight = content.desiredSize().width() / desiredContentWidth;
                float resize = dx * weight;
                content.resizeHorizontal(resize);
            }
        } currentSize.addWidth(dx);
    }

    public void resizeVertical(float dy) {
        if (dy == 0) return;
        if (dy < 0) {
            float sumWiggle = currentSize.height() - desiredSize.height();
            if (sumWiggle <= 0) return;
            dy = -Math.min(sumWiggle,-dy);
            for (Box content : contents) {
                content.resizeVertical(dy);
            }
        } else {
            for (Box content : contents) {
                content.resizeVertical(dy);
            }
        } currentSize.addHeight(dy);
    }

    public void restore() {
        if (totalScaleX() == 1 && totalScaleY() == 1) return;
        Size minSize = outerSize();
        float height = 0;
        float width = 0;
        for (Box content : contents) {
            content.restore();
            height = Math.max(height,content.desiredSize().height());
            width += content.desiredSize().width();
        }for (Box content : contents) {
            float resize = height - content.currentSize().height();
            content.resizeVertical(resize);
        } desiredSize.set(minSize).add(width,height);
        currentSize.set(desiredSize);
    }

    public void addContent(Box content) {
        restore();
        content.restore();
        desiredSize.addWidth(content.desiredSize().width());
        currentSize.addWidth(content.currentSize().width());
        float maxY = desiredSize.height() - outerSize.height();
        float contentY = content.desiredSize().height();
        float resize = contentY - maxY;
        if (contentY > maxY) {
            resizeVertical(resize);
            desiredSize.addHeight(resize);
        } else if (contentY < maxY) {
            content.resizeVertical(-resize);
        } contents.add(content);
    }
}
