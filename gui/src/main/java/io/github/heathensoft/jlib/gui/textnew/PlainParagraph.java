package io.github.heathensoft.jlib.gui.textnew;

import io.github.heathensoft.jlib.common.utils.U;

import java.util.Arrays;

/**
 * PlainParagraph can be edited while Parsed cannot.
 *
 * @author Frederik Dahl
 * 19/09/2023
 */


public class PlainParagraph extends Paragraph {

    protected Word[] words;
    protected int length;

    public PlainParagraph() {
        this((String) null);
    }

    public PlainParagraph(String string) {
        words = stringToWords(string);
        length = calculateLength(words);
    }

    protected PlainParagraph(Word[] words) {
        this.words = words;
        this.length = calculateLength(words);
    }

    // Trim *this* paragraph before appending.
    protected void append(PlainParagraph paragraph) {
        if (!paragraph.isBlank()) {
            if (isBlank()) {
                this.words = paragraph.words;
                this.length = paragraph.length;
            } else { int c1 = wordCount();
                int c2 = paragraph.wordCount();
                Word[] p_words = paragraph.words;
                Word[] new_words = new Word[c1 + c2 + 1];
                System.arraycopy(words,0,new_words,0,c1);
                System.arraycopy(p_words,0,new_words,c1,c2);
                new_words[new_words.length - 1] = Word.END_OF_LINE();
                words = new_words;
                length = calculateLength(words);
            }
        }
    }

    protected PlainParagraph splitByWord(int index) {
        int word_count = wordCount();
        if (index < 0 || index > word_count)
            throw new IndexOutOfBoundsException("split index: " + index + " words: " + word_count);
        PlainParagraph paragraph;
        if (index == 0) {
            paragraph = new PlainParagraph(words);
            this.words = new Word[] { Word.END_OF_LINE() };
            this.length = 1;
        } else if (index == word_count) {
            paragraph = new PlainParagraph();
        } else { Word[] sub = Arrays.copyOfRange(words,index,words.length);
            paragraph = new PlainParagraph(sub);
            sub = new Word[index + 1];
            System.arraycopy(words,0,sub,0,index);
            sub[index] = Word.END_OF_LINE();
            this.words = sub;
            this.length = calculateLength(words);
        } return paragraph;
    }

    protected PlainParagraph split(int index) {
        int cher_count = numPrintable();
        if (index > cher_count || index < 0) {
            throw new IndexOutOfBoundsException("index: " + index + " chars: " + cher_count);
        } else if (index == 0) {
            Word[] words = this.words;
            this.words = new Word[] { Word.END_OF_LINE() };
            this.length = 1;
            return new PlainParagraph(words);
        } else if (index == cher_count) return new PlainParagraph();
        int word_count = wordCount();
        StringBuilder builder = new StringBuilder(cher_count + 1);
        for (int i = 0; i < word_count; i++) {
            byte[] word = words[i].get();
            for (byte b : word) {
                builder.append((char) b);
            } builder.append(' ');
        } words = stringToWords(builder.substring(0,index));
        length = calculateLength(words);
        return new PlainParagraph(builder.substring(index,cher_count));
    }

    protected boolean deleteChar(int index) {
        int cher_count = numPrintable();
        if (index >= cher_count || index < 0) {
            throw new IndexOutOfBoundsException("index: " + index + " chars: " + cher_count);
        } int word_count = wordCount();
        for (int i = 0, p = 0; i < word_count; i++) {
            byte[] w = words[i].get();
            p += w.length;
            if (p > index) {
                // Atp the index points at a character of a word.
                if (w.length == 1) { // local index should be 0.
                    if (word_count == 1) {
                        words = new Word[] {Word.END_OF_LINE()};
                        length = 1;
                    }
                    else {
                        if (i == (word_count - 1)) {
                            // Atp. we are at the last word. And it is not the first
                            // We have to remove the final word and append space to the new last word
                            words = Arrays.copyOf(words,words.length-1);
                            word_count = wordCount();
                            w = words[word_count - 1].get();
                            byte[] n = Arrays.copyOf(w,w.length + 1);
                            n[w.length] = 32;
                            words[word_count - 1] = new Word(n);
                            length --;
                        }
                        else {
                            words[i] = null;
                            Word[] new_words = new Word[words.length - 1];
                            for (int j = 0, k = 0; j < words.length; j++) {
                                Word word = words[j];
                                if (word == null) k++;
                                else new_words[j-k] = word;
                            } words = new_words;
                            length -= 2; // space + char
                        }
                    }
                } else {
                    int local = w.length - p + index;
                    words[i] = new Word(removeCharFrom(w,local));
                    length--;
                } return true;
            } else if (p == index) {
                // Atp. We are in the space between two words.
                // We are not between a word and '\n' (EOL)
                Word spliced = new Word(U.splice(w,words[i+1].get()));
                words[i] = spliced;
                words[i+1] = null;
                Word[] new_words = new Word[words.length - 1];
                for (int j = 0, k = 0; j < words.length; j++) {
                    Word word = words[j];
                    if (word == null) k++;
                    else new_words[j-k] = word;
                } words = new_words;
                length--;
                return true;
            } else p++;
        } return false;
    }

