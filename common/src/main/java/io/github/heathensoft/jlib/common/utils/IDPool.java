package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;

/**
 * @author Frederik Dahl
 * 17/11/2022
 */


public class IDPool {
    private final IntQueue ids
            = new IntQueue(16);
    private int new_id = 0;
    public int obtainID() {
        if (ids.isEmpty()) {
            return new_id++;
        } else return ids.dequeue();
    } public void returnID(int id) {
        ids.enqueue(id);
    }
    public void clear() {
        ids.clear();
        new_id = 0;
    }
}
