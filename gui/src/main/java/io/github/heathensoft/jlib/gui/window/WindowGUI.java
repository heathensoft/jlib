package io.github.heathensoft.jlib.gui.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.gui.GUI.Windows.gui_windows_is_focused;
import static io.github.heathensoft.jlib.gui.GUI.Windows.gui_windows_focus;

/**
 * @author Frederik Dahl
 * 01/11/2023
 */


public class WindowGUI implements Disposable {

    protected Rectanglef transform_initial;
    protected Rectanglef transform_target;
    protected RootContainer content;
    protected Vector2f position;
    protected Anchor anchor;
    protected String name;
    protected boolean open;
    protected boolean destroy;
    protected boolean dragging;
    protected boolean restoringX;
    protected boolean restoringY;
    protected boolean maximizingX;
    protected boolean maximizingY;
    protected boolean initialized;
    protected float transform_timer;

    public WindowGUI(String name, Anchor anchor) {
        this.position = new Vector2f();
        this.anchor = anchor;
        this.name = name;
        this.transform_initial = new Rectanglef();
        this.transform_target = new Rectanglef();
    }

    public WindowGUI(String name) {
        this(name,Anchor.NONE);
    }

    /** This is called when the window gets added to the front. */
    public void onFocus() { content.onWindowFocus(this); }

    /** This is called before render loop. */
    public void prepare(float dt) {  }

    public void create(RootContainer content) {
        if (!initialized && content != null) {
            this.content = content;
            content.restore();
            Resolution resolution = screen_resolution();
            float screen_width = resolution.width();
            float screen_height = resolution.height();
            float screen_center_x = screen_width / 2f;
            float screen_center_y = screen_height / 2f;
            float content_width = content.currentSize.width();
            float content_height = content.currentSize.height();
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
            bounds(transform_target);
            transform_timer = 1.0f;
            restoringX = true;
            restoringY = true;
            GUI.Windows.gui_windows_add_new_window(this);
            initialized = true;
        }
    }

