package io.github.heathensoft.jlib.tiles.physics;


import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.storage.primitive.IntStack;
import io.github.heathensoft.jlib.common.storage.primitive.QTreeCompressed;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.tiles.TileUtil;
import io.github.heathensoft.jlib.tiles.physics.obstacle.Circle;
import io.github.heathensoft.jlib.tiles.physics.obstacle.Edge;
import io.github.heathensoft.jlib.tiles.physics.obstacle.LightObstacle;
import io.github.heathensoft.jlib.tiles.structure.TMap;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Frederik Dahl
 * 10/11/2022
 */


public class GeoRegion {
    
    private static final int CIRCLE_S_QID = 0;
    private static final int CIRCLE_M_QID = 1;
    private static final int CIRCLE_L_QID = 2;
    private static final int[][] quadrant_offset =
    new int[][] {{0,1},{1,1},{1,0},{0,0}}; // NW,NE,SE,SW
    private final List<List<BlockCluster>> blockGeometry;
    private final QTreeCompressed[] circleGeometry;
    
    
    protected GeoRegion(TMap map, int rX0, int rY0) {
        this.circleGeometry = new QTreeCompressed[4];
        this.blockGeometry = new ArrayList<>(4);
        int qSize = TMap.SECTION_SIZE / 2;
        rX0 *= TMap.SECTION_SIZE;
        rY0 *= TMap.SECTION_SIZE;
        for (int q = 0; q < 4; q++) {
            int x0 = rX0 + quadrant_offset[q][0] * qSize;
            int y0 = rY0 + quadrant_offset[q][1] * qSize;
            int x1 = x0 + qSize - 1;
            int y1 = y0 + qSize - 1;
            circleGeometry[q] = new QTreeCompressed(x0,y0,qSize);
            blockGeometry.add(new ArrayList<>(1));
            buildQuadrant(map,new Area(x0,y0,x1,y1),q);
        }
    }
    
    protected void gatherGeometry(Vector2f lightPos, Area lightArea,
    Set<Vector2f> vertices, List<LightObstacle> edges, int rX0, int rY0) {
        final int S = TMap.SECTION_SIZE;
        rX0 *= TMap.SECTION_SIZE;
        rY0 *= TMap.SECTION_SIZE;
        Area area = new Area(rX0,rY0,S-1,S-1);
        if (lightArea.intersection(area)) {
            final int qSize = S / 2;
            Area subArea = new Area();
            for (int q = 0; q < 4; q++) { // NW,NE,SE,SW
                int x0 = rX0 + quadrant_offset[q][0] * qSize;
                int y0 = rY0 + quadrant_offset[q][1] * qSize;
                int x1 = x0 + qSize - 1;
                int y1 = y0 + qSize - 1;
                subArea.set(x0,y0,x1,y1);
                if (area.intersection(subArea)) {
                    circleGeometry[q].query(subArea, (x, y, type) -> {
                        float radius; switch (type) {
                            case CIRCLE_S_QID -> { radius = BlockType.CIRCLE_S.radius(); }
                            case CIRCLE_M_QID -> { radius = BlockType.CIRCLE_M.radius(); }
                            case CIRCLE_L_QID -> { radius = BlockType.CIRCLE_L.radius(); }
                            default -> { return; }
                        } Circle circle = new Circle(x,y,radius);
                        circle.collect(lightPos,edges,vertices);
                    }); List<BlockCluster> clusters = blockGeometry.get(q);
                    for (BlockCluster cluster : clusters) {
                        if (subArea.intersects(cluster.bounds())) {
                            List<Edge> clusterGeo = cluster.edges();
                            for (Edge edge : clusterGeo) {
                                edge.collect(lightPos,edges,vertices);
                            }
                        }
                    }
                }
            }
        }
    }
    
