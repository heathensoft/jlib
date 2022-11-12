package io.github.heathensoft.jlib.tiles.physics.obstacle;

import org.joml.Vector2f;

import java.util.Collection;

/**
 * @author Frederik Dahl
 * 05/11/2022
 */


public class EdgeH extends Edge {
    
    
    public EdgeH(int axis, int p0, int p1) {
        super(axis, p0, p1);
    }
    
    @Override
    public void moveX(int tiles) {
        p0 += tiles;
        p1 += tiles;
    }
    
    @Override
    public void moveY(int tiles) {
        axis += tiles;
    }
    
    @Override
    public Face face() {
        if (segmentDirection() < 0) {
            return Face.SOUTH;
        } return Face.NORTH;
    }
    
    @Override
    public void v0(Vector2f dest) {
        dest.set(p0,axis);
    }
    
    @Override
    public void v1(Vector2f dest) {
        dest.set(p1,axis);
    }
    
    
    @Override
    public void collect(Vector2f lightPos, Collection<LightObstacle> edges, Collection<Vector2f> dest) {
        float y = axis;
        if (face() == Face.NORTH) {
            if (lightPos.y > y) {
                edges.add(this);
                dest.add(new Vector2f(p0,y));
                dest.add(new Vector2f(p1,y));}
        } else {
            if (lightPos.y < y) {
                edges.add(this);
                dest.add(new Vector2f(p0,y));
                dest.add(new Vector2f(p1,y));
            }
        }
    }
    
    @Override
    public float rayIntersect(float ox, float oy, float dx, float dy) {
        float v1X = ox - p0;
        float v1Y = oy - axis;
        float v2X = p1 - p0;
        float invV23 = 1.0f / (-v2X * dy);
        float t1 = (v2X * v1Y) * invV23;
        float t2 = (v1Y * dx - v1X * dy) * invV23;
        if (t1 >= 0.0f && t2 >= 0.0f && t2 <= 1.0f)
            return t1;
        return -1.0f;
    }
}
