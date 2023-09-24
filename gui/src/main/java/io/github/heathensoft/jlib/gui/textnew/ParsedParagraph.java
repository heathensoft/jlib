package io.github.heathensoft.jlib.gui.textnew;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.github.heathensoft.jlib.gui.textnew.TextUtils.isHexadecimal;
import static io.github.heathensoft.jlib.gui.textnew.TextUtils.isNumeric;

/**
 * @author Frederik Dahl
 * 19/09/2023
 */


public class ParsedParagraph extends Paragraph {

    protected static final ParsedParagraph EMPTY_PARAGRAPH = new ParsedParagraph();
    protected static final Word[] NEW_LINE = new Word[] { Word.END_OF_LINE() };
    private final Word[] words;

    public ParsedParagraph() { this(null); }

    public ParsedParagraph(String string) {
        words = parse(string);
    }

    protected Word[] parse(String string) {
        if (string == null || string.isBlank()) return NEW_LINE;
        String[] split_string = string.split("\\s+");
        List<Word> list = new ArrayList<>();
        if (isRegularParsed()) {
            for (String word : split_string) {
                char first_char = word.charAt(0);
                if (first_char > 34 && first_char < 39 && word.length() > 1) {
                    // Possible KEYWORD "%" , "#" , "$"+ , "&"+
                    // If the word is a keyword: add the word to list. Then continue.
                    // If not, pass the word on to see if it is a "Value"
                    String regex = first_char == '$' ? "\\$+\\w+[\\,.:]??" : first_char +"+\\w+[\\,.:]??";
                    if (Pattern.matches(regex,word)) { // KEYWORD
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
                    }
                }
                if (Character.isDigit(first_char)) {
                    // Possible VALUE "0v" , "0x" or otherwise numeric
                    // If the word is a Value: add the word to list. Then continue.
                    // If not, pass the word on and add it as a regular Word
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
                } list.add(new Word(word));
            }
        } else { // Highlighted, Comment, Debug, Warning
            if (this instanceof DebugLine) {
                for (String word : split_string) {
                    char first_char = word.charAt(0);
                    if (Character.isDigit(first_char)) {
                        // Possible VALUE "0v" , "0x" or otherwise numeric
                        // If the word is a Value: add the word to list. Then continue.
                        // If not, pass the word on and add it as a regular Word
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
                    if (word.equals("true") || word.equals("false")) {
                        list.add(new Keyword.Value(word));
                        continue;
                    }
                    list.add(new Word(word));
                }
            }
            else {
                for (String word : split_string) {
                    list.add(new Word(word));
                }
            }
        }
        list.add(Word.END_OF_LINE());
        Word[] words = new Word[list.size()];
        for (int i = 0; i < words.length; i++) {
            words[i] = list.get(i);
        } return words;
    }

    /**
     * If this is not the parsed paragraph superclass
     * Then it is one of its subclasses.
     * @return if this class equals ParsedParagraphClass
     */
    public boolean isRegularParsed() {
        return this.getClass().equals(ParsedParagraph.class);
    }

    protected Word[] words() {
        return words;
    }


}
