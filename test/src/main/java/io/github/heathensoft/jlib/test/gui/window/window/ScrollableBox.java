package io.github.heathensoft.jlib.test.gui.window.window;


import io.github.heathensoft.jlib.test.graphicsOld.Color;
import io.github.heathensoft.jlib.test.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.test.graphicsOld.TextureRegion;
import io.github.heathensoft.jlib.test.gui.Debug;
import io.github.heathensoft.jlib.test.gui.GUIGraphics;
import io.github.heathensoft.jlib.test.gui.window.Box;
import io.github.heathensoft.jlib.test.gui.window.Size;
import io.github.heathensoft.jlib.test.gui.window.Spacing;
import io.github.heathensoft.jlib.lwjgl.window.Engine;


import static io.github.heathensoft.jlib.test.gui.GUI.*;

/**
 * @author Frederik Dahl
 * 22/12/2022
 */


public class ScrollableBox<T extends VirtualWindow> extends Box {

    protected T virtualWindow;
    protected Size currentSize;
    protected Size desiredSize;
    protected Scrollbar scrollbar;
    protected ScrollWheel scrollWheel;
    protected float window_position;
    private float DY;

    public ScrollableBox(T content, Spacing margin, Spacing border, Spacing padding, Color backgroundColor, Color borderColor) {
        super(margin, border, padding, backgroundColor, borderColor);
        float desired_h = content.desiredSize().height() + outerHeight();
        float desired_w = content.desiredSize().width() + outerWidth() + SCROLLBAR_AREA_WIDTH;
        this.desiredSize = new Size(desired_w,desired_h);
        this.currentSize = new Size(desiredSize);
        this.scrollbar = new Scrollbar(this);
        this.scrollWheel = new ScrollWheel(this);
        this.virtualWindow = content;
        this.virtualWindow.setContainer(this);
    }

    public ScrollableBox(T content, Spacing margin, Spacing border, Spacing padding) {
        super(margin, border, padding);
        float desired_h = content.desiredSize().height() + outerHeight();
        float desired_w = content.desiredSize().width() + outerWidth() + SCROLLBAR_AREA_WIDTH;
        this.desiredSize = new Size(desired_w,desired_h);
        this.currentSize = new Size(desiredSize);
        this.scrollbar = new Scrollbar(this);
        this.scrollWheel = new ScrollWheel(this);
        this.virtualWindow = content;
        this.virtualWindow.setContainer(this);
    }

    public void render(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        drawBackground(batch, graphics, x0, y0);
        drawBorder(batch, graphics, x0, y0);
        x0 += (margin.left() + border.left() + padding.left());
        y0 -= (margin.top() + border.top() + padding.top());
        virtualWindow.render(batch, graphics, x0, y0);
        x0 += (int)virtualWindow.currentSize().width();
        drawScrollbar(batch, graphics, x0, y0);
    }

