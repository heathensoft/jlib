package io.github.heathensoft.jlib.tiles.physics;

import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.tiles.physics.obstacle.Edge;
import io.github.heathensoft.jlib.tiles.physics.obstacle.EdgeH;
import io.github.heathensoft.jlib.tiles.physics.obstacle.EdgeV;
import io.github.heathensoft.jlib.tiles.structure.TMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 05/11/2022
 */


public class BlockCluster {
    
    private final Area bounds;
    private final List<Edge> edges;
    
    public BlockCluster(TMap context, Area bounds, BitSet layout) {
        this.edges = new ArrayList<>();
        this.bounds = bounds;
        final int x0 = bounds.minX();
        final int y0 = bounds.minY();
        final int h = bounds.rows();
        final int w = bounds.cols();
        if (layout.cardinality() == bounds.size()) {
            Edge N = new EdgeH(y0+h, x0, x0+w);
            Edge S = new EdgeH(y0,x0 + w, x0);
            Edge E = new EdgeV(x0+w,y0,y0+h);
            Edge W = new EdgeV(x0,y0+h,y0);
            edges.add(N); edges.add(S);
            edges.add(E); edges.add(W);
        } else { Block[][] blocks = new Block[h][w];
            for (int cy = 0; cy < h; cy++) {
                for (int cx = 0; cx < w; cx++) {
                    if (layout.get(cx + cy * w)) {
                        blocks[cy][cx] = new Block();
                    }
                }
            } for (int cy = 0; cy < h; cy++) {
                int my = y0 + cy;
                for (int cx = 0; cx < w; cx++) {
                    int mx = x0 + cx;
                    if (layout.get(cx + cy * w)) {
                        int L = cx - 1; int R = cx + 1;
                        int U = cy + 1; int D = cy - 1;
                        Block C = blocks[cy][cx];
                        Block N = U < h  ? blocks[U][cx] : null;
                        Block S = D > -1 ? blocks[D][cx] : null;
                        Block E = R < w  ? blocks[cy][R] : null;
                        Block W = L > -1 ? blocks[cy][L] : null;
                        if (W == null) {
                            if (S != null && S.hasEdge(Edge.Face.WEST)) {
                                int id = S.getID(Edge.Face.WEST);
                                edges.get(id).addP0(1);
                                C.setID(Edge.Face.WEST,id);
                            } else { C.setID(Edge.Face.WEST,edges.size());
                                edges.add(new EdgeV(mx,my+1,my));}
                        } if (E == null) {
                            if (S != null && S.hasEdge(Edge.Face.EAST)) {
                                int id = S.getID(Edge.Face.EAST);
                                edges.get(id).addP1(1);
                                C.setID(Edge.Face.EAST,id);
                            } else { C.setID(Edge.Face.EAST,edges.size());
                                edges.add(new EdgeV(mx+1,my,my+1));}
                        } if (N == null) {
                            if (W != null && W.hasEdge(Edge.Face.NORTH)) {
                                int id = W.getID(Edge.Face.NORTH);
                                edges.get(id).addP1(1);
                                C.setID(Edge.Face.NORTH,id);
                            } else { C.setID(Edge.Face.NORTH,edges.size());
                                edges.add(new EdgeH(my+1,mx,mx+1));}
                        } if (S == null) {
                            if (W != null && W.hasEdge(Edge.Face.SOUTH)) {
                                int id = W.getID(Edge.Face.SOUTH);
                                edges.get(id).addP0(1);
                                C.setID(Edge.Face.SOUTH,id);
                            } else { C.setID(Edge.Face.SOUTH,edges.size());
                                edges.add(new EdgeH(my,mx+1,mx));
                            }
                        }
                    }
                }
            }
        }
    }
    
    public Area bounds() {
        return bounds;
    }
    
    public List<Edge> edges() {
        return edges;
    }
    
    private final static class Block {
        final int[] edge_ids = new int[] {-1,-1,-1,-1};
        boolean hasEdge(Edge.Face edge) {return edge_ids[edge.id] > -1;}
        int getID(Edge.Face edge) {return edge_ids[edge.id];}
        void setID(Edge.Face edge, int id) {edge_ids[edge.id] = id;}
    }
    
    /*
    public void blocks(List<Tile> blocks) {
        int count = count();
        if (count == 1) {
            Tile block = context.tile(
            bounds.minX(),bounds.minY());
            blocks.add(block);
        } else { int idx = 0;
            final int x0 = bounds.minX();
            final int y0 = bounds.minY();
            final int rows = bounds.rows();
            final int cols = bounds.cols();
            while (count > 0) {
                if (layout.get(idx)) {
                    int y = y0 + (idx / cols);
                    int x = x0 + (idx % cols);
                    blocks.add(context.tile(x,y));
                    count--;
                } idx++;
            }
        }
    }
    
    public int count() {
        return layout.cardinality();
    }
    
    public BitSet layout() {
        return layout;
    }
    
    public boolean contains(int x, int y) {
        if (bounds.contains(x,y)) {
            int lx = x - bounds.minX();
            int ly = y - bounds.minY();
            return layout.get(lx + ly * bounds.cols());
        } return false;
    }
    
     */
    
}
