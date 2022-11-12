package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

/**
 * @author Frederik Dahl
 * 04/06/2022
 */


public abstract class Pool<T> {
    
    
    private int peak;
    private final int max;
    protected final Queue<T> free;
    
    
    public Pool() {
        this(16);
    }
    
    public Pool(int initialCap) {
        this(initialCap,Integer.MAX_VALUE);
    }
    
    public Pool(int initialCap, int max) {
        this.free = new Queue<>(initialCap);
        this.max = max;
    }
    
    abstract protected T newObject();
    
    public T obtain() {
        return free.size() == 0 ? newObject() : free.dequeue();
    }
    
    public void free(T object) {
        if (object == null) throw new IllegalArgumentException("object cannot be null.");
        if (free.size() < max) {
            free.enqueue(object);
            peak = Math.max(peak,free.size());
            reset(object);
        } else discard(object);
    }
    
    public void fill(int size) {
        for (int i = 0; i < size; i++)
            if (free.size() < max) free.enqueue(newObject());
        peak = Math.max(peak, free.size());
    }
    
    protected void reset(T object) {
        if (object instanceof Poolable)
            ((Poolable)object).reset();
    }
    
    protected void discard(T object) {
        if (object instanceof Disposable)
            ((Disposable)object).dispose();
    }
    
    public void clear() {
        free.clear();
    }

    public int freeObjects() {
        return free.size();
    }
    
}