    private void drawScrollbar(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {

        TextureRegion blank = graphics.blank();
        TextureRegion scb_top = graphics.coreAsset(GUIGraphics.SCROLLBAR_TOP);
        TextureRegion scb_bot = graphics.coreAsset(GUIGraphics.SCROLLBAR_BOTTOM);
        TextureRegion scb_con_top = graphics.coreAsset(GUIGraphics.SCROLLBAR_CONTAINER_TOP);
        TextureRegion scb_con_bot = graphics.coreAsset(GUIGraphics.SCROLLBAR_CONTAINER_BOTTOM);
        float background_color = SCROLLBAR_BG_COLOR.toFloatBits();
        float container_color = SCROLLBAR_OUTER_COLOR.toFloatBits();
        float scrollbar_color = SCROLLBAR_INNER_COLOR.toFloatBits();
        int scb_height = (int)windowHeight();
        int scb_width = SCROLLBAR_AREA_WIDTH;
        int scb_id = scrollbar.iData();
        int scw_id = scrollWheel.iData();
        int scb_outer_height = SCROLLBAR_EDGE_HEIGHT + SCROLLBAR_EDGE_HEIGHT + SCROLLBAR_WHEEL_EXTRA;
        int scb_inner_height = scb_height - scb_outer_height;
        int scb_filling_height = (int) (scb_inner_height * scrollbarSizeNormalized());
        int scb_pos = (int) ((scb_inner_height - scb_filling_height) * windowPositionNormalized()); // - SCROLLBAR_WHEEL_EXTRA;
        scb_filling_height += SCROLLBAR_WHEEL_EXTRA;

        Debug.out("HEATHENSOFT");
        Debug.out("Runtime",(float) Engine.get().time().runTimeSeconds());
        Debug.out("FPS", Engine.get().time().fps());
        Debug.out("UPS", Engine.get().time().ups());
        Debug.out("GRAB dy", DY);
        //DebugText.add("FPS: " + Engine.get().time().fps());
        //DebugText.add("Scrollbar height: " + scb_height);
        //DebugText.add("Scrollbar outer height: " + scb_outer_height);
        //DebugText.add("Scrollbar inner height: " + scb_inner_height);
        //DebugText.add("Scrollbar filling height: " + scb_filling_height);
        //DebugText.add("Scrollbar position: " + scb_pos);
        //DebugText.add("Window position: " + window_position);
        //DebugText.add("Number values -> \t value1: " + windowPositionNormalized() + "\t value2: " + 5);
        //DebugText.add("Window virtual height: " + (int)virtualWindow.currentSize().height());
        //DebugText.add("Window height: " + (int)windowHeight());


        // scrollbar background
        y0 -= scb_height;
        batch.draw(blank,x0,y0,scb_width,scb_height,background_color,scb_id);
        // scrollbar container bottom
        batch.draw(scb_con_bot,x0,y0,scb_width,SCROLLBAR_EDGE_HEIGHT,container_color,scb_id);
        // scrollbar border left
        batch.draw(blank,x0,y0,1,scb_height,container_color,scb_id);
        // scrollbar border right
        batch.draw(blank,x0 + (scb_width - 1),y0,1,scb_height,container_color,scb_id);
        // scrollbar container top
        y0 += (scb_height - SCROLLBAR_EDGE_HEIGHT);
        batch.draw(scb_con_top,x0,y0,scb_width,SCROLLBAR_EDGE_HEIGHT,container_color,scb_id);
        // scrollbar top
        y0 -= scb_pos;
        batch.draw(scb_top,x0,y0,scb_width,SCROLLBAR_EDGE_HEIGHT,scrollbar_color,scw_id);
        // scrollbar middle
        y0 -= scb_filling_height;
        int offset = (SCROLLBAR_AREA_WIDTH - SCROLLBAR_WHEEL_WIDTH) / 2;
        batch.draw(blank,x0 + offset,y0,SCROLLBAR_WHEEL_WIDTH,scb_filling_height,scrollbar_color,scw_id);
        // scrollbar bottom
        y0 -= SCROLLBAR_EDGE_HEIGHT;
        batch.draw(scb_bot,x0,y0,scb_width,SCROLLBAR_EDGE_HEIGHT,scrollbar_color,scw_id);
    }

    public T content() {
        return virtualWindow;
    }

    public void resizeHorizontal(float dx) {
        currentSize.addWidth(dx);
        virtualWindow.refresh();
    }

    public void resizeVertical(float dy) {
        currentSize.addHeight(dy);
        virtualWindow.refresh();
    }

    public void restore() {
        if (!currentSize.equals(desiredSize)) {
            currentSize.set(desiredSize);
            virtualWindow.refresh();
        }
    }

    /**
     * Called from virtual window when it is refreshed
     */
    public void onSizeAltered() {
        float H = virtualWindow.currentSize().height();
        float h = windowHeight();
        float max = H - h;
        window_position = Math.min(window_position,max);
        window_position = Math.max(window_position,0);
    }

    /**
     * @param dy in pixels
     */
    public void onDrag(float dy) {

    }

    /**
     * @param dy in mouse wheel scroll units
     */

    public void onScroll(float dy) {
        if (dy != 0) {
            dy *= virtualWindow.scrollDelta();
            if (dy < 0) {
                float min = 0;
                if (window_position > min) {
                    window_position += dy;
                    window_position = Math.max(window_position,min);
                }
            } else {
                float H = virtualWindow.currentSize().height();
                float h = windowHeight();
                float max = H - h;
                if (window_position < max) {
                    window_position += dy;
                    window_position = Math.min(window_position,max);
                }
            }
        }
    }

    protected Size currentSize() {
        return currentSize;
    }

    protected Size desiredSize() {
        return desiredSize;
    }

    public float windowPosition() {
        return window_position;
    }

    public Size windowSize() {
        return new Size(windowWidth(),windowHeight());
    }

    public float windowHeight() {
        float box_height = currentSize.height();
        return box_height - outerHeight();
    }

    public float windowWidth() {
        float width = currentSize.width() - outerWidth();
        width -= SCROLLBAR_AREA_WIDTH;
        return width;
    }

    public float windowPositionNormalized() { // Also scb_norm
        float H = virtualWindow.currentSize().height();
        float h = windowHeight();
        float wiggle = H - h;
        if (wiggle <= 0) return 0;
        return window_position / (H - h);
    }

    public float scrollbarSizeNormalized() {
        return windowHeight() / virtualWindow.currentSize.height();
    }

    public void update(float dt) {
        virtualWindow.update(dt);
    }

    public void dispose() {
        scrollbar.dispose();
        scrollWheel.dispose();
        virtualWindow.dispose();
    }
}
