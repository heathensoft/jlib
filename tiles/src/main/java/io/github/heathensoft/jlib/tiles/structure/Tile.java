package io.github.heathensoft.jlib.tiles.structure;

import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.tiles.physics.BlockType;
import io.github.heathensoft.jlib.tiles.terrain.TerrainType;

/**
 * @author Frederik Dahl
 * 06/11/2022
 */


public class Tile extends Coordinate {

    private final TMap map;
    
    public Tile(int x, int y, TMap map) {
        super(x, y);
        this.map = map;
    }
    
    public TMap map() {
        return map;
    }
    
    public boolean isObstacle() {
        return map.isObstacle(x,y);
    }
    
    public boolean isBlock() {
        return blockType().isBlock();
    }
    
    public TerrainType terrainType() {
        return map.terrain(x,y);
    }
    
    public BlockType blockType() {
        return map.block(x,y);
    }
    
    public int movementPenalty() {
        return map.movementPenalty(x,y);
    }
    
    public int roomID() {
        return map.roomID(x,y);
    }
    
    public int get() {
        return map.get(x,y);
    }
    
    
}