    private byte[] removeCharFrom(byte[] src, int index) {
        byte[] dst = new byte[src.length - 1];
        for (int i = 0, j = 0; i < src.length; i++) {
            if (i == index) j++;
            else dst[i-j] = src[i];
        } return dst;
    }

    protected boolean append(byte c) {
        if (c != 10) {
            int word_count = wordCount();
            if (word_count == 0) {
                if (c != 32) {
                    Word w = new Word(c);
                    words = new Word[] {w,words[0]};
                    length++;
                    return true;
                }
            } else {
                byte[] w = words[word_count - 1].get();
                int cher_count = numPrintable();
                if (c == 32) {
                    if (w[w.length - 1] != 32) {
                        byte[] n = Arrays.copyOf(w,w.length + 1);
                        n[w.length] = c;
                        words[word_count - 1] = new Word(n);
                        length++;
                        return true;
                    }
                }
                else {
                    if (w[w.length - 1] == 32) { // New word
                        byte[] n = Arrays.copyOf(w,w.length - 1);
                        words[word_count - 1] = new Word(n); // remove internal space
                        Word[] new_words = new Word[words.length + 1];
                        new_words[words.length] = Word.END_OF_LINE();
                        new_words[word_count] = new Word(c);
                        System.arraycopy(words, 0, new_words, 0, word_count);
                        words = new_words;
                    } else {
                        // Append current with non-space char
                        byte[] n = Arrays.copyOf(w,w.length + 1);
                        n[w.length] = c;
                        words[word_count - 1] = new Word(n);
                    }
                    length++;
                    return true;
                }
            }

        } return false;
    }


    protected boolean insert(byte c, int index) {
        int cher_count = numPrintable();
        if (index > cher_count || index < 0)
            throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
        if (index == cher_count) return append(c);
        // Note: Paragraph is not blank atp.
        // Word count would never be 0 atp.
        // If so, an exception would be thrown or
        // the char would have been passed on to "append"
        if (c != 10) {
            int word_count = wordCount(); // <-- != 0
            for (int i = 0, p = 0; i < word_count; i++) {
                byte[] w = words[i].get();
                p += w.length;
                if (p > index) {
                    // Atp the index points at a character of a word.
                    int local_index = w.length - p + index;
                    if (c == 32) {
                        // Attempting to insert a 32 character
                        // If it points to a 32 character, we return false.
                        // If the index points at the first char in the word,
                        // Then the previous char is a space. So return false
                        if (local_index == 0 || w[local_index] == 32) return false;
                        // Atp. The word length > 1. And will divide the word.
                        // We must split the word in two
                        byte[] s1 = Arrays.copyOfRange(w,0,local_index);
                        byte[] s2 = Arrays.copyOfRange(w,local_index,w.length);
                        words[i] = new Word(s1);
                        Word[] new_words = new Word[words.length + 1];
                        int idx = 0;
                        for (int j = 0; j < new_words.length; j++) {
                            if (j == (i+1)) new_words[j] = new Word(s2);
                            else {
                                new_words[j] = words[idx];
                                idx++;
                            }
                        } words = new_words;
                    } else {
                        byte[] n = new byte[w.length + 1];
                        int w_idx = 0;
                        for (int j = 0; j < n.length; j++) {
                            if (j == local_index) {
                                n[j] = c;
                            } else {
                                n[j] = w[w_idx];
                                w_idx++;
                            }
                        }
                        words[i] = new Word(n);
                    }
                    length++;
                    return true;

                } else if (p == index) {
                    // Atp. We are in the space between two words.
                    // We are not between a word and '\n'
                    // If the char to be inserted == 32 (space),
                    // then we can return false.
                    // else, place the char at the end of the word
                    // before the space
                    if (c == 32) return false;
                    byte[] n = Arrays.copyOf(w,w.length + 1);
                    n[w.length] = c;
                    words[i] = new Word(n);
                    length++;
                    return true;
                } else p++;
            }
        }
        return false;
    }

    /**
     * If this is not the unparsed paragraph superclass
     * Then it is one of its subclasses.
     * @return if this class equals UnParsedParagraphClass
     */
    public boolean isRegularUnParsed() {
        return this.getClass().equals(PlainParagraph.class);
    }

    @Override
    public int length() {
        return length;
    }

    /**
     * all characters except '\n'
     * @return length - 1
     */
    public int numPrintable() {
        return length - 1;
    }


    protected Word[] stringToWords(String string) {
        if (string == null || string.isBlank())
            return new Word[] { Word.END_OF_LINE() };
        String[] split_string = string.split("\\s+");
        Word[] words = new Word[split_string.length + 1];
        for (int i = 0; i < split_string.length; i++) {
            words[i] = new Word(split_string[i]);
        } words[split_string.length] = Word.END_OF_LINE();
        return words;
    }

    protected void trim() {
        if (words.length > 1) {
            Word last = words[wordCount() - 1];
            byte[] w = last.get();
            if (w[w.length - 1] == 32) {
                if (w.length == 1) { // Should not occur
                    words = Arrays.copyOf(words,words.length - 1);
                    words[words.length - 1] = Word.END_OF_LINE();
                    length = calculateLength(words);
                } else { w = Arrays.copyOf(w,w.length - 1);
                    words[wordCount() - 1] = new Word(w);
                    length--;
                }
            }
        }
    }


    protected Word[] words() {
        return words;
    }

}
