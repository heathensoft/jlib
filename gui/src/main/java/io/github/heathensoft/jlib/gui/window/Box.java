package io.github.heathensoft.jlib.gui.window;


import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.Interactable;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 29/10/2023
 */


public abstract class Box implements Interactable, Disposable {

    protected final Size currentSize = new Size();
    protected final Size restingSize = new Size();
    protected boolean lockedHorizontal;
    protected boolean lockedVertical;
    protected int id;


    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) { }

    public void renderText(RendererGUI renderer, float x, float y) { }

    public void lockVertical() { lockedVertical = true; }

    public void lockHorizontal() { lockedHorizontal = true; }

    public boolean isLockedVertical() { return lockedVertical; }

    public boolean isLockedHorizontal() { return lockedHorizontal; }

    public boolean isRestored() { return wiggleRoomXInside() == 0 && wiggleRoomYInside() == 0; }

    protected Size restingSize() { return restingSize; }

    protected Size currentSize() { return currentSize; }

    protected void setRestingSize(float w, float h) {
        this.restingSize.set(w, h);
    }

    protected Rectanglef bounds(Rectanglef dst, float x, float y) {
        dst.minX = x;
        dst.minY = y - currentSize.height();
        dst.maxX = x + currentSize.width();
        dst.maxY = y;
        return dst;
    }

    protected float wiggleRoomXInside() { return currentSize.width() - restingSize.width(); }

    protected float wiggleRoomYInside() { return currentSize.height() - restingSize.height(); }

    protected float width_limit() { return lockedHorizontal ? currentSize.width() : Float.MAX_VALUE; }

    protected float height_limit() { return lockedVertical ? currentSize.height() : Float.MAX_VALUE; }

    protected void restore() { currentSize.set(restingSize); }

    protected void resizeVertical(float dy) { currentSize.addHeight(dy); }

    protected void resizeHorizontal(float dx) { currentSize.addWidth(dx); }

    protected void onWindowOpen(WindowGUI context) { }

    protected void onWindowClose(WindowGUI context) { }

    public void onWindowFocus(WindowGUI context) { }

    public int interactableID() { return id; }

    public void dispose() { if (iHasID()) iReturnID(); }
}
