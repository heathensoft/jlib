package io.github.heathensoft.jlib.gui.text;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class Word  {

    private final byte[] value;

    protected Word(String string) {
        this(string.getBytes(StandardCharsets.US_ASCII));
    }
    
    protected Word(byte[] value) {
        this.value = value;
    }
    
    protected byte[] get() {
        return value;
    }

    public byte get(int idx) throws ArrayIndexOutOfBoundsException {
        return value[idx];
    }

    public byte lastChar() {
        return value[length() - 1];
    }

    public byte firstChar() {
        return value[0];
    }

    public int length() {
        return value.length;
    }

    public String toString() {
        return new String(value);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Arrays.equals(value, word.value);
    }

    public int hashCode() {
        return Arrays.hashCode(value);
    }
    
}
