package io.github.heathensoft.jlib.tiles.structure;

import io.github.heathensoft.jlib.common.storage.primitive.*;
import io.github.heathensoft.jlib.common.storage.primitive.BitSet;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.IDGen;
import io.github.heathensoft.jlib.tiles.TileUtil;

import java.util.*;

/**
 * @author Frederik Dahl
 * 08/11/2022
 */


public class Rooms {
    
    public static final int OBSTACLE = 0;
    
    private final TMap map;
    private final IDGen ids;
    private final IntBag rooms;
    private final IntArray2D layout;
    
    public Rooms(TMap map) {
        TMap.Size map_dim = map.dimension();
        int m_size = map_dim.size();
        this.map = map;
        this.ids = new IDGen();
        this.ids.obtainID(); // start from 1 (0 is obstacle)
        this.rooms = new IntBag(32);
        this.layout = new IntArray2D(m_size,m_size);
        int[][] adj = TileUtil.adjacent4;
        Area m_bounds = map.area();
        BitSet visited = new BitSet(map_dim.tileCount());
        IntQueue search_queue =
        new IntQueue(m_size * 2);
        int cx, cy, nx, ny, v_idx;
        for (int r = 0; r < m_size; r++) {
            for (int c = 0; c < m_size; c++) {
                v_idx = c + r * m_size;
                if (!visited.get(v_idx)) {
                    visited.set(v_idx);
                    if (!map.isObstacle(c,r)) {
                        search_queue.enqueue(c);
                        search_queue.enqueue(r);
                        int room = newRoom();
                        while (!search_queue.isEmpty()) {
                            cx = search_queue.dequeue();
                            cy = search_queue.dequeue();
                            layout.set(room,cx,cy);
                            addTile(room);
                            for (int i = 0; i < 4; i++) {
                                nx = cx + adj[i][0];
                                ny = cy + adj[i][1];
                                if (m_bounds.contains(nx,ny)) {
                                    v_idx = nx + ny * m_size;
                                    if (!visited.get(v_idx)) {
                                        visited.set(v_idx);
                                        if (!map.isObstacle(nx,ny)) {
                                            search_queue.enqueue(nx);
                                            search_queue.enqueue(ny);
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
    
    public synchronized void remove(Area area) {
        Area tmpA0 = new Area(area);
        Area m_bounds = map.area();
        if (m_bounds.intersection(tmpA0)) {
            TMap.Size map_dim = map.dimension();
            int tmp_room_id = newRoom();
            int room_id = -1;
            int x0 = area.minX();
            int y0 = area.minY();
            int x1 = area.maxX();
            int y1 = area.maxY();
            for (int r = y0; r <= y1; r++) {
                for (int c = x0; c <= x1; c++) {
                    room_id = getID(c,r);
                    if (room_id != OBSTACLE) {
                        removeTile(room_id);
                    } addTile(tmp_room_id);
                    layout.set(tmp_room_id,c,r);}
            } Set<Integer> unique_rooms = new HashSet<>();
            int w = x0 - 1; int n = y1 + 1;
            int e = x1 + 1; int s = y0 - 1;
            if (w >= m_bounds.minX()) { // SW -> NW
                for (int r = y0; r <= y1; r++) {
                    room_id = getID(w,r);
                    if (room_id != OBSTACLE)
                        unique_rooms.add(room_id);}
            } if (n <= m_bounds.maxY()) { // NW -> NE
                for (int c = x0; c <= x1; c++) {
                    room_id = getID(c,n);
                    if (room_id != OBSTACLE)
                        unique_rooms.add(room_id);}
            } if (e <= m_bounds.maxX()) { // NE -> SE
                for (int r = y1; r >= y0; r--) {
                    room_id = getID(e,r);
                    if (room_id != OBSTACLE)
                        unique_rooms.add(room_id);}
            } if (s >= m_bounds.minY()) { // SE -> SW
                for (int c = x1; c >= x0; c--) {
                    room_id = getID(c,s);
                    if (room_id != OBSTACLE)
                        unique_rooms.add(room_id);}
            } if (!unique_rooms.isEmpty()){
                // An empty set means that the carved out area is surrounded by obstacles.
                // That means we don't need to flood fill + we have already built a new room.
                // No need to do anything (return).
                // atp. the set is NOT empty. So we have to find the largest room and flood fill
                // its id into smaller adjacent rooms.
                if (unique_rooms.size() == 1){
                    for (var i : unique_rooms) room_id = i;
                } else { List<Integer> room_list = new ArrayList<>(unique_rooms);
                    room_list.sort((o1, o2) -> Integer.compare(rooms.get(o2),rooms.get(o1)));
                    room_id = room_list.get(0);
                } int[][] adj = TileUtil.adjacent4;
                BitSet visited = new BitSet(map_dim.tileCount());
                IntQueue search_queue = new IntQueue(128);
                int cx = tmpA0.minX(); // arbitrary point in area
                int cy = tmpA0.minY(); // arbitrary point in area
                int nx, ny, idx;
                search_queue.enqueue(cx);
                search_queue.enqueue(cy);
                visited.set(cx + cy * map_dim.size());
                layout.set(room_id,cx,cy);
                removeTile(tmp_room_id);
                addTile(room_id);
                while (!search_queue.isEmpty()) {
                    cx = search_queue.dequeue();
                    cy = search_queue.dequeue();
                    for (int i = 0; i < 4; i++) {
                        nx = cx + adj[i][0];
                        ny = cy + adj[i][1];
                        if (m_bounds.contains(nx,ny)) {
                            idx = nx + ny * map_dim.size();
                            if (!visited.get(idx)) {
                                visited.set(idx);
                                int prev_id = getID(nx,ny);
                                if (prev_id != OBSTACLE) {
                                    if (prev_id != room_id) {
                                        search_queue.enqueue(nx);
                                        search_queue.enqueue(ny);
                                        layout.set(room_id,nx,ny);
                                        removeTile(prev_id);
                                        addTile(room_id);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public synchronized void place(Area area) {
        // this is a generic placement method.
        // it does not matter how many tiles are placed at once.
        // more to consider than if we were to only place singles.
        Area tmpA0 = new Area(area);
        Area m_bounds = map.area();
        if (m_bounds.intersection(tmpA0)) {
            int x0 = tmpA0.minX();
            int y0 = tmpA0.minY();
            int x1 = tmpA0.maxX();
            int y1 = tmpA0.maxY();
            int space_removed = 0;
            for (int r = y0; r <= y1; r++) {
                for (int c = x0; c <= x1; c++) {
                    int room = getID(c,r);
                    if (room != OBSTACLE) {
                        layout.set(OBSTACLE,c,r);
                        space_removed++;
                        removeTile(room);
                    }
                }
            }
            if (space_removed > 0) {
                tmpA0.expand(4);
                m_bounds.intersection(tmpA0);
                x0 = tmpA0.minX();
                y0 = tmpA0.minY();
                x1 = tmpA0.maxX();
                y1 = tmpA0.maxY();
                int lx, ly, idx,
                cx, cy, nx, ny, room;
                int cols = tmpA0.cols();
                int rows = tmpA0.rows();
                Area tmpA1 = new Area(area);
                tmpA1.expand(1);
                m_bounds.intersection(tmpA1);
                int[][] adj = TileUtil.adjacent4;
                BitSet visited = new BitSet(rows*cols);
                IntQueue search_queue = new IntQueue(rows * 2);
                List<Divide> divide_list = new ArrayList<>();
                for (int m_y = y0; m_y <= y1; m_y++) {
                    ly = m_y - y0;
                    for (int m_x = x0; m_x <= x1; m_x++) {
                        lx = m_x - x0;
                        idx = lx + ly * cols;
                        if (!visited.get(idx)) {
                            visited.set(idx);
                            room = getID(m_x,m_y);
                            if (room != OBSTACLE) {
                                search_queue.enqueue(m_x);
                                search_queue.enqueue(m_y);
                                Divide divide = new Divide(m_x,m_y,room);
                                while (!search_queue.isEmpty()) {
                                    cx = search_queue.dequeue();
                                    cy = search_queue.dequeue();
                                    divide.expand(cx,cy);
                                    for (int i = 0; i < 4; i++) {
                                        nx = cx + adj[i][0];
                                        ny = cy + adj[i][1];
                                        if (tmpA0.contains(nx,ny)) {
                                            idx = (nx - x0) + (ny - y0) * cols;
                                            if (!visited.get(idx)) {
                                                visited.set(idx);
                                                room = getID(nx,ny);
                                                if (room != OBSTACLE) {
                                                    search_queue.enqueue(nx);
                                                    search_queue.enqueue(ny);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (divide.area.intersects(tmpA1))
                                    divide_list.add(divide);
                            }
                        }
                    }
                } if (divide_list.size() > 1) {
                    TMap.Size map_dim = map.dimension();
                    Divides divides = new Divides(divide_list);
                    if (!divides.ofInterest.isEmpty()) {
                        visited = new BitSet(map_dim.tileCount());
                        for (var entry: divides.ofInterest.entrySet()) {
                            List<Divide> divides_by_room = entry.getValue();
                            int c_room = entry.getKey();
                            int n_divides = divides_by_room.size();
                            divides_by_room.sort(Comparator.comparingInt(o -> o.area.size()));
                            for (int d = 0; d < divides_by_room.size() - 1; d++) {
                                Divide divide = divides_by_room.get(d);
                                cx = divide.x; cy = divide.y;
                                idx = cx + cy * map_dim.size();
                                if (!visited.get(idx)) {
                                    int newRoom = newRoom();
                                    search_queue.clear();
                                    search_queue.enqueue(cx);
                                    search_queue.enqueue(cy);
                                    while (!search_queue.isEmpty() ) {
                                        cx = search_queue.dequeue();
                                        cy = search_queue.dequeue();
                                        removeTile(c_room);
                                        layout.set(newRoom,cx,cy);
                                        addTile(newRoom);
                                        for (int i = 0; i < 4; i++) {
                                            nx = cx + adj[i][0];
                                            ny = cy + adj[i][1];
                                            if (m_bounds.contains(nx,ny)) {
                                                idx = nx + ny * map_dim.size();
                                                if (!visited.get(idx)) {
                                                    visited.set(idx);
                                                    if (getID(nx,ny) == c_room) {
                                                        search_queue.enqueue(nx);
                                                        search_queue.enqueue(ny);
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
            }
        }
    }
    
    public int getID(int x, int y) {
        return layout.get(x,y);
    }
    
    public boolean isObstacle(int x, int y) {
        return layout.get(x,y) == OBSTACLE;
    }
    
    private void addTile(int room) {
        rooms.data()[room]++;
    }
    
    private void removeTile(int room) {
        rooms.data()[room]--;
        if (rooms.data()[room] == 0) {
            ids.returnID(room);
        }
    }
    
    private int newRoom() {
        int room = ids.obtainID();
        rooms.set(room,0);
        return room;
    }
    
    private static final class Divide {
        private final int x, y, r;
        private final Area area;
        private Divide(int x, int y, int r) {
            this.x = x; this.y = y; this.r = r;
            this.area = new Area(x,y);
        }public int x() {return x;}
        public int y() {return y;}
        public int room() {return r;}
        public Area area() {return area;}
        public void expand(int x, int y) {
            area.expandToContain(x,y);
        }
    }
    
    private static final class Divides {
        public final Map<Integer,List<Divide>> ofInterest;
        public Divides(List<Divide> list) {
            ofInterest = new HashMap<>();
            for (Divide divide : list) {
                int room = divide.r;
                List<Divide> byRoom = ofInterest.get(room);
                if (byRoom == null) {
                    byRoom = new ArrayList<>(2);
                    byRoom.add(divide);
                    ofInterest.put(room,byRoom);
                } else byRoom.add(divide);
            } final IntStack stack = new IntStack(ofInterest.size());
            ofInterest.forEach((key, value) -> {
                if (value.size() == 1) {
                    stack.push(key);
                }}); while (!stack.isEmpty()) {
                ofInterest.remove(stack.pop());
            }
        }
    }
}
