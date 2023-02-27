package io.github.heathensoft.jlib.common.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Frederik Dahl
 * 03/01/2023
 */


public class DiscreteLine implements Iterable<Coordinate> {

    private final List<Coordinate> points;

    public DiscreteLine(int x0, int y0, int x1, int y1) {
        this(new Coordinate(x0,y0),new Coordinate(x1,y1));
    }

    public DiscreteLine(Coordinate p0, Coordinate p1) {
        int capacity = p0.distance(p1) + 1;
        points = new ArrayList<>(capacity);
        build(p0, p1);
    }

    public void set(int x0, int y0, int x1, int y1) {
        set(new Coordinate(x0,y0),new Coordinate(x1,y1));
    }

    public void set(Coordinate p0, Coordinate p1) {
        Coordinate start = points.get(0);
        if (size() > 1) {
            Coordinate end = points.get(size() - 1);
            if (p0.equals(start)) {
                if (p1.equals(end)) return;
            } else if (p0.equals(end)) {
                if (p1.equals(start)) return;
            }
        } else {
            if (p0.equals(p1)) {
                if (p0.equals(start)) {
                    return;
                }
            }
        }
        points.clear();
        build(p0, p1);
    }

    public Coordinate p0() {
        return points.get(0);
    }

    public Coordinate p1() {
        return points.get(size() - 1);
    }

    public Iterator<Coordinate> iterator() {
        return points.iterator();
    }

    public void forEach(Consumer<? super Coordinate> action) {
        points.forEach(action);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int size() {
        return points.size();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscreteLine line = (DiscreteLine) o;
        Coordinate p00 = p0();
        Coordinate p01 = p1();
        Coordinate p10 = line.p0();
        Coordinate p11 = line.p1();
        return p00.equals(p10) && p01.equals(p11);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Coordinate p0 = p0();
        Coordinate p1 = p1();
        result = prime * result + p0.x;
        result = prime * result + p0.y;
        result = prime * result + p1.x;
        result = prime * result + p1.y;
        return result;
    }

    public void build(Coordinate p0, Coordinate p1) {
        int x0 = p0.x; int y0 = p0.y;
        int x1 = p1.x; int y1 = p1.y;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx-dy;
        int e2;
        while (true)  {
            points.add(new Coordinate(x0,y0));
            if (x0 == x1 && y0 == y1) break;
            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            } if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
    }
}
