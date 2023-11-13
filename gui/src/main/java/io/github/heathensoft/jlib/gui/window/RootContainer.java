package io.github.heathensoft.jlib.gui.window;


import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.CursorObjects;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 06/11/2023
 */


public abstract class RootContainer extends BoxContainer {

    protected Size max_desired_size = new Size();
    protected int drag_area;
    protected int padding;

    public RootContainer() {
        this(0);
    }

    public RootContainer(int padding) {
        this(9999,9999,padding);
    }

    public RootContainer(int max_content_width, int max_content_height, int padding) {
        this.max_desired_size.set(max_content_width + 2 * padding, max_content_height + 2 * padding);
        this.padding = padding;
        this.id = iObtainID();
    }

    public void addBox(Box box) {
        if (contents.isEmpty()) {
            box.restore();
            restingSize.set(box.restingSize);
            restingSize.add(padding * 2, padding * 2);
            currentSize.set(restingSize);
            max_desired_size.setWidth(Math.max(max_desired_size.width(),currentSize.width()));
            max_desired_size.setHeight(Math.max(max_desired_size.height(),currentSize.height()));
            lockedVertical = box.lockedVertical;
            lockedHorizontal = box.lockedHorizontal;
            contents.add(box);
        }
    }

    protected void resizeHorizontal(float dx) {
        Box content = contents.get(0);
        content.resizeHorizontal(dx);
        currentSize.set(content.currentSize).add(padding * 2);
    }

    protected void resizeVertical(float dy) {
        Box content = contents.get(0);
        content.resizeVertical(dy);
        currentSize.set(content.currentSize).add(padding * 2);
    }

    protected void processRootInteraction(WindowGUI context, float x, float y) {

        final int NONE = 0;
        final int CENTER = 1;
        final int TOP = 2;
        final int RIGHT = 4;
        final int BOTTOM = 8;
        final int LEFT = 16;

        Vector2f mouse_position = context.mouse_position(MathLib.vec2());
        float mouse_x = mouse_position.x;
        float mouse_y = mouse_position.y;
        float width = currentSize.width();
        float height = currentSize.height();
        Anchor anchor = context.anchor;

        if (iClickedNotGrabbed(Mouse.LEFT)) {
            if (!context.isMaximized()) {
                context.maximize();
            } else if (!context.isRestored()) {
                context.restore();
            }
        }
        else if (iPressed(Mouse.LEFT)) {

            if (iJustPressed()) context.focus();

            if (padding > 0) {

                boolean grabbed = iGrabbed(Mouse.LEFT);

                if (drag_area == NONE) {
                    if (!lockedHorizontal) {
                        if (mouse_x <= (x + padding) &! anchor.anchored_left) { drag_area |= LEFT;
                        } else if (mouse_x >= (x + width - padding) &! anchor.anchored_right) { drag_area |= RIGHT; }
                    } if (!lockedVertical) {
                        if (mouse_y <= (y - height + padding) &! anchor.anchored_bottom) { drag_area |= BOTTOM;
                        } else if (mouse_y >= (y - padding) &! anchor.anchored_top) { drag_area |= TOP; }
                    } if (drag_area == NONE) drag_area = CENTER;
                }

                final int TOP_RIGHT =       TOP | RIGHT;
                final int BOTTOM_RIGHT =    BOTTOM | RIGHT;
                final int BOTTOM_LEFT =     BOTTOM | LEFT;
                final int TOP_LEFT =        TOP | LEFT;

                switch (drag_area) {
                    case CENTER -> {
                        Vector2f drag_vector = context.mouse_drag_vector(MathLib.vec2(),Mouse.LEFT);
                        if (grabbed) context.move(drag_vector);
                    } case TOP -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                        if (grabbed) context.dragTop();
                    } case TOP_RIGHT -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                        if (grabbed) { context.dragTop(); context.dragRight();}
                    } case RIGHT -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                        if (grabbed) context.dragRight();
                    } case BOTTOM_RIGHT -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                        if (grabbed) { context.dragBottom(); context.dragRight();}
                    } case BOTTOM -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                        if (grabbed) context.dragBottom();
                    } case BOTTOM_LEFT -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                        if (grabbed) { context.dragBottom(); context.dragLeft();}
                    } case LEFT -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                        if (grabbed) context.dragLeft();
                    } case TOP_LEFT -> {
                        GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                        if (grabbed) { context.dragTop(); context.dragLeft();}
                    }default -> {}
                }
            } else { drag_area = CENTER; }
        } else {
            drag_area = NONE;
            if (iHovered()) {
                if (padding > 0) {
                    if (lockedHorizontal) {
                        if (!lockedVertical) {
                            if (mouse_y <= (y - height + padding)) {
                                GUI.State.setCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            } else if (mouse_y >= (y - padding)) {
                                GUI.State.setCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            }
                        }
                    } else {
                        if (lockedVertical) {
                            if (mouse_x <= (x + padding)) {
                                GUI.State.setCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            } else if (mouse_x >= (x + width - padding)) {
                                GUI.State.setCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            }
                        } else {
                            if (mouse_x <= (x + padding)) {
                                if (mouse_y <= (y - height + padding)) {
                                    GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                } else if (mouse_y >= (y - padding)) {
                                    GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                } else GUI.State.setCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            } else if (mouse_x >= (x + width - padding)) {
                                if (mouse_y <= (y - height + padding)) {
                                    GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                } else if (mouse_y >= (y - padding)) {
                                    GUI.State.setCursorIcon(CursorObjects.CURSOR_CROSS_HAIR);
                                } else GUI.State.setCursorIcon(CursorObjects.CURSOR_H_RESIZE);
                            } else if (mouse_y <= (y - height + padding)) {
                                GUI.State.setCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            } else if (mouse_y >= (y - padding)) {
                                GUI.State.setCursorIcon(CursorObjects.CURSOR_V_RESIZE);
                            }
                        }
                    }
                }
            }
        }
    }


}
