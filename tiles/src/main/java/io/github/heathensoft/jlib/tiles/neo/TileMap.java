package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.ai.pathfinding.AStarGrid;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.primitives.Rectanglef;
import org.joml.primitives.Rectanglei;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static io.github.heathensoft.jlib.common.utils.U.ceil;
import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.tiles.neo.Tile.*;

/**
 *
 * @author Frederik Dahl
 * 20/06/2023
 */

/* TODO: when mixing terrain: noise lerp with the layer below */
/* TODO: on region generation, use modulo to wrap the map */

public abstract class TileMap implements AStarGrid, Disposable {

    private static final int TM_LAYOUT = 0x01;
    private static final int TM_BLOCKS = 0x02;
    private static final int TM_TERRAIN = 0x04;

    private MapSize map_size;
    private ByteBuffer chunks_in_view;
    private BitSet chunk_in_view_flags;
    private BitSet chunk_update_layout_flags;
    private BitSet chunk_update_blocks_flags;
    private BitSet chunk_update_terrain_flags;
    private Terrain terrain;
    private Network network;
    private Chunk[][] chunks;
    private int[][] room_layout;
    private int[][] tiles;


    public Network network() {
        return network;
    }

    public Terrain terrain() {
        return terrain;
    }

    public Tile tile_wrapper(int x, int y) {
        return new Tile(x,y,this);
    }

    public int tile_data(int x, int y) {
        return tiles[y][x];
    }

    public int room_id(int x, int y) {
        return room_layout[y][x];
    }

    public int rows() {
        return map_size.length_tiles;
    }

    public int cols() {
        return map_size.length_tiles;
    }

    public int movementPenalty(int x, int y) {
        return tile_terrain_top_layer(tiles[y][x]).movement_penalty;
    }

    public boolean isObstacle(int x, int y) {
        return tile_is_obstacle(tile_data(x,y));
    }

    public boolean contains(int x, int y) {
        return x >= 0 && x < cols() && y >= 0 && y < rows();
    }

    public boolean is_tile_in_view(int x, int y) {
        return is_chunk_in_view(x / 16, y / 16);
    }

    public boolean is_chunk_in_view(int x, int y) {
        int index = x + y * map_size.length_chunks;
        return chunk_in_view_flags.getUnchecked(index);
    }

    public boolean contains_terrain_layer(TerrainType type, int x, int y) {
        return tile_terrain_contains_layer(tile_data(x,y),type);
    }

    public MapSize map_size() {
        return map_size;
    }

    public ByteBuffer chunks_in_view() {
        return chunks_in_view;
    }

    public void rooms_of_chunk(IntBuffer dst, int chunk_x, int chunk_y) {
        chunks[chunk_y][chunk_x].getRooms(dst);
    }

    public void set_tile(int tile, int x, int y) {
        tiles[y][x] = tile;
        queue_chunk_update(TM_BLOCKS | TM_LAYOUT | TM_TERRAIN,x/16,y/16);
    }

    public void set_block_type(int block_type, int x, int y) {
        int tile = tiles[y][x];
        tiles[y][x] = tile_set_block_type(tile,block_type);
        if (tile_is_block(tile)) queue_chunk_update(TM_BLOCKS,x/16,y/16);
    }

    public void set_block_subtype(int sub_type, int x, int y) {
        int tile = tiles[y][x];
        tiles[y][x] = tile_set_block_subtype(tile,sub_type);
        if (tile_is_block(tile)) queue_chunk_update(TM_BLOCKS,x/16,y/16);
    }

    public void set_block_sprite_variant(int variant, int x, int y) {
        int tile = tiles[y][x];
        tiles[y][x] = tile_set_block_sprite_variant(tile,variant);
        if (tile_is_block(tile)) queue_chunk_update(TM_BLOCKS,x/16,y/16);
    }

    public void set_clearance_level(int clearance, int x, int y) {
        int tile = tiles[y][x];
        clearance = clearance & 0b11;
        int current_clearance = tile_clearance_level(tile);
        if (clearance == current_clearance) return;
        tiles[y][x] = tile_set_clearance_level(tile,clearance);
        queue_chunk_update(TM_LAYOUT,x/16,y/16);
    }

    public void place_terrain_layer(TerrainType type, int x, int y) {
        int tile = tiles[y][x];
        TerrainType current_top_layer = tile_terrain_top_layer(tile);
        if (type.id > current_top_layer.id) {
            queue_chunk_update(TM_TERRAIN,x/16,y/16);
        } tiles[y][x] = tile_terrain_add_layer(tile,type);
    }

    public void remove_terrain_layer(TerrainType type, int x, int y) {
        if (type == TerrainType.T0) return;
        int tile = tiles[y][x];
        TerrainType current_top_layer = tile_terrain_top_layer(tile);
        if (type == tile_terrain_top_layer(tile)) {
            queue_chunk_update(TM_TERRAIN,x/16,y/16);
        } tiles[y][x] = tile_terrain_remove_layer(tile,type);
    }

    public void clear_terrain(int x, int y) {
        int tile = tiles[y][x];
        if (tile_terrain_top_layer(tile) == TerrainType.T0) return;
        tiles[y][x] = tile_terrain_clear(tile);
        queue_chunk_update(TM_TERRAIN,x/16,y/16);
    }

    public void place_block(int type, int subtype, int x, int y) {
        int tile = tiles[y][x];
        tile = tile_set_block_type(tile,type);
        tile = tile_set_block_subtype(tile,subtype);
        tiles[y][x] = tile_set_block_damage(tile,0);
        queue_chunk_update(TM_BLOCKS, x/16, y/16);
    }

