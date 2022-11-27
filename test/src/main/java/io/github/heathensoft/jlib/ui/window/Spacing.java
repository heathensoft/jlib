package io.github.heathensoft.jlib.ui.window;

/**
 * @author Frederik Dahl
 * 23/11/2022
 */


public class Spacing {

    private float top;
    private float right;
    private float bottom;
    private float left;

    public Spacing(float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public Spacing(float horizontal, float vertical) {
        this(vertical,horizontal,vertical,horizontal);
    }

    public Spacing(float spacing) {
        this(spacing,spacing);
    }

    public Spacing() {
        this(0);
    }

    public float top() {
        return top;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float right() {
        return right;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float bottom() {
        return bottom;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public float left() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float horizontal() {
        return right + left;
    }

    public void setHorizontal(float spacing) {
        setRight(spacing);
        setLeft(spacing);
    }

    public float vertical() {
        return top + bottom;
    }

    public void setVertical(float spacing) {
        setTop(spacing);
        setBottom(spacing);
    }

    public void set(float spacing) {
        setHorizontal(spacing);
        setVertical(spacing);
    }
}
