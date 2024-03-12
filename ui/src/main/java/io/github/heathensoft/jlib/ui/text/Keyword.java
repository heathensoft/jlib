package io.github.heathensoft.jlib.ui.text;

/**
 * @author Frederik Dahl
 * 25/02/2024
 */


public class Keyword extends Word {

    private Type type;

    protected Keyword(String string, Type type) {
        super(string);
        this.type = type;
    }

    protected Keyword(byte[] value, Type type) {
        super(value);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    public void setType(Type type) {
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
