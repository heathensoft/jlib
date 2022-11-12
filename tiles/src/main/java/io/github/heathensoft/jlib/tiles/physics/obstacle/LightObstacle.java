package io.github.heathensoft.jlib.tiles.physics.obstacle;


import org.joml.Vector2f;

import java.util.Collection;


/**
 * @author Frederik Dahl
 * 04/11/2022
 */


public interface LightObstacle {
    
    /**
     * Collects vertices and edges if they are facing the light.
     * @param lightPos position of light source
     * @param edges destination obstacle array
     * @param vertices destination vertex array
     */
    void collect(Vector2f lightPos, Collection<LightObstacle> edges, Collection<Vector2f> vertices);
    
    float rayIntersect(float ox, float oy, float dx, float dy);
    
    
    
}
