package io.github.heathensoft.jlib.hud;


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
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public void set(float size) {
        set(size,size);
    }
    
    public void set(float width, float height) {
        setWidth(width);
        setHeight(height);
    }
    
    public void set(Size size) {
        set(size.width,size.height);
    }
    
    public void addWidth(float x) {
        width += x;
    }
    
    public void addHeight(float y) {
        height += y;
    }
    
    public void add(Size size) {
        add(size.width,size.height);
    }
    
    public void add(float size) {
        add(size,size);
    }
    
    public void add(float width, float height) {
        addWidth(width);
        addHeight(height);
    }
    
    public void subWidth(float x) {
        width -= x;
    }
    
    public void subHeight(float y) {
        height -= y;
    }
    
    public void sub(Size size) {
        sub(size.width,size.height);
    }
    
    public void sub(float size) {
        sub(size,size);
    }
    
    public void sub(float width, float height) {
        subWidth(width);
        subHeight(height);
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
