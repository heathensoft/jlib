package io.github.heathensoft.jlib.ai.pathfinding;


import io.github.heathensoft.jlib.common.utils.Coordinate;

/**
 * @author Frederik Dahl
 * 14/07/2022
 */


public class PathNode extends Coordinate {
    
    protected PathNode parent;
    
    public PathNode(int x, int y) {
        super(x, y);
    }
    
    public PathNode(Coordinate coordinate) {
        super(coordinate);
    }
    
    public void setParent(PathNode parent) {
        this.parent = parent;
    }
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public PathNode getParent() {
        return parent;
    }
    
    public PathNode addReturnNext(int x, int y) {
        PathNode child = new PathNode(x,y);
        child.setParent(this);
        return child;
    }
    
    public PathNode getSuper() {
        PathNode node = this;
        while (node.hasParent()) {
            node = node.parent;
        } return node;
    }
    
    public PathNode getSuper(int[] depth) {
        PathNode node = this;
        while (node.hasParent()) {
            depth[0]++;
            node = node.parent;
        } return node;
    }
    
    
}
