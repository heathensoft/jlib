package io.github.heathensoft.jlib.gui.deprecated;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 19/09/2023
 */


public abstract class Text<T extends Paragraph> implements Iterable<T>{

    public abstract void clear();

    public abstract int size();

    public abstract boolean isBlank();

    public int length() {
        int length = 0;
        for (Paragraph p : this) {
            length += p.length();
        } return length;
    }

    public String toString() {
        if (isBlank()) return "";
        StringBuilder sb = new StringBuilder(length());
        for (Paragraph p : this) {
            p.toString(sb);
        } return sb.toString();
    }

    protected List<T> matching(String string) {
        List<T> result;
        if (this instanceof ParsedText)
        result = new ArrayList<>();
        else result = new LinkedList<>();
        if (!isBlank() && string != null && !string.isBlank()) {
            String search_for = string.trim().toUpperCase();
            for (T p : this) {
                if (!p.isBlank()) {
                    if (p.length() > search_for.length()) {
                        String to_search = p.toString().toUpperCase();
                        if (to_search.contains(search_for)) {
                            result.add(p);
                        }
                    }
                }
            }
        } return result;
    }

}
