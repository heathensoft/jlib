package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.ai.pathfinding.AStarGrid;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.primitives.Rectanglef;
import org.joml.primitives.Rectanglei;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.tiles.neo.Tile.*;

/**
 *
 * NOT THREAD SAFE. ONLY WRITE TO TILEMAP FROM THE MAIN THREAD (ONE CORE THREAD). BUT READ FROM ANY (NOT SYNCHRONIZED)
 *
 *
 * NOTE: The whole Tile System is dependent on the size of a Chunk being 16x16 tiles ( 4 + 4 bits). This is tied to the data
 * structure itself. I.e. room-id, vertex data etc. It can not be otherwise. Else it won't fit the 32-bit integer.
 * Throughout the code when you see 16 and 256 (16 x 16), that's what that is. 16 is permanent and will not change in the future.
 * For the same reason, the map size cannot exceed 1024 x 1024 tiles.
 *
 *
 * @author Frederik Dahl
 * 20/06/2023
 */

/* TODO: when mixing terrain: noise lerp with the layer below */
/* TODO: on region generation, use modulo to wrap the map */

public class TileMap implements AStarGrid, Disposable {

    private final Rectanglei view_last_update;
    private final MapSize map_size;
    private final ByteBuffer chunks_in_view;
    private final ByteBuffer chunks_layout_queue;
    private final BitSet chunk_in_view_flags;
    private final BitSet chunk_update_layout_set;
    private final BitSet chunk_update_blocks_set;
    private final BitSet chunk_update_terrain_set;
    private final Terrain terrain;
    private final Network network;
    private final Chunk[][] chunks;
    private final int[][] room_layout;
    private final int[][] tiles;


    public TileMap(MapSize map_size, int terrain_texture_size) {
        this.map_size = map_size;
        this.terrain = new Terrain(map_size,terrain_texture_size);
        this.view_last_update = new Rectanglei();
        this.network = new Network(map_size);
        this.tiles = new int[rows()][cols()];
        this.room_layout = new int[rows()][cols()];
        this.chunks_in_view = ByteBuffer.allocate(map_size.chunks_count * 2);
        this.chunks_layout_queue = ByteBuffer.allocate(map_size.chunks_count * 2);
        this.chunk_in_view_flags = new BitSet(map_size.chunks_count);
        this.chunk_update_layout_set = new BitSet(map_size.chunks_count);
        this.chunk_update_blocks_set = new BitSet(map_size.chunks_count);
        this.chunk_update_terrain_set = new BitSet(map_size.chunks_count);
        int chunk_rows = rows() / 16;
        int chunk_cols = cols() / 16;
        this.chunks = new Chunk[chunk_rows][chunk_cols];
        for (int r = 0; r < chunk_rows; r++) {
            for (int c = 0; c < chunk_cols; c++) {
                chunks[r][c] = new Chunk();
            }
        }
    }

    // REMEMBER TO SET UP TERRAIN
    public void initialize() {
        int chunk_rows = rows() / 16;
        int chunk_cols = cols() / 16;
        for (int r = 0; r < chunk_rows; r++) {
            for (int c = 0; c < chunk_cols; c++) {
                Chunk chunk = chunks[r][c];
                chunk.update_blocks(this,c,r);
                // chunk.update_layout(this,c,r);
                chunk.update_terrain(this,c,r);
            }
        } terrain.blend_map().generateMipmap();
        chunks_layout_queue.clear();
        chunks_in_view.clear();
        chunk_in_view_flags.clear();
        chunk_update_blocks_set.clear();
        chunk_update_layout_set.clear();
        chunk_update_terrain_set.clear();
        view_last_update.minX = -1;
        view_last_update.minY = -1;
        view_last_update.maxX = -1;
        view_last_update.maxY = -1;
    }

