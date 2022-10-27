
package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.CharReader;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple auto-growing stack structure for primitives.
 *
 * @author Frederik Dahl
 * 29/05/2022
 */

public class CharStack implements ReadableChar {
	
	private char[] s;
	private int p;
	
	/**
	 * @param cap Initial capacity
	 */
	public CharStack(int cap) {
		if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
		s = new char[cap];
	}
	
	public CharStack() {
		this(0);
	}
	
	@Override
	public void read(CharReader itr) {
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
			char[] tmp = s;
			s = new char[size];
			System.arraycopy(tmp,0, s,0,p);
		}
	}
	
	/**
	 * If the stack has reached its capacity, the underlying array will double in size
	 * @param c the value to stack on top
	 */
	public void push(char c) {
		if (p == this.s.length) {
			char[] tmp = this.s;
			this.s = new char[this.s.length * 2 + 1];
			System.arraycopy(tmp,0, this.s,0,tmp.length);
		} this.s[p++] = c;
	}
	
	public void push(char[] s) {
		if (s != null) { makeRoom(s.length);
			for (char v : s) s[p++] = v;
		}
	}
	
	public void push(Collection<Character> c) {
		if (c != null) { makeRoom(c.size());
			for (Character s : c) this.s[p++] = s;
		}
	}
	
	/**
	 * Does not check for out of bounds. Use with: isEmpty()
	 * @return top element value
	 */
	public char pop() {
		return s[--p];
	}
	
	public void popAll(CharReader itr) {
		while (p > 0) itr.next(pop());
	}
	
	/**
	 * Fit underlying array to match its size.
	 */
	public void fit(int min) {
		int cap = Math.max(Math.max(0,min),p);
		char[] tmp = s;
		s = new char[cap];
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
			CharStack tmp = new CharStack(capacity());
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
		return p * Character.BYTES;
	}
	
	public int capacity() {
		return s.length;
	}
	
	public boolean isEmpty() {
		return p == 0;
	}
	
}