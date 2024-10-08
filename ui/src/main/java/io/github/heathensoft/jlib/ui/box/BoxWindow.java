package io.github.heathensoft.jlib.ui.box;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Window;
import io.github.heathensoft.jlib.ui.WindowAnchor;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.TextAlignment;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.floor;

/**
 * @author Frederik Dahl
 * 07/03/2024
 */


public final class BoxWindow extends Window {

    private static final int STATE_AUTO_RESTORE = 0x0010;
    private static final int STATE_DRAGGING = 0x0020;
    private static final int STATE_RESTORING_X = 0x0040;
    private static final int STATE_RESTORING_Y = 0x0080;
    private static final int STATE_MAXIMIZING_X = 0x0100;
    private static final int STATE_MAXIMIZING_Y = 0x0200;
    private static final int STATE_MAXIMIZED_X = 0x0400;
    private static final int STATE_MAXIMIZED_Y = 0x0800;

    private final WindowAnchor anchor;
    private final RootContainer content;
    private final FadingDisplay fading_display;
    private final Map<String,Box> named_content;
    private final Rectanglef transform_initial = new Rectanglef();
    private final Rectanglef transform_target = new Rectanglef();
    private final Vector2f position = new Vector2f();
    private float transform_timer;

    public BoxWindow(RootContainer content) {
        this(content,WindowAnchor.NONE);
    }

    public BoxWindow(RootContainer content, String name) {
        this(content,null,name);
    }

    public BoxWindow(RootContainer content, WindowAnchor anchor) {
        this(content,anchor,"untitled window");
    }

    public BoxWindow(RootContainer content, WindowAnchor anchor, String name) {
        if (content == null) throw new RuntimeException("GUI: Null BoxWindow content");
        this.anchor = anchor == null ? WindowAnchor.NONE : anchor;
        this.fading_display = new FadingDisplay();
        this.named_content = new HashMap<>();
        this.content = content;
        this.name = name;
    }

    protected void prepare(float dt) { content.prepareBox(this,dt); }

    protected void render(RendererGUI renderer, float dt) {
        Rectanglef bounds = bounds(U.popRect());
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
        Rectanglef transform = U.popRect();
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
        U.pushRect(2);
        // Update whether window is maximized ***********************************
        int maximum_width = round(maximumWidth());
        int width = round(content.currentWidth());
        if (width >= maximum_width) setStateIsMaximizedX();
        else clearStateIsMaximizedX();
        int maximum_height = round(maximumHeight());
        int height = round(content.currentHeight());
        if (height >= maximum_height) setStateIsMaximizedY();
        else clearStateIsMaximizedY();

        content.renderBox(this,renderer,(int)position.x,(int)position.y,dt,0);
        content.renderBoxText(this,renderer,(int)position.x,(int)position.y,dt);
        content.windowResizeEvents(this,(int)position.x,(int)position.y);
        fading_display.draw(renderer,dt);
        if (!GUI.state.anyInteractablePressed()) { clearStateCurrentlyDragging(); }
    }

    protected void onInit(String name)  {
        anchorContent(content);
        content.initializeBox(this,null);
    }

    protected void onOpen() { content.openBox(); }

    protected void onClose() {
        if (isAutoRestoringOnClose()) {
            anchorContent(content);
        } content.closeBox();
    }

