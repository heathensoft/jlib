package io.github.heathensoft.jlib.ai.pathfinding;



/**
 * @author Frederik Dahl
 * 14/07/2022
 */


public class NodeChain {
    
    private PathNode start;
    private PathNode end;
    private int count;


    public NodeChain(PathNode endNode) {
        int[] depth = new int[1];
        this.end = endNode;
        this.start = endNode.getSuper(depth);
        this.count = depth[0] + 1;
    }
    
    public void append(NodeChain chain) {
        if (chain.count > 0) {
            if (count == 0) {
                end = chain.end;
                start = chain.start;
                count = chain.count;
            } else {
                if (end.equalsUnchecked(chain.start)) {
                    pop();
                    if (count == 0) {
                        end = chain.end;
                        start = chain.start;
                        count = chain.count;
                        return;
                    }
                }
                chain.start.setParent(end);
                end = chain.end;
                count += chain.count;
                chain.count = count;
            }
        }
    }
    
    private void pop() {
        PathNode node = end;
        if (end != null) {
            PathNode next = node.getParent();
            node.setParent(null);
            end = next;
            if(end == null) {
                start = null;
            } count--;
        }
    }
    
    public int[] retracePath(boolean collapse) {
        if (collapse) collapse();
        int[] path = new int[count * 2];
        int index = path.length;
        if (index != 0) {
            PathNode current = end;
            while (current != null) {
                path[--index] = current.y;
                path[--index] = current.x;
                current = current.getParent();
            }
        } return path;
        
    }
    
    private void collapse() {
        PathNode rear = end;
        PathNode center = rear.getParent();
        PathNode front;
        float dirRCX, dirCFX;
        float dirRCY, dirCFY;
        while (center != null) {
            front = center.getParent();
            if (front == null) break;
            dirRCX = Math.signum(rear.x - center.x);
            dirRCY = Math.signum(rear.y - center.y);
            dirCFX = Math.signum(center.x - front.x);
            dirCFY = Math.signum(center.y - front.y);
            if (dirRCX == dirCFX && dirRCY == dirCFY) {
                rear.setParent(front); count--;
            } else rear = rear.getParent();
            center = front;
        }
    }
    
    public PathNode getStart() {
        return start;
    }
    
    public PathNode getEnd() {
        return end;
    }
    
    public int count() {
        return count;
    }
    
    
}