    public void dragTop() {
        if (!content.isLockedVertical()) {
            if (!anchor.anchored_top) {
                if (!dragging) {
                    dragging = true;
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                restoringX = false;
                restoringY = false;
                maximizingX = false;
                maximizingY = false;
                Resolution res = screen_resolution();
                float origin_y = transform_initial.maxY;
                float mouse_y = mouse_position(MathLib.vec2()).y;
                mouse_y = U.clamp(mouse_y,0,res.height());
                float dy = mouse_y - origin_y;
                float y = origin_y + dy;
                float height = y - transform_initial.minY;
                if (dy < 0) {
                    float min_height = content.restingSize.height();
                    height = Math.max(min_height,height);
                    y = transform_initial.minY + height;
                    transform_target.maxY = y;
                } else if (dy > 0) {
                    float max_height = content.max_desired_size.height();
                    height = Math.min(max_height,height);
                    y = transform_initial.minY + height;
                    transform_target.maxY = y;
                }
            }
        }
    }

    public void dragRight() {
        if (!content.isLockedHorizontal()) {
            if (!anchor.anchored_right) {
                if (!dragging) {
                    dragging = true;
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                restoringX = false;
                restoringY = false;
                maximizingX = false;
                maximizingY = false;
                Resolution res = screen_resolution();
                float origin_x = transform_initial.maxX;
                float mouse_x = mouse_position(MathLib.vec2()).x;
                mouse_x = U.clamp(mouse_x,0,res.width());
                float dx = mouse_x - origin_x;
                float x = origin_x + dx;
                float width = x - transform_initial.minX;
                if (dx < 0) {
                    float min_width = content.restingSize.width();
                    width = Math.max(min_width,width);
                    x = transform_initial.minX + width;
                    transform_target.maxX = x;
                } else if (dx > 0) {
                    float max_width = content.max_desired_size.width();
                    width = Math.min(max_width,width);
                    x = transform_initial.minX + width;
                    transform_target.maxX = x;
                }
            }
        }
    }

    public void dragBottom() {
        if (!content.isLockedVertical()) {
            if (!anchor.anchored_bottom) {
                if (!dragging) {
                    dragging = true;
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                restoringX = false;
                restoringY = false;
                maximizingX = false;
                maximizingY = false;
                Resolution res = screen_resolution();
                float origin_y = transform_initial.minY;
                float mouse_y = mouse_position(MathLib.vec2()).y;
                mouse_y = U.clamp(mouse_y,0,res.height());
                float dy = mouse_y - origin_y;
                float y = origin_y + dy;
                float height = transform_initial.maxY - y;
                if (dy > 0) {
                    float min_height = content.restingSize.height();
                    height = Math.max(min_height,height);
                    y = transform_initial.maxY - height;
                    transform_target.minY = y;
                } else if (dy < 0) {
                    float max_height = content.max_desired_size.height();
                    height = Math.min(max_height,height);
                    y = transform_initial.maxY - height;
                    transform_target.minY = y;
                }
            }
        }
    }

    public void dragLeft() {
        if (!content.isLockedHorizontal()) {
            if (!anchor.anchored_left) {
                if (!dragging) {
                    dragging = true;
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                restoringX = false;
                restoringY = false;
                maximizingX = false;
                maximizingY = false;
                Resolution res = screen_resolution();
                float origin_x = transform_initial.minX;
                float mouse_x = mouse_position(MathLib.vec2()).x;
                mouse_x = U.clamp(mouse_x,0,res.width());
                float dx = mouse_x - origin_x;
                float x = origin_x + dx;
                float width = transform_initial.maxX - x;
                if (dx > 0) {
                    float min_width = content.restingSize.width();
                    width = Math.max(min_width,width);
                    x = transform_initial.maxX - width;
                    transform_target.minX = x;
                } else if (dx < 0) {
                    float max_width = content.max_desired_size.width();
                    width = Math.min(max_width,width);
                    x = transform_initial.maxX - width;
                    transform_target.minX = x;
                }
            }
        }
    }

    public void move(Vector2f drag_vector) {
        if (anchor == Anchor.NONE) {
            if (!isScreenSized()) {
                if (!dragging) {
                    dragging = true;
                    bounds(transform_initial);
                    bounds(transform_target);
                } transform_timer = 1.0f;
                restoringX = false;
                restoringY = false;
                maximizingX = false;
                maximizingY = false;
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

    private void translate(Vector2f translation) {
        Resolution res = screen_resolution();
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

    public void render(RendererGUI renderer, float dt) {
        Rectanglef bounds = bounds(MathLib.rectf());
        Resolution resolution = screen_resolution();
        final float screen_width = resolution.width();
        final float screen_height = resolution.height();
        final float content_width = content.currentSize.width();
        final float content_height = content.currentSize.height();
        final float content_desired_width = content.restingSize.width();
        final float content_desired_height = content.restingSize.height();
        final float screen_center_x = screen_width / 2f;
        final float screen_center_y = screen_height / 2f;
        final boolean offScreen = isOffScreen();
        if (maximizingX) {
            float max_width;
            if(content.isLockedHorizontal()) {
                max_width = content_width;
            } else {
                max_width = Math.min(screen_width,content.max_desired_size.width());
                max_width = Math.max(max_width,content_width);
            } switch (anchor) {
                case NONE -> {
                    float window_center_x = position.x + (content_width / 2f);
                    transform_target.minX = window_center_x - (max_width / 2f);
                    transform_target.maxX = window_center_x + (max_width / 2f);
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
                    transform_target.minX = screen_center_x - (max_width / 2f);
                    transform_target.maxX = screen_center_x + (max_width / 2f);
                } case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> {
                    transform_target.minX = screen_width - max_width;
                    transform_target.maxX = screen_width;
                } case BOTTOM_LEFT, LEFT, TOP_LEFT -> {
                    transform_target.minX = 0;
                    transform_target.maxX = max_width;
                }
            }
        } else if (restoringX) {
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
        } else {
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
        if (maximizingY) {
            float max_height;
            if(content.isLockedVertical()) { max_height = content_height;
            } else { max_height = Math.min(screen_height,content.max_desired_size.height());
                max_height = Math.max(max_height,content_height);
            } switch (anchor) {
                case NONE -> {
                    float window_center_y = position.y - (content_height / 2f);
                    transform_target.minY = window_center_y - (max_height / 2f);
                    transform_target.maxY = window_center_y + (max_height / 2f);
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
                    transform_target.minY = screen_center_y - (max_height / 2f);
                    transform_target.maxY = screen_center_y + (max_height / 2f);
                } case TOP, TOP_RIGHT, TOP_LEFT -> {
                    transform_target.minY = screen_height - max_height;
                    transform_target.maxY = screen_height;
                } case BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT -> {
                    transform_target.minY = 0;
                    transform_target.maxY = max_height;
                }
            }
        } else if (restoringY) {
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
        } else  {
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

        content.render(this,renderer, (int)position.x, (int)position.y,dt,0);
        content.renderText(renderer, (int)position.x, (int)position.y);

        if (!GUI.State.anyInteractablePressed()) { dragging = false; }

    }


    public void open() {
        if (!open) { open = true;
            content.onWindowOpen(this);
        }
    }

    public void close() {
        if (open) { open = false;
            content.onWindowClose(this);
        }
    }

    public void focus() { if (!hasFocus())  gui_windows_focus(this); }

    public void maximizeX() {
        if (!maximizingX) {
            restoringX = false;
            maximizingX = true;
            transform_timer = 0;
            transform_initial.minX = position.x;
            transform_initial.maxX = position.x + content.currentSize.width();
        }
    }

    public void maximizeY() {
        if (!maximizingY) {
            restoringY = false;
            maximizingY = true;
            transform_timer = 0;
            transform_initial.minY = position.y - content.currentSize.height();
            transform_initial.maxY = position.y;
        }
    }

    public void maximize() {
        maximizeX();
        maximizeY();
    }


    public void restoreX() {
        maximizingX = false;
        restoringX = true;
        transform_timer = 0;
        transform_target.minX = transform_initial.minX;
        transform_target.maxX = transform_initial.maxX;
        transform_initial.minX = position.x;
        transform_initial.maxX = position.x + content.currentSize.width();
        int current_width_discrete = round(transform_initial.lengthY());
        int desired_width_discrete = round(transform_target.lengthY());
        if (current_width_discrete == desired_width_discrete) {
            Resolution resolution = screen_resolution();
            float screen_width = resolution.width();
            float desired_width = content.restingSize.width();
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

    public void restoreY() {
        maximizingY = false;
        restoringY = true;
        transform_timer = 0;
        transform_target.minY = transform_initial.minY;
        transform_target.maxY = transform_initial.maxY;
        transform_initial.minY = position.y - content.currentSize.height();
        transform_initial.maxY = position.y;
        int current_height_discrete = round(transform_initial.lengthY());
        int desired_height_discrete = round(transform_target.lengthY());
        if (current_height_discrete == desired_height_discrete) {
            Resolution resolution = screen_resolution();
            float screen_height = resolution.height();
            float screen_center_y = screen_height / 2f;
            float window_center_y = (transform_initial.maxY + transform_initial.minY) / 2f;
            float desired_height = content.restingSize.height();
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

    public void restore() {
        if (restoringX && restoringY) return;
        maximizingX = false;
        maximizingY = false;
        restoringX = true;
        restoringY = true;
        transform_timer = 0;
        transform_target.set(transform_initial);
        bounds(transform_initial);
        if (round(transform_initial.lengthX()) == round(transform_target.lengthX())) {
            if (round(transform_initial.lengthY()) == round(transform_target.lengthY())) {
                Resolution resolution = screen_resolution();
                float screen_width = resolution.width();
                float screen_height = resolution.height();
                float screen_center_x = screen_width / 2f;
                float screen_center_y = screen_height / 2f;
                float desired_width = content.restingSize.width();
                float desired_height = content.restingSize.height();
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

    public void destroy() { destroy = true; }

    public boolean isOpen() { return open; }

    public float width() { return content.currentSize().width(); }

    public float height() { return content.currentSize().height(); }

    public float maxWidth() {
        float max_width;
        float content_width = content.currentSize.width();
        if(content.isLockedHorizontal()) {
            max_width = content_width;
        } else { Resolution resolution = screen_resolution();
            float screen_width = resolution.width();
            float window_desired_max_width = content.max_desired_size.width();
            max_width = Math.min(screen_width,window_desired_max_width);
            max_width = Math.max(max_width,content_width);
        } return max_width;
    }

    public float maxHeight() {
        float max_height;
        float content_height = content.currentSize.height();
        if(content.isLockedVertical()) {
            max_height = content_height;
        } else { Resolution resolution = screen_resolution();
            float screen_height = resolution.height();
            float window_desired_max_height = content.max_desired_size.height();
            max_height = Math.min(screen_height,window_desired_max_height);
            max_height = Math.max(max_height,content_height);
        } return max_height;
    }

    public boolean hasFocus() { return gui_windows_is_focused(this); }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isMaximizedX() {
        if (maximizingX) return true;
        else { int max_width = round(maxWidth());
            int width = round(content.currentSize.width());
            return width >= max_width;
        }
    }

    public boolean isMaximizedY() {
        if (maximizingY) return true;
        else { int max_height = round(maxHeight());
            int height = round(content.currentSize.height());
            return height >= max_height;
        }
    }

    public boolean isMaximized() {
        return isMaximizedX() && isMaximizedY();
    }

    public boolean isRestoredX() {
        return restoringX;
    }

    public boolean isRestoredY() {
        return restoringY;
    }

    public boolean isRestored() {
        return isRestoredX() && isRestoredY();
    }

    public boolean isAbleToFitOnScreen() {
        Resolution res = screen_resolution();
        if (content.restingSize.width() > res.width()) return false;
        return !(content.restingSize.height() > res.height());
    }

    public boolean tooLargeForScreen() {
        Resolution res = screen_resolution();
        if (content.currentSize.width() > res.width()) return true;
        return content.currentSize.height() > res.height();
    }

    public boolean isOffScreen() {
        Resolution resolution = screen_resolution();
        int w = resolution.width();
        int h = resolution.height();
        int minX = ceil(position.x);
        int minY = ceil(position.y);
        int maxX = floor(position.x + content.currentSize.width());
        int maxY = floor(position.y - content.currentSize.height());
        return (minX < 0 || maxX > w || minY < 0 || maxY > h);
    }

    public String name() {
        return name;
    }

    public <T extends WindowGUI> T cast(Class<T> clazz) {
        if (getClass() != clazz) {
            throw new ClassCastException("wrong cast of gui window");
        } return clazz.cast(this);
    }

    public Rectanglef bounds(Rectanglef dst) {
        dst.minX = position.x; dst.maxY = position.y;
        dst.maxX = position.x + content.currentSize.width();
        dst.minY = position.y - content.currentSize.height();
        return dst;
    }

    public Anchor anchor() {
        return anchor;
    }

    public Vector2f position(Vector2f dst) {
        return dst.set(position);
    }

    public Vector2f mouse_position(Vector2f dst) {
        Resolution resolution = screen_resolution();
        dst.set(Engine.get().input().mouse().position());
        return dst.mul(resolution.width(),resolution.height());
    }

    public Vector2f mouse_drag_vector(Vector2f dst, int button) {
        Resolution resolution = screen_resolution();
        dst.set(Engine.get().input().mouse().drag_vector(button));
        return dst.mul(resolution.width(),resolution.height());
    }

    public Resolution screen_resolution() {
        return Engine.get().window().appResolution();
    }

    public boolean queued_for_destruction() {
        return destroy;
    }

    protected BoxContainer content() {
        return content;
    }

    private boolean isTransforming() {
        return transform_timer < 1.0f;
    }

    public boolean isScreenSized() {
        Resolution res = screen_resolution();
        return round(width()) >= res.width() && round(height()) >= res.height();
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

    public void dispose() {
        Disposable.dispose(content);
    }
}
