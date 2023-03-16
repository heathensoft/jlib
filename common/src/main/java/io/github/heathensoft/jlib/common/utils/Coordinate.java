package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.Defined;

import java.nio.ByteBuffer;

/**
 * Discrete coordinate
 * @author Frederik Dahl
 * 10/07/2022
 */

public class Coordinate implements Defined {

    public int x, y;
    
    public Coordinate(int x, int y) { set(x,y);}

    public Coordinate() {
        set(0,0);
    }
    
    public Coordinate(Coordinate c) {
        set(c.x,c.y);
    }
    
    public void set(Coordinate c) {
        set(c.x, c.y);
    }
    
    public void set(int x, int y) { this.x = x; this.y = y; }

    public void sub(Coordinate c) {
        sub(c.x,c.y);
    }

    public void sub(int x, int y) {
        this.x -= x; this.y -= y;
    }

    public void add(Coordinate c) {
        add(c.x,c.y);
    }

    public void add(int x, int y) {
        this.x += x; this.y += y;
    }
    
    /**
     * @param o coordinata
     * @return minimum discrete moves (diagonal allowed)
     */
    public int distance(Coordinate o) {
        return distance(o.x,o.y);
    }
    
    public int distance(int x, int y) {
        int xDist = Math.abs(this.x - x);
        int yDist = Math.abs(this.y - y);
        if (xDist < yDist)
            return xDist + (yDist - xDist);
        else return yDist + (xDist - yDist);
    }
    
    public int distanceX(Coordinate o) {
        return distanceX(o.x);
    }
    
    public int distanceX(int x) {
        return Math.abs(this.x - x);
    }
    
    public int distanceY(Coordinate o) {
        return distanceY(o.y);
    }
    
    public int distanceY(int y) {
        return Math.abs(this.y - y);
    }
    
    public boolean adjacentTo(Coordinate o) {
        return adjacentTo(o.x,o.y);
    }
    
    public boolean adjacentTo(int x, int y) {
        return Math.abs(this.x - x) < 2 && Math.abs(this.y - y) < 2;
    }
    
    public boolean equalsUnchecked(Coordinate o) {
        return this.x == o.x && this.y == o.y;
    }
    
    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }
    
    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate c = (Coordinate)o;
        return this.x == c.x && this.y == c.y;
    }

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public String toString () {
        return "(" + x + ", " + y + ")";
    }


    public void setProperties(ByteBuffer buffer) {
        x = buffer.getInt();
        y = buffer.getInt();
    }

    public void getProperties(ByteBuffer buffer) {
        buffer.putInt(x).putInt(y);
    }

    public int sizeOfProperties() {
        return 8;
    }
}
