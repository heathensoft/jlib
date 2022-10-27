package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.IntReader;

import java.util.Arrays;

/**
 * A malleable multipurpose array. Thin wrapper around primitive array.
 * set: only grows by to fit.
 * add: grows by (size * 2 + 1)
 * be sure to ensure capacity.
 *
 * Use with caution.
 *
 * @author Frederik Dahl
 * 06/06/2022
 */


public class IntBag implements ReadableInt {
 
    private int size;
    private int[] data;
    
    public IntBag(int cap) {
        if (cap < 0) throw new NegativeArraySizeException("bits < 0: " + cap);
        data = new int[cap];
    }
    
    public IntBag() {
        this(0);
    }
    
    public int get(int idx) {
        return data[idx];
    }
    
    public void set(int idx, int val) {
        if (idx >= data.length) {
            grow(idx + 1);
        } size = Math.max(size,idx + 1);
        data[idx] = val;
    }
    
    public void setUnsafe(int idx, int val) {
        size = Math.max(size,idx + 1);
        data[idx] = val;
    }
    
    public void push(int val) {
        if (size == data.length) {
            grow(size * 2 + 1);
        } data[size++] = val;
    }
    
    public void pushUnsafe(int val) {
        data[size++] = val;
    }
    
    /**
     * store data[idx] in val.
     * move data[--size] to data[idx].
     * return val
     *
     * @param idx index
     * @return removed value
     */
    public int remove(int idx) {
        int val = data[idx];
        data[idx] = data[--size];
        return val;
    }
    
    public int pop() {
        return data[--size];
    }
    
    public void swap(int idx1, int idx2) {
        int tmp = data[idx1];
        data[idx1] = idx2;
        data[idx2] = tmp;
    }
    
    public int[] data() {
        return data;
    }
    /** Use with caution**/
    public void setSize(int size) {
        this.size = size;
    }
    
    public int size() {
        return size;
    }
    
    public int sizeBytes() {
        return size * Integer.BYTES;
    }
    
    public int capacity() {
        return data.length;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Used to fit the underlying array to max(size, min)
     * @param min the minimum wanted size.
     */
    
    public void fit(int min) {
        int s = Math.max(Math.max(0, min), size);
        if (s != data.length) grow(s);
    }
    
    public void fit() {
        fit(0);
    }
    
    public void ensureCapacity(int size) {
        if (size > data.length) grow(size);
    }
    
    public void clear() {
        size = 0;
    }
    
    public void sort() {
        Arrays.sort(data,0,size);
    }
    
    @Override
    public void read(IntReader reader) {
        for (int i = 0; i < size; i++) {
            reader.next(data[i]);
        }
    }
    
    private void grow(int s) {
        int[] tmp = data;
        data = new int[s];
        if (isEmpty()) return;
        System.arraycopy(tmp, 0, data, 0, size);
    }
    
}
    

