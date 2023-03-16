package io.github.heathensoft.jlib.tiles.physics;


import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.lwjgl.graphics.Color32;
import org.joml.Vector2f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Before drawing a light, it's necessary to check if its geometry
 * is valid. I.e. it had more than 2 vertices. sizeFloat > 4.
 * If the light was built, it will be valid.
 * Light geometry is drawn as a triangle-fan.
 *
 * @author Frederik Dahl
 * 12/11/2022
 */


public class Light2D {
    
    private final Geometry context;
    private final List<Ray2D> rays;
    private final Vector2f origin;
    private final Color32 color;
    private float intensity;
    private float radius;
    private float height;
    
    public Light2D(Geometry context, Vector2f origin, Color32 color, float radius, float height) {
        this.rays = new ArrayList<>();
        this.context = context;
        this.origin = origin;
        this.color = color;
        this.radius = radius;
        this.height = height;
    }
    
    public Light2D(Geometry context, Vector2f origin, Color32 color, float radius) {
        this(context,origin,color,radius,1f);
    }
    
    public Light2D(Geometry context, Vector2f origin, float radius) {
        this(context,origin,new Color32(),radius);
    }
    
    public void get(FloatBuffer buffer) {
        origin.get(buffer);
        for (Ray2D r : rays) {
            buffer.put(r.x).put(r.y);
        }
    }
    
    public int vertices() {
        return 1 + rays.size();
    }
    
    public int sizeFloat() {
        return vertices() * 2;
    }
    
    public float x() {
        return origin.x;
    }
    
    public float y() {
        return origin.y;
    }
    
    public float radius() {
        return radius;
    }
    
    public void setRadius(float r) {
        radius = r;
    }
    
    public void adjustRadius(float amount) {
        radius += amount;
    }
    
    public float height() {
        return height;
    }
    
    public void setHeight(float h) {
        height = h;
    }
    
    public void adjustHeight(float amount) {
        height += amount;
    }
    
    public float intensity() {
        return intensity;
    }
    
    public void setIntensity(float i) {
        intensity = i;
    }
    
    public void adjustIntensity(float amount) {
        intensity += amount;
    }
    
    public Color32 color() {
        return color;
    }
    
    public void setColor(Color32 c) {
        color.set(c);
    }
    
    public Vector2f origin() {
        return origin;
    }
    
    public Coordinate originDiscrete() {
        return new Coordinate((int)origin.x,(int)origin.y);
    }
    
    public void setOriginDiscrete(int x, int y) {
        origin.set(x + 0.5f, y + 0.5f);
    }
    
    public void setOriginDiscrete(Coordinate c) {
        setOriginDiscrete(c.x,c.y);
    }
    
    public void setOrigin(float x, float y) {
        origin.set(x, y);
    }
    
    public void translate(float x, float y) {
        origin.add(x, y);
    }
    
    public void translateDiscrete(int x, int y) {
        int x0 = (int)origin.x;
        int y0 = (int)origin.y;
        origin.add(x0 + x + 0.5f, y0 + y + 0.5f);
    }
    
    public void setOrigin(Vector2f position) {
        setOrigin(position.x,position.y);
    }
    
    public void translate(Vector2f translation) {
        translate(translation.x,translation.y);
    }
    
    public void updateGeometry() {
        context.buildLight(this);
        Collections.sort(rays);
    }
    
    protected void put(Ray2D ray) {
        rays.add(ray);
    }
    
    protected void clear() {
        rays.clear();
    }
}
