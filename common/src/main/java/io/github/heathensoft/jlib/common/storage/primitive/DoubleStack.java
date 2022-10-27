
package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.DoubleReader;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple auto-growing stack structure for primitives.
 *
 * @author Frederik Dahl
 * 29/05/2022
 */

public class DoubleStack implements ReadableDouble {
	
	private double[] s;
	private int p;
	
	/**
	 * @param cap Initial capacity
	 */
	public DoubleStack(int cap) {
		if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
		s = new double[cap];
	}
	
	public DoubleStack() {
		this(0);
	}
	
	@Override
	public void read(DoubleReader itr) {
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
			double[] tmp = s;
			s = new double[size];
			System.arraycopy(tmp,0, s,0,p);
		}
	}
	
	/**
	 * If the stack has reached its capacity, the underlying array will double in size
	 * @param d the value to stack on top
	 */
	public void push(double d) {
		if (p == s.length) {
			double[] tmp = s;
			s = new double[s.length * 2 + 1];
			System.arraycopy(tmp,0, s,0,tmp.length);
		} s[p++] = d;
	}
	
	public void push(double[] d) {
		if (d != null) { makeRoom(d.length);
			for (double v : d) s[p++] = v;
		}
	}
	
	public void push(Collection<Double> c) {
		if (c != null) { makeRoom(c.size());
			for (Double d : c) s[p++] = d;
		}
	}
	
	/**
	 * Does not check for out of bounds. Use with: isEmpty()
	 * @return top element value
	 */
	public double pop() {
		return s[--p];
	}
	
	public void popAll(DoubleReader itr) {
		while (p > 0) itr.next(pop());
	}
	
	/**
	 * Fit underlying array to match its size.
	 */
	public void fit(int min) {
		int cap = Math.max(Math.max(0,min),p);
		double[] tmp = s;
		s = new double[cap];
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
			DoubleStack tmp = new DoubleStack(capacity());
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
		return p * Double.BYTES;
	}
	
	public int capacity() {
		return s.length;
	}
	
	public boolean isEmpty() {
		return p == 0;
	}
	
}