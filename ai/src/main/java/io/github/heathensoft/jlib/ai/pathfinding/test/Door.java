package io.github.heathensoft.jlib.ai.pathfinding.test;

/**
 * @author Frederik Dahl
 * 23/03/2023
 */


public class Door extends RoomTest {

    private int clearance_level;

    public Door(int id, int clearanceLevel) {
        super(id);
        clearance_level = clearanceLevel;
    }

    public Door(int id) {
        super(id);
        clearance_level = 0;
    }

    public boolean validClearance(int clearance) {
        return clearance >= this.clearance_level;
    }

    public void setClearanceLevel(int clearance) {
        this.clearance_level = clearance;
    }

    public int clearanceLevel() {
        return clearance_level;
    }
}
