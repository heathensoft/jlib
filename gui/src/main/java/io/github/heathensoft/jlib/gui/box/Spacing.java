package io.github.heathensoft.jlib.gui.box;

/**
 * @author Frederik Dahl
 * 23/11/2022
 */


public class Spacing {

    private int top;
    private int right;
    private int bottom;
    private int left;

    public Spacing(int top, int right, int bottom, int left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public Spacing(int horizontal, int vertical) {
        this(vertical,horizontal,vertical,horizontal);
    }

    public Spacing(int spacing) {
        this(spacing,spacing);
    }

    public Spacing() {
        this(0);
    }

    public int top() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int right() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int bottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int left() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int horizontal() {
        return right + left;
    }

    public void setHorizontal(int spacing) {
        setRight(spacing);
        setLeft(spacing);
    }

    public int vertical() {
        return top + bottom;
    }

    public void setVertical(int spacing) {
        setTop(spacing);
        setBottom(spacing);
    }

    public void set(int spacing) {
        setHorizontal(spacing);
        setVertical(spacing);
    }
}
