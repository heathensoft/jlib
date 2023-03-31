package io.github.heathensoft.jlib.ai.pathfinding.grid;

/**
 *
 * ROOM
 *
 * 8 bit local_id
 * 8 bit chunk_x_coord
 * 8 bit chunk_y_coord
 * 5 bit unused
 * 2 bit clearance_level (carries over from tile data)
 * 1 bit obstacle_bit (carries over from tile data)
 *
 * @author Frederik Dahl
 * 24/03/2023
 */


public class Room {

    public static int room_create(int local_id, int chunk_x, int chunk_y, int clearance) {
        local_id = (local_id & 0xFF);
        chunk_x = (chunk_x & 0xFF) << 8;
        chunk_y = (chunk_y & 0xFF) << 16;
        // unused bits
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
    
    public static boolean room_occupy_same_chunk(int room1, int room2) {
        return (room1 & 0x00FFFF00) == (room2 & 0x00FFFF00);
    }
    
    public static long room_connection_key(long room1, long room2) {
        return room1 > room2 ? (room1 | (room2 << 32)) : (room2 | (room1 << 32));
    }


    
}
