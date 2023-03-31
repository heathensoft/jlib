package io.github.heathensoft.jlib.ai.pathfinding.grid;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;

/**
 * @author Frederik Dahl
 * 12/11/2022
 */


public interface Grid {
    
    Area area();
    
    int roomID(int x, int y);
    
    int movementPenalty(int x, int y);
    
    boolean isObstacle(int x, int y);
    
    default boolean isTraversable(int x, int y) {
        return !isObstacle(x,y);
    }
    
    default boolean contains(int x, int y) {
        return area().contains(x, y);
    }
    
    default int roomID(Coordinate c) {
        return roomID(c.x,c.y);
    }
    
    default int movementPenalty(Coordinate c) {
        return movementPenalty(c.x,c.y);
    }
    
    default boolean isObstacle(Coordinate c) {
        return isObstacle(c.x,c.y);
    }
    
    default boolean isTraversable(Coordinate c) {
        return isTraversable(c.x,c.y);
    }
    
    default boolean contains(Coordinate c) {
        return contains(c.x,c.y);
    }
    
    default int rows() {
        return area().rows();
    }

    default int cols() {
        return area().cols();
    }
}
