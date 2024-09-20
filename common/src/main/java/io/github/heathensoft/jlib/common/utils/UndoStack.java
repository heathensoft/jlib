package io.github.heathensoft.jlib.common.utils;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.FixedStack;

import java.util.function.Consumer;

/**
 * @author Frederik Dahl
 * 07/05/2024
 */


public class UndoStack<T> implements Disposable {

    private T current;
    private final FixedStack<T> undo_stack;
    private final FixedStack<T> redo_stack;
    private final Consumer<T> undo;
    private final Consumer<T> redo;

    public UndoStack(int capacity, Consumer<T> undo, Consumer<T> redo) {
        this.undo_stack = new FixedStack<>(capacity);
        this.redo_stack = new FixedStack<>(capacity);
        this.undo = undo;
        this.redo = redo;
    }

    public T current() { return current; }
    public T peakUndo() { return current == null ? undo_stack.peak() : current; }
    public T peakRedo() {
        return redo_stack.peak();
    }
    public boolean canUndo() { return !undo_stack.isEmpty() || current != null; }
    public boolean canRedo() {
        return !redo_stack.isEmpty();
    }
    public int undoCount() {
        return undo_stack.size();
    }
    public int redoCount() {
        return redo_stack.size();
    }


    /**
     * Start a new edit. Any current edit will get pushed on the undo-stack.
     * You can continue to edit the object after adding it.
     * @param object edit
     */
    public void newEdit(T object) {
        if (current != null) {
            undo_stack.push(current);
        } redo_stack.dispose();
        current = object;
    }

    /**
     * You may push the current onto the undo-stack
     */
    public void pushCurrent() {
        newEdit(null);
    }

    public boolean undo() {
        if (current != null) {
            T object = current;
            undo.accept(object);
            redo_stack.push(object);
            current = null;
            return true;
        } else if (!undo_stack.isEmpty()) {
            T object = undo_stack.pop();
            undo.accept(object);
            redo_stack.push(object);
            return true;
        } return false;
    }

    public boolean redo() {
        if (!redo_stack.isEmpty()) {
            T object = redo_stack.pop();
            redo.accept(object);
            undo_stack.push(object);
            return true;
        } return false;
    }

    public void dispose() {
        redo_stack.dispose();
        undo_stack.dispose();
    }

}
