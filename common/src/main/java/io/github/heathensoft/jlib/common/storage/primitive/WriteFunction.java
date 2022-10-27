package io.github.heathensoft.jlib.common.storage.primitive;


/**
 * @author Frederik Dahl
 * 30/05/2022
 */


public enum WriteFunction {
    /** o = i2 */
    EQU(new ByteFuncEQU(),new ShortFuncEQU(),new CharFuncEQU(),new IntFuncEQU(),new LongFuncEQU(),new FloatFuncEQU(),new DoubleFuncEQU()),
    /** o = i1 + i2 */
    ADD(new ByteFuncADD(),new ShortFuncADD(),new CharFuncADD(),new IntFuncADD(),new LongFuncADD(),new FloatFuncADD(),new DoubleFuncADD()),
    /** o = i1 - i2 */
    SUB(new ByteFuncSUB(),new ShortFuncSUB(),new CharFuncSUB(),new IntFuncSUB(),new LongFuncSUB(),new FloatFuncSUB(),new DoubleFuncSUB()),
    /** o = i1 * i2 */
    MUL(new ByteFuncMUL(),new ShortFuncMUL(),new CharFuncMUL(),new IntFuncMUL(),new LongFuncMUL(),new FloatFuncMUL(),new DoubleFuncMUL()),
    /**  o = i1 / i2 (Be careful with this one)*/
    DIV(new ByteFuncDIV(),new ShortFuncDIV(),new CharFuncDIV(),new IntFuncDIV(),new LongFuncDIV(),new FloatFuncDIV(),new DoubleFuncDIV()),
    /** o = i1 | i2 */
    SET(new ByteFuncSET(),new ShortFuncSET(),new CharFuncSET(),new IntFuncSET(),new LongFuncSET(),new FloatFuncSET(),new DoubleFuncSET()),
    /** o = i1 & i2 */
    AND(new ByteFuncAND(),new ShortFuncAND(),new CharFuncAND(),new IntFuncAND(),new LongFuncAND(),new FloatFuncAND(),new DoubleFuncAND()),
    /** o = i1 & ~i2 */
    CLR(new ByteFuncCLR(),new ShortFuncCLR(),new CharFuncCLR(),new IntFuncCLR(),new LongFuncCLR(),new FloatFuncCLR(),new DoubleFuncCLR());
    
    WriteFunction(ByteFunc byteFunc, ShortFunc shortFunc, CharFunc charFunc, IntFunc intFunc, LongFunc longFunc, FloatFunc floatFunc, DoubleFunc doubleFunc) {
        this.byteFunc = byteFunc;
        this.shortFunc = shortFunc;
        this.charFunc = charFunc;
        this.intFunc = intFunc;
        this.longFunc = longFunc;
        this.floatFunc = floatFunc;
        this.doubleFunc = doubleFunc;
    }
    
    protected interface ByteFunc { byte calc(byte val1, byte val2); }
    protected interface ShortFunc { short calc(short val1, short val2); }
    protected interface CharFunc { char calc(char val1, char val2); }
    protected interface IntFunc { int calc(int val1, int val2); }
    protected interface LongFunc { long calc(long val1, long val2); }
    protected interface FloatFunc { float calc(float val1, float val2); }
    protected interface DoubleFunc { double calc(double val1, double val2); }
    
