package io.github.heathensoft.jlib.test.brush;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.DiscreteLine;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.Set;

/**
 * @author Frederik Dahl
 * 05/01/2023
 */


public class Canvas implements Disposable {

    private PaintTool tool;
    private DiscreteLine lineDrawCoordinates;
    private Set<Coordinate> freeHandCoordinates;
    private Matrix4f canvasMatrix;
    private Area rectangleArea;
    private Coordinate cursorCurrent;
    private Coordinate cursorStart;
    private Brush brush;
    private Area canvasArea;
    private boolean editing;


    public void render() {

    }

    public void beginEdit(Vector2f mouseWorld) {
        if (!editing) {
            int mouse_x = (int) mouseWorld.x;
            int mouse_y = (int) mouseWorld.y;
            if (canvasArea.contains(mouse_x,mouse_y)) {
                cursorStart.set(mouse_x,mouse_y);
                editing = true;
            }
        }
    }

    public void onInput(Vector2f mouseWorld) {
        int mouse_x = (int) mouseWorld.x;
        int mouse_y = (int) mouseWorld.y;
        cursorCurrent.set(mouse_x,mouse_y);
        if (editing) {
            switch (tool) {
                case FREE_HAND -> {
                    Coordinate coordinate = new Coordinate(cursorCurrent);
                    freeHandCoordinates.add(coordinate);
                } case LINE_DRAW -> {
                    lineDrawCoordinates.set(cursorStart,cursorCurrent);
                } case RECTANGLE -> {
                    rectangleArea.set(cursorStart,cursorCurrent);
                }
            }
        }
    }

    public void undo() {
        // save backbuffer area (the size of undo area) as a redo-object
        //
    }

    public void endEdit() {
        // save backbuffer area as an undo-object
        // draw edit directly to backbuffer
        // then draw backbuffer to front-buffer
        if (editing) {
            freeHandCoordinates.clear();
            editing = false;
        }

    }

    public void translate(int x, int y) {
        canvasArea.translate(x, y);
    }

    public PaintTool tool() {
        return tool;
    }

    public BrushShape brushShape() {
        return brush.shape();
    }

    public int drawColor() {
        return brush.depthColor();
    }

    public int brushSize() {
        return brush.size();
    }

    public void dispose() {

    }
}
