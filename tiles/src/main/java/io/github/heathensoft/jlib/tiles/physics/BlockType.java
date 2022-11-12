package io.github.heathensoft.jlib.tiles.physics;

/**
 * @author Frederik Dahl
 * 05/11/2022
 */


public enum BlockType {
    
    NONE(false,0),
    SQUARE(true,0.5f),
    CIRCLE_S(true,0.125f),
    CIRCLE_M(true,0.25f),
    CIRCLE_L(true,0.5f);
    
    private final boolean isBlock;
    private final float radius;
    
    BlockType(boolean isBlock, float radius) {
        this.isBlock = isBlock;
        this.radius = radius;
    }
    
    public boolean isBlock() {
        return isBlock;
    }
    
    public float radius() {
        return radius;
    }
}
