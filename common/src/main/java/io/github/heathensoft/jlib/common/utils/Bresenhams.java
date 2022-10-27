package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;

/**
 * @author Frederik Dahl
 * 08/08/2022
 */


public class Bresenhams {
    
    
    private static final IntQueue points = new IntQueue(16);
    
    public static IntQueue plot(int x0, int y0, int x1, int y1) {
        points.clear();
        final int dx = x1 - x0;
        final int dy = y1 - y0;
        int D = 2*dy - dx;
        int y = y0;
        for (int x = x0; x <= x1; x++) {
            points.enqueue(x);
            points.enqueue(y);
            if (D > 0) {
                y = y + 1;
                D = D - 2*dx;
            } D = D + 2*dy;
        } return points;
    }
    
    public static IntQueue plot(Coordinate c0, Coordinate c1) {
       return plot(c0.x, c0.y, c1.x, c1.y);
    }
    
    public IntQueue plot(int x0, int y0, int x1, int y1, IntQueue points) {
        final int dx = x1 - x0;
        final int dy = y1 - y0;
        int D = 2*dy - dx;
        int y = y0;
        for (int x = x0; x <= x1; x++) {
            points.enqueue(x);
            points.enqueue(y);
            if (D > 0) {
                y = y + 1;
                D = D - 2*dx;
            } D = D + 2*dy;
        } return points;
    }
    
    public IntQueue plot(Coordinate c0, Coordinate c1, IntQueue points) {
        return plot(c0.x, c0.y, c1.x, c1.y, points);
    }
}
