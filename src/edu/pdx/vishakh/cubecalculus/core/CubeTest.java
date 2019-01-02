package edu.pdx.vishakh.cubecalculus.core;

import java.io.PrintStream;
import java.util.Set;

public class CubeTest {

    public static void main(String[] args) {
        boolean latex = false;

        Minterm m7 = new Minterm("0111");
        Minterm m6 = new Minterm("0110");

        BooleanCube c7 = BooleanCube.fromMinterm(m7);
        BooleanCube c6 = BooleanCube.fromMinterm(m6);

        TestUtils.assertStringEquals("0111", c7.toPlainNumericString(), "c7.toPlainNumericString()");
        TestUtils.assertStringEquals("\\bitp{0111}", c7.toNumericString(), "c7.toNumericString()");
        TestUtils.assertStringEquals("\\bar{x_{1}}x_{2}x_{3}x_{4}", c7.toAlgebraicString(), "c7.toAlgebraicString()");

        TestUtils.assertStringEquals("0110", c6.toPlainNumericString(), "c6.toPlainNumericString()");
        TestUtils.assertStringEquals("\\bitp{0110}", c6.toNumericString(), "c6.toNumericString()");
        TestUtils.assertStringEquals("\\bar{x_{1}}x_{2}x_{3}\\bar{x_{4}}", c6.toAlgebraicString(), "c6.toAlgebraicString()");

        TestUtils.assertNull(c7.intersection(c6), "c7 * c6");
        TestUtils.assertStringEquals("011*", c7.supercube(c6).toPlainNumericString(), "c7.supercube(c6)");
        TestUtils.assertStringEquals("011*", c6.supercube(c7).toPlainNumericString(), "c6.supercube(c7)");


        PrintStream out = System.out;

        if (latex) {
            TestUtils.printLaTeXHeader(out, "Testing BooleanCube");
        }
        testComplements(out, latex);
        testIntersections(out);
        testSuperCubes(out);
        testIncludes(out);
        testSharps(out);
        testMerges(out);
        testNumericValue();
        TestUtils.printLaTeXFooter(out);
    }

    private static void testMerges(PrintStream out) {
        String[][] mergeExamples = {
                {"1011", "1001", "1021"},

        };

        TestUtils.printLaTeXTestHeader(out, "Testing Merge");
        for (String[] data : mergeExamples) {
            testMerge(out, data[0], data[1], data[2]);
        }
        TestUtils.printLaTeXTestFooter(out);
    }

    private static void testMerge(PrintStream out, String x, String y, String expectedResult) {
        BooleanCube xCube = BooleanCube.fromString(x);
        BooleanCube yCube = BooleanCube.fromString(y);
        BooleanCube merged = xCube.merge(yCube);
        BooleanCube expCube = BooleanCube.fromString(expectedResult);
        String message = String.format("merge($%s$, $%s$)", xCube, yCube);
        TestUtils.assertCubeEqualsLaTeX(expCube, merged, out, message);
    }

    private static void testSharps(PrintStream out) {
        String[][] sharpExamples = {
                {"**0*", "*1*1"},
                {"1111", "0101"},
                {"0**1", "00*1"},
                {"01*1", "**01"},
                {"0111", "*1*1"}
        };

        TestUtils.printLaTeXTestHeader(out, "Testing Sharp");
        for (String[] data : sharpExamples) {
            testSharp(out, data[0], data[1]);
        }

        TestUtils.printLaTeXTestFooter(out);

    }


    private static void testSharp(PrintStream out, String x, String y) {
        BooleanCube xCube = BooleanCube.fromString(x);
        BooleanCube yCube = BooleanCube.fromString(y);
        Set<BooleanCube> nSharp = xCube.nonDisjointSharp(yCube);
        Set<BooleanCube> dSharp = xCube.disjointSharp(yCube);

        System.out.printf(" & $%s\\,\\,\\texttt{n\\#}\\,\\,%s = %s$\\\\\n", xCube, yCube, nSharp);
        System.out.printf(" & $%s\\,\\,\\texttt{d\\#}\\,\\,%s = %s$\\\\\n", xCube, yCube, dSharp);

    }

    private static void testComplements(PrintStream out, boolean latex) {
        String[][] complementExamples = {
                {"110", "001"},
                {"11*", "ERROR"}
        };

        if (latex) {
            TestUtils.printLaTeXTestHeader(out, "Testing Complement");
        }

        testComplement(out, "110", "001", latex);
        testComplement(out, "11*", null, latex);

        if (latex) {
            TestUtils.printLaTeXTestFooter(out);
        }

    }

