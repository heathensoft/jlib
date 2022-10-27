package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

/**
 * @author Frederik Dahl
 * 09/08/2022
 */

@SuppressWarnings("all")
public class DynamicArray<E extends RemOnItr> implements Readable<E>, Disposable {
    
    private Reader<E> onRemoval = (Reader<E>) tmp_removal;
    private E[] items;
    private int iCount;
    
    public DynamicArray(Reader<E> onRemoval, int cap) {
        if (cap < 0) throw new IllegalArgumentException("cap must be >= 0");
        this.items = (E[])new Object[cap];
        this.onRemoval = onRemoval;
    }
    
    public DynamicArray(int cap) {
        this((Reader<E>) tmp_removal,cap);
    }
    
    public void add(E item) {
        if (iCount == items.length) {
            grow(iCount * 2 + 1);
        } items[iCount++] = item;
    }
    
    @Override
    public void read(Reader<E> reader) {
        for (int i = iCount - 1; i >= 0; --i) {
            E item = items[i];
            if (item.removeOnNextIteration()) {
                items[i] = items[--iCount];
                items[iCount] = null;
                onRemoval.next(item);
            } reader.next(item);
        }
    }
    
    @Override
    public void collect(Reader<E> collector) {
        for (int i = 0; i < iCount; i++) {
            collector.next(items[i]);
            items[i] = null;
        } iCount = 0;
    }
    
    public void setCallback(Reader<E> onRemoval) {
        this.onRemoval = onRemoval;
    }
    
    @Override
    public void dispose() {
        E item; for (int i = 0; i < iCount; i++) {
            item = items[i];
            if (item instanceof Disposable) {
                ((Disposable)item).dispose();
            } items[i] = null;
        } iCount = 0;
    }
    
    public void makeRoom(int n) {
        ensureCapacity(n + iCount);
    }
    
    public void ensureCapacity(int size) {
        if (size > items.length) grow(size);
    }
    
    public void fit(int min) {
        int cap = Math.max(Math.max(0,min), iCount);
        if (cap != items.length) grow(cap);
    }
    
    public void fit() {
        fit(0);
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
    
    private void grow(int size) {
        E[] tmp = items;
        items = (E[])new Object[size];
        System.arraycopy(tmp, 0, items, 0, iCount);
    }
    
    private static final Reader<?> tmp_removal = (Reader<Object>) item -> {
        /* prevent null checks */
    };
    
}
