package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

/**
 * @author Frederik Dahl
 * 02/02/2023
 */

@SuppressWarnings("unchecked")
public class FixedStack<E> implements Disposable, Readable<E> {

    private final E[] items;
    private int idx;
    private int size;

    public FixedStack(final int cap) {
        if (cap < 1) throw new IllegalArgumentException("cap < 1: " + cap);
        this.items = (E[])new Object[cap];
    }

    public void push(E item) {
        if (item == null) throw new IllegalArgumentException("item == null");
        E existing = items[idx];
        if (existing == null) size++;
        else if (existing instanceof Disposable) {
            ((Disposable) existing).dispose();
        } items[idx] = item;
        idx = nextIndex();
    }

    public E pop() {
        if (size == 0) return null;
        idx = prevIndex();
        E item = items[idx];
        items[idx] = null;
        size--;
        return item;
    }

    public E peak() {
        return items[prevIndex()];
    }

    public int size() {
        return size;
    }

    public float loadFactor() {
        return (float) (size() / items.length);
    }

    public int length() {
        return items.length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private int nextIndex() {
        return (idx + 1) % items.length;
    }

    private int prevIndex() {
        if (idx == 0) return (items.length - 1);
        return (idx - 1);
    }

    public void clear() {
        collect(item -> item = null);
    }

    public void dispose() {
        collect(item -> {
            if (item instanceof Disposable)
                ((Disposable)item).dispose();
        });
    }

    public void read(Reader<E> reader) {
        E[] items = this.items;
        int count = this.size;
        int idx = this.idx;
        while (count != 0) {
            idx = idx == 0 ? (items.length - 1) : (idx - 1);
            reader.next(items[idx]);
            count--;
        }

    }

    public void collect(Reader<E> collector) {
        while (!isEmpty()) {
            collector.next(pop());
        }
    }
}
