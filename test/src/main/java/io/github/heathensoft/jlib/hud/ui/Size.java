package io.github.heathensoft.jlib.hud.ui;


import java.util.Objects;

/**
 * @author Frederik Dahl
 * 18/11/2022
 */


public class Size {
 
    private float width;
    private float height;
    
    public Size() {
        this(0);
    }
    
    public Size(float size) {
        this(size,size);
    }
    
    public Size(Size size) {
        this(size.width,size.height);
    }
    
    public Size(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    public float width() {
        return width;
    }
    
    public float height() {
        return height;
    }
    
    public Size setWidth(float width) {
        this.width = width;
        return this;
    }
    
    public Size setHeight(float height) {
        this.height = height;
        return this;
    }
    
    public Size set(float size) {
        return set(size,size);
    }
    
    public Size set(float width, float height) {
        return setWidth(width).setHeight(height);
    }
    
    public Size set(Size size) {
        return set(size.width,size.height);
    }
    
    public Size addWidth(float x) {
        width += x;
        return this;
    }
    
    public Size addHeight(float y) {
        height += y;
        return this;
    }
    
    public Size add(Size size) {
        return add(size.width,size.height);
    }
    
    public Size add(float size) {
        return add(size,size);
    }
    
    public Size add(float width, float height) {
        return addWidth(width).addHeight(height);
    }
    
    public Size subWidth(float x) {
        width -= x;
        return this;
    }
    
    public Size subHeight(float y) {
        height -= y;
        return this;
    }
    
    public Size sub(Size size) {
        return sub(size.width,size.height);
    }
    
    public Size sub(float size) {
        return sub(size,size);
    }
    
    public Size sub(float width, float height) {
        return subWidth(width).subHeight(height);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Size size = (Size) o;
        return Float.compare(size.width, width) == 0 && Float.compare(size.height, height) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "Size{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
