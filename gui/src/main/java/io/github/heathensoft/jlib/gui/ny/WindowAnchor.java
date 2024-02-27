package io.github.heathensoft.jlib.gui.ny;

/**
 * @author Frederik Dahl
 * 31/10/2023
 */


public enum WindowAnchor {
    NONE(false,false,false,false),
    TOP(true,false,false,false),
    TOP_RIGHT(true,true,false,false),
    RIGHT(false,true,false,false),
    BOTTOM_RIGHT(false,true,true,false),
    BOTTOM(false,false,true,false),
    BOTTOM_LEFT(false,false,true,true),
    LEFT(false,false,false,true),
    TOP_LEFT(true,false,false,true);
    public final boolean anchored_top;
    public final boolean anchored_right;
    public final boolean anchored_bottom;
    public final boolean anchored_left;
    WindowAnchor(boolean anchored_top, boolean anchored_right, boolean anchored_bottom, boolean anchored_left) {
        this.anchored_top = anchored_top;
        this.anchored_right = anchored_right;
        this.anchored_bottom = anchored_bottom;
        this.anchored_left = anchored_left;
    }
}