    // updates geometry to reflect the current TMap state
    protected void refreshArea(TMap map, Area refreshArea, int rX0, int rY0) {
        final int S = TMap.SECTION_SIZE;
        rX0 *= TMap.SECTION_SIZE;
        rY0 *= TMap.SECTION_SIZE;
        Area regionArea = new Area(rX0,rY0,S-1,S-1);
        if (refreshArea.intersection(regionArea)) {
            final int qSize = S / 2;
            Area quadArea = new Area();
            for (int q = 0; q < 4; q++) { // NW,NE,SE,SW
                int x0 = rX0 + quadrant_offset[q][0] * qSize;
                int y0 = rY0 + quadrant_offset[q][1] * qSize;
                int x1 = x0 + qSize - 1;
                int y1 = y0 + qSize - 1;
                quadArea.set(x0,y0,x1,y1);
                if (regionArea.intersects(quadArea)) {
                    buildQuadrant(map,quadArea,q);
                }
            }
        }
    }
    
    private void buildQuadrant(TMap map, Area quadArea, int quadrant) {
        final int[][] adj = TileUtil.adjacent4;
        final int x0 = quadArea.minX();
        final int y0 = quadArea.minY();
        final int x1 = quadArea.maxX();
        final int y1 = quadArea.maxY();
        final int S = quadArea.cols();
        if (!blockGeometry.get(quadrant).isEmpty())
            blockGeometry.get(quadrant).clear();
        if (!circleGeometry[quadrant].isEmpty())
            circleGeometry[quadrant] = new QTreeCompressed(x0,y0,S);
        List<BlockCluster> blockGeo = blockGeometry.get(quadrant);
        QTreeCompressed circleGeo = circleGeometry[quadrant];
        int lx, ly, cx, cy, mnx, mny, lnx, lny, idx;
        BitSet visited = new BitSet(quadArea.size());
        IntQueue search = new IntQueue(32);
        IntStack blocks = new IntStack(32);
        for (int my = y0; my <= y1; my++) {
            ly = my - y0;
            for (int mx = x0; mx <= x1; mx++) {
                lx = mx - x0;
                idx = lx + ly * S;
                if (!visited.get(idx)) {
                    visited.set(idx);
                    BlockType type = map.block(mx,my);
                    switch (map.block(mx,my)) {
                        case NONE -> {}
                        case SQUARE -> {
                            Area clusterArea = new Area(mx,my);
                            search.clear();
                            search.enqueue(lx);
                            search.enqueue(ly);
                            blocks.push(mx);
                            blocks.push(my);
                            while (!search.isEmpty()) {
                                cx = search.dequeue();
                                cy = search.dequeue();
                                for (int i = 0; i < 4; i++) {
                                    lnx = cx + adj[i][0];
                                    lny = cy + adj[i][1];
                                    if (lnx >= 0 && lnx < S
                                    &&  lny >= 0 && lny < S) {
                                        idx = lnx + lny * S;
                                        if (!visited.get(idx)) {visited.set(idx);
                                            mnx = x0 + lnx; mny = y0 + lny;
                                            if (map.isBlock(mnx,mny)) {
                                                clusterArea.expandToContain(mnx,mny);
                                                blocks.push(mnx);
                                                blocks.push(mny);
                                                search.enqueue(lnx);
                                                search.enqueue(lny);
                                            }
                                        }
                                    }
                                }
                            }
                            int cx0 = clusterArea.minX();
                            int cy0 = clusterArea.minY();
                            int cr = clusterArea.rows();
                            int cc = clusterArea.cols();
                            BitSet layout = new BitSet(cc*cr);
                            while (!blocks.isEmpty()) {
                                cy = blocks.pop() - cy0;
                                cx = blocks.pop() - cx0;
                                layout.set(cx + cy * cc);
                            } blockGeo.add(new BlockCluster(map,clusterArea,layout));
                        }
                        case CIRCLE_S -> {circleGeo.insert(mx,my,CIRCLE_S_QID);}
                        case CIRCLE_M -> {circleGeo.insert(mx,my,CIRCLE_M_QID);}
                        case CIRCLE_L -> {circleGeo.insert(mx,my,CIRCLE_L_QID);}
                    }
                }
            }
        }
    }
    
    
}
