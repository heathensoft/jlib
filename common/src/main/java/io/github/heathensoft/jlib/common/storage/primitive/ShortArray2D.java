package io.github.heathensoft.jlib.common.storage.primitive;


import io.github.heathensoft.jlib.common.storage.primitive.iterators.ShortReader;
import io.github.heathensoft.jlib.common.storage.primitive.iterators.ShortReader2D;

import java.nio.CharBuffer;
import java.nio.ShortBuffer;

/**
 * Two-dimensional array of primitive values.
 *
 * Has some very useful options for iteration and "writing".
 * Writing operation depends on the current "writeFunction".
 *
 * For example:
 * writeFunction.EQU will replace the current value with a new value.
 * writeFunction.SET runs OR bit-operations on values on write.
 * etc.
 *
 * Writing and iteration are also available for subregions of the array.
 *
 * This way you can manipulate the contents however you like, and very easily.
 * Typical use-cases: Heightmap, canvas, texture data etc.
 *
 * @author Frederik Dahl
 * 30/05/2022
 */

public class ShortArray2D extends PrimitiveArray2D implements ReadableShort2D {
    
    protected final short[][] array;
    
    public ShortArray2D(int rows, int cols) {
        this.array = new short[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }
    
    public ShortArray2D(short[][] array2D) {
        if (array2D == null) throw new IllegalArgumentException("array2D == null");
        this.array = array2D;
        this.rows = array2D.length;
        this.cols = array2D[0].length;
    }
    
    @Override
    public int sizeBytes() {
        return size() * Short.BYTES;
    }
    
    /**
     * Iterates through all values in order row by row: 0 -> num cols
     * @param itr iterator
     */
    @Override
    public void read(ShortReader itr) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                itr.next(array[r][c]);
            }
        }
    }
    /**
     * Iterates through all values in order row by row: 0 -> num cols
     * @param itr iterator
     */
    @Override
    public void read(ShortReader2D itr) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                itr.next(c,r, array[r][c]);
            }
        }
    }
    /**
     * Iterates through given row
     * @param itr iterator
     * @param row row
     */
    @Override
    public void readRow(ShortReader itr, int row) {
        if (hasRow(row)) {
            for (int c = 0; c < cols; c++) {
                itr.next(array[row][c]);
            }
        }
    }
    /**
     * Iterates through given colon
     * @param itr iterator
     * @param col col
     */
    @Override
    public void readCol(ShortReader itr, int col) {
        if (hasCol(col)) {
            for (int r = 0; r < rows; r++) {
                itr.next(array[r][col]);
            }
        }
    }
    
    /**
     * Iterate a rectangular portion of array values given by
     * "min / max" coordinates. Does not matter if min > max. Still a rectangle.
     * @param itr iterator
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    @Override
    public void readArea(ShortReader itr, int minX, int minY, int maxX, int maxY) {
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        for (int r = minY; r <= maxY; r++) {
            for (int c = minX; c <= maxX; c++) {
                itr.next(array[r][c]);
            }
        }
    }
    /**
     * Iterate a rectangular portion of array values given by
     * "min / max" coordinates. Does not matter if min > max. Still a rectangle.
     * @param itr iterator
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    @Override
    public void readArea(ShortReader2D itr, int minX, int minY, int maxX, int maxY) {
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        for (int r = minY; r <= maxY; r++) {
            for (int c = minX; c <= maxX; c++) {
                itr.next(c,r, array[r][c]);
            }
        }
    }
    
    /**
     * Writes values stored in buffer to a subregion of the array.
     * Notes:
     * Buffer must be in read mode (flipped).
     * values are "consumed" (uses buffer.get())
     * buffer.remaining() must be >= area of subregion,
     * otherwise, write is not executed.
     * both min and max values are inclusive!
     * @param buffer buffer
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    public void write(ShortBuffer buffer, int minX, int minY, int maxX, int maxY)  {
        if (buffer == null) throw new IllegalArgumentException("buffer == null");
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int size = width * height;
        if (buffer.remaining() < size)
            throw new RuntimeException("buffer remaining won't cover area(width:"+width+",height:"+height+")");
        if (writeFunction == WriteFunction.EQU) {
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = buffer.get();
                }
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = func.calc(array[r][c],buffer.get());
                }
            }
        }
    }
    /**
     * Writes values stored in buffer to the array (row by row).
     * Notes:
     * Buffer must be in read mode (flipped).
     * values are "consumed" (uses buffer.get())
     * buffer.remaining() must be >= array.size(),
     * otherwise, write is not executed.
     * @param buffer buffer
     */
    public void write(ShortBuffer buffer)  {
        if (buffer == null) throw new IllegalArgumentException("buffer == null");
        if (buffer.remaining() < size())
            throw new RuntimeException("buffer remaining won't cover area(width:"+cols+",height:"+rows+")");
        if (writeFunction == WriteFunction.EQU) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    array[r][c] = buffer.get();
                }
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    array[r][c] = func.calc(array[r][c],buffer.get());
                }
            }
        }
    }
    
    /**
     * Writes values to a subregion of the array specified by
     * the min and max values (inclusive).
     * values.size() must be >= area of subregion
     * @param values values
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    public void write(ShortQueue values, int minX, int minY, int maxX, int maxY) {
        if (values == null) throw new IllegalArgumentException("values == null");
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int size = width * height;
        if (values.size() < size)
            throw new RuntimeException("queue size won't cover area(width:"+cols+",height:"+rows+")");
        if (writeFunction == WriteFunction.EQU) {
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = values.dequeue();
                }
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = func.calc(array[r][c],values.dequeue());
                }
            }
        }
    }
    
    /**
     * Writes values to cover the entire area of the array.
     * values.size() must be >= this area.
     * @param values values
     */
    public void write(ShortQueue values) {
        if (values == null) throw new IllegalArgumentException("values == null");
        if (values.size() < size())
            throw new RuntimeException("queue size won't cover area(width:"+cols+",height:"+rows+")");
        if (writeFunction == WriteFunction.EQU) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    array[r][c] = values.dequeue();
                }
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    array[r][c] = func.calc(array[r][c], values.dequeue());
                }
            }
        }
    }
    
    /**
     * Writes values to a subregion of the array specified by
     * the min and max values (inclusive).
     * values.size() must be >= area of subregion
     * @param values values
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    public void write(ShortStack values, int minX, int minY, int maxX, int maxY) {
        if (values == null) throw new IllegalArgumentException("values == null");
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int size = width * height;
        if (values.size() < size)
            throw new RuntimeException("stack size won't cover area(width:"+cols+",height:"+rows+")");
        if (writeFunction == WriteFunction.EQU) {
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = values.pop();
                }
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = func.calc(array[r][c],values.pop());
                }
            }
        }
    }
    
    /**
     * Writes values to cover the entire area of the array.
     * values.size() must be >= this area.
     * @param values values
     */
    public void write(ShortStack values) {
        if (values == null) throw new IllegalArgumentException("values == null");
        if (values.size() < size())
            throw new RuntimeException("stack size won't cover area(width:"+cols+",height:"+rows+")");
        if (writeFunction == WriteFunction.EQU) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    array[r][c] = values.pop();
                }
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    array[r][c] = func.calc(array[r][c], values.pop());
                }
            }
        }
    }
    
    /**
     * Write value argument to provided array coordinates stored as value-pairs in the queue argument.
     * Points outside of bounds will be ignored. Queue elements are consumed.
     * The queue argument size must be dividable by 2.
     * @param points coordinate value-pairs
     * @param value value written to array
     */
    public void write(IntQueue points, short value) {
        if (points == null) throw new IllegalArgumentException("points == null");
        if ((points.size() & 1) == 1)
            throw new RuntimeException("queue size not dividable by 2");
        if (writeFunction == WriteFunction.EQU) {
            while (points.isEmpty()) {
                int x = points.dequeue();
                int y = points.dequeue();
                if (hasRow(y) && hasCol(x))
                    array[y][x] = value;
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            while (points.isEmpty()) {
                int x = points.dequeue();
                int y = points.dequeue();
                if (hasRow(y) && hasCol(x)) {
                    array[y][x] = func.calc(array[y][x],value);
                }
            }
        }
    }
    
    /**
     * Write value argument to provided array coordinates stored as value-pairs in the queue argument.
     * Queue elements are consumed. The queue argument size must be dividable by 2.
     * @param points coordinate value-pairs
     * @param value value written to array
     */
    public void writeUnchecked(IntQueue points, short value) {
        if (points == null) throw new IllegalArgumentException("points == null");
        if ((points.size() & 1) == 1)
            throw new RuntimeException("queue size not dividable by 2");
        if (writeFunction == WriteFunction.EQU) {
            while (points.isEmpty()) {
                int x = points.dequeue();
                int y = points.dequeue();
                array[y][x] = value;
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            while (points.isEmpty()) {
                int x = points.dequeue();
                int y = points.dequeue();
                array[y][x] = func.calc(array[y][x],value);
            }
        }
    }
    
    /**
     * Write value argument to provided array coordinates stored as value-pairs in the stack argument.
     * Points outside of bounds will be ignored. Stack elements are consumed.
     * The stack argument size must be dividable by 2.
     * @param points coordinate value-pairs
     * @param value value written to array
     */
    public void write(IntStack points, short value) {
        if (points == null) throw new IllegalArgumentException("points == null");
        if ((points.size() & 1) == 1)
            throw new RuntimeException("stack size not dividable by 2");
        if (writeFunction == WriteFunction.EQU) {
            while (points.isEmpty()) {
                int x = points.pop();
                int y = points.pop();
                if (hasRow(y) && hasCol(x))
                    array[y][x] = value;
            }
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            while (points.isEmpty()) {
                int x = points.pop();
                int y = points.pop();
                if (hasRow(y) && hasCol(x)) {
                    array[y][x] = func.calc(array[y][x],value);
                }
            }
        }
    }
    
    /**
     * Write value argument to provided array coordinates stored as value-pairs in the stack argument.
     * Stack elements are consumed.The stack argument size must be dividable by 2.
     * @param points coordinate value-pairs
     * @param value value written to array
     */
    public void writeUnchecked(ShortStack points, short value) {
        if (points == null) throw new IllegalArgumentException("points == null");
        if ((points.size() & 1) == 1)
            throw new RuntimeException("stack size not dividable by 2");
        if (writeFunction == WriteFunction.EQU) {
            while (points.isEmpty()) {
                int x = points.pop();
                int y = points.pop();
                array[y][x] = value;}
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            while (points.isEmpty()) {
                int x = points.pop();
                int y = points.pop();
                array[y][x] = func.calc(array[y][x],value);
            }
        }
    }
    
    /**
     * Write array values to this. Arrays must be of same dimensions.
     * @param from source array
     */
    public void write(ShortArray2D from) {
        if (!sameDimension(from)) throw new RuntimeException("array is either null or not of same size as the write target array");
        for (int r = 0; r < rows; r++) writeFunction.write(array[r],from.array[r]);
    }
    
    /**
     * Write array values to this. Arrays must be of same dimensions.
     * min and max values are inclusive and forms the area to write.
     * @param from source array
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    public void write(ShortArray2D from, int minX, int minY, int maxX, int maxY) {
        if (!sameDimension(from)) throw new RuntimeException("array is either null or not of same size as the write target array");
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        if (writeFunction == WriteFunction.EQU) {
            for (int r = minY; r <= maxY; r++)
                for (int c = minX; c <= maxX; c++)
                    array[r][c] = from.array[r][c];
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = func.calc(array[r][c],from.array[r][c]);
                }
            }
        }
    }
    
    /**
     * Write value to all. Arrays must be of same dimensions.
     * min and max values are inclusive and forms the area to write.
     * @param minX p1.x
     * @param minY p1.y
     * @param maxX p2.x
     * @param maxY p2.y
     */
    public void write(short value, int minX, int minY, int maxX, int maxY) {
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        } if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;}
        minX = Math.max(0,minX);
        minY = Math.max(0,minY);
        maxX = Math.min(cols-1,maxX);
        maxY = Math.min(rows-1,maxY);
        if (writeFunction == WriteFunction.EQU) {
            for (int r = minY; r <= maxY; r++)
                for (int c = minX; c <= maxX; c++)
                    array[r][c] = value;
        } else { WriteFunction.ShortFunc func = writeFunction.shortFunc;
            for (int r = minY; r <= maxY; r++) {
                for (int c = minX; c <= maxX; c++) {
                    array[r][c] = func.calc(array[r][c],value);
                }
            }
        }
    }
    
    /**
     * Write value to all.
     * @param value value
     */
    public void write(short value) {
        for (int r = 0; r < rows; r++) {
            writeFunction.write(array[r],value);
        }
    }
    
    /**
     * Write value to coordinate. unchecked.
     * @param value value
     * @param x p.x
     * @param y p.y
     */
    public void write(short value, int x, int y) {
        if (writeFunction == WriteFunction.EQU) array[y][x] = value;
        else array[y][x] = writeFunction.shortFunc.calc(array[y][x],value);
    }
    
    /**
     * Set value of array coordinate to value. unchecked.
     * Equivalent to single write with WriteFunction.EQU. Only this is faster.
     * (grid[y][x] = value)
     * @param value value
     * @param x p.x
     * @param y p.y
     */
    public void set(short value, int x, int y) {
        array[y][x] = value;
    }
    
    /**
     * Get value stored in array at p( row = y, col = x) unchecked;
     * @param x p.x
     * @param y p.y
     * @return value
     */
    public short get(int x, int y) {
        return array[y][x];
    }
    
    public short[][] get() {
        return array;
    }

    public void get(ShortBuffer buffer) {
        for (int r = 0; r < rows; r++) {
            buffer.put(array[r]);
        }
    }
}
