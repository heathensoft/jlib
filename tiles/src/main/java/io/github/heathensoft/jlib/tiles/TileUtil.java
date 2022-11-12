package io.github.heathensoft.jlib.tiles;

/**
 * @author Frederik Dahl
 * 06/11/2022
 */


public class TileUtil {
    
    private static final int[] logTable;
    
    static {
        logTable = new int[256];
        logTable[0] = logTable[1] = 0;
        for (int i=2; i<256; i++) logTable[i] = 1 + logTable[i/2];
        logTable[0] = -1;
    }
    
    public static int nextPowerOfTwo(int value) {
        if (value-- == 0) return 1;
        value |= value >>> 1;
        value |= value >>> 2;
        value |= value >>> 4;
        value |= value >>> 8;
        value |= value >>> 16;
        return value + 1;
    }
    
    public static int log2(float f) { // Not mine
        int x = Float.floatToIntBits(f);
        int c = x >> 23;
        if (c != 0) return c - 127; //Compute directly from exponent.
        else { //Subnormal, must compute from mantissa.
            int t = x >> 16;
            if (t != 0) return logTable[t] - 133;
            else return (x >> 8 != 0) ? logTable[t] - 141 : logTable[x] - 149;
        }
    }
    
    public static final int[][] adjacent8 = {
            {-1,-1},{-1, 0},{-1, 1},
            { 0,-1}        ,{ 0, 1},
            { 1,-1},{ 1, 0},{ 1, 1}
    };
    
    public static final int[][] adjacent9 = {
            {-1,-1},{-1, 0},{-1, 1},
            { 0,-1},{ 0, 0},{ 0, 1},
            { 1,-1},{ 1, 0},{ 1, 1}
    };
    
    public static final int[][] adjacent5 = new int[][] {
            {-1, 0},{ 0,-1},{ 0, 1},{ 1, 0},{ 0, 0}
    };
    
    public static final int[][] adjacent4 = new int[][] {
            {-1, 0},{ 0,-1},{ 0, 1},{ 1, 0}
    };
    
    private int mask8_to_mask4(int mask8) {
        int mask4 = 0;
        final int bit_convert = mask8 & 0b1011010;
        if ((bit_convert & 64) > 0) mask4 += 8;
        if ((bit_convert & 16) > 0) mask4 += 4;
        if ((bit_convert & 8) > 0) mask4 += 2;
        if ((bit_convert & 2) > 0) mask4 += 1;
        return mask4;
    }
}
