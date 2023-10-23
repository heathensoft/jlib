package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.gui.gfx.Fonts;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 18/10/2023
 */


public class Word {

    public enum Type {
        REGULAR(0,"Regular"),
        VALUE(1,"Value"),
        KEYWORD(2,"Keyword"),
        INLINE_COMMENT(3,"Inline Comment"),
        ENTITY_PLAYER(4,"Player Entity"),
        ENTITY_OTHER(5,"Other Entity"),
        LOCATION(6,"Location"),
        OBJECT(7,"Object"),
        ITEM(8,"Item"),
        ACTION(9,"Action"),
        SUCCESS(10,"Success"),
        FAILURE(11,"Failure");
        public final String name;
        public final int colorIndex;
        Type(int colorIndex, String name) {
            this.colorIndex = colorIndex;
            this.name = name;
        }
    }

    private byte[] value;

    protected Word(String string) {
        this(string.getBytes(StandardCharsets.US_ASCII));
    }

    protected Word(byte[] value) {
        this.value = value;
    }

    protected Word(byte c) { this(new byte[]{c}); }

    public Type type() { return Type.REGULAR; }

    public byte get(int index) throws ArrayIndexOutOfBoundsException { return value[index]; }

    public byte lastChar() { return value[length() - 1]; }

    public byte firstChar() { return value[0]; }

    public int length() { return value.length; }

    public String toString() { return new String(value); }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Arrays.equals(value, word.value);
    }

    public int hashCode() { return Arrays.hashCode(value); }

    public float widthPixels(Fonts boundFont) {
        if (boundFont.isMonospaced()) {
            float advance = boundFont.advance('x');
            return length() * advance;
        } float width = 0;
        for (int i = 0; i < length(); i++) {
            width += boundFont.advance((char)value[i]);
        } return width;
    }

    protected byte[] get() {
        return value;
    }

    protected void insert(byte c, int index) throws ArrayIndexOutOfBoundsException {
        byte[] bytes = new byte[length() + 1];
        bytes[index] = c;
        System.arraycopy(value, 0, bytes, 0, index);
        if (index < value.length) {
            if (bytes.length - (index + 1) >= 0)
                System.arraycopy(value,index + 1 - 1, bytes,index + 1,bytes.length - (index + 1));
        } value = bytes;
    }

    protected void remove(int index) throws ArrayIndexOutOfBoundsException {
        byte[] bytes = new byte[value.length - 1];
        if (index >= 0) System.arraycopy(value,0, bytes,0, index);
        if (value.length - (index + 1) >= 0)
            System.arraycopy(value,index + 1, bytes,index + 1 - 1,value.length - (index + 1));
        value = bytes;
    }

    protected void set(byte[] value) { this.value = value; }

    protected void append(byte c) {
        byte[] bytes = new byte[length() + 1];
        bytes[length()] = c;
        System.arraycopy(value, 0, bytes, 0, value.length);
        value = bytes;
    }

    protected void append(Word word) {
        byte[] w1 = value;
        byte[] w2 = word.value;
        byte[] n = new byte[w1.length + w2.length];
        System.arraycopy(w1, 0, n, 0, w1.length);
        System.arraycopy(w2, 0, n, w1.length, w2.length);
        value = n;
    }

    protected Word[] split(int index) throws ArrayIndexOutOfBoundsException {
        if (index == 0 || index == length()) {
            return new Word[] { this };
        } Word[] divided = new Word[2];
        byte[] wi = new byte[index];
        byte[] w2 = new byte[length() - index];
        System.arraycopy(value, 0, wi, 0, wi.length);
        System.arraycopy(value, index, w2, 0, w2.length);
        if (this instanceof Keyword) {
            divided[0] = new Keyword(wi,type());
            divided[1] = new Keyword(w2,type());
        } else { divided[0] = new Word(wi);
            divided[1] = new Word(w2);
        } return divided;
    }

    protected Word copy() {
        byte[] bytes = Arrays.copyOf(value,value.length);
        if (this instanceof Keyword word) {
            return new Keyword(bytes,word.type());
        } else return new Word(bytes);
    }

    protected void trim() {
        if (length() > 0) {
            String string = toString().trim();
            value = string.getBytes(StandardCharsets.US_ASCII);
        }
    }
}
