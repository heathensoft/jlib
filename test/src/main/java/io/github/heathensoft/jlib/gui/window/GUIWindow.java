package io.github.heathensoft.jlib.gui.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.Stack;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.gui.GUI;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Frederik Dahl
 * 17/12/2022
 */


public abstract class GUIWindow implements Disposable {

    private static final List<GUIWindow> GUI_WINDOW_LIST = new ArrayList<>();
    private static final Map<String, GUIWindow> GUI_WINDOW_MAP = new HashMap<>();
    protected static final Area tmpArea = new Area();

    protected final Area area;
    protected final GUI context;
    protected final String title;
    protected final Vector2f position;
    protected boolean isOpen;


    protected GUIWindow(GUI context, String title) {
        this.position = new Vector2f();
        this.area = new Area();
        this.context = context;
        this.title = title;
        GUI_WINDOW_MAP.put(title,this);
        GUI_WINDOW_LIST.add(this);
    }

    public abstract void render(SpriteBatch batch);

    /**
     * update before interactable events
     * @param dt delta time
     */
    public abstract void update(float dt);

    protected abstract void disposeContents();

    public void focus() {
        if (hasFocus()) return;
        Stack<GUIWindow> stack = new Stack<>(GUI_WINDOW_LIST.size());
        GUI_WINDOW_LIST.remove(this);
        for (int i = GUI_WINDOW_LIST.size() - 1; i >= 0; i--) {
            stack.push(GUI_WINDOW_LIST.remove(i));
        } stack.push(this);
        while (!stack.isEmpty()) {
            GUI_WINDOW_LIST.add(stack.pop());
        }
    }

    public void dispose() {
        close();
        GUI_WINDOW_MAP.remove(title);
        GUI_WINDOW_LIST.remove(this);
        disposeContents();
    }

    public void open() {
        isOpen = true;
    }

    public void close() {
        isOpen = false;
    }

    public void setPosition(Vector2f position) {
        this.position.set(position.x, position.y);
    }

    public abstract int width();

    public abstract int height();

    public int x0() {
        return (int) position.x;
    }

    public int y0() {
        return (int) position.y;
    }

    public Vector2f position() {
        return position;
    }

    public Area area() {
        int x0 = x0();
        int y0 = y0() - height();
        int x1 = x0 + width();
        int y1 = y0();
        area.set(x0,y0,x1,y1);
        return area;
    }

    public GUI context() {
        return context;
    }

    public String title() {
        return title;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isClosed() {
        return !isOpen;
    }

    private boolean hasFocus() {
        return GUI_WINDOW_LIST.get(0) == this;
    }

    public boolean insideBounds() {
        tmpArea.set(0,0,context.WIDTH,context.HEIGHT);
        return tmpArea.intersects(area());
    }

    public boolean outsideBounds() {
        return !insideBounds();
    }

    public static List<GUIWindow> list() {
        return GUI_WINDOW_LIST;
    }

    public static void updateAll(float dt) {
        for (GUIWindow item : GUI_WINDOW_LIST) {
            if (item.isOpen())
                item.update(dt);
        }
    }

    public static void renderAll(SpriteBatch batch) {
        for (int i = GUI_WINDOW_LIST.size() - 1; i >= 0; i--) {
            GUIWindow item = GUI_WINDOW_LIST.get(i);
            if (item.isOpen()) item.render(batch);
        }
    }

    public static void disposeAll() {
        Stack<GUIWindow> stack = new Stack<>(GUI_WINDOW_LIST.size());
        for (GUIWindow window : GUI_WINDOW_LIST) {
            stack.push(window);
        }
        GUI_WINDOW_LIST.clear();
        GUI_WINDOW_MAP.clear();
        while (!stack.isEmpty())
            Disposable.dispose(stack.pop());
    }

    public static void open(String title) {
        GUIWindow item = GUI_WINDOW_MAP.get(title);
        if (item != null) item.open();
    }

    public static <T extends GUIWindow> void openAll(Class<T> superClass) {
        for (GUIWindow item : GUI_WINDOW_LIST) {
            if (superClass.isAssignableFrom(item.getClass()))
                item.open();
        }
    }

    public static void close(String title) {
        GUIWindow item = GUI_WINDOW_MAP.get(title);
        if (item != null) item.close();
    }

    public static void closeAll() {
        for (GUIWindow item : GUI_WINDOW_LIST) item.close();
    }

    public static <T extends GUIWindow> void closeAll(Class<T> superClass) {
        for (GUIWindow item : GUI_WINDOW_LIST) {
            if (superClass.isAssignableFrom(item.getClass()))
                item.close();
        }
    }

}
