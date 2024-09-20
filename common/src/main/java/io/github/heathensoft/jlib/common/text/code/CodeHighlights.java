package io.github.heathensoft.jlib.common.text.code;

import java.util.Arrays;

/**
 * Highlighted sequences of broken down source code.
 * Each sequence represent a chain of characters that share the same highlight.
 * Cannot represent text larger than 2 GB.
 *
 * @author Frederik Dahl
 * 20/05/2024
 */


public class CodeHighlights {

    private static final int[] EMPTY_ARRAY = new int[0];
    private static final int TYP_OFFSET = 0;
    private static final int LEN_OFFSET = 1;
    private int[] array;
    private int pointer;
    private int count;

    protected CodeHighlights() { this(0); }
    protected CodeHighlights(int capacity) {
        if (capacity > 0) { array = new int[capacity * 2];
        } else array = EMPTY_ARRAY;
    }

    /**unchecked*/
    public void nextSequence() { pointer += 2; }
    public void rewind() { pointer = 0; }
    public int numSequences() { return count; }
    public boolean hasNext() { return (pointer / 2 + 1) < count; }
    public boolean isEmpty() { return count == 0; }
    public int sequenceIndex() { return pointer / 2; }
    public int sequenceLength() { return array[pointer + LEN_OFFSET]; }
    public int sequenceTypeOrdinal() { return array[pointer]; }
    public CodeColor sequenceHighlight() { return CodeColor.typeByOrdinal(array[pointer]); }


    protected void addSequence(CodeColor type, int length) {
    // Will also set current sequence -> appended sequence
        int len = count * 2;
        int cap = array.length;
        if (len >= cap) array = Arrays.copyOf(array,cap + 2 * 256);
        pointer = len;
        array[pointer + TYP_OFFSET] = type.ordinal();
        array[pointer + LEN_OFFSET] = length;
        count++;
    }

    protected void ensureCapacity(int capacity) {
        capacity *= 2;
        int current = array.length;
        int growth = capacity - current;
        if (growth > 0) array = Arrays.copyOf(array,capacity);
    }

    protected void trim() {
        if (isEmpty()) array = EMPTY_ARRAY;
        else if (count * 2 < array.length) {
            array = Arrays.copyOf(array,count * 2);
        }
    }
}
