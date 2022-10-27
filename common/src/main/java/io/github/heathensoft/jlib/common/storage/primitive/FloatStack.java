
package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.FloatReader;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple auto-growing stack structure for primitives.
 *
 * @author Frederik Dahl
 * 29/05/2022
 */

public class FloatStack implements ReadableFloat {
	
	private float[] s;
	private int p;
	
	/**
	 * @param cap Initial capacity
	 */
	public FloatStack(int cap) {
		if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
		s = new float[cap];
	}
	
	public FloatStack() {
		this(0);
	}
	
	@Override
	public void read(FloatReader itr) {
		for (int i = 0; i < p; i++) itr.next(s[i]);
	}
	
	/**
	 * Ensure space for n additional elements. This is useful before pushing, as the underlying
	 * array would only need to "grow" once instead of potentially multiple times.
	 * @param n additional elements
	 */
	public void makeRoom(int n) {
		ensureCapacity(n + p);
	}
	
	public void ensureCapacity(int size) {
		if (size > s.length) {
			float[] tmp = s;
			s = new float[size];
			System.arraycopy(tmp,0, s,0,p);
		}
	}
	
	/**
	 * If the stack has reached its capacity, the underlying array will double in size
	 * @param f the value to stack on top
	 */
	public void push(float f) {
		if (p == s.length) {
			float[] tmp = s;
			s = new float[s.length * 2 + 1];
			System.arraycopy(tmp,0, s,0,tmp.length);
		} s[p++] = f;
	}
	
	public void push(float[] f) {
		if (f != null) { makeRoom(f.length);
			for (float v : f) s[p++] = v;
		}
	}
	
	public void push(Collection<Float> c) {
		if (c != null) { makeRoom(c.size());
			for (Float f : c) s[p++] = f;
		}
	}
	
	/**
	 * Does not check for out of bounds. Use with: isEmpty()
	 * @return top element value
	 */
	public float pop() {
		return s[--p];
	}
	
	public void popAll(FloatReader itr) {
		while (p > 0) itr.next(pop());
	}
	
	/**
	 * Fit underlying array to match its size.
	 */
	public void fit(int min) {
		int cap = Math.max(Math.max(0,min),p);
		float[] tmp = s;
		s = new float[cap];
		System.arraycopy(tmp,0, s,0, p);
	}
	
	/**
	 * Values are sorted to pop highest values
	 */
	public void sortDescending() {
		if (p > 1) {
			if (p > 10000) Arrays.parallelSort(s,0, p);
			else Arrays.sort(s,0, p);
		}
	}
	
	/**
	 * Values are sorted to pop lowest values
	 */
	public void sortAscending() {
		if (p > 1) {
			if (p > 10000) Arrays.parallelSort(s,0, p);
			else Arrays.sort(s,0, p);
			FloatStack tmp = new FloatStack(capacity());
			popAll(tmp::push);
			this.p = tmp.p;
			this.s = tmp.s;
		}
	}
	
	public void clear() {
		p = 0;
	}
	
	public int size() {
		return p;
	}
	
	public int sizeBytes() {
		return p * Float.BYTES;
	}
	
	public int capacity() {
		return s.length;
	}
	
	public boolean isEmpty() {
		return p == 0;
	}
	
}