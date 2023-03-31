package io.github.heathensoft.jlib.tiles.neo;

/**
 *
 * TILE (READ / WRITE)
 *
 * 8 bit tile_block_mask
 * 4 bit tile_block_type
 * 1 bit tile_block_sub_type
 * 2 bit tile_block_damage
 * 1 bit tile_has_block
 * 4 bit tile_terrain_layers
 * 4 bit tile_terrain_layer_variation
 * 5 bit tile_unused
 * 2 bit tile_clearance_level (0 = no door, 1, 2, 3)
 * 1 bit tile_is_obstacle (sign bit)
 *
 * ROOM (READ)
 *
 * 8 bit room_local_id
 * 8 bit room_chunk_x_coord
 * 8 bit room_chunk_y_coord
 * 5 bit room_unused
 * 2 bit room_clearance_level (carries over from tile data)
 * 1 bit room_obstacle_bit (carries over from tile data)
 *
 * @author Frederik Dahl
 * 24/03/2023
 */


public class Tile {



    public static int tile_block_mask(int tile) {
        return (tile & 0xFF);
    }

    public static int tile_block_type(int tile) {
        return (tile >> 8) & 0x0F;
    }

    public static int tile_block_sub_type(int tile) {
        return (tile >> 12) & 0b01;
    }

    public static int tile_block_damage(int tile) {
        return (tile >> 13) & 0b11;
    }

    public static int tile_block_bit(int tile) {
        return (tile >> 15) & 0b01;
    }

    public static int tile_terrain_layers(int tile) {
        return (tile >> 16) & 0x0F;
    }

    public static int tile_terrain_layer_variation(int tile) {
        return (tile >> 20) & 0x0F;
    }

    public static int tile_clearance_level(int tile) {
        return (tile >> 29) & 0b11;
    }

    public static int tile_obstacle_bit(int tile) {
        return (tile >> 31) & 0b01;
    }

    public static boolean tile_is_door(int tile) {
        return tile_clearance_level(tile) > 0;
    }

    public static boolean tile_is_obstacle(int tile) {
        return tile < 0;
    }

    public static boolean tile_not_obstacle(int tile) {
        return tile >= 0;
    }

    public static int tile_block_uv_index(int tile) {
        int mask = tile_block_mask(tile);
        int variant = tile_block_sub_type(tile);
        int index = variant == 0 ? 0 : 32;
        int corner = (mask >> 4) & 0x0F;
        int cardinal = mask & 0x0F;
        index += cardinal == 0x0F ? (16 + corner) : cardinal;
        return (index + (64 * tile_block_type(tile)));
    }

    public static int tile_set_block_mask(int tile, int mask) {
        return (tile &~ 0xFF) | (mask & 0xFF);
    }

    public static int tile_set_block_type(int tile, int type) {
        return (tile &~ 0x0F00) | ((type & 0x0F) << 8);
    }

    public static int tile_set_block_sub_type(int tile, int sub_type) {
        return (tile &~ 0x1000) | ((sub_type & 0b01) << 12);
    }

    public static int tile_set_block_damage(int tile, int damage) {
        return (tile &~ 0x6000) | ((damage & 0b11) << 13);
    }

    public static int tile_set_block_bit(int tile, int bit) {
        return (tile &~ 0x8000) | ((bit & 0b01) << 15);
    }

    public static int tile_set_terrain_layers(int tile, int layers) {
        return (tile &~ 0x000F_0000) | ((layers & 0x0F) << 16);
    }

    public static int tile_set_terrain_layer_variation(int tile, int variation) {
        return (tile &~ 0x00F0_0000) | ((variation & 0x0F) << 20);
    }

    public static int tile_set_clearance_level(int tile, int clearance) {
        return (tile &~ 0x0300_0000) | ((clearance & 0b11) << 29);
    }

    public static int tile_set_obstacle_bit(int tile, int obstacle) {
        return (tile &~ 0x8000_0000) | ((obstacle & 0b01) << 31);
    }

    public static int room_create(int local_id, int chunk_x, int chunk_y, int clearance) {
        local_id = (local_id & 0xFF);
        chunk_x = (chunk_x & 0xFF) << 8;
        chunk_y = (chunk_y & 0xFF) << 16;
        clearance = (clearance & 0b11) << 29;
        return local_id | chunk_x | chunk_y | clearance;
    }

    public static int room_local_id(int room) {
        return room & 0xFF;
    }

    public static int room_chunk_x(int room) {
        return (room >> 8) & 0xFF;
    }

    public static int room_chunk_y(int room) {
        return (room >> 16) & 0xFF;
    }

    public static short room_chunk_id(int room) {
        return (short) ((room & 0xFFFF00) >> 8);
    }

    public static int room_clearance_level(int room) {
        return (room >> 29) & 0b11;
    }

    public static int room_obstacle_bit(int room) {
        return (room >> 31) & 0b01;
    }

    public static int room_obstacle() {
        return -1;
    }

    public static boolean room_is_obstacle(int room) {
        return room < 0;
    }

    public static boolean room_not_obstacle(int room) {
        return room >= 0;
    }

    public static boolean room_occupy_same_chunk(int room1, int room2) {
        return (room1 & 0x00FFFF00) == (room2 & 0x00FFFF00);
    }

    public static long room_connection_key(long room1, long room2) {
        return room1 > room2 ? (room1 | (room2 << 32)) : (room2 | (room1 << 32));
    }

    public static int nextPowerOfTwo(int value) {
        if (value-- == 0) return 1;
        value |= value >>> 1;
        value |= value >>> 2;
        value |= value >>> 4;
        value |= value >>> 8;
        value |= value >>> 16;
        return value + 1;
    }

    public static final int[][] adjacent4 = new int[][] {
            {-1, 0},{ 0,-1},{ 0, 1},{ 1, 0}
    };

    public static final int[][] adjacent8 = {
            {-1, 1},{ 0, 1},{ 1, 1},
            {-1, 0}        ,{ 1, 0},
            {-1,-1},{ 0,-1},{ 1,-1}
    };

    private static final byte[] TERRAIN_MASK_TO_INDEX = {

            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            13, 13, 14, 14, 13, 13, 14, 14, 15, 15, 16, 17, 15, 15, 16, 17,
            18, 18, 19, 19, 18, 18, 20, 20, 21, 21, 22, 23, 21, 21, 24, 25,
            13, 13, 14, 14, 13, 13, 14, 14, 26, 26, 27, 28, 26, 26, 27, 28,
            18, 18, 19, 19, 18, 18, 20, 20, 29, 29, 30, 31, 29, 29, 32, 33,
            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            13, 13, 14, 14, 13, 13, 14, 14, 15, 15, 16, 17, 15, 15, 16, 17,
            34, 34, 35, 35, 34, 34, 36, 36, 37, 37, 38, 39, 37, 37, 40, 41,
            13, 13, 14, 14, 13, 13, 14, 14, 26, 26, 27, 28, 26, 26, 27, 28,
            34, 34, 35, 35, 34, 34, 36, 36, 42, 42, 43, 44, 42, 42, 45, 46
    };

    public static int terrain_mask_to_uv(int mask) {
        return TERRAIN_MASK_TO_INDEX[mask & 0xFF];
    }

}
