package io.github.heathensoft.jlib.tiles.terrain;

/**
 * @author Frederik Dahl
 * 05/11/2022
 */


public enum TerrainType {
    
    T0((short) 0x0000,0),
    T1((short) 0xF000,1),
    T2((short) 0x0F00,2),
    T3((short) 0x00F0,3),
    T4((short) 0x000F,4);
    
    public final int rgba4;
    public final int layer;
    
    TerrainType(short rgba4, int layer) {
        this.rgba4 = rgba4;
        this.layer = layer;
    }
}
