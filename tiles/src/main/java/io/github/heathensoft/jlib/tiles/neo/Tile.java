package io.github.heathensoft.jlib.tiles.neo;

/**
 *
 * TILE DATA (READ / WRITE)
 *
 * 12-bit: unused               Offset: 0
 * 4-bit: block-type            Offset: 12 (16 types)
 * 4-bit: block-variation       Offset: 16
 * 4-bit: block-damage          Offset: 20 (visibly damaged: damage > 1)
 * 4-bit: terrain-layer-mask    Offset: 24
 * 2-bit: clearance-level       Offset: 28 (0 = no door, 1, 2, 3)
 * 1-bit: is-block              Offset: 30
 * 1-bit: is-obstacle           Offset: 31 (sign bit)
 *
 * ROOM DATA (READ ONLY)
 *
 * 8 bit room_local_id
 * 8 bit room_chunk_x_coord
 * 8 bit room_chunk_y_coord
 * 5 bit room_unused
 * 2 bit room_clearance_level (carries over from tile data)
 * 1 bit room_obstacle_bit (carries over from tile data)
 *
 * @author Frederik Dahl
 * 16/06/2023
 */


public class Tile {

    public static final int[][] adjacent8 = {
            {-1, 1},{ 0, 1},{ 1, 1},
            {-1, 0}        ,{ 1, 0},
            {-1,-1},{ 0,-1},{ 1,-1}
    };

