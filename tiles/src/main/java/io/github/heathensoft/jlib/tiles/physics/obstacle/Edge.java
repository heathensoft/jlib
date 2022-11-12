package io.github.heathensoft.jlib.tiles.physics.obstacle;

import io.github.heathensoft.jlib.common.utils.Area;
import org.joml.Vector2f;

import java.util.List;

/**
 * Condensed two vectors and a facing used to represent a directed
 * edge down to 3 integers. At the cost of some extra runtime calculations.
 * This works for rectangular blocks.
 *
 * @author Frederik Dahl
 * 05/11/2022
 */


public abstract class Edge implements LightObstacle {
    
    public enum Axis { X,Y }
    
    public enum Face {
        NORTH(0, Axis.X,new Vector2f(0,1),"NORTH"),
        SOUTH(1, Axis.X,new Vector2f(0,-1),"SOUTH"),
        EAST(2, Axis.Y,new Vector2f(-1,0),"EAST"),
        WEST(3, Axis.Y,new Vector2f(1,0), "WEST");
        public int id;
        public Axis axis;
        public String name;
        public Vector2f normal;
        Face(int id, Axis axis, Vector2f normal, String name) {
            this.axis = axis;
            this.name = name;
            this.normal = normal;
            this.id = id;
        }
    }
    
    protected int axis;
    protected int p0;
    protected int p1;
    
    public Edge(int axis, int p0, int p1) {
        this.axis = axis;
        this.p0 = p0;
        this.p1 = p1;
    }
    
    public final void move(int tilesX, int tilesY) {
        moveX(tilesX);
        moveX(tilesY);
    }
    
    public abstract void moveX(int tiles);
    
    public abstract void moveY(int tiles);
    
    public abstract Face face();
    
    public final Vector2f v0() {
        Vector2f v0 = new Vector2f();
        v0(v0);
        return v0;
    }
    
    public final Vector2f v1() {
        Vector2f v1 = new Vector2f();
        v1(v1);
        return v1;
    }
    
    public abstract void v0(Vector2f dest);
    
    public abstract void v1(Vector2f dest);
    
    public final void set(int axis, int p0, int p1) {
        this.axis = axis;
        this.p0 = p0;
        this.p1 = p1;
    }
    
    public final void setSegment(int p0, int p1) {
        this.p0 = p0;
        this.p1 = p1;
    }
    
    public final void setAxis(int axis) {
        this.axis = axis;
    }
    
    public final void setP0(int p0) {
        this.p0 = p0;
    }
    
    public final void setP1(int p1) {
        this.p1 = p1;
    }
    
    public final void addP0(int tiles) {
        this.p0 += tiles;
    }
    
    public final void addP1(int tiles) {
        this.p1 += tiles;
    }
    
    public final void incP0() {
        this.p0++;
    }
    
    public final void incP1() {
        this.p0++;
    }
    
    public final void addAxis(int tiles) {
        this.axis += tiles;
    }
    
    public final int segmentDirection() {
        return (p1 - p0) < 0 ? -1 : 1;
    }
    
    public final void invertFacing() {
        int tmp = p0;
        p0 = p1;
        p1 = tmp;
    }
    
    public final int axis() {
        return axis;
    }
    
    public final int p0() {
        return p0;
    }
    
    public final int p1() {
        return p1;
    }
    
    public static void buildRect(Area area, List<Edge> dest, boolean inwardFacing) {
        final int x0 = area.minX();
        final int y0 = area.minY();
        final int w = area.cols();
        final int h = area.rows();
        Edge N = new EdgeH(y0+h, x0, x0+w);
        Edge S = new EdgeH(y0,x0 + w, x0);
        Edge E = new EdgeV(x0+w,y0,y0+h);
        Edge W = new EdgeV(x0,y0+h,y0);
        if (inwardFacing) {
            N.invertFacing();
            S.invertFacing();
            E.invertFacing();
            W.invertFacing();
        }
        dest.add(N);
        dest.add(S);
        dest.add(E);
        dest.add(W);
    }
    
    @Override
    public String toString() {
        return "Edge{ "+ face().name +", axis: "+ axis + ", p0: " + p0 + ", p1: " + p1 + " }";
    }
}
