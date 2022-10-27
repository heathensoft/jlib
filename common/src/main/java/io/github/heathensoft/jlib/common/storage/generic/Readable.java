package io.github.heathensoft.jlib.common.storage.generic;

/**
 * Classes implementing Readable, implements two methods of traversal:
 *
 * read() iterates all non-null items.
 * collect() iterates all non-null items, and removes them.
 *
 * Readers can be created anonymously (lambda), but for continuous
 * iteration it's recommended to create and use the same reader.
 *
 * @author Frederik Dahl
 * 03/06/2022
 */


public interface Readable<E> {
    
    /**
     * unconditionally reads all non-null items of array
     * @param reader reader
     */
    void read(Reader<E> reader);
    /**
     * unconditionally collects all non-null items of array (remove -> get)
     * @param collector collector
     */
    void collect(Reader<E> collector);
}
