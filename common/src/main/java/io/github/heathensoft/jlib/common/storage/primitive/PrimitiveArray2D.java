package io.github.heathensoft.jlib.common.storage.primitive;

/**
 * @author Frederik Dahl
 * 31/05/2022
 */


public abstract class PrimitiveArray2D {
    
    protected WriteFunction writeFunction = WriteFunction.EQU;
    
    protected int rows;
    protected int cols;
    
    public int rows() {
        return rows;
    }
    
    public int cols() {
        return cols;
    }
    
    /**
     * @return rows * cols
     */
    public int size() {
        return rows * cols;
    }
    
    /**
     * @return Byte-size of the 2D array of primitives
     */
    public abstract int sizeBytes();
    
    /**
     * Set the function for write operations
     * @param enumerator WriteFunction
     */
    public void setWriteFunction(WriteFunction enumerator) {
        this.writeFunction = enumerator;
    }
    
    /**
     * Get the function for write operations
     * @return WriteFunction
     */
    public WriteFunction writeFunction() {
        return writeFunction;
    }
    
    protected boolean hasRow(int row) {
        return (row >= 0 && row < rows);
    }
    
    protected boolean hasCol(int col) {
        return (col >= 0 && col < rows);
    }
    
    protected boolean sameDimension(PrimitiveArray2D other) {
        return other != null && rows == other.rows && cols == other.cols;
    }
    
}