    public static final byte[] map_mask_to_local_uv_index = {
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

    public static int tile_block_type(int tile) {
        return (tile >> 12) & 0x0F;
    }

    public static int tile_block_variant(int tile) {
        return (tile >> 16) & 0x0F;
    }

    public static int tile_block_damage(int tile) {
        return (tile >> 20) & 0x0F;
    }

    public static int tile_terrain_layer_mask(int tile) {
        return (tile >> 24) & 0x0F;
    }

    public static int tile_clearance_level(int tile) {
        return (tile >> 28) & 0b11;
    }

    public static int tile_block_bit(int tile) {
        return (tile >> 30) & 0b01;
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

    public static boolean tile_is_block(int tile) {
        return tile_block_bit(tile) == 1;
    }

    public static boolean tile_is_visibly_damaged(int tile) {
        return tile_block_damage(tile) > 7;
    }

    public static TerrainType tile_terrain_type(int tile) { // Top layer
        return TerrainType.top_layer(tile_terrain_layer_mask(tile));
    }

    public static int tile_block_uv_index(int tile, int mask) {
        int block_type = tile_block_type(tile);
        int x = (block_type % 8) * 8;
        int y = (block_type / 8) * 8;
        if (mask == 255) {
            int variant = tile_block_variant(tile);
            x += (variant % 8);
            y += (6 + (variant / 8));
        } else {
            int local_uv_index = map_mask_to_local_uv_index[mask & 0xFF];
            x += (local_uv_index % 8);
            y += (local_uv_index / 8);
        } return (y * 32 + x);
    }

    public static int tile_set_block_type(int tile, int type) {
        return (tile &~ 0xF000) | ((type & 0x0F) << 12);
    }

    public static int tile_set_block_variant(int tile, int sub_type) {
        return (tile &~ 0xF_0000) | ((sub_type & 0x0F) << 16);
    }

    public static int tile_set_block_damage(int tile, int damage) {
        return (tile &~ 0xF0_0000) | ((damage & 0x0F) << 20);
    }

    public static int tile_set_terrain_layer_mask(int tile, int terrain_mask) {
        return (tile &~ 0x0F00_0000) | ((terrain_mask & 0x0F) << 24);
    }

    public static int tile_set_clearance_level(int tile, int clearance) {
        return (tile &~ 0x3000_0000) | ((clearance & 0b11) << 28);
    }

    public static int tile_set_block_bit(int tile, int bit) {
        return (tile &~ 0x4000_0000) | ((bit & 0b01) << 30);
    }

    public static int tile_set_block_bit(int tile, boolean bit) {
        return (tile &~ 0x4000_0000) | ((bit ? 1 : 0) << 30);
    }

    public static int tile_set_obstacle_bit(int tile, int bit) {
        return (tile &~ 0x8000_0000) | ((bit & 0b01) << 31);
    }

    public static int tile_set_obstacle_bit(int tile, boolean bit) {
        return (tile &~ 0x8000_0000) | ((bit ? 1 : 0) << 31);
    }

    public static int tile_terrain_add_type(int tile, TerrainType type) {
        if (type == TerrainType.T0) return tile;
        int terrain_mask = tile_terrain_layer_mask(tile);
        terrain_mask = TerrainType.place_terrain_type(terrain_mask,type);
        return tile_set_terrain_layer_mask(tile,terrain_mask);
    }

    public static int tile_terrain_remove_type(int tile, TerrainType type) {
        if (type == TerrainType.T0) return tile;
        int terrain_mask = tile_terrain_layer_mask(tile);
        terrain_mask = TerrainType.remove_terrain_type(terrain_mask,type);
        return tile_set_terrain_layer_mask(tile,terrain_mask);
    }

    public static int tile_terrain_remove_top_layer(int tile) {
        int terrain_mask = tile_terrain_layer_mask(tile);
        if (terrain_mask == TerrainType.T0.mask) return tile;
        else { TerrainType top_layer = TerrainType.top_layer(terrain_mask);
            terrain_mask = TerrainType.remove_terrain_type(terrain_mask,top_layer);
            return tile_set_terrain_layer_mask(tile,terrain_mask);
        }
    }

    public static int tile_terrain_clear(int tile) { // Sets the terrain to T0 (0x0000)
        return tile_set_terrain_layer_mask(tile,0);
    }

    public static boolean tile_terrain_contains_type(int tile, TerrainType type) { // can't see any use for this
        return TerrainType.contains(tile_terrain_layer_mask(tile),type); // T0 should always be true
    }

    public static void room_out(int room) {
        String tab1 = "\t";
        System.out.println("Room {");
        System.out.println(tab1 + "Local-id: " + room_local_id(room));
        System.out.println(tab1 + "Clearance: " + room_clearance_level(room));
        System.out.println(tab1 + "Obstacle: " + room_is_obstacle(room));
        System.out.println(tab1 + "Chunk-x: " + room_chunk_x(room));
        System.out.println(tab1 + "Chunk-y: " + room_chunk_y(room));
        System.out.println("}");
    }

    public static void tile_out(int tile) {
        int block_damage = tile_block_damage(tile);
        int terrain_layer_mask = tile_terrain_layer_mask(tile);
        String tab1 = "\t";
        String tab2 = tab1 + tab1;
        System.out.println("Tile {");
        System.out.println(tab1 + "Block: " + tile_is_block(tile));
        System.out.println(tab1 + "Obstacle: " + tile_is_obstacle(tile));
        System.out.println(tab1 + "Door: " + tile_is_door(tile));
        System.out.println(tab1 + "Clearance: " + tile_clearance_level(tile));
        System.out.println(tab1 + "Block {");
        System.out.println(tab2 + "Type: " + tile_block_type(tile));
        System.out.println(tab2 + "Variant: " + tile_block_variant(tile));
        System.out.println(tab2 + "Damage: " + block_damage);
        System.out.println(tab2 + "Visibly Damaged: " + (block_damage > 7));
        System.out.println(tab1 + "}");
        System.out.println(tab1 + "Terrain {");
        System.out.println(tab2 + "Top-layer (Type): " + TerrainType.top_layer(terrain_layer_mask).name());
        System.out.println(tab2 + "Layer-mask: 0b" + Integer.toBinaryString(terrain_layer_mask));
        System.out.println(tab2 + "ABGR4: 0x" + Integer.toHexString(0xFFFF & (TerrainType.top_layer(terrain_layer_mask).abgr4)));
        System.out.println(tab1 + "}");
        System.out.println("}");
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

}
