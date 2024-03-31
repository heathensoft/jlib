package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.storage.primitive.IntStack;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.BufferObject;
import io.github.heathensoft.jlib.lwjgl.gfx.VertexAttributes;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static io.github.heathensoft.jlib.tiles.neo.Tile.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;

/**
 *
 * @author Frederik Dahl
 * 24/03/2023
 */


public class Chunk implements Disposable {

    private IntBuffer rooms;
    private final VertexAttributes gfx_tiles_vao;
    private final BufferObject gfx_tiles_vbo;


    public Chunk() {
        rooms = IntBuffer.allocate(4).flip(); // Not allocating max (256). Auto-grows if needed. (read-mode on init)
        gfx_tiles_vao = new VertexAttributes().bind();
        gfx_tiles_vbo = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW).bind();
        gfx_tiles_vbo.bufferData((long) 256 * Integer.BYTES);
        glVertexAttribIPointer(0,1,GL_UNSIGNED_INT,Integer.BYTES,0);
        glEnableVertexAttribArray(0);
    }

    public void update_blocks(Tilemap tilemap, int chunk_x, int chunk_y) {
        // When to update:
        // Block removed or added
        // Block type is changed
        // Block sub-type is changed
        // Block visibly damaged flag has changed
        // Triggers when the chunk is in view. No need to update a chunk before you can see it
        int[][] tile_data = tilemap.tiles();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(256);
            int lim = tile_data.length - 1;
            int chunk_origin_x = chunk_x * 16;
            int chunk_origin_y = chunk_y * 16;
            for (int r = 0; r < 16; r++) {
                int y = chunk_origin_y + r;
                for (int c = 0; c < 16; c++) {
                    int x = chunk_origin_x + c;
                    int tile = tile_data[y][x];
                    int block_uv_index = 0;
                    boolean is_block = tile_is_block(tile);
                    if (is_block) {
                        int type = tile_block_type(tile);
                        int mask = 0;
                        for (int i = 0; i < 8; i++) {
                            int[] offset = adjacent8[i];
                            int nx = offset[0] + x;
                            int ny = offset[1] + y;
                            if (nx < 0 || nx > lim || ny < 0 || ny > lim) {
                                mask += (1 << i);
                            } else {
                                int adj_tile = tile_data[ny][nx];
                                if (tile_is_block(adj_tile)) {
                                    if (tile_block_type(adj_tile) == type) {
                                        mask += (1 << i);
                                    }
                                }
                            }
                        }
                        block_uv_index = tile_block_uv_index(tile,mask);
                    }
                    // 6 bit chunk x
                    // 6 bit chunk y
                    // 12 bit uv index
                    // 4 bit damage
                    // 3 bit unused
                    // 1 bit block

                    int block_vertex_data = chunk_x & 0x3F;
                    block_vertex_data |= ((chunk_y & 0x3F) << 6);
                    block_vertex_data |= ((block_uv_index & 0xFFF) << 12);
                    block_vertex_data |= ((tile_block_damage(tile) & 0x0F) << 24);
                    block_vertex_data |= ((is_block ? 1 : 0) << 31);
                    buffer.put(block_vertex_data);
                }
            } gfx_tiles_vao.bind();
            gfx_tiles_vbo.bind().bufferData(buffer.flip());
        }
    }



    protected void update_terrain(Tilemap tilemap, int chunk_x, int chunk_y) {
        // When to update:
        // Terrain has been altered
        // Triggers when the chunk is in view
        // No need to update a chunk before you can see it

        // TODO:

        /*
        int[][] tile_data = tilemap.tiles();
        Texture blend_map = tilemap.terrain().blend_map();
        int chunk_origin_x = chunk_x * 16;
        int chunk_origin_y = chunk_y * 16;
        try (MemoryStack stack = MemoryStack.stackPush()){
            ShortBuffer buffer = stack.mallocShort(256);
            for (int r = 0; r < 16; r++) {
                int y = chunk_origin_y + r;
                for (int c = 0; c < 16; c++) {
                    int x = chunk_origin_x + c;
                    buffer.put(Tile.tile_terrain_rgba4(tile_data[y][x]));
                }
            }
            blend_map.bindToActiveSlot();
            blend_map.uploadSubData(buffer.flip(),0,16,16,chunk_origin_x,chunk_origin_y);
        }

         */
    }


    //Shared temp-buffer, Main-Thread only. 256 is the maximum possible number of rooms that can be placed on a chunk.
    private static final IntBuffer TMP_ROOMS = IntBuffer.allocate(256); //(Theoretically you could place 256 doors)

    protected void update_layout(Tilemap tilemap, int chunk_x, int chunk_y) {
        // When to update:
        // Obstacles are placed or removed
        // Doors are placed or removed (clearance changed)
        // No need to update:
        // Obstacle replaced by another
        // Has to be updated, even if not in view

        // On Update:
        // 1. Synchronized network disconnect
        // 2. Rebuild layout
        // 3. Synchronized network connect

        Network network = tilemap.network();
        int[][] tile_data = tilemap.tiles();
        int[][] room_layout = tilemap.layout();

        {
            // While rebuilding the chunk's room layout, the old layout is available to the pathfinding.
            if (getRooms(TMP_ROOMS.clear()) > 0) { // synchronized on this
                network.disconnect(TMP_ROOMS.flip()); // synchronized on Network
                TMP_ROOMS.clear();
            }
        }
        // rebuild layout

        int chunk_origin_x = chunk_x * 16;
        int chunk_origin_y = chunk_y * 16;
        int[][] adj = new int[][] {{-1, 0},{ 0,-1},{ 0, 1},{ 1, 0}};
        IntStack doors = new IntStack(16 * 3);
        {
            IntQueue search_queue = new IntQueue(16);
            BitSet visited = new BitSet(256);
            int local_id = 0;
            for (int r = 0; r < 16; r++) {
                for (int c = 0; c < 16; c++) {
                    int v_idx = c + r * 16;
                    if (!visited.get(v_idx)) {
                        visited.set(v_idx);
                        int tile_y = chunk_origin_y + r;
                        int tile_x = chunk_origin_x + c;
                        int current_tile = tile_data[tile_y][tile_x];
                        if (tile_is_obstacle(current_tile)) { // obstacle
                            room_layout[tile_y][tile_x] = room_obstacle();
                        }  else if (tile_clearance_level(current_tile) > 0) { // door
                            int door = room_create(local_id++,chunk_x,chunk_y,
                            tile_clearance_level(current_tile));
                            room_layout[tile_y][tile_x] = door;
                            doors.push(tile_y);
                            doors.push(tile_x);
                            doors.push(door);
                            TMP_ROOMS.put(door);
                        } else { // regular room
                            int room = room_create( local_id++,chunk_x,chunk_y,0);
                            TMP_ROOMS.put(room);
                            search_queue.enqueue(c);
                            search_queue.enqueue(r);
                            while (!search_queue.isEmpty()) {
                                int local_x = search_queue.dequeue();
                                int local_y = search_queue.dequeue();
                                room_layout[local_y + chunk_origin_y][local_x + chunk_origin_x] = room;
                                for (int i = 0; i < 4; i++) {
                                    int adj_local_x = local_x + adj[i][0];
                                    int adj_local_y = local_y + adj[i][1];
                                    if (adj_local_x >= 0 && adj_local_x < 16
                                            && adj_local_y >= 0 && adj_local_y < 16) {
                                        v_idx = adj_local_x + adj_local_y * 16;
                                        if (!visited.get(v_idx)) {
                                            visited.set(v_idx);
                                            int adj_map_x = chunk_origin_x + adj_local_x;
                                            int adj_map_y = chunk_origin_y + adj_local_y;
                                            int tile = tile_data[adj_map_y][adj_map_x];
                                            if (Tile.tile_is_obstacle(tile)) { // obstacle
                                                room_layout[adj_map_y][adj_map_x] = room_obstacle();
                                            }  else if (tile_clearance_level(tile) > 0) { // door
                                                int door = room_create(local_id++,chunk_x,chunk_y, tile_clearance_level(tile));
                                                room_layout[adj_map_y][adj_map_x] = door;
                                                doors.push(adj_map_y);
                                                doors.push(adj_map_x);
                                                doors.push(door);
                                                TMP_ROOMS.put(door);
                                            } else { // regular room
                                                search_queue.enqueue(adj_local_x);
                                                search_queue.enqueue(adj_local_y);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (TMP_ROOMS.flip().hasRemaining()) { // If the chunk has rooms in it (not filled with obstacles)

            // connect doors and edges

            Set<Long> connections = new HashSet<>(32);


            // Connect doors to adjacent rooms
            // (All doors are single tile rooms)

            while (!doors.isEmpty()) {
                int door = doors.pop();
                int door_x = doors.pop();
                int door_y = doors.pop();
                for (int i = 0; i < 4; i++) {
                    int adj_x = door_x + adj[i][0];
                    int adj_y = door_y + adj[i][1];
                    if (adj_x >= 0 && adj_x < tile_data.length
                            && adj_y >= 0 && adj_y < tile_data.length) {
                        int adj_room = room_layout[adj_y][adj_x];
                        if (room_not_obstacle(adj_room)) {
                            long connection = room_connection_key(door,adj_room);
                            connections.add(connection);
                        }
                    }
                }
            }

            // Trace the edges of the chunk for connections.
            // Skip edges that lay on the edges of the tile map

            if (chunk_x > 0) {
                for (int i = 0; i < 16; i++) {
                    int y = i + chunk_origin_y;
                    int room_1 = room_layout[y][chunk_origin_x];
                    int room_2 = room_layout[y][chunk_origin_x - 1];
                    if (room_not_obstacle(room_1) && room_not_obstacle(room_2)) {
                        long connection = room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            } if (chunk_x < ((tile_data.length) / 16) - 1) {
                for (int i = 0; i < 16; i++) {
                    int y = i + chunk_origin_y;
                    int room_1 = room_layout[y][chunk_origin_x + 16];
                    int room_2 = room_layout[y][chunk_origin_x + 15];
                    if (room_not_obstacle(room_1) && room_not_obstacle(room_2)) {
                        long connection = room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            } if (chunk_y > 0) {
                for (int i = 0; i < 16; i++) {
                    int x = i + chunk_origin_x;
                    int room_1 = room_layout[chunk_origin_y][x];
                    int room_2 = room_layout[chunk_origin_y - 1][x];
                    if (room_not_obstacle(room_1) && room_not_obstacle(room_2)) {
                        long connection = room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            } if (chunk_y < ((tile_data.length) / 16) - 1) {
                for (int i = 0; i < 16; i++) {
                    int x = i + chunk_origin_x;
                    int room_1 = room_layout[chunk_origin_y + 16][x];
                    int room_2 = room_layout[chunk_origin_y + 15][x];
                    if (room_not_obstacle(room_1) && room_not_obstacle(room_2)) {
                        long connection = room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            }

            if (!connections.isEmpty()) network.connect(connections); // synchronized

            synchronized (this) {
                rooms.clear();
                if (rooms.capacity() < TMP_ROOMS.remaining()) {
                    int size = U.nextPowerOfTwo(TMP_ROOMS.remaining());
                    rooms = IntBuffer.allocate(size);
                } rooms.put(TMP_ROOMS);
                rooms.flip();
            }


        } else {

            synchronized (this) {
                rooms.clear().flip(); // sets remaining to 0, instead of capacity
            }
        }
        TMP_ROOMS.clear();
    }

    public synchronized int getRooms(IntBuffer dst) {
        int num_rooms = rooms.remaining();
        if (num_rooms > 0) {
            dst.put(rooms);
            rooms.rewind();
        } return num_rooms;
    }

    public void draw_tiles() {
        gfx_tiles_vao.bind();
        gfx_tiles_vbo.bind(); // should not matter
        glDrawArrays(GL_POINTS,0,256);
    }

    public void dispose() {
        Disposable.dispose(
                gfx_tiles_vao,
                gfx_tiles_vbo
        );
    }

}
