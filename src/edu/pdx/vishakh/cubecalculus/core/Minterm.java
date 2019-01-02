package edu.pdx.vishakh.cubecalculus.core;

import java.util.BitSet;


/**
 * Represents a minterm using java.util.BitSet.
 *
 * In addition to the functions of a BitSet, a Minterm keeps the number of variables. The size of the
 * BitSet is equal or more than then number of variables. The lower {@code size} bits of {@code bits} defines the minterm.
 *
 * Each bit in the Minterm represent the status (on/off) of a variable.
 */

public class Minterm extends BitSetWithSize {

    /**
     * Creates a Minterm with all zeros.
     * @param nVars Number of variables
     */
    public Minterm(int nVars) {
        super(nVars);
    }

    /**
     * Creates a Minterm from the lower {@Code size} of a {@Code BitSet}.
     * @param nVars Size of the Minterm.
     * @param b The input BitSet.
     */
    public Minterm(int nVars, BitSet b) {
        super(nVars, b);
    }

    /**
     * Creates a Minterm from a string of 1s and 0s.
     * @param bits A String with only 1s and 0s.
     */
    public Minterm(String bits) {
        super(bits);
    }

    /**
     * Creates a Minterm from a a number.
     *
     * @param nVars Size of Minterm.
     * @param value An integer, the lower size will be used for the Minterm.
     */
    public Minterm(int nVars, long value) {
        super(nVars, value);
    }

    public int getNVars() {
        return size();
    }
}
