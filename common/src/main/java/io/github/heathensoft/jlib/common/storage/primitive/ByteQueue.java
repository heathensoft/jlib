package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.ByteReader;

import java.util.Collection;
import java.util.EmptyStackException;

/**
 * Simple auto-growing circular queue structure for primitives.
 *
 * Note:
 * The sorting algorithm is not optimized for queue. Might optimize for later version,
 * but why use a queue when if you need to sort the values?
 * Just don't sort a queue 60 times a second. That is all.
 *
 * @author Frederik Dahl
 * 30/05/2022
 */


public class ByteQueue implements ReadableByte {
    
    private byte[] q;
    private int f,r,p;
    
    public ByteQueue(int cap) {
        if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
        q = new byte[cap];
    }
    
    public ByteQueue() {
        this(0);
    }
    
    @Override
    public void read(ByteReader itr) {
        for (int i = 0; i < p; i++) itr.next(q[(f+i)%q.length]);
    }
    
    /**
     * Ensure space for n additional elements. This is useful before enqueuing, as the underlying
     * array would only need to "grow" once instead of potentially multiple times.
     * @param n additional elements
     */
    public void makeRoom(int n) {
        ensureCapacity(n + p);
    }
    
    public void ensureCapacity(int size) {
        if (size > q.length) {
            byte[] tmp = q;
            q = new byte[size];
            for (int v = 0; v < p; v++)
                q[v] = tmp[(f+v) % tmp.length];
            r = p; f = 0;
        }
    }
    
    public void enqueue(byte b) {
        if (p == q.length) {
            byte[] tmp = q;
            q = new byte[p * 2 + 1];
            for (int v = 0; v < p; v++)
                q[v] = tmp[(f+v) % p];
            r = p; f = 0;
        } q[r] = b;
        r = (r+1) % q.length;
        p++;
    }
    
    /**
     * Also calls method makeRoom, before enqueuing
     * @param i the array to enqueue
     */
    public void enqueue(byte[] i) {
        if (i != null) { makeRoom(i.length);
            for (byte v : i) enqueue(v);
        }
    }
    
    /**
     * Also calls method makeRoom, before enqueuing
     * @param c the collection to enqueue
     */
    public void enqueue(Collection<Byte> c) {
        if (c != null) { makeRoom(c.size());
            for (Byte i : c) enqueue(i);
        }
    }
    
    /**
     * Throws EmptyStackException. Use with: isEmpty()
     * @return top element value
     */
    
    public byte dequeue() {
        if (p == 0) throw new EmptyStackException();
        byte v = q[f];
        if (--p == 0) f = r = p;
        else f = (f+1) % q.length;
        return v;
    }
    
    public void dequeueAll(ByteReader itr) {
        while (p > 0) itr.next(dequeue());
    }
    
    /**
     * Fit underlying array to match its size.
     */
    public void fit(int min) {
        int size = Math.max(p,min);
        if (q.length > size) {
            int n = p;
            byte[] tmp = new byte[size];
            for (int i = 0; i < n; i++) {
                tmp[i] = dequeue();
            } q = tmp; p = n;
        }
    }
    
    /**
     * Values are sorted to dequeue highest values
     */
    public void sortDescending() {
        if (p > 1) {
            ByteStack s = new ByteStack(p);
            dequeueAll(s::push);
            s.sortDescending();
            s.popAll(this::enqueue);
        }
    }
    
    /**
     * Values are sorted to dequeue lowest values
     */
    public void sortAscending() {
        if (p > 1) {
            ByteStack s = new ByteStack(p);
            dequeueAll(s::push);
            s.sortAscending();
            s.popAll(this::enqueue);
        }
    }

    public byte[] array() {
        return q;
    }

    public void clear() {
        f = r = p = 0;
    }
    
    public int size() {
        return p;
    }
    
    public int sizeBytes() {
        return p;
    }
    
    public int capacity() {
        return q.length;
    }
    
    public boolean isEmpty() {
        return p == 0;
    }
    
}
