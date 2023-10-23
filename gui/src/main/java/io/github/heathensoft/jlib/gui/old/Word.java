package io.github.heathensoft.jlib.gui.old;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *
 * @author Frederik Dahl
 * 16/11/2022
 */


public class Word  {

    public static EOL END_OF_LINE() {
        return EOL.singleton;
    }

    public static final class EOL extends Word {
        static EOL singleton = new EOL();
        private EOL() { super("\n"); }
    }

    private final byte[] value;

    protected Word(String string) {
        this(string.getBytes(StandardCharsets.US_ASCII));
    }

    protected Word(byte c) { this(new byte[]{c}); }
    
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

    public boolean isEndOfLine() { return this instanceof EOL; }

    public boolean isRegularWord() {
        return this.getClass().equals(Word.class);
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
