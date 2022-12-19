package io.github.heathensoft.jlib.hud.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.Stack;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.hud.Hud;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
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


public abstract class HudItem implements Disposable {

    private static final List<HudItem> itemList = new ArrayList<>();
    private static final Map<String,HudItem> itemMap = new HashMap<>();
    protected static final Area tmpArea = new Area();

    protected final Area area;
    protected final Hud context;
    protected final String title;
    protected final Vector2f position;
    protected boolean isOpen;


    protected HudItem(Hud context, String title) {
        this.position = new Vector2f();
        this.area = new Area();
        this.context = context;
        this.title = title;
        itemMap.put(title,this);
        itemList.add(this);
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
        Stack<HudItem> stack = new Stack<>(itemList.size());
        itemList.remove(this);
        for (int i = itemList.size() - 1; i >= 0; i--) {
            stack.push(itemList.remove(i));
        } stack.push(this);
        while (!stack.isEmpty()) {
            itemList.add(stack.pop());
        }
    }

    public void dispose() {
        close();
        itemMap.remove(title);
        itemList.remove(this);
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
        return itemList.get(0) == this;
    }

    public boolean insideBounds() {
        tmpArea.set(0,0,context.WIDTH,context.HEIGHT);
        return tmpArea.intersects(area());
    }

    public boolean outsideBounds() {
        return !insideBounds();
    }

    public static List<HudItem> list() {
        return itemList;
    }

    public static void updateAll(float dt) {
        for (HudItem item : itemList) {
            if (item.isOpen()) item.update(dt);
        }
    }

    public static void renderAll(SpriteBatch batch) {
        for (int i = itemList.size() - 1; i >= 0; i--) {
            HudItem item = itemList.get(i);
            if (item.isOpen()) item.render(batch);
        }
    }

    public static void disposeAll() {
        Stack<HudItem> stack = new Stack<>(itemList.size());
        for (HudItem window : itemList) {
            stack.push(window);
        }itemList.clear();
        itemMap.clear();
        while (!stack.isEmpty())
            Disposable.dispose(stack.pop());
    }

    public static void open(String title) {
        HudItem item = itemMap.get(title);
        if (item != null) item.open();
    }

    public static <T extends HudItem> void openAll(Class<T> superClass) {
        for (HudItem item : itemList) {
            if (superClass.isAssignableFrom(item.getClass()))
                item.open();
        }
    }

    public static void close(String title) {
        HudItem item = itemMap.get(title);
        if (item != null) item.close();
    }

    public static void closeAll() {
        for (HudItem item : itemList) item.close();
    }

    public static <T extends HudItem> void closeAll(Class<T> superClass) {
        for (HudItem item : itemList) {
            if (superClass.isAssignableFrom(item.getClass()))
                item.close();
        }
    }

}
