package io.github.heathensoft.jlib.common.storage.primitive;

import io.github.heathensoft.jlib.common.storage.primitive.iterators.DoubleReader;

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


public class DoubleQueue implements ReadableDouble {
    
    private double[] q;
    private int f,r,p;
    
    public DoubleQueue(int cap) {
        if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
        q = new double[cap];
    }
    
    public DoubleQueue() {
        this(0);
    }
    
    @Override
    public void read(DoubleReader itr) {
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
            double[] tmp = q;
            q = new double[size];
            for (int v = 0; v < p; v++)
                q[v] = tmp[(f+v) % tmp.length];
            r = p; f = 0;
        }
    }
    
    public void enqueue(double d) {
        if (p == q.length) {
            double[] tmp = q;
            q = new double[p * 2 + 1];
            for (int v = 0; v < p; v++)
                q[v] = tmp[(this.f +v) % p];
            r = p; this.f = 0;
        } q[r] = d;
        r = (r+1) % q.length;
        p++;
    }
    
    /**
     * Also calls method makeRoom, before enqueuing
     * @param i the array to enqueue
     */
    public void enqueue(double[] i) {
        if (i != null) { makeRoom(i.length);
            for (double v : i) enqueue(v);
        }
    }
    
    /**
     * Also calls method makeRoom, before enqueuing
     * @param c the collection to enqueue
     */
    public void enqueue(Collection<Double> c) {
        if (c != null) { makeRoom(c.size());
            for (Double i : c) enqueue(i);
        }
    }
    
    /**
     * Throws EmptyStackException. Use with: isEmpty()
     * @return top element value
     */
    
    public double dequeue() {
        if (p == 0) throw new EmptyStackException();
        double v = q[f];
        if (--p == 0) f = r = p;
        else f = (f+1) % q.length;
        return v;
    }
    
    public void dequeueAll(DoubleReader itr) {
        while (p > 0) itr.next(dequeue());
    }
    
    /**
     * Fit underlying array to match its size.
     */
    public void fit(int min) {
        int size = Math.max(p,min);
        if (q.length > size) {
            int n = p;
            double[] tmp = new double[size];
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
            DoubleStack s = new DoubleStack(p);
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
            DoubleStack s = new DoubleStack(p);
            dequeueAll(s::push);
            s.sortAscending();
            s.popAll(this::enqueue);
        }
    }
    
    public void clear() {
        f = r = p = 0;
    }
    
    public int size() {
        return p;
    }
    
    public int sizeBytes() {
        return p * Double.BYTES;
    }
    
    public int capacity() {
        return q.length;
    }
    
    public boolean isEmpty() {
        return p == 0;
    }
    
}
