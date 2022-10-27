package io.github.heathensoft.jlib.common.storage.primitive;

import java.util.Arrays;

import static java.lang.Long.*;

/**
 * This class works very similar to the Java BitSet class, only slimmer and less safe.
 * Inspired by artemis-odb BitVector, which is a modified version of libgdx Bits.
 * It does not keep track over words in use. (Java Bitset does) There are advantages and disadvantages.
 * For setting and getting single flags, this should be more performant.
 * For comparing "Bitsets", Bitset would be more performant for large sets.
 *
 * @author mzechner (libgdx Bits)
 * @author jshapcott (libgdx Bits)
 * @author junkdog (artemis-odb BitVector)
 * @author Frederik Dahl
 *
 * BitVector:
 * https://github.com/junkdog/artemis-odb/blob/develop/artemis-core/artemis/src/main/java/com/artemis/utils/BitVector.java
 * Bits:
 * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/utils/Bits.java
 */


public class BitSet {
    
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    
    private long[] words;
    
    public BitSet() {
        words = new long[0];
    }
    
    public BitSet(BitSet bits) {
        if (bits == null) throw new IllegalArgumentException("bits == null");
        words = Arrays.copyOf(bits.words, bits.words.length);
    }
    
    public BitSet(int nbits) {
        if (nbits < 0) throw new NegativeArraySizeException("nbits < 0: " + nbits);
        words = new long[((nbits-1) >>> ADDRESS_BITS_PER_WORD) + 1];
    }
    
    public boolean get(int idx) {
        final int word = idx >>> ADDRESS_BITS_PER_WORD;
        return word < words.length && (words[word] & (1L << idx)) != 0L;
    }
    
    public boolean getUnsafe(int idx) {
        return (words[idx >>> ADDRESS_BITS_PER_WORD] & (1L << idx)) != 0L;
    }
    
    public void set(int idx) {
        final int word = idx >>> ADDRESS_BITS_PER_WORD;
        checkCapacity(word);
        words[word] |= 1L << idx;
    }
    
    public void setUnsafe(int idx) {
        words[idx >>> ADDRESS_BITS_PER_WORD] |= 1L << idx;
    }
    
    public void clear(int idx) {
        final int word = idx >>> ADDRESS_BITS_PER_WORD;
        if (word < words.length) words[word] &= ~(1L << idx);
    }
    
    public void clearUnsafe(int idx) {
        words[idx >>> ADDRESS_BITS_PER_WORD] &= ~(1L << idx);
    }
    
    public void clear() {
        Arrays.fill(words, 0L);
    }
    
    public boolean getAndClear (int idx) {
        final int word = idx >>> 6;
        if (word >= words.length) return false;
        long oldBits = words[word];
        words[word] &= ~(1L << (idx & 0x3F));
        return words[word] != oldBits;
    }
    
    public boolean getAndSet (int idx) {
        final int word = idx >>> 6;
        checkCapacity(word);
        long oldBits = words[word];
        words[word] |= 1L << (idx & 0x3F);
        return words[word] == oldBits;
    }
    
    public void flip(int idx) {
        final int word = idx >>> ADDRESS_BITS_PER_WORD;
        checkCapacity(word);
        words[word] ^= 1L << idx;
    }
    
    public void flipUnsafe(int idx) {
        words[idx >>> ADDRESS_BITS_PER_WORD] ^= 1L << idx;
    }
    
    public void ensureCapacity(int nbits) {
        final int word = ((nbits-1) >>> ADDRESS_BITS_PER_WORD) + 1;
        if (word > words.length) {
            long[] tmp = new long[word + 1];
            System.arraycopy(words, 0, tmp, 0, words.length);
            words = tmp;
        }
    }
    
    public void and(BitSet other) {
        final int l = words.length;
        final int common = Math.min(l, other.words.length);
        for (int i = 0; common > i; i++) words[i] &= other.words[i];
        if (l > common) for (int i = common; l > i; i++) words[i] = 0L;
    }
    
    public void andNot(BitSet other) {
        int common = Math.min(words.length, other.words.length);
        for (int i = 0; common > i; i++) {
            words[i] &= ~other.words[i];
        }
    }
    
    public void or(BitSet other) {
        final int ol = other.words.length;
        final int common = Math.min(words.length, ol);
        for (int i = 0; common > i; i++) words[i] |= other.words[i];
        if (common < ol) { checkCapacity(ol);
            for (int i = common; ol > i; i++) {
                words[i] = other.words[i];
            }
        }
    }
    
    public void xor(BitSet other) {
        final int ol = other.words.length;
        final int common = Math.min(words.length, ol);
        for (int i = 0; common > i; i++) words[i] ^= other.words[i];
        if (common < ol) { checkCapacity(ol);
            for (int i = common; ol > i; i++) {
                words[i] = other.words[i];
            }
        }
    }
    
