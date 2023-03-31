package io.github.heathensoft.jlib.ai.pathfinding.test;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;

/**
 * @author Frederik Dahl
 * 23/03/2023
 */


public interface I_TileMap {

    Area area();


    int rows();

    int cols();

    int roomID(int x, int y);

    int movementPenalty(int x, int y);

    boolean contains(int x, int y);

    boolean isObstacle(int x, int y);

    default int numTiles() { return rows() * cols(); }

    default int roomID(Coordinate c) {
        return roomID(c.x,c.y);
    }

    default boolean contains(Coordinate c) {
        return contains(c.x,c.y);
    }

    default boolean isObstacle(Coordinate c) {
        return isObstacle(c.x,c.y);
    }

    default boolean isTraversable(int x, int y) {
        return !isObstacle(x,y);
    }

    default boolean isTraversable(Coordinate c) {
        return isTraversable(c.x,c.y);
    }
}
