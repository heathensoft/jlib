package io.github.heathensoft.jlib.gui.ny.text;

import io.github.heathensoft.jlib.gui.ny.GUI;
import io.github.heathensoft.jlib.gui.ny.gfx.FontsGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import org.joml.Vector4f;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 25/02/2024
 */


public class Word {

    public enum Type {
        REGULAR(        Color.hex_to_rgb("A9B7C6FF",new Vector4f()),"Regular"),
        VALUE(          Color.hex_to_rgb("0000BBFF",new Vector4f()),"Value"),
        KEYWORD(        Color.hex_to_rgb("CC7832FF",new Vector4f()),"Keyword"),
        INLINE_COMMENT( Color.hex_to_rgb("808080FF",new Vector4f()),"Inline Comment"),
        ENTITY_PLAYER(  Color.hex_to_rgb("9876AAFF",new Vector4f()),"Player Entity"),
        ENTITY_OTHER(   Color.hex_to_rgb("9876AAFF",new Vector4f()),"Other Entity"),
        LOCATION(       Color.hex_to_rgb("9876AAFF",new Vector4f()),"Location"),
        OBJECT(         Color.hex_to_rgb("9876AAFF",new Vector4f()),"Object"),
        ITEM(           Color.hex_to_rgb("9876AAFF",new Vector4f()),"Item"),
        ACTION(         Color.hex_to_rgb("FFC66DFF",new Vector4f()),"Action"),
        SUCCESS(        Color.hex_to_rgb("77B767FF",new Vector4f()),"Success"),
        FAILURE(        Color.hex_to_rgb("FF0000FF",new Vector4f()),"Failure");
        public final String name;
        public final Vector4f color;
        Type(Vector4f color, String name) {
            this.color = color;
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

    public Word.Type type() { return Word.Type.REGULAR; }
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

    public float width() { // Unscaled width pixels
        FontsGUI boundFont = GUI.fonts;
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
