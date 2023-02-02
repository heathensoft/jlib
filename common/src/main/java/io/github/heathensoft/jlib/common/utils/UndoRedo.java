package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.FixedStack;

/**
 * @author Frederik Dahl
 * 02/02/2023
 */


public class UndoRedo<T> implements Disposable {

    private final FixedStack<T> undoObjects;
    private final FixedStack<T> redoObjects;


    public UndoRedo(final int capacity) {
        undoObjects = new FixedStack<>(capacity);
        redoObjects = new FixedStack<>(capacity);
    }

    /**
     * Any redo objects will be disposed
     * @param state state prior to edit
     */
    public void onEdit(T state) {
        redoObjects.dispose();
        undoObjects.push(state);
    }

    /**
     * @param state current state
     * @return prior to state
     */
    public T undo(T state) {
        T previousState = undoObjects.pop();
        redoObjects.push(state);
        return previousState;
    }

    /**
     * @param state current state
     * @return prior to state
     */
    public T redo(T state) {
        T previousState = redoObjects.pop();
        undoObjects.push(state);
        return previousState;
    }

    public T peakUndo() {
        return undoObjects.peak();
    }

    public T peakRedo() {
        return redoObjects.peak();
    }

    public boolean canUndo() {
        return !undoObjects.isEmpty();
    }

    public boolean canRedo() {
        return !redoObjects.isEmpty();
    }

    public int undoCount() {
        return undoObjects.size();
    }

    public int redoCount() {
        return redoObjects.size();
    }

    public void dispose() {
        undoObjects.dispose();
        redoObjects.dispose();
    }

}
