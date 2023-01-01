package io.github.heathensoft.jlib.gui.text;

import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class Word {

    private final byte[] value;
    
    public Word(byte[] value) {
        this.value = value;
    }
    
    public byte[] get() {
        return value;
    }
    
    public int length() {
        return value.length;
    }

    public int widthPixels(AsciiFont font, int scale) {
        int width = 0;
        for (byte c : value) {
            width += font.getChar(c).advance();
        } width *= scale;
        return width;
    }
    
    @Override
    public String toString() {
        return new String(value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Arrays.equals(value, word.value);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
    
}
