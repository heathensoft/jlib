package io.github.heathensoft.jlib.gui.text;


import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public class Paragraph implements FormattedText, Iterable<Word>{

    private final Word[] words;

    public Paragraph(String line) {
        if (line == null || line.isBlank()) {
            words = NEW_LINE;
        } else words = parseInternal(line.trim());
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

    private Word[] parseInternal(String line) {

        List<Word> list = new ArrayList<>();
        String[] split_paragraph = line.split("\\s+");
        if (this instanceof Header || this instanceof Warning || this instanceof Comment) {
            for (String word : split_paragraph) list.add(new Word(word));
        } else { for (String word : split_paragraph) {
                if (!(this instanceof Debug)) {
                    char first_char = word.charAt(0);
                    if (first_char > 34 && first_char < 39 && word.length() > 1) { // Atp. It could be a keyword
                        String regex = first_char == '$' ? "\\$+\\w+[\\,.:]??" : first_char +"+\\w+[\\,.:]??";
                        if (Pattern.matches(regex,word)) // Keyword
                        {
                            if (first_char == '&') { // Atp. We know the word is an Action
                                char second_char = word.charAt(1);
                                if (second_char == '&') {
                                    char third_char = word.charAt(2);
                                    if (third_char == '&') { // Failure Action
                                        word = word.replaceFirst("&&&","");
                                        String[] split_word = word.split("_+");
                                        for (String s : split_word)
                                            if (!s.isBlank()) list.add(new Keyword.Action.Failure(s));
                                    } else { // Success Action
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
                                String[] split_word = word.split("_+");
                                for (String s : split_word) {
                                    if (!s.isBlank()) list.add(new Keyword.Comment(s));
                                }
                            } else if (first_char == '$') {
                                word = word.replaceFirst("\\$","");
                                String[] split_word = word.split("_+");
                                for (String s : split_word) {
                                    if (!s.isBlank()) list.add(new Keyword.Entity(s));
                                }
                            } else { // generic keyword
                                word = word.replaceFirst("%","");
                                String[] split_word = word.split("_+");
                                for (String s : split_word) {
                                    if (!s.isBlank()) list.add(new Keyword(s));
                                }
                            }
                            continue;
                        }
                        // Not a keyword
                    }
                }
                if (Character.isDigit(word.charAt(0))) {
                    String number = word;
                    if (number.length() > 3) {
                        if (number.charAt(0) == '0') {
                            if (number.charAt(1) == 'x') {
                                number = number.substring(2,number.length()-1);
                            }
                        }
                    }
                    Pattern p = Pattern.compile("[\\S]+[\\.,:%]");
                    if (p.matcher(number).matches()) { // ends on . , : or %
                        number = number.substring(0,number.length() - 1);
                        if (number.endsWith("%")) {
                            number = number.substring(0,number.length() - 1);
                        }
                    }
                    if (isNumeric(number)) {
                        list.add(new Number(word));
                        continue;
                    }
                }
                list.add(new Word(word));
            }
        }
        list.add(EOL);
        Word[] words = new Word[list.size()];
        for (int i = 0; i < words.length; i++) {
            words[i] = list.get(i);
        }
        return words;
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        try { double d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            try { int h = HexFormat.fromHexDigits(s);
            } catch (IllegalArgumentException i) {
                return false;
            } return true;
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

    private static final Word EOL = new EOL();
    private static final Word[] NEW_LINE = new Word[]{EOL};
    private static final Paragraph EMPTY_PARAGRAPH = new Paragraph(null);

    public static Paragraph parse(String string) {
        if (string == null || string.isBlank()) return EMPTY_PARAGRAPH;
        string = string.trim();
        if (string.length() > 2) {
            char first_char = string.charAt(0);
            if (first_char > 32 && first_char < 48) {
                if (string.startsWith("##")) {
                    string = string.replaceFirst("##\\s*","");
                    return new Header(string);
                } else if (string.startsWith("//")) {
                    string = string.replaceFirst("//\\s*","");
                    return new Comment(string);
                } else if (string.startsWith("**")) {
                    string = string.replaceFirst("\\*\\*\\s*","");
                    return new Debug(string);
                } else if (string.startsWith("!!")) {
                    string = string.replaceFirst("!!\\s*","");
                    return new Warning(string);
                }
            }
        } return new Paragraph(string);
    }

}
