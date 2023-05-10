package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.storage.primitive.IntStack;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Frederik Dahl
 * 24/03/2023
 */


public class Chunk {

    /*
        Shared temp-buffer, Main-Thread only.
        256 is the maximum possible number of rooms that can be placed on a chunk.
        (Theoretically you could place 256 doors)
     */
    private static final IntBuffer TMP_ROOMS = IntBuffer.allocate(256);
    private IntBuffer rooms; // in read-mode by default (set to read-mode on initialization)




    protected void updateTerrain(Texture terrain, int[][] tile_data, int chunk_x, int chunk_y) {
        int[][] adj = Tile.adjacent8;
        int[] layer_masks = new int[4];
        int map_across = tile_data.length;
        int lim = map_across / 16 - 1;
        int chunk_origin_x = chunk_x * 16;
        int chunk_origin_y = chunk_y * 16;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(256 * 4);
            if (chunk_x == 0 || chunk_x == lim || chunk_y == 0 || chunk_y == lim) {
                for (int r = 0; r < 16; r++) {
                    for (int c = 0; c < 16; c++) {
                        int tile = tile_data[r][c];
                        int tile_layers = Tile.tile_terrain_layers(tile);
                        int tile_variant = Tile.tile_terrain_layer_variation(tile);
                        if (tile_layers == 0) {
                            buffer.put((byte)0xFF);
                            buffer.put((byte)0xFF);
                            buffer.put((byte)0xFF);
                            buffer.put((byte)0xFF);
                        } else {
                            layer_masks[0] = 0;
                            layer_masks[1] = 0;
                            layer_masks[2] = 0;
                            layer_masks[3] = 0;
                            int tile_x = chunk_origin_x + c;
                            int tile_y = chunk_origin_y + r;
                            for (int i = 0; i < 8; i++) {
                                int x = adj[i][0] + tile_x;
                                int y = adj[i][1] + tile_y;
                                if (x < 0 || x >= map_across || y < 0 || y >= map_across) {
                                    layer_masks[0] |= (1 << i);
                                    layer_masks[1] |= (1 << i);
                                    layer_masks[2] |= (1 << i);
                                    layer_masks[3] |= (1 << i);
                                } else {
                                    int adj_layers = Tile.tile_terrain_layers(tile_data[y][x]);
                                    layer_masks[0] |= (adj_layers & 0b0001) == 0 ? 0 : (1 << i);
                                    layer_masks[1] |= (adj_layers & 0b0010) == 0 ? 0 : (1 << i);
                                    layer_masks[2] |= (adj_layers & 0b0100) == 0 ? 0 : (1 << i);
                                    layer_masks[3] |= (adj_layers & 0b1000) == 0 ? 0 : (1 << i);
                                }
                            }
                            for (int i = 0; i < 4; i++) {
                                if((tile_layers & (1 << i)) == 0)
                                    buffer.put((byte)0xFF);
                                else {
                                    int mask = layer_masks[i];
                                    int idx = mask == 0xFF ? (48 + (tile_variant & 0x0F)) : Tile.terrain_mask_to_uv(mask);
                                    buffer.put((byte)(idx + 64 * i)); // shader: idx += biome * 256
                                }
                            }
                        }
                    }
                }
            }
            terrain.bindToActiveSlot();
            terrain.uploadSubData(buffer.flip(),0,16,16,chunk_origin_x,chunk_origin_y);
        }
    }


    protected void updateLayout(Network network, int[][] room_layout, int[][] tile_data, int chunk_x, int chunk_y) {

        /*
            On Update:
            1. Synchronized network disconnect
            2. Rebuild layout
            3. Synchronized network connect
         */


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
        int[][] adj = Tile.adjacent4;
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
                        if (Tile.tile_is_obstacle(current_tile)) {
                            // obstacle
                            room_layout[tile_y][tile_x] = Tile.room_obstacle();
                        }
                        else if (Tile.tile_clearance_level(current_tile) > 0) {
                            // door
                            int door = Tile.room_create(local_id++,chunk_x,chunk_y,
                            Tile.tile_clearance_level(current_tile));
                            room_layout[tile_y][tile_x] = door;
                            doors.push(tile_y);
                            doors.push(tile_x);
                            doors.push(door);
                            TMP_ROOMS.put(door);
                        } else {
                            // regular room
                            int room = Tile.room_create(
                                    local_id++,chunk_x,chunk_y,0);
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
                                            if (Tile.tile_is_obstacle(tile)) {
                                                // obstacle
                                                room_layout[adj_map_y][adj_map_x] = Tile.room_obstacle();
                                            }
                                            else if (Tile.tile_clearance_level(tile) > 0) {
                                                // door
                                                int door = Tile.room_create(local_id++,chunk_x,chunk_y,
                                                        Tile.tile_clearance_level(tile));
                                                room_layout[adj_map_y][adj_map_x] = door;
                                                doors.push(adj_map_y);
                                                doors.push(adj_map_x);
                                                doors.push(door);
                                                TMP_ROOMS.put(door);
                                            } else {
                                                // regular room
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

        TMP_ROOMS.flip();

        if (TMP_ROOMS.hasRemaining()) { // If the chunk has rooms in it (not filled with obstacles)

            // connect doors and edges

            Set<Long> connections = new HashSet<>(32);

            /*
                Connect doors to adjacent rooms
                (All doors are single tile rooms)
             */

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
                        if (Tile.room_not_obstacle(adj_room)) {
                            long connection = Tile.room_connection_key(door,adj_room);
                            connections.add(connection);
                        }
                    }
                }
            }

            /*
                Trace the edges of the chunk for connections.
                Skip edges that lay on the edges of the tile map
             */

            if (chunk_x > 0) {
                for (int i = 0; i < 16; i++) {
                    int y = i + chunk_origin_y;
                    int room_1 = room_layout[y][chunk_origin_x];
                    int room_2 = room_layout[y][chunk_origin_x - 1];
                    if (Tile.room_not_obstacle(room_1) && Tile.room_not_obstacle(room_2)) {
                        long connection = Tile.room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            } if (chunk_x < ((tile_data.length) / 16) - 1) {
                for (int i = 0; i < 16; i++) {
                    int y = i + chunk_origin_y;
                    int room_1 = room_layout[y][chunk_origin_x + 16];
                    int room_2 = room_layout[y][chunk_origin_x + 15];
                    if (Tile.room_not_obstacle(room_1) && Tile.room_not_obstacle(room_2)) {
                        long connection = Tile.room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            } if (chunk_y > 0) {
                for (int i = 0; i < 16; i++) {
                    int x = i + chunk_origin_x;
                    int room_1 = room_layout[chunk_origin_y][x];
                    int room_2 = room_layout[chunk_origin_y - 1][x];
                    if (Tile.room_not_obstacle(room_1) && Tile.room_not_obstacle(room_2)) {
                        long connection = Tile.room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            } if (chunk_y < ((tile_data.length) / 16) - 1) {
                for (int i = 0; i < 16; i++) {
                    int x = i + chunk_origin_x;
                    int room_1 = room_layout[chunk_origin_y + 16][x];
                    int room_2 = room_layout[chunk_origin_y + 15][x];
                    if (Tile.room_not_obstacle(room_1) && Tile.room_not_obstacle(room_2)) {
                        long connection = Tile.room_connection_key(room_1,room_2);
                        connections.add(connection);
                    }
                }
            }

            if (!connections.isEmpty())
                network.connect(connections); // synchronized

            synchronized (this) {
                rooms.clear();
                if (rooms.capacity() < TMP_ROOMS.remaining()) {
                    int size = Tile.nextPowerOfTwo(TMP_ROOMS.remaining());
                    rooms = IntBuffer.allocate(size);
                } rooms.put(TMP_ROOMS);
                rooms.flip();
            }


        } else {

            synchronized (this) {
                rooms.clear();
                rooms.flip();
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

}
