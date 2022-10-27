package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.PrimitiveReader;

/**
 * @author Frederik Dahl
 * 29/05/2022
 */

interface ReadablePrimitive<I extends PrimitiveReader> {
    void read(I itr);
}
