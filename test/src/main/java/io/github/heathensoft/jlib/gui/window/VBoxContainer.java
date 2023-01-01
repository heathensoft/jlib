package io.github.heathensoft.jlib.gui.window;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


public class VBoxContainer extends BoxContainer {


    public VBoxContainer(Spacing margin, Spacing border, Spacing padding, Color backgroundColor, Color borderColor) {
        super(margin, border, padding, backgroundColor, borderColor);
    }

    public VBoxContainer(Spacing margin, Spacing border, Spacing padding) {
        super(margin, border, padding);
    }

    public void resizeHorizontal(float dx) {
        if (dx == 0) return;
        if (dx < 0) {
            float sumWiggle = currentSize.width() - desiredSize.width();
            if (sumWiggle <= 0) return;
            dx = -Math.min(sumWiggle,-dx);
            for (Box content : contents) {
                content.resizeHorizontal(dx);
            }
        } else {
            for (Box content : contents) {
                content.resizeHorizontal(dx);
            }
        } currentSize.addWidth(dx);
    }

    public void resizeVertical(float dy) {
        if (dy == 0) return;
        float currentContentHeight = currentSize.height() - outerSize.height();
        float desiredContentHeight = desiredSize.height() - outerSize.height();
        if (dy < 0) {
            float sumWiggle = currentContentHeight - desiredContentHeight;
            if (sumWiggle <= 0) return;
            dy = -(Math.min(sumWiggle,-dy));
            for (Box content : contents) {
                float wiggle = content.wiggleRoomY();
                float weight = wiggle / sumWiggle;
                float resize = dy * weight;
                content.resizeVertical(resize);
            }
        } else {
            for (Box content : contents) {
                float weight = content.desiredSize().height() / desiredContentHeight;
                float resize = dy * weight;
                content.resizeVertical(resize);
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
            width = Math.max(width,content.desiredSize().width());
            height += content.desiredSize().height();
        }for (Box content : contents) {
            float resize = width - content.currentSize().width();
            content.resizeHorizontal(resize);
        } desiredSize.set(minSize).add(width,height);
        currentSize.set(desiredSize);
    }

    public void addContent(Box content) {
        restore();
        content.restore();
        desiredSize.addHeight(content.desiredSize().height());
        currentSize.addHeight(content.currentSize().height());
        float maxX = desiredSize.width() - outerSize.width();
        float contentX = content.desiredSize().width();
        float resize = contentX - maxX;
        if (contentX > maxX) {
            resizeHorizontal(resize);
            desiredSize.addWidth(resize);
        } else if (contentX < maxX) {
            content.resizeHorizontal(-resize);
        } contents.add(content);
    }
}