    public void refresh(Rectanglef orthographic_view) {

        // UPDATE LAYOUT (EVEN IF NOT IN VIEW)
        if (chunks_layout_queue.position() != 0) {
            chunks_layout_queue.flip();
            while (chunks_layout_queue.hasRemaining()) {
                int chunk_x = chunks_layout_queue.get();
                int chunk_y = chunks_layout_queue.get();
                //chunks[chunk_y][chunk_x].update_layout(this,chunk_x,chunk_y);
                chunk_update_layout_set.clearUnchecked(chunk_x + chunk_y * map_size.length_chunks);
            } chunks_layout_queue.clear();
        }

        { // UPDATE CHUNKS IN VIEW (IF VIEW HAS CHANGED)
            Rectanglei view = MathLib.recti(
                    floor(orthographic_view.minX), floor(orthographic_view.minY),
                    ceil(orthographic_view.maxX), ceil(orthographic_view.maxY));

            if (!view.equals(view_last_update)) {
                chunk_in_view_flags.clear();
                chunks_in_view.clear();
                query_chunks_in_view(chunks_in_view,view);
                view_last_update.set(view); // Update previous view
            }
        }

        // UPDATE TERRAIN AND BLOCKS ONLY IF THE CHUNK IS IN VIEW
        int in_view_position = chunks_in_view.position();
        boolean regenerate_blend_map_mipmap = false;
        if (in_view_position != 0) { // IF ANY CHUNKS ARE VISIBLE
            for (int i = 0; i < in_view_position; i += 2) { // BUFFER POSITION REMAINS THE SAME
                int chunk_x = chunks_in_view.get(i);
                int chunk_y = chunks_in_view.get(i+1);
                int chunk_index = chunk_x + chunk_y * map_size.length_chunks;

                // UPDATE TERRAIN IF QUEUED FOR UPDATE
                if (chunk_update_terrain_set.getUnchecked(chunk_index)) {
                    regenerate_blend_map_mipmap = true;
                    chunks[chunk_y][chunk_x].update_terrain(this,chunk_x,chunk_y);
                    chunk_update_terrain_set.clearUnchecked(chunk_index);
                }
                // UPDATE BLOCKS IF QUEUED FOR UPDATE
                if (chunk_update_blocks_set.getUnchecked(chunk_index)) {
                    chunks[chunk_y][chunk_x].update_blocks(this,chunk_x,chunk_y);
                    chunk_update_blocks_set.clearUnchecked(chunk_index);
                }
            }
        }

        // REGENERATE BLEND MAP MIP MAP
        if (regenerate_blend_map_mipmap) {
            terrain.blend_map().generateMipmap();
        }
    }

    public void draw() { // Own draw method for room view
        int in_view_position = chunks_in_view.position();
        if (in_view_position != 0) { // IF ANY CHUNKS ARE VISIBLE
            for (int i = 0; i < in_view_position; i += 2) { // BUFFER POSITION REMAINS THE SAME
                int chunk_x = chunks_in_view.get(i);
                int chunk_y = chunks_in_view.get(i+1);
                //if (chunk_x == 0 && chunk_y == 0)
                    chunks[chunk_y][chunk_x].draw_tiles();
            }
        }
    }

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

    public int[][] layout() {
        return room_layout;
    }

    public int[][] tiles() {
        return tiles;
    }

    public void rooms_of_chunk(IntBuffer dst, int chunk_x, int chunk_y) {
        chunks[chunk_y][chunk_x].getRooms(dst);
    }

    public void set_tile(int tile, int x, int y) {
        tiles[y][x] = tile;
        int chunk_x = x/16;
        int chunk_y = y/16;
        queue_chunk_update_blocks(chunk_x,chunk_y);
        queue_chunk_update_layout(chunk_x,chunk_y);
        queue_chunk_update_terrain(chunk_x,chunk_y);
    }

    public void set_block_type(int block_type, int x, int y) {
        int tile = tiles[y][x];
        tiles[y][x] = tile_set_block_type(tile,block_type);
        if (tile_is_block(tile)) queue_chunk_update_blocks(x/16,y/16);
    }

    public void set_block_subtype(int sub_type, int x, int y) {
        int tile = tiles[y][x];
        tiles[y][x] = tile_set_block_subtype(tile,sub_type);
        if (tile_is_block(tile)) queue_chunk_update_blocks(x/16,y/16);
    }

    public void set_block_sprite_variant(int variant, int x, int y) {
        int tile = tiles[y][x];
        tiles[y][x] = tile_set_block_sprite_variant(tile,variant);
        if (tile_is_block(tile)) queue_chunk_update_blocks(x/16,y/16);
    }

    public void set_clearance_level(int clearance, int x, int y) {
        int tile = tiles[y][x];
        clearance = clearance & 0b11;
        int current_clearance = tile_clearance_level(tile);
        if (clearance == current_clearance) return;
        tiles[y][x] = tile_set_clearance_level(tile,clearance);
        queue_chunk_update_layout(x/16,y/16);
    }

