package io.github.heathensoft.jlib.ui.box;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 15/02/2024
 */


public class Box implements Interactable {

    protected float current_width;
    protected float current_height;
    protected float desired_width;
    protected float desired_height;
    protected boolean locked_horizontal;
    protected boolean locked_vertical;
    protected boolean built;
    protected int iID;

    public Box() { }

    public Box(float width, float height) {
        desired_width = width;
        desired_height = height;
    }

    protected void onWindowInit(BoxWindow window, BoxContainer parent) { }

    protected void onWindowOpen(BoxWindow window) { }

    protected void onWindowClose(BoxWindow window) { }

    protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) { }

    protected void renderText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) { }

    public int interactableID() { return iID; }

    public void lockHorizontal() {
        if (built) throw new IllegalStateException("Box: cannot lock built box");
        locked_horizontal = true;
    }
    
    public void lockVertical() {
        if (built) throw new IllegalStateException("Box: cannot lock built box");
        locked_vertical = true;
    }
    
    public float currentWidth() { return current_width; }
    
    public float currentHeight() { return current_height; }
    
    public float desiredWidth() {
        return desired_width;
    }
    
    public float desiredHeight() {
        return desired_height;
    }
    
    public boolean isLockedHorizontal() {
        return locked_horizontal;
    }
    
    public boolean isLockedVertical() { return locked_vertical; }
    
    public boolean isBuilt() { return built; }

    public boolean isRestoredX() { return U.floatEquals(current_width,desired_width,0.5f); }

    public boolean isRestoredY() { return U.floatEquals(current_height,desired_height,0.5f); }

    public boolean isRestored() { return isRestoredX() && isRestoredY(); }

    public Rectanglef bounds(Rectanglef dst, float x, float y) {
        dst.minX = x;
        dst.maxY = y;
        dst.maxX = x + current_width;
        dst.minY = y - current_height;
        return dst;
    }

    public <T extends Box> T cast(Class<T> clazz) {
        if (getClass() != clazz) {
            throw new ClassCastException("Box");
        } return clazz.cast(this);
    }

    protected void setCurrentWidth(float width) { current_width = width; }
    
    protected void setCurrentHeight(float height) { current_height = height; }
    
    protected void setDesiredWidth(float width) { this.desired_width = width; }
    
    protected void setDesiredHeight(float height) { this.desired_height = height; }
    
    protected void resizeHorizontal(float dx) { current_width += dx; }
    
    protected void resizeVertical(float dy) { current_height += dy; }
    
    protected float wiggleRoomX() { return current_width - desired_width; }
    
    protected float wiggleRoomY() { return current_height - desired_height; }
    
    protected float unlockedDesiredWidth() { return locked_horizontal ? 0 : desired_width; }
    
    protected float unlockedDesiredHeight() { return locked_vertical ? 0 : desired_height; }

    protected boolean getBoundsOf(Box target, Rectanglef dst, float x, float y) {
        if (this == target) {
            bounds(dst,x,y);
            return true;
        } return false;
    }

    protected void build() {
        if (built) throw new IllegalStateException("Box already built");
        if (desired_width <= 0) throw new IllegalStateException("Box: cannot build, desired width <= 0");
        if (desired_height <= 0) throw new IllegalStateException("Box: cannot build, desired height <= 0");
        current_width = desired_width;
        current_height = desired_height;
        built = true;
    }
    
    protected void adjustDesiredWidth(float dx) {
        desired_width += dx;
        current_width = desired_width;
    }
    
    protected void adjustDesiredHeight(float dy) {
        desired_height += dy;
        current_height = desired_height;
    }

    protected void restoreToDesiredSize() {
        current_width = desired_width;
        current_height = desired_height;
    }
    
    protected void toString(StringBuilder builder, int depth) {
        builder.append("\n");
        builder.append("\t".repeat(Math.max(0, depth)));
        builder.append(this);
    }
    
    public String toString() {
        return "Box { Width:" + current_width + " Height: " + current_height  +" }";
    }

    public void dispose() {
        Interactable.super.dispose();
        iID = 0;
    }
}
