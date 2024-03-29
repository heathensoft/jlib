package io.github.heathensoft.jlib.ui.text;

import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Frederik Dahl
 * 25/02/2024
 */


public class Paragraph implements Iterable<Word> {

    public enum Type {
        DEFAULT(0,"Default"),
        COMMENT(1,"Comment"),
        DEBUG  (2,"Debug"),
        WARNING(3,"Warning");
        public final int id;
        public final String name;
        Type(int id, String name) {
            this.name = name;
            this.id = id;
        }
    }

    protected Type type;
    protected LinkedList<Word> words;

    public Paragraph() { this((String) null); }
    public Paragraph(Type type) { this((String) null,type); }
    public Paragraph(String text) { this(text, Type.DEFAULT); }
    public Paragraph(String text, Type type) {
        this.words = stringToWords(text,type);
        this.type = type;
    }
    protected Paragraph(LinkedList<Word> words, Type type) {
        this.words = words;
        this.type = type;
    }

    public void clear() { words.clear(); }
    public Iterator<Word> iterator() { return words.iterator(); }
    public void setType(Type type) { this.type = type; }
    public LinkedList<Word> words() { return words; }
    public Type type() { return type; }
    public int wordCount() { return words.size(); }
    public boolean isBlank() { return words.isEmpty(); }

    public Paragraph appendValue(String string) { return append(string, Word.Type.VALUE); }
    public Paragraph appendEntityPlayer(String string) { return append(string, Word.Type.ENTITY_FRIENDLY); }
    public Paragraph appendEntityOther(String string) { return append(string, Word.Type.ENTITY_HOSTILE); }
    public Paragraph appendLocation(String string) { return append(string, Word.Type.LOCATION); }
    public Paragraph appendObject(String string) { return append(string, Word.Type.OBJECT); }
    public Paragraph appendItem(String string) { return append(string, Word.Type.ITEM); }
    public Paragraph appendAction(String string) { return append(string, Word.Type.ACTION); }
    public Paragraph appendSuccess(String string) { return append(string, Word.Type.ACTION_SUCCESS); }
    public Paragraph appendFailure(String string) { return append(string, Word.Type.ACTION_FAILURE); }

    public Paragraph append(String string, Word.Type type) {
        if (string != null && !string.isBlank()) {
            String[] split = string.trim().split("\\s+");
            for (String s : split) words.addLast(new Keyword(s,type));
        } return this;
    }

    public Paragraph append(String string) {
        if (string != null && !string.isBlank()) {
            String[] split = string.trim().split("\\s+");
            for (String s : split) words.addLast(new Word(s));
        } return this;
    }

    public Paragraph append(Paragraph paragraph) {
        for (Word word : paragraph) {
            words.addLast(word);
        } return this;
    }

    public void findKeywords(Set<String> dst, Word.Type type) {
        if (!isBlank()) {
            int word_count = words.size();
            StringBuilder sb = new StringBuilder(length());
            int index_outer = 0;
            for (Word word_outer : words) {
                if (word_outer.type() == type) {
                    sb.delete(0,Math.max(0,sb.length()-1));
                    sb.append(word_outer.toString().trim());
                    dst.add(sb.toString());
                    int index_inner = 0;
                    for (Word word_inner : words) {
                        if (index_inner > index_outer) {
                            if (word_inner.type() == type) {
                                sb.append(' ').append(word_inner.toString().trim());
                                dst.add(sb.toString());
                            } else break;;
                        } index_inner++;
                    }
                } index_outer++;
            }
        }
    }

    /**
     * Will return true if string is blank
     * @param string string to search for
     * @param case_sensitive case-sensitive
     * @return if a match was found
     */
    public boolean matching(String string, boolean case_sensitive) {
        if (string == null || string.isBlank()) return true;
        if (!isBlank()) {
            String paragraph = toString();
            if (!case_sensitive) {
                string = string.toUpperCase();
                paragraph = paragraph.toUpperCase();
            } return paragraph.contains(string);
        } return false;
    }

    public float width() { // Unscaled width pixels. Bind font before call
        if (!isBlank()) {
            FontsGUI fonts = GUI.fonts;
            float space = fonts.advance(' ');
            float width = 0;
            for (Word w : words) {
                width += w.width();
                width += space;
            } width -= space;
            return width;
        } return 0;
    }

    public int length() {
        if (isBlank()) return 0;
        int length = 0;
        for (Word word : words) {
            length += (word.length() + 1);
        } return length - 1;
    }

    public Paragraph copy() {
        Paragraph copy = new Paragraph(type);
        for (Word word : words) {
            copy.words.addLast(word.copy());
        } return copy;
    }

    public void toString(StringBuilder sb) {
        if (!isBlank()) {
            for (Word word : words) {
                sb.append(word).append(' ');
            } sb.deleteCharAt(sb.length() - 1);
        }
    }

