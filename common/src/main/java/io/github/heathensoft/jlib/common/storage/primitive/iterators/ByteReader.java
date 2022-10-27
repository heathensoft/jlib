package io.github.heathensoft.jlib.common.storage.primitive.iterators;

/**
 * @author Frederik Dahl
 * 29/05/2022
 */

@FunctionalInterface
public interface ByteReader extends PrimitiveReader {
    void next(byte value);
}
