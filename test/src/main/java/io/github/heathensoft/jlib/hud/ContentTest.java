package io.github.heathensoft.jlib.hud;

/**
 * @author Frederik Dahl
 * 25/11/2022
 */


public class ContentTest extends Content {

    private final Size desiredSize;
    private final Size currentSize;

    public ContentTest(Size desired, Size current) {
        desiredSize = desired;
        currentSize = current;
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
    protected Size minimumSize() {
        return new Size();
    }

    @Override
    protected void resizeHorizontal(float dx) {
        currentSize.addWidth(dx);
    }

    @Override
    protected void resizeVertical(float dy) {
        currentSize.addHeight(dy);
    }

    @Override
    protected void restore() {
        currentSize.set(desiredSize);
    }

    @Override
    protected float contentScaleX() {
        return scaleX();
    }

    @Override
    protected float contentScaleY() {
        return scaleY();
    }
}
