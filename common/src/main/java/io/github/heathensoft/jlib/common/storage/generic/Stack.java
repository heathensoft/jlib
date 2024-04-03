package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

/**
 * @author Frederik Dahl
 * 04/06/2022
 */

@SuppressWarnings("unchecked")
public class Stack<E> implements Disposable, Readable<E>  {
    
    private E[] items;
    private int p; // pointer
    
    public Stack(int cap) {
        if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
        this.items = (E[])new Object[cap];
    }
    
    public Stack() {
        this(0);
    }
    
    public void push(E item) {
        if (item == null) throw new IllegalArgumentException("item == null");
        if (p == length()) { E[] tmp = items;
            items = (E[])new Object[p * 2 + 1];
            System.arraycopy(tmp,0, items,0,tmp.length);
        } items[p++] = item;
    }
    
    public E pop() {
        E item = items[--p];
        items[p] = null;
        return item;
    }
    
    public E peak() {
        if (isEmpty()) return null;
        return items[p - 1];
    }
    
    @Override
    public void collect(Reader<E> collector) {
        while (p > 0) collector.next(pop());
    }
    
    public void fit(int min) {
        int cap = Math.max(Math.max(0,min), p);
        if (cap != items.length) grow(cap);
    }
    
    public void fit() {
        fit(0);
    }
    
    public void ensureCapacity(int size) {
        if (size > items.length) grow(size);
    }
    
    public void clear() {
        collect(item -> item = null);
    }
    
    public int size() {
        return p;
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
    
    @Override
    public void read(Reader<E> reader) {
        E[] items = this.items;
        for (int i = 0; i < p; i++) {
            reader.next(items[i]);
        }
    }
    
    @Override
    public void dispose() {
        collect(item -> {
            if (item instanceof Disposable)
                ((Disposable)item).dispose();
        });
    }
    
    private void grow(int size) {
        E[] tmp = items;
        items = (E[])new Object[size];
        System.arraycopy(tmp, 0, items, 0, p);
    }
    
    
}
