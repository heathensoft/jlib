package io.github.heathensoft.jlib.ai.pathfinding.test;

import io.github.heathensoft.jlib.common.storage.primitive.BitSet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Frederik Dahl
 * 23/03/2023
 */


public class RoomTest {

    protected final Set<RoomTest> connections;
    protected final int id;
    protected int size;


    public RoomTest(int id) {
        this.connections = new HashSet<>();
        this.id = id;
    }

    protected void addTile() {
        size++;
    }

    protected void removeTile() {
        size--;
    }

    protected void connect(RoomTest room) {
        connections.add(room);
    }

    protected void disconnect(RoomTest room) {
        connections.remove(room);
    }

    protected void isolate() {
        for (var room : connections)
            room.disconnect(this);
        connections.clear();
    }

    public int size() {
        return size;
    }

    public int id() {
        return id;
    }

    public boolean isDoor() {
        return this instanceof Door;
    }

    public boolean connectedTo(RoomTest room) {
        if (this == room) return true;
        BitSet visited = new BitSet(64);
        return connectedTo(room,visited);
    }

    public boolean connectedTo(RoomTest room, int clearance) {
        if (this == room) return true;
        BitSet visited = new BitSet(64);
        return connectedTo(room,visited,clearance);
    }

    protected boolean connectedTo(RoomTest room, BitSet visited) {
        if (visited.get(id)) return false;
        if (this == room) return true;
        boolean connected = false;
        visited.set(id);
        for (var r : connections) {
            connected = r.connectedTo(room,visited);
            if (connected) break;
        } return connected;
    }

    protected boolean connectedTo(RoomTest room, BitSet visited, int clearance) {
        if (visited.get(id)) return false;
        if (this == room) return true;
        boolean connected = false;
        visited.set(id);
        for (var r : connections) {
            if (r.isDoor()) {
                if (!((Door)r).validClearance(clearance)) continue;
            } else { connected = r.connectedTo(room,visited,clearance);
            } if (connected) break;
        } return connected;
    }

    protected static void connect(RoomTest r1, RoomTest r2) {
        r1.connect(r2);
        r2.connect(r1);
    }

    protected static void disconnect(RoomTest r1, RoomTest r2) {
        r1.disconnect(r2);
        r2.disconnect(r1);
    }
}
