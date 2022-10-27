package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

import java.util.EmptyStackException;

/**
 * @author Frederik Dahl
 * 04/06/2022
 */

@SuppressWarnings("unchecked")
public class Queue<E> implements Disposable, Readable<E>  {
    
    private E[] items;
    private int f,r,p;
    
    public Queue(int cap) {
        if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
        this.items = (E[])new Object[cap];
    }
    
    public Queue() {
        this(0);
    }
    
    public void enqueue(E item) {
        if (item == null) throw new IllegalArgumentException("item == null");
        if (p == items.length) { E[] tmp = items;
            items = (E[])new Object[p * 2 + 1];
            for (int i = 0; i < p; i++) {
                items[i] = tmp[(f+i)%p];
            } r = p; f = 0;
        } items[r] = item;
        r = (r+1) % length();
        p++;
    }
    
    public E dequeue() {
        if (p == 0) throw new EmptyStackException();
        E item = items[f];
        items[f] = null;
        if (--p == 0) f = r = p;
        else f = (f+1) % length();
        return item;
    }
    
    @Override
    public void collect(Reader<E> collector) {
        while (p > 0) collector.next(dequeue());
    }
    
    public void fit(int min) {
        int size = Math.max(p,min);
        if (length() > size) {
            int n = p;
            E[] tmp = (E[])new Object[size];
            for (int i = 0; i < n; i++) {
                tmp[i] = dequeue();
            } items = tmp; p = n;
        }
    }
    
    public void fit() {
        fit(0);
    }
    
    public void ensureCapacity(int size) {
        if (size > length()) {
            E[] tmp = items;
            items = (E[])new Object[size];
            for (int v = 0; v < p; v++)
                items[v] = tmp[(f+v) % tmp.length];
            r = p; f = 0;
        }
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
        for (int i = 0; i < p; i++)
            reader.next(items[(f+i)%length()]);
    }
    
    @Override
    public void dispose() {
        collect(item -> {
            if (item instanceof Disposable)
                ((Disposable)item).dispose();
        });
    }
    
}
