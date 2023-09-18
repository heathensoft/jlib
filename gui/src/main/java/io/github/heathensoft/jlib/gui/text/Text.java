package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.lwjgl.gfx.Color32;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public abstract class Text implements Iterable<Paragraph> {

    public void add(String paragraph) {
        add(Paragraph.create(paragraph));
    }

    public void add(Text text) {
        for (Paragraph p : text) {
            add(p);
        }
    }

    public void addNewline() {
        add(Paragraph.EMPTY_PARAGRAPH);
    }

    public abstract void add(List<Paragraph> list);

    public abstract void add(Paragraph paragraph);

    public abstract void clear();

    public abstract int size();

    public abstract boolean isBlank();

    public ParagraphList search(String string) {
        return new ParagraphList(matching(string));
    }

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
            Word[] words = p.words();
            for (int i = 0; i < (words.length - 1); i++) {
                sb.append(words[i]).append(' ');
            } sb.deleteCharAt(sb.length()-1).append("\n");
        } return sb.toString();
    }

    private List<Paragraph> matching(String string) {
        List<Paragraph> result = new ArrayList<>();
        if (!isBlank() && string != null && !string.isBlank()) {
            String search_for = string.trim().toUpperCase();
            for (Paragraph p : this) {
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

    public static String hexFormat(Color32 color) {
        return "0x" + color;
    }

    public static String hexFormat(int i) {
        return "0x" + Integer.toHexString(i).toUpperCase();
    }

    public static String wrapInlineComment(String string) {
        if (string == null || string.isBlank()) return "";
        return "#" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapValue(String string) {
        if (string == null || string.isBlank()) return "";
        return "0v" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapEntity(String string) {
        if (string == null || string.isBlank()) return "";
        return "$" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapEntityFriendly(String string) {
        if (string == null || string.isBlank()) return "";
        return "$$" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapEntityHostile(String string) {
        if (string == null || string.isBlank()) return "";
        return "$$" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapKeyword(String string) {
        if (string == null || string.isBlank()) return "";
        return "%" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapAction(String string) {
        if (string == null || string.isBlank()) return "";
        return "&" + string.trim().replaceAll("\\s+","_");
    }

    public static String wrapActionSuccess(String string) {
        if (string == null || string.isBlank()) return "";
        return "&&" + string.trim().replaceAll("\\s+","_");
    }

    static String wrapActionFailure(String string) {
        if (string == null || string.isBlank()) return "";
        return "&&&" + string.trim().replaceAll("\\s+","_");
    }

}
