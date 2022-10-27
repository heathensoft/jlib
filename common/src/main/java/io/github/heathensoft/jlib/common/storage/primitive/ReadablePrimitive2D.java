package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.PrimitiveReader;
import io.github.heathensoft.jlib.common.storage.primitive.iterators.PrimitiveReader2D;

/**
 * @author Frederik Dahl
 * 29/05/2022
 */


interface ReadablePrimitive2D<I1 extends PrimitiveReader, I2 extends PrimitiveReader2D> extends ReadablePrimitive<I1> {
    void read(I2 itr);
    void readRow(I1 itr, int row);
    void readCol(I1 itr, int col);
    void readArea(I1 itr, int minX, int minY, int maxX, int maxY);
    void readArea(I2 itr, int minX, int minY, int maxX, int maxY);
}
