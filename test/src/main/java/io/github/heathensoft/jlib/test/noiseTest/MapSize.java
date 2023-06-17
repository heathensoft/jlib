package io.github.heathensoft.jlib.test.noiseTest;

/**
 * @author Frederik Dahl
 * 25/03/2023
 */


public enum MapSize {

    TINY(64,"Tiny"),        // 64 x 64
    SMALL(128,"Small"),     // 128 x 128
    MEDIUM(256,"Medium"),   // 256 x 256
    LARGE(512,"Large"),     // 512 x 512
    HUGE(1024,"Huge");      // 1024 x 1024

    public final String descriptor;
    public final int tiles_across;
    public final int chunks_across;
    public final int tiles_count;
    public final int chunks_count;

    MapSize(int tiles_across, String descriptor) {
        this.tiles_across = tiles_across;
        this.chunks_across = tiles_across / 16;
        this.tiles_count = tiles_across * tiles_across;
        this.chunks_count = chunks_across * chunks_across;
        this.descriptor = descriptor;
    }
}
