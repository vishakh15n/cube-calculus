package edu.pdx.vishakh.cubecalculus.core;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class BooleanCube  implements Comparable {

    /** Number of bits per variable.  2 for boolean variables. */
    protected static final int N_BITS_PER_VAR = 2;

    public static final String OFF = "0";
    public static final String ON = "1";
    public static final String DONTCARE = "*";
    public static final String ERROR = "E";

    /** Number of variables/bits. */
    private int nVars;

    /** Number of dont-care literals. */
    private int order;

    /**
     * Each pair of bits indicate a variable.
     * 10 : 0
     * 01 : 1
     * 11 : Don't care
     * 00 : Error
     *
     * Bits 0-1 represents variable 0, 2-3 represents variable 1 etc.
     */
    private BitSet bits;

    // PUBLIC METHODS

    /**
     * Creates a BooleanCube from the string representation.  String can have 0, 1 or 2.  Length of the string is assumed
     * to be number of variables.
     * @param trits String representation consisting of 0, 1 or *.
     * @return A BooleanCube represented by the String. Null if invalid input.
     */
    public static BooleanCube fromString(String trits) {
        int nTrits = trits.length();
        BooleanCube cube = new BooleanCube(nTrits);
        for (int i = 0; i < nTrits; ++i) {
            char c = trits.charAt(i);
            if (c == '1') {
                cube.makeOnVar(i);
            } else if (c == '0') {
                cube.makeOffVar(i);
            } else if (c == '*') {
                cube.makeDontCareVar(i);
            } else {
                return null;
            }
        }
        cube.calcOrder();
        return cube;
    }


    /**
     * Creates a 0-oreder cube from a minterm.
     * @param minterm The input minterm.
     * @return An equivalent cube.
     */
    public static BooleanCube fromMinterm(Minterm minterm) {
        int nVars = minterm.getNVars();

        BooleanCube c = new BooleanCube(nVars);

        for (int i = 0; i < nVars; ++i) {
            if (minterm.get(i)) {
                c.makeOnVar(i);
            } else {
                c.makeOffVar(i);
            }
        }
        c.order = 0;
        return c;
    }


    /** Clones another cube. */
    public BooleanCube(BooleanCube c) {
        nVars = c.nVars;
        bits = (BitSet) c.bits.clone();
        order = c.order;
    }



    /** Mutable method to set a variable to ON.
     * Sets the variable at idx to ON (1).
     **/
    public void makeOnVar(int idx) {
        if (isDontCareVar(idx)) {
            --order;
        }
        int idx1 = idx + idx;
        int idx2 = idx1 + 1;

        this.bits.clear(idx1);
        this.bits.set(idx2);
    }

    /** Mutable method to set a variable to OFF.
     * Sets the variable at idx to OFF (0).
     **/
    public void makeOffVar(int idx) {
        if (isDontCareVar(idx)) {
            --order;
        }
        int idx1 = idx + idx;
        int idx2 = idx1 + 1;

        this.bits.set(idx1);
        this.bits.clear(idx2);

    }

    /** Mutable method to set a variable to DONTCARE.
     * Sets the variable at idx to DONTCARE (2).
     **/
    public void makeDontCareVar(int idx) {
        if (!isDontCareVar(idx)) {
            ++order;

            int idx1 = idx + idx;
            int idx2 = idx1 + 1;

            this.bits.set(idx1);
            this.bits.set(idx2);
        }


    }


    /** Mutable method to flip a variable.
     * If the variable at index idx is ON, make it OFF.
     * If it is OFF, make it ON.
     * No change if it is DONTCARE.
     */
    public void flipVar(int idx) {
        if (isOnVar(idx)) {
            makeOffVar(idx);
        } else if (isOffVar(idx)) {
            makeOnVar(idx);
        }
    }

    /** Returns true if the variable at index idx is ON. */
    public boolean isOnVar(int idx) {
        return getVar(idx) == ON;
    }

    /** Returns true if the variable at index idx is OFF. */
    public boolean isOffVar(int idx) {
        return getVar(idx) == OFF;
    }

    /** Returns true if the variable at index idx is DONTCARE. */
    public boolean isDontCareVar(int i) {
        return getVar(i) == DONTCARE;
    }

    /** Returns the variable at index idx.
     * It returns one of ON, OFF or DONTCARE, so == operator can be used to comparae, rather than equals() method.
     */
    public String getVar(int idx) {
        int idx1 = idx + idx;
        int idx2 = idx1 + 1;

        boolean b1 = bits.get(idx1);
        boolean b2 = bits.get(idx2);

        if (b1) {
            return b2 ? DONTCARE : OFF;
        } else {
            return b2 ? ON : ERROR;
        }
    }

    /** Creates a new cube as the complement of {@Code this} cube. */
    public BooleanCube complement() {
        BitSet bitset = (BitSet) this.bits.clone();
        bitset.flip(0, nVars * 2);
        BooleanCube cube = new BooleanCube(nVars, bitset);
        if (!cube.isValid()) {
            return null;
        }
        return cube;
    }

    /**
     * Creates a cube from the intersection of two cubes.
     */
    public BooleanCube intersection(BooleanCube other) {
        assert this.nVars == other.nVars;
        assert this.order == other.order;

        BooleanCube cube = new BooleanCube(this);
        cube.bits.and(other.bits);
        if (!cube.isValid()) {
            return null;
        }
        cube.calcOrder();
        return cube;
    }

    /** Returns the number of ONs in the cube. */
    public int width() {
        int count = 0;
        for (int i = 0; i < nVars; ++i) {
            if (isOnVar(i)) {
                ++count;
            }
        }
        return count;
    }

    /** Merges two cubes of the order n to a new one of order (n+1).  Returns {@Code null} if they cannot be merged. */
    public BooleanCube merge(BooleanCube other) {
        if (!this.isAdjacentTo(other)) {
            return null;
        }
        BitSet bitset = (BitSet) this.bits.clone();
        int maxLength = bitset.length() > other.bits.length() ? bitset.length() : other.bits.length();

        for (int i = 0; i < maxLength; ++i) {
            if (bitset.get(i) != other.bits.get(i)) {
                bitset.set(i);
            }
        }
        return new BooleanCube(nVars, bitset);
    }

    /** Checks whether if two cubes are adjacent,
     * i.e, their dontcares are at the same places and the rest differ by exactly one bit.
     */
    public boolean isAdjacentTo(BooleanCube other) {
        if (other.nVars != nVars) {
            return false;
        }
        if (other.order != order) {
            return false;
        }

        int count = 0;
        for (int i = 0; i < nVars; ++i) {
            String first = this.getVar(i);
            String second = other.getVar(i);
            if (first != second) {
                if (first == DONTCARE || second == DONTCARE) {
                    return false;
                } else {
                    ++count;
                    if (count > 1) {
                        return false;
                    }
                }
            }
        }
        return count == 1;
    }

    /**
     * Creates a cube as the supercube of two cubes.
     */
    public BooleanCube supercube(BooleanCube other) {
        assert this.nVars == other.nVars;
        assert this.order == other.order;
        BooleanCube cube = new BooleanCube(this);
        cube.bits.or(other.bits);
        if (!isValid()) {
            return null;
        }
        return cube;
    }

    /** Returns true if cube {@code other} is fully contained in {@Code this} cube. */
    public boolean includes(BooleanCube other) {
        try {
            return this.intersection(other).equals(other);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /** The non-disjoint sharp function.  It may return more than one BooleanCube. */
    public Set<BooleanCube> disjointSharp(BooleanCube other) {
        assert nVars == other.nVars;
        Set<BooleanCube> result = new HashSet<>();

        BooleanCube masterIntersection = this.intersection(other);
        if (masterIntersection == null) {
            return null;
        }

        for (int i = 0; i < nVars; ++i) {
            int idx = N_BITS_PER_VAR * i;

            // Extract bits from this.
            BitSet first = this.bits.get(idx, idx + 2);
            BitSet second = other.bits.get(idx, idx + 2);

            BitSet common = (BitSet) first.clone();
            common.and(second);

            if (!common.equals(first)) {
                common.flip(0, 2);
                BooleanCube cube = new BooleanCube(masterIntersection);
                cube.setBitValue(idx, common.get(0));
                cube.setBitValue(idx+1, common.get(1));
                result.add(cube);
            }
        }

        return result.isEmpty() ? null : result;
    }

    /** The non-disjoint sharp function.  It may return more than one BooleanCube. */
    public Set<BooleanCube> nonDisjointSharp(BooleanCube other) {
        BooleanCube intersection = this.intersection(other);
        if (intersection == null) {
            Set<BooleanCube> result = new HashSet<>();
            result.add(this);
            return result;
        } else if (intersection.equals(this)) {
            return null;
        } else {
            return basicSharp(other);
        }
    }

    /** Returns the number of literals in the ciube. */
    public int cost() {
        return nVars - order;
    }

    /** Compares the cost of two cubes. */
    public boolean isCostlierThan(BooleanCube other) {
        return order < other.order;
    }

    // PUBLIC ACCESS METHODS.

    public int getOrder() {
        return order;
    }

    public int getNumberOfVariables() {
        return nVars;
    }

    // PUBLIC STRING REPRESENTATIONS.


    /** Numeric representation, with "1" for ON, "0" for OFF and "*" for DONTCARE. **/
    public String toNumericString() {
        return "\\bitp{" + toPlainNumericString() + "}";
    }

    public String toPlainNumericString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nVars; ++i) {
            String v = getVar(i);
            sb.append(v);
        }
        return sb.toString();
    }

    /** Short algebraic representation, suitable for typesetting in LaTeX. **/
    public String toAlgebraicString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nVars; ++i) {
            String v = getVar(i);
            if (v.equals("1")) {
                sb.append(String.format("x_{%d}", i+1));
            } else if (v.equals("0")) {
                sb.append(String.format("\\bar{x_{%d}}", i+1));
            }
        }
        return sb.toString();
    }

    /** Combination of numeric and algebraic representations, suitable for typesetting in LaTeX. */
    public String toString() {
        // return  toAlgebraicString() + " (\\bitp{" + toNumericString() + "})" ;
        return toNumericString();
    }


    // PUBLIC OVERRIDDEN METHODS SO THAT THIS MAY BE USED IN COMMON DATA STRUCTURES.

    @Override
    public int compareTo(Object o) {
        BooleanCube c = (BooleanCube) o;
        return this.toNumericString().compareTo(c.toNumericString());
    }

    @Override
    public boolean equals(Object other) {
        return (other != null) && (other instanceof BooleanCube) &&
                (this.toNumericString().equals(((BooleanCube) other).toNumericString()));
    }

    @Override
    public int hashCode() {
        return this.toNumericString().hashCode();
    }

    // PRIVATE METHODS

    /** Constructs a BooleanCube with all OFFs. */
    private BooleanCube(int nVars) {
        this.nVars = nVars;
        bits = new BitSet(N_BITS_PER_VAR * nVars);
        order = 0;
    }


    /** Constructs a BooleanCube from the lower size variables of a bitset. */
    private BooleanCube(int nVars, BitSet bits) {
        this.nVars = nVars;
        this.bits = (BitSet) bits.clone();
        calcOrder();
    }

    private void setBitValue(int idx, boolean b) {
        this.bits.set(idx, b);
    }

    /**
     * Computes the basic sharp between two cubes.
     * NOTE:  This has two problems:
     *   (1) It handles only 2-bite per var case.
     *   (2) It is not efficient.
     * @param other The other cube
     * @return Set of cubes as the result of the sharp operation.
     */
    private Set<BooleanCube> basicSharp(BooleanCube other) {
        assert nVars == other.nVars;
        Set<BooleanCube> result = new HashSet<>();

        for (int i = 0; i < nVars; ++i) {
            int idx = N_BITS_PER_VAR * i;

            // Extract bits from this.
            BitSet first = this.bits.get(idx, idx + 2);
            BitSet second = other.bits.get(idx, idx + 2);

            BitSet common = (BitSet) first.clone();
            common.and(second);

            if (!common.equals(first)) {
                common.flip(0, 2);
                BooleanCube cube = new BooleanCube(this);
                cube.setBitValue(idx, common.get(0));
                cube.setBitValue(idx+1, common.get(1));
                result.add(cube);
            }
        }

        return result.isEmpty() ? null : result;
    }

    /** Sets the member variable order to the number of dontcare variables. */
    private void calcOrder() {
        int count = 0;
        for (int i = 0; i < nVars; ++i) {
            if (isDontCareVar(i)) {
                ++count;
            }
        }
        order = count;
    }

    private boolean isValid() {
        for (int i = 0; i < nVars; ++i) {
            if (getVar(i) == ERROR) {
                return false;
            }
        }
        return true;
    }

    // Returns the numeric value of minterms, -1 for others.
    public int numericValue() {
        if (!isMinterm()) {
            return -1;
        }

        int value = 0;
        for (int i = 0; i < nVars; ++i) {
            value <<= 1;
            if (this.isOnVar(i)) {
                value += 1;
            }
        }
        return value;
    }

    private boolean isMinterm() {
        return order == 0;
    }
}
