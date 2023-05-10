package io.github.heathensoft.jlib.ai.pathfinding;


import io.github.heathensoft.jlib.common.utils.Coordinate;

/**
 * @author Frederik Dahl
 * 16/07/2022
 */


public class AStarNode extends PathNode implements Comparable<AStarNode> {

    private int hCost;      // distance from end node
    private int gCost;      // distance from starting node

    public AStarNode(int x, int y) {
        super(x, y);
    }
    
    public AStarNode(Coordinate coordinate) {
        super(coordinate);
    }
    
    public int getFCost() {
        return hCost + gCost;
    }
    
    public int getGCost() {
        return gCost;
    }
    
    public int getHCost() {
        return hCost;
    }
    
    public void setGCost(int gCost) {
        this.gCost = gCost;
    }
    
    public void setHCost(int hCost) {
        this.hCost = hCost;
    }
    
    public int getDistanceCost(AStarNode other) {
        int xDist = Math.abs(x - other.x);
        int yDist = Math.abs(y - other.y);
        if (xDist < yDist)
            return 14  * xDist + 10 * (yDist - xDist);
        else return 14 * yDist + 10 * (xDist - yDist);
    }

    @Override
    public int compareTo(AStarNode o) {
        int compare = Integer.compare(o.getFCost(), getFCost());
        return compare == 0 ? Integer.compare(o.hCost,hCost) : compare;
    }
}
