package io.github.heathensoft.jlib.ui.text;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import org.joml.Vector4f;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static io.github.heathensoft.jlib.common.utils.Color.hex_to_rgb;

/**
 * @author Frederik Dahl
 * 25/02/2024
 */


public class Word {

    public enum Type {
        T00(Color.hex_to_rgb("BBCDD4FF"),"Regular"),
        T01(Color.hex_to_rgb("83C5CBFF"),"Value"),
        T02(Color.hex_to_rgb("83C5CBFF"),"Value High"),
        T03(Color.hex_to_rgb("83C5CBFF"),"Value Low"),
        T04(Color.hex_to_rgb("D5BE87FF"),"Entity Neutral"),
        T05(Color.hex_to_rgb("8AC789FF"),"Entity Friendly"),
        T06(Color.hex_to_rgb("BF776CFF"),"Entity Hostile"),
        T07(Color.hex_to_rgb("#7b98b0"),"Item Common"),
        T08(Color.hex_to_rgb("D841DBFF"),"Item Rare"),
        T09(Color.hex_to_rgb("FF6D00FF"),"Item Unique"),
        T10(Color.hex_to_rgb("BBCDD4FF"),"Action Neutral"),
        T11(Color.hex_to_rgb("5AC81EFF"),"Action Success"),
        T12(Color.hex_to_rgb("B74545FF"),"Action Failure"),
        T13(Color.hex_to_rgb("9E79D5FF"),"Location"),
        T14(Color.hex_to_rgb("7D7BB0FF"),"Object"),
        T15(Color.hex_to_rgb("CFB857FF"),"Resource");
        private static final Type[] array = values();
        public static final int count = array.length;
        public static Type get(int ordinal) { return array[ordinal]; }
        public final Vector4f color;
        public String name;
        Type(Vector4f color, String name) {
            this.color = color;
            this.name = name;
        } public String toString() {
            String hex = Color.rgb_to_hex(color);
            return "W " + ordinal() + " " + hex + " " + name;
        } public static void toFileFormat(List<String> dst) {
            for (Type type : array) dst.add(type.toString());
        } public static void fromFileFormat(List<String> list) {
            for (String string : list) {
                String[] split = string.trim().split("\\s+");
                if (split.length >= 3 && split[0].equals("W")) {
                    try { int ordinal = Integer.parseInt(split[1]);
                        if (ordinal < count) {
                            String hex = split[2];
                            Color.hex_to_rgb(hex,get(ordinal).color);
                            if (split.length > 3) {
                                get(ordinal).name = split[2];
                            }
                        }
                    }catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
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

    public Type type() { return Type.T00; }
    public byte get(int index) throws ArrayIndexOutOfBoundsException { return value[index]; }
    public byte lastChar() { return value[length() - 1]; }
    public byte firstChar() { return value[0]; }
    public int length() { return value.length; }
    public String toString() { return new String(value); }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Word word) {
            if (word.type() == type())
                return Arrays.equals(value, word.value);
        } return false;
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
