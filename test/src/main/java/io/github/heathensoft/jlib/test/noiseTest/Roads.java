package io.github.heathensoft.jlib.test.noiseTest;

import io.github.heathensoft.jlib.ai.pathfinding.AStarNode;
import io.github.heathensoft.jlib.ai.pathfinding.Grid;
import io.github.heathensoft.jlib.ai.pathfinding.NodeChain;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.storage.generic.HeapSet;
import io.github.heathensoft.jlib.common.utils.U;

import java.util.HashSet;
import java.util.Set;



/**
 * @author Frederik Dahl
 * 16/04/2023
 */


public class Roads {


    public static int[] path(Coordinate start, Coordinate end, Grid grid) {
        int distance = start.distance(end);
        if (grid.contains(start) && grid.contains(end) && distance > 0) {
            int[][] adjacent = U.adj_8;
            int initial_cap = U.nextPowerOfTwo(distance * 4);
            HeapSet<AStarNode> open = new HeapSet<>(initial_cap);
            Set<AStarNode> closed = new HashSet<>(initial_cap);
            AStarNode start_node = new AStarNode(start);
            AStarNode target_node = new AStarNode(end);
            AStarNode tmp_node = new AStarNode(0,0);
            open.set(start_node);
            AStarNode current_node;
            while (open.notEmpty()) {
                current_node = open.pop();
                if (current_node.equals(target_node)) {
                    return new NodeChain(current_node).retracePath(false);
                } closed.add(current_node);
                for (int i = 0; i < 8; i++) {
                    int[] offset = adjacent[i];
                    int adjacent_x = current_node.x + offset[0];
                    int adjacent_y = current_node.y + offset[1];
                    if (grid.contains(adjacent_x,adjacent_y)) {
                        int adj_move_penalty = grid.movementPenalty(adjacent_x,adjacent_y);
                        if (adj_move_penalty != Integer.MAX_VALUE) {
                            tmp_node.set(adjacent_x,adjacent_y);
                            if (!closed.contains(tmp_node)) {
                                int g_cost = tmp_node.distance(current_node) + adj_move_penalty;
                                AStarNode adjacent_node = open.get(tmp_node);
                                if (adjacent_node == null) { // not in open set
                                    adjacent_node = new AStarNode(tmp_node);
                                    adjacent_node.setGCost(g_cost + current_node.getGCost());
                                    adjacent_node.setHCost(adjacent_node.distance(target_node));
                                    adjacent_node.setParent(current_node);
                                    open.set(adjacent_node);
                                } else {
                                    g_cost += current_node.getGCost();
                                    if (g_cost < adjacent_node.getGCost()) {
                                        adjacent_node.setGCost(g_cost);
                                        adjacent_node.setParent(current_node);
                                        open.set(adjacent_node);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } return new int[0];
    }



}
