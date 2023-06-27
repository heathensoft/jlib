package io.github.heathensoft.jlib.tiles.neo;

/**
 *
 * Terrain cannot count as obstacles. But there is no limit to movement penalty
 *
 * @author Frederik Dahl
 * 11/05/2023
 */


public enum TerrainType {

    T0(0x00,(short) 0x0000,0,"CLEAR"), // Base Layer
    T1(0x01,(short) 0x000F,1,"ALPHA"),
    T2(0x02,(short) 0x00F0,2,"BLUE"),
    T3(0x04,(short) 0x0F00,3,"GREEN"),
    T4(0x08,(short) 0xF000,4,"RED"); // Top Layer

    public static final String DESCRIPTOR = "Terrain Type";
    public static final TerrainType[] ALL = values();
    public static final int COUNT = ALL.length;

    public static TerrainType get(int id) {
        return ALL[id % COUNT];
    }

    // Adjustable (i'm sure i'll add more)
    public String name = "Terrain";
    public int movement_penalty = 0;
    public boolean flammable = false;

    public final String color_channel;
    public final int id;
    public final int mask;
    public final short abgr4;

    TerrainType(int mask, short abgr4, int id, String color_channel) {
        this.color_channel = color_channel;
        this.abgr4 = abgr4;
        this.mask = mask;
        this.id = id;
    }

    public TerrainType next() {
        return get(id + 1);
    }

    public TerrainType prev() {
        return id == 0 ? ALL[(COUNT - 1)] : ALL[id - 1];
    }

    public static TerrainType top_layer(int tile_terrain_layers) {
        if ((tile_terrain_layers & T4.mask) == T4.mask) return T4;
        if ((tile_terrain_layers & T3.mask) == T3.mask) return T3;
        if ((tile_terrain_layers & T2.mask) == T2.mask) return T2;
        if ((tile_terrain_layers & T1.mask) == T1.mask) return T1;
        return T0;
    }

    public static short rgba4(int tile_terrain_layers) {
        int rgba4 = 0;
        rgba4 |= ((tile_terrain_layers & T1.mask) == T1.mask ? T1.abgr4 : 0);
        rgba4 |= ((tile_terrain_layers & T2.mask) == T2.mask ? T2.abgr4 : 0);
        rgba4 |= ((tile_terrain_layers & T3.mask) == T3.mask ? T3.abgr4 : 0);
        rgba4 |= ((tile_terrain_layers & T4.mask) == T4.mask ? T4.abgr4 : 0);
        return (short) rgba4;
    }

    public static boolean contains(int tile_terrain_layers, TerrainType type) {
        return (tile_terrain_layers & type.mask) == type.mask;
    }

    public static int place_terrain_type(int tile_terrain_layers, TerrainType type) {
        return tile_terrain_layers | type.mask;
    }

    public static int remove_terrain_type(int tile_terrain_layers, TerrainType type) {
        return tile_terrain_layers &~ type.mask;
    }
}
