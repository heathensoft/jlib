package io.github.heathensoft.jlib.tiles.physics.obstacle;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.Collection;

/**
 *
 * I think I accidentally stumbled upon something genius:
 * First I thought 2 vertices and do ray-line intersection for a single edge that "turns toward the light".
 * Because that would be good enough to simulate a circle.
 * But using ray circle intersection, we can instead generate a collection of vertices along that line facing the light.
 * Then the rays will stop at the circle instead. Since the vertices are just guidelines anyway.
 * What this boils down to: jesus this is good, we only do one intersection test
 * instead of having x-edges to simulate a circle and do intersections on all of them.
 * We concentrate rays towards the line, and intersect with a circle. (semi-circle)
 *
 *
 * @author Frederik Dahl
 * 05/11/2022
 */


public class Circle implements LightObstacle {
    
    public static int ray_density = 1;
    private final Vector2f center;
    private float radius;
    
    public Circle(int x, int y) {
        this(x,y,0.5f);
    }
    
    public Circle(int x, int y, float r) {
        this.center = new Vector2f(x + 0.5f, y + 0.5f);
        this.radius = r;
    }
    
    @Override
    public void collect(Vector2f lightPos, Collection<LightObstacle> edges, Collection<Vector2f> vertices) {
        // check whether we are inside here?
        final int d = Math.max(1, ray_density * Math.round(radius));
        final float l = radius / d;
        Vector2f p = new Vector2f(center)
                .sub(lightPos).perpendicular().normalize();
        for (int i = -d; i <= d; i++) {
            Vector2f v = new Vector2f(p);
            v.mul(l * i).add(center);
            vertices.add(v);
        } edges.add(this);
    }
    
    @Override
    public float rayIntersect(float ox, float oy, float dx, float dy) {
        // could check to see if we are inside of circle first.
        // I think we already are checking-->
        // I have checked. If the light is within the circle, the
        // circle will contain the light entirely
        // could be I want this. Not sure.
        float r2 = radius * radius;
        float Lx = center.x - ox;
        float Ly = center.y - oy;
        float tca = Lx * dx + Ly * dy;
        float d2 = Lx * Lx + Ly * Ly - tca * tca;
        if (d2 > r2) return -1.0f; // <-- here
        float thc = Math.sqrt(r2 - d2);
        float t0 = tca - thc;
        float t1 = tca + thc;
        if (t0 < t1 && t1 >= 0.0f) {
            return t0;
        } return -1.0f;
    }
    
    public float radius() {
        return radius;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public Vector2f position() {
        return center;
    }
    
    public void setPosition(float x, float y) {
        center.set(x, y);
    }
    
    
}
