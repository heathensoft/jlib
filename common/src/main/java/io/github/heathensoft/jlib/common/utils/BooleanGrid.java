package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.storage.primitive.BitSet;

/**
 * @author Frederik Dahl
 * 03/01/2023
 */


public class BooleanGrid {

    private final BitSet bits;
    private final Area area;


    public BooleanGrid(Area area) {
        this.area = new Area(area);
        this.bits = new BitSet(area.size());
    }

    public void setSubArea(Area area) {
        Area intersection = new Area(area);
        if (this.area.intersection(intersection)) {
            int minX = intersection.minX;
            int minY = intersection.minY;
            int maxX = intersection.maxX;
            int maxY = intersection.maxY;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    int local_x = c - this.area.minX();
                    int local_y = r - this.area.minY();
                    bits.setUnsafe(local_x + local_y * area.cols());
                }
            }
        }
    }

    public void clearSubArea(Area area) {
        Area intersection = new Area(area);
        if (this.area.intersection(intersection)) {
            int minX = intersection.minX;
            int minY = intersection.minY;
            int maxX = intersection.maxX;
            int maxY = intersection.maxY;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    int local_x = c - this.area.minX();
                    int local_y = r - this.area.minY();
                    bits.clearUnsafe(local_x + local_y * area.cols());
                }
            }
        }
    }

    public void setUnsafe(int x, int y) {
        int local_x = x - area.minX();
        int local_y = y - area.minY();
        bits.setUnsafe(local_x + local_y * area.cols());
    }

    public void set(int x, int y) {
        if (area.contains(x, y)) {
            int local_x = x - area.minX();
            int local_y = y - area.minY();
            bits.setUnsafe(local_x + local_y * area.cols());
        }
    }

    public void clear(int x, int y) {
        if (area.contains(x, y)) {
            int local_x = x - area.minX();
            int local_y = y - area.minY();
            bits.clearUnsafe(local_x + local_y * area.cols());
        }
    }

    public boolean getUnsafe(int x, int y) {
        int local_x = x - area.minX();
        int local_y = y - area.minY();
        return bits.getUnsafe(local_x + local_y * area.cols());
    }

    public boolean get(int x, int y) {
        if (area.contains(x, y)) {
            int local_x = x - area.minX();
            int local_y = y - area.minY();
            return bits.getUnsafe(local_x + local_y * area.cols());
        } return false;
    }

    public Area area() {
        return area;
    }

    public Area area(Area dest) {
        dest.set(area);
        return dest;
    }

    public void clear() {
        bits.clear();
    }


}
