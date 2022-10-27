package io.github.heathensoft.jlib.common.storage.generic;

import static io.github.heathensoft.jlib.common.storage.generic.Heap.NOT_ASSIGNED;

/**
 *
 * @author Frederik Dahl
 * 16/07/2022
 */


public interface HeapNode<T> extends Comparable<T> {
    
    /**
     * Should return the assigned heap index.
     * When implementing this interface. This value should always be
     * initialized as -1. (Heap.NOT_ASSIGNED)
     * @return the index into the heap
     */
    int heapIndex();
    
    /**
     * Should set an integer value representing the current heap index,
     * returned by the method @heapIndex();
     * This value should always be initialized as -1 (Heap.NOT_ASSIGNED),
     * for new instances of objects implementing this interface.
     * @param idx the index into the heap
     */
    void assignHeapIndex(int idx);
    
    
    default boolean isAssignedToHeap() {
        return heapIndex() != NOT_ASSIGNED;
    }
}
