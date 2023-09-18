package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.lwjgl.gfx.Color32;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public class Paragraph implements Iterable<Word>{


    protected Word[] words;

    public Paragraph(String text) {
        if (text == null || text.isBlank()) {
            words = NEW_LINE;
        } else words = parse(text.trim());
    }

    public Word[] words() {
        return words;
    }

    public boolean isBlank() {
        return words.length == 1;
    }

    public int length() {
        if (isBlank()) return 1;
        int word_count = words.length - 1;
        int num = 0;
        for (int i = 0; i < word_count; i++) {
            Word word = words[i];
            num += word.length() + 1;
        } return num;
    }

    public String toString() {
        if (isBlank()) return "\n";
        StringBuilder sb = new StringBuilder(length());
        for (int i = 0; i < (words.length - 1); i++) {
            sb.append(words[i]).append(' ');
        } sb.deleteCharAt(sb.length()-1).append("\n");
        return sb.toString();
    }

    public Iterator<Word> iterator() {
        return new WordIterator(words);
    }


    protected Word[] parse(String line) {
        List<Word> list = new ArrayList<>();
        String[] split_paragraph = line.split("\\s+");
        if (this.getClass().equals(Paragraph.class) || this instanceof PDebug) {
            for (String word : split_paragraph) {
                if (!(this instanceof PDebug)) {
                    char first_char = word.charAt(0);
                    if (first_char > 34 && first_char < 39 && word.length() > 1) { // Atp. It could be a keyword
                        String regex = first_char == '$' ? "\\$+\\w+[\\,.:]??" : first_char +"+\\w+[\\,.:]??";
                        if (Pattern.matches(regex,word)) { // Keyword
                            if (first_char == '&') { // Atp. We know the word is an Action
                                char second_char = word.charAt(1);
                                if (second_char == '&') {
                                    char third_char = word.charAt(2);
                                    if (third_char == '&') { // Negative Action
                                        word = word.replaceFirst("&&&","");
                                        String[] split_word = word.split("_+");
                                        for (String s : split_word)
                                            if (!s.isBlank()) list.add(new Keyword.Action.Failure(s));
                                    } else { // Positive Action
                                        word = word.replaceFirst("&&","");
                                        String[] split_word = word.split("_+");
                                        for (String s : split_word)
                                            if (!s.isBlank()) list.add(new Keyword.Action.Success(s));
                                    }
                                } else { // Neutral Action
                                    word = word.replaceFirst("&","");
                                    String[] split_word = word.split("_+");
                                    for (String s : split_word)
                                        if (!s.isBlank()) list.add(new Keyword.Action(s));
                                }
                            } else if (first_char == '#') {
                                word = word.replaceFirst("#","");
                                word = "("+word+")";
                                String[] split_word = word.split("_+");
                                for (String s : split_word) {
                                    if (!s.isBlank()) {
                                        list.add(new Keyword.InlineComment(s));
                                    }
                                }
                            }
                            else if (first_char == '$') {
                                char second_char = word.charAt(1);
                                if (second_char == '$') {
                                    char third_char = word.charAt(2);
                                    if (third_char == '$') { // Hostile
                                        word = word.replaceFirst("\\$\\$\\$","");
                                        String[] split_word = word.split("_+");
                                        for (String s : split_word)
                                            if (!s.isBlank()) list.add(new Keyword.Entity.Hostile(s));
                                    } else { // Friendly
                                        word = word.replaceFirst("\\$\\$","");
                                        String[] split_word = word.split("_+");
                                        for (String s : split_word)
                                            if (!s.isBlank()) list.add(new Keyword.Entity.Friendly(s));
                                    }
                                } else { // Neutral
                                    word = word.replaceFirst("\\$","");
                                    String[] split_word = word.split("_+");
                                    for (String s : split_word)
                                        if (!s.isBlank()) list.add(new Keyword.Entity(s));
                                }
                            }

                            else { // generic keyword
                                word = word.replaceFirst("%","");
                                String[] split_word = word.split("_+");
                                for (String s : split_word) {
                                    if (!s.isBlank()) list.add(new Keyword(s));
                                }
                            }
                            continue;
                        } // Not a keyword
                    }
                }

                if (Character.isDigit(word.charAt(0))) { // Numerics values

                    if (word.charAt(0) == '0' && word.length() > 3) {
                        if (word.charAt(1) == 'v') {
                            word = word.replaceFirst("0v","");
                            String[] split_word = word.split("_+");
                            for (String s : split_word) {
                                if (!s.isBlank()) {
                                    list.add(new Keyword.Value(s));
                                }
                            } continue;
                        }
                        else if (word.charAt(1) == 'x') {
                            String number = word.substring(2,word.length()-1);
                            if (isHexadecimal(number)) {
                                list.add(new Keyword.Value(number.toUpperCase()));
                                continue;
                            }
                        }
                    }

                    String number = word;
                    Pattern p = Pattern.compile("[\\S]+[\\.,:%]");
                    if (p.matcher(word).matches()) { // ends on . , : or %
                        number = word.substring(0,word.length() - 1);
                        if (number.endsWith("%")) {
                            number = number.substring(0,number.length() - 1);
                        }
                    }
                    if (isNumeric(number)) {
                        list.add(new Keyword.Value(word));
                        continue;
                    }
                }
                if (this instanceof PDebug) {
                    if (word.equals("true") || word.equals("false")) {
                        list.add(new Keyword.Value(word));
                        continue;
                    }
                }
                list.add(new Word(word));
            }
        } else {
            for (String word : split_paragraph) list.add(new Word(word));
        }
        list.add(EOL);
        Word[] words = new Word[list.size()];
        for (int i = 0; i < words.length; i++) {
            words[i] = list.get(i);
        }
        return words;
    }

    private boolean isHexadecimal(String s) {
        if (s == null) return false;
        try { int h = HexFormat.fromHexDigits(s);
        } catch (IllegalArgumentException i) {
            return false;
        } return true;
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        try { double d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        } return true;
    }

    private static class WordIterator implements Iterator<Word> {
        private int index = 0;
        private final Word[] words;
        WordIterator(Word[] words) { this.words = words; }
        public boolean hasNext() { return index < words.length; }
        public Word next() { return words[index++]; }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    private static final Word EOL = new Word.EOL();
    private static final Word[] NEW_LINE = new Word[]{EOL};
    protected static final Paragraph EMPTY_PARAGRAPH = new Paragraph(null);

    public static Paragraph colored(String string, Color32 color) {
        if (string == null || color == null || string.isBlank()) return EMPTY_PARAGRAPH;
        return new PColored(string.trim(),color);
    }

    public static Paragraph create(String string) {
        if (string == null || string.isBlank()) return EMPTY_PARAGRAPH;
        string = string.trim();
        if (string.length() > 2) {
            char first_char = string.charAt(0);
            if (first_char > 32 && first_char < 48) {
                if (string.startsWith("##")) {
                    string = string.replaceFirst("##\\s*","");
                    return new PHeader(string);
                } else if (string.startsWith("//")) {
                    string = string.replaceFirst("//\\s*","");
                    return new PComment(string);
                } else if (string.startsWith("**")) {
                    string = string.replaceFirst("\\*\\*\\s*","");
                    return new PDebug(string);
                } else if (string.startsWith("!!")) {
                    string = string.replaceFirst("!!\\s*","");
                    return new PWarning(string);
                }
            }
        } return new Paragraph(string);
    }

}
