package io.github.heathensoft.jlib.tiles.physics;

import io.github.heathensoft.jlib.common.storage.generic.Pool;

/**
 * @author Frederik Dahl
 * 03/11/2022
 */


class Ray2D implements Comparable<Ray2D> {
    
    public float x;
    public float y;
    public float theta;
    
    Ray2D() {}
   
    Ray2D(float dx, float dy, float theta) {
        this.x = dx;
        this.y = dy;
        this.theta = theta;
    }
    
    @Override
    public int compareTo(Ray2D o) {
        return Float.compare(o.theta, theta);
    }
    
    
    public static Pool<Ray2D> createPool(int initialCap) {
        return new Pool<>() {
            @Override
            protected Ray2D newObject() {
                return new Ray2D();
            }
        };
    }
    
}
