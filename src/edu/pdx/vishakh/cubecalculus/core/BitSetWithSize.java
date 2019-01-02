package edu.pdx.vishakh.cubecalculus.core;

import java.util.BitSet;

public class BitSetWithSize  implements Comparable {
    private int size;
    private BitSet bits;

    /**
     * Creates a BitSetWithSize with all zeros.
     * @param size Number of variables
     */
    public BitSetWithSize(int size) {
        this.size = size;
        bits = new BitSet(size);
    }

    /**
     * Creates a BitSetWithSize from the lower {@Code size} of a {@Code BitSet}.
     * @param size Size of the BitSetWithSize.
     * @param b The input BitSet.
     */
    public BitSetWithSize(int size, BitSet b) {
        this.size = size;
        bits = b;
    }

    /**
     * Creates a BitSetWithSize from a string of 1s and 0s.
     * @param bits A String with only 1s and 0s.
     */
    public BitSetWithSize(String bits) {
        this.size = bits.length();
        BitSet result = new BitSet(bits.length());
        for (int i = 0; i < bits.length(); ++i) {
            if (bits.charAt(i) == '1') {
                result.set(i);
            }
        }
        this.bits = result;
    }

    /**
     * Creates a BitSetWithSize from a a number.
     *
     * @param size Size of BitSetWithSize.
     * @param value An integer, the lower size will be used for the BitSetWithSize.
     */
    public BitSetWithSize(int size, long value) {
        assert size > 0;
        assert value < (1L << size);
        this.size = size;
        bits = new BitSet(size);
        int idx = size - 1;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(idx);
            }
            --idx;
            value >>>= 1;
        }

    }


    /**
     * Returns the size of the BitSetWithSize, i.e., the number of variables.
     * @return Size of BitSetWithSize.
     */
    public int size() {
        return size;
    }

    public BitSet bitSet() {
        return bits;
    }

    /**
     * Sets the bit/variable at an index to 1.
     * @param index The index of the bit to be set.
     */
    public void set(int index) {
        assert index >= 0 && index < size;
        bits.set(index);
    }

    /**
     * Sets the bit/variable at an index to 0.
     * @param index The index of the bit to be cleared.
     */
    public void clear(int index) {
        assert index >= 0 && index < size;
        bits.clear(index);
    }

    /**
     * Flips the status (1 to 0 and 0 to 1) of the bit/variable at a given index.
     * @param index The index of the bit to be flipped.
     */
    public void flip(int index) {
        assert index >= 0 && index < size;
        bits.flip(index);
    }

    /**
     * Retrieves the bit/variable at an index
     * @param index The index.
     * @return The status (true or false) of the variable at the index.
     */
    public boolean get(int index) {
        assert index >= 0 && index < size;
        return bits.get(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.append(bits.get(i) ? "1" : "0");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof BitSetWithSize) && this.toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public int compareTo(Object o) {
        BitSetWithSize m = (BitSetWithSize) o;
        return toString().compareTo(m.toString());
    }
}
