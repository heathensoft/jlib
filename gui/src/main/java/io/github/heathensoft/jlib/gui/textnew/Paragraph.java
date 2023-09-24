package io.github.heathensoft.jlib.gui.textnew;

import java.util.Iterator;

/**
 *
 * Paragraph
 *   ParsedParagraph (Un-editable)
 *     Highlighted                  %%
 *     Comment                      ##
 *     DebugLine                    **
 *     Warning                      !!
 *   PlainParagraph (Editable)
 *     ColoredParagraph
 *       CommandLine
 *
 *
 * All Paragraphs en with EOL -> Words[words.length-1] == Word.EOL.singleton
 *
 * @author Frederik Dahl
 * 19/09/2023
 */


public abstract class Paragraph implements Iterable<Word>{

    protected abstract Word[] words();


    public byte charAt(int index) throws IndexOutOfBoundsException {
        int p = 0;
        Word[] words = words();
        int num_words = words.length - 1;
        for (int i = 0; i < words.length; i++) {
            byte[] w = words[i].get();
            p += w.length;
            if (p > index) {
                return w[w.length - p + index];
            } else if (p == index) {
                if (i == (num_words - 1)) {
                    return 10;
                } else return 32;
            } else p++;
        } throw new IndexOutOfBoundsException("index: " + index + " length: " + length());
    }

    public void toString(StringBuilder sb) {
        if (isBlank()) sb.append('\n');
        else { Word[] words = words();
            for (int i = 0; i < (words.length - 1); i++) {
                sb.append(words[i]).append(' ');
            } sb.deleteCharAt(sb.length()-1).append("\n");
        }
    }

    public String toString() {
        if (isBlank()) return "\n";
        Word[] words = words();
        StringBuilder sb = new StringBuilder(length());
        for (int i = 0; i < (words.length - 1); i++) {
            sb.append(words[i]).append(' ');
        } sb.deleteCharAt(sb.length()-1).append("\n");
        return sb.toString();
    }

    public String toStringTrimmed() {
        if (isBlank()) return "";
        Word[] words = words();
        StringBuilder sb = new StringBuilder(length());
        for (int i = 0; i < (words.length - 1); i++) {
            sb.append(words[i]).append(' ');
        } sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    /**
     * @return length of paragraph in characters, including '\n'
     */
    public int length() {
        return calculateLength(words());
    }

    protected int calculateLength(Word[] words) {
        if (words.length == 1) return 1;
        int word_count = words.length - 1;
        int num = 0;
        for (int i = 0; i < word_count; i++) {
            Word word = words[i];
            num += word.length() + 1;
        } return num;
    }

    /**
     * Word-count excluding END OF LINE.
     * @return word count
     */
    public int wordCount() {
        return words().length - 1;
    }

    public boolean isParsed() {
        return this instanceof ParsedParagraph;
    }

    public boolean isBlank() {
        return words().length == 1;
    }

    public Iterator<Word> iterator() {
        return new WordIterator(words());
    }

    private static class WordIterator implements Iterator<Word> {
        private int index = 0;
        private final Word[] words;
        WordIterator(Word[] words) { this.words = words; }
        public boolean hasNext() { return index < words.length; }
        public Word next() { return words[index++]; }
        public void remove() { throw new UnsupportedOperationException(); }
    }

}
