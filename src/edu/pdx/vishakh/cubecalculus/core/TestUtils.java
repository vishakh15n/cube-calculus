package edu.pdx.vishakh.cubecalculus.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

public class TestUtils {
    /** Returns thhe full path of an output test file. */
    public static String getTestFilePath(String fileName) {
        return System.getProperty("user.home") + "/github-projects/cubecalculus/docs/tests/" + fileName;
        // return fileName;
    }

    public static String getDataFilePath(String fileName) {
        return System.getProperty("user.home") + "/github-projects/cubecalculus/docs/data/" + fileName;
    }

    public static PrintStream createPrintStream(String testName) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new FileOutputStream(getTestFilePath(testName) + ".tex"));
        return ps;
    }

    public static void printLaTeXHeader(PrintStream out, String name) {
        out.println("\\documentclass{scrartcl}");
        out.println("\\usepackage{amsmath}");
        out.println("\\input{ml-macros}");
        out.println("\\title{" + name + "}");
        out.println("\\author{Vishakh Nair}");
        out.println("\\date{Test run on \\today}");
        out.println("\\begin{document}");
        out.println("\\maketitle");
    }

    public static void printLaTeXTestHeader(PrintStream out, String name) {
        out.println("\\section{" + name + "}");
        out.println("\\begin{tabular}{ll}\\\\");
        out.println("\\hline");
        out.println("\\textbf{Result} & \\textbf{Details} \\\\");
        out.println("\\hline");
    }
    public static void printLaTeXTestFooter(PrintStream out) {
        out.println("\\hline");
        out.println("\\end{tabular}");
    }

    public static void printLaTeXFooter(PrintStream out) {
        out.println("\\end{document}");
    }

    // Asserts whether two strings are equal and displays the result on standard out.
    public static void assertStringEquals(String exp, String act, String message) {
        if (exp.equals(act)) {
            System.out.printf("PASS: %s: %s\n", message, act);
        } else {
            System.out.printf("FAIL: %s: exp = %s, act = %s\n", message, exp, act);
        }
    }

    public static void assertCubeEquals(BooleanCube expectedCube, BooleanCube actualCube, PrintStream out, String message) {
        if (expectedCube == null && actualCube == null) {
            out.printf("PASS: %s = %s$\n", message, "null");
        }
        else if (actualCube == null) {
            out.printf("FAIL: %s : Expected = %s, Actual = null\n", message, expectedCube);
        }

        else if (expectedCube.equals(actualCube)) {
            out.printf("PASS: %s = %s\n", message, actualCube.toPlainNumericString());
        } else {
            out.printf("FAIL: %s: Expected = %s, Actual = %s\n", message, expectedCube.toPlainNumericString(), actualCube.toPlainNumericString());
        }

    }

    public static void assertCubeEqualsLaTeX(BooleanCube expectedCube, BooleanCube actualCube, PrintStream out, String message) {
        if (expectedCube == null && actualCube == null) {
            out.printf("\\texttt{PASS} &  %s = $%s$\\\\\n", message, "\\phi");
        }
        else if (expectedCube == null || actualCube == null) {
            out.printf("\\texttt{FAIL} & %s : Expected = $%s$, Actual = $%s$\\\\\n", message, expectedCube, actualCube);
        }

        else if (expectedCube.toNumericString().equals(actualCube.toNumericString())) {
            out.printf("\\texttt{PASS} &  %s = $%s$\\\\\n", message, actualCube.toString());
        } else {
            out.printf("\\texttt{FAIL} & %s : Expected = $%s$, Actual = $%s$\\\\\n", message, expectedCube.toString(), actualCube.toString());
        }
    }

    public static void printCubes(PrintStream out, Set<BooleanCube> cubes, String caption) {
        printCustomCubes(out, cubes, caption, "Cube");
    }


    public static void printCustomCubes(PrintStream out, Set<BooleanCube> cubes, String caption, String cubeCaption) {
        if (out == null) return;

        out.println("\\begin{table}[H]");
        out.println("\\centering");
        out.println("\\begin{tabular}{|r|c|c|r|}");
        out.println("\\hline");
        out.printf("\\textbf{No.} & \\multicolumn{2}{c|}{\\textbf{%s}} & \\textbf{Cost} \\\\\n", cubeCaption);
        out.println("\\hline");
        int no = 0;
        int cost = 0;
        for (BooleanCube c : cubes) {
            ++no;
            out.printf("%d & \\multicolumn{1}{c}{$%s$} & (\\bitp{%s}) & %d \\\\\n", no, c.toAlgebraicString(), c.toNumericString(), c.cost());
            cost += c.cost();
        }
        out.println("\\hline");
        out.printf("\\multicolumn{4}{|c|}{Cost = %d}\\\\\n", cost);
        out.println("\\hline");
        out.println("\\end{tabular}");
        out.printf("\\caption{%s}\n", caption);
        out.println("\\end{table}\n");
    }

    public static void printOnAndDcCubes(PrintStream out, Set<BooleanCube> onCubes, Set<BooleanCube> dcCubes, String caption) {
        if (out == null) return;

        out.println("\\begin{table}[H]");
        out.println("\\centering");
        out.println("\\begin{tabular}{|r|c|c|r|}");
        out.println("\\hline");
        int no = 0;
        int cost = 0;
        if (onCubes != null) {
            out.println("\\textbf{No.} & \\multicolumn{2}{c|}{\\textbf{ON cubes}} & \\textbf{Cost} \\\\");
            out.println("\\hline");
            for (BooleanCube c : onCubes) {
                ++no;
                out.printf("%d & \\multicolumn{1}{c}{$%s$} & (\\bitp{%s}) & %d \\\\\n", no, c.toAlgebraicString(), c.toNumericString(), c.cost());
                cost += c.cost();
            }
            out.println("\\hline");
            out.printf("\\multicolumn{4}{|c|}{Cost = %d}\\\\\n", cost);
            out.println("\\hline");
        }
        if (dcCubes != null) {
            out.println("\\textbf{No.} & \\multicolumn{2}{c|}{\\textbf{DC cubes}} & \\textbf{Cost} \\\\");
            out.println("\\hline");
            for (BooleanCube c : dcCubes) {
                ++no;
                out.printf("%d & \\multicolumn{1}{c}{$%s$} & (\\bitp{%s}) & %d \\\\\n", no, c.toAlgebraicString(), c.toNumericString(), c.cost());
            }
        }
        out.println("\\hline");
        out.println("\\end{tabular}");
        out.printf("\\caption{%s}\n", caption);
        out.println("\\end{table}\n");
    }

    public static void assertNull(Object o, String message) {
        if (o == null) {
            System.out.printf("PASS: %s: is null as expected.\n", message);
        } else {
            System.out.printf("FAIL: %s: Expected = null, Actual = not null\n", message);
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (condition) {
            System.out.printf("PASS: %s: is true as expected.\n", message);
        } else {
            System.out.printf("FAIL: %s: Expected = true, Actual = false\n", message);
        }
    }

    public static void assertNotTrue(boolean condition, String message) {
        if (!condition) {
            System.out.printf("PASS: %s: is false as expected.\n", message);
        } else {
            System.out.printf("FAIL: %s: Expected = false, Actual = true\n", message);
        }

    }
}
