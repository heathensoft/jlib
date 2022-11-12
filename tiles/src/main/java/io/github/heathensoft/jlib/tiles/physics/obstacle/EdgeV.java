package io.github.heathensoft.jlib.tiles.physics.obstacle;

import org.joml.Vector2f;

import java.util.Collection;

/**
 * @author Frederik Dahl
 * 05/11/2022
 */


public class EdgeV extends Edge {
    
    
    public EdgeV(int axis, int p0, int p1) {
        super(axis, p0, p1);
    }
    
    @Override
    public void moveX(int tiles) {
        axis += tiles;
    }
    
    @Override
    public void moveY(int tiles) {
        p0 += tiles;
        p1 += tiles;
    }
    
    @Override
    public Face face() {
        if (segmentDirection() < 0) {
            return Face.WEST;
        } return Face.EAST;
    }
    
    @Override
    public void v0(Vector2f dest) {
        dest.set(axis,p0);
    }
    
    @Override
    public void v1(Vector2f dest) {
        dest.set(axis,p1);
    }
    
    @Override
    public void collect(Vector2f lightPos, Collection<LightObstacle> edges, Collection<Vector2f> vertices) {
        float x = axis;
        if (face() == Face.WEST) {
            if (lightPos.x < x) {
                edges.add(this);
                vertices.add(new Vector2f(x,p0));
                vertices.add(new Vector2f(x,p1));}
        } else {
            if (lightPos.x > x) {
                edges.add(this);
                vertices.add(new Vector2f(x,p0));
                vertices.add(new Vector2f(x,p1));
            }
        }
    }
    
    @Override
    public float rayIntersect(float ox, float oy, float dx, float dy) {
        float v1X = ox - axis;
        float v1Y = oy - p0;
        float v2Y = p1 - p0;
        float invV23 = 1.0f / (v2Y * dx);
        float t1 = (-v2Y * v1X) * invV23;
        float t2 = (v1Y * dx - v1X * dy) * invV23;
        if (t1 >= 0.0f && t2 >= 0.0f && t2 <= 1.0f)
            return t1;
        return -1.0f;
    }
}
