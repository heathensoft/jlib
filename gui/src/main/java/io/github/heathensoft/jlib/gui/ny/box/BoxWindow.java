package io.github.heathensoft.jlib.gui.ny.box;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.ny.GUI;
import io.github.heathensoft.jlib.gui.ny.WindowAnchor;
import io.github.heathensoft.jlib.gui.ny.WindowGUI;
import io.github.heathensoft.jlib.gui.ny.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.floor;

/**
 * @author Frederik Dahl
 * 23/02/2024
 */


public class BoxWindow extends WindowGUI {

    protected static final int STATE_DRAGGING = 0x0010;
    protected static final int STATE_RESTORING_X = 0x0020;
    protected static final int STATE_RESTORING_Y = 0x0040;
    protected static final int STATE_MAXIMIZING_X = 0x0080;
    protected static final int STATE_MAXIMIZING_Y = 0x0100;
    protected static final int STATE_MAXIMIZED_X = 0x0200;
    protected static final int STATE_MAXIMIZED_Y = 0x0400;

    protected WindowAnchor anchor;
    protected RootContainer content;
    protected Rectanglef transform_initial = new Rectanglef();
    protected Rectanglef transform_target = new Rectanglef();
    protected Vector2f position = new Vector2f();
    protected float transform_timer;

    /**
     * Creates a new Box Window.
     * Content box is built, and window is registered.
     * @param content content
     * @param anchor anchor
     * @param name name
     * @throws Exception window already registered
     * or if another window is registered with the same name.
     */
    public void create(RootContainer content, WindowAnchor anchor, String name) throws Exception {
        if (isRegistered()) throw new Exception("GUI Window: \"" + this.name + "\" already registered");
        if (content == null || anchor == null || name == null) throw new RuntimeException("GUI Window: null argument");
        if (content.isBuilt()) content.restoreToDesiredSize();
        else content.build();
        this.content = content;
        this.anchor = anchor;
        this.name = name;
        register();
    }

    protected void onRegistered() {
        Resolution resolution = GUI.resolution();
        float screen_width = resolution.width();
        float screen_height = resolution.height();
        float screen_center_x = screen_width / 2f;
        float screen_center_y = screen_height / 2f;
        float content_width = content.currentWidth();
        float content_height = content.currentHeight();
        switch (anchor) {
            case NONE -> {
                position.x = screen_center_x - (content_width / 2f);
                position.y = screen_center_y + (content_height / 2f);
            }case TOP -> {
                position.x = screen_center_x - (content_width / 2f);
                position.y = screen_height;
            }case TOP_RIGHT -> {
                position.x = screen_width - content_width;
                position.y = screen_height;
            }case RIGHT -> {
                position.x = screen_width - content_width;
                position.y = screen_center_y + (content_height / 2f);
            }case BOTTOM_RIGHT -> {
                position.x = screen_width - content_width;
                position.y = content_height;
            }case BOTTOM -> {
                position.x = screen_center_x - (content_width / 2f);
                position.y = content_height;
            }case BOTTOM_LEFT -> {
                position.x = 0;
                position.y = content_height;
            }case LEFT -> {
                position.x = 0;
                position.y = screen_center_y + (content_height / 2f);
            }case TOP_LEFT -> {
                position.x = 0;
                position.y = screen_height;
            }
        }
        bounds(transform_initial);
        transform_target.set(transform_initial);
        transform_timer = 1.0f;
        setStateCurrentlyRestoringX();
        setStateCurrentlyRestoringY();
        content.onWindowRegistered(this,null);
    }

    protected void update(float dt) { }

    protected void onOpen() { content.onWindowOpen(this); }

    protected void onClose() { content.onWindowClose(this); }

    protected void onDestroyed() {
        content.dispose();
        content = null;
        anchor = null;
    }

