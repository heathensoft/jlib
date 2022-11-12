package io.github.heathensoft.jlib.common.storage.primitive;

import io.github.heathensoft.jlib.common.utils.Area;

/**
 * Fast compressed quadtree
 *
 * x range 0 - 4095
 * y range 0 - 4095
 * v range 0 - 255
 *
 * @author Frederik Dahl
 * 10/11/2022
 */


public class QTreeCompressed {
    
    @FunctionalInterface
    public interface Itr {
        void next(int x, int y, int value);
    }
    
    private static final int NW = 0;
    private static final int NE = 1;
    private static final int SW = 2;
    private static final int SE = 3;
    private static final int CAP = 4;
    private final short x0, y0, s;
    private final int[] points;
    private QTreeCompressed[] regions;
    private byte idx;
    
    
    /**
     * Convenience constructor. Values are cast to short.
     * Tree does not support x,y values outside range 0-4095
     * @param x0 start x
     * @param y0 start y
     * @param size size
     */
    public QTreeCompressed(int x0, int y0, int size) {
        this((short) x0,(short) y0,(short) size);
    }
    
    public QTreeCompressed(short x0, short y0, short size) {
        this.points = new int[CAP];
        this.s = size;
        this.x0 = x0;
        this.y0 = y0;
    }
    
    public void insert(int compressed) {
        int x = compressed >> 20;
        int y = (compressed >> 8) & 0xFFF;
        byte v = (byte) (compressed & 0xFF);
        insertInternal(x,y,v);
    }
    
    public void insert(int x, int y, int v) {
        insertInternal(x&0xFFF,y&0xFFF,(byte)(v&0xFF));
    }
    
    public void insert(int x, int y, byte v) {
        insertInternal(x&0xFFF,y&0xFFF,v);
    }
    
    private void insertInternal(int x, int y, byte v) {
        if (contains(x,y)) {
            if (idx<CAP) {
                points[idx++] = (x << 20) | (y << 8) | v;
            } else {
                if (idx==CAP) {
                    split();
                    idx++;
                }
                regions[NW].insert(x,y,v);
                regions[NE].insert(x,y,v);
                regions[SW].insert(x,y,v);
                regions[SE].insert(x,y,v);
            }
        }
    }
    
    public void query(Area area,Itr itr) {
        if (overlap(area)) {
            int l = Math.min(idx,CAP);
            for (int i = 0; i < l; i++) {
                int p = points[i];
                int x = p >> 20;
                int y = (p >> 8) & 0xFFF;
                if (area.contains(x,y))
                    itr.next(x,y,(p & 0xFF));
            } if (idx > CAP) { // if split
                regions[NW].query(area,itr);
                regions[NE].query(area,itr);
                regions[SW].query(area,itr);
                regions[SE].query(area,itr);
            }
        }
    }
    
    public void query(Area area, IntQueue q) {
        if (overlap(area)) {
            int l = Math.min(idx,CAP);
            for (int i = 0; i < l; i++) {
                int p = points[i];
                int x = p >> 20;
                int y = (p >> 8) & 0xFFF;
                if (area.contains(x,y))
                    q.enqueue(p);
            } if (idx > CAP) { // if split
                regions[NW].query(area,q);
                regions[NE].query(area,q);
                regions[SW].query(area,q);
                regions[SE].query(area,q);
            }
        }
    }
    
    // returns compressed values
    public void empty(IntQueue q) {
        int l = Math.min(idx,CAP);
        for (int i = 0; i < l; i++) {
            q.enqueue(points[i]);
        } if (idx > CAP) {
            regions[NW].empty(q);
            regions[NE].empty(q);
            regions[SW].empty(q);
            regions[SE].empty(q);
            regions[NW] = null;
            regions[NE] = null;
            regions[SW] = null;
            regions[SE] = null;
        } idx = 0;
    }
    
    public boolean isEmpty() {
        return idx == 0;
    }
    
    private void split() {
        final short s =  (short) (this.s / 2);
        final short x1 = (short) (x0 + s);
        final short y1 = (short) (y0 + s);
        regions = new QTreeCompressed[4];
        regions[SW] = new QTreeCompressed(x0, y0, s);
        regions[SE] = new QTreeCompressed(x1, y0, s);
        regions[NW] = new QTreeCompressed(x0, y1, s);
        regions[NE] = new QTreeCompressed(x1, y1, s);
    }
    
    private boolean contains(int x, int y) {
        return x0 <= x && x0 + s > x && y0 <= y && y0 + s > y;
    }
    
    private boolean overlap(Area area) {
        int l = s - 1;
        return area.intersects(x0,y0,x0+l,y0+l);
    }
    
}