    public boolean intersects(BitSet other) {
        long[] bits = this.words;
        long[] otherBits = other.words;
        for (int i = 0, s = Math.min(bits.length, otherBits.length); s > i; i++) {
            if ((bits[i] & otherBits[i]) != 0) return true;
        } return false;
    }
    
    public boolean containsAll(BitSet other) {
        long[] bits = this.words;
        long[] otherBits = other.words;
        final int ol = otherBits.length;
        final int l = bits.length;
        for (int i = l; i < ol; i++)
            if (otherBits[i] != 0) return false;
        for (int i = 0, s = Math.min(l, ol); s > i; i++) {
            if ((bits[i] & otherBits[i]) != otherBits[i]) return false;
        } return true;
    }
    
    public int cardinality() {
        int count = 0;
        for (long word : words) count += bitCount(word);
        return count;
    }
    
    public void indicesUnchecked(IntBag out, int count) {
        out.setSize(count);
        int[] data = out.data();
        for (int i = 0, idx = 0; count > idx; i++) {
            long word = words[i];
            int wordBits = i << ADDRESS_BITS_PER_WORD;
            while (word != 0) {
                long t = word & -word;
                data[idx] = wordBits + bitCount(t - 1);
                word ^= t;
                idx++;
            }
        }
    }
    
    public void indices(IntBag out) {
        if (isEmpty()) {
            out.setSize(0);
            return;
        } final int count = cardinality();
        out.ensureCapacity(count);
        out.setSize(count);
        int[] data = out.data();
        for (int i = 0, idx = 0; count > idx; i++) {
            long word = words[i];
            int wordBits = i << ADDRESS_BITS_PER_WORD;
            while (word != 0) {
                long t = word & -word;
                data[idx] = wordBits + bitCount(t - 1);
                word ^= t;
                idx++;
            }
        }
    }
    
    public int logicalLength() {
        long[] bits = this.words;
        for (int word = bits.length - 1; word >= 0; --word) {
            long wordBits = bits[word];
            if (wordBits != 0)
                return (word << ADDRESS_BITS_PER_WORD) + BITS_PER_WORD - numberOfLeadingZeros(wordBits);
        } return 0;
    }
    
    public boolean isEmpty() {
        final int l = words.length;
        for (int i = 0; i < l; i++) {
            if (words[i] != 0L) return false;
        } return true;
    }
    
    public long getWord(int idx) {
        return idx < words.length ? words[idx] : 0L;
    }
    
    public long[] getWords() {
        return words;
    }
    
    public void setWord(int idx, long word) {
        if (idx >= words.length) {
            long[] newWords = new long[idx + 1];
            System.arraycopy(words, 0, newWords, 0, words.length);
            this.words = newWords;
        } words[idx] = word;
    }
    
    @Override
    public int hashCode() {
        final int word = logicalLength() >>> 6; int hash = 0;
        for (int i = 0; word >= i; i++) { hash = 127 * hash + (int) (words[i] ^ (words[i] >>> 32));
        } return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BitSet other = (BitSet) obj;
        long[] otherBits = other.words;
        int common = Math.min(words.length, otherBits.length);
        for (int i = 0; common > i; i++) {
            if (words[i] != otherBits[i]) return false;
        } if (words.length == otherBits.length) return true;
        return logicalLength() == other.logicalLength();
    }
    
    @Override
    public String toString() {
        int cardinality = cardinality();
        int end = Math.min(128, cardinality);
        int count = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Bits[").append(cardinality);
        if (cardinality > 0) {
            sb.append(": {");
            for (int i = nextSetBit(0); end > count && i != -1; i = nextSetBit(i + 1)) {
                if (count != 0) sb.append(", ");
                sb.append(i);
                count++;
            } if (cardinality > end)
                sb.append(" ...");
            sb.append("}");
        } sb.append("]");
        return sb.toString();
    }
    
    private void checkCapacity(int l) {
        if (l >= words.length) {
            long[] tmp = new long[++l];
            System.arraycopy(words, 0, tmp, 0, words.length);
            words = tmp;
        }
    }
    
    /** Returns the index of the first bit that is set to true that occurs on or after the specified starting index. If no such bit
     * exists then -1 is returned. */
    private int nextSetBit(int fromIndex) {
        final int word = fromIndex >>> ADDRESS_BITS_PER_WORD;
        if (word < words.length) {
            long bitmap = words[word] >>> fromIndex;
            if (bitmap == 0) {
                for (int i = 1 + word; i < words.length; i++) {
                    bitmap = words[i];
                    if (bitmap != 0) {
                        return i * BITS_PER_WORD + numberOfTrailingZeros(bitmap);
                    }
                }
            } else return fromIndex + numberOfTrailingZeros(bitmap);
        } return -1;
    }
    
}
