package io.github.heathensoft.jlib.ui.box;


import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.CursorObjects;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.WindowAnchor;
import org.joml.Vector2f;

/**
 * This is the Root Container of the Box Window. It determines the max width / height and border padding for the window.
 *
 *
 * @author Frederik Dahl
 * 18/02/2024
 */


public abstract class RootContainer extends BoxContainer {

    protected float max_width = Float.MAX_VALUE;
    protected float max_height = Float.MAX_VALUE;
    protected float border_padding;
    protected int drag_zone;

    public float maxWidth() { return max_width; }
    public float maxHeight() { return max_height; }
    public float borderPadding() { return border_padding; }


    public void setMaxHeight(float height) {
        if (built) throw new IllegalStateException("Cannot adjust built container");
        this.max_height = height;
    }
    
    public void setMaxWidth(float width) {
        if (built) throw new IllegalStateException("Cannot adjust built container");
        this.max_width = width;
    }
    
    public void setBorderPadding(float padding) {
        if (built) throw new IllegalStateException("Cannot adjust built container");
        this.border_padding = Math.max(padding,0);
    }
    
    public void build() {
        if (built) throw new IllegalStateException("BoxContainer already built");
        if (isEmpty()) throw new IllegalStateException("Cannot build empty BoxContainer");
        Box root = contents.get(0);
        root.build();
        desired_width = root.desired_width;
        desired_height = root.desired_height;
        desired_width += (2 * border_padding);
        desired_height += (2 * border_padding);
        current_width = desired_width;
        current_height = desired_height;
        locked_horizontal = root.locked_horizontal;
        locked_vertical = root.locked_vertical;
        max_width = Math.max(current_width,max_width);
        max_height = Math.max(current_height,max_height);
        built = true;
    }
    
    public void resize(float dx, float dy) {
        resizeHorizontal(dx);
        resizeVertical(dy);
    }
    
    public void resizeHorizontal(float dx) {
        if (!locked_horizontal) {
            Box root = contents.get(0);
            if (dx > 0) {
                float wiggle = max_width - current_width;
                if (wiggle > 0) {
                    dx = Math.min(wiggle,dx);
                    root.resizeHorizontal(dx);}
            } else if (dx < 0) root.resizeHorizontal(dx);
            current_width = root.current_width;
            current_width += (border_padding * 2);
        }
    }
    
    public void resizeVertical(float dy) {
        if (!locked_vertical) {
            Box root = contents.get(0);
            if (dy > 0) {
                float wiggle = max_height - current_height;
                if (wiggle > 0) {
                    dy = Math.min(wiggle,dy);
                    root.resizeVertical(dy);}
            } else if (dy < 0) root.resizeVertical(dy);
            current_height = root.current_height;
            current_height += (border_padding * 2);
        }
    }

    protected void windowResizeEvents(BoxWindow window, float x, float y) {
        if (iHasID()) {
            Vector2f mouse_position = GUI.mousePosition(U.popVec2());
            WindowAnchor anchor = window.anchor();
            final int N = 0x00; final int C = 0x01;
            final int T = 0x02; final int R = 0x04;
            final int B = 0x08; final int L = 0x10;
            final float mx = mouse_position.x;
            final float my = mouse_position.y;
            final float w  = current_width;
            final float h = current_height;
            final float x_max = x + w;
            final float y_min = y - h;
            U.pushVec2();
            if (iClickedNotGrabbed(Mouse.LEFT)) {
                if (window.isMaximized())
                    window.restore();
                else window.maximize();
            } else if (iPressed(Mouse.LEFT)) {
                if (iJustPressed()) {
                    GUI.windows.focusRequest(window);
                } if (border_padding >= 1) {
                    boolean grabbed = iGrabbed(Mouse.LEFT);
                    final int TR = T | R; final int BR = B | R;
                    final int BL = B | L; final int TL = T | L;
                    if (drag_zone == N) {
                        if (!locked_horizontal) {
                            if (mx <= (x + border_padding) &! anchor.anchored_left) { drag_zone |= L; }
                            else if (mx >= (x_max - border_padding) &! anchor.anchored_right) { drag_zone |= R; }
                        } if (!locked_vertical) {
                            if (my <= (y_min + border_padding) &! anchor.anchored_bottom) { drag_zone |= B; }
                            else if (my >= (y - border_padding) &! anchor.anchored_top) { drag_zone |= T; }
                        } if (drag_zone == N) drag_zone = C;
                    } switch (drag_zone) {
                        case C -> { Vector2f drag_vector = GUI.mouseDragVector(U.popVec2(),Mouse.LEFT);
                            if (grabbed) window.move(drag_vector); U.pushVec2();
                        } case T  -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            if (grabbed) window.dragTop(mouse_position);
                        } case TR -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { window.dragTop(mouse_position); window.dragRight(mouse_position);}
                        } case R -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            if (grabbed) { window.dragRight(mouse_position); }
                        } case BR -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { window.dragBottom(mouse_position); window.dragRight(mouse_position);}
                        } case B -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            if (grabbed) window.dragBottom(mouse_position);
                        } case BL -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { window.dragBottom(mouse_position); window.dragLeft(mouse_position);}
                        } case L -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            if (grabbed) { window.dragLeft(mouse_position);}
                        } case TL -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { window.dragTop(mouse_position); window.dragLeft(mouse_position);}
                        }default -> {}
                    }
                }  else drag_zone = C;
            }  else { drag_zone = N;
                if (iHovered()) {
                    if (border_padding >= 1) {
                        if (locked_horizontal) {
                            if (!locked_vertical) {
                                if (my <= (y_min + border_padding) &!anchor.anchored_bottom) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                } else if (my >= (y - border_padding) &! anchor.anchored_top) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                }
                            }
                        } else {
                            if (locked_vertical) {
                                if (mx <= (x + border_padding) &!anchor.anchored_left) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                } else if (mx >= (x_max - border_padding) &!anchor.anchored_right) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);}
                            } else { if (mx <= (x + border_padding) &! anchor.anchored_left) {
                                    if (my <= (y_min + border_padding) &! anchor.anchored_bottom) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else if (my >= (y - border_padding) &! anchor.anchored_top) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                } else if (mx >= (x_max - border_padding) &! anchor.anchored_right) {
                                    if (my <= (y_min + border_padding) &! anchor.anchored_bottom) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else if (my >= (y - border_padding) &! anchor.anchored_top) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                } else if (my <= (y_min + border_padding) &! anchor.anchored_bottom) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                } else if (my >= (y - border_padding) &! anchor.anchored_top) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected final void adjustDesiredWidth(float dx) { throw new RuntimeException("Illegal call for RootContainer"); }
    protected final void adjustDesiredHeight(float dy) { throw new RuntimeException("Illegal call for RootContainer"); }
    protected final float unlockedDesiredWidth() { throw new RuntimeException("Illegal call for RootContainer"); }
    protected final float unlockedDesiredHeight() { throw new RuntimeException("Illegal call for RootContainer"); }
    protected final float innerSpacingSumHorizontal() { throw new RuntimeException("Illegal call for RootContainer"); }
    protected final float innerSpacingSumVertical() { throw new RuntimeException("Illegal call for RootContainer"); }
    
    public String toString() {
        if (built) {
            StringBuilder builder = new StringBuilder(1024);
            contents.get(0).toString(builder,0);
            return builder.toString();
        } return "RootContainer";
    }
}
