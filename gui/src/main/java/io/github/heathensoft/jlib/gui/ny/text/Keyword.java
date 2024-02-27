package io.github.heathensoft.jlib.gui.ny.text;

/**
 * @author Frederik Dahl
 * 25/02/2024
 */


public class Keyword extends Word {

    private Word.Type type;

    protected Keyword(String string, Word.Type type) {
        super(string);
        this.type = type;
    }

    protected Keyword(byte[] value, Word.Type type) {
        super(value);
        this.type = type;
    }

    public Word.Type type() {
        return type;
    }

    public void setType(Word.Type type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Keyword keyword = (Keyword) o;
        return type == keyword.type;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
