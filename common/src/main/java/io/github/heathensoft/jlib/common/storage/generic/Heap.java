package io.github.heathensoft.jlib.common.storage.generic;


import io.github.heathensoft.jlib.common.Disposable;

/**
 * Heap with 0[1] look up (Indexed nodes). O(log n) as usual for the rest.
 *
 * @author Frederik Dahl
 * 04/06/2022
 */


public class Heap<T extends HeapNode<T>> implements Disposable, Readable<T> {
    
    public static final int NOT_ASSIGNED = -1;
    
    private T[] nodes;
    private int count;
    
    @SuppressWarnings("unchecked")
    public Heap(int cap) {
        nodes = (T[])new HeapNode[nextPowerOfTwo(Math.max(1,cap))];
    }
    
    public Heap() {
        this(16);
    }
    
    public void add(T node) {
        assertNotAssigned(node);
        if (count == nodes.length) grow();
        node.assignHeapIndex(count);
        nodes[count] = node;
        sortUp(count++);
    }
    
    public T pop() {
        T node = nodes[0];
        nodes[0] = nodes[--count];
        nodes[count] = null;
        if (count > 0) { sortDown(0);
        } node.assignHeapIndex(NOT_ASSIGNED);
        return node;
    }
    
    public void remove(T node) {
        assertNotNull(node);
        int index = node.heapIndex();
        if (node == nodes[index]) {
            T moved = nodes[--count];
            nodes[count] = null;
            if (moved != node) {
                nodes[index] = moved;
                sortDown(index);
            } node.assignHeapIndex(NOT_ASSIGNED);
        }
    }
    
    public void update(T node) {
        assertIsNode(node);
        int index = node.heapIndex();
        int parentIndex = (index - 1) >> 1;
        if (parentIndex >= 0) {
            T parent = nodes[parentIndex];
            if (node.compareTo(parent) > 0) {
                sortUp(index);
            } else sortDown(index);
        } else sortDown(index);
    }
    
    public boolean contains(T node) {
        assertNotNull(node);
        int index = node.heapIndex();
        if (index >= nodes.length || index < 0) return false;
        T nodeAtIndex = nodes[index];
        return nodeAtIndex == node;
    }
    
    public T peak() {
        return nodes[0];
    }
    
    public boolean isEmpty () {
        return count == 0;
    }
    
    public boolean notEmpty () {
        return count > 0;
    }
    
    private void sortUp(int index) {
        T node = nodes[index];
        while (index > 0) {
            int parentIndex = (index - 1) >> 1;
            T parent = nodes[parentIndex];
            if (node.compareTo(parent) > 0) {
                nodes[index] = parent;
                parent.assignHeapIndex(index);
                index = parentIndex;
            } else break;
        } nodes[index] = node;
        node.assignHeapIndex(index);
    }
    
    private void sortDown(int index) {
        T node = nodes[index];
        while (true) {
            int leftIndex = 1 + (index << 1);
            if (leftIndex >= count) break;
            int rightIndex = leftIndex + 1;
            T left = nodes[leftIndex];
            if (rightIndex < count) {
                T right = nodes[rightIndex];
                if (right.compareTo(left) > 0) {
                    if (node.compareTo(right) >= 0) break;
                    nodes[index] = right;
                    nodes[rightIndex] = node;
                    right.assignHeapIndex(index);
                    index = rightIndex;
                } else {
                    if (node.compareTo(left) >= 0) break;
                    nodes[index] = left;
                    nodes[leftIndex] = node;
                    left.assignHeapIndex(index);
                    index = leftIndex;
                }
            } else {
                if (node.compareTo(left) >= 0) break;
                nodes[index] = left;
                nodes[leftIndex] = node;
                left.assignHeapIndex(index);
                index = leftIndex;
            }
        }
        nodes[index] = node;
        node.assignHeapIndex(index);
    }
    
    private void assertNotAssigned(T node) {
        if (node == null || node.isAssignedToHeap())
            throw new IllegalStateException("node == null OR already assigned (to this or another a heap)");
    }
    
    private void assertNotNull(T node) {
        if (node == null) throw new IllegalStateException("node == null");
    }
    
    private void assertIsNode(T node) {
        if (node == null || node != nodes[node.heapIndex()]) throw new IllegalStateException("node == null OR not in heap");
    }
    
    @SuppressWarnings("unchecked")
    private void grow() {
        T[] tmp = nodes;
        nodes = (T[])new HeapNode[count << 1];
        System.arraycopy(tmp, 0, nodes, 0, count);
    }
    
    private int nextPowerOfTwo(int value) {
        if (value-- == 0) return 1;
        value |= value >>> 1;
        value |= value >>> 2;
        value |= value >>> 4;
        value |= value >>> 8;
        value |= value >>> 16;
        return value + 1;
    }
    
    @Override
    public void dispose() {
        for (int i = 0; i < count; i++) {
            T node = nodes[i];
            node.assignHeapIndex(NOT_ASSIGNED);
            nodes[i] = null;
            if (node instanceof Disposable) {
                ((Disposable)node).dispose();
            }
        } count = 0;
    }
    
    /**
     * Not in order.
     * @param reader reader
     */
    @Override
    public void read(Reader<T> reader) {
        for (int i = 0; i < count; i++) {
            reader.next(nodes[i]);
        }
    }
    
    /**
     * Not in order.
     * @param collector collector
     */
    @Override
    public void collect(Reader<T> collector) {
        for (int i = 0; i < count; i++) {
            T node = nodes[i];
            nodes[i] = null;
            node.assignHeapIndex(NOT_ASSIGNED);
            collector.next(node);
        } count = 0;
    }
    
}
