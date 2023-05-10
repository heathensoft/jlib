package io.github.heathensoft.jlib.ai.pathfinding;

import io.github.heathensoft.jlib.common.utils.Coordinate;

/**
 * @author Frederik Dahl
 * 18/04/2023
 */


public interface Grid {

    int rows();

    int cols();

    int movementPenalty(int x, int y);

    boolean isObstacle(int x, int y);

    boolean contains(int x, int y);

    default int movementPenalty(Coordinate coordinate) {
        return movementPenalty(coordinate.x,coordinate.y);
    }

    default boolean isTraversable(int x, int y) {
        return !isObstacle(x, y);
    }

    default boolean isTraversable(Coordinate coordinate) {
        return isTraversable(coordinate.x,coordinate.y);
    }

    default boolean isObstacle(Coordinate coordinate) {
        return isObstacle(coordinate.x,coordinate.y);
    }

    default boolean contains(Coordinate coordinate) {
        return contains(coordinate.x,coordinate.y);
    }

}
