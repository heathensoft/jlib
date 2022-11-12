package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.common.storage.generic.Reader;
import org.joml.Vector2f;
import org.joml.primitives.Circlef;
import org.joml.primitives.Intersectionf;
import org.joml.primitives.Rectanglef;

import java.util.Collection;

/**
 * Squared QuadTree (width == height)
 * Not thread-safe (internal static objects)
 *
 * @author Frederik Dahl
 * 10/08/2022
 */

@SuppressWarnings("unchecked")
public class QuadTree<E> {
    
    private static final record Point<E>(float x, float y, E e) { }
    
    private static final int NW = 0;
    private static final int NE = 1;
    private static final int SW = 2;
    private static final int SE = 3;
    private static final int CAP = 5;
    
    private static final Circlef C = new Circlef();
    private static final Rectanglef R = new Rectanglef();
    
    private final Point<E>[] points;
    private QuadTree<E>[] regions;
    private int idx;
    
    private final float x0, y0, s;
    
    public QuadTree(float x0, float y0, float size) {
        this.points = new Point[CAP];
        this.s = size;
        this.x0 = x0;
        this.y0 = y0;
    }
    
    public void insert(E e, Vector2f v) {
        insert(new Point<>(v.x, v.y, e));
    }
    
    public void insert(E e, float x, float y) {
        insert(new Point<>(x, y, e));
    }
    
    public void query(float x, float y, float r, Reader<E> itr) {
        // Notice that C.r is set to radius squared
        C.x = x; C.y = y; C.r = r * r; queryC(itr);
    }
    
    public void query(Circlef c, Reader<E> itr) {
        // Notice that C.r is set to radius squared
        C.x = c.x; C.y = c.y; C.r = c.r * c.r; queryC(itr);
    }
    
    public void query(float x0, float y0, float w, float h, Reader<E> itr) {
        R.setMax(x0+w,y0+h);
        R.setMin(x0,y0);
        queryR(itr);
    }
    
    public void query(Rectanglef r, Reader<E> itr) {
        R.set(r);
        queryR(itr);
    }
    
    private void queryC(Reader<E> itr) {
        if (overlapsC()) {
            int l = Math.min(idx,CAP);
            for (int i = 0; i < l; i++) {
                Point<E> p = points[i];
                final float dx = p.x - C.x;
                final float dy = p.y - C.y;
                final float d2 = dx * dx + dy * dy;
                if (d2 < C.r) itr.next(p.e);
            } if (idx > CAP) { // if split
                regions[NW].queryC(itr);
                regions[NE].queryC(itr);
                regions[SW].queryC(itr);
                regions[SE].queryC(itr);
            }
        }
    }
    
    private void queryR(Reader<E> itr) {
        if (overlapsR()) {
            int l = Math.min(idx,CAP);
            for (int i = 0; i < l; i++) {
                Point<E> p = points[i];
                if (R.containsPoint(p.x,p.y))
                    itr.next(p.e);
            } if (idx > CAP) { // if split
                regions[NW].queryR(itr);
                regions[NE].queryR(itr);
                regions[SW].queryR(itr);
                regions[SE].queryR(itr);
            }
        }
    }
    
    public void empty(Collection<E> c) {
        int l = Math.min(idx,CAP);
        for (int i = 0; i < l; i++) {
            Point<E> p = points[i];
            points[i] = null;
            c.add(p.e);
        } if (idx > CAP) {
            regions[NW].empty(c);
            regions[NE].empty(c);
            regions[SW].empty(c);
            regions[SE].empty(c);
            regions[NW] = null;
            regions[NE] = null;
            regions[SW] = null;
            regions[SE] = null;
        } idx = 0;
    }
    
    private void insert(Point<E> p) {
        if (contains(p.x,p.y)) {
            if (idx < CAP) points[idx++] = p;
            else {
                if (idx == CAP){
                    split();
                    idx++;
                }
                regions[NW].insert(p);
                regions[NE].insert(p);
                regions[SW].insert(p);
                regions[SE].insert(p);
            }
        }
    }
    
    private void split() {
        final float s = this.s / 2;
        final float x1 = x0 + s;
        final float y1 = y0 + s;
        regions = new QuadTree[4];
        regions[SW] = new QuadTree<>(x0, y0, s);
        regions[SE] = new QuadTree<>(x1, y0, s);
        regions[NW] = new QuadTree<>(x0, y1, s);
        regions[NE] = new QuadTree<>(x1, y1, s);
    }
    
    // can't inhabit than one region, so we include/exclude two edge-pairs
    private boolean contains(float x, float y) {
        return x0 <= x && x0 + s > x && y0 <= y && y0 + s > y;
    }
    
    private boolean overlapsR() {
        final float x = x0 + s;
        final float y = y0 + s;
        return Intersectionf.testAarAar(x0,y0,x,y,R.minX,R.minY,R.maxX,R.maxY);
    }
    
    private boolean overlapsC() {
        final float x = x0 + s;
        final float y = y0 + s;
        return Intersectionf.testAarCircle(x0,y0,x,y,C.x,C.y,C.r);
    }
    
}