    public void remove_block(int x, int y) {
        int tile = tiles[y][x];
        if (tile_is_block(tile)) {
            tiles[y][x] = tile_set_block_bit(tile,false);
            queue_chunk_update(TM_BLOCKS, x/16, y/16);
        }
    }

    public void place_obstacle(int x, int y) {
        int tile = tiles[y][x];
        if (tile_is_obstacle(tile)) return;
        tiles[y][x] = tile_set_obstacle_bit(tile,true);
        queue_chunk_update(TM_LAYOUT,x/16,y/16);
    }

    public void remove_obstacle(int x, int y) {
        int tile = tiles[y][x];
        if (tile_is_obstacle(tile)) {
            tiles[y][x] = tile_set_obstacle_bit(tile,false);
            queue_chunk_update(TM_LAYOUT,x/16,y/16);
        }
    }

    public boolean damage_block(int amount, int x, int y) {
        // returns true if a block is destroyed
        // does nothing if no block is present or amount <= 0
        // if a block is destroyed, it is no longer a block
        // Damage is also reset to 0. Block type is not changed, so if there is a drop of material
        // the block type can still be queried.
        // If the block was an obstacle, it is still an obstacle.
        // You have to remove the obstacle bit explicitly
        if (amount > 0) {
            int tile = tiles[y][x];
            if (tile_is_block(tile)) {
                int damage = clamp(tile_block_damage(tile) + amount,0,15);
                if (damage == 15) {
                    tile = tile_set_block_damage(tile,0);
                    tiles[y][x] = tile_set_block_bit(tile,false);
                    queue_chunk_update(TM_BLOCKS, x/16, y/16);
                    return true;
                } else  {
                    tiles[y][x] = tile_set_block_damage(tile,damage);
                    queue_chunk_update(TM_BLOCKS, x/16, y/16);
                }
            }
        } return false;
    }

    public void repair_block(int amount, int x, int y) {
        if (amount > 0) {
            int tile = tiles[y][x];
            if (tile_is_block(tile)) {
                int damage_before = tile_block_damage(tile);
                if (damage_before > 0) {
                    int damage_after = damage_before - amount;
                    damage_after = Math.max(damage_after, 0);
                    tiles[y][x] = tile_set_block_damage(tile,damage_after);
                    queue_chunk_update(TM_BLOCKS,x/16,y/16);
                }
            }
        }
    }


    private void query_chunks_in_view(ByteBuffer dst, Rectanglef view) {
        // Clear the flags before the update
        chunk_in_view_flags.clear();
        int size = map_size.length_tiles;
        Rectanglei view_i = MathLib.recti(
                (int) view.minX, (int) view.minY,
                ceil(view.maxX), ceil(view.maxY));
        Rectanglei quad_i = MathLib.recti(0,0,size,size);
        if (view_i.intersectsRectangle(quad_i)) query_chunks_in_view(dst,view_i,quad_i);
    }

    private void query_chunks_in_view(ByteBuffer dst, Rectanglei view, Rectanglei tmp) {
        final int x = tmp.minX;
        final int y = tmp.minY;
        int s = tmp.lengthX();
        if (s == 16) {
            byte cx = (byte)(x/16);
            byte cy = (byte)(y/16);
            dst.put(cx).put(cy);
            chunk_in_view_flags.setUnchecked(cx + cy * map_size.length_chunks);
        } else { s /= 2;
            tmp.setMax(tmp.minX + s,tmp.minY + s);
            if (view.intersectsRectangle(tmp)) query_chunks_in_view(dst,view,tmp);
            tmp.setMin(x + s,y);
            tmp.setMax(tmp.minX + s,tmp.minY + s);
            if (view.intersectsRectangle(tmp)) query_chunks_in_view(dst,view,tmp);
            tmp.setMin(x ,y + s);
            tmp.setMax(tmp.minX + s,tmp.minY + s);
            if (view.intersectsRectangle(tmp)) query_chunks_in_view(dst,view,tmp);
            tmp.setMin(x + s,y + s);
            tmp.setMax(tmp.minX + s,tmp.minY + s);
            if (view.intersectsRectangle(tmp)) query_chunks_in_view(dst,view,tmp);
        }
    }

    private boolean is_queued_for_update(int flag, int x, int y) {
        switch (flag) {
            case TM_LAYOUT  -> { return chunk_update_layout_flags.getUnchecked(x + (y / cols())); }
            case TM_BLOCKS  -> { return chunk_update_blocks_flags.getUnchecked(x + (y / cols())); }
            case TM_TERRAIN -> { return chunk_update_terrain_flags.getUnchecked(x + (y / cols())); }
            default -> throw new IllegalStateException("Unexpected value: " + flag);
        }
    }

    private void queue_chunk_update(int flags, int x, int y) {
        if ((flags & TM_LAYOUT) > 0) chunk_update_layout_flags.setUnchecked(x + (y / cols()));
        if ((flags & TM_BLOCKS) > 0) chunk_update_blocks_flags.setUnchecked(x + (y / cols()));
        if ((flags & TM_TERRAIN) > 0) chunk_update_terrain_flags.clearUnchecked(x + (y / cols()));
    }


    public void dispose() {
        for (Chunk[] chunk_row : chunks) {
            Disposable.dispose(chunk_row);
        } Disposable.dispose(
                terrain
        );
    }
}
