package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.Stack;

/**
 * @author Frederik Dahl
 * 03/01/2023
 */


public class UndoRedo<T> implements Disposable {

    private final Stack<T> redoObjects;
    private final Stack<T> undoObjects;

    public UndoRedo() {
        redoObjects = new Stack<>(1);
        undoObjects = new Stack<>(1);
    }

    /**
     * Any redo objects will be disposed
     * @param preEditState state prior to edit
     */
    public void edit(T preEditState) {
        redoObjects.dispose();
        undoObjects.push(preEditState);
    }

    /**
     * @param currentState current state
     * @return prior to state
     */
    public T undo(T currentState) {
        T previousState = undoObjects.pop();
        redoObjects.push(currentState);
        return previousState;
    }

    /**
     * @param currentState current state
     * @return prior to state
     */
    public T redo(T currentState) {
        T previousState = redoObjects.pop();
        undoObjects.push(currentState);
        return previousState;
    }

    public boolean canUndo() {
        return !undoObjects.isEmpty();
    }

    public boolean canRedo() {
        return !redoObjects.isEmpty();
    }

    @Override
    public void dispose() {
        undoObjects.dispose();
        redoObjects.dispose();
    }
}
