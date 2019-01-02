package edu.pdx.vishakh.cubecalculus.tools;

import java.util.*;

/**
 * Generates an ordered list of gray codes of a given length.
 * The main use is in Karnaugh maps, where the minterms are listed in the gray code order.
 *
 * Uses a recursive algorithm that builds an n-long gray code from an (n-1)-long gray code.
 * Computed gray codes are cached so that they need not be computed next time.
 */
public class GrayCodeGenerator {

    /** Singleton instance for public interface. */
    private static final GrayCodeGenerator GENERATOR = new GrayCodeGenerator();

    /** Map of length to the list of gray codes.
     * Gray codes are cached this way for efficiency.
     */
    private Map<Integer, List<String>> grayCodeMap = new HashMap<>();

    /** Public function to generate gray codes for a given length.
     *
     * @param length Length of the gray code.
     * @return An ordered list (of length 2^{length}) of Strings (of length {@Code length} each) that lists all
     * the permutations of {@Code length} number of 1s and 0s in the gray code order.
     */
    public static List<String> generate(int length) {
        return GENERATOR.generateFor(length);
    }

    private List<String> generateFor(int length) {
        List<String> result;
        if (length < 1) {
            result =  null;
        } else if (grayCodeMap.containsKey(length)) {
            result = grayCodeMap.get(length);
        } else {
            // Recursive call.
            List<String> prevList = generateFor(length-1);

            result = new ArrayList<>();

            for (int i = 0; i < prevList.size(); ++i) {
                result.add("0" + prevList.get(i));
            }

            for (int i = prevList.size() - 1; i >= 0; --i) {
                result.add("1" + prevList.get(i));
            }

            grayCodeMap.put(length, result);
        }
        return result;
    }

    private GrayCodeGenerator() {
        List<String> forOne = new ArrayList<>();
        forOne.add("0");
        forOne.add("1");
        grayCodeMap.put(1, forOne);
    }

    // Tests.
    public static void main(String[] args) {
        testGrayCodeGenerator(1,
                new String[]{
                        "0", "1"
                });
        testGrayCodeGenerator(2,
                new String[]{
                        "00", "01", "11", "10"
                });
        testGrayCodeGenerator(3,
                new String[]{
                        "000", "001", "011", "010", "110", "111", "101", "100"
                });
        testGrayCodeGenerator(4,
                new String[]{
                        "0000", "0001", "0011", "0010", "0110", "0111",
                        "0101", "0100", "1100", "1101", "1111", "1110",
                        "1010", "1011", "1001", "1000"
                });
        testGrayCodeGenerator(5,
                new String[]{
                        "00000", "00001", "00011", "00010", "00110", "00111", "00101", "00100",
                        "01100", "01101", "01111", "01110", "01010", "01011", "01001", "01000",
                        "11000", "11001", "11011", "11010", "11110", "11111", "11101", "11100",
                        "10100", "10101", "10111", "10110", "10010", "10011", "10001", "10000"
                });
    }

    private static void testGrayCodeGenerator(int nVars, String[] grayCodes) {
        List<String> actualGrayCodes = GrayCodeGenerator.generate(nVars);
        assert actualGrayCodes.size() == grayCodes.length;

        int idx = 0;
        for (String gc : actualGrayCodes) {
            assert gc.equals(grayCodes[idx]);
            ++idx;
        }
    }

}