    private static void testComplement(PrintStream out, String x, String y, boolean latex) {
        BooleanCube xCube = BooleanCube.fromString(x);
        BooleanCube yCube = null;
        if (y != null) {
            yCube = BooleanCube.fromString(y);
        }
        BooleanCube complement = xCube.complement();
        if (y == null) {
            TestUtils.assertNull(complement, "Complement of " + x);
        } else {
            if (latex) {
                String message = String.format("$A = %s, \\bar{A}$", xCube.toString());
                TestUtils.assertCubeEqualsLaTeX(yCube, complement, out, message);
            } else {
                String message = String.format("Complement of " + xCube.toPlainNumericString());
                TestUtils.assertCubeEquals(yCube, complement, out, message);

            }
        }

    }

    private static void testSuperCubes(PrintStream out) {
        String[][] superCubeExamples = {
                {"11*", "*10", "*1*"},
                {"0*", "*1", "**"}

        };

        TestUtils.printLaTeXTestHeader(out, "Testing Supercube");
        for (String[] data : superCubeExamples) {
            testSuperCube(out, data[0], data[1], data[2]);
        }


        TestUtils.printLaTeXTestFooter(out);
    }

    private static void testSuperCube(PrintStream out, String x, String y, String expectedResult) {
        BooleanCube xCube = BooleanCube.fromString(x);
        BooleanCube yCube = BooleanCube.fromString(y);
        BooleanCube expCube = BooleanCube.fromString(expectedResult);
        BooleanCube superCube = xCube.supercube(yCube);
        String message = String.format("$%s \\cup %s$", xCube.toString(), yCube.toString());
        TestUtils.assertCubeEqualsLaTeX(expCube, superCube, out, message);

    }

    private static void testIntersections(PrintStream out) {
        String[][] intersectionExamples = {
                {"11*", "*10", "110"},
                {"01**", "01*0", "01*0"},
                {"1*1*", "0*1*", "ERROR"},
                {"0*", "*1", "01"}

        };

        TestUtils.printLaTeXTestHeader(out, "Testing Intersection");
        for (String[] data : intersectionExamples) {
            testIntersection(out, data[0], data[1], data[2]);
        }


        TestUtils.printLaTeXTestFooter(out);
    }

    private static void testIntersection(PrintStream out, String x, String y, String expIntersection) {
        BooleanCube xCube = BooleanCube.fromString(x);
        BooleanCube yCube = BooleanCube.fromString(y);
        BooleanCube expCube = BooleanCube.fromString(expIntersection);
        BooleanCube intCube = xCube.intersection(yCube);
        String message = String.format("$%s \\cap %s$", xCube.toString(), yCube.toString());
        TestUtils.assertCubeEqualsLaTeX(expCube, intCube, out, message);
    }

    private static void testIncludes(PrintStream out) {
        String[][] includesExamples = {
                {"01**", "010*"}
        };

        String[][] doesNotIncludeExamples = {
                {"010*", "011*"}
        };

        TestUtils.printLaTeXTestHeader(out, "Testing includes");

        for (String[] data : includesExamples) {
            BooleanCube a = BooleanCube.fromString(data[0]);
            BooleanCube b = BooleanCube.fromString(data[1]);
            if (a.includes(b)) {
                System.out.printf("\\texttt{PASS} & $%s \\le %s$ = true\\\\\n", b.toString(), a.toString());
            } else {
                System.out.printf("\\texttt{FAIL} & $%s \\le %s$ : Expected = true, Actual = false\\\\\n", b.toString(), a.toString());
            }
        }

        for (String[] data : doesNotIncludeExamples) {
            BooleanCube a = BooleanCube.fromString(data[0]);
            BooleanCube b = BooleanCube.fromString(data[1]);
            if (a.includes(b)) {
                System.out.printf("\\texttt{FAIL} & $%s \\le %s$ : Expected = false, Actual = true\\\\\n", b.toString(), a.toString());
            } else {
                System.out.printf("\\texttt{PASS} & $%s \\le %s$ = false\\\\\n", b.toString(), a.toString());
            }
        }

        TestUtils.printLaTeXTestFooter(out);

    }

    public static void testNumericValue() {
        int nVars = 5;
        int[] inputs = {3, 7, 13, 26};

        TestUtils.printLaTeXTestHeader(System.out, "Testing Numeric value");

        for (int input : inputs) {
            Minterm m = new Minterm(nVars, input);
            BooleanCube c = BooleanCube.fromMinterm(m);
            int output = c.numericValue();
            if (output == input) {
                System.out.printf("\\texttt{PASS} & %d\\\\\n", input);
            } else {
                System.out.printf("\\texttt{FAIL} & Expected = %d, Actual = %d\\\\\n", input, output);
            }
        }
        TestUtils.printLaTeXTestFooter(System.out);
    }

}
