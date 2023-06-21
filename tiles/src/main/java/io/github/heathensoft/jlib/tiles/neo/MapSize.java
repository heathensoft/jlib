package io.github.heathensoft.jlib.tiles.neo;

/**
 * @author Frederik Dahl
 * 25/03/2023
 */


public enum MapSize {

    TINY(64,0,"Tiny"),        // 64 x 64
    SMALL(128,1,"Small"),     // 128 x 128
    MEDIUM(256,2,"Medium"),   // 256 x 256
    LARGE(512,3,"Large"),     // 512 x 512
    HUGE(1024,4,"Huge");      // 1024 x 1024

    public static final String DESCRIPTOR = "Map Size";
    public static final MapSize[] ALL = values();
    public static final int COUNT = ALL.length;

    public final String descriptor;
    public final int length_tiles;
    public final int length_chunks;
    public final int tiles_count;
    public final int chunks_count;
    public final int id;

    MapSize(int length_tiles, int id, String descriptor) {
        this.length_tiles = length_tiles;
        this.length_chunks = length_tiles / 16;
        this.tiles_count = length_tiles * length_tiles;
        this.chunks_count = length_chunks * length_chunks;
        this.descriptor = descriptor;
        this.id = id;
    }

    public static MapSize get(int id) {
        return ALL[id % COUNT];
    }

    public MapSize next() {
        return get(id + 1);
    }

    public MapSize prev() {
        return id == 0 ? ALL[(COUNT - 1)] : ALL[id - 1];
    }
}
