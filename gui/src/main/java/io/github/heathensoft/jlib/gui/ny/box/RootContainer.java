package io.github.heathensoft.jlib.gui.ny.box;


import io.github.heathensoft.jlib.gui.ny.GUI;
import io.github.heathensoft.jlib.gui.ny.WindowAnchor;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.CursorObjects;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 18/02/2024
 */


public class RootContainer extends BoxContainer {
    
    protected float max_width = Float.MAX_VALUE;
    protected float max_height = Float.MAX_VALUE;
    protected float border_padding;
    protected boolean interactable;
    protected int drag_zone;

    public RootContainer() { this(false); }

    public RootContainer(boolean interactable) {
        this.interactable_id = iObtainID();
        this.interactable = interactable;
    }

    /** Interactable root container is used to interact with the window*/
    public boolean isInteractable() { return interactable; }

    public float maxWidth() { return max_width; }
    
    public float maxHeight() { return max_height; }
    
    public float borderPadding() { return border_padding; }

    public void setStateInteractable(boolean b) {
        if (interactable &! b) {
            drag_zone = 0;
        } interactable = b;
    }
    
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
                    root.resizeHorizontal(dx);
                }
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
                    root.resizeVertical(dy);
                }
            } else if (dy < 0) root.resizeVertical(dy);
            current_height = root.current_height;
            current_height += (border_padding * 2);
        }
    }

    protected void processRootInteraction(BoxWindow context, float x, float y) {
        if (interactable) {

            Vector2f mouse_position = GUI.mouse_position(MathLib.vec2());
            WindowAnchor anchor = context.anchor();

            final int NONE      = 0x00;
            final int CENTER    = 0x01;
            final int TOP       = 0x02;
            final int RIGHT     = 0x04;
            final int BOTTOM    = 0x08;
            final int LEFT      = 0x10;

            final float mouse_x = mouse_position.x;
            final float mouse_y = mouse_position.y;
            final float width  = current_width;
            final float height = current_height;
            final float x_bounds = x + width;
            final float y_bounds = y - height;

            if (iClickedNotGrabbed(Mouse.LEFT)) {
                if (context.isMaximized())
                    context.restore();
                else context.maximize();
            }
            else if (iPressed(Mouse.LEFT)) {

                if (iJustPressed()) {
                    context.focus();
                }

                if (border_padding >= 1) {

                    boolean grabbed = iGrabbed(Mouse.LEFT);
                    final int TOP_RIGHT     = TOP       | RIGHT;
                    final int BOTTOM_RIGHT  = BOTTOM    | RIGHT;
                    final int BOTTOM_LEFT   = BOTTOM    | LEFT;
                    final int TOP_LEFT      = TOP       | LEFT;

                    if (drag_zone == NONE) {
                        if (!locked_horizontal) {
                            if (mouse_x <= (x + border_padding) &! anchor.anchored_left) { drag_zone |= LEFT; }
                            else if (mouse_x >= (x_bounds - border_padding) &! anchor.anchored_right) { drag_zone |= RIGHT; }
                        } if (!locked_vertical) {
                            if (mouse_y <= (y_bounds + border_padding) &! anchor.anchored_bottom) { drag_zone |= BOTTOM; }
                            else if (mouse_y >= (y - border_padding) &! anchor.anchored_top) { drag_zone |= TOP; }
                        } if (drag_zone == NONE) drag_zone = CENTER;
                    }

                    switch (drag_zone) {
                        case CENTER -> {
                            Vector2f drag_vector = GUI.mouse_drag_vector(MathLib.vec2(),Mouse.LEFT);
                            if (grabbed) context.move(drag_vector);
                        } case TOP -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            if (grabbed) context.dragTop(mouse_position);
                        } case TOP_RIGHT -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { context.dragTop(mouse_position); context.dragRight(mouse_position);}
                        } case RIGHT -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            if (grabbed) { context.dragRight(mouse_position); }
                        } case BOTTOM_RIGHT -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { context.dragBottom(mouse_position); context.dragRight(mouse_position);}
                        } case BOTTOM -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            if (grabbed) context.dragBottom(mouse_position);
                        } case BOTTOM_LEFT -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { context.dragBottom(mouse_position); context.dragLeft(mouse_position);}
                        } case LEFT -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            if (grabbed) { context.dragLeft(mouse_position);}
                        } case TOP_LEFT -> {GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                            if (grabbed) { context.dragTop(mouse_position); context.dragLeft(mouse_position);}
                        }default -> {}
                    }
                }
                else drag_zone = CENTER;
            }
            else { drag_zone = NONE;

                if (iHovered()) {

                    if (border_padding >= 1) {
                        if (locked_horizontal) {
                            if (!locked_vertical) {
                                if (mouse_y <= (y_bounds + border_padding)) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                } else if (mouse_y >= (y - border_padding)) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                }
                            }
                        } else {
                            if (locked_vertical) {
                                if (mouse_x <= (x + border_padding)) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                } else if (mouse_x >= (x_bounds - border_padding)) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                }
                            } else {
                                if (mouse_x <= (x + border_padding)) {
                                    if (mouse_y <= (y_bounds + border_padding)) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else if (mouse_y >= (y - border_padding)) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                } else if (mouse_x >= (x_bounds - border_padding)) {
                                    if (mouse_y <= (y_bounds + border_padding)) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else if (mouse_y >= (y - border_padding)) {
                                        GUI.state.useCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                    } else GUI.state.useCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                                } else if (mouse_y <= (y_bounds + border_padding)) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                } else if (mouse_y >= (y - border_padding)) {
                                    GUI.state.useCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void adjustDesiredWidth(float dx) { throw new RuntimeException("Illegal call for RootContainer"); }
    
    protected void adjustDesiredHeight(float dy) { throw new RuntimeException("Illegal call for RootContainer"); }
    
    protected float unlockedDesiredWidth() { throw new RuntimeException("Illegal call for RootContainer"); }
    
    protected float unlockedDesiredHeight() { throw new RuntimeException("Illegal call for RootContainer"); }
    
    protected float innerSpacingSumHorizontal() { throw new RuntimeException("Illegal call for RootContainer"); }
    
    protected float innerSpacingSumVertical() { throw new RuntimeException("Illegal call for RootContainer"); }
    
    public String toString() {
        if (built) {
            StringBuilder builder = new StringBuilder(1024);
            contents.get(0).toString(builder,0);
            return builder.toString();
        } return "RootContainer";
    }
}
