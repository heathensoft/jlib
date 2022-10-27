package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

import java.util.EmptyStackException;

/**
 * Grows on demand as elements are accessed.
 * Used as either an indexed or unordered array.
 *
 * @author Frederik Dahl
 * 05/06/2022
 */

@SuppressWarnings("unchecked")
public class Container<E> implements Disposable, Readable<E> {
    
    private E[] items;
    private int peak;
    private int iCount;
    
    public Container(int cap) {
        if (cap < 0) throw new IllegalArgumentException("cap must be >= 0");
        this.items = (E[])new Object[cap];
    }
    
    public Container() {
        this(0);
    }
    
    public E get(int index) {
        return items[index];
    }
    
    public void set(int index, E item) {
        if (item == null) {
            if (index < items.length) {
                if (items[index] == null) return;
                items[index] = null;
                int peak = this.peak - 1;
                if (index == peak) {
                    this.peak = 1 + nextPeak(peak);
                } iCount--;
            }
        } else {
            if (index >= items.length) {
                grow(index + 1);
                items[index] = item;
                peak = index + 1;
                iCount++;
            } else {
                E existing = items[index];
                items[index] = item;
                if (existing == null) {
                    peak = Math.max(peak, index + 1);
                    iCount++;
                }
            }
        }
    }
    
    public void add(E item) {
        if (item == null) throw new IllegalArgumentException("Adding null items not allowed. Use set");
        if (peak == items.length) {
            grow(peak * 2 + 1);
        } items[peak++] = item;
        iCount++;
    }
    
    public E remove(int index) {
        E item = items[index];
        if (item != null) { iCount--;
            int peak = this.peak - 1;
            if (index == peak) {
                items[index] = null;
            } else {
                items[index] = items[peak];
                items[peak] = null;
            } peak = nextPeak(peak);
            this.peak = peak + 1;
        } return item;
    }
    
    public E remove() {
        if (peak == 0) throw new EmptyStackException();
        return remove(peak - 1);
    }
    
    public void ensureCapacity(int size) {
        if (size > items.length) grow(size);
    }
    
    public void fit(int min) {
        int cap = Math.max(Math.max(0,min), peak);
        if (cap != items.length) grow(cap);
    }
    
    public void fit() {
        fit(0);
    }
    
    public void stack() {
        if (isStacked()) return;
        int l = 0;
        int r = peak;
        while (l<r) {
            while (items[l] != null) l++;
            while (items[r] == null) r--;
            items[l++] = items[r];
            items[r--] = null;
        } peak = iCount - 1;
    }
    
    public void clear() {
        for (int i = 0; i < peak; i++) {
            items[i] = null;
        } peak = 0; iCount = 0;
    }
    
    @Override
    public void dispose() {
        E item; for (int i = 0; i < peak; i++) {
            item = items[i];
            if (item instanceof Disposable) {
                ((Disposable)item).dispose();
            } items[i] = null;
        } peak = 0; iCount = 0;
    }
    
    @Override
    public void read(Reader<E> reader) {
        if (isStacked()) {
            for (int i = 0; i < iCount; i++)
                reader.next(items[i]);
        } else {
            E item; for (int i = 0; i < peak; i++) {
                item = items[i];
                if (item == null) continue;
                reader.next(item);
            }
        }
    }
    
    public void read(ReaderIDX<E> reader) {
        if (isStacked()) {
            for (int i = 0; i < iCount; i++)
                reader.next(items[i],i);
        } else {
            E item; for (int i = 0; i < peak; i++) {
                item = items[i];
                if (item == null) continue;
                reader.next(item,i);
            }
        }
    }
    
    @Override
    public void collect(Reader<E> collector) {
        E item;
        if (isStacked()) {
            for (int i = 0; i < iCount; i++) {
                item = items[i];
                items[i] = null;
                collector.next(item);
            }
        } else {
            for (int i = 0; i < peak; i++) {
                item = items[i];
                if (item == null) continue;
                items[i] = null;
                collector.next(item);
            }
        } peak = 0; iCount = 0;
    }
    
    public boolean isEmpty() {
        return iCount == 0;
    }
    
    public int capacity() {
        return items.length;
    }
    
    public int count() {
        return iCount;
    }
    
    public int peak() {
        return peak;
    }
    
    public boolean isStacked() {
        return (peak + 1) == iCount;
    }
    
    private void grow(int size) {
        E[] tmp = items;
        items = (E[])new Object[size];
        System.arraycopy(tmp, 0, items, 0, peak);
    }
    
    private int nextPeak(int peak) {
        peak--;
        while (peak > 0) {
            if (items[--peak] == null) continue;
            return peak;
        } return -1;
    }
    
}