    public final ByteFunc byteFunc;
    public final ShortFunc shortFunc;
    public final CharFunc charFunc;
    public final IntFunc intFunc;
    public final LongFunc longFunc;
    public final FloatFunc floatFunc;
    public final DoubleFunc doubleFunc;
    
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(byte[] dest, byte val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = byteFunc.calc(dest[i],val);
                }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(byte[] dest, byte val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(byte[] dest, byte[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = byteFunc.calc(dest[i],src[i]);
                }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(byte[] dest, byte[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(short[] dest, short val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = shortFunc.calc(dest[i],val);
                }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(short[] dest, short val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(short[] dest, short[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = shortFunc.calc(dest[i],src[i]);
                }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(short[] dest, short[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(char[] dest, char val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                dest[i] = charFunc.calc(dest[i],val);
            }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(char[] dest, char val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(char[] dest, char[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                dest[i] = charFunc.calc(dest[i],src[i]);
            }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(char[] dest, char[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(int[] dest, int val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = intFunc.calc(dest[i],val);
                }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(int[] dest, int val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(int[] dest, int[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = intFunc.calc(dest[i],src[i]);
                }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(int[] dest, int[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(long[] dest, long val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = longFunc.calc(dest[i],val);
                }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(long[] dest, long val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(long[] dest, long[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = longFunc.calc(dest[i],src[i]);
                }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(long[] dest, long[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(float[] dest, float val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = floatFunc.calc(dest[i],val);
                }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(float[] dest, float val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(float[] dest, float[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = floatFunc.calc(dest[i],src[i]);
                }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(float[] dest, float[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(double[] dest, double val, int fromIndex, int toIndex) {
        if (dest != null) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] = val;
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = doubleFunc.calc(dest[i],val);
                }
            }
        }
    }
    /**
     * Writes value to array. The result depends on the WriteFunction used.
     * No checks for out of bounds.
     * @param dest array to be manipulated
     * @param val value written to array
     */
    public final void write(double[] dest, double val) {
        if (dest != null) write(dest,val,0,dest.length);
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     * @param fromIndex start index inclusive
     * @param toIndex stop index exclusive
     */
    public final void write(double[] dest, double[] src, int fromIndex, int toIndex) {
        if (dest != null && src != null && dest.length == src.length) {
            if (this == EQU) {
                for (int i = fromIndex; i < toIndex; i++) dest[i] =src[i];
            } else { for (int i = fromIndex; i < toIndex; i++) {
                    dest[i] = doubleFunc.calc(dest[i],src[i]);
                }
            }
        }
    }
    /**
     * Writes values from one array to another. The resulting values depends on the WriteFunction used.
     * No checks for out of bounds. Note: Arrays must be of equal length.
     * @param dest array to be written to
     * @param src array to be written from
     */
    public final void write(double[] dest, double[] src) {
        if (dest != null) write(dest,src,0,dest.length);
    }
    
    private static final class ByteFuncEQU implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return val2;
        }
    }
    private static final class ByteFuncADD implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 + val2);
        }
    }
    private static final class ByteFuncSUB implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 - val2);
        }
    }
    private static final class ByteFuncMUL implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 * val2);
        }
    }
    private static final class ByteFuncDIV implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 / val2);
        }
    }
    private static final class ByteFuncSET implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 | val2);
        }
    }
    private static final class ByteFuncAND implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 & val2);
        }
    }
    private static final class ByteFuncCLR implements ByteFunc {
        @Override public byte calc(byte val1, byte val2) {
            return (byte) (val1 & ~val2);
        }
    }
    public static final class ShortFuncEQU implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return val2;
        }
    }
    public static final class ShortFuncADD implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 + val2);
        }
    }
    public static final class ShortFuncSUB implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 - val2);
        }
    }
    public static final class ShortFuncMUL implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 * val2);
        }
    }
    public static final class ShortFuncDIV implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 / val2);
        }
    }
    public static final class ShortFuncSET implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 | val2);
        }
    }
    public static final class ShortFuncAND implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 & val2);
        }
    }
    public static final class ShortFuncCLR implements ShortFunc {
        @Override public short calc(short val1, short val2) {
            return (short) (val1 & ~val2);
        }
    }
    private static final class CharFuncEQU implements CharFunc {
        @Override public char calc(char val1, char val2) {
            return val2;
        }
    }
    private static final class CharFuncADD implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 + val2); }
    }
    private static final class CharFuncSUB implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 - val2); }
    }
    private static final class CharFuncMUL implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 * val2); }
    }
    private static final class CharFuncDIV implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 / val2); }
    }
    private static final class CharFuncSET implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 | val2); }
    }
    private static final class CharFuncAND implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 & val2); }
    }
    private static final class CharFuncCLR implements CharFunc {
        @Override public char calc(char val1, char val2) { return (char) (val1 & ~val2); }
    }
    public static final class IntFuncEQU implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return val2;
        }
    }
    public static final class IntFuncADD implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 + val2);
        }
    }
    public static final class IntFuncSUB implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 - val2);
        }
    }
    public static final class IntFuncMUL implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 * val2);
        }
    }
    public static final class IntFuncDIV implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 / val2);
        }
    }
    public static final class IntFuncSET implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 | val2);
        }
    }
    public static final class IntFuncAND implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 & val2);
        }
    }
    public static final class IntFuncCLR implements IntFunc {
        @Override public int calc(int val1, int val2) {
            return (int) (val1 & ~val2);
        }
    }
    public static final class LongFuncEQU implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return val2;
        }
    }
    public static final class LongFuncADD implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 + val2);
        }
    }
    public static final class LongFuncSUB implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 - val2);
        }
    }
    public static final class LongFuncMUL implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 * val2);
        }
    }
    public static final class LongFuncDIV implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 / val2);
        }
    }
    public static final class LongFuncSET implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 | val2);
        }
    }
    public static final class LongFuncAND implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 & val2);
        }
    }
    public static final class LongFuncCLR implements LongFunc {
        @Override public long calc(long val1, long val2) {
            return (long) (val1 & ~val2);
        }
    }
    public static final class FloatFuncEQU implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return val2;
        }
    }
    public static final class FloatFuncADD implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) (val1 + val2);
        }
    }
    public static final class FloatFuncSUB implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) (val1 - val2);
        }
    }
    public static final class FloatFuncMUL implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) (val1 * val2);
        }
    }
    public static final class FloatFuncDIV implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) (val1 / val2);
        }
    }
    public static final class FloatFuncSET implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) ((int)val1 | (int) val2);
        }
    }
    public static final class FloatFuncAND implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) ((int)val1 & (int) val2);
        }
    }
    public static final class FloatFuncCLR implements FloatFunc {
        @Override public float calc(float val1, float val2) {
            return (float) ((int)val1 & ~((int) val2));
        }
    }
    public static final class DoubleFuncEQU implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return val2;
        }
    }
    public static final class DoubleFuncADD implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) (val1 + val2);
        }
    }
    public static final class DoubleFuncSUB implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) (val1 - val2);
        }
    }
    public static final class DoubleFuncMUL implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) (val1 * val2);
        }
    }
    public static final class DoubleFuncDIV implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) (val1 / val2);
        }
    }
    public static final class DoubleFuncSET implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) ((long)val1 | (long) val2);
        }
    }
    public static final class DoubleFuncAND implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) ((long)val1 & (long) val2);
        }
    }
    public static final class DoubleFuncCLR implements DoubleFunc {
        @Override public double calc(double val1, double val2) {
            return (double) ((long)val1 & ~((long) val2));
        }
    }
}