    protected void render(RendererGUI renderer, float dt) {
        Rectanglef bounds = bounds(MathLib.rectf());
        Resolution resolution = GUI.resolution();
        final float screen_width = resolution.width();
        final float screen_height = resolution.height();
        final float content_width = content.currentWidth();
        final float content_height = content.currentHeight();
        final float content_desired_width = content.desiredWidth();
        final float content_desired_height = content.desiredHeight();
        final float screen_center_x = screen_width / 2f;
        final float screen_center_y = screen_height / 2f;
        final boolean offScreen = isOffScreen();
        if (isCurrentlyMaximizingX()) {
            float maximum_width = maximumWidth();
            switch (anchor) {
                case NONE -> {
                    float window_center_x = position.x + (content_width / 2f);
                    transform_target.minX = window_center_x - (maximum_width / 2f);
                    transform_target.maxX = window_center_x + (maximum_width / 2f);
                    if (transform_target.minX < 0) {
                        float adjust = - transform_target.minX;
                        transform_target.minX += adjust;
                        transform_target.maxX += adjust;
                    } else if (transform_target.maxX > screen_width) {
                        float adjust = transform_target.maxX - screen_width;
                        transform_target.minX -= adjust;
                        transform_target.maxX -= adjust;
                    }
                }
                case TOP, BOTTOM -> {
                    transform_target.minX = screen_center_x - (maximum_width / 2f);
                    transform_target.maxX = screen_center_x + (maximum_width / 2f);
                } case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                    transform_target.minX = screen_width - maximum_width;
                    transform_target.maxX = screen_width;
                } case BOTTOM_LEFT, LEFT, TOP_LEFT -> {
                    transform_target.minX = 0;
                    transform_target.maxX = maximum_width;
                }
            }
        }  else if (isCurrentlyRestoringX()) {
            if (offScreen) {
                switch (anchor) {
                    case NONE, TOP, BOTTOM -> {
                        transform_target.minX = screen_center_x - (content_desired_width / 2f);
                        transform_target.maxX = screen_center_x + (content_desired_width / 2f);
                    } case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                        transform_target.minX = screen_width - content_desired_width;
                        transform_target.maxX = screen_width;
                    } case BOTTOM_LEFT, LEFT, TOP_LEFT -> {
                        transform_target.minX = 0;
                        transform_target.maxX = content_desired_width;
                    }
                }
            }
        }  else {
            switch (anchor) {
                case NONE, TOP, BOTTOM -> { }
                case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                    transform_target.minX = screen_width - transform_target.lengthX();
                    transform_target.maxX = screen_width;
                } case BOTTOM_LEFT, LEFT, TOP_LEFT -> {
                    transform_target.maxX = transform_target.lengthX();
                    transform_target.minX = 0;
                }
            }
        }
        if (isCurrentlyMaximizingY()) {
            float maximum_height = maximumHeight();
            switch (anchor) {
                case NONE -> {
                    float window_center_y = position.y - (content_height / 2f);
                    transform_target.minY = window_center_y - (maximum_height / 2f);
                    transform_target.maxY = window_center_y + (maximum_height / 2f);
                    if (transform_target.minY < 0) {
                        float adjust = - transform_target.minY;
                        transform_target.minY += adjust;
                        transform_target.maxY += adjust;
                    } else if (transform_target.maxY > screen_height) {
                        float adjust = transform_target.maxY - screen_height;
                        transform_target.minY -= adjust;
                        transform_target.maxY -= adjust;
                    }
                }
                case RIGHT, LEFT -> {
                    transform_target.minY = screen_center_y - (maximum_height / 2f);
                    transform_target.maxY = screen_center_y + (maximum_height / 2f);
                } case TOP, TOP_RIGHT, TOP_LEFT -> {
                    transform_target.minY = screen_height - maximum_height;
                    transform_target.maxY = screen_height;
                } case BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT -> {
                    transform_target.minY = 0;
                    transform_target.maxY = maximum_height;
                }
            }
        }  else if (isCurrentlyRestoringY()) {
            if (offScreen) {
                switch (anchor) {
                    case NONE, RIGHT, LEFT -> {
                        transform_target.minY = screen_center_y - (content_desired_height / 2f);
                        transform_target.maxY = screen_center_y + (content_desired_height / 2f);
                    } case TOP, TOP_RIGHT, TOP_LEFT -> {
                        transform_target.minY = screen_height - content_desired_height;
                        transform_target.maxY = screen_height;
                    } case BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT -> {
                        transform_target.minY = 0;
                        transform_target.maxY = content_desired_height;
                    }
                }
            }
        }  else  {
            switch (anchor) {
                case NONE, RIGHT, LEFT -> { }
                case TOP, TOP_RIGHT, TOP_LEFT -> {
                    transform_target.minY = screen_height - transform_target.lengthY();
                    transform_target.maxY = screen_height;
                } case BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT -> {
                    transform_target.minY = 0;
                    transform_target.maxY = transform_target.lengthY();
                }
            }
        }
        // Transform ***********************************************************
        Rectanglef transform = MathLib.rectf();
        if (isTransforming()) {
            transform_timer += (dt * 5f);
            float t = U.smooth(U.clamp(transform_timer));
            transform_interpolation(t,transform_initial, transform_target,transform);
        } else transform.set(transform_target);

        if (!transform.equals(bounds)) {
            float trans_x = transform.minX - bounds.minX;
            float trans_y = transform.maxY - bounds.maxY;
            float resize_x = transform.lengthX() - bounds.lengthX();
            float resize_y = transform.lengthY() - bounds.lengthY();
            position.x = position.x + trans_x;
            position.y = position.y + trans_y;
            content.resizeHorizontal(resize_x);
            content.resizeVertical(resize_y);
        }
        // Update whether window is maximized ***********************************
        int maximum_width = round(maximumWidth());
        int width = round(content.currentWidth());
        if (width >= maximum_width) setStateIsMaximizedX();
        else clearStateIsMaximizedX();
        int maximum_height = round(maximumHeight());
        int height = round(content.currentHeight());
        if (height >= maximum_height) setStateIsMaximizedY();
        else clearStateIsMaximizedY();

        content.render(this,renderer,(int)position.x,(int)position.y,dt,0);
        content.renderText(this,renderer,(int)position.x,(int)position.y,dt);
        if (!GUI.state.anyInteractablePressed()) { clearStateCurrentlyDragging(); }
    }

    public void restoreX() {
        if (!isCurrentlyRestoringX()) {
            setStateCurrentlyRestoringX();
            clearStateCurrentlyMaximizingX();
            transform_timer = 0;
            transform_target.minX = transform_initial.minX;
            transform_target.maxX = transform_initial.maxX;
            transform_initial.minX = position.x;
            transform_initial.maxX = position.x + content.currentWidth();
            int current_width_discrete = round(transform_initial.lengthY());
            int desired_width_discrete = round(transform_target.lengthY());
            if (current_width_discrete == desired_width_discrete) {
                Resolution resolution = GUI.resolution();
                float screen_width = resolution.width();
                float desired_width = content.desiredWidth();
                switch (anchor) {
                    case NONE -> {
                        float window_center_x = (transform_initial.maxX + transform_initial.minX) / 2f;
                        transform_target.minX = window_center_x - (desired_width / 2f);
                        transform_target.maxX = window_center_x + (desired_width / 2f);
                    } case TOP, BOTTOM -> {
                        float screen_center_x = screen_width / 2f;
                        transform_target.minX = screen_center_x - (desired_width / 2f);
                        transform_target.maxX = screen_center_x + (desired_width / 2f);
                    } case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                        transform_target.minX = screen_width - desired_width;
                        transform_target.maxX = screen_width;
                    } case BOTTOM_LEFT, LEFT, TOP_LEFT -> {
                        transform_target.minX = 0f;
                        transform_target.maxX = desired_width;
                    }
                }
            }
        }
    }

    public void restoreY() {
        if (!isCurrentlyRestoringY()) {
            setStateCurrentlyRestoringY();
            clearStateCurrentlyMaximizingY();
            transform_timer = 0;
            transform_target.minY = transform_initial.minY;
            transform_target.maxY = transform_initial.maxY;
            transform_initial.minY = position.y - content.currentWidth();
            transform_initial.maxY = position.y;
            int current_height_discrete = round(transform_initial.lengthY());
            int desired_height_discrete = round(transform_target.lengthY());
            if (current_height_discrete == desired_height_discrete) {
                Resolution resolution = GUI.resolution();
                float screen_height = resolution.height();
                float screen_center_y = screen_height / 2f;
                float window_center_y = (transform_initial.maxY + transform_initial.minY) / 2f;
                float desired_height = content.desiredHeight();
                switch (anchor) {
                    case NONE -> {
                        transform_target.minY = window_center_y - (desired_height / 2f);
                        transform_target.maxY = window_center_y + (desired_height / 2f);
                    } case RIGHT, LEFT -> {
                        transform_target.minY = screen_center_y - (desired_height / 2f);
                        transform_target.maxY = screen_center_y + (desired_height / 2f);
                    }case TOP, TOP_RIGHT, TOP_LEFT -> {
                        transform_target.minY = screen_height - desired_height;
                        transform_target.maxY = screen_height;
                    } case BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT -> {
                        transform_target.maxY = desired_height;
                        transform_target.minY = 0f;
                    }
                }
            }
        }
    }

    public void restore() {
        if (!(isCurrentlyRestoringX() && isCurrentlyRestoringY())) {
            setStateCurrentlyRestoringX();
            setStateCurrentlyRestoringY();
            clearStateCurrentlyMaximizingX();
            clearStateCurrentlyMaximizingY();
            transform_timer = 0;
            transform_target.set(transform_initial);
            bounds(transform_initial);
            if (round(transform_initial.lengthX()) == round(transform_target.lengthX())) {
                if (round(transform_initial.lengthY()) == round(transform_target.lengthY())) {
                    Resolution resolution = GUI.resolution();
                    float screen_width = resolution.width();
                    float screen_height = resolution.height();
                    float screen_center_x = screen_width / 2f;
                    float screen_center_y = screen_height / 2f;
                    float desired_width = content.desiredWidth();
                    float desired_height = content.desiredHeight();
                    switch (anchor) {
                        case NONE -> {
                            float window_center_y = (transform_initial.maxY + transform_initial.minY) / 2f;
                            float window_center_x = (transform_initial.maxX + transform_initial.minX) / 2f;
                            transform_target.minX = window_center_x - (desired_width / 2f);
                            transform_target.maxX = window_center_x + (desired_width / 2f);
                            transform_target.minY = window_center_y - (desired_height / 2f);
                            transform_target.maxY = window_center_y + (desired_height / 2f);
                        } case TOP -> {
                            transform_target.minX = screen_center_x - (desired_width / 2f);
                            transform_target.maxX = screen_center_x + (desired_width / 2f);
                            transform_target.minY = screen_height - desired_height;
                            transform_target.maxY = screen_height;
                        } case BOTTOM -> {
                            transform_target.minX = screen_center_x - (desired_width / 2f);
                            transform_target.maxX = screen_center_x + (desired_width / 2f);
                            transform_target.maxY = desired_height;
                            transform_target.minY = 0f;
                        } case TOP_RIGHT -> {
                            transform_target.minX = screen_width - desired_width;
                            transform_target.maxX = screen_width;
                            transform_target.minY = screen_height - desired_height;
                            transform_target.maxY = screen_height;
                        } case RIGHT -> {
                            transform_target.minX = screen_width - desired_width;
                            transform_target.maxX = screen_width;
                            transform_target.minY = screen_center_y - (desired_height / 2f);
                            transform_target.maxY = screen_center_y + (desired_height / 2f);
                        } case BOTTOM_RIGHT -> {
                            transform_target.minX = screen_width - desired_width;
                            transform_target.maxX = screen_width;
                            transform_target.maxY = desired_height;
                            transform_target.minY = 0f;
                        } case BOTTOM_LEFT -> {
                            transform_target.minX = 0f;
                            transform_target.maxX = desired_width;
                            transform_target.maxY = desired_height;
                            transform_target.minY = 0f;
                        } case LEFT -> {
                            transform_target.minX = 0f;
                            transform_target.maxX = desired_width;
                            transform_target.minY = screen_center_y - (desired_height / 2f);
                            transform_target.maxY = screen_center_y + (desired_height / 2f);
                        } case TOP_LEFT -> {
                            transform_target.minX = 0f;
                            transform_target.maxX = desired_width;
                            transform_target.minY = screen_height - desired_height;
                            transform_target.maxY = screen_height;
                        }
                    }
                }
            }
        }
    }

    public void maximizeX() {
        if (!isCurrentlyMaximizingX()) {
            clearStateCurrentlyRestoringX();
            setStateCurrentlyMaximizingX();
            transform_timer = 0;
            transform_initial.minX = position.x;
            transform_initial.maxX = position.x + content.currentWidth();
        }
    }

    public void maximizeY() {
        if (!isCurrentlyMaximizingY()) {
            clearStateCurrentlyRestoringY();
            setStateCurrentlyMaximizingY();
            transform_timer = 0;
            transform_initial.minY = position.y - content.currentHeight();
            transform_initial.maxY = position.y;
        }
    }

    public void maximize() {
        maximizeX();
        maximizeY();
    }

    public void move(Vector2f drag_vector) {
        if (anchor == WindowAnchor.NONE) {
            if (!isScreenSizedOrLarger()) {
                if (!isCurrentlyDragging()) {
                    setStateCurrentlyDragging();
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                clearStateCurrentlyRestoringX();
                clearStateCurrentlyRestoringY();
                clearStateCurrentlyMaximizingX();
                clearStateCurrentlyMaximizingY();
                Vector2f origin_translation = MathLib.vec2(
                        transform_initial.minX,
                        transform_initial.maxY);
                origin_translation.sub(
                        transform_target.minX,
                        transform_target.maxY);
                transform_target.translate(origin_translation);
                translate(drag_vector);
            }
        }
    }

    public void dragLeft(Vector2f mouse) {
        if (!content.isLockedHorizontal()) {
            if (!anchor.anchored_left) {
                if (!isCurrentlyDragging()) {
                    setStateCurrentlyDragging();
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                clearStateCurrentlyRestoringX();
                clearStateCurrentlyRestoringY();
                clearStateCurrentlyMaximizingX();
                clearStateCurrentlyMaximizingY();
                Resolution resolution = GUI.resolution();
                float origin_x = transform_initial.minX;
                float mouse_x = mouse.x;
                mouse_x = U.clamp(mouse_x,0,resolution.width());
                float dx = mouse_x - origin_x;
                float x = origin_x + dx;
                float width = transform_initial.maxX - x;
                if (dx > 0) {
                    float min_width = content.desiredWidth();
                    width = Math.max(min_width,width);
                    x = transform_initial.maxX - width;
                    transform_target.minX = x;
                } else if (dx < 0) {
                    float max_width = content.maxWidth();
                    width = Math.min(max_width,width);
                    x = transform_initial.maxX - width;
                    transform_target.minX = x;
                }
            }
        }
    }

    public void dragRight(Vector2f mouse) {
        if (!content.isLockedHorizontal()) {
            if (!anchor.anchored_right) {
                if (!isCurrentlyDragging()) {
                    setStateCurrentlyDragging();
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                clearStateCurrentlyRestoringX();
                clearStateCurrentlyRestoringY();
                clearStateCurrentlyMaximizingX();
                clearStateCurrentlyMaximizingY();
                Resolution resolution = GUI.resolution();
                float origin_x = transform_initial.maxX;
                float mouse_x = mouse.x;
                mouse_x = U.clamp(mouse_x,0,resolution.width());
                float dx = mouse_x - origin_x;
                float x = origin_x + dx;
                float width = x - transform_initial.minX;
                if (dx < 0) {
                    float min_width = content.desiredWidth();
                    width = Math.max(min_width,width);
                    x = transform_initial.minX + width;
                    transform_target.maxX = x;
                } else if (dx > 0) {
                    float max_width = content.maxWidth();
                    width = Math.min(max_width,width);
                    x = transform_initial.minX + width;
                    transform_target.maxX = x;
                }
            }
        }
    }

    public void dragTop(Vector2f mouse) {
        if (!content.isLockedVertical()) {
            if (!anchor.anchored_top) {
                if (!isCurrentlyDragging()) {
                    setStateCurrentlyDragging();
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                clearStateCurrentlyRestoringX();
                clearStateCurrentlyRestoringY();
                clearStateCurrentlyMaximizingX();
                clearStateCurrentlyMaximizingY();
                Resolution resolution = GUI.resolution();
                float origin_y = transform_initial.maxY;
                float mouse_y = mouse.y;
                mouse_y = U.clamp(mouse_y,0,resolution.height());
                float dy = mouse_y - origin_y;
                float y = origin_y + dy;
                float height = y - transform_initial.minY;
                if (dy < 0) {
                    float min_height = content.desiredHeight();
                    height = Math.max(min_height,height);
                    y = transform_initial.minY + height;
                    transform_target.maxY = y;
                } else if (dy > 0) {
                    float max_height = content.maxHeight();
                    height = Math.min(max_height,height);
                    y = transform_initial.minY + height;
                    transform_target.maxY = y;
                }
            }
        }
    }

    public void dragBottom(Vector2f mouse) {
        if (!content.isLockedVertical()) {
            if (!anchor.anchored_bottom) {
                if (!isCurrentlyDragging()) {
                    setStateCurrentlyDragging();
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                clearStateCurrentlyRestoringX();
                clearStateCurrentlyRestoringY();
                clearStateCurrentlyMaximizingX();
                clearStateCurrentlyMaximizingY();
                Resolution resolution = GUI.resolution();
                float origin_y = transform_initial.minY;
                float mouse_y = mouse.y;
                mouse_y = U.clamp(mouse_y,0,resolution.height());
                float dy = mouse_y - origin_y;
                float y = origin_y + dy;
                float height = transform_initial.maxY - y;
                if (dy > 0) {
                    float min_height = content.desiredHeight();
                    height = Math.max(min_height,height);
                    y = transform_initial.maxY - height;
                    transform_target.minY = y;
                } else if (dy < 0) {
                    float max_height = content.maxHeight();
                    height = Math.min(max_height,height);
                    y = transform_initial.maxY - height;
                    transform_target.minY = y;
                }
            }
        }
    }

    public float width() { return content.currentWidth(); }

    public float height() { return content.currentHeight(); }

    public boolean isTransforming() { return transform_timer < 1.0f; }

    public RootContainer content() { return content; }

    public WindowAnchor anchor() { return anchor; }

    public Vector2f position(Vector2f dst) { return dst.set(position); }

    public Rectanglef bounds(Rectanglef dst) {
        dst.minX = position.x;
        dst.minY = position.y - content.currentHeight();
        dst.maxX = position.x + content.currentWidth();
        dst.maxY = position.y;
        return dst;
    }

    public float maximumWidth() { float max_width;
        float content_width = content.currentWidth();
        if (content.isLockedHorizontal()) {
            max_width = content_width;
        } else { Resolution resolution = GUI.resolution();
            float gui_width = resolution.width();
            float content_max_width = content.maxWidth();
            max_width = Math.min(gui_width,content_max_width);
            max_width = Math.max(max_width,content_width);
        } return max_width;
    }

    public float maximumHeight() { float max_height;
        float content_height = content.currentHeight();
        if(content.isLockedVertical()) {
            max_height = content_height;
        } else { Resolution resolution = GUI.resolution();
            float gui_height = resolution.height();
            float content_max_height = content.maxHeight();
            max_height = Math.min(gui_height,content_max_height);
            max_height = Math.max(max_height,content_height);
        } return max_height;
    }

    public boolean isAbleToFitOnScreen() {
        Resolution resolution = GUI.resolution();
        if (content.desiredWidth() > resolution.width()) return false;
        return ! (content.desiredHeight() > resolution.height());
    }

    public boolean isScreenSizedOrLarger() {
        Resolution res = GUI.resolution();
        return round(width()) >= res.width() && round(height()) >= res.height();
    }

    public boolean isOffScreen() {
        Resolution resolution = GUI.resolution();
        int w = resolution.width();
        int h = resolution.height();
        int minX = ceil(position.x);
        int minY = ceil(position.y);
        int maxX = floor(position.x + content.currentWidth());
        int maxY = floor(position.y - content.currentHeight());
        return (minX < 0 || maxX > w || minY < 0 || maxY > h);
    }

    public boolean isRestoredX() { return content.isRestoredX(); }
    public boolean isRestoredY() { return content.isRestoredY(); }
    public boolean isRestored() { return isRestoredX() && isRestoredY(); }
    public boolean isMaximized() { return isMaximizedX() && isMaximizedY(); }
    public boolean isMaximizedX() { return (state & STATE_MAXIMIZED_X) == STATE_MAXIMIZED_X; }
    public boolean isMaximizedY() { return (state & STATE_MAXIMIZED_Y) == STATE_MAXIMIZED_Y; }
    public boolean isCurrentlyDragging() { return (state & STATE_DRAGGING) == STATE_DRAGGING; }
    public boolean isCurrentlyRestoringX() { return (state & STATE_RESTORING_X) == STATE_RESTORING_X; }
    public boolean isCurrentlyRestoringY() { return (state & STATE_RESTORING_Y) == STATE_RESTORING_Y; }
    public boolean isCurrentlyMaximizingX() { return (state & STATE_MAXIMIZING_X) == STATE_MAXIMIZING_X; }
    public boolean isCurrentlyMaximizingY() { return (state & STATE_MAXIMIZING_Y) == STATE_MAXIMIZING_Y; }

    protected void setStateCurrentlyDragging() { state |= STATE_DRAGGING; }
    protected void setStateCurrentlyRestoringX() { state |= STATE_RESTORING_X; }
    protected void setStateCurrentlyRestoringY() { state |= STATE_RESTORING_Y; }
    protected void setStateCurrentlyMaximizingX() { state |= STATE_MAXIMIZING_X; }
    protected void setStateCurrentlyMaximizingY() { state |= STATE_MAXIMIZING_Y; }
    protected void setStateIsMaximizedX() { state |= STATE_MAXIMIZED_X; }
    protected void setStateIsMaximizedY() { state |= STATE_MAXIMIZED_Y; }

    protected void clearStateCurrentlyDragging() { state = (state &~ STATE_DRAGGING); }
    protected void clearStateCurrentlyRestoringX() { state = (state &~ STATE_RESTORING_X); }
    protected void clearStateCurrentlyRestoringY() { state = (state &~ STATE_RESTORING_Y); }
    protected void clearStateCurrentlyMaximizingX() { state = (state &~ STATE_MAXIMIZING_X); }
    protected void clearStateCurrentlyMaximizingY() { state = (state &~ STATE_MAXIMIZING_Y); }
    protected void clearStateIsMaximizedX() { state = (state &~ STATE_MAXIMIZED_X); }
    protected void clearStateIsMaximizedY() { state = (state &~ STATE_MAXIMIZED_Y); }

    private void translate(Vector2f translation) {
        Resolution res = GUI.resolution();
        float dx = translation.x;
        float dy = translation.y;
        float x, y;
        if (dx < 0) {
            x = transform_target.minX + dx;
            x = Math.max(x,0);
            dx = x - transform_target.minX;
        } else if (dx > 0) {
            x = transform_target.maxX + dx;
            x = Math.min(x,res.width());
            dx = x - transform_target.maxX;
        } if (dy < 0) {
            y = transform_target.minY + dy;
            y = Math.max(y,0);
            dy = y - transform_target.minY;
        } else if (dy > 0) {
            y = transform_target.maxY + dy;
            y = Math.min(y,res.height());
            dy = y - transform_target.maxY;
        } transform_target.translate(dx,dy);
    }

    private static void transform_interpolation(float t, Rectanglef a, Rectanglef b, Rectanglef dst) {
        float a_x = (a.minX + a.maxX) / 2f;
        float b_x = (b.minX + b.maxX) / 2f;
        float a_y = (a.minY + a.maxY) / 2f;
        float b_y = (b.minY + b.maxY) / 2f;
        float x = a_x + (b_x - a_x) * t;
        float y = a_y + (b_y - a_y) * t;
        float wh = (a.lengthX() + (b.lengthX() - a.lengthX()) * t) / 2f;
        float hh = (a.lengthY() + (b.lengthY() - a.lengthY()) * t) / 2f;
        dst.minX = x - wh; dst.maxX = x + wh; dst.minY = y - hh; dst.maxY = y + hh;
    }
}
