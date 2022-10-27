
package io.github.heathensoft.jlib.common.storage.primitive;



import io.github.heathensoft.jlib.common.storage.primitive.iterators.ByteReader;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple auto-growing stack structure for primitives.
 *
 * @author Frederik Dahl
 * 29/05/2022
 */

public class ByteStack implements ReadableByte {
	
	private byte[] s;
	private int p;
	
	/**
	 * @param cap Initial capacity
	 */
	public ByteStack(int cap) {
		if (cap < 0) throw new NegativeArraySizeException("cap < 0: " + cap);
		s = new byte[cap];
	}
	
	public ByteStack() {
		this(0);
	}
	
	@Override
	public void read(ByteReader itr) {
		for (int i = 0; i < p; i++) itr.next(s[i]);
	}
	
	/**
	 * Ensures space for n additional elements. This is useful before pushing, as the underlying
	 * array would only need to "grow" once instead of potentially multiple times.
	 * @param n additional elements
	 */
	public void makeRoom(int n) {
		ensureCapacity(n + p);
	}
	
	public void ensureCapacity(int size) {
		if (size > s.length) {
			byte[] tmp = s;
			s = new byte[size];
			System.arraycopy(tmp,0, s,0,p);
		}
	}
	
	/**
	 * If the stack has reached its capacity, the underlying array will double in size
	 * @param b the value to stack on top
	 */
	public void push(byte b) {
		if (p == s.length) {
			byte[] tmp = s;
			s = new byte[s.length * 2 + 1];
			System.arraycopy(tmp,0, s,0,tmp.length);
		} s[p++] = b;
	}
	
	public void push(byte[] b) {
		if (b != null) { makeRoom(b.length);
			for (byte v : b) s[p++] = v;
		}
	}
	
	public void push(Collection<Byte> c) {
		if (c != null) { makeRoom(c.size());
			for (Byte b : c) s[p++] = b;
		}
	}
	
	/**
	 * Does not check for out of bounds. Use with: isEmpty()
	 * @return top element value
	 */
	public byte pop() {
		return s[--p];
	}
	
	public void popAll(ByteReader itr) {
		while (p > 0) itr.next(pop());
	}
	
	/**
	 * Fit underlying array to match its size.
	 */
	public void fit(int min) {
		int cap = Math.max(Math.max(0,min),p);
		byte[] tmp = s;
		s = new byte[cap];
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
			ByteStack tmp = new ByteStack(capacity());
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
		return p;
	}
	
	public int capacity() {
		return s.length;
	}
	
	public boolean isEmpty() {
		return p == 0;
	}
	
}