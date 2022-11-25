package io.github.heathensoft.jlib.hud;

/**
 * @author Frederik Dahl
 * 24/11/2022
 */


public class HContainer extends Container {

    public HContainer(Spacing margin, Spacing border, Spacing padding) {
        super(margin, border, padding);
    }

    public HContainer(Spacing border, Spacing padding) {
        super(border, padding);
    }

    public HContainer(Spacing padding) {
        super(padding);
    }

    public HContainer() {
        super();
    }

    @Override
    public void addContent(Content content) {
        restore();
        content.restore();
        desiredSize.addWidth(content.desiredSize().width());
        currentSize.addWidth(content.currentSize().width());
        Size minimumSize = minimumSize();
        float maxY = desiredSize.height() - minimumSize.height();
        float contentY = content.desiredSize().height();
        float resize = contentY - maxY;
        if (contentY > maxY) {
            resizeVertical(resize);
            desiredSize.addHeight(resize);
        } else if (contentY < maxY) {
            content.resizeVertical(-resize);
        } contents.add(content);
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
    protected void resizeHorizontal(float dx) {
        if (dx == 0) return;
        Size minSize = minimumSize();
        float currentContentWidth = currentSize.width() - minSize.width();
        float desiredContentWidth = desiredSize.width() - minSize.width();
        if (dx < 0) {
            float sumWiggle = currentContentWidth - desiredContentWidth;
            if (sumWiggle <= 0) return;
            dx = -(Math.min(sumWiggle,-dx));
            for (Content content : contents) {
                float wiggle = content.currentSize().width() -
                desiredContentWidth;
                float weight = wiggle / sumWiggle;
                float resize = dx * weight;
                content.resizeHorizontal(resize);
            }
        } else {
            for (Content content : contents) {
                float weight = content.desiredSize().width() /
                desiredContentWidth;
                float resize = dx * weight;
                content.resizeHorizontal(resize);
            }
        } currentSize.addWidth(dx);
    }

    @Override
    protected void resizeVertical(float dy) {
        if (dy == 0) return;
        if (dy < 0) {
            float sumWiggle = currentSize.height() - desiredSize.height();
            if (sumWiggle <= 0) return;
            dy = -Math.min(sumWiggle,-dy);
            for (Content content : contents) {
                content.resizeVertical(dy);
            }
        } else {
            for (Content content : contents) {
                content.resizeVertical(dy);
            }
        } currentSize.addHeight(dy);
    }

    @Override
    protected void restore() {
        if (scaleX() == 1 && scaleY() == 1) return;
        Size minSize = minimumSize();
        float height = 0;
        float width = 0;
        for (Content content : contents) {
            content.restore();
            height = Math.max(height,content.desiredSize().height());
            width += content.desiredSize().width();
        }
        for (Content content : contents) {
            float resize = height - content.currentSize().height();
            content.resizeVertical(resize);
        }
        desiredSize.set(minSize);
        desiredSize.add(width,height);
        currentSize.set(desiredSize);
    }

}
