package io.github.heathensoft.jlib.tiles.neo;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Thread-safe network structure of rooms and connections
 *
 * Top level of the hierarchical pathfinding.
 *
 *
 * @author Frederik Dahl
 * 25/03/2023
 */


public class Network {

    private final Map<Integer,IntBuffer> room_adjacent; // writing mode by default
    private final Set<Long> connection_hash;
    private final Set<Long> tmp_set;


    public Network(MapSize mapSize) {
        connection_hash = new HashSet<>(mapSize.chunks_count * 4);
        room_adjacent = new HashMap<>(mapSize.chunks_count * 2);
        tmp_set = new HashSet<>(256);
    }

    public synchronized void connect(Set<Long> connections) {
        for (var connection : connections) connect(connection);
    }

    public synchronized void disconnect(Set<Long> connections) {
        for (var connection : connections) disconnect(connection);
    }

    public synchronized void disconnect(IntBuffer rooms) {
        if (rooms.hasRemaining()) {
            tmp_set.clear();
            for (int i = rooms.position(); i < rooms.limit(); i++) {
                int room = rooms.get(i);
                IntBuffer connected = room_adjacent.get(room);
                if (connected != null) {
                    if (connected.position() > 0) {
                        connected.flip();
                        while (connected.hasRemaining()) {
                            tmp_set.add(Tile.room_connection_key(room,connected.get()));
                        } connected.limit(connected.capacity());

                    }
                }
            } disconnect(tmp_set);
        }
    }

    public synchronized int getAdjacent(int room, IntBuffer dst) {
        IntBuffer connected = room_adjacent.get(room);
        if (connected != null) {
            connected.flip();
            int remaining = connected.remaining();
            dst.put(connected);
            connected.limit(connected.capacity());
            return remaining;
        } return 0;
    }

    /*
        fast hash checks to see if any two rooms are adjacent

     */
    public boolean areAdjacent(int room1, int room2) {
        return connection_hash.contains(Tile.room_connection_key(room1,room2));
    }

    public synchronized boolean areAdjacentSynchronized(int room1, int room2) {
        return connection_hash.contains(Tile.room_connection_key(room1,room2));
    }

    public boolean areAdjacentOrEqual(int room1, int room2) {
        return room1 == room2 || connection_hash.contains(Tile.room_connection_key(room1,room2));
    }

    private void connect(long connection) {
        int r1 = (int)((connection)       & 0xFFFF_FFFFL);
        int r2 = (int)((connection >> 32) & 0xFFFF_FFFFL);
        if (r1 == r2) throw new IllegalStateException(
                "attempting to connect room to itself");
        if (connection_hash.add(connection)) {
            int rr = r1;
            for (int i = 0; i < 2; i++) {
                r1 = i == 0 ? r1 : r2;
                r2 = i == 0 ? r2 : rr;
                IntBuffer connections = room_adjacent.get(r1);
                if (connections == null) {
                    connections = IntBuffer.allocate(4);
                    room_adjacent.put(r1,connections);
                } else if (!connections.hasRemaining()) {
                    IntBuffer tmp = IntBuffer.allocate(connections.capacity() * 2);
                    tmp.put(connections.flip());
                    connections = tmp;
                } connections.put(r2);
            }
        }
    }

    private void disconnect(long connection) {
        int r1 = (int)((connection)       & 0xFFFF_FFFFL);
        int r2 = (int)((connection >> 32) & 0xFFFF_FFFFL);
        if (r1 == r2) throw new IllegalStateException(
                "attempting to disconnect room from itself");
        if (connection_hash.remove(connection)) {
            int rr = r1;
            for (int i = 0; i < 2; i++) {
                r1 = i == 0 ? r1 : r2;
                r2 = i == 0 ? r2 : rr;
                IntBuffer connections = room_adjacent.get(r1);
                int pos = connections.position();
                for (int j = 0; j < pos; j++) {
                    if (connections.get(j) == r2) {
                        connections.put(j,connections.get(pos - 1));
                        connections.position(pos - 1);
                        if (connections.position() == 0) {
                            room_adjacent.remove(r1);
                        } break;
                    }
                }
            }
        }
    }



}