    public void place_terrain_layer(TerrainType type, int x, int y) {
        int tile = tiles[y][x];
        TerrainType current_top_layer = tile_terrain_top_layer(tile);
        if (type.id > current_top_layer.id) {
            queue_chunk_update_terrain(x/16,y/16);
        } tiles[y][x] = tile_terrain_add_layer(tile,type);
    }

    public void remove_terrain_layer_top(int x, int y) {
        int tile = tiles[y][x];
        TerrainType top_layer = tile_terrain_top_layer(tile);
        if (top_layer == TerrainType.T0) return;
        queue_chunk_update_terrain(x/16,y/16);
        tiles[y][x] = tile_terrain_remove_layer(tile,top_layer);
    }

    public void remove_terrain_layer(TerrainType type, int x, int y) {
        if (type == TerrainType.T0) return;
        int tile = tiles[y][x];
        TerrainType current_top_layer = tile_terrain_top_layer(tile);
        if (type == tile_terrain_top_layer(tile)) {
            queue_chunk_update_terrain(x/16,y/16);
        } tiles[y][x] = tile_terrain_remove_layer(tile,type);
    }

    public void clear_terrain(int x, int y) {
        int tile = tiles[y][x];
        if (tile_terrain_top_layer(tile) == TerrainType.T0) return;
        tiles[y][x] = tile_terrain_clear(tile);
        queue_chunk_update_terrain(x/16,y/16);
    }

    public void place_block(int type, int subtype, int x, int y) {
        int tile = tiles[y][x];
        tile = tile_set_block_bit(tile,true);
        tile = tile_set_block_type(tile,type);
        tile = tile_set_block_subtype(tile,subtype);
        tiles[y][x] = tile_set_block_damage(tile,0);
        queue_chunk_update_blocks(x/16,y/16);
    }

    public void remove_block(int x, int y) {
        int tile = tiles[y][x];
        if (tile_is_block(tile)) {
            tiles[y][x] = tile_set_block_bit(tile,false);
            queue_chunk_update_blocks(x/16,y/16);
        }
    }

    public void place_obstacle(int x, int y) {
        int tile = tiles[y][x];
        if (tile_is_obstacle(tile)) return;
        tiles[y][x] = tile_set_obstacle_bit(tile,true);
        queue_chunk_update_layout(x/16,y/16);
    }

    public void remove_obstacle(int x, int y) {
        int tile = tiles[y][x];
        if (tile_is_obstacle(tile)) {
            tiles[y][x] = tile_set_obstacle_bit(tile,false);
            queue_chunk_update_layout(x/16,y/16);
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
                    queue_chunk_update_blocks(x/16,y/16);
                    return true;
                } else  {
                    tiles[y][x] = tile_set_block_damage(tile,damage);
                    queue_chunk_update_blocks(x/16,y/16);
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
                    queue_chunk_update_blocks(x/16,y/16);
                }
            }
        }
    }


    private void query_chunks_in_view(ByteBuffer dst, Rectanglei view) {
        int size = map_size.length_tiles;
        Rectanglei quad = MathLib.recti(0,0,size,size);
        if (view.intersectsRectangle(quad)) query_chunks_in_view(dst,view,quad);
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

    private void queue_chunk_update_blocks(int chunk_x, int chunk_y) {
        chunk_update_blocks_set.setUnchecked(chunk_x + chunk_y * map_size.length_chunks);
    }

    private void queue_chunk_update_terrain(int chunk_x, int chunk_y) {
        chunk_update_terrain_set.setUnchecked(chunk_x + chunk_y * map_size.length_chunks);
    }

    private void queue_chunk_update_layout(int chunk_x, int chunk_y) {
        int index = chunk_x + chunk_y * map_size.length_chunks;
        if (!chunk_update_layout_set.getUnchecked(index)) {
            chunk_update_terrain_set.setUnchecked(index);
            chunks_layout_queue.put((byte) chunk_x).put((byte) chunk_y);
        }

    }

    public void dispose() {
        for (Chunk[] chunk_row : chunks) {
            for (Chunk chunk : chunk_row)
                Disposable.dispose(chunk);
        } Disposable.dispose(terrain);
    }
}
