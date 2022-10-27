package io.github.heathensoft.jlib.common.storage.generic;

/**
 * Interface used to unconditionally read ALL (non-null) items of a container.
 * This is faster than the java Iterator for this purpose.
 *
 * @author Frederik Dahl
 * 03/06/2022
 */


@FunctionalInterface
public interface Reader<E> {
    /**
     * For all implementations in the "storage" package:
     * Passed items are never null.
     * @param item passed item
     */
    void next(E item);
    
}
