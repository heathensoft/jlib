package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.graphicsOld.Color;
import io.github.heathensoft.jlib.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.graphicsOld.TextureRegion;
import io.github.heathensoft.jlib.gui.GUIGraphics;
import io.github.heathensoft.jlib.gui.window.Size;
import io.github.heathensoft.jlib.gui.window.window.VirtualWindow;

/**
 * @author Frederik Dahl
 * 23/12/2022
 */


public class Field extends VirtualWindow {


    private int elements;
    private int elementSize;
    private int rows;
    private int cols;
    private Size desiredSize;


    public Field(int elements, int elementSize, int minRows, int cols) {
        this.elements = elements;
        this.elementSize = elementSize;
        float desiredW = cols * elementSize;
        float desiredH = calculateHeight(desiredW);
        desiredH = Math.max(minRows * elementSize,desiredH);
        this.desiredSize = new Size(desiredW,desiredH);
        this.currentSize = new Size(desiredW,desiredH);
    }


    public void addElements(int count) {
        elements += count;
        refresh();
    }

    public void removeElements(int count) {
        elements -= count;
        refresh();
    }

    public void dispose() {}

    public int scrollDelta() {
        return elementSize;
    }

    protected float calculateHeight(float width) {
        int cols = (int)(width / elementSize);
        int rows = (int)Math.ceil((float)elements / cols);
        return rows * elementSize;
    }

    protected Size desiredSize() {
        return desiredSize;
    }

    public void render2(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        int winPos = (int)container.windowPosition();
        int windowHeight = (int) container.windowHeight();
        int padding = 2;
        int size = elementSize - padding - padding;
        int cols = (int) (currentSize.width() / elementSize);
        int rows = (int)Math.ceil((float)elements / cols);
        int rowsOffset = (int)Math.ceil((double) winPos / elementSize);
    }

    public void render(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        float windowPos = container.windowPosition();




        float modTicks = (windowPos % elementSize);
        float delta = windowPos - modTicks;
        //boolean shouldRoundUp = modTicks > (elementSize / 2f);
        //DebugText.add("Round up: " + shouldRoundUp);
        delta += modTicks > 0 ? elementSize : 0;


        TextureRegion blank = graphics.blank();
        float color = Color.WHITE.toFloatBits();
        int padding = 2;

        int winPos = (int)delta;
        int y_start = (int) (y0 + winPos);
        int size = elementSize - padding - padding;
        float widthInElements = currentSize.width() / elementSize;
        //DebugText.add("width in elements: " + widthInElements);
        int cols = (int) widthInElements;
        int x_off = (int) (((widthInElements - cols) * elementSize) / 2f);
        int rows = (int)Math.ceil((float)elements / cols);
        int rowsOffset = (int)Math.ceil((double) winPos / elementSize);
        //DebugText.add("rows: " + rows);
        //DebugText.add("cols: " + cols);
        //DebugText.add("modulus: " + (elements % cols));
        int remaining = elements- (cols * rowsOffset);
        outside:
        for (int r = rowsOffset; r < rows; r++) {
            int y_offset = ((r + 1) * elementSize);
            for (int c = 0; c < cols; c++) {
                if (remaining == 0) break outside;
                int x_offset = (c * elementSize);
                int x = x0 + x_offset + padding + x_off;
                int y = y_start - y_offset + padding;
                batch.draw(blank,x,y,size,size,color,0);
                remaining--;
            }
        }
        Size windowSize = container.windowSize();
        float w = windowSize.width();
        float h = windowSize.height();
        float y = y0 - h;
        //color = new Color(1,0,0,0.5f).toFloatBits();
        //batch.draw(blank,x0,y,w,h,color,0);

    }

    public void update(float dt) {

    }
}
