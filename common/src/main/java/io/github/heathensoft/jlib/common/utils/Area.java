package io.github.heathensoft.jlib.common.utils;



/**
 * Discrete area
 * @author Frederik Dahl
 * 10/07/2022
 */


public class Area {
    
    protected int minX;
    protected int minY;
    protected int maxX;
    protected int maxY;
    
    
    public Area(int x1, int y1, int x2, int y2) {
        set(x1, y1, x2, y2);
    }
    
    public Area(int x, int y) {
        set(x, y);
    }
    
    public Area(Coordinate... coordinates) {
        set(coordinates);
    }
    
    public Area(Coordinate c1, Coordinate c2) {
        set(c1,c2);
    }
    
    public Area(Area a) {
        set(a);
    }
    
    public Area(Coordinate c) {
        set(c);
    }
    
    private Area() {
    
    }
    
    public void set(Area a) {
        this.minX = a.minX;
        this.minY = a.minY;
        this.maxX = a.maxX;
        this.maxY = a.maxY;
    }
    
    public void set(Coordinate c) {
        set(c.x,c.y);
    }
    
    public void set(int x, int y) {
        minX = x;
        minY = y;
        maxX = x;
        maxY = y;
    }
    
    public void set(int x1, int y1, int x2, int y2) {
        if (x1 < x2) {
            minX = x1;
            maxX = x2;
        } else {
            minX = x2;
            maxX = x1;
        }
        if (y1 < y2) {
            minY = y1;
            maxY = y2;
        } else {
            minY = y2;
            maxY = y1;
        }
    }
    
    public void set(Coordinate c1, Coordinate c2) {
        set(c1);
        expandToContain(c2);
    }
    
    public void set(Coordinate... coordinates) {
        if (coordinates == null) throw new IllegalArgumentException("coordinates == null");
        if (coordinates.length == 0) return;
        set(coordinates[0]);
        if (coordinates.length > 1) {
            for (int i = 1; i < coordinates.length; i++) {
                expandToContain(coordinates[i]);
            }
        }
    }
    
    public void expandToContain(Coordinate c) {
        expandToContain(c.x, c.y);
    }
    
    public void expandToContain(int x, int y) {
        minX = Math.min(x, minX);
        minY = Math.min(y, minY);
        maxX = Math.max(x, maxX);
        maxY = Math.max(y, maxY);
    }
    
    public void expandToContain(Area o) {
        minX = Math.min(minX, o.minX);
        minY = Math.min(minY, o.minY);
        maxX = Math.max(maxX, o.maxX);
        maxY = Math.max(maxY, o.maxY);
    }

    public void translate(Coordinate c) {
        translate(c.x,c.y);
    }
    
    public void translate(int x, int y) {
        minX += x;
        minY += y;
        maxX += x;
        maxY += y;
    }
    
    public void expand(int amount) {
        minX -= amount;
        minY -= amount;
        maxX += amount;
        maxY += amount;
    }
    
    public void expand(int amount, Area dest) {
        dest.set(this);
        dest.expand(amount);
    }
    
    public boolean onEdge(Coordinate c) {
        return onEdge(c.x,c.y);
    }
    
    public boolean onEdge(int x, int y) {
        if (x == minX || x == maxX) {
            return y >= minY && y <= maxY;
        } else if (y == minY || y == maxY) {
            return x >= minX && x <= maxX;
        } return false;
    }
    
    public boolean isCorner(Coordinate c) {
        return isCorner(c.x,c.y);
    }
    
    public boolean isCorner(int x, int y) {
        if (x == minX || x == maxX) {
            return y == minY || y == maxY;
        } return false;
    }
    
    public boolean contains(Coordinate c) {
        return contains(c.x,c.y);
    }
    
    public boolean contains(int x, int y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
    
    public boolean contains(Area o) {
        return o.minX >= minX && o.maxX <= maxX &&
               o.minY >= minY && o.maxY <= maxY;
    }
    
    public boolean intersects(Area o) {
        return minX <= o.maxX && maxX >= o.minX &&
               maxY >= o.minY && minY <= o.maxY;
    }
    
    public boolean intersects(int minX, int minY, int maxX, int maxY) {
        return this.minX <= maxX && this.maxX >= minX &&
               this.maxY >= minY && this.minY <= maxY;
    }
    
    public boolean intersection(Area o, Area dest) {
        dest.minX = Math.max(minX, o.minX);
        dest.minY = Math.max(minY, o.minY);
        dest.maxX = Math.min(maxX, o.maxX);
        dest.maxY = Math.min(maxY, o.maxY);
        return dest.isValid();
    }
    
    public boolean intersection(Area dest) {
        dest.minX = Math.max(minX, dest.minX);
        dest.minY = Math.max(minY, dest.minY);
        dest.maxX = Math.min(maxX, dest.maxX);
        dest.maxY = Math.min(maxY, dest.maxY);
        return dest.isValid();
    }
    
    public Coordinate closestPoint(Coordinate c, Coordinate dest) {
        if (c.x < minX) { dest.x = minX;
        } else dest.x = Math.min(c.x, maxX);
        if (c.y < minY) { dest.y = minY;
        } else dest.y = Math.min(c.y, maxY);
        return dest;
    }
    
    public void corners(Coordinate ur, Coordinate br, Coordinate bl, Coordinate ul) {
        ur.set(maxX,maxY);
        br.set(maxX,minY);
        bl.set(minX,minY);
        ul.set(minX,maxY);
    }
    
    public int minX() {
        return minX;
    }
    
    public int minY() {
        return minY;
    }
    
    public int maxX() {
        return maxX;
    }
    
    public int maxY() {
        return maxY;
    }
    
    public int cols() {
        return maxX - minX + 1;
    }
    
    public int rows() {
        return maxY - minY + 1;
    }
    
    public int size() {
        return cols() * rows();
    }
    
    public Coordinate max(Coordinate dest) {
        dest.set(maxX,maxY);
        return dest;
    }
    
    public Coordinate min(Coordinate dest) {
        dest.set(minX,minY);
        return dest;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + maxX;
        result = prime * result + maxY;
        result = prime * result + minX;
        result = prime * result + minY;
        return result;
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area)) return false;
        Area other = (Area) o;
        if (maxX != other.maxX) return false;
        if (maxY != other.maxY) return false;
        if (minX != other.minX) return false;
        return minY == other.minY;
    }
    
    public boolean isValid() {
        return minX <= maxX && minY <= maxY;
    }
    
    @Override
    public String toString () {
        return "min(" + minX + ", " + minY + ") , max(" + maxX + ", " + maxY + ")";
    }


}
