package io.github.heathensoft.jlib.tiles.physics;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.tiles.physics.obstacle.Edge;
import io.github.heathensoft.jlib.tiles.physics.obstacle.LightObstacle;
import io.github.heathensoft.jlib.tiles.structure.TMap;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.*;

/**
 * @author Frederik Dahl
 * 11/11/2022
 */


public class Geometry {
    
    private final TMap map;
    private final GeoRegion[][] regions;
    
    
    public Geometry(TMap map) {
        int size = map.dimension().sizeInRegions();
        this.map = map;
        this.regions = new GeoRegion[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                regions[r][c] = new GeoRegion(map,c,r);
            }
        }
    }
    
    public Light2D newLight(Vector2f origin, float radius) {
        return new Light2D(this,origin,radius);
    }
    
    protected void buildLight(Light2D light) {
        List<LightObstacle> edges = new ArrayList<>(16);
        Set<Vector2f> vertices = new HashSet<>(31);
        buildLight(light,edges,vertices);
    }
    
    protected void buildLight(List<Light2D> lights) {
        if (!lights.isEmpty()) {
            List<LightObstacle> edges = new ArrayList<>(32); // arbitrary
            Set<Vector2f> vertices = new HashSet<>(61); // arbitrary prime
            for (Light2D light : lights) {
                buildLight(light,edges,vertices);
            }
        }
    }
    
    private void buildLight(Light2D light, List<LightObstacle> edges, Set<Vector2f> vertices) {
        vertices.clear();
        edges.clear();
        light.clear();
        final float radius = light.radius();
        final float ox = light.x();
        final float oy = light.y();
        Area lightArea = new Area(
        (int)(ox-radius),(int)(oy-radius),
        (int)(ox+radius),(int)(oy+radius));
        if (map.area().intersection(lightArea)) {
            List<Edge> lightOuter = new ArrayList<>(4);
            Edge.buildRect(lightArea,lightOuter,true);
            for (Edge outerEdge : lightOuter)
                outerEdge.collect(light.origin(),edges,vertices);
            final int rx0 = lightArea.minX() / TMap.SECTION_SIZE;
            final int ry0 = lightArea.minY() / TMap.SECTION_SIZE;
            final int rx1 = lightArea.maxX() / TMap.SECTION_SIZE;
            final int ry1 = lightArea.maxY() / TMap.SECTION_SIZE;
            for (int r = rx0; r <= rx1; r++) {
                for (int c = ry0; c <= ry1; c++) {
                    regions[r][c].gatherGeometry(
                    light.origin(),lightArea,vertices,edges,c,r);
                }
            } for (Vector2f v : vertices) {
                float rdx = v.x - ox;
                float rdy = v.y - oy;
                float base_angle = Math.atan2(rdy,rdx);
                float angle = base_angle - 0.0002f;
                for (int i = 0; i < 3; i++) {
                    float t_min = Float.MAX_VALUE;
                    float x_min = 0, y_min = 0;
                    angle += 0.0001f;
                    rdx = Math.cos(angle);
                    rdy = Math.sin(angle);
                    for (LightObstacle edge : edges) {
                        float t = edge.rayIntersect(
                        ox,oy,rdx,rdy);
                        if (t != -1) {
                            if (t < t_min) {
                                t_min = t;
                                x_min = ox + t * rdx;
                                y_min = oy + t * rdy;
                                // atan2 here?
                            }
                        }
                    }
                    if (t_min < Float.MAX_VALUE) {
                        light.put(new Ray2D(x_min,y_min,angle));
                    }
                }
            }
        }
    }
    
    public void refresh(Area area) {
        Area intersection = new Area(area);
        if (map.area().intersection(intersection)) {
            final int rx0 = intersection.minX() / TMap.SECTION_SIZE;
            final int ry0 = intersection.minY() / TMap.SECTION_SIZE;
            final int rx1 = intersection.maxX() / TMap.SECTION_SIZE;
            final int ry1 = intersection.maxY() / TMap.SECTION_SIZE;
            for (int r = rx0; r <= rx1; r++) {
                for (int c = ry0; c <= ry1; c++) {
                    regions[r][c].refreshArea(map,intersection,c,r);
                }
            }
        }
    }
    
    
}