    protected void onTermination() {
        Disposable.dispose(content);
        named_content.clear();
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
            // In the case where the window is maximized and restored at the same time.
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

    public void maximizeInstantX() {
        if (!isCurrentlyMaximizingX()) {
            clearStateCurrentlyRestoringX();
            setStateCurrentlyMaximizingX();
            transform_timer = 1.0f;
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

    public void maximizeInstantY() {
        if (!isCurrentlyMaximizingY()) {
            clearStateCurrentlyRestoringY();
            setStateCurrentlyMaximizingY();
            transform_timer = 1.0f;
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
                Vector2f origin_translation = U.popSetVec2(
                        transform_initial.minX,
                        transform_initial.maxY);
                origin_translation.sub(
                        transform_target.minX,
                        transform_target.maxY);
                transform_target.translate(origin_translation);
                U.pushVec2();
                translate(drag_vector);
            }
        }
    }

    public void dragLeft(Vector2f position) {
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
                float mouse_x = position.x;
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

    public void dragRight(Vector2f position) {
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
                float mouse_x = position.x;
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

    public void dragTop(Vector2f position) {
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
                float mouse_y = position.y;
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

    public void dragBottom(Vector2f position) {
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
                float mouse_y = position.y;
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

    public void addNameContent(String key, Box box) {
        Box existing = named_content.put(key,box);
        if (existing != null) {
            Logger.debug("GUI: Window: \"{}\" , named content replaced");
        }
    }

    public void displayFading(String descriptor, String prefix, Number value, String suffix, Rectanglef box_bounds) {
        fading_display.display(descriptor, prefix, value, suffix, box_bounds);
    }

    public void displayFading(String descriptor, Number value, Rectanglef box_bounds) {
        fading_display.display(descriptor, null, value, null, box_bounds);
    }

    public void displayFading(String prefix, Number value, String suffix, Rectanglef box_bounds) {
        fading_display.display(null, prefix, value, suffix, box_bounds);
    }

    public void displayFading(String string, Rectanglef box_bounds) {
        fading_display.display(string, null, null, null, box_bounds);
    }


    public boolean getBoundsOf(Box target, Rectanglef dst) {
        return content.getBoundsOf(target,dst,(int)position.x,(int)position.y);
    }

    public Box namedContent(String key) { return named_content.get(key); }

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

    /** Will reset the content to desired size on close */
    public void autoRestoreOnClose(boolean on) {
        if (on) window_state |= STATE_AUTO_RESTORE;
        else window_state = (window_state &~ STATE_AUTO_RESTORE);
    }

    public boolean isRestoredX() { return content.isRestoredX(); }
    public boolean isRestoredY() { return content.isRestoredY(); }
    public boolean isRestored() { return isRestoredX() && isRestoredY(); }
    public boolean isMaximized() { return isMaximizedX() && isMaximizedY(); }
    public boolean isMaximizedX() { return (window_state & STATE_MAXIMIZED_X) == STATE_MAXIMIZED_X; }
    public boolean isMaximizedY() { return (window_state & STATE_MAXIMIZED_Y) == STATE_MAXIMIZED_Y; }
    public boolean isCurrentlyDragging() { return (window_state & STATE_DRAGGING) == STATE_DRAGGING; }
    public boolean isCurrentlyRestoringX() { return (window_state & STATE_RESTORING_X) == STATE_RESTORING_X; }
    public boolean isCurrentlyRestoringY() { return (window_state & STATE_RESTORING_Y) == STATE_RESTORING_Y; }
    public boolean isCurrentlyMaximizingX() { return (window_state & STATE_MAXIMIZING_X) == STATE_MAXIMIZING_X; }
    public boolean isCurrentlyMaximizingY() { return (window_state & STATE_MAXIMIZING_Y) == STATE_MAXIMIZING_Y; }
    public boolean isAutoRestoringOnClose() { return (window_state & STATE_AUTO_RESTORE) == STATE_AUTO_RESTORE; }

    private void setStateCurrentlyDragging() { window_state |= STATE_DRAGGING; }
    private void setStateCurrentlyRestoringX() { window_state |= STATE_RESTORING_X; }
    private void setStateCurrentlyRestoringY() { window_state |= STATE_RESTORING_Y; }
    private void setStateCurrentlyMaximizingX() { window_state |= STATE_MAXIMIZING_X; }
    private void setStateCurrentlyMaximizingY() { window_state |= STATE_MAXIMIZING_Y; }
    private void setStateIsMaximizedX() { window_state |= STATE_MAXIMIZED_X; }
    private void setStateIsMaximizedY() { window_state |= STATE_MAXIMIZED_Y; }

    private void clearStateCurrentlyDragging() { window_state = (window_state &~ STATE_DRAGGING); }
    private void clearStateCurrentlyRestoringX() { window_state = (window_state &~ STATE_RESTORING_X); }
    private void clearStateCurrentlyRestoringY() { window_state = (window_state &~ STATE_RESTORING_Y); }
    private void clearStateCurrentlyMaximizingX() { window_state = (window_state &~ STATE_MAXIMIZING_X); }
    private void clearStateCurrentlyMaximizingY() { window_state = (window_state &~ STATE_MAXIMIZING_Y); }
    private void clearStateIsMaximizedX() { window_state = (window_state &~ STATE_MAXIMIZED_X); }
    private void clearStateIsMaximizedY() { window_state = (window_state &~ STATE_MAXIMIZED_Y); }

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

    private void anchorContent(RootContainer content) {
        if (content.isBuilt()) content.restoreToDesiredSize();
        else content.build();
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
        clearStateIsMaximizedX();
        clearStateIsMaximizedY();
        clearStateCurrentlyMaximizingX();
        clearStateCurrentlyMaximizingY();
        setStateCurrentlyRestoringX();
        setStateCurrentlyRestoringY();
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
        dst.minX = x - wh;
        dst.maxX = x + wh;
        dst.minY = y - hh;
        dst.maxY = y + hh;
    }

    private static final class FadingDisplay {

        private float fadeOut_timer;
        private String string;
        private String value;
        private final Rectanglef value_bounds;
        private final Rectanglef string_bounds;

        FadingDisplay() {
            this.value_bounds = new Rectanglef();
            this.string_bounds = new Rectanglef();
            this.fadeOut_timer = 1f;
        }

        void display(String string, String value_prefix, Number value, String value_suffix, Rectanglef box_bounds) {
            String value_string = value_string(value_prefix,value,value_suffix);
            this.fadeOut_timer = 1.0f;
            this.string = null;
            this.value = null;
            if (string == null) {
                if (value_string != null)
                    displayValue(value_string,box_bounds);
            } else if (value_string == null) {
                displayString(string,box_bounds);
            } else { displayString(string,box_bounds);
                displayValue(value_string,box_bounds);
            }
        }

        void displayValue(String value, Rectanglef box_bounds) {
            if (value != null && !value.isBlank() && box_bounds.isValid()) {
                GUI.GlobalVariables global = GUI.variables;
                float area_width;
                float area_height = global.boxwindow_fadedisplay_desired_height;
                float padding = global.boxwindow_fadedisplay_padding;
                float bounds_w = box_bounds.lengthX();
                float bounds_h = box_bounds.lengthY();
                if (bounds_h < area_height) {
                    area_height = bounds_h;
                } float text_size = area_height - (2 * padding);
                if (text_size > 1f) {
                    FontsGUI fonts = GUI.fonts;
                    fonts.bindFontMetrics(global.boxwindow_fadedisplay_font);
                    area_width = fonts.advanceSumSized(value,text_size);
                    area_width += (global.boxwindow_fadedisplay_padding * 2);
                    if (bounds_w < area_width) {
                        area_height *= (bounds_w / area_width);
                        area_width = bounds_w;
                    } text_size = area_height - (2 * padding);
                    if (text_size > 1f) {
                        value_bounds.minX = box_bounds.maxX - area_width;
                        value_bounds.minY = box_bounds.maxY - area_height;
                        value_bounds.maxX = box_bounds.maxX;
                        value_bounds.maxY = box_bounds.maxY;
                        if (value_bounds.isValid()) {
                            this.value = value;
                            this.fadeOut_timer = 0f;
                        }
                    }
                }
            }
        }

        void displayString(String string, Rectanglef box_bounds) {
            if (string != null && !string.isBlank() && box_bounds.isValid()) {
                GUI.GlobalVariables global = GUI.variables;
                float padding = global.boxwindow_fadedisplay_padding;
                float area_width;
                float area_height = global.boxwindow_fadedisplay_desired_height;
                float bounds_w = box_bounds.lengthX();
                float bounds_h = box_bounds.lengthY();
                if (bounds_h < area_height) {
                    area_height = bounds_h;
                } float text_size = area_height - (2 * padding);
                if (text_size > 1f) {
                    FontsGUI fonts = GUI.fonts;
                    fonts.bindFontMetrics(global.boxwindow_fadedisplay_font);
                    area_width = fonts.advanceSumSized(string,text_size);
                    area_width += (padding * 2);
                    if (bounds_w < area_width) {
                        area_height *= (bounds_w / area_width);
                        area_width = bounds_w;
                    } text_size = area_height - (2 * padding);
                    if (text_size > 1f) {
                        string_bounds.minX = box_bounds.minX;
                        string_bounds.maxY = box_bounds.maxY;
                        string_bounds.maxX = box_bounds.minX + area_width;
                        string_bounds.minY = box_bounds.maxY - area_height;
                        if (string_bounds.isValid()) {
                            this.string = string;
                            this.fadeOut_timer = 0f;
                        }
                    }
                }
            }
        }

        void draw(RendererGUI renderer, float dt) {
            if (fadeOut_timer < 1f) {
                float alpha = 1 - U.smooth(clamp(fadeOut_timer));
                GUI.GlobalVariables global = GUI.variables;
                int padding = global.boxwindow_fadedisplay_padding;
                int font = global.boxwindow_fadedisplay_font;
                Vector4f rgb = U.popSetVec4(0,0,0,alpha * 0.75f);
                int bg_color = Color.rgb_to_intBits(rgb);
                rgb.set(GUI.variables.boxwindow_fadedisplay_text_color);
                rgb.w *= alpha;
                int text_color = Color.rgb_to_intBits(rgb);
                U.pushVec4();
                if (string == null) {
                    if (value != null) { // value only
                        renderer.drawElement(value_bounds,bg_color,RendererGUI.SKIP_ID);
                        TextAlignment alignment = TextAlignment.CENTERED;
                        renderer.drawStringDynamicVerticalCentered(value,alignment, value_bounds,font,text_color,padding,0);
                    }
                } else {
                    if (value == null) { // text only
                        renderer.drawElement(string_bounds,bg_color,RendererGUI.SKIP_ID);
                        TextAlignment alignment = TextAlignment.LEFT;
                        renderer.drawStringDynamicVerticalCentered(string,alignment, string_bounds,font,text_color,padding,0);
                    } else {
                        if (string_bounds.intersectsRectangle(value_bounds)) { // Combine
                            TextAlignment alignment = TextAlignment.CENTERED;
                            String combined_string = string + " " +  value;
                            Rectanglef combined_rect = U.popRect();
                            combined_rect.minX = string_bounds.minX;
                            combined_rect.maxX = value_bounds.maxX;
                            combined_rect.maxY = value_bounds.maxY;
                            combined_rect.minY = Math.max(value_bounds.minY,string_bounds.minY);
                            renderer.drawElement(combined_rect,bg_color,RendererGUI.SKIP_ID);
                            renderer.drawStringDynamicVerticalCentered(combined_string,alignment,combined_rect,font,text_color,padding,0);
                            U.pushRect();
                        } else { // both
                            renderer.drawElement(value_bounds,bg_color,RendererGUI.SKIP_ID);
                            TextAlignment alignment = TextAlignment.CENTERED;
                            renderer.drawStringDynamicVerticalCentered(value,alignment, value_bounds,font,text_color,padding,0);
                            renderer.drawElement(string_bounds,bg_color,RendererGUI.SKIP_ID);
                            alignment = TextAlignment.LEFT;
                            renderer.drawStringDynamicVerticalCentered(string,alignment, string_bounds,font,text_color,padding,0);
                        }
                    }
                }
                fadeOut_timer += dt;
            }
        }

        private String value_string(String value_prefix, Number value, String value_suffix) {
            if (value != null) {
                String value_string;
                if (value instanceof Float f) {
                    value_string = "" + U.round(f,2);
                } else if (value instanceof Double d) {
                    value_string = "" + U.round(d,2);
                } else if (value instanceof Integer i) {
                    value_string = "" + i;
                } else if (value instanceof Byte b) {
                    value_string = Integer.toHexString(b);
                    if (value_string.length() == 1) {
                        value_string += "0";
                    } value_string = value_string.toUpperCase();
                    value_string = "0x" + value_string;
                } else return null;
                if (value_prefix != null) {
                    value_string = value_prefix + value_string;
                } if (value_suffix != null) {
                    value_string = value_string + value_suffix;
                } return value_string;
            } return null;
        }
    }

}