    public String toString() {
        if (isBlank()) return "";
        StringBuilder sb = new StringBuilder(length() + 1);
        for (Word word : words) {
            sb.append(word).append(' ');
        } return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public byte charAt(int index) throws IndexOutOfBoundsException {
        int p = 0;
        int num_words = words.size();
        for (Word w : words) {
            p += w.length();
            if (p > index) {
                return w.get(w.length() - p + index);
            } else if (p == index) {
                return 32;
            } else p++;
        } throw new IndexOutOfBoundsException("index: " + index + " length: " + length());
    }

    public Paragraph[] split(int index) {
        int length = length();
        if (index > length || index < 0) {
            throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
        } else if (index == 0 || index == length) {
            return new Paragraph[] {this};
        } else { int p = 0;
            LinkedList<Word> l1 = new LinkedList<>();
            LinkedList<Word> l2 = new LinkedList<>();
            boolean split = false;
            for (Word word : words) {
                p += word.length();
                if (p > index) {
                    if (split) {
                        l2.addLast(word);
                    } else {
                        int local = word.length() - p + index;
                        Word[] split_words = word.split(local);
                        if (split_words.length == 1) {
                            l2.addLast(split_words[0]);
                        } else {
                            l1.addLast(split_words[0]);
                            l2.addLast(split_words[1]);
                        }
                        split = true;
                    }
                } else if (p == index) {
                    l1.addLast(word);
                    split = true;
                } else { p++;
                    l1.addLast(word);
                }
            }
            Paragraph[] result = new Paragraph[2];
            result[0] = new Paragraph(l1,type);
            result[1] = new Paragraph(l2,type);
            return result;
        }
    }

    public boolean insert(int c, int index) { return insert((byte)(c & 0xFF), index); }
    public boolean insert(char c, int index) { return insert((byte)(c & 0xFF), index); }
    public boolean insert(byte c, int index) {
        if (inRange(c)) {
            int length = length();
            if (index > length || index < 0)
                throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
            if (index == length) return append(c);
            int word_count = wordCount();
            Word[] words = this.words.toArray(new Word[word_count]);
            for (int i = 0, p = 0; i < word_count; i++) {
                Word word = words[i];
                p += word.length();
                if (p > index) {
                    int local = word.length() - p + index;
                    if (c == 32) {
                        if (local == 0 || word.get(local) == 32)
                            return false;
                        Word[] split = word.split(local);
                        this.words.remove(i);
                        for (int j = 0; j < split.length; j++)
                            this.words.add(i + j, split[j]);
                    } else  word.insert(c,local);
                    return true;
                } else if (p == index) {
                    if (c == 32) return false;
                    word.append(c);
                    return true;
                } else p++;
            }
        } return false;
    }

    public boolean append(char c) { return append((byte) (c & 0xFF)); }
    public boolean append(int c) { return append((byte) (c & 0xFF)); }
    public boolean append(byte c) {
        if (inRange(c)) {
            if (words.size() > 0) {
                Word last = words.peekLast();
                if (c == 32) {
                    if (last.lastChar() != 32) {
                        last.append(c);
                        return true;
                    }
                } else {
                    if (last.lastChar() == 32) {
                        last.trim();
                        if (last.length() == 0) {
                            words.removeLast();
                        } words.addLast(new Word(c));
                    } else { last.append(c);
                    } return true;
                }
            } else if (c != 32){
                String s = Character.toString((char) c);
                words.addLast(new Word(s));
                return true;
            }
        } return false;
    }

    public void delete(int index) {
        int length = length();
        if (index >= length || index < 0) {
            throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
        } int word_count = wordCount();
        Word[] words = this.words.toArray(new Word[word_count]);
        for (int i = 0, p = 0; i < word_count; i++) {
            Word word = words[i];
            p += word.length();
            if (p > index) {
                int local = word.length() - p + index;
                if (word.length() == 1) {
                    if (word_count == 1) {
                        this.words.clear();
                    } else {
                        if (i == (word_count - 1)) {
                            this.words.removeLast();
                            assert this.words.peekLast() != null;
                            this.words.peekLast().append((byte)' ');
                        } else this.words.remove(i);
                    }
                } else word.remove(local);
                break;
            } else if (p == index) {
                word.append(words[i+1]);
                this.words.remove(i+1);
                break;
            } else p++;
        }
    }

    public Paragraph trim() {
        if (words.size() > 0) {
            Word first = words.peekFirst();
            first.trim();
            if (first.length() == 0) {
                words.removeFirst();
                if (words.size() > 0) {
                    Word last = words.peekLast();
                    last.trim();
                    if (last.length() == 0) {
                        words.removeLast();
                    }
                }
            } else {
                if (words.size() > 1) {
                    Word last = words.peekLast();
                    last.trim();
                    if (last.length() == 0) {
                        words.removeLast();
                    }
                }
            }
        }
        return this;
    }

    protected boolean inRange(byte c) { return (c >= 32 && c < 127); }

    protected LinkedList<Word> stringToWords(String line, Type type) {
        LinkedList<Word> list = new LinkedList<>();
        if (line != null && !line.isBlank()) {
            String[] split_paragraph = line.trim().split("\\s+");
            switch (type) {
                case DEFAULT, COMMENT -> {
                    for (String word : split_paragraph)
                        list.add(new Word(word));
                } case DEBUG, WARNING -> {
                    for (String word : split_paragraph) {
                        if (Character.isDigit(word.charAt(0))) {
                            // Possible Hexadecimal
                            if (word.charAt(0) == '0' && word.length() > 3) {
                                if (word.charAt(1) == 'x') {
                                    String hex = word.substring(2,word.length()-1);
                                    if (is_hexadecimal(hex)) {
                                        hex = "0x"+hex.toUpperCase();
                                        list.add(new Keyword(hex, Keyword.Type.VALUE));
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
                            if (is_numeric(number)) {
                                list.add(new Keyword(word, Keyword.Type.VALUE));
                                continue;
                            }
                        }
                        list.add(new Word(word));
                    }
                }
            }
        } return list;
    }

    private boolean is_hexadecimal(String s) {
        if (s == null) return false;
        try { int h = HexFormat.fromHexDigits(s);
        } catch (IllegalArgumentException i) {
            return false;
        } return true;
    }

    private boolean is_numeric(String s) {
        if (s == null) return false;
        try { double d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        } return true;
    }

}
