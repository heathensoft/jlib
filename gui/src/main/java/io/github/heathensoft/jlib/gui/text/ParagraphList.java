package io.github.heathensoft.jlib.gui.text;


/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public abstract class ParagraphList implements FormattedText, Iterable<Paragraph> {

    public void add(String paragraph) {
        add(Paragraph.parse(paragraph));
    }

    public abstract void add(Paragraph paragraph);

    public abstract void clear();

    public abstract int size();

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
}
