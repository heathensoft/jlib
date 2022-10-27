package io.github.heathensoft.jlib.common.storage.primitive.iterators;

/**
 * @author Frederik Dahl
 * 29/05/2022
 */

@FunctionalInterface
public interface FloatReader2D extends PrimitiveReader2D {
    void next(int x, int y, float value);
}
